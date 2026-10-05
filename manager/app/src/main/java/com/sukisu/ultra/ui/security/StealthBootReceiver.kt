package com.sukisu.ultra.ui.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 开机后：
 *  1. 确保桌面图标在（顺便治好旧版本藏起来的图标）；
 *
 * ---- ShizuSU 移植说明（照搬 7kimisu StealthBootReceiver.kt v2.29）----
 *   包名 com.sevenk.core -> com.sukisu.ultra。7kimisu 原注释提及的「网页管理器自启」
 *   跑在 ksud 里，与 App 进程/开机广播无关，故此处只保留 ensureLauncherVisible 一项。
 */
class StealthBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        Stealth.ensureLauncherVisible(context)
    }
}
