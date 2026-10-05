package com.sukisu.ultra

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import com.sukisu.ultra.Natives.Profile.RootProfileFlag
import com.sukisu.ultra.ui.util.rootAvailable

/**
 * @author weishu
 * @date 2022/12/8.
 */
object Natives {
    // minimal supported kernel version
    // 10915: allowlist breaking change, add app profile
    // 10931: app profile struct add 'version' field
    // 10946: add capabilities
    // 10977: change groups_count and groups to avoid overflow write
    // 11071: Fix the issue of failing to set a custom SELinux type.
    // 12143: breaking: new supercall impl
    // 32310: new get_allow_list ioctl
    // 32336: new set_sepolicy ioctl
    // 32377: add set_init_pgrp ioctl
    // 32513: add uapi version
    const val MINIMAL_SUPPORTED_KERNEL = 32513

    // Get full version
    // The kernel returns null when the GET_FULL_VERSION supercall is unavailable.
    external fun getFullVersion(): String?
    const val MINIMAL_SUPPORTED_KERNEL_FULL = "v4.0.0"

    // 12040: Support disable sucompat mode
    const val KERNEL_SU_DOMAIN = "u:r:ksu:s0"

    const val ROOT_UID = 0
    const val ROOT_GID = 0

    fun isVersionLessThan(v1Full: String, v2Full: String): Boolean {
        fun extractVersionParts(version: String): List<Int> {
            val match = Regex("""v\d+(\.\d+)*""").find(version)
            val simpleVersion = match?.value ?: version
            return simpleVersion.trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
        }

        val v1Parts = extractVersionParts(v1Full)
        val v2Parts = extractVersionParts(v2Full)
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        for (i in 0 until maxLength) {
            val num1 = v1Parts.getOrElse(i) { 0 }
            val num2 = v2Parts.getOrElse(i) { 0 }
            if (num1 != num2) return num1 < num2
        }
        return false
    }

    init {
        System.loadLibrary("kernelsu")
    }

    val version: Int
        external get

    val isSafeMode: Boolean
        external get

    val isLkmMode: Boolean
        external get

    val isLkmBundled: Boolean
        external get

    val isLateLoadMode: Boolean
        external get

    val isManager: Boolean
        external get

    val isPrBuild: Boolean
        external get

    external fun uidShouldUmount(uid: Int): Boolean

    /**
     * Get the profile of the given package.
     * @param key usually the package name
     * @return return null if failed.
     */
    external fun getAppProfile(key: String?, uid: Int): Profile
    external fun setAppProfile(profile: Profile?): Boolean

    /**
     * `su` compat mode can be disabled temporarily.
     *  0: disabled
     *  1: enabled
     *  negative : error
     */
    external fun isSuEnabled(): Boolean
    external fun setSuEnabled(enabled: Boolean): Boolean

    /**
     * Kernel module umount can be disabled temporarily.
     *  0: disabled
     *  1: enabled
     *  negative : error
     */
    external fun isKernelUmountEnabled(): Boolean
    external fun setKernelUmountEnabled(enabled: Boolean): Boolean

    /**
     * SELinux hide can be disabled temporarily.
     *  0: disabled
     *  1: enabled
     *  negative : error
     */
    external fun isSelinuxHideEnabled(): Boolean
    external fun setSelinuxHideEnabled(enabled: Boolean): Int

    /**
     * Get the user name for the uid.
     */
    external fun getUserName(uid: Int): String?

    external fun getSuperuserCount(): Int

    external fun getHookType(): String

    private const val NON_ROOT_DEFAULT_PROFILE_KEY = "$"
    private const val NOBODY_UID = 9999

    fun setDefaultUmountModules(umountModules: Boolean): Boolean {
        Profile(
            NON_ROOT_DEFAULT_PROFILE_KEY,
            NOBODY_UID,
            false,
            umountModules = umountModules
        ).let {
            return setAppProfile(it)
        }
    }

    fun isDefaultUmountModules(): Boolean {
        getAppProfile(NON_ROOT_DEFAULT_PROFILE_KEY, NOBODY_UID).let {
            return it.umountModules
        }
    }

    val kernelUAPIVersion: Int
        external get

    val managerUAPIVersion: Int
        external get

    // ---- [ShizuSU 补丁2] 内核隐身（stealth）native 入口 ----
    // 照搬 7kimisu Natives.kt 对应方法（对照 manager/.../Natives.kt:71 stealthSet）。
    // 底层 ioctl：
    //   stealthState() -> KSU_IOCTL_STEALTH_GET ('K',25, struct ksu_stealth_cmd{__u8 enabled})
    //   stealthSet()   -> KSU_IOCTL_STEALTH_SET ('K',26, struct ksu_stealth_cmd{__u8 enabled})
    // 内核侧 perm_check=only_manager：由多签名表加冕出的 is_manager() 把关，App 不另做签名校验。
    // TODO(ShizuSU 集成)：以下两个 external 需在 cpp（libkernelsu）中实现对应的 JNI 函数；
    //   当前基线 Natives.kt 无此 native，未实现时调用会抛 UnsatisfiedLinkError（Stealth.kt 已 runCatching 兜底为 -1/false）。

    /**
     * 读隐身开关。
     * @return 1 = 隐身开, 0 = 隐身关, -1 = 内核不支持 / ioctl 失败 / 本 App 未被内核认主。
     */
    external fun stealthState(): Int

    /**
     * 写隐身开关并持久化（内核落到 /data/adb/shizusu/stealth）。
     * @return true = 成功。
     */
    external fun stealthSet(enabled: Boolean): Boolean

    fun isFullFeatured(): Boolean {
        val kernelFullVersion = getFullVersion()
        return (kernelFullVersion != null && isVersionLessThan(kernelFullVersion, MINIMAL_SUPPORTED_KERNEL_FULL)) ||
                isManager && kernelUAPIVersion == managerUAPIVersion && rootAvailable()
    }

    @Keep
    @Immutable
    @Parcelize
    @Serializable
    data class Profile(
        // and there is a default profile for root and non-root
        val name: String,
        // current uid for the package, this is convivent for kernel to check
        // if the package name doesn't match uid, then it should be invalidated.
        val currentUid: Int = 0,

        // if this is true, kernel will grant root permission to this package
        val allowSu: Boolean = false,

        // these are used for root profile
        val rootUseDefault: Boolean = true,
        val rootTemplate: String? = null,
        val uid: Int = ROOT_UID,
        val gid: Int = ROOT_GID,
        val groups: List<Int> = mutableListOf(),
        val capabilities: List<Int> = mutableListOf(),
        val context: String = KERNEL_SU_DOMAIN,
        val namespace: Int = Namespace.INHERITED.ordinal,

        val nonRootUseDefault: Boolean = true,
        val umountModules: Boolean = true,
        var rules: String = "", // this field is save in ksud!!

        val flags: Long = FLAG_KSU_NO_NEW_PRIVS,
    ) : Parcelable {
        @Keep
        enum class RootProfileFlag(val display: String, val desc: Int) {
            NO_NEW_PRIVS(
                "NO_NEW_PRIVS",
                R.string.profile_flags_desc_no_new_privs
            )
        }

        enum class Namespace {
            INHERITED,
            GLOBAL,
            INDIVIDUAL,
        }

        constructor() : this("")
    }

    const val FLAG_KSU_NO_NEW_PRIVS = 1L
}

fun List<RootProfileFlag>.toRawFlags(): Long =
    fold(0L) { acc, flag -> acc.or(1L.shl(flag.ordinal)) }

fun List<RootProfileFlag>.toOrdinalList(): List =
    map { it.ordinal }

fun Long.toRootProfileFlags(): List<RootProfileFlag> =
    RootProfileFlag.entries.filter { 1L.shl(it.ordinal).and(this) != 0L }.toList()
