# ShizuSUploader 工程重建记录

## 背景
`I:\文档\sukisuultra\module-uploader` 整目录曾被外部删除（非用户移动、未入 git，无法从 git 恢复）。
经用户确认，改为从设备 `bc757a76` 拉 base.apk → jadx 反编译 → 逐文件重建 Gradle 工程 + Kotlin 源码。

## 反编译产物位置
- APK：`I:\文档\sukisuultra\.recovery\uploader-base.apk`（14,284,821 字节）
- jadx：`I:\文档\sukisuultra\.recovery\jadx\`（jadx 1.5.1）
- 反编译输出：`I:\文档\sukisuultra\.recovery\jadx-out\sources\`（Java）、`...\resources\`（res/AndroidManifest）

## 重建后工程结构（app/src/main）
- `AndroidManifest.xml` — package `com.qym.shizusu.uploader`，INTERNET 权限，`.MainActivity`，Theme.ModuleUploader
- `res/` — strings/app_name="ShizuSU 模块上传"、themes、colors、mipmap-anydpi + 各密度 ic_launcher（青蓝云上传图标）
- `kotlin/com/qym/shizusu/uploader/`：
  - TokenStore.kt / ModuleProp.kt / SubmissionIssue.kt / SubmissionPayload.kt / States.kt
  - GitHubApi.kt（多仓库：默认 ShizuSU-Modules，构造参数可切 ShizuSU-Apps；listSubmissions/getGistFile/commentIssue/closeIssue/getRepoFile/pushFiles/deleteFiles）
  - ModulePackager.kt（root 打包已安装模块 + targz 解析）
  - ModuleJsonBuilder.kt（catalog/config/detail 三种 JSON）
  - ApprovalViewModel.kt（审批：下载 gist 分片→base64 拼接→校验 module.prop→pushFiles 四件套→comment+close）
  - UploadViewModel.kt（模块上传 + 应用上传 uploadApp + 已发布管理）
  - MainActivity.kt（三 tab：上传/已发布/审批 + 设置对话框；UploadScreen/ModuleFormFields/AppFormFields/ManageScreen/SettingsScreen/ApprovalListScreen/ApprovalDetailScreen）

## 打回的修复（onValueChange 简介为空根因）
所有 `OutlinedTextField.onValueChange` 此前写成 `{ vm.updateXForm { it.copy(field = it.field) } }`——
外层新输入 String 被内层 lambda 的 `it`（表单对象）遮蔽，用户输入从未写回状态。
重建时全部按修复范式写回：
`onValueChange = { v -> vm.updateXForm { it.copy(field = v...) } }`
覆盖：ModuleFormFields 6 字段（moduleId/moduleName/author/versionName/versionCode/summaryZh + zygisk 勾选）
与 AppFormFields 5 字段（appName/packageName/versionName/versionCode/summary）。

## 关键结构约定（与线上/审批链路一致）
- `modules.json` = 顶层 JSONArray，按 moduleId 去重后追加。
- `modules.config.json` = **对象结构** `{modules:[], outputDir, cacheDir, cache, cacheTtlSeconds:3600}`——只读对象、只改 `.modules` 子数组，**顶层四键不触碰**。
- 模块 zip 下载前缀：`https://github.com/qianyumeng0228/ShizuSU-Modules/raw/main/private/<id>.zip`
- 应用 apk 下载前缀：`https://github.com/qianyumeng0228/ShizuSU-Apps/raw/main/private/<id>.apk`
- `apps.json` = `{"apps":[{id,name,package,versionName,versionCode,summary,author,downloadUrl,updatedAt}]}`

## 构建修复（本次重建踩到的根因）
1. 路径含中文 `文档` → `gradle.properties` 加 `android.overridePathCheck=true`。
2. AGP 9.4.1 内置 Kotlin → 移除 `id("org.jetbrains.kotlin.android")` 插件（仅保留 compose 插件）。
3. `kotlinOptions { jvmTarget }` 在 AGP9 已移除 → 删除（compileOptions 已设 Java 21）。
4. 依赖坐标修正：`kotlinx-coroutines-android:1.9.4`（不存在）→ `1.9.0`；
   `libsu` 用 JitPack：settings 加 `https://jitpack.io`，坐标 `com.github.topjohnwu.libsu:core:5.2.2`。
5. OkHttp：补 `import okhttp3.RequestBody.Companion.toRequestBody`。
6. MainActivity 内 `applicationContext` 不可用于 Composable → 改 `LocalContext.current.applicationContext`。

## 构建结果
- `:app:assembleRelease` → **BUILD SUCCESSFUL**（1m11s，47 tasks）
  - 产物：`app\build\outputs\apk\release\app-release-unsigned.apk`（14,615,519 字节）
  - 说明：`keystore.properties` 与 keystore 文件已随目录丢失（未入 git），故 release 未签名；构建脚本在无 keystore 时自动退化为 unsigned。
- `:app:assembleDebug` → **BUILD SUCCESSFUL**
  - 产物：`app\build\outputs\apk\debug\app-debug.apk`（21,414,497 字节，debug 签名，可装机）

## 遗留
- release keystore 丢失：如需签名 release，需重新生成 keystore 并写 keystore.properties；
  或直接用 debug 包装机（与设备上已装的 debug 签名一致）。
- module-uploader 整目录仍未入 git（父仓库 sukisuultra 共享 .git，分支 feature/module-submission，但本目录从未被跟踪）。
  建议后续 `git add module-uploader` 纳入跟踪以防再次丢失。
