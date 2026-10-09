package com.sukisu.ultra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.ui.screen.apprepo.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppRepoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val apps: List<AppEntry> = emptyList(),
)

class AppRepoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppRepoUiState())
    val uiState: StateFlow<AppRepoUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AppRepoVm"
        private val URLS = listOf(
            "https://raw.githubusercontent.com/qianyumeng0228/ShizuSU-Apps/main/apps.json",
            "https://cdn.jsdelivr.net/gh/qianyumeng0228/ShizuSU-Apps@main/apps.json",
            "https://ghproxy.net/https://raw.githubusercontent.com/qianyumeng0228/ShizuSU-Apps/main/apps.json",
        )
    }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = withContext(Dispatchers.IO) { fetch() }
            result.onSuccess { apps ->
                _uiState.value = AppRepoUiState(isLoading = false, apps = apps)
            }.onFailure { e ->
                Log.e(TAG, "fetch failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "unknown")
            }
        }
    }

    private fun fetch(): Result<List<AppEntry>> {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        var lastErr: Exception? = null
        for (url in URLS) {
            try {
                val req = Request.Builder().url(url)
                    .header("User-Agent", "ShizuSU-Manager/1.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    Log.e(TAG, "apps.json $url -> ${resp.code}")
                    if (!resp.isSuccessful) return@use
                    val body = resp.body?.string().orEmpty()
                    val root = JSONObject(body)
                    val arr = root.getJSONArray("apps")
                    val out = ArrayList<AppEntry>(arr.length())
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out += AppEntry(
                            id = o.optString("id", ""),
                            name = o.optString("name", ""),
                            pkg = o.optString("package", ""),
                            versionName = o.optString("versionName", ""),
                            versionCode = o.optLong("versionCode", 0L),
                            summary = o.optString("summary", ""),
                            author = o.optString("author", ""),
                            downloadUrl = o.optString("downloadUrl", ""),
                            updatedAt = o.optLong("updatedAt", 0L),
                        )
                    }
                    out.sortByDescending { it.updatedAt }
                    return Result.success(out)
                }
            } catch (e: Exception) {
                lastErr = e
                Log.e(TAG, "apps.json $url FAIL: ${e.message}")
            }
        }
        return Result.failure(lastErr ?: RuntimeException("all mirrors failed"))
    }
}
