package com.qym.shizusu.uploader

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.util.zip.ZipFile

class ApprovalViewModel(private val app: Context) : ViewModel() {

    val hasToken: StateFlow<Boolean> = MutableStateFlow(TokenStore.hasToken(app))
    val submissions = MutableStateFlow<List<SubmissionIssue>>(emptyList())
    val selected = MutableStateFlow<SubmissionIssue?>(null)
    val payload = MutableStateFlow<SubmissionPayload?>(null)
    val pendingZipSize = MutableStateFlow(-1L)
    val pendingProp = MutableStateFlow<ModuleProp?>(null)
    val state = MutableStateFlow<ApprovalState>(ApprovalState.Idle)

    private var pendingZip: ByteArray? = null

    fun refresh() {
        val token = TokenStore.loadToken(app)
        if (token.isNullOrBlank()) {
            state.value = ApprovalState.Error("请先配置令牌")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val arr = GitHubApi(token).listSubmissions()
                val list = ArrayList<SubmissionIssue>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        SubmissionIssue(
                            number = o.optInt("number"),
                            title = o.optString("title"),
                            createdAt = o.optString("created_at"),
                            body = o.optString("body")
                        )
                    )
                }
                submissions.value = list
            } catch (e: Exception) {
                state.value = ApprovalState.Error("加载失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun openDetail(issue: SubmissionIssue) {
        val token = TokenStore.loadToken(app)
        if (token.isNullOrBlank()) {
            state.value = ApprovalState.Error("请先配置令牌")
            return
        }
        selected.value = issue
        pendingZip = null
        pendingZipSize.value = -1L
        pendingProp.value = null
        payload.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.value = ApprovalState.Loading("解析提交信息…")
                val p = parsePayload(issue.body) ?: throw IllegalStateException("issue 正文缺少 ```json 提交信息块")
                if (p.gistId.isBlank() || p.shards.isEmpty()) throw IllegalStateException("提交信息缺少 gistId 或分片列表")
                payload.value = p
                val api = GitHubApi(token)
                state.value = ApprovalState.Loading("下载 gist 分片（${p.shards.size}）…")
                val sb = StringBuilder()
                for (name in p.shards) sb.append(api.getGistFile(p.gistId, name).trim())
                val bytes = Base64.decode(sb.toString(), Base64.DEFAULT)
                state.value = ApprovalState.Loading("校验 module.prop…")
                val f = File(app.cacheDir, "approve_${p.moduleId}.zip")
                f.writeBytes(bytes)
                ZipFile(f).use { zf ->
                    val entry = zf.getEntry("module.prop") ?: throw IllegalStateException("zip 内没有 module.prop")
                    val prop = zf.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { ModuleProp.parse(it.readText()) }
                    if (prop.id.isNotBlank() && prop.id != p.moduleId)
                        throw IllegalStateException("module.prop id=${prop.id} 与提交的 ${p.moduleId} 不一致")
                    pendingZip = bytes
                    pendingZipSize.value = bytes.size.toLong()
                    pendingProp.value = prop
                }
                state.value = ApprovalState.Idle
            } catch (e: Exception) {
                state.value = ApprovalState.Error("详情加载失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun closeDetail() {
        selected.value = null
        pendingZip = null
        pendingZipSize.value = -1L
        pendingProp.value = null
        payload.value = null
        state.value = ApprovalState.Idle
    }

    fun approve() {
        val token = TokenStore.loadToken(app) ?: run {
            state.value = ApprovalState.Error("请先配置令牌"); return
        }
        val issue = selected.value
        val p = payload.value
        val zip = pendingZip
        if (issue == null || p == null) return
        if (zip == null) {
            state.value = ApprovalState.Error("zip 未就绪，请重新打开详情"); return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.value = ApprovalState.Loading("推送入库…")
                val api = GitHubApi(token)
                val prop = pendingProp.value
                val moduleId = p.moduleId
                var moduleName = p.moduleName
                if (moduleName.isBlank()) moduleName = prop?.name ?: moduleId
                var author = p.author
                if (author.isBlank()) author = prop?.author ?: "unknown"
                var versionName = p.versionName
                if (versionName.isBlank()) versionName = prop?.version ?: "v1.0"
                val versionCode = if (p.versionCode > 0) p.versionCode else (prop?.versionCode ?: 1)
                var summaryZh = p.summaryZh
                if (summaryZh.isBlank()) summaryZh = moduleName
                val time = Instant.now().toString()
                val downloadUrl = "https://github.com/qianyumeng0228/ShizuSU-Modules/raw/main/private/$moduleId.zip"

                // modules.json（顶层数组）
                val existing = api.getRepoFile("modules.json") ?: "[]"
                val arr = JSONArray(existing)
                val keep = JSONArray()
                for (i in 0 until arr.length()) {
                    if (arr.getJSONObject(i).optString("moduleId") != moduleId) keep.put(arr.getJSONObject(i))
                }
                keep.put(ModuleJsonBuilder.catalogEntry(moduleId, moduleName, author, summaryZh, p.zygisk, versionName, versionCode, downloadUrl, time))

                // modules.config.json（对象结构，保留顶层键）
                val cfgText = api.getRepoFile("modules.config.json")
                val cfgObj = if (cfgText != null) JSONObject(cfgText)
                else JSONObject().put("modules", JSONArray()).put("outputDir", "output").put("cacheDir", "cache").put("cacheTtlSeconds", 3600)
                val cfgArr = cfgObj.optJSONArray("modules") ?: JSONArray()
                val cfgKeep = JSONArray()
                for (i in 0 until cfgArr.length()) {
                    if (cfgArr.getJSONObject(i).optString("moduleId") != moduleId) cfgKeep.put(cfgArr.getJSONObject(i))
                }
                cfgKeep.put(ModuleJsonBuilder.configEntry(moduleId, moduleName, author, summaryZh, summaryZh, p.zygisk, versionName, versionCode, downloadUrl, zip.size.toLong(), time))
                cfgObj.put("modules", cfgKeep)

                val detail = ModuleJsonBuilder.detail(moduleId, summaryZh, p.zygisk, versionName, downloadUrl, zip.size.toLong(), time)

                api.pushFiles(
                    "chore: 审批通过上架模块 $moduleId（$moduleName）",
                    mapOf(
                        "private/$moduleId.zip" to zip,
                        "modules.json" to keep.toString().toByteArray(Charsets.UTF_8),
                        "modules.config.json" to cfgObj.toString().toByteArray(Charsets.UTF_8),
                        "module/$moduleId.json" to detail.toString().toByteArray(Charsets.UTF_8)
                    )
                )
                state.value = ApprovalState.Loading("评论并关闭 issue…")
                api.commentIssue(issue.number, "已通过，已上架 $moduleName（$moduleId）。\n下载地址：$downloadUrl")
                api.closeIssue(issue.number)
                state.value = ApprovalState.Success("已通过并上架 $moduleName")
                refresh()
                closeDetail()
            } catch (e: Exception) {
                state.value = ApprovalState.Error("通过失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun reject(reason: String) {
        val token = TokenStore.loadToken(app) ?: run {
            state.value = ApprovalState.Error("请先配置令牌"); return
        }
        val issue = selected.value ?: return
        if (reason.isBlank()) {
            state.value = ApprovalState.Error("拒绝原因不能为空"); return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.value = ApprovalState.Loading("评论并关闭 issue…")
                val api = GitHubApi(token)
                api.commentIssue(issue.number, "已拒绝：$reason")
                api.closeIssue(issue.number)
                state.value = ApprovalState.Success("已拒绝并关闭 issue")
                refresh()
                closeDetail()
            } catch (e: Exception) {
                state.value = ApprovalState.Error("操作失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private fun parsePayload(body: String): SubmissionPayload? {
        val m = Regex("```(?:json)?\\s*\\r?\\n(.*?)```", RegexOption.DOT_MATCHES_ALL).find(body) ?: return null
        return try {
            val o = JSONObject(m.groupValues[1].trim())
            val shardsArr = o.optJSONArray("shards") ?: JSONArray()
            val shards = ArrayList<String>()
            for (i in 0 until shardsArr.length()) shards.add(shardsArr.getString(i))
            SubmissionPayload(
                gistId = o.optString("gistId"),
                shards = shards,
                moduleId = o.optString("moduleId"),
                moduleName = o.optString("moduleName"),
                author = o.optString("author"),
                versionName = o.optString("versionName"),
                versionCode = o.optInt("versionCode", 0),
                summaryZh = o.optString("summaryZh"),
                zygisk = o.optBoolean("zygisk", false)
            )
        } catch (e: Exception) {
            null
        }
    }
}
