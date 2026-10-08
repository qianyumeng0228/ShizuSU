package com.sukisu.ultra.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.ui.screen.xposedrepo.XposedModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class XposedRepoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val modules: List<XposedModule> = emptyList(),
)

class XposedRepoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(XposedRepoUiState())
    val uiState: StateFlow<XposedRepoUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "XposedRepoVm"
        private val URLS = listOf(
            "https://modules.lsposed.org/modules.json",
            "https://modules-blogcdn.lsposed.org/modules.json",
            "https://modules-cloudflare.lsposed.org/modules.json",
            "https://raw.githubusercontent.com/qianyumeng0228/ShizuSU/main/docs/xposed/modules.json",
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
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val errs = StringBuilder()
        for (u in URLS) {
            try {
                val req = Request.Builder().url(u)
                    .header("User-Agent", "ShizuSU-Manager/1.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) { errs.append("$u -> ${resp.code}\n"); return@use }
                    val body = resp.body?.string() ?: return@use
                    val arr = JSONArray(body)
                    val out = ArrayList<XposedModule>(arr.length())
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out += XposedModule(
                            name = o.optString("name", ""),
                            summary = o.optString("summary", ""),
                            description = o.optString("description", ""),
                            url = o.optString("url", ""),
                            homepageUrl = o.optString("homepageUrl", ""),
                            sourceUrl = o.optString("sourceUrl", ""),
                            latestRelease = o.optString("latestRelease", ""),
                            latestReleaseTime = o.optLong("latestReleaseTime", 0L),
                        )
                    }
                    out.sortByDescending { it.latestReleaseTime }
                    return Result.success(out)
                }
            } catch (e: Exception) {
                errs.append("$u -> ${e.message}\n")
            }
        }
        return Result.failure(RuntimeException("all mirrors failed:\n$errs"))
    }
}
