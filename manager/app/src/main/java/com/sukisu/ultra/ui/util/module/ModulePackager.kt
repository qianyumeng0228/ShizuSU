package com.sukisu.ultra.ui.util.module

import android.content.Context
import com.topjohnwu.superuser.Shell
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * [ShizuSU] 已安装模块打包：root 执行 `tar -czf /data/adb/modules/<id>`，
 * 经 commons-compress 解 tar.gz、剥顶层目录、剔除运行时状态文件后重打为标准模块 zip。
 *
 * 移植自 module-uploader/.../ModulePackager.kt（包名改 com.sukisu.ultra.ui.util.module）。
 */
object ModulePackager {

    /** 打包时需要排除的运行时/状态文件（与电脑端 zip_private.py 一致）。 */
    private val EXCLUDE_NAMES = setOf(
        "disable", "disable_after_update", "update", ".hma_config_done",
        "now_version", "Log.txt", "Number_of_brick_rescue.log", "Asphyxia.toml"
    )
    private val EXCLUDE_SUFFIXES = listOf(".log", ".log.old", ".bak", "~")

    /** 列出已安装模块（含 module.prop 的目录）。返回 id -> module.prop 文本。 */
    fun listInstalledModules(): List<Pair<String, String>> {
        val dirs = Shell.cmd("ls /data/adb/modules 2>/dev/null").exec().out.filter { it.isNotBlank() }
        val result = mutableListOf<Pair<String, String>>()
        for (id in dirs) {
            val prop = Shell.cmd("cat /data/adb/modules/$id/module.prop 2>/dev/null").exec().out
                .joinToString("\n").trim()
            if (prop.isNotBlank()) result.add(id to prop)
        }
        return result.sortedBy { it.first.lowercase() }
    }

    private fun shouldExclude(name: String): Boolean =
        name in EXCLUDE_NAMES || EXCLUDE_SUFFIXES.any { name.endsWith(it) }

    /**
     * 打包已安装模块为标准模块 zip（module.prop 位于 zip 根）。
     * @return 生成的 zip 文件；root 不可用/模块缺 module.prop 时抛错。
     */
    fun packInstalled(context: Context, moduleId: String): File {
        val cacheDir = File(context.cacheDir, "pack").apply { mkdirs() }
        val tarFile = File(cacheDir, "$moduleId.tar.gz")
        val zipFile = File(cacheDir, "$moduleId.zip")
        tarFile.delete(); zipFile.delete()

        val rc = Shell.cmd(
            "cd /data/adb/modules && tar -czf '${tarFile.absolutePath}' '$moduleId' 2>/dev/null"
        ).exec()
        if (rc.code != 0 || !tarFile.exists()) {
            throw IllegalStateException("root 打包失败（需要 root 权限）")
        }

        var propInRoot = false
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(tarFile)))).use { tis ->
                var entry = tis.nextEntry
                while (entry != null) {
                    if (entry.isFile) {
                        var name = entry.name
                        // tar 含顶层目录 <id>/，剥掉
                        if (name.startsWith("$moduleId/")) name = name.removePrefix("$moduleId/")
                        else if (name == moduleId) { entry = tis.nextEntry; continue }
                        if (name.isEmpty()) { entry = tis.nextEntry; continue }
                        val base = name.substringAfterLast('/')
                        if (shouldExclude(base)) { entry = tis.nextEntry; continue }
                        if (name == "module.prop") propInRoot = true
                        zos.putNextEntry(ZipEntry(name))
                        tis.copyTo(zos, 1 shl 16)
                        zos.closeEntry()
                    }
                    entry = tis.nextEntry
                }
            }
        }
        if (!propInRoot) throw IllegalStateException("模块缺少 module.prop，无法打包")
        tarFile.delete()
        return zipFile
    }
}

/** module.prop 解析。 */
data class ModulePropInfo(
    val id: String,
    val name: String,
    val author: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val updateJson: String
) {
    companion object {
        fun parse(text: String): ModulePropInfo {
            val kv = mutableMapOf<String, String>()
            for (line in text.lineSequence()) {
                val i = line.indexOf('=')
                if (i > 0) kv[line.substring(0, i).trim()] = line.substring(i + 1).trim()
            }
            fun get(k: String) = kv[k].orEmpty()
            return ModulePropInfo(
                id = get("id"),
                name = get("name"),
                author = get("author"),
                version = get("version"),
                versionCode = get("versionCode").toIntOrNull() ?: 0,
                description = get("description"),
                updateJson = get("updateJson")
            )
        }
    }
}
