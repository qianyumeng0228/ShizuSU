package com.qym.shizusu.uploader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme) {
                AppRoot()
            }
        }
    }
}

class AppViewModelFactory(private val ctx: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = UploadViewModel(ctx) as T
}

class ApprovalViewModelFactory(private val ctx: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ApprovalViewModel(ctx) as T
}

private enum class Tab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    UPLOAD("上传", Icons.Filled.Upload),
    PUBLISHED("已发布", Icons.Filled.List),
    APPROVAL("审批", Icons.Filled.Gavel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val ctx = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val vm: UploadViewModel = viewModel(factory = AppViewModelFactory(ctx))
    val approvalVm: ApprovalViewModel = viewModel(factory = ApprovalViewModelFactory(ctx))
    var tab by remember { mutableStateOf(Tab.UPLOAD) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ShizuSU 模块上传") },
                actions = {
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Settings, contentDescription = "设置") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.UPLOAD -> UploadScreen(vm)
                Tab.PUBLISHED -> ManageScreen(vm)
                Tab.APPROVAL -> ApprovalListScreen(approvalVm)
            }
        }
    }

    if (showSettings) SettingsScreen(vm) { showSettings = false }
}

// ======================= 上传页 =======================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(vm: UploadViewModel) {
    val sourceType by vm.sourceType.collectAsState()
    val installed by vm.installedModules.collectAsState()
    val selectedId by vm.selectedModuleId.collectAsState()
    val selectedZip by vm.selectedZipName.collectAsState()
    val form by vm.form.collectAsState()
    val state by vm.uploadState.collectAsState()
    val hasToken by vm.hasToken.collectAsState()
    val kind by vm.kind.collectAsState()
    val appForm by vm.appForm.collectAsState()
    val apkName by vm.selectedApkName.collectAsState()

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { vm.selectZipFile(it) }
    }
    val apkPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { vm.pickApk(it) }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasToken) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("尚未配置 GitHub 令牌，请先点击右上角设置填写", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        item {
            Text("上传类型", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(selected = kind == UploadKind.MODULE, onClick = { vm.selectKind(UploadKind.MODULE) }, label = { Text("模块 (zip)") })
                FilterChip(selected = kind == UploadKind.APP, onClick = { vm.selectKind(UploadKind.APP) }, label = { Text("应用 (APK)") })
            }
        }

        if (kind == UploadKind.APP) {
            item {
                OutlinedButton(onClick = { apkPicker.launch(arrayOf("application/vnd.android.package-archive", "*/*")) }) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (apkName.isNotBlank()) "已选择：$apkName" else "选择本地 APK 文件")
                }
            }
            item { Text("应用信息（自动解析，可修改）", style = MaterialTheme.typography.titleMedium) }
            item { AppFormFields(vm, appForm) }
        } else {
            item {
                Text("模块来源", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = sourceType == SourceType.INSTALLED, onClick = { vm.selectSource(SourceType.INSTALLED) }, label = { Text("已安装模块（需 root）") })
                    FilterChip(selected = sourceType == SourceType.ZIP_FILE, onClick = { vm.selectSource(SourceType.ZIP_FILE) }, label = { Text("本地 zip 文件") })
                }
            }
            if (sourceType == SourceType.INSTALLED) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("已安装模块", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.refreshInstalled() }) { Text("刷新") }
                    }
                }
                items(installed) { (id, prop) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedId == id, onClick = { vm.selectInstalled(id) })
                        Column {
                            Text(id, fontSize = 14.sp)
                            Text(prop.lineSequence().firstOrNull() ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                item {
                    OutlinedButton(onClick = { filePicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) }) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (selectedZip.isNotBlank()) "已选择：$selectedZip" else "选择模块 zip 文件")
                    }
                }
            }
            item { Text("模块信息（自动填充，可修改）", style = MaterialTheme.typography.titleMedium) }
            item { ModuleFormFields(vm) }
        }

        item {
            when (val s = state) {
                is UploadState.Uploading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp); Text(s.step)
                }
                is UploadState.Success -> Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(s.message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                is UploadState.Error -> Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(s.message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                else -> {}
            }
        }

        item {
            Button(
                onClick = { if (kind == UploadKind.APP) vm.uploadApp() else vm.upload() },
                enabled = (state as? UploadState.Uploading) == null,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (kind == UploadKind.APP) "上传到应用仓库" else "上传到模块仓库", fontSize = 16.sp)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun ModuleFormFields(vm: UploadViewModel, idEditable: Boolean = true) {
    val form by vm.form.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = form.moduleId, onValueChange = { v -> vm.updateForm { it.copy(moduleId = v.trim()) } },
            label = { Text("模块 ID（目录名）") }, singleLine = true, enabled = idEditable, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = form.moduleName, onValueChange = { v -> vm.updateForm { it.copy(moduleName = v.trim()) } },
            label = { Text("模块名称（显示名）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = form.author, onValueChange = { v -> vm.updateForm { it.copy(author = v.trim()) } },
            label = { Text("作者") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = form.versionName, onValueChange = { v -> vm.updateForm { it.copy(versionName = v.trim()) } },
                label = { Text("版本名") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = form.versionCode, onValueChange = { v -> vm.updateForm { it.copy(versionCode = v.filter { c -> c.isDigit() }) } },
                label = { Text("版本号（数字）") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = form.summaryZh, onValueChange = { v -> vm.updateForm { it.copy(summaryZh = v) } },
            label = { Text("中文简介（详情页展示）") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.zygisk, onCheckedChange = { c -> vm.updateForm { it.copy(zygisk = c) } })
            Text("Zygisk 模块")
        }
    }
}

@Composable
fun AppFormFields(vm: UploadViewModel, f: AppForm) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = f.appName, onValueChange = { v -> vm.updateAppForm { it.copy(appName = v.trim()) } },
            label = { Text("应用名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = f.packageName, onValueChange = { v -> vm.updateAppForm { it.copy(packageName = v.trim()) } },
            label = { Text("包名（即 id）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = f.versionName, onValueChange = { v -> vm.updateAppForm { it.copy(versionName = v.trim()) } },
                label = { Text("版本名") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = f.versionCode, onValueChange = { v -> vm.updateAppForm { it.copy(versionCode = v.filter { c -> c.isDigit() }) } },
                label = { Text("版本号（数字）") }, singleLine = true, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = f.summary, onValueChange = { v -> vm.updateAppForm { it.copy(summary = v) } },
            label = { Text("简介（中文优先）") }, minLines = 3, modifier = Modifier.fillMaxWidth())
    }
}

// ======================= 已发布管理页 =======================

@Composable
fun ManageScreen(vm: UploadViewModel) {
    val published by vm.publishedModules.collectAsState()
    val hasToken by vm.hasToken.collectAsState()
    val editing by vm.editingPublished.collectAsState()

    LaunchedEffect(Unit) { vm.refreshPublished() }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!hasToken) {
            item { Text("配置令牌后即可读取已发布模块", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("已发布模块（${published.size}）", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.refreshPublished() }) { Text("刷新") }
            }
        }
        if (editing) {
            item {
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("编辑模块信息", style = MaterialTheme.typography.titleMedium)
                        ModuleFormFields(vm, idEditable = false)
                        Row {
                            Button(onClick = { vm.updatePublishedInfo() }) { Text("保存信息") }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { vm.cancelEdit() }) { Text("取消") }
                        }
                    }
                }
            }
        }
        items(published) { m ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text(m.moduleName, style = MaterialTheme.typography.titleSmall)
                    Text("${m.moduleId} · ${m.author} · ${m.version}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(m.summary, style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { vm.startEdit(m) }) { Text("编辑") }
                        TextButton(onClick = { vm.deletePublished(m.moduleId) }) { Text("删除") }
                    }
                }
            }
        }
    }
}

// ======================= 设置页 =======================

@Composable
fun SettingsScreen(vm: UploadViewModel, onDismiss: () -> Unit) {
    var token by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (vm.saveToken(token)) { msg = "令牌已保存"; onDismiss() } else msg = "令牌不能为空"
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = { vm.clearToken(); onDismiss() }) { Text("清除令牌") }
        },
        title = { Text("GitHub 令牌") },
        text = {
            Column {
                Text("填写具有 Contents:write + issues 权限的 fine-grained PAT（仓库：ShizuSU-Modules / ShizuSU-Apps）", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("PAT") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (msg.isNotBlank()) Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

// ======================= 审批页 =======================

@Composable
fun ApprovalListScreen(vm: ApprovalViewModel) {
    val submissions by vm.submissions.collectAsState()
    val selected by vm.selected.collectAsState()
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    if (selected != null) {
        ApprovalDetailScreen(vm)
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("待审批（${submissions.size}）", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.refresh() }) { Text("刷新") }
            }
        }
        if (state is ApprovalState.Error) item { Text((state as ApprovalState.Error).message, color = MaterialTheme.colorScheme.error) }
        if (state is ApprovalState.Loading) item { Text((state as ApprovalState.Loading).step) }
        items(submissions) { iss ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text(iss.title, style = MaterialTheme.typography.titleSmall)
                    Text("#${iss.number} · ${iss.createdAt.take(10)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { vm.openDetail(iss) }) { Text("查看详情") }
                }
            }
        }
    }
}

@Composable
fun ApprovalDetailScreen(vm: ApprovalViewModel) {
    val iss by vm.selected.collectAsState()
    val payload by vm.payload.collectAsState()
    val zipSize by vm.pendingZipSize.collectAsState()
    val prop by vm.pendingProp.collectAsState()
    val state by vm.state.collectAsState()
    var rejectReason by remember { mutableStateOf("") }
    var showReject by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("提交详情 #${iss?.number}", style = MaterialTheme.typography.titleMedium)
            Text(iss?.title ?: "", style = MaterialTheme.typography.bodyMedium)
        }
        payload?.let { p ->
            item {
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("模块：${p.moduleName}（${p.moduleId}）")
                        Text("作者：${p.author}  ·  版本：${p.versionName} (${p.versionCode})")
                        Text("Zygisk：${p.zygisk}  ·  分片：${p.shards.size}")
                        Text("简介：${p.summaryZh}")
                        Text("zip 大小：${zipSize / 1024} KB")
                    }
                }
            }
        }
        prop?.let { item { Text("module.prop id=${it.id} name=${it.name}", style = MaterialTheme.typography.bodySmall) } }
        when (state) {
            is ApprovalState.Loading -> item { Text((state as ApprovalState.Loading).step) }
            is ApprovalState.Success -> item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Text((state as ApprovalState.Success).message, Modifier.padding(12.dp)) } }
            is ApprovalState.Error -> item { Text((state as ApprovalState.Error).message, color = MaterialTheme.colorScheme.error) }
            else -> {}
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.approve() }, modifier = Modifier.weight(1f)) { Text("通过并上架") }
                OutlinedButton(onClick = { showReject = true }, modifier = Modifier.weight(1f)) { Text("拒绝") }
                TextButton(onClick = { vm.closeDetail() }) { Text("返回") }
            }
        }
    }

    if (showReject) {
        AlertDialog(
            onDismissRequest = { showReject = false },
            confirmButton = { TextButton(onClick = { vm.reject(rejectReason); showReject = false }) { Text("确认拒绝") } },
            dismissButton = { TextButton(onClick = { showReject = false }) { Text("取消") } },
            title = { Text("拒绝原因") },
            text = { OutlinedTextField(value = rejectReason, onValueChange = { rejectReason = it }, modifier = Modifier.fillMaxWidth()) }
        )
    }
}
