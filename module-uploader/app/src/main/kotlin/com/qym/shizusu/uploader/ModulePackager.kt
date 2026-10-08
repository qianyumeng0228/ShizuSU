package com.qym.shizusu.uploader

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

object ModulePackager {

    private val EXCLUDE_NAMES = setOf(
        "disable", "disable_after_update", "update", ".hma_config_done",
        "now_version", "Log.txt", "Number_of_brick_rescue.log", "Asphyxia.toml"
    )
    private val EXCLUDE_SUFFIXES = listOf(".log", ".log.old", ".bak", "~")

    fun listInstalledModules(): List<Pair<String, String>> {
        val out = Shell.cmd("ls /data/adb/modules 2>/dev/null").exec().out.filter { it.isNotBlank() }
        val result = ArrayList<Pair<String, String>>()
        for (id in out) {
            val prop = Shell.cmd("cat /data/adb/modules/$id/module.prop 2>/dev/null").exec().out.joinToString("\n").trim()
            if (prop.isNotBlank()) result.add(id to prop)
        }
        return result.sortedBy { it.first.lowercase() }
    }

    private fun shouldExclude(name: String): Boolean {
        if (name in EXCLUDE_NAMES) return true
        return EXCLUDE_SUFFIXES.any { name.endsWith(it) }
    }

    fun packInstalled(context: Context, moduleId: String): File {
        val dir = File(context.cacheDir, "pack").apply { mkdirs() }
        val tgz = File(dir, "$moduleId.tar.gz")
        val zip = File(dir, "$moduleId.zip")
        tgz.delete(); zip.delete()
        val exec = Shell.cmd("cd /data/adb/modules && tar -czf '${tggPath(tgz)}' '$moduleId' 2>/dev/null").exec()
        if (exec.code != 0 || !tgz.exists()) throw IllegalStateException("root 打包失败（需要 root 权限）")

        var hasProp = false
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zip))).use { zos ->
            TarArchiveInputStream(GzipCompressorInputStream(BufferedInputStream(FileInputStream(tgz)))).use { tis ->
                var entry = tis.nextEntry
                while (entry != null) {
                    if (entry.isFile) {
                        var name = entry.name
                        if (name.startsWith("$moduleId/")) {
                            name = name.removePrefix("$moduleId/")
                        } else if (name == moduleId) {
                            entry = tis.nextEntry; continue
                        }
                        if (name.isEmpty()) { entry = tis.nextEntry; continue }
                        if (shouldExclude(name.substringAfterLast('/'))) { entry = tis.nextEntry; continue }
                        if (name == "module.prop") hasProp = true
                        zos.putNextEntry(ZipEntry(name))
                        tis.copyTo(zos, 65536)
                        zos.closeEntry()
                    }
                    entry = tis.nextEntry
                }
            }
        }
        if (!hasProp) throw IllegalStateException("模块缺少 module.prop，无法打包")
        tgz.delete()
        return zip
    }

    private fun tggPath(f: File) = f.absolutePath
}
