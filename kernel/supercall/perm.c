#include <linux/types.h>

#include "supercall/internal.h"
#include "manager/manager_identity.h"
#include "policy/allowlist.h"

bool only_manager(void)
{
	return is_manager();
}

bool only_root(void)
{
	return current_uid().val == 0;
}

bool manager_or_root(void)
{
	return current_uid().val == 0 || is_manager();
}

bool always_allow(void)
{
	return true;
}

bool allowed_for_su(void)
{
	return is_manager() || ksu_is_allow_uid_for_current(current_uid().val);
}

/*
 * [ShizuSU 补丁2] stealth 安全阀 —— 照搬 7kimisu kernel/supercall/perm.c:70-81 的
 * 纯 uid 版，**剪掉第三判据**。
 *
 * 7kimisu 原实现（perm.c:70-81）三判据：
 *   1) current_uid().val == 0            （root，救砖）
 *   2) is_manager()                       （已认主管理器）
 *   3) ksu_is_signed_manager_uid(uid)     （签名候选，未必认主）
 *
 * ShizuSU **不学**第三判据所依赖的整套机制（清单 2.5 / 设计文档 D4 修正）：
 *   - `ksu_is_signed_manager_uid()` / `ksu_signed_manager_appid` 与 7kimisu 的
 *     「断代闸门」(KSU_MANAGER_MIN_GEN=2) 深度耦合，属明确不学的反模式；
 *   - 基线 SukiSU 根本没有该符号（kernel/manager/manager_identity.h 无此函数）。
 * 故仅保留前两条，第三判据位置直接 return false。
 *
 * 用途：首版 STEALTH_GET/SET 的 perm_check 仍是 only_manager（见 dispatch.c 命令表）；
 *   本 valve 预留给未来「未认主也可密令开关」的安全阀命令，首版不接线（清单 2.3④：可后置）。
 *
 * 注意：拨号密令（*#*#70707#*#*）、发送方可信判定（默认拨号器/预装系统应用）、
 *   连错锁30s 等**全部在 App 层** StealthReceiver.kt，内核侧不做、也不存任何密令常量
 *   （核验笔记 7kimisu.md §3 已确认内核 perm.c 无密令/拨号器逻辑）。
 */
bool stealth_valve_allowed(void)
{
	if (current_uid().val == 0) {
		return true;
	}

	if (is_manager()) {
		return true;
	}

	return false;
}
