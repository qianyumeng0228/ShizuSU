/* SPDX-License-Identifier: GPL-2.0 */
/*
 * 隐身模式(stealth) 内核接口 —— 照搬 7kimisu kernel/manager/stealth.h:1-11,
 * ShizuSU 仅追加 CONFIG_KSU_STEALTH=n 时的内联桩（[自研]，见本文件尾部）。
 *
 * 上游来源：github.com/hartgerinktarcila-design/7kimisu @ v2.29 (0246ca0)
 *   kernel/manager/stealth.h
 */
#ifndef __KSU_H_STEALTH
#define __KSU_H_STEALTH

#include <linux/types.h>
#include <linux/errno.h>

#ifdef CONFIG_KSU_STEALTH
void ksu_stealth_load(void);
bool ksu_stealth_is_enabled(void);
int ksu_stealth_set(bool enabled);
#else
/*
 * [自研] CONFIG_KSU_STEALTH=n 时的内联桩。
 *
 * 目的：do_get_info() 的门控 `is_manager() && !ksu_stealth_is_enabled()` 在隐身功能被
 * 裁剪时仍能无条件编译、且行为退化为「恒不隐身」（is_enabled 恒 false）。这样内核不必
 * 在调用点再套一层 #ifdef，最小 diff。stealth.o 此时不编入（见 kernel/Kbuild）。
 */
static inline void ksu_stealth_load(void) {}
static inline bool ksu_stealth_is_enabled(void) { return false; }
static inline int ksu_stealth_set(bool enabled)
{
	(void)enabled;
	return -ENODEV;
}
#endif // CONFIG_KSU_STEALTH

#endif // __KSU_H_STEALTH
