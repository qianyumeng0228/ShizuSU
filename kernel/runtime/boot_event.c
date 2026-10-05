#include "feature/selinux_hide.h"
#include <linux/err.h>
#include <linux/fs.h>
#include <linux/namei.h>
#include <linux/printk.h>

#include "policy/allowlist.h"
#include "klog.h" // IWYU pragma: keep
#include "runtime/ksud_boot.h"
#include "runtime/ksud.h"
#include "manager/manager_observer.h"
#include "manager/throne_tracker.h"
#include "manager/stealth.h" // [ShizuSU 补丁2] ksu_stealth_load()

bool ksu_module_mounted __read_mostly = false;
bool ksu_boot_completed __read_mostly = false;

void on_post_fs_data(void)
{
	static bool done = false;

	if (done) {
		pr_info("on_post_fs_data already done\n");
		return;
	}

	done = true;
	pr_info("on_post_fs_data!\n");

	ksu_load_allow_list();
	/* [ShizuSU 补丁2] POST_FS_DATA 时载入隐身状态（文件不存在/未写 '1' = 默认关闭）。
	 * 落点对照：7kimisu 直接在 dispatch.c:117 do_report_event 的 EVENT_POST_FS_DATA
	 * 分支调 ksu_stealth_load()；ShizuSU 以自身 on_post_fs_data() 为统一落点
	 * （清单 2.1②：与 allowlist 同生命周期，/data/adb 此时已挂载）。stealth.h 在
	 * CONFIG_KSU_STEALTH=n 时提供内联空桩，故此处无条件调用即可。 */
	ksu_stealth_load();
	ksu_observer_init();
	// Sanity check for safe mode only needs early-boot input samples.
	ksu_stop_input_hook_runtime();
	ksu_selinux_hide_handle_post_fs_data();
}

extern void ext4_unregister_sysfs(struct super_block *sb);

int nuke_ext4_sysfs(const char *mnt)
{
	struct path path;
	int err = kern_path(mnt, 0, &path);

	if (err) {
		pr_err("nuke path err: %d\n", err);
		return err;
	}

	if (strcmp(path.dentry->d_inode->i_sb->s_type->name, "ext4") != 0) {
		pr_info("nuke but module aren't mounted\n");
		path_put(&path);
		return -EINVAL;
	}

	ext4_unregister_sysfs(path.dentry->d_inode->i_sb);
	path_put(&path);
	return 0;
}

void on_module_mounted(void)
{
	pr_info("on_module_mounted!\n");
	ksu_module_mounted = true;
}

void on_boot_completed(void)
{
	ksu_boot_completed = true;
	pr_info("on_boot_completed!\n");
	track_throne(true);
	ksu_selinux_hide_drop_backup_if_unused();
}
