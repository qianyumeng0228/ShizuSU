package com.sukisu.ultra.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.sukisu.ultra.ui.MainActivity

private const val TAG = "ShizuSU"

private fun suExec(cmd: String): Int {
    return try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        val out = p.inputStream.bufferedReader().readText().trim()
        val err = p.errorStream.bufferedReader().readText().trim()
        val code = p.waitFor()
        Log.e(TAG, "su [$cmd] -> code=$code out='$out' err='$err'")
        code
    } catch (e: Exception) {
        Log.e(TAG, "su EXC [$cmd]: ${e.message}")
        -1
    }
}

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

    // [ShizuSU] 终解：root 权限精准改 launcher db intent + 清 icon 缓存 + force-stop
    runCatching {
        val launcherPkg = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0
        )?.activityInfo?.packageName
        if (!launcherPkg.isNullOrEmpty() && launcherPkg != context.packageName) {
            val pkg = context.packageName
            val dbPath = "/data/user_de/0/$launcherPkg/databases/launcher4x6.db"

            // 1. 放开目录 traverse + db 写权限
            suExec("chmod 755 /data/user_de/0 /data/user_de/0/$launcherPkg /data/user_de/0/$launcherPkg/databases")
            suExec("chmod 666 $dbPath")

            // 2. UPDATE favorites: intent 指向当前 component + 清 icon 缓存
            val targetComponent = if (useAlt) {
                "$pkg/${MainActivity::class.java.name}Alias"
            } else {
                "$pkg/${MainActivity::class.java.name}"
            }
            val newIntent = "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=$targetComponent;end"
            try {
                SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
                    db.execSQL(
                        "UPDATE favorites SET intent=?, iconPackage=NULL, iconResource=NULL, icon=NULL, iconType=0 WHERE intent LIKE '%$pkg%'",
                        arrayOf(newIntent)
                    )
                    Log.e(TAG, "DB UPDATE OK useAlt=$useAlt target=$targetComponent")
                }
            } catch (e: Exception) {
                Log.e(TAG, "DB UPDATE FAILED: ${e.message}")
            }

            // 3. 恢复权限 + force-stop launcher
            suExec("chmod 600 $dbPath")
            suExec("chmod 700 /data/user_de/0/$launcherPkg/databases /data/user_de/0/$launcherPkg")
            suExec("am force-stop $launcherPkg")
        }
    }
}
