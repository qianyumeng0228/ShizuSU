package com.sukisu.ultra.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.sukisu.ultra.ui.MainActivity

fun toggleLauncherIcon(context: Context, useAlt: Boolean) {
    val pm = context.packageManager
    val main = ComponentName(context, MainActivity::class.java.name)
    val alias = ComponentName(context, "${MainActivity::class.java.name}Alias")

    pm.setComponentEnabledSetting(
        if (useAlt) alias else main,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )

    pm.setComponentEnabledSetting(
        if (useAlt) main else alias,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
    )

    // [ShizuSU] 发系统广播通知 launcher 重读图标（MIUI/原生通用）
    runCatching {
        context.sendBroadcast(Intent(Intent.ACTION_PACKAGE_CHANGED, Uri.parse("package:${context.packageName}")))
    }
}