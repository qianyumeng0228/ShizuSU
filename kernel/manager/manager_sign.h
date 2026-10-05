#ifndef __KSU_H_MANAGER_SIGN
#define __KSU_H_MANAGER_SIGN

/*
 * ShizuSU 多签名表 —— 管理器身份信任根（唯一签名源）。
 *
 * [自研] 组织形式：ReSukiSU(main @ 648e598) 不存在独立 manager_sign.h，签名表硬编码于
 *   kernel/manager/apk_sign.c:6-16。ShizuSU 将其抽为独立头文件，仅由 kernel/manager/apk_sign.c
 *   包含，便于 Kbuild 编译期注入 ShizuSU 自家管理器签名（见下方 KSU_MANAGER_EXTRA_* 段）。
 *
 * [照搬] 表内 6 条 size + sha256 数据逐字符来自 ReSukiSU kernel/manager/apk_sign.c:10-15
 *   (commit 648e598 快照；RKSU / 官方 KSU / WKSU / KowSU / KSUN / MKSU)。
 */

struct apk_sign_key {
	unsigned size;		/* 证书 DER 长度（先按长度粗筛） */
	const char *sha256;	/* 证书内容 SHA-256 小写 hex */
};

static const struct apk_sign_key apk_sign_keys[] = {
	{ 0x396,  "f415f4ed9435427e1fdf7f1fccd4dbc07b3d6b8751e4dbcec6f19671f427870b" }, /* RKSU */
	{ 0x033b, "c371061b19d8c7d7d6133c6a9bafe198fa944e50c1b31c9d8daa8d7f1fc2d2d6" }, /* 官方 KSU */
	{ 0x381,  "52d52d8c8bfbe53dc2b6ff1c613184e2c03013e090fe8905d8e3d5dc2658c2e4" }, /* WKSU */
	{ 0x375,  "484fcba6e6c43b1fb09700633bf2fb4758f13cb0b2f4457b80d075084b26c588" }, /* KowSU */
	{ 0x3e6,  "79e590113c4c4c0c222978e413a5faa801666957b1212a328e46c00c69821bf7" }, /* KSUN */
	{ 384,    "7e0c6d7278a3bb8e364e0fcba95afaf3666cf5ff3c245a3b63c8833bd0445cc4" }, /* MKSU */
#ifdef KSU_MANAGER_EXTRA_SIZE
	/* [自研] ShizuSU 自家管理器签名，由 kernel/Kbuild 通过
	 *   ccflags -DKSU_MANAGER_EXTRA_SIZE=<der_len> -DKSU_MANAGER_EXTRA_HASH="<hex>"
	 * 编译期注入到表尾；未定义时表仅含上面 6 条上游签名。换自家管理器证书时改这里。 */
	{ KSU_MANAGER_EXTRA_SIZE, KSU_MANAGER_EXTRA_HASH },
#endif
};

/* [自研] ShizuSU 软上限（上游无签名数量概念；非上游值）。
 * 编译期断言：签名表条数不得超过 KSU_MAX_MANAGER_KEYS。
 * BUILD_BUG_ON 为内核通用编译期设施（ReSukiSU 全仓唯一 BUILD_BUG_ON 在
 *   kernel/policy/app_profile.c:138 且与签名数量无关，属 ShizuSU 自研安全约束）。
 * 注意：BUILD_BUG_ON 宏需函数作用域；本头文件按「文件作用域静态断言」等价落地——
 *   enum 常量以 1/(cond<=limit) 形式在数组越界时引发除零编译错误，效果等同 BUILD_BUG_ON。 */
#define KSU_MAX_MANAGER_KEYS 64
enum {
	ksu_manager_keys_within_limit = 1 / (ARRAY_SIZE(apk_sign_keys) <= KSU_MAX_MANAGER_KEYS)
};

#endif // __KSU_H_MANAGER_SIGN
