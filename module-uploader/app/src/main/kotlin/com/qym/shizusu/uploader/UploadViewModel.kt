package com.qym.shizusu.uploader

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant

class UploadViewModel(private val app: Context) : ViewModel() {

    val hasToken: StateFlow<Boolean> = MutableStateFlow(TokenStore.hasToken(app))
    val sourceType = MutableStateFlow(SourceType.INSTALLED)
    val installedModules = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val selectedModuleId = MutableStateFlow("")
    val selectedZipName = MutableStateFlow("")
    val selectedZipUri = MutableStateFlow<Uri?>(null)
    val form = MutableStateFlow(FormState())
    val uploadState = MutableStateFlow<UploadState>(UploadState.Idle)

    val kind = MutableStateFlow(UploadKind.MODULE)
    val appForm = MutableStateFlow(AppForm())
    val selectedApkUri = MutableStateFlow<Uri?>(null)
    val selectedApkName = MutableStateFlow("")

    val publishedModules = MutableStateFlow<List<PublishedModule>>(emptyList())
    val editingPublished = MutableStateFlow(false)
    val editingPublishedId = MutableStateFlow("")

    init { refreshInstalled() }

    fun refreshInstalled() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                installedModules.value = ModulePackager.listInstalledModules()
            } catch (_: Exception) {
                installedModules.value = emptyList()
            }
        }
    }

    fun selectSource(type: SourceType) { sourceType.value = type }

    fun selectInstalled(moduleId: String) {
        selectedModuleId.value = moduleId
        val pair = installedModules.value.firstOrNull { it.first == moduleId } ?: return
        applyProp(ModuleProp.parse(pair.second))
    }

    fun selectZipFile(uri: Uri) {
        selectedZipUri.value = uri
        selectedZipName.value = uri.lastPathSegment ?: "module.zip"
    }

    private fun applyProp(p: ModuleProp) {
        form.value = form.value.copy(
            moduleId = p.id.ifBlank { form.value.moduleId },
            moduleName = p.name.ifBlank { form.value.moduleName },
            author = p.author.ifBlank { form.value.author },
            versionName = p.version.ifBlank { form.value.versionName },
            versionCode = if (p.versionCode > 0) p.versionCode.toString() else form.value.versionCode,
            summaryZh = p.description.ifBlank { form.value.summaryZh }
        )
    }

    fun updateForm(transform: (FormState) -> FormState) {
        form.value = transform(form.value)
    }

    fun upload() {
        val token = TokenStore.loadToken(app)
        if (token.isNullOrBlank()) {
            uploadState.value = UploadState.Error("请先在设置中填写 GitHub 令牌")
            return
        }
        val f = form.value
        if (f.moduleId.isBlank() || f.moduleName.isBlank()) {
            uploadState.value = UploadState.Error("模块 ID 和名称不能为空")
            return
        }
        viewModelScope.launch {
            uploadState.value = UploadState.Uploading("准备 zip…")
            try {
                val zip = withContext(Dispatchers.IO) { prepareZip(sourceType.value) }
                val time = Instant.now().toString()
                val versionCode = f.versionCode.toIntOrNull() ?: 100
                val downloadUrl = "https://github.com/qianyumeng0228/ShizuSU-Modules/raw/main/private/${f.moduleId}.zip"
                withContext(Dispatchers.IO) {
                    val api = GitHubApi(token)
                    val existing = api.getRepoFile("modules.json") ?: "[]"
                    val arr = JSONArray(existing)
                    val keep = JSONArray()
                    for (i in 0 until arr.length()) {
                        if (arr.getJSONObject(i).optString("moduleId") != f.moduleId) keep.put(arr.getJSONObject(i))
                    }
                    keep.put(ModuleJsonBuilder.catalogEntry(f.moduleId, f.moduleName, f.author, f.summaryZh, f.zygisk, f.versionName, versionCode, downloadUrl, time))

                    val cfgObj = readConfigObj(api)
                    val cfgArr = cfgObj.optJSONArray("modules") ?: JSONArray()
                    val cfgKeep = JSONArray()
                    for (i in 0 until cfgArr.length()) {
                        if (cfgArr.getJSONObject(i).optString("moduleId") != f.moduleId) cfgKeep.put(cfgArr.getJSONObject(i))
                    }
                    cfgKeep.put(ModuleJsonBuilder.configEntry(f.moduleId, f.moduleName, f.author, f.summaryZh, f.summaryZh, f.zygisk, f.versionName, versionCode, downloadUrl, zip.length(), time))
                    cfgObj.put("modules", cfgKeep)

                    val detail = ModuleJsonBuilder.detail(f.moduleId, f.summaryZh, f.zygisk, f.versionName, downloadUrl, zip.length(), time)

                    api.pushFiles(
                        "chore: 上传模块 ${f.moduleId}（${f.moduleName}）",
                        mapOf(
                            "private/${f.moduleId}.zip" to zip.readBytes(),
                            "modules.json" to keep.toString().toByteArray(Charsets.UTF_8),
                            "modules.config.json" to cfgObj.toString().toByteArray(Charsets.UTF_8),
                            "module/${f.moduleId}.json" to detail.toString().toByteArray(Charsets.UTF_8)
                        )
                    )
                }
                uploadState.value = UploadState.Success("模块 ${f.moduleName} 已上传到模块商店")
            } catch (e: Exception) {
                uploadState.value = UploadState.Error("上传失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private suspend fun prepareZip(source: SourceType): File {
        return withContext(Dispatchers.IO) {
            when (source) {
                SourceType.INSTALLED -> ModulePackager.packInstalled(app, selectedModuleId.value)
                SourceType.ZIP_FILE -> {
                    val uri = selectedZipUri.value ?: throw IllegalStateException("未选择 zip 文件")
                    val tmp = File(app.cacheDir, "selected.zip")
                    app.contentResolver.openInputStream(uri)?.use { it.copyTo(tmp.outputStream()) }
                        ?: throw IllegalStateException("读取 zip 失败")
                    tmp
                }
            }
        }
    }

    private fun readConfigObj(api: GitHubApi): JSONObject {
        val text = api.getRepoFile("modules.config.json") ?: return JSONObject()
            .put("modules", JSONArray()).put("outputDir", "output").put("cacheDir", "cache").put("cacheTtlSeconds", 3600)
        return JSONObject(text)
    }

    // ===== 应用上传 =====

    fun selectKind(k: UploadKind) { kind.value = k }
    fun updateAppForm(transform: (AppForm) -> AppForm) { appForm.value = transform(appForm.value) }

    fun pickApk(uri: Uri) {
        selectedApkUri.value = uri
        selectedApkName.value = uri.lastPathSegment ?: "app.apk"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tmp = File(app.cacheDir, "picked.apk")
                app.contentResolver.openInputStream(uri)?.use { it.copyTo(tmp.outputStream()) }
                    ?: throw IllegalStateException("读取 APK 失败")
                val pm = app.packageManager
                val info = pm.getPackageArchiveInfo(tmp.absolutePath, PackageManager.GET_META_DATA)
                    ?: throw IllegalStateException("无法解析 APK")
                info.applicationInfo?.let { ai ->
                    ai.sourceDir = tmp.absolutePath
                    ai.publicSourceDir = tmp.absolutePath
                }
                val label = runCatching { info.applicationInfo?.loadLabel(pm)?.toString() }.getOrNull() ?: ""
                val vc = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt() else info.versionCode
                appForm.value = appForm.value.copy(
                    appName = label.ifBlank { appForm.value.appName },
                    packageName = info.packageName ?: appForm.value.packageName,
                    versionName = info.versionName ?: appForm.value.versionName,
                    versionCode = if (vc > 0) vc.toString() else appForm.value.versionCode
                )
            } catch (e: Exception) {
                uploadState.value = UploadState.Error("解析 APK 失败：${e.message}")
            }
        }
    }

    fun uploadApp() {
        val token = TokenStore.loadToken(app)
        if (token == null) {
            uploadState.value = UploadState.Error("请先在设置中填写 GitHub 令牌"); return
        }
        val f = appForm.value
        val uri = selectedApkUri.value
        if (uri == null) {
            uploadState.value = UploadState.Error("请先选择本地 APK 文件"); return
        }
        if (f.packageName.isBlank() || f.appName.isBlank()) {
            uploadState.value = UploadState.Error("应用名和包名不能为空"); return
        }
        val id = f.packageName
        viewModelScope.launch {
            uploadState.value = UploadState.Uploading("准备 APK…")
            try {
                val apkFile = withContext(Dispatchers.IO) {
                    val tmp = File(app.cacheDir, "app_$id.apk")
                    app.contentResolver.openInputStream(uri)?.use { it.copyTo(tmp.outputStream()) }
                        ?: throw IllegalStateException("读取 APK 失败")
                    tmp
                }
                val versionCode = f.versionCode.toIntOrNull() ?: 0
                val time = java.time.LocalDate.now().toString()
                val downloadUrl = "https://github.com/${GitHubApi.APPS_REPO}/raw/main/private/$id.apk"
                withContext(Dispatchers.IO) {
                    val api = GitHubApi(token, GitHubApi.APPS_REPO)
                    val existing = api.getRepoFile("apps.json") ?: """{"apps":[]}"""
                    val root = JSONObject(existing)
                    val arr = root.optJSONArray("apps") ?: JSONArray()
                    val keep = JSONArray()
                    for (i in 0 until arr.length()) {
                        if (arr.getJSONObject(i).optString("id") != id) keep.put(arr.getJSONObject(i))
                    }
                    keep.put(
                        JSONObject()
                            .put("id", id).put("name", f.appName).put("package", f.packageName)
                            .put("versionName", f.versionName.ifBlank { "1.0.0" }).put("versionCode", versionCode)
                            .put("summary", f.summary).put("author", "qianyumeng0228")
                            .put("downloadUrl", downloadUrl).put("updatedAt", time)
                    )
                    root.put("apps", keep)
                    api.pushFiles(
                        "chore: 新增/更新应用 $id（${f.appName}）",
                        mapOf(
                            "private/$id.apk" to apkFile.readBytes(),
                            "apps.json" to root.toString().toByteArray(Charsets.UTF_8)
                        )
                    )
                }
                uploadState.value = UploadState.Success("应用 ${f.appName} 已上传到应用仓库")
            } catch (e: Exception) {
                uploadState.value = UploadState.Error(
                    "上传失败：${e.message ?: e.javaClass.simpleName}\n（若为 404/403，请确认令牌已授权 ShizuSU-Apps 的 Contents 写权限）"
                )
            }
        }
    }

    // ===== 已发布模块管理 =====

    fun refreshPublished() {
        val token = TokenStore.loadToken(app) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = GitHubApi(token)
                val text = api.getRepoFile("modules.json") ?: "[]"
                val arr = JSONArray(text)
                val list = ArrayList<PublishedModule>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PublishedModule(
                            moduleId = o.optString("moduleId"),
                            moduleName = o.optString("moduleName"),
                            author = o.optJSONArray("authors")?.optJSONObject(0)?.optString("name") ?: "",
                            version = o.optJSONObject("latestRelease")?.optString("name") ?: "",
                            summary = o.optString("summary")
                        )
                    )
                }
                publishedModules.value = list
            } catch (_: Exception) {
            }
        }
    }

    fun startEdit(module: PublishedModule) {
        editingPublished.value = true
        editingPublishedId.value = module.moduleId
        form.value = FormState(
            moduleId = module.moduleId,
            moduleName = module.moduleName,
            author = module.author,
            versionName = module.version,
            versionCode = "",
            summaryZh = module.summary
        )
    }

    fun cancelEdit() {
        editingPublished.value = false
        editingPublishedId.value = ""
        form.value = FormState()
        uploadState.value = UploadState.Idle
    }

    fun updatePublishedInfo() {
        val token = TokenStore.loadToken(app)
        if (token.isNullOrBlank()) { uploadState.value = UploadState.Error("请先在设置中填写 GitHub 令牌"); return }
        val f = form.value
        if (f.moduleId.isBlank() || f.moduleName.isBlank()) { uploadState.value = UploadState.Error("模块 ID 和名称不能为空"); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = GitHubApi(token)
                val time = Instant.now().toString()
                val versionCode = f.versionCode.toIntOrNull() ?: 1
                val downloadUrl = "https://github.com/qianyumeng0228/ShizuSU-Modules/raw/main/private/${f.moduleId}.zip"
                val existing = api.getRepoFile("modules.json") ?: "[]"
                val arr = JSONArray(existing)
                val keep = JSONArray()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    if (o.optString("moduleId") == f.moduleId) {
                        keep.put(ModuleJsonBuilder.catalogEntry(f.moduleId, f.moduleName, f.author, f.summaryZh, false, f.versionName, versionCode, downloadUrl, time))
                    } else keep.put(o)
                }
                api.pushFiles("chore: 更新模块信息 ${f.moduleId}", mapOf("modules.json" to keep.toString().toByteArray(Charsets.UTF_8)))
                uploadState.value = UploadState.Success("已更新 ${f.moduleName}")
            } catch (e: Exception) {
                uploadState.value = UploadState.Error("更新失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun updatePublishedZip() { upload() }

    fun deletePublished(moduleId: String) {
        val token = TokenStore.loadToken(app)
        if (token.isNullOrBlank()) { uploadState.value = UploadState.Error("请先在设置中填写 GitHub 令牌"); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = GitHubApi(token)
                api.deleteFiles("chore: 删除模块 $moduleId", listOf("private/$moduleId.zip", "module/$moduleId.json"))
                val existing = api.getRepoFile("modules.json") ?: "[]"
                val arr = JSONArray(existing)
                val keep = JSONArray()
                for (i in 0 until arr.length()) {
                    if (arr.getJSONObject(i).optString("moduleId") != moduleId) keep.put(arr.getJSONObject(i))
                }
                val cfgText = api.getRepoFile("modules.config.json")
                if (cfgText != null) {
                    val cfg = JSONObject(cfgText)
                    val cArr = cfg.optJSONArray("modules") ?: JSONArray()
                    val cKeep = JSONArray()
                    for (i in 0 until cArr.length()) {
                        if (cArr.getJSONObject(i).optString("moduleId") != moduleId) cKeep.put(cArr.getJSONObject(i))
                    }
                    cfg.put("modules", cKeep)
                    api.pushFiles("chore: 删除模块 $moduleId（目录）", mapOf(
                        "modules.json" to keep.toString().toByteArray(Charsets.UTF_8),
                        "modules.config.json" to cfg.toString().toByteArray(Charsets.UTF_8)
                    ))
                } else {
                    api.pushFiles("chore: 删除模块 $moduleId（目录）", mapOf("modules.json" to keep.toString().toByteArray(Charsets.UTF_8)))
                }
                uploadState.value = UploadState.Success("已删除 $moduleId")
                refreshPublished()
            } catch (e: Exception) {
                uploadState.value = UploadState.Error("删除失败：${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    fun saveToken(token: String): Boolean {
        if (token.isBlank()) return false
        TokenStore.saveToken(app, token)
        (hasToken as MutableStateFlow).value = true
        return true
    }

    fun clearToken() {
        app.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().remove("github_token_enc").apply()
        (hasToken as MutableStateFlow).value = false
    }
}
