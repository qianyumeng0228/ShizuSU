package com.qym.shizusu.uploader

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GitHubApi(private val token: String, private val repo: String = REPO) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    companion object {
        const val REPO = "qianyumeng0228/ShizuSU-Modules"
        const val APPS_REPO = "qianyumeng0228/ShizuSU-Apps"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private fun request(method: String, url: String, body: String? = null): okhttp3.Response {
        val rb = body?.toRequestBody(JSON)
        val req = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("User-Agent", "ShizuSU-ModuleUploader")
            .header("Accept", "application/vnd.github+json")
            .apply { if (body != null) method(method, rb) else method(method, null) }
            .build()
        return client.newCall(req).execute()
    }

    private fun getJson(path: String): JSONObject {
        request("GET", "https://api.github.com/$path").use { resp ->
            if (!resp.isSuccessful) throw IOException("GET $path -> ${resp.code}")
            return JSONObject(resp.body?.string() ?: "{}")
        }
    }

    private fun postJson(path: String, body: JSONObject): JSONObject {
        request("POST", "https://api.github.com/$path", body.toString()).use { resp ->
            if (!resp.isSuccessful) throw IOException("POST $path -> ${resp.code}: ${resp.body?.string()}")
            return JSONObject(resp.body?.string() ?: "{}")
        }
    }

    private fun patch(path: String, body: JSONObject): JSONObject {
        request("PATCH", "https://api.github.com/$path", body.toString()).use { resp ->
            if (!resp.isSuccessful) throw IOException("PATCH $path -> ${resp.code}")
            return JSONObject(resp.body?.string() ?: "{}")
        }
    }

    private fun patchRaw(path: String, body: String): String {
        request("PATCH", "https://api.github.com/$path", body).use { resp ->
            if (!resp.isSuccessful) throw IOException("PATCH $path -> ${resp.code}")
            return resp.body?.string() ?: "{}"
        }
    }

    private fun getArray(path: String): JSONArray {
        request("GET", "https://api.github.com/$path").use { resp ->
            if (!resp.isSuccessful) throw IOException("GET $path -> ${resp.code}")
            return JSONArray(resp.body?.string() ?: "[]")
        }
    }

    // ===== 社区提交审批（issues + gist，固定 Modules 仓库）=====

    fun listSubmissions(): JSONArray =
        getArray("repos/$REPO/issues?state=open&labels=submission")

    fun getGistFile(gistId: String, filename: String): String {
        request("GET", "https://api.github.com/gists/$gistId").use { resp ->
            if (!resp.isSuccessful) throw IOException("读取 gist $gistId -> ${resp.code}")
            val gist = JSONObject(resp.body?.string() ?: "{}")
            val files = gist.optJSONObject("files")
                ?: throw IOException("gist $gistId 无 files 字段")
            val file = files.optJSONObject(filename)
                ?: throw IOException("gist 中找不到分片 $filename")
            return file.optString("content")
        }
    }

    fun commentIssue(number: Int, body: String) {
        postJson("repos/$REPO/issues/$number/comments", JSONObject().put("body", body))
    }

    fun closeIssue(number: Int) {
        patchRaw("repos/$REPO/issues/$number", JSONObject().put("state", "closed").toString())
    }

    fun verifyToken(): String {
        request("GET", "https://api.github.com/user").use { resp ->
            if (!resp.isSuccessful) throw IOException("令牌无效或权限不足（${resp.code}）")
            val j = JSONObject(resp.body?.string() ?: "{}")
            return j.optString("login", "?")
        }
    }

    // ===== 内容读写（实例 repo：Modules 或 Apps）=====

    fun getRepoFile(path: String): String? {
        request("GET", "https://api.github.com/repos/$repo/contents/$path").use { resp ->
            if (resp.code == 404) return null
            if (!resp.isSuccessful) throw IOException("读取 $path -> ${resp.code}")
            val j = JSONObject(resp.body?.string() ?: "{}")
            return String(android.util.Base64.decode(j.optString("content"), android.util.Base64.DEFAULT), Charsets.UTF_8)
        }
    }

    fun pushFiles(message: String, files: Map<String, ByteArray>) {
        val ref = getJson("repos/$repo/git/ref/heads/main")
        val headSha = ref.getJSONObject("object").getString("sha")
        val commit = getJson("repos/$repo/git/commits/$headSha")
        val baseTree = commit.getJSONObject("tree").getString("sha")

        val items = JSONArray()
        for ((path, data) in files) {
            val b64 = android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP)
            val blob = postJson("repos/$repo/git/blobs", JSONObject().put("content", b64).put("encoding", "base64"))
            items.put(JSONObject().put("path", path).put("mode", "100644").put("type", "blob").put("sha", blob.getString("sha")))
        }

        val newTree = postJson(
            "repos/$repo/git/trees",
            JSONObject().put("base_tree", baseTree).put("tree", items)
        )

        val newCommit = postJson(
            "repos/$repo/git/commits",
            JSONObject()
                .put("message", message)
                .put("tree", newTree.getString("sha"))
                .put("parents", JSONArray().put(headSha))
        )

        patch("repos/$repo/git/refs/heads/main", JSONObject().put("sha", newCommit.getString("sha")).put("force", false))
    }

    fun deleteFiles(message: String, targets: List<String>) {
        val ref = getJson("repos/$repo/git/ref/heads/main")
        val headSha = ref.getJSONObject("object").getString("sha")
        val commit = getJson("repos/$repo/git/commits/$headSha")
        val baseTree = commit.getJSONObject("tree").getString("sha")

        val tree = getJson("repos/$repo/git/trees/$baseTree?recursive=1")
        val entries = tree.getJSONArray("tree")

        val items = JSONArray()
        for (i in 0 until entries.length()) {
            val e = entries.getJSONObject(i)
            val path = e.optString("path", "")
            if (targets.contains(path)) continue
            items.put(
                JSONObject()
                    .put("path", path)
                    .put("mode", e.optString("mode", "100644"))
                    .put("type", e.optString("type", "blob"))
                    .put("sha", e.getString("sha"))
            )
        }

        val newTree = postJson("repos/$repo/git/trees", JSONObject().put("base_tree", baseTree).put("tree", items))
        val newCommit = postJson(
            "repos/$repo/git/commits",
            JSONObject()
                .put("message", message)
                .put("tree", newTree.getString("sha"))
                .put("parents", JSONArray().put(headSha))
        )
        patch("repos/$repo/git/refs/heads/main", JSONObject().put("sha", newCommit.getString("sha")).put("force", false))
    }
}
