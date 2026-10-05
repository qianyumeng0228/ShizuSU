package com.sukisu.ultra.ui.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.MainActivity

/**
 * 拨号盘密令入口:`*#*#70707#*#*`(可在设置里自定义)
 *
 * 关闭隐身、恢复到正常界面,并直接打开管理器。
 * 显式启动 [MainActivity](而不是 getLaunchIntentForPackage),
 * 这样和桌面入口的状态完全无关,一定能拉起界面。
 *
 * 密令比对用的是 [StealthCodeStore.acceptedCodes] —— **取并集**:
 * `/data/adb/shizusu/stealth_code` 那份(能跨卸载)和 App 设置那份,任意一个匹配就通过。
 * 读那份要起 root shell,不能在主线程做,所以这里用 goAsync() 丢到子线程。
 *
 * ---- ShizuSU 移植说明（照搬 7kimisu StealthReceiver.kt v2.29 收紧版，逐逻辑未改）----
 *   包名 com.sevenk.core -> com.sukisu.ultra；R/MainActivity 引用同步改。
 *   [自研] TAG 改 "shizusu-stealth"；默认密令取自 [StealthCodeStore.DEFAULT_CODE]（BuildConfig 可配）。
 *   ⚠️ 发送方校验 / 防暴力 / 三态文案逻辑一行未动（v2.29 审计修复全部保留）。
 */
class StealthReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SECRET_CODE") return

        // 🔒 2026-09-25(朋友审计)：**先验发送方，再看内容**。
        //    SECRET_CODE 不是受保护广播 —— 任何 App 都能自己发一条带
        //    android_secret_code://70707 的广播。以前只看 action + 拨的号 =「谁发的都收」。
        if (!senderTrusted(context)) {
            android.util.Log.w(TAG, "密令广播来自非拨号盘发送方，已忽略")
            return
        }

        // 因为清单里只声明了 scheme,所以会收到"所有"密令。
        val dialed = intent.data?.host ?: return

        // 防暴力猜：默认密令是公开的 70707。连错到上限就临时挡下（纯内存计数）。
        // 放在最前面：被挡时连 root shell 都不起。
        val now = System.currentTimeMillis()
        if (now < blockedUntil) {
            android.util.Log.w(TAG, "密令尝试被暂时挡下（连错过多），剩余 ${blockedUntil - now}ms")
            return
        }

        val pending = goAsync()
        Thread {
            try {
                // 读 /data/adb 的密令要起 root shell,不能在主线程;
                val restore = prepareRestore(context, dialed)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        finishRestore(context, restore)
                    } catch (t: Throwable) {
                        android.util.Log.w(TAG, "密令收尾异常", t)
                    } finally {
                        runCatching { pending.finish() }
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.w(TAG, "密令处理异常", t)
                runCatching { pending.finish() }
            }
        }.start()
    }

    /**
     * 发送方可信吗？—— 只认「拨号盘那一路」发来的密令广播。
     *
     * 判据（Android 14 / API 34 起才有发送方信息）：
     *   · 系统(1000) / 电话进程(1001) / 我们自己            → 放行（这三个 uid 不可能是别的角色）
     *   · 其余 uid：必须能查出"包身份"，且满足任一条才放行
     *       ① 本机默认拨号器（包名对上）
     *       ② 预装/系统应用（FLAG_SYSTEM）—— 覆盖"拨号由厂商电话组件发"的 ROM
     *   · 查不出包身份的（例如 adb shell / am broadcast，uid 2000）→ 拒绝
     *
     * 🔒 2026-09-27 修：上一版「uid < 10000 一律放行」被删 —— adb shell 就是 uid 2000，
     *   一条 `am broadcast -a ...SECRET_CODE -d android_secret_code://70707` 就能关隐身。
     *   现在任何非 1000/1001/自己 的 uid 都要有系统级包身份，adb 无包身份 → 直接拒。
     *
     * 拿不到发送方时 fail-open：API < 34 没有 sentFromUid，"识别不出拨号器而拒掉密令 = 锁死"。
     */
    private fun senderTrusted(context: Context): Boolean {
        // API < 34：拿不到发送方信息，退回旧行为（宁可不加这层，也不能把人锁死）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true

        val uid = runCatching { sentFromUidCompat() }.getOrDefault(-1)
        if (uid < 0) return true // 未知发送方 → 同上，放行
        if (uid == Process.SYSTEM_UID || uid == Process.PHONE_UID || uid == Process.myUid()) return true

        // 其余 uid：先查出它的"包身份"
        val pkgs = LinkedHashSet<String>()
        runCatching { sentFromPackageCompat() }.getOrNull()
            ?.takeIf { it.isNotBlank() }?.let { pkgs.add(it) }
        runCatching { context.packageManager.getPackagesForUid(uid) }
            .getOrNull()?.forEach { pkgs.add(it) }
        if (pkgs.isEmpty()) return false

        // ① 本机默认拨号器
        val dialer = runCatching {
            context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        }.getOrNull()
        if (dialer != null && pkgs.contains(dialer)) return true

        // ② 预装/系统应用（覆盖厂商电话组件）
        return pkgs.any { pkg ->
            runCatching {
                val ai = context.packageManager.getApplicationInfo(pkg, 0)
                (ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            }.getOrDefault(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun sentFromUidCompat(): Int = sentFromUid

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun sentFromPackageCompat(): String? = sentFromPackage

    /** 后台线程:比对 + 关隐身,返回需要主线程展示的结果 */
    private fun prepareRestore(context: Context, dialed: String): RestorePlan {
        // 注:第一项(内核标志)只为日志。比对**取并集**(磁盘那份 + App 设置那份)。
        val kernelState = runCatching { Stealth.kernelState() }.getOrDefault(-1)
        val accepted = StealthCodeStore.acceptedCodes(context)
        // 🔒 密令内容一个字都不许进日志：只记数量与状态，不记数字。
        android.util.Log.i(TAG, "收到密令(内容已隐去) 候选=${accepted.size} 隐身状态=$kernelState")
        if (dialed !in accepted) {
            android.util.Log.w(TAG, "密令不匹配,已忽略")
            // 防暴力猜：连错 MAX_FAILS 次就临时锁 BLOCK_MILLIS（纯内存，重启即清）。
            fails++
            if (fails >= MAX_FAILS) {
                blockedUntil = System.currentTimeMillis() + BLOCK_MILLIS
                fails = 0
                android.util.Log.w(TAG, "密令连错 $MAX_FAILS 次，暂停 ${BLOCK_MILLIS / 1000} 秒")
            }
            return RestorePlan(showRestore = false, message = "")
        }
        // 匹配成功：清空失败计数
        fails = 0

        // 三态：wasEnabled 必须用「明确读到 1」，不能把读失败(-1)和关着(0)压成 false。
        val wasEnabled = kernelState == 1
        val restored = Stealth.setEnabled(false)
        Stealth.ensureLauncherVisible(context)
        // 只有"原本确实在隐身 且 这次确实关成功"才补通知。
        if (wasEnabled && restored) {
            notifyStealthRestored(context)
        }
        android.util.Log.i(TAG, "恢复完成:kernelState=$kernelState restored=$restored")
        return RestorePlan(
            showRestore = true,
            // TODO(ShizuSU 集成)：下列字符串需在 strings.xml 补齐（沿用 7kimisu 文案 key）：
            //   stealth_restore_not_enabled / stealth_restore_disabled /
            //   stealth_restore_foreign_kernel / stealth_restore_kernel_too_old /
            //   stealth_restore_not_recognized
            message = when {
                kernelState == 0 -> context.getString(R.string.stealth_restore_not_enabled)
                restored -> context.getString(R.string.stealth_restore_disabled)
                !Stealth.kernelHasStealthFeature() && !Stealth.kernelLooksOurs() ->
                    context.getString(R.string.stealth_restore_foreign_kernel)
                !Stealth.kernelHasStealthFeature() ->
                    context.getString(R.string.stealth_restore_kernel_too_old)
                else -> context.getString(R.string.stealth_restore_not_recognized)
            },
        )
    }

    /** 主线程:提示 + 拉起界面 */
    private fun finishRestore(context: Context, plan: RestorePlan) {
        if (!plan.showRestore) return
        Toast.makeText(context, plan.message, Toast.LENGTH_LONG).show()

        val launch = Intent(context, MainActivity::class.java).apply {
            // 用 CLEAR_TASK 而不是 CLEAR_TOP：清任务重建才是干净的（避免旧 ViewModel 旧状态）。
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val started = runCatching { context.startActivity(launch) }
        android.util.Log.i(TAG, "启动界面=${started.isSuccess}")
        if (started.isFailure) {
            Toast.makeText(context, "已关闭隐身,但界面启动失败,请点桌面图标", Toast.LENGTH_LONG).show()
        }
    }

    /** 供主线程收尾用的结果 */
    private data class RestorePlan(val showRestore: Boolean, val message: String)

    /**
     * 关隐身成功后发一条**可点**的低存在感通知（IMPORTANCE_LOW + VISIBILITY_SECRET）。
     * Android 10+ 禁止后台启动 Activity，密令在后台把隐身关掉后界面不一定自己弹出来；
     * 通知栏至少让用户看到"已经关掉了"，点一下就能打开管理器。
     */
    private fun notifyStealthRestored(context: Context) {
        runCatching {
            val manager = context.getSystemService(NotificationManager::class.java)
                ?: return@runCatching

            // Android 8+ 通知必须先有渠道
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            manager.createNotificationChannel(channel)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.i(TAG, "用户没给通知权限，静默跳过关隐身通知")
                return@runCatching
            }

            val open = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("已关闭隐身")
                .setContentText("点这里打开管理器")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build()

            manager.notify(NOTIFICATION_ID, notification)
            android.util.Log.i(TAG, "已发出「关闭隐身」通知")
        }.onFailure {
            android.util.Log.w(TAG, "发通知失败(忽略，不影响关隐身)", it)
        }
    }

    companion object {
        // [自研] 默认密令：取 StealthCodeStore.DEFAULT_CODE（BuildConfig.STEALTH_SECRET_CODE 可配）。
        //   7kimisu 原值 const DEFAULT_SECRET_CODE = "70707"。
        const val DEFAULT_SECRET_CODE = StealthCodeStore.DEFAULT_CODE
        private const val TAG = "shizusu-stealth"

        /** 「关闭隐身」辅助通知的渠道 id / 渠道名 */
        private const val CHANNEL_ID = "stealth_restore"
        private const val CHANNEL_NAME = "隐身状态"

        /**
         * 「关闭隐身」辅助通知的通知 id（故意公开）：MainActivity.onResume 回到前台时要
         * cancel(NOTIFICATION_ID) 撤掉它，两处必须用同一个值。
         */
        const val NOTIFICATION_ID = 70001

        /** 密令连错几次就临时锁住 */
        private const val MAX_FAILS = 5

        /** 锁多久（毫秒） */
        private const val BLOCK_MILLIS = 30_000L

        /**
         * 失败计数 / 解禁时刻：**纯内存**（进程被杀或重启即清零），
         * 只为挡住"脚本连续乱试"这种低成本暴力破解，不落盘。
         */
        @Volatile private var fails = 0
        @Volatile private var blockedUntil = 0L
    }
}
