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

    // [ShizuSU] 发系统广播通知 launcher 重读图标
    runCatching {
        context.sendBroadcast(Intent(Intent.ACTION_PACKAGE_CHANGED, Uri.parse("package:${context.packageName}")))
    }

    // [ShizuSU] 终解：root 权限 force-stop launcher + 清图标缓存 → 桌面自动重建 → 图标即时刷新
    runCatching {
        val launcherPkg = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0
        )?.activityInfo?.packageName
        if (!launcherPkg.isNullOrEmpty() && launcherPkg != context.packageName) {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop $launcherPkg")).waitFor()
            // 清图标缓存（MIUI launcher 持久缓存，force-stop 不失效）
            Runtime.getRuntime().exec(arrayOf("su", "-c",
                "rm -rf /data/user_de/0/$launcherPkg/cache/*icon* /data/user_de/0/$launcherPkg/cache/*Icon* " +
                "/data/data/$launcherPkg/cache/*icon* /data/data/$launcherPkg/cache/*Icon* 2>/dev/null"
            )).waitFor()
        }
    }
}