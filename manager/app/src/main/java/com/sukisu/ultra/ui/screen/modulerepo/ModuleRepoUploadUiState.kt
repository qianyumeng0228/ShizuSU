package com.sukisu.ultra.ui.screen.modulerepo

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class ModuleUploadSource { INSTALLED, LOCAL_ZIP }

sealed class ModuleUploadStatus {
    data object Idle : ModuleUploadStatus()
    data object Packaging : ModuleUploadStatus()
    data class UploadingGist(val done: Int, val total: Int) : ModuleUploadStatus()
    data object SubmittingIssue : ModuleUploadStatus()
    data class Success(val issueNumber: Int) : ModuleUploadStatus()
    data class Failed(val message: String) : ModuleUploadStatus()
}

@Immutable
data class InstalledModuleEntry(
    val id: String,
    val name: String,
)

@Immutable
data class ModuleRepoUploadUiState(
    val source: ModuleUploadSource = ModuleUploadSource.INSTALLED,
    val installedModules: List<InstalledModuleEntry> = emptyList(),
    val installedLoading: Boolean = false,
    val selectedInstalledId: String? = null,
    val localZipName: String? = null,
    // 表单
    val moduleId: String = "",
    val moduleName: String = "",
    val author: String = "",
    val versionName: String = "",
    val versionCode: String = "",
    val summaryZh: String = "",
    val zygisk: Boolean = false,
    // 令牌（内存持有，不持久化）
    val token: String = "",
    // 状态
    val status: ModuleUploadStatus = ModuleUploadStatus.Idle,
) {
    val isBusy: Boolean
        get() = status is ModuleUploadStatus.Packaging ||
                status is ModuleUploadStatus.UploadingGist ||
                status is ModuleUploadStatus.SubmittingIssue

    val canSubmit: Boolean
        get() = !isBusy &&
                moduleId.isNotBlank() &&
                moduleName.isNotBlank() &&
                token.isNotBlank() &&
                (source == ModuleUploadSource.LOCAL_ZIP || selectedInstalledId != null)
}

@Immutable
data class ModuleRepoUploadActions(
    val onBack: () -> Unit,
    val onSelectSource: (ModuleUploadSource) -> Unit,
    val onSelectInstalled: (String) -> Unit,
    val onPickLocalZip: () -> Unit,
    val onModuleIdChange: (String) -> Unit,
    val onModuleNameChange: (String) -> Unit,
    val onAuthorChange: (String) -> Unit,
    val onVersionNameChange: (String) -> Unit,
    val onVersionCodeChange: (String) -> Unit,
    val onSummaryZhChange: (String) -> Unit,
    val onZygiskChange: (Boolean) -> Unit,
    val onTokenChange: (String) -> Unit,
    val onSubmit: () -> Unit,
)
