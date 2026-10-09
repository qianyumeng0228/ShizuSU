package com.sukisu.ultra.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Simple APK downloader + installer.
 * UI structure reference: LSPosed (GPL-3.0), code rewritten.
 * Downloads to cacheDir then launches ACTION_VIEW install intent via FileProvider.
 */
object ApkDownloader {
    private const val TAG = "ApkDownloader"

    sealed class DlState {
        object Idle : DlState()
        data class Downloading(val percent: Int) : DlState()
        object Done : DlState()
        data class Failed(val msg: String) : DlState()
    }

    /** key = fileName, value = state */
    val states = MutableStateFlow<Map<String, DlState>>(emptyMap())

    private val fastClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val dlClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    /** GitHub release download mirror prefixes (CN-friendly). Tried in order before direct URL. */
    private val GH_MIRRORS = listOf(
        "https://ghproxy.net/",
        "https://mirror.ghproxy.com/",
        "https://gh-proxy.com/",
    )

    private fun buildCandidates(url: String): List<String> {
        if (!url.startsWith("https://github.com/") || !url.contains("/releases/download/")) {
            return listOf(url)
        }
        val out = ArrayList<String>(GH_MIRRORS.size + 1)
        for (m in GH_MIRRORS) out.add(m + url)
        out.add(url)
        return out
    }

    suspend fun downloadAndInstall(ctx: Context, url: String, fileName: String): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                states.value = states.value + (fileName to DlState.Downloading(0))
                val candidates = buildCandidates(url)
                var lastErr: Exception? = null
                for (cand in candidates) {
                    try {
                        Log.i(TAG, "trying: $cand")
                        val req = Request.Builder().url(cand)
                            .header("User-Agent", "ShizuSU-Manager/1.0")
                            .build()
                        dlClient.newCall(req).execute().use { resp ->
                            if (!resp.isSuccessful) {
                                lastErr = RuntimeException("HTTP ${resp.code} for $cand")
                                Log.w(TAG, "fail: $cand -> ${resp.code}")
                                return@use
                            }
                            val body = resp.body
                            val total = body.contentLength()
                            val out = File(ctx.cacheDir, fileName)
                            var downloaded = 0L
                            body.byteStream().use { input ->
                                out.outputStream().use { fos ->
                                    val buf = ByteArray(8192)
                                    while (true) {
                                        val n = input.read(buf)
                                        if (n < 0) break
                                        fos.write(buf, 0, n)
                                        downloaded += n
                                        if (total > 0) {
                                            val pct = (downloaded * 100 / total).toInt()
                                            states.value = states.value + (fileName to DlState.Downloading(pct))
                                        }
                                    }
                                }
                            }
                            Log.i(TAG, "downloaded ${out.length()} bytes via $cand")
                            states.value = states.value + (fileName to DlState.Done)
                            install(ctx, out)
                            return@runCatching out
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "error on $cand: ${e.message}")
                        lastErr = e
                    }
                }
                throw lastErr ?: RuntimeException("all candidates failed")
            }.onFailure { e ->
                Log.e(TAG, "download failed", e)
                states.value = states.value + (fileName to DlState.Failed(e.message ?: "error"))
            }
        }

    private fun install(ctx: Context, apk: File) {
        val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }
}
