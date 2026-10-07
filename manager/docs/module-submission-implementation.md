# ShizuSU 模块商店「社区提交 → 管理员审批」实现说明

分支：`feature/module-submission`（基线 `main` tip `fabaa109`，原探查笔记中的 `a5086b83` 为签名配置提交，已在历史中）。

## 一、架构总览

GitHub 原生无服务器方案：
1. 用户在管理器内选来源（已安装模块 / 本地 zip），App 端把模块打成标准 zip；
2. zip 整体 Base64 编码后按 < 800KB 切片，一次性 `POST /gists` 写入私有 Gist（files 名 `<id>.b64` / `<id>.b64.p2`…）；
3. `POST /repos/qianyumeng0228/ShizuSU-Modules/issues` 创建 issue，title=`[提交] <模块名>`，labels=`["submission"]`，body 为 JSON（gistId + 模块元数据）；
4. 管理员在 Gist 里取 zip，审核通过后由 `module-repo-builder` 入库。

提交者令牌只需 `gist` scope，无需仓库写权限。令牌仅在 ViewModel 内存持有，不写 SharedPreferences。

## 二、新增/修改文件清单

### 新增
- `app/src/main/java/com/sukisu/ultra/ui/util/module/ModuleSubmitApi.kt` — GitHub API 封装（裸 OkHttp + org.json，自带长超时 client）：`verifyToken()` / `createGistWithShards()` / `createSubmissionIssue()`。
- `app/src/main/java/com/sukisu/ultra/ui/util/module/ModulePackager.kt` — 移植自 module-uploader：`listInstalledModules()` / `packInstalled()`（root `tar -czf /data/adb/modules/<id>` → commons-compress 解 tar.gz → 剥顶层目录 → EXCLUDE_NAMES/SUFFIXES 过滤 → 重打 zip）+ `ModulePropInfo.parse()`。
- `app/src/main/java/com/sukisu/ultra/ui/screen/modulerepo/ModuleRepoUploadUiState.kt` — `ModuleUploadSource` / `ModuleUploadStatus` / `ModuleRepoUploadUiState` / `ModuleRepoUploadActions`。
- `app/src/main/java/com/sukisu/ultra/ui/screen/modulerepo/ModuleRepoUploadViewModel.kt` — 共享 ViewModel：加载已装模块、打包、SAF 选 zip 拷入 cache 并读 module.prop 自动填表、提交状态机（Idle/Packaging/UploadingGist(done,total)/SubmittingIssue/Success(issueNo)/Failed(msg)）。
- `app/src/main/java/com/sukisu/ultra/ui/screen/modulerepo/ModuleRepoUploadScreen.kt` — 编排：`viewModel<ModuleRepoUploadViewModel>()` + `rememberLauncherForActivityResult(GetContent())` + `when(LocalUiMode)` 分发。
- `app/src/main/java/com/sukisu/ultra/ui/screen/modulerepo/ModuleRepoUploadMiuix.kt` — Miuix 端 UI（Scaffold + TopAppBar + LazyColumn，来源 Card、已装模块列表 Card、表单 TextField、Zygisk Switch、token TextField、状态文本、提交 Button）。
- `app/src/main/java/com/sukisu/ultra/ui/screen/modulerepo/ModuleRepoUploadMaterial.kt` — Material 端 UI（material3 Scaffold + TopAppBar + OutlinedTextField + OutlinedButton + Switch）。
- `docs/module-submission-implementation.md`（本文件）。

### 修改
- `ui/navigation3/Routes.kt` — 新增 `data object ModuleRepoUpload : Route`。
- `ui/MainActivity.kt` — 新增 `entry<Route.ModuleRepoUpload> { ModuleRepoUploadScreen() }` + import。
- `ui/screen/modulerepo/ModuleRepoUiState.kt` — `ModuleRepoActions` 加 `onOpenUpload: () -> Unit`。
- `ui/screen/modulerepo/ModuleRepoScreen.kt` — actions 里接 `onOpenUpload = { navigator.push(Route.ModuleRepoUpload) }`。
- `ui/screen/modulerepo/ModuleRepoMiuix.kt` — TopAppBar actions 在排序 IconButton 前加 `MiuixIcons.UploadCloud` IconButton；新增 import。
- `ui/screen/modulerepo/ModuleRepoMaterial.kt` — SearchAppBar actions 在排序 IconButton 前加 `Icons.Filled.Upload` IconButton；新增 import。
- `res/values/strings.xml` + `res/values-zh-rCN/strings.xml` — 新增 26 条 `module_repo_upload_*` 字符串。

## 三、双端实现说明

- **入口位置**：商店页 TopAppBar `actions{}` 区，排序按钮**左侧**新增上传 IconButton（Miuix 用 `MiuixIcons.UploadCloud`，Material 用 `Icons.Filled.Upload`），与现有排序按钮并列，遵循 `ModuleRepoMiuix.kt:164` / `ModuleRepoMaterial.kt:143` 的 actions 惯例。
- **双端变体**：`ModuleRepoUploadScreen.kt` 做 ViewModel 持有 + `when(LocalUiMode.current)` 分发到 Miuix/Material 两个纯 UI 文件，与 `ModuleRepoScreen.kt` 既有模式一致。共享 `ModuleRepoUploadViewModel`（无自定义 Factory，界面直接 `viewModel<>()`）。
- **来源切换**：`ModuleUploadSource.INSTALLED` / `LOCAL_ZIP`。
  - INSTALLED：进入页面即 `ModulePackager.listInstalledModules()`（root shell `ls /data/adb/modules` + `cat .../module.prop`）列出；点选某项即 `packInstalled()` 打 zip 并从 `/data/adb/modules/<id>/module.prop` 自动填表。
  - LOCAL_ZIP：`ActivityResultContracts.GetContent()` 选 `application/zip`，拷入 `cacheDir/pack/local_<ts>.zip`，用 `ZipFile` 读 zip 根 `module.prop` 自动填表。
- **表单**：ID / 名称 / 作者 / 版本号 / 版本码（纯数字过滤）/ 中文简介 / Zygisk Switch。
- **令牌**：明文 TextField，仅 ViewModel 内存持有（不写 SharedPreferences，不持久化）。
- **提交状态**：`ModuleUploadStatus` 密封类驱动 UI——Packaging 转圈、UploadingGist(done,total) 显示分片进度、SubmittingIssue 转圈、Success(issueNo) 绿色成功文案、Failed(msg) 红色错误文案。提交按钮 `enabled = state.canSubmit`（ID/名称/token 非空且已选来源）。

## 四、提交流程（ModuleSubmitApi）

1. `verifyToken()` — `GET /user`（预留，当前未在 UI 主动调用；提交失败时由 GitHub 401 反馈）。
2. `createGistWithShards(moduleId, zipBytes, onShardUploaded)`：
   - `Base64.encodeToString(zipBytes, NO_WRAP)`；
   - `b64.chunked(700*1024)` 切片；
   - files 映射：第一片 `<id>.b64`，其后 `<id>.b64.p2`、`.p3`…；
   - 一次性 `POST /gists`，`public=false`，返回 gist `id`。
3. `createSubmissionIssue(gistId, ...)`：
   - title=`[提交] <moduleName>`，labels=`["submission"]`，body 为 ```json ``` 包裹的 `{gistId,moduleId,moduleName,author,versionName,versionCode,summaryZh,zygisk}`；
   - `POST /repos/qianyumeng0228/ShizuSU-Modules/issues`，返回 issue number。

所有请求头：`Authorization: Bearer <token>` / `User-Agent: ShizuSU-ModuleSubmit` / `Accept: application/vnd.github+json`。Client 超时：connect 15s / read 90s / write 180s。

## 五、构建

```
cd manager
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleRelease --no-daemon
```

**本机注意**：工程路径含中文 `I:\文档\sukisuultra`，protoc 无法识别非 ASCII 路径，需先 `subst S: "I:\文档\sukisuultra"` 再从 `S:\manager` 构建；同时 `app/src/main/cpp/uapi` 是 git symlink（Windows 检出为 19B 文本文件），需 `Remove-Item uapi; mklink /J uapi ..\..\..\..\uapi` 重建为目录 junction。

结果：`BUILD SUCCESSFUL`，产物 `app/build/outputs/apk/release/ShizuSU_v1.0.6_37305-release.apk`（v2 签名，release signing 走本地 `keystore.properties`）。

## 六、后续待办（不在本分支范围）

- 在 `qianyumeng0228/ShizuSU-Modules` 仓库创建 `submission` label（当前只有 GitHub 默认集）。
- 管理员审批后从 Gist 取 zip、入库 `private/<id>.zip`，并按 `modules.config.json`（OBJECT）格式追加 `.modules` 条目——**不要**照 module-uploader 旧代码把 config 当 JSONArray 覆盖。
- 可选：把 Gist 设置为在 issue 合并后自动删除（GitHub 不支持，需管理员手动或加 workflow）。
