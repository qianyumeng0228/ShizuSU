package com.qym.shizusu.uploader

enum class SourceType { INSTALLED, ZIP_FILE }

enum class UploadKind { MODULE, APP }

sealed interface UploadState {
    data object Idle : UploadState
    data class Uploading(val step: String) : UploadState
    data class Success(val message: String) : UploadState
    data class Error(val message: String) : UploadState
}

data class FormState(
    val moduleId: String = "",
    val moduleName: String = "",
    val author: String = "",
    val versionName: String = "",
    val versionCode: String = "",
    val summaryZh: String = "",
    val zygisk: Boolean = false
)

data class AppForm(
    val appName: String = "",
    val packageName: String = "",
    val versionName: String = "",
    val versionCode: String = "",
    val summary: String = ""
)

data class PublishedModule(
    val moduleId: String,
    val moduleName: String,
    val author: String,
    val version: String,
    val summary: String
)

sealed interface ApprovalState {
    data object Idle : ApprovalState
    data class Loading(val step: String) : ApprovalState
    data class Success(val message: String) : ApprovalState
    data class Error(val message: String) : ApprovalState
}
