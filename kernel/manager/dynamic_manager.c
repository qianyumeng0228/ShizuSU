// SPDX-License-Identifier: GPL-2.0
/*
 * [自研] ShizuSU 动态管理器热注册通道（补丁 1）。
 *
 * 设计依据（清单 1.3 / 设计文档 D2）：
 *   - 上游 ReSukiSU main@648e598 无运行时注册命令、无持久化（核验 resukisu.md 第 3 节）。
 *   - 本文件为 ShizuSU 自研：提供 KSU_IOCTL_DYNAMIC_MANAGER_SET('K',107) 热注册通道，
 *     权限收紧为 uid==0 || is_manager()（dispatch.c 表项 perm_check=manager_or_root），
 *     注册成功后持久化包名到 /data/adb/shizusu/manager，并在 POST_FS_DATA 读回重新加冕。
 *   - 加冕本身复用 throne_tracker 的 ksu_set_manager_appid()；allowlist 修剪复用 track_throne(false)；
 *     卸载检测复用 track_throne 既有「当前管理器不在已安装列表则 invalidate」分支（清单 1.3 边界）。
 */
#include <linux/cred.h>
#include <linux/err.h>
#include <linux/fs.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/types.h>
#include <linux/uaccess.h>
#include <linux/version.h>

#include "ksu.h" // ksu_cred
#include "klog.h" // IWYU pragma: keep
#include "uapi/supercall.h" // struct ksu_dynamic_manager_set_cmd
#include "manager/apk_sign.h" // is_manager_apk / get_pkg_from_apk_path
#include "manager/manager_identity.h" // ksu_set_manager_appid / KSU_INVALID_APPID
#include "manager/throne_tracker.h" // track_throne
#include "dynamic_manager.h"

#define SYSTEM_PACKAGES_LIST_PATH "/data/system/packages.list"

/* [自研] 持久化文件：root-only 目录 /data/adb/shizusu（0700），文件 0644。
 * 内容 = 被注册管理器的包名（单行文本）。设计决策：存包名而非 uid——uid 随重建/多用户可能变，
 *   包名稳定；恢复时再按包名查 packages.list 得到当前 uid。 */
#define SHIZUSU_DIR "/data/adb/shizusu"
#define SHIZUSU_MANAGER_FILE "/data/adb/shizusu/manager"

/*
 * [自研] 按包名在 /data/system/packages.list 中查 uid。
 * 解析逻辑照搬 throne_tracker.c:269-309 的逐行 strsep 方式（只读，独立实现以避免改动 throne_tracker.c）。
 * 命中返回 uid，未命中返回 KSU_INVALID_APPID。
 */
static u32 lookup_uid_by_package(const char *pkg)
{
	const struct cred *old_cred = override_creds(ksu_cred);
	struct file *fp = filp_open(SYSTEM_PACKAGES_LIST_PATH, O_RDONLY, 0);
	u32 result = KSU_INVALID_APPID;

	if (IS_ERR(fp)) {
		pr_err("dynamic_manager: open packages.list failed: %ld\n", PTR_ERR(fp));
		goto out;
	}

	char chr = 0;
	loff_t pos = 0;
	loff_t line_start = 0;
	char buf[KSU_MAX_PACKAGE_NAME];
	for (;;) {
		ssize_t count = kernel_read(fp, &chr, sizeof(chr), &pos);
		if (count != sizeof(chr))
			break;
		if (chr != '\n')
			continue;

		count = kernel_read(fp, buf, sizeof(buf) - 1, &line_start);
		if (count <= 0)
			break;
		buf[count] = '\0';

		char *tmp = buf;
		const char *delim = " ";
		char *package = strsep(&tmp, delim);
		char *uid = strsep(&tmp, delim);
		if (!uid || !package)
			break;

		if (strncmp(package, pkg, KSU_MAX_PACKAGE_NAME) == 0) {
			u32 res;
			if (kstrtou32(uid, 10, &res) == 0) {
				result = res;
			}
			break;
		}
		line_start = pos;
	}
	filp_close(fp, 0);
out:
	revert_creds(old_cred);
	return result;
}

/*
 * [自研] 写持久化文件。读写模式参考基线 policy/allowlist.c:419-424
 *   （override_creds(ksu_cred) + filp_open(O_WRONLY|O_CREAT|O_TRUNC, 0644) + kernel_write）。
 * /data/adb/shizusu 目录不存在时 best-effort 创建（0700 root-only）。
 */
static void persist_manager_package(const char *pkg)
{
	const struct cred *old_cred = override_creds(ksu_cred);
	struct file *fp;
	loff_t off = 0;
	size_t len = strlen(pkg);

	/* best-effort 建目录；已存在返回 -EEXIST，忽略。待实测：ksys_mkdir 导出符号。 */
	/* FIX(Phase1): ksys_mkdir/ksys_mkdirat not exported in GKI 5.15/6.1; dir precreated by ksud. */

	fp = filp_open(SHIZUSU_MANAGER_FILE, O_WRONLY | O_CREAT | O_TRUNC, 0644);
	if (IS_ERR(fp)) {
		pr_err("dynamic_manager: persist open failed: %ld (pkg=%s)\n", PTR_ERR(fp), pkg);
		goto out;
	}
	if (kernel_write(fp, pkg, len, &off) != len) {
		pr_err("dynamic_manager: persist write failed (pkg=%s)\n", pkg);
	}
	filp_close(fp, 0);
out:
	revert_creds(old_cred);
}

/*
 * [自研] IOCTL 'K',107 handler。
 * 流程：拷参数 -> 校验 APK 命中签名表(is_manager_apk) -> 取包名 -> 查 uid ->
 *   ksu_set_manager_appid 加冕 -> 持久化包名 -> track_throne(false) 修剪 allowlist。
 * 卸载检测无需额外代码：track_throne(false) 走基线既有分支，若加冕管理器被卸载则自动
 *   ksu_invalidate_manager_uid() 并重新扫描（throne_tracker.c:328-337）。
 */
int do_dynamic_manager_set(void __user *arg)
{
	struct ksu_dynamic_manager_set_cmd cmd;
	char path[PATH_MAX];
	char pkg[KSU_MAX_PACKAGE_NAME];
	u32 uid;
	int ret = 0;

	if (copy_from_user(&cmd, arg, sizeof(cmd)))
		return -EFAULT;
	if (!cmd.path)
		return -EINVAL;

	if (strncpy_from_user(path, (const char __user *)cmd.path, sizeof(path)) < 0)
		return -EFAULT;
	path[sizeof(path) - 1] = '\0';

	/* 信任根：APK 必须命中多签名表（任一表中签名）。 */
	if (!is_manager_apk(path)) {
		pr_err("dynamic_manager: %s not a signed manager apk\n", path);
		return -EACCES;
	}

	if (get_pkg_from_apk_path(pkg, path) < 0) {
		pr_err("dynamic_manager: cannot parse package from %s\n", path);
		return -EINVAL;
	}

	uid = lookup_uid_by_package(pkg);
	if (uid == KSU_INVALID_APPID) {
		pr_err("dynamic_manager: package %s not found in packages.list\n", pkg);
		return -EINVAL;
	}

	pr_info("dynamic_manager: crowning %s (uid=%u)\n", pkg, uid);
	ksu_set_manager_appid(uid);

	persist_manager_package(pkg);

	/* 同步修剪 allowlist：旧管理器残留授权随 uid 消失被清理（清单 1.3 第 2 步）。 */
	track_throne(false);
	return ret;
}

/*
 * [自研] POST_FS_DATA 持久化恢复：读回包名 -> 查 uid -> 加冕。
 * 载入点挂在 dispatch.c do_report_event() 的 EVENT_POST_FS_DATA 分支（on_post_fs_data() 之后）。
 * 时序说明（T5 / L3 定案）：简单恢复，不做额外时序对齐；packages.list 此刻可能未完全就绪，
 *   查不到则跳过，后续 packages.list 变更触发 track_throne(false) 时仍会重新加冕。
 */
void ksu_dynamic_manager_load(void)
{
	const struct cred *old_cred = override_creds(ksu_cred);
	struct file *fp;
	char pkg[KSU_MAX_PACKAGE_NAME];
	loff_t off = 0;
	ssize_t n;
	u32 uid;

	fp = filp_open(SHIZUSU_MANAGER_FILE, O_RDONLY, 0);
	if (IS_ERR(fp)) {
		pr_info("dynamic_manager: no persisted manager (%ld)\n", PTR_ERR(fp));
		goto out;
	}

	n = kernel_read(fp, pkg, sizeof(pkg) - 1, &off);
	filp_close(fp, 0);
	if (n <= 0) {
		pr_info("dynamic_manager: persisted manager file empty\n");
		goto out;
	}
	pkg[n] = '\0';
	/* 去掉尾部换行/空白 */
	while (n > 0 && (pkg[n - 1] == '\n' || pkg[n - 1] == '\r' || pkg[n - 1] == ' '))
		pkg[--n] = '\0';

	uid = lookup_uid_by_package(pkg);
	if (uid == KSU_INVALID_APPID) {
		pr_info("dynamic_manager: persisted pkg %s not installed, skip crowning\n", pkg);
		goto out;
	}

	pr_info("dynamic_manager: restore crowned manager %s (uid=%u)\n", pkg, uid);
	ksu_set_manager_appid(uid);
out:
	revert_creds(old_cred);
}
