package com.sukisu.ultra.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
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

    // [ShizuSU] 终解：root 权限精准清 launcher 数据库 icon 字段 + force-stop
    runCatching {
        val launcherPkg = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0
        )?.activityInfo?.packageName
        if (!launcherPkg.isNullOrEmpty() && launcherPkg != context.packageName) {
            val pkg = context.packageName
            // 1. 临时放开目录+db 权限
            Runtime.getRuntime().exec(arrayOf("su", "-c",
                "chmod 755 /data/user_de/0/$launcherPkg /data/user_de/0/$launcherPkg/databases 2>/dev/null && " +
                "chmod 666 /data/user_de/0/$launcherPkg/databases/launcher4x6.db 2>/dev/null"
            )).waitFor()
            // 2. 清自身条目 icon 字段（favorites 表）
            runCatching {
                val db = SQLiteDatabase.openDatabase(
                    "/data/user_de/0/$launcherPkg/databases/launcher4x6.db",
                    null, SQLiteDatabase.OPEN_READWRITE
                )
                db.execSQL("UPDATE favorites SET iconPackage=NULL, iconResource=NULL, icon=NULL, iconType=0 WHERE intent LIKE '%$pkg%'")
                db.close()
            }
            // 3. 恢复权限 + force-stop launcher
            Runtime.getRuntime().exec(arrayOf("su", "-c",
                "chmod 600 /data/user_de/0/$launcherPkg/databases/launcher4x6.db 2>/dev/null && " +
                "chmod 700 /data/user_de/0/$launcherPkg/databases /data/user_de/0/$launcherPkg 2>/dev/null && " +
                "am force-stop $launcherPkg"
            )).waitFor()
        }
    }
}
