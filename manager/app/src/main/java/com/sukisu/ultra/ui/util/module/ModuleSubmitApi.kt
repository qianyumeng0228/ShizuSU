package com.sukisu.ultra.ui.util.module

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Gist 创建结果：id + 分片文件名列表（按顺序）。 */
data class GistUploadResult(
    val gistId: String,
    val shards: List<String>,
)

/**
 * [ShizuSU] 社区模块提交 API（GitHub 原生无服务器方案）。
 *
 * 流程：
 *  1. 把模块 zip 整体 Base64 编码后按 < [SHARD_LIMIT] 字节切片；
 *  2. POST /gists 创建匿名/私有 Gist，files 名为 `<moduleId>.b64` / `<moduleId>.b64.p2` ……
 *  3. POST /repos/qianyumeng0228/ShizuSU-Modules/issues 发起 issue，
 *     body 为 JSON { gistId, shards, moduleId, moduleName, author, versionName, versionCode, summaryZh, zygisk }，
 *     labels=["submission"]。管理员审批后按 shards 顺序从 Gist 取分片拼接 base64 解码入库。
 *
 * 提交者令牌只需要 `gist` scope（无需仓库写权限）。令牌仅内存持有，不持久化。
 */
class ModuleSubmitApi(private val token: String) {

    companion object {
        const val REPO = "qianyumeng0228/ShizuSU-Modules"
        /** 每片 base64 文本大小上限（字节），留余量 < 800KB。 */
        const val SHARD_LIMIT = 700 * 1024
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private const val BASE = "https://api.github.com"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .build()

    private fun request(method: String, url: String, body: String? = null): okhttp3.Response {
        val reqBuilder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("User-Agent", "ShizuSU-ModuleSubmit")
            .header("Accept", "application/vnd.github+json")
        val rb = body?.toRequestBody(JSON)
        reqBuilder.method(method, rb)
        return client.newCall(reqBuilder.build()).execute()
    }

    private fun postJson(path: String, body: JSONObject): JSONObject {
        request("POST", "$BASE/$path", body.toString()).use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IOException("POST $path -> ${resp.code}: $text")
            }
            return JSONObject(text)
        }
    }

    /** 校验令牌（GET /user）。成功返回 login，失败抛 IOException。 */
    fun verifyToken(): String {
        request("GET", "$BASE/user", null).use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IOException("令牌无效或权限不足（HTTP ${resp.code}）")
            return JSONObject(text).optString("login", "?")
        }
    }

    /**
     * 创建 Gist 并写入 base64 分片。
     *
     * @param moduleId 模块 id，用于文件名前缀
     * @param zipBytes 模块 zip 原始字节
     * @param onShardUploaded 每片上传完成回调（已上传片数 / 总片数）
     * @return [GistUploadResult]（gist id + 分片文件名按顺序列表）
     */
    fun createGistWithShards(
        moduleId: String,
        zipBytes: ByteArray,
        onShardUploaded: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): GistUploadResult {
        // 1) base64 整体编码（NO_WRAP 便于切片）
        val b64 = android.util.Base64.encodeToString(zipBytes, android.util.Base64.NO_WRAP)
        // 2) 切片
        val chunks = b64.chunked(SHARD_LIMIT)
        // 3) 构造 files 映射：第一片 <id>.b64，其后 <id>.b64.pN
        val filesObj = JSONObject()
        val shardNames = ArrayList<String>(chunks.size)
        chunks.forEachIndexed { idx, chunk ->
            val name = if (idx == 0) "$moduleId.b64" else "$moduleId.b64.p${idx + 1}"
            shardNames.add(name)
            filesObj.put(name, JSONObject().put("content", chunk))
            onShardUploaded(idx, chunks.size) // 已组装 idx 片（上传前）
        }
        // 4) 一次性 POST /gists（files 全部带上）
        val payload = JSONObject()
            .put("description", "ShizuSU 模块提交: $moduleId")
            .put("public", false)
            .put("files", filesObj)
        val resp = postJson("gists", payload)
        val gistId = resp.optString("id", "")
        if (gistId.isEmpty()) throw IOException("Gist 创建失败：未返回 id")
        onShardUploaded(chunks.size, chunks.size)
        return GistUploadResult(gistId = gistId, shards = shardNames)
    }

    /**
     * 在 ShizuSU-Modules 仓库创建提交 issue。
     *
     * @param shards Gist 内分片文件名列表（按顺序），审批端按此顺序拼接 base64
     * @return issue number
     */
    fun createSubmissionIssue(
        gistId: String,
        shards: List<String>,
        moduleId: String,
        moduleName: String,
        author: String,
        versionName: String,
        versionCode: Int,
        summaryZh: String,
        zygisk: Boolean
    ): Int {
        val shardsArr = JSONArray()
        shards.forEach { shardsArr.put(it) }
        val meta = JSONObject()
            .put("gistId", gistId)
            .put("shards", shardsArr)
            .put("moduleId", moduleId)
            .put("moduleName", moduleName)
            .put("author", author)
            .put("versionName", versionName)
            .put("versionCode", versionCode)
            .put("summaryZh", summaryZh)
            .put("zygisk", zygisk)
        val title = "[提交] $moduleName"
        val body = buildString {
            appendLine("```json")
            append(meta.toString(2))
            appendLine()
            appendLine("```")
        }
        val payload = JSONObject()
            .put("title", title)
            .put("labels", JSONArray().put("submission"))
            .put("body", body)
        val resp = postJson("repos/$REPO/issues", payload)
        return resp.optInt("number", -1)
    }
}
