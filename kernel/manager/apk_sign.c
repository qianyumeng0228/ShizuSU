#include "util.h"
#include <linux/err.h>
#include <linux/fs.h>
#include <linux/gfp.h>
#include <linux/kernel.h>
#include <linux/limits.h>
#include <linux/slab.h>
#include <linux/version.h>
#ifdef CONFIG_KSU_DEBUG
#include <linux/moduleparam.h>
#endif
#include <crypto/hash.h>
#if LINUX_VERSION_CODE >= KERNEL_VERSION(5, 11, 0)
#include <crypto/sha2.h>
#else
#include <crypto/sha.h>
#endif
#if LINUX_VERSION_CODE >= KERNEL_VERSION(6, 4, 0)
#include <linux/hex.h>
#endif

#include "manager/apk_sign.h"
#include "manager/manager_sign.h" /* [改造] ShizuSU 多签名表（唯一签名源），替换原 EXPECTED_* 宏 */
#include "uapi/app_profile.h"
#include "klog.h" // IWYU pragma: keep

struct sdesc {
	struct shash_desc shash;
	char ctx[];
};

static struct sdesc *init_sdesc(struct crypto_shash *alg)
{
	struct sdesc *sdesc;
	int size;

	size = sizeof(struct shash_desc) + crypto_shash_descsize(alg);
	sdesc = kzalloc(size, GFP_KERNEL);
	if (!sdesc)
		return ERR_PTR(-ENOMEM);
	sdesc->shash.tfm = alg;
	return sdesc;
}

static int calc_hash(struct crypto_shash *alg, const unsigned char *data, unsigned int datalen, unsigned char *digest)
{
	struct sdesc *sdesc;
	int ret;

	sdesc = init_sdesc(alg);
	if (IS_ERR(sdesc)) {
		pr_info("can't alloc sdesc\n");
		return PTR_ERR(sdesc);
	}

	ret = crypto_shash_digest(&sdesc->shash, data, datalen, digest);
	kfree(sdesc);
	return ret;
}

static int ksu_sha256(const unsigned char *data, unsigned int datalen, unsigned char *digest)
{
	struct crypto_shash *alg;
	char *hash_alg_name = "sha256";
	int ret;

	alg = crypto_alloc_shash(hash_alg_name, 0, 0);
	if (IS_ERR(alg)) {
		pr_info("can't alloc alg %s\n", hash_alg_name);
		return PTR_ERR(alg);
	}
	ret = calc_hash(alg, data, datalen, digest);
	crypto_free_shash(alg);
	return ret;
}

static bool read_exact(struct file *fp, void *buffer, size_t size, loff_t *pos, loff_t end)
{
	if (*pos < 0 || *pos > end || size > (size_t)(end - *pos))
		return false;

	return kernel_read(fp, buffer, size, pos) == (ssize_t)size;
}

static bool read_length_prefixed_end(struct file *fp, loff_t *pos, loff_t container_end, loff_t *value_end)
{
	u32 length;

	if (!read_exact(fp, &length, sizeof(length), pos, container_end))
		return false;
	if (length > INT_MAX || length > (u64)(container_end - *pos))
		return false;

	*value_end = *pos + length;
	return true;
}

/*
 * [改造] 单签名比对 -> 遍历多签名表 apk_sign_keys[]。
 * [照搬] 遍历语义照搬 ReSukiSU kernel/manager/apk_sign.c:83-111：
 *   for 循环 + 证书长度粗筛 + ksu_sha256 + bin2hex + strcmp 命中即 return true。
 * [保留] v2 签名块结构解析（read_length_prefixed_end/read_exact 边界检查）沿用基线
 *   apk_sign.c:97-116，比 ReSukiSU 无边界 kernel_read 更安全，不改动。
 */
static bool check_block(struct file *fp, loff_t *pos, loff_t block_end)
{
	loff_t signers_end, signer_end, signed_data_end, digests_end, certificates_end;
	u32 certificate_size;
	int i;

	// v2 block: signers sequence -> first signer -> signed data -> digests
	if (!read_length_prefixed_end(fp, pos, block_end, &signers_end) ||
	    !read_length_prefixed_end(fp, pos, signers_end, &signer_end) ||
	    !read_length_prefixed_end(fp, pos, signer_end, &signed_data_end) ||
	    !read_length_prefixed_end(fp, pos, signed_data_end, &digests_end))
		return false;

	*pos = digests_end;
	if (!read_length_prefixed_end(fp, pos, signed_data_end, &certificates_end) ||
	    !read_exact(fp, &certificate_size, sizeof(certificate_size), pos, certificates_end))
		return false;

	if (certificate_size > INT_MAX || certificate_size > (u64)(certificates_end - *pos))
		return false;

#define CERT_MAX_LENGTH 1024
	for (i = 0; i < ARRAY_SIZE(apk_sign_keys); i++) {
		/* [照搬] ReSukiSU apk_sign.c:86-87：先按证书 DER 长度粗筛，不等即跳过 */
		if (certificate_size != apk_sign_keys[i].size)
			continue;

		/* [自研] 证书缓冲改为按需 kmalloc：官方上限 CERT_MAX_LENGTH=1024 是按官方管理器
		 * 证书（约 827B）设定的栈数组；ShizuSU 自家证书 DER 为 1292B（超限），
		 * 且 2026 年后管理器证书普遍 >1KB。动态分配支持任意证书长度，
		 * certificate_size > INT_MAX 的防护已在函数入口检查。 */
		char *cert = kmalloc(certificate_size, GFP_KERNEL);
		if (!cert)
			return false;
		if (!read_exact(fp, cert, certificate_size, pos, certificates_end)) {
			kfree(cert);
			return false;
		}

		unsigned char digest[SHA256_DIGEST_SIZE];
		if (ksu_sha256(cert, certificate_size, digest)) {
			pr_info("sha256 error\n");
			kfree(cert);
			return false;
		}
		kfree(cert);

		char hash_str[SHA256_DIGEST_SIZE * 2 + 1];
		hash_str[SHA256_DIGEST_SIZE * 2] = '\0';

		bin2hex(hash_str, digest, SHA256_DIGEST_SIZE);
		pr_info("sha256: %s, expected: %s\n", hash_str, apk_sign_keys[i].sha256);
		/* [照搬] ReSukiSU apk_sign.c:108-110：命中表中任一签名即认可 */
		if (strcmp(apk_sign_keys[i].sha256, hash_str) == 0)
			return true;
	}
	return false;
}

/* [照搬] ReSukiSU kernel/manager/apk_sign.c:115-127：ZIP 本地文件头，用于探测 v1 签名 */
struct zip_entry_header {
	uint32_t signature;
	uint16_t version;
	uint16_t flags;
	uint16_t compression;
	uint16_t mod_time;
	uint16_t mod_date;
	uint32_t crc32;
	uint32_t compressed_size;
	uint32_t uncompressed_size;
	uint16_t file_name_length;
	uint16_t extra_field_length;
} __attribute__((packed));

/* [照搬] ReSukiSU apk_sign.c:130-162：存在 META-INF/MANIFEST.MF 即视为 v1 签名。
 * 这是必要但不充分条件，对我们足够（v2 有效时禁止 v1）。 */
static bool has_v1_signature_file(struct file *fp)
{
	struct zip_entry_header header;
	const char MANIFEST[] = "META-INF/MANIFEST.MF";

	loff_t pos = 0;

	while (kernel_read(fp, &header, sizeof(struct zip_entry_header), &pos) == sizeof(struct zip_entry_header)) {
		if (header.signature != 0x04034b50) {
			// ZIP magic: 'PK'
			return false;
		}
		// Read the entry file name
		if (header.file_name_length == sizeof(MANIFEST) - 1) {
			char fileName[sizeof(MANIFEST)];
			kernel_read(fp, fileName, header.file_name_length, &pos);
			fileName[header.file_name_length] = '\0';

			// Check if the entry matches META-INF/MANIFEST.MF
			if (strncmp(MANIFEST, fileName, sizeof(MANIFEST) - 1) == 0) {
				return true;
			}
		} else {
			// Skip the entry file name
			pos += header.file_name_length;
		}

		// Skip to the next entry
		pos += header.extra_field_length + header.compressed_size;
	}

	return false;
}

/*
 * [改造] 去掉原 expected_size/expected_sha256 形参（单签名），改为调用表遍历 check_block。
 * [保留] APK 容器解析沿用基线：EOCD 定位 / ZIP64 拒绝 / central directory 校验 /
 *   "APK Sig Block 42" 魔串校验 / 逐条 length-prefixed pair 扫描（基线 apk_sign.c:145-300）。
 * [照搬] v3(0xf05368c0)/v3.1(0x1b93ad61) 存在即拒、v2 块数!=1 视为异常、v2 有效时禁止 v1
 *   —— 照搬 ReSukiSU apk_sign.c:233-276 的判定流程。
 */
static __always_inline bool check_v2_signature(char *path)
{
	unsigned char buffer[0x10] = { 0 };
	u32 cd_offset, cd_size;
	u32 zip64_locator_magic;
	u64 size_of_block, size_of_block_at_head;

	loff_t pos, pairs_end, file_size, eocd_offset;

	bool v2_signing_valid = false;
	int v2_signing_blocks = 0;
	bool v3_signing_exist = false;
	bool v3_1_signing_exist = false;

	int i;
	struct file *fp = ksu_filp_open_nonotify(path, O_RDONLY | O_NOATIME);
	if (IS_ERR(fp)) {
		pr_err("open %s error.\n", path);
		return false;
	}

	file_size = generic_file_llseek(fp, 0, SEEK_END);
	if (file_size < 0)
		goto clean;

	// https://en.wikipedia.org/wiki/Zip_(file_format)#End_of_central_directory_record_(EOCD)
	for (i = 0; ; i++) {
		unsigned short comment_size;
		u32 magic;
		pos = file_size - i - 2;
		if (!read_exact(fp, &comment_size, sizeof(comment_size), &pos, file_size))
			goto clean;
		if (comment_size == i) {
			pos -= 22;
			if (!read_exact(fp, &magic, sizeof(magic), &pos, file_size))
				goto clean;
			if (magic == 0x06054b50) {
				/* [修复] eocd_offset 须指向 EOCD 记录起始（magic 位置）：
				 * read_exact 后 pos 已前进 sizeof(magic)，故用 pos - sizeof(magic)，
				 * 与上游基线 7fbbb1f1 apk_sign.c 一致。此前误写 + 使偏移 +8，
				 * 后续 cd_size 读取错位越界，任何 v2 签名 APK 均被拒（管理器无法加冕）。 */
				eocd_offset = pos - sizeof(magic);
				break;
			}
		}
		if (i == 0xffff) {
			pr_info("error: cannot find eocd\n");
			goto clean;
		}
	}

	// Reject ZIP64 before looking for a signing block
	if (eocd_offset >= 20) {
		pos = eocd_offset - 20;
		if (!read_exact(fp, &zip64_locator_magic, sizeof(zip64_locator_magic), &pos, file_size))
			goto clean;
		if (zip64_locator_magic == 0x07064b50)
			goto clean;
	}

	pos = eocd_offset + 12;
	// size of central directory
	if (!read_exact(fp, &cd_size, sizeof(cd_size), &pos, file_size))
		goto clean;
	// offset of central directory
	if (!read_exact(fp, &cd_offset, sizeof(cd_offset), &pos, file_size))
		goto clean;
	if ((u64)cd_offset > (u64)eocd_offset || (u64)cd_size != (u64)eocd_offset - cd_offset)
		goto clean;
	if (cd_offset < 0x20)
		goto clean;

	pairs_end = (loff_t)cd_offset - 0x18;
	pos = pairs_end;

	if (!read_exact(fp, &size_of_block, sizeof(size_of_block), &pos, cd_offset))
		goto clean;
	if (!read_exact(fp, &buffer, sizeof(buffer), &pos, cd_offset))
		goto clean;
	if (memcmp((char *)buffer, "APK Sig Block 42", sizeof(buffer)))
		goto clean;

	if (size_of_block < 0x18 || size_of_block > INT_MAX - 0x8 || size_of_block > (u64)cd_offset - 0x8)
		goto clean;

	pos = (loff_t)cd_offset - (loff_t)size_of_block - 0x8;
	if (!read_exact(fp, &size_of_block_at_head, sizeof(size_of_block_at_head), &pos, pairs_end))
		goto clean;
	if (size_of_block_at_head != size_of_block)
		goto clean;

	// Scan every length-prefixed pair, matching AOSP's signing block parser
	while (pos < pairs_end) {
		uint32_t id;
		u64 size_of_pair;
		loff_t pair_end;

		if (!read_exact(fp, &size_of_pair, sizeof(size_of_pair), &pos, pairs_end))
			goto invalid;
		if (size_of_pair < sizeof(id) || size_of_pair > INT_MAX || size_of_pair > (u64)(pairs_end - pos))
			goto invalid;

		pair_end = pos + (loff_t)size_of_pair;
		if (!read_exact(fp, &id, sizeof(id), &pos, pair_end))
			goto invalid;

		if (id == 0x7109871au) { // v2 signing block
			v2_signing_blocks++;
			v2_signing_valid = check_block(fp, &pos, pair_end);
		} else if (id == 0xf05368c0u) { // [照搬] v3 scheme -> 存在即拒
			v3_signing_exist = true;
		} else if (id == 0x1b93ad61u) { // [照搬] v3.1 scheme -> 存在即拒
			v3_1_signing_exist = true;
		} else if (id != 0x42726577u) { // APK verdict padding（基线允许的未知 id）
#ifdef CONFIG_KSU_DEBUG
			pr_info("Unexpected signature block id: 0x%08x\n", id);
#endif
			goto invalid;
		}
		pos = pair_end;
	}

	if (v2_signing_blocks != 1) { // [照搬] v2 块数 != 1 视为异常
#ifdef CONFIG_KSU_DEBUG
		pr_err("Unexpected v2 signature count: %d\n", v2_signing_blocks);
#endif
		v2_signing_valid = false;
	}

	if (v2_signing_valid) { // [照搬] v2 有效时禁止 v1
		int has_v1_signing = has_v1_signature_file(fp);
		if (has_v1_signing) {
			pr_err("Unexpected v1 signature scheme found! \n");
			filp_close(fp, 0);
			return false;
		}
	}

	goto clean;

invalid:
	v2_signing_valid = false;
clean:
	filp_close(fp, 0);

	if (v3_signing_exist || v3_1_signing_exist) { // [照搬] 存在 v3/v3.1 即拒
#ifdef CONFIG_KSU_DEBUG
		pr_err("Unexpected v3 signature scheme found! \n");
#endif
		return false;
	}

	return v2_signing_valid;
}

#ifdef CONFIG_KSU_DEBUG
int ksu_debug_manager_appid = -1;

#include "manager/manager_identity.h"

static int set_expected_size(const char *val, const struct kernel_param *kp)
{
	int rv = param_set_uint(val, kp);
	ksu_set_manager_appid(ksu_debug_manager_appid);
	pr_info("ksu_manager_appid set to %d\n", ksu_debug_manager_appid);
	return rv;
}

static struct kernel_param_ops expected_size_ops = {
	.set = set_expected_size,
	.get = param_get_uint,
};

module_param_cb(ksu_debug_manager_appid, &expected_size_ops, &ksu_debug_manager_appid, S_IRUSR | S_IWUSR);

#endif

int get_pkg_from_apk_path(char *pkg, const char *path)
{
	int len = strlen(path);
	if (len >= KSU_MAX_PACKAGE_NAME || len < 1)
		return -1;

	const char *last_slash = NULL;
	const char *second_last_slash = NULL;

	int i;
	for (i = len - 1; i >= 0; i--) {
		if (path[i] == '/') {
			if (!last_slash) {
				last_slash = &path[i];
			} else {
				second_last_slash = &path[i];
				break;
			}
		}
	}

	if (!last_slash || !second_last_slash)
		return -1;

	const char *last_hyphen = strchr(second_last_slash, '-');
	if (!last_hyphen || last_hyphen > last_slash)
		return -1;

	int pkg_len = last_hyphen - second_last_slash - 1;
	if (pkg_len >= KSU_MAX_PACKAGE_NAME || pkg_len <= 0)
		return -1;

	// Copying the package name
	memcpy(pkg, second_last_slash + 1, pkg_len);
	pkg[pkg_len] = '\0';

	return 0;
}

bool is_manager_apk(char *path)
{
#ifdef KSU_MANAGER_PACKAGE
	char pkg[KSU_MAX_PACKAGE_NAME];
	if (get_pkg_from_apk_path(pkg, path) < 0) {
		pr_err("Failed to get package name from apk path: %s\n", path);
		return false;
	}

	// pkg is `<real package>`
	if (strncmp(pkg, KSU_MANAGER_PACKAGE, sizeof(KSU_MANAGER_PACKAGE))) {
		return false;
	}
#endif
	/* [改造] 基线原按 EXPECTED_SIZE/HASH 比对、可选 EXPECTED_SIZE2/HASH2；
	 * 多签名表接入后，命中表中任一签名即认可（依据清单 1.2 第 5 步：清理基线冗余）。
	 * EXPECTED_SIZE2/HASH2 相关分支随单签名宏一并移除。 */
	return check_v2_signature(path);
}
