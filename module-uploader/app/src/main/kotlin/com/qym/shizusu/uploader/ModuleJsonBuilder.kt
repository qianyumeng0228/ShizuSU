package com.qym.shizusu.uploader

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

object ModuleJsonBuilder {

    private fun now(): String = Instant.now().toString()

    fun time(): String = now()

    fun catalogEntry(
        moduleId: String, moduleName: String, author: String, summaryZh: String,
        zygisk: Boolean, versionName: String, versionCode: Int, downloadUrl: String, time: String
    ): JSONObject = JSONObject()
        .put("moduleId", moduleId)
        .put("moduleName", moduleName)
        .put("authors", JSONArray().put(JSONObject().put("name", author).put("link", "")))
        .put("summary", summaryZh)
        .put("metamodule", false)
        .put("zygisk", zygisk)
        .put("stargazerCount", 0)
        .put("updatedAt", time)
        .put("createdAt", time)
        .put("latestRelease", JSONObject()
            .put("name", versionName).put("time", time)
            .put("versionCode", versionCode).put("downloadUrl", downloadUrl))

    fun detail(
        moduleId: String, readmeZh: String, zygisk: Boolean,
        versionName: String, downloadUrl: String, zipSize: Long, time: String
    ): JSONObject = JSONObject()
        .put("readme", readmeZh)
        .put("readmeHTML", "<p>" + readmeZh.replace("\n", "<br/>") + "</p>")
        .put("homepageUrl", "")
        .put("sourceUrl", "")
        .put("url", "")
        .put("latestRelease", JSONObject().put("name", versionName).put("version", versionName)
            .put("time", time).put("downloadUrl", downloadUrl))
        .put("releases", JSONArray().put(JSONObject()
            .put("name", versionName).put("tagName", versionName).put("publishedAt", time)
            .put("descriptionHTML", "")
            .put("releaseAssets", JSONArray().put(JSONObject()
                .put("name", "$moduleId.zip").put("downloadUrl", downloadUrl)
                .put("size", zipSize).put("downloadCount", 0)))))

    fun configEntry(
        moduleId: String, moduleName: String, author: String, summaryZh: String, readmeZh: String,
        zygisk: Boolean, versionName: String, versionCode: Int, downloadUrl: String, zipSize: Long, time: String
    ): JSONObject = JSONObject()
        .put("moduleId", moduleId)
        .put("moduleName", moduleName)
        .put("authors", JSONArray().put(JSONObject().put("name", author).put("link", "")))
        .put("summary", summaryZh)
        .put("summaryZh", summaryZh)
        .put("readmeZh", readmeZh)
        .put("zygisk", zygisk)
        .put("latestRelease", JSONObject().put("name", versionName).put("version", versionName)
            .put("time", time).put("downloadUrl", downloadUrl).put("size", zipSize))
        .put("versionCodeOverride", versionCode)
        .put("note", "由手机端 ShizuSU 模块上传工具新增")
}
