package com.sukisu.ultra.ui.security

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
// import androidx.compose.ui.platform.LocalContext // TODO(ShizuSU): 按基线 Compose 栈决定是否需要
import com.sukisu.ultra.Natives

/**
 * 隐身模式 = **只伪装界面**,不动桌面图标。
 *
 * 开启后:内核在 GET_INFO 时不再上报 MANAGER 标志 -> [Natives.isManager] 变 false,
 * 管理器整个界面自动变成"未安装 / 点击安装",超级用户、模块、设置里的 root 项
 * 全部消失。内核真实权限不变,所以本 App 还能自己把这个开关关掉。
 *
 * 关掉之后界面立即恢复成正常的管理器。
 *
 * ---- ShizuSU 移植说明（照搬 7kimisu manager/.../ui/security/Stealth.kt v2.29）----
 *   包名 com.sevenk.core -> com.sukisu.ultra；Natives / MainActivity 引用同步改。
 *   [自研] 磁盘标志路径 /data/adb/sevenk/stealth -> /data/adb/shizusu/stealth，
 *          必须与内核 kernel/manager/stealth.c 的 KSU_STEALTH_PATH 逐字符一致。
 */
object Stealth {

    // [已验证] launcher alias 全限定名。推理:build.gradle.kts 中 namespace="com.sukisu.ultra"、
    //   applicationId=managerPackageName="com.qym.shizusu"(二者分离);AndroidManifest.xml 里
    //   activity-alias 写的是相对名 ".ui.MainActivityAlias"(targetActivity=".ui.MainActivity")。
    //   AGP 解析 manifest 组件相对名时按 **namespace**(而非 applicationId)拼包名,
    //   故实际组件名 = "com.sukisu.ultra" + ".ui.MainActivityAlias" = 下方常量,与
    //   ensureLauncherVisible 里 ComponentName(packageName, LAUNCHER_ALIAS) 的使用一致
    //   (Context.packageName 取 applicationId,但 ComponentName 第二参数传的是全限定组件名,不受影响)。
    //   基线 7kimisu 原值 "com.sevenk.core.ui.LauncherAlias"。
    private const val LAUNCHER_ALIAS = "com.sukisu.ultra.ui.MainActivityAlias"

    /**
     * 内核 UAPI 里**隐身功能**是哪一版加进来的。
     *
     * 依据：本仓 uapi/supercall.h 的版本注释 `6: add KSU_IOCTL_STEALTH_GET(25)/SET(26)`。
     * 用来把"内核太旧、根本没这个功能"和"内核有、但本 App 没被认主"分开
     * （审计 P0-2 要求拨号密令的三种文案说真话）。
     *
     * [自研] ShizuSU 把隐身放在 UAPI 6（7kimisu 是 5），因此本值取 6。
     */
    private const val UAPI_WITH_STEALTH = 6

    /**
     * 磁盘上那份**持久化**隐身标志（内核写，掉电/卸载重装都不丢）。
     *
     * 必须与内核侧 kernel/manager/stealth.c 的 KSU_STEALTH_PATH **逐字符一致**；
     * 内核在 POST_FS_DATA 时就是按它加载的（ksu_stealth_load）。
     * [自研] 路径适配：7kimisu 原值 "/data/adb/sevenk/stealth"。
     */
    const val STEALTH_FLAG_PATH = "/data/adb/shizusu/stealth"

    /** 内核里的开关:1 = 开启,0 = 关闭,其它(-1) = 内核不支持 / 读取失败 / 没被认主 */
    // 已实现：见 cpp/jni.cc Java_com_sukisu_ultra_Natives_stealthState（ioctl KSU_IOCTL_STEALTH_GET，'K',25）。
    fun kernelState(): Int = runCatching { Natives.stealthState() }.getOrDefault(-1)

    /**
     * 隐身的**三态**(v2.1,审计 P0-2)。
     * 以前只有一个布尔(kernelState()==1),把「明确读到 0(关着)」和「读失败/内核不支持」
     * 压成同一个 false。后果危险:一次 ioctl 读失败 -> 界面以为"隐身没开" -> 把只有 root
     * 管理器才有的东西露出来 -> 隐身当场穿帮。
     */
    enum class State {
        /** 明确读到 1:隐身开着 */
        ON,

        /** 明确读到 0:隐身关着 */
        OFF,

        /** 读失败 / 内核没这个功能 / 本 App 没被认主 —— **不知道**,一律按"可能开着"办 */
        UNKNOWN,
    }

    /**
     * 读三态。
     * ⚠️ 安全方向：拿不准时**按"可能开着"处理** —— 宁可不露馅。
     */
    fun state(): State = when (kernelState()) {
        1 -> State.ON
        0 -> State.OFF
        else -> State.UNKNOWN
    }

    /**
     * 界面上要不要**藏起 root 特征**(安装页的「直接安装」、越狱按钮、SELinux 卡…)。
     * =「不是明确读到 0」。开着 -> 藏;读失败/不支持/没被认主 -> **也藏**。
     */
    fun looksStealthy(): Boolean = state() != State.OFF

    /** ⚠️ 语义是"读不到就当没开",**不要**用它决定该不该藏 root 特征,用 [looksStealthy] */
    fun isEnabled(): Boolean = kernelState() == 1

    /**
     * 内核到底**有没有**隐身功能(和"认不认主"无关)。
     * 判据是 UAPI 版本:>= [UAPI_WITH_STEALTH] 就说明内核是带隐身那一代。
     */
    fun kernelHasStealthFeature(): Boolean =
        runCatching { Natives.kernelUAPIVersion >= UAPI_WITH_STEALTH }.getOrDefault(false)

    /**
     * 内核到底**是不是我们的**（不看功能新旧）—— 只看"内核认不认这个 App 当管理器"。
     */
    fun kernelLooksOurs(): Boolean = runCatching { Natives.isManager }.getOrDefault(false)

    // 已实现：见 cpp/jni.cc Java_com_sukisu_ultra_Natives_stealthSet（ioctl KSU_IOCTL_STEALTH_SET，'K',26）。
    fun setEnabled(enabled: Boolean): Boolean =
        runCatching { Natives.stealthSet(enabled) }.getOrDefault(false)

    /**
     * 设置页用:开/关隐身并**把成败说出来**(v2.1,审计 P0-2)。
     * @return null = 成功;非 null = 给用户看的原因(调用方必须弹出来)
     */
    fun setEnabledReporting(enabled: Boolean): String? {
        if (setEnabled(enabled)) return null
        val action = if (enabled) "开启" else "关闭"
        return when {
            kernelHasStealthFeature() ->
                "${action}隐身失败:本 App 现在没被内核认主。请重启设备,或卸载另一个管理器后再试"
            kernelLooksOurs() -> "内核太旧,不支持隐身模式"
            else -> "内核不是 ShizuSU(当前 root 由别的方案提供):请重启设备,或卸载另一个 root 管理器后再试"
        }
    }

    /**
     * 确保桌面图标在。每次启动、开机都调一次 ——
     * 既保证正常状态下图标一定存在,也顺手把旧版本藏起来的图标找回来。
     */
    fun ensureLauncherVisible(context: Context) {
        Thread {
            runCatching {
                val pm = context.packageManager
                val component = ComponentName(context.packageName, LAUNCHER_ALIAS)
                if (pm.getComponentEnabledSetting(component) !=
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                ) {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }
        }.start()
    }
}

/**
 * 「重建界面」的推荐姿势:先清 ViewModelStore,再 recreate。
 * 为什么要清 ViewModel:ViewModel 会跨 Activity.recreate() 存活(官方设计)。
 */
fun restartUiFresh(context: Context) {
    val activity = context.findActivity()
    (activity as? androidx.lifecycle.ViewModelStoreOwner)?.let {
        runCatching { it.viewModelStore.clear() }
    }
    activity?.recreate()
}

/**
 * 从任意 Context 里挖出真正的 Activity。
 * Compose 的 LocalContext 有时是 ContextThemeWrapper 而不是 Activity。
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
