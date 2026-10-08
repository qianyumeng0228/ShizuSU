package com.qym.shizusu.uploader

data class SubmissionPayload(
    val gistId: String,
    val shards: List<String>,
    val moduleId: String,
    val moduleName: String,
    val author: String,
    val versionName: String,
    val versionCode: Int,
    val summaryZh: String,
    val zygisk: Boolean
)
