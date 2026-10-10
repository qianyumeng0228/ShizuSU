package com.sukisu.ultra.ui.viewmodel

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
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class XposedRepoUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val modules: List<XposedModule> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
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
        // Full list fallback URLs
        private val FULL_URLS = listOf(
            "https://backup.modules.lsposed.org/modules.json",
            "https://qianyumeng0228.github.io/ShizuSU-Xposed/modules.json",
            "https://raw.githubusercontent.com/qianyumeng0228/ShizuSU/main/docs/xposed/modules.json",
            "https://modules.lsposed.org/modules.json",
        )
        // Pagination base URL (ShizuSU-Xposed GitHub Pages)
        private const val PAGE_BASE = "https://qianyumeng0228.github.io/ShizuSU-Xposed/module-list"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = withContext(Dispatchers.IO) { fetchPage(1) }
            result.onSuccess { (mods, total, totalPages) ->
                _uiState.value = XposedRepoUiState(
                    isLoading = false,
                    modules = mods,
                    totalCount = total,
                    currentPage = 1,
                    totalPages = totalPages,
                )
            }.onFailure { e ->
                Log.e(TAG, "refresh failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "unknown")
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage >= state.totalPages) return
        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            val nextPage = state.currentPage + 1
            val result = withContext(Dispatchers.IO) { fetchPage(nextPage) }
            result.onSuccess { (mods, total, totalPages) ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    modules = state.modules + mods,
                    currentPage = nextPage,
                    totalCount = total,
                    totalPages = totalPages,
                )
            }.onFailure { e ->
                Log.e(TAG, "loadMore failed", e)
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }

    /** Fetch a single page. Returns (modules, totalCount, totalPages) */
    private fun fetchPage(page: Int): Result<Triple<List<XposedModule>, Int, Int>> {
        // Try pagination first
        val pageUrl = "$PAGE_BASE/page_$page.json"
        try {
            val req = Request.Builder().url(pageUrl)
                .header("User-Agent", "ShizuSU-Manager/1.0")
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@use
                    val obj = JSONObject(body)
                    val total = obj.optInt("total", 0)
                    val totalPages = obj.optInt("totalPages", 0)
                    val arr = obj.optJSONArray("modules") ?: JSONArray()
                    val mods = parseModuleArray(arr)
                    Log.e(TAG, "page $page loaded: ${mods.size} mods, total=$total")
                    return Result.success(Triple(mods, total, totalPages))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "page $page fetch failed: ${e.message}")
        }

        // Pagination failed — for page 1, fallback to full modules.json
        if (page == 1) {
            Log.e(TAG, "falling back to full modules.json")
            return fetchFullList()
        }
        return Result.failure(RuntimeException("page $page not available"))
    }

    /** Fallback: fetch full modules.json from any mirror */
    private fun fetchFullList(): Result<Triple<List<XposedModule>, Int, Int>> {
        val errs = StringBuilder()
        for (u in FULL_URLS) {
            try {
                val req = Request.Builder().url(u)
                    .header("User-Agent", "ShizuSU-Manager/1.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) { errs.append("$u -> ${resp.code}\n"); return@use }
                    val body = resp.body?.string() ?: return@use
                    val arr = JSONArray(body)
                    val mods = parseModuleArray(arr)
                    Log.e(TAG, "full list loaded: ${mods.size} mods")
                    return Result.success(Triple(mods, mods.size, 1))
                }
            } catch (e: Exception) {
                errs.append("$u -> ${e.message}\n")
            }
        }
        return Result.failure(RuntimeException("all mirrors failed:\n$errs"))
    }

    private fun parseModuleArray(arr: JSONArray): List<XposedModule> {
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
        return out
    }

    private fun parseTime(o: JSONObject): Long {
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
        return result
    }
}
