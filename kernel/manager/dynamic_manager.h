#ifndef __KSU_H_DYNAMIC_MANAGER
#define __KSU_H_DYNAMIC_MANAGER

#include <linux/types.h>
#include <linux/uaccess.h>

/*
 * [自研] ShizuSU 动态管理器热注册通道（补丁 1）。
 * 上游 ReSukiSU main@648e598 无此实现（核验笔记 resukisu.md 第 3 节 / 清单 D2：
 *   上游仅靠扫描 /data/app 自动加冕，无运行时注册命令、无持久化）。
 * 本通道由设计文档批准：权限收紧（仅 root 或已认主管理器可注册）+ 持久化 + track_throne 同步。
 */

#if defined(CONFIG_KSU_DYNAMIC_MANAGER) && !defined(CONFIG_KSU_DISABLE_MANAGER)

/* IOCTL 'K',107：把指定 APK 注册为新管理器。
 * arg 指向 struct ksu_dynamic_manager_set_cmd（见 uapi/supercall.h）。
 * 权限由 dispatch.c 命令表 perm_check=manager_or_root 兜底（uid==0 || is_manager()）。 */
int do_dynamic_manager_set(void __user *arg);

/* POST_FS_DATA 阶段读回 /data/adb/shizusu/manager 并重新加冕（持久化恢复）。 */
void ksu_dynamic_manager_load(void);

#else
/* 未启用或禁用管理器时：提供空桩，避免 dispatch.c 链接失败。 */
static inline int do_dynamic_manager_set(void __user *arg)
{
	(void)arg;
	return -EOPNOTSUPP;
}

static inline void ksu_dynamic_manager_load(void)
{
}
#endif

#endif // __KSU_H_DYNAMIC_MANAGER
