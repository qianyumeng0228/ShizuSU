package com.sukisu.ultra.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.ui.screen.xposedrepo.XposedModule
import com.sukisu.ultra.ui.screen.xposedrepo.XposedRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.util.concurrent.TimeUnit

data class XposedRepoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val modules: List<XposedModule> = emptyList(),
)

class XposedRepoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(XposedRepoUiState())
    val uiState: StateFlow<XposedRepoUiState> = _uiState.asStateFlow()

    private val _selected = MutableStateFlow<XposedModule?>(null)
    val selected: StateFlow<XposedModule?> = _selected.asStateFlow()

    fun openDetail(m: XposedModule) { _selected.value = m }
    fun closeDetail() { _selected.value = null }

    companion object {
        private const val TAG = "XposedRepoVm"
        private val URLS = listOf(
            "https://backup.modules.lsposed.org/modules.json",
            "https://qianyumeng0228.github.io/ShizuSU-Xposed/modules.json",
            "https://raw.githubusercontent.com/qianyumeng0228/ShizuSU/main/docs/xposed/modules.json",
            "https://modules.lsposed.org/modules.json",
            "https://modules-blogcdn.lsposed.org/modules.json",
            "https://modules-cloudflare.lsposed.org/modules.json",
        )
    }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = withContext(Dispatchers.IO) { fetch() }
            result.onSuccess { mods ->
                _uiState.value = XposedRepoUiState(isLoading = false, modules = mods)
            }.onFailure { e ->
                Log.e(TAG, "fetch failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "unknown")
            }
        }
    }

    private fun fetch(): Result<List<XposedModule>> {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
        val errs = StringBuilder()

        // ETag cache: load cached body + etag
        val cacheFile = File(com.sukisu.ultra.ksuApp.cacheDir, "xposed_modules.json")
        val etagFile = File(com.sukisu.ultra.ksuApp.cacheDir, "xposed_modules.etag")
        var cachedBody: String? = null
        var cachedEtag: String? = null
        if (cacheFile.exists() && cacheFile.length() > 0) {
            cachedBody = cacheFile.readText()
            if (etagFile.exists()) cachedEtag = etagFile.readText()
            Log.e(TAG, "cache hit: ${cacheFile.length()} bytes, etag=$cachedEtag")
        }

        for (u in URLS) {
            try {
                val reqBuilder = Request.Builder().url(u)
                    .header("User-Agent", "ShizuSU-Manager/1.0")
                // Send ETag if we have cached content
                if (cachedEtag != null) reqBuilder.header("If-None-Match", cachedEtag)
                val req = reqBuilder.build()
                client.newCall(req).execute().use { resp ->
                    when {
                        resp.code == 304 && cachedBody != null -> {
                            // Not modified — use cache
                            Log.e(TAG, "304 Not Modified — using cached list")
                            return parseModules(cachedBody)
                        }
                        !resp.isSuccessful -> {
                            errs.append("$u -> ${resp.code}\n")
                            // Fall through to next URL
                        }
                        else -> {
                            val body = resp.body?.string() ?: return@use
                            val etag = resp.header("ETag") ?: resp.header("etag")
                            // Save to cache
                            try {
                                cacheFile.writeText(body)
                                if (etag != null) etagFile.writeText(etag)
                            } catch (e: Exception) {
                                Log.e(TAG, "cache write failed: ${e.message}")
                            }
                            return parseModules(body)
                        }
                    }
                }
            } catch (e: Exception) {
                errs.append("$u -> ${e.message}\n")
            }
        }
        // All URLs failed — try using cache as fallback
        if (cachedBody != null) {
            Log.e(TAG, "all mirrors failed — using stale cache")
            return parseModules(cachedBody)
        }
        return Result.failure(RuntimeException("all mirrors failed:\n$errs"))
    }

    private fun parseModules(body: String): Result<List<XposedModule>> {
        return runCatching {
            val arr = JSONArray(body)
            val out = ArrayList<XposedModule>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val authors = ArrayList<String>()
                val collab = o.optJSONArray("collaborators")
                if (collab != null) for (j in 0 until collab.length()) {
                    val c = collab.getJSONObject(j)
                    authors += c.optString("name").ifBlank { c.optString("login") }
                }
                val releases = ArrayList<XposedRelease>()
                val rels = o.optJSONArray("releases")
                if (rels != null) for (j in 0 until rels.length()) {
                    val r = rels.getJSONObject(j)
                    var dl = ""
                    var size = 0L
                    var count = 0
                    val assets = r.optJSONArray("releaseAssets")
                    if (assets != null && assets.length() > 0) {
                        val a = assets.getJSONObject(0)
                        dl = a.optString("downloadUrl", "")
                        size = a.optLong("size", 0L)
                        count = a.optInt("downloadCount", 0)
                    }
                    releases += XposedRelease(
                        name = r.optString("name", ""),
                        tagName = r.optString("tagName", ""),
                        createdAt = r.optString("createdAt", ""),
                        downloadUrl = dl,
                        size = size,
                        downloadCount = count,
                    )
                }
                out += XposedModule(
                    name = o.optString("name", ""),
                    summary = o.optString("summary", ""),
                    description = o.optString("description", ""),
                    url = o.optString("url", ""),
                    homepageUrl = o.optString("homepageUrl", ""),
                    sourceUrl = o.optString("sourceUrl", ""),
                    latestRelease = o.optString("latestRelease", ""),
                    latestReleaseTime = parseTime(o),
                    latestReleaseTimeStr = o.optString("latestReleaseTime", ""),
                    stars = o.optInt("stargazerCount", 0),
                    authors = authors,
                    releases = releases,
                    readme = o.optString("readme", ""),
                )
            }
            out.sortByDescending { it.latestReleaseTime }
            out
        }
    }

    private fun parseTime(o: org.json.JSONObject): Long {
        val v = o.opt("latestReleaseTime")
        val result = when (v) {
            is Long -> if (v < 10_000_000_000L) v * 1000L else v
            is Int -> v.toLong() * 1000L
            is Number -> v.toLong().let { if (it < 10_000_000_000L) it * 1000L else it }
            is String -> runCatching {
                val s = v.trim()
                if (s.matches(Regex("^\\d+$"))) {
                    s.toLong().let { if (it < 10_000_000_000L) it * 1000L else it }
                } else {
                    runCatching { java.time.Instant.parse(s).toEpochMilli() }.getOrElse {
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }.parse(s).time
                    }
                }
            }.getOrDefault(0L)
            else -> 0L
        }
        android.util.Log.e("XposedDetail", "parseTime raw=$v → ts=$result")
        return result
    }
}
