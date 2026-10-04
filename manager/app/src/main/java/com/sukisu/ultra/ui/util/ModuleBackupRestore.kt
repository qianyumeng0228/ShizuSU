package com.sukisu.ultra.ui.util

import android.net.Uri
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.sukisu.ultra.ksuApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [ShizuSU Phase3 · 补丁3] 模块 / allowlist 备份恢复（App 侧 SAF 导出）。
 *
 * 照搬自 KernelSU-Next manager `BackupRestore.kt`（upstreams/next/manager/app/src/main/java/
 * com/rifsxd/ksunext/ui/screen/BackupRestore.kt）的四个 IO 函数，包名改 com.sukisu.ultra；
 * tar 命令逐字保留；导出建议文件名按 N1 定案加品牌前缀。
 *
 * tar 布局与 ksud `module backup` 同源 busybox tar，故 App SAF 导出的 tar 与 ksud 内部导出
 * 字节兼容、可互相恢复：
 *  - 备份 modules   : Next :61  `tar -cpf tmp -C /data/adb/modules $(ls /data/adb/modules)`
 *  - 备份 allowlist : Next :89  `tar -cpf tmp -C /data/adb/ksu .allowlist`
 *  - 还原 modules   : Next :126 `tar -xpf tmp -C /data/adb/modules_update`（重启生效）
 *  - 还原 allowlist : Next :152 `tar -xpf tmp -C /data/adb/ksu`
 *
 * 命名说明（N1 定案，n1-backup-prefix-evidence.md §五）：
 *  - 用户可见建议名 = `shisu_modules_backup_<yyyyMMdd_HHmmss>.tar` /
 *                    `shisu_allowlist_backup_<yyyyMMdd_HHmmss>.tar`
 *  - cache 临时名沿用 Next 的 `modules_backup_<ts>.tar` / `allowlist_backup_<ts>.tar`（用完即删）。
 *  - 不与 boot 镜像备份前缀（/data/adb/ksu/ksu_backup_<sha1>）撞名：词位第二段区分用途。
 *
 * 注：基线 `ui/screen/settings/tools/ToolsUtils.kt` 已有**裸 cp** 版 allowlist 导出
 * （`ksu_allowlist_backup.bin`，无时间戳）。本文件提供的是 Next 的 **tar** 版 allowlist
 * 导出，函数名加 `Tar` 后缀以区分、不覆盖旧实现。
 */

private const val BUSYBOX = "/data/adb/ksu/bin/busybox"

private fun backupTimestamp(): String =
    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

/** N1 定案：模块备份建议文件名（写入 ACTION_CREATE_DOCUMENT 的 EXTRA_TITLE，用户可见）。 */
fun shisuModulesBackupSuggestedName(): String =
    "shisu_modules_backup_${backupTimestamp()}.tar"

/** N1 定案：allowlist 备份建议文件名。 */
fun shisuAllowlistBackupSuggestedName(): String =
    "shisu_allowlist_backup_${backupTimestamp()}.tar"

/** 以 root 执行一条 shell 命令并返回是否成功（沿用基线 ToolsUtils.kt 的 Shell.cmd idiom）。 */
private fun shellOk(cmd: String): Boolean =
    runCatching { Shell.cmd(cmd).exec().isSuccess }.getOrDefault(false)

/**
 * 备份 /data/adb/modules 整个目录为 tar，流式写入 [destUri]。
 * 照搬 Next `BackupRestore.kt:54-77`（含空目录早退、finally 删临时文件）。
 */
suspend fun backupModulesToUri(destUri: Uri): Boolean = withContext(Dispatchers.IO) {
    val modulesDir = SuFile("/data/adb/modules")
    if (modulesDir.listFiles()?.isEmpty() != false) return@withContext false

    val tmpPath = "${ksuApp.cacheDir}/modules_backup_${backupTimestamp()}.tar"
    val tarCmd = "$BUSYBOX tar -cpf '$tmpPath' -C /data/adb/modules \$(ls /data/adb/modules)"
    if (!shellOk(tarCmd)) return@withContext false

    return@withContext try {
        SuFile(tmpPath).newInputStream().use { input ->
            ksuApp.contentResolver.openOutputStream(destUri)?.use { out ->
                input.copyTo(out)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        SuFile(tmpPath).delete()
    }
}

/**
 * 备份 /data/adb/ksu/.allowlist 为 tar，流式写入 [destUri]。
 * 照搬 Next `BackupRestore.kt:83-105`。
 */
suspend fun backupAllowlistToUriTar(destUri: Uri): Boolean = withContext(Dispatchers.IO) {
    if (!SuFile("/data/adb/ksu/.allowlist").exists()) return@withContext false

    val tmpPath = "${ksuApp.cacheDir}/allowlist_backup_${backupTimestamp()}.tar"
    val tarCmd = "$BUSYBOX tar -cpf '$tmpPath' -C /data/adb/ksu .allowlist"
    if (!shellOk(tarCmd)) return@withContext false

    return@withContext try {
        SuFile(tmpPath).newInputStream().use { input ->
            ksuApp.contentResolver.openOutputStream(destUri)?.use { out ->
                input.copyTo(out)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        SuFile(tmpPath).delete()
    }
}

/**
 * 从 [srcUri] 选 tar 还原模块到 /data/adb/modules_update（重启后生效）。
 * 照搬 Next `BackupRestore.kt:111-131`。
 */
suspend fun restoreModulesFromUri(srcUri: Uri): Boolean = withContext(Dispatchers.IO) {
    val tmpPath = "${ksuApp.cacheDir}/modules_restore_${backupTimestamp()}.tar"
    try {
        ksuApp.contentResolver.openInputStream(srcUri)?.use { input ->
            SuFile(tmpPath).newOutputStream().use { out ->
                input.copyTo(out)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext false
    }

    val extractCmd =
        "mkdir -p '/data/adb/modules_update' && $BUSYBOX tar -xpf '$tmpPath' -C /data/adb/modules_update"
    val result = shellOk(extractCmd)

    SuFile(tmpPath).delete()
    return@withContext result
}

/**
 * 从 [srcUri] 选 tar 还原 allowlist 到 /data/adb/ksu。
 * 照搬 Next `BackupRestore.kt:137-157`。
 */
suspend fun restoreAllowlistFromUriTar(srcUri: Uri): Boolean = withContext(Dispatchers.IO) {
    val tmpPath = "${ksuApp.cacheDir}/allowlist_restore_${backupTimestamp()}.tar"
    try {
        ksuApp.contentResolver.openInputStream(srcUri)?.use { input ->
            SuFile(tmpPath).newOutputStream().use { out ->
                input.copyTo(out)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext false
    }

    val extractCmd = "$BUSYBOX tar -xpf '$tmpPath' -C /data/adb/ksu"
    val result = shellOk(extractCmd)

    SuFile(tmpPath).delete()
    return@withContext result
}
