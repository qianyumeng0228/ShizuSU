package com.sukisu.ultra.ui.screen.modulerepo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.module.ModulePackager
import com.sukisu.ultra.ui.util.module.ModulePropInfo
import com.sukisu.ultra.ui.util.module.ModuleSubmitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

/**
 * [ShizuSU] 模块提交 ViewModel。
 *
 * 流程：选来源 → 准备 zip（root 打包 或 SAF 选文件拷入 cache）→ 表单（可由 module.prop 自动填充）
 * → 提交（Gist base64 分片 → issue）。令牌仅在内存持有，不写入 SharedPreferences。
 */
class ModuleRepoUploadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ModuleRepoUploadUiState())
    val uiState: StateFlow<ModuleRepoUploadUiState> = _uiState.asStateFlow()

    /** 已准备好待上传的 zip（内存持有，不进 StateFlow）。 */
    private var preparedZip: File? = null

    init {
        loadInstalled()
    }

    fun loadInstalled() {
        viewModelScope.launch {
            _uiState.update { it.copy(installedLoading = true) }
            val list = withContext(Dispatchers.IO) {
                runCatching {
                    ModulePackager.listInstalledModules().map { (id, propText) ->
                        val prop = ModulePropInfo.parse(propText)
                        InstalledModuleEntry(id = id, name = prop.name.ifBlank { id })
                    }
                }.getOrDefault(emptyList())
            }
            _uiState.update { it.copy(installedModules = list, installedLoading = false) }
        }
    }

    fun onSelectSource(source: ModuleUploadSource) {
        _uiState.update { it.copy(source = source) }
    }

    /** 选中一个已安装模块：打包并自动填充表单。 */
    fun onSelectInstalled(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = ModuleUploadStatus.Packaging, selectedInstalledId = id) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val zip = ModulePackager.packInstalled(ksuApp, id)
                    preparedZip = zip
                    // 从 module.prop 自动填表
                    val propFile = File("/data/adb/modules/$id/module.prop")
                    val propText = runCatching { propFile.readText() }.getOrDefault("")
                    val prop = ModulePropInfo.parse(propText)
                    Triple(zip, prop, id)
                }
            }
            result.onSuccess { (_, prop, fallbackId) ->
                _uiState.update {
                    it.copy(
                        moduleId = prop.id.ifBlank { fallbackId },
                        moduleName = prop.name,
                        author = prop.author,
                        versionName = prop.version,
                        versionCode = prop.versionCode.takeIf { c -> c > 0 }?.toString().orEmpty(),
                        summaryZh = prop.description,
                        status = ModuleUploadStatus.Idle,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(status = ModuleUploadStatus.Failed(e.message ?: "打包失败"), selectedInstalledId = null)
                }
            }
        }
    }

    /** SAF 选完本地 zip 后调用：拷入 cache，读 module.prop 填表。 */
    fun onLocalZipPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = ModuleUploadStatus.Packaging) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val cacheDir = File(ksuApp.cacheDir, "pack").apply { mkdirs() }
                    val dst = File(cacheDir, "local_${System.currentTimeMillis()}.zip")
                    ksuApp.contentResolver.openInputStream(uri).use { input ->
                        requireNotNull(input) { "无法打开所选文件" }
                        dst.outputStream().use { out -> input.copyTo(out) }
                    }
                    preparedZip = dst
                    // 读 zip 根的 module.prop
                    var propText = ""
                    runCatching {
                        ZipFile(dst).use { zf ->
                            val entry = zf.getEntry("module.prop") ?: return@use
                            propText = zf.getInputStream(entry).bufferedReader().use { r -> r.readText() }
                        }
                    }
                    val prop = ModulePropInfo.parse(propText)
                    val displayName = dst.name
                    Triple(dst, prop, displayName)
                }
            }
            result.onSuccess { (_, prop, displayName) ->
                _uiState.update {
                    it.copy(
                        localZipName = displayName,
                        moduleId = prop.id,
                        moduleName = prop.name,
                        author = prop.author,
                        versionName = prop.version,
                        versionCode = prop.versionCode.takeIf { c -> c > 0 }?.toString().orEmpty(),
                        summaryZh = prop.description,
                        status = ModuleUploadStatus.Idle,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(status = ModuleUploadStatus.Failed(e.message ?: "读取 zip 失败")) }
            }
        }
    }

    fun onModuleIdChange(v: String) = _uiState.update { it.copy(moduleId = v) }
    fun onModuleNameChange(v: String) = _uiState.update { it.copy(moduleName = v) }
    fun onAuthorChange(v: String) = _uiState.update { it.copy(author = v) }
    fun onVersionNameChange(v: String) = _uiState.update { it.copy(versionName = v) }
    fun onVersionCodeChange(v: String) = _uiState.update { it.copy(versionCode = v.filter { c -> c.isDigit() }) }
    fun onSummaryZhChange(v: String) = _uiState.update { it.copy(summaryZh = v) }
    fun onZygiskChange(v: Boolean) = _uiState.update { it.copy(zygisk = v) }
    fun onTokenChange(v: String) = _uiState.update { it.copy(token = v) }

    fun submit() {
        val state = _uiState.value
        val zip = preparedZip
        if (zip == null || !zip.exists()) {
            _uiState.update { it.copy(status = ModuleUploadStatus.Failed("尚未选择模块 zip")) }
            return
        }
        if (state.token.isBlank()) {
            _uiState.update { it.copy(status = ModuleUploadStatus.Failed("请填写 GitHub 令牌")) }
            return
        }
        if (state.moduleId.isBlank() || state.moduleName.isBlank()) {
            _uiState.update { it.copy(status = ModuleUploadStatus.Failed("模块 ID / 名称不能为空")) }
            return
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val api = ModuleSubmitApi(state.token.trim())
                    // 1) 可选：校验令牌
                    _uiState.update { it.copy(status = ModuleUploadStatus.UploadingGist(0, 1)) }
                    // 2) 读 zip 字节 + base64 分片上传 gist
                    val bytes = zip.readBytes()
                    val gistId = api.createGistWithShards(state.moduleId, bytes) { done, total ->
                        _uiState.update { s -> s.copy(status = ModuleUploadStatus.UploadingGist(done, total)) }
                    }
                    // 3) 提 issue
                    _uiState.update { it.copy(status = ModuleUploadStatus.SubmittingIssue) }
                    val issueNo = api.createSubmissionIssue(
                        gistId = gistId,
                        moduleId = state.moduleId,
                        moduleName = state.moduleName,
                        author = state.author,
                        versionName = state.versionName,
                        versionCode = state.versionCode.toIntOrNull() ?: 0,
                        summaryZh = state.summaryZh,
                        zygisk = state.zygisk,
                    )
                    issueNo
                }
            }.onSuccess { issueNo ->
                _uiState.update { it.copy(status = ModuleUploadStatus.Success(issueNo)) }
            }.onFailure { e ->
                _uiState.update { it.copy(status = ModuleUploadStatus.Failed(e.message ?: "提交失败")) }
            }
        }
    }
}
