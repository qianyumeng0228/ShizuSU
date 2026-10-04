# ShizuSU Phase 3 · 补丁 3「模块管理便利功能」实现交付

> 依据：`master/KernelSUX-migration.md` **第四章（补丁 3 · 模块管理便利，即清单 3.1–3.6）** 定案逐项实现。
> 上游照搬来源：KernelSU-Next = `upstreams/next` @ dev `3b1e46b`。
> 基线（只读未改）：`I:\文档\sukisuultra` (SukiSU-Ultra @ 7fbbb1f1)。
> 续作基础：`work/phase1/`（多管理器）、`work/phase2/`（stealth）——两者均只动内核/uapi，
> **未触碰 ksud 与 manager App**；故本阶段 ksud/App 文件 = 基线原样 + 本补丁追加。
> N1 前缀定案：`s_000ch7W13vx/artifacts/n1-backup-prefix-evidence.md`。
>
> 标注口径：**照搬**=逐段抄写改路径/包名；**改造**=同逻辑改行为；**自研**=上游无实现、ShizuSU 新增；
> **待实测**=源码无法证实，须真机验证（不编造结果）。

---

## 0. 本阶段交付文件清单（相对 work/phase3/）

| 文件 | 类型 | 说明 |
|---|---|---|
| `userspace/ksud/src/defs.rs` | 改造(基线) | 新增 ALLOWLIST_FILE + 模块/allowlist 备份前缀常量 |
| `userspace/ksud/src/module.rs` | 改造(基线,末尾追加) | backup/restore/install-batch/create(image)/hosts 占位函数 |
| `userspace/ksud/src/cli.rs` | 改造(基线) | Module 枚举 + 分发新增 7 个子命令 |
| `manager/.../ui/util/ModuleBackupRestore.kt` | 新建(照搬) | App SAF 备份恢复 4 函数 + N1 建议名 |
| `manager/.../ui/screen/flash/FlashUtils.kt` | 改造(基线) | 批量安装失败收集不中断 |
| `manager/.../ui/screen/settings/hosts/HideHostsPlaceholder.kt` | 新建(自研占位) | hosts 隐藏设置项占位文案 |

> 说明：
> - **Cargo.toml 不改**：ksud 复用已内嵌 busybox 执行 tar（不新增 `tar` crate），依赖零变化。
> - **AndroidManifest.xml 不改**：SAF（ACTION_CREATE_DOCUMENT / ACTION_GET_CONTENT）经系统
>   DocumentsUI，**无需任何存储权限**；基线 Manifest 也无 READ/WRITE_EXTERNAL_STORAGE，已核实。

---

## 1. 逐文件改动点与源码依据

### 1.1 `userspace/ksud/src/defs.rs`（改造）
- 在基线 `KSU_TEMP_BACKUP_DIR_NAME`（基线 :52）之后追加（现 :54-63）：
  - `ALLOWLIST_FILE = concatcp!(WORKING_DIR, ".allowlist")` —— 基线 defs.rs 原本**无**
    allowlist 路径常量（App 侧硬编码 `/data/adb/ksu/.allowlist`，见基线 ToolsUtils.kt:61）。
  - `SHISU_MODULES_BACKUP_PREFIX = "shisu_modules_backup_"`
  - `SHISU_ALLOWLIST_BACKUP_PREFIX = "shisu_allowlist_backup_"`
- **[自研] 命名依据**：N1 定案 §五。boot 镜像备份前缀 `KSU_BACKUP_FILE_PREFIX="ksu_backup_"`
  **保持基线不改**——该槽位专属于 boot（消费点全在 boot_patch.rs，clean_backup() 用
  starts_with 扫 /data/adb/ksu/）；品牌化为 `shisu_backup_` 会触碰 boot_patch/utils.rs OTA
  恢复契约，属 boot 补丁范畴，不在本模块管理便利范围内（已在注释写明）。

### 1.2 `userspace/ksud/src/module.rs`（改造，末尾追加，基线 1274 行原样保留）
> 基线全家桶函数**已与 Next 逐行等价**（已核验）：
> `enable_module`(基线:900≈Next:862)、`disable_module`(:921≈Next:883)、
> `disable_all_modules`(:937≈Next:899)、`uninstall_all_modules`(:945≈Next:907)、
> `mark_all_modules`(:954≈Next:916)、`run_action`(:881≈Next:854)、`list_modules`(:1221)、
> `install_module`(:822≈Next:802)、`undo_uninstall_module`(:834≈Next:814 restore_module)。
> 故本文件**不重写**这些函数，只追加新能力。

追加于文件末尾（现 :1275 起）：

- `shisu_backup_timestamp()`（:1287）——**[自研]** `yyyyMMdd_HHmmss` 时间戳，对齐 Next
  BackupRestore.kt:58 `SimpleDateFormat`。用 `chrono::Local`（基线 sulog.rs:2/256 已用，feature 可用）。
- `shisu_run_tar(shell_cmd, what)`（:1299）——**[自研]** 走 `assets::BUSYBOX_PATH sh -c` 跑 tar。
  选 busybox 而非新增 `tar` crate 的理由见函数注释：与 App 侧同用 busybox tar → 两端 tar 字节兼容。
- `backup_modules(dest_dir)`（:1317）——**[自研]** `ksud module backup <dir>`：
  - 空 modules 目录则跳过（对齐 Next :56 早退）；无 .allowlist 则跳过 allowlist tar（对齐 Next :84）。
  - modules tar 命令**逐字对齐** Next BackupRestore.kt:61
    `tar -cpf tmp -C /data/adb/modules $(ls /data/adb/modules)`。
  - allowlist tar 命令**逐字对齐** Next :89 `tar -cpf tmp -C /data/adb/ksu .allowlist`。
  - 文件名 = `<dir>/shisu_modules_backup_<ts>.tar` / `shisu_allowlist_backup_<ts>.tar`（N1）。
  - 额外写 `.meta` sidecar（时间戳/ksud_version/布局说明）——**[自研]** 元数据。
- `restore_modules_from_backup(src_tar)`（现约 :1370）——**[自研]** `ksud module restore-from-backup <tar>`：
  - 文件名含 `allowlist` → 解回 `/data/adb/ksu`（对齐 Next :152）；
  - 否则 → `mkdir -p /data/adb/modules_update && tar -xpf ... -C modules_update`（对齐 Next :126，重启生效）。
- `install_modules_batch(zips)`（现约 :1410）——**[改造]** `ksud module install-batch <zip...>`：
  逐 zip 调既有 `install_module`，**失败不中断**，收集 (路径, 原因)，结束打印汇总；
  仅当全部失败才返回 Err。与 Next Flash.kt:74-76 / 基线 FlashUtils.kt:114 的「失败即 return」相反。
- `create_module_image(size)`（现约 :1440）——**[自研][待实测]** `ksud module create [--size N]`：
  预留桩。核验：基线 ksud **无**模块稀疏镜像（grep image_size 仅命中 lkm_image.rs 内核 LKM 打包；
  模块直接存 ext4 /data/adb/modules）。当前只接受并记录 --size、打印说明，**不创建任何镜像、
  不假定 6G 默认生效**；待真机确认 /data/adb 是否外挂镜像后再接 make_ext4/resize2fs。
- `hosts_hide(action)`（现约 :1460）——**[自研·占位]** `ksud module hosts`：只打印占位说明，
  不读/写任何 hosts 文件；完整逻辑 Phase 4 实现。

### 1.3 `userspace/ksud/src/cli.rs`（改造）
- `enum Module`（基线 :320-377）在 `Config` 之后追加 7 个子命令变体（现 :379-...）：
  `Backup{dest_dir}`、`RestoreFromBackup{src}`、`InstallBatch{zips}`、
  `DisableAll{--yes}`、`UninstallAll`、`Create{--size}`、`Hosts{action}`。
  - `RestoreFromBackup` 命名刻意避开既有 `UndoUninstall`（基线 :328，语义=删 remove 标记），
    消除「restore」歧义（清单 3.2① 要求）。
- 分发 `run()` 的 `Commands::Module` match（基线 :811-822）在 `Module::List` 后追加对应 arms（现 :888-907）：
  - `DisableAll{yes}`：**[自研] 二次确认**——`!yes` 时 `anyhow::bail!` 拒绝并提示
    `ksud module disable-all --yes`；防误触丢 root 环境。底层仍调既有 `module::disable_all_modules()`。
- clap derive 自动 kebab-case：`backup / restore-from-backup / install-batch / disable-all /
  uninstall-all / create / hosts`。

### 1.4 `manager/.../ui/util/ModuleBackupRestore.kt`（新建，照搬 Next）
- 包名 `com.sukisu.ultra.ui.util`（与 KsuCli.kt 同包，直接用 `ksuApp`/`Shell`）。
- 照搬 Next `BackupRestore.kt` 四个 IO 函数，逐函数行号：
  - `backupModulesToUri(uri)` ← Next :54-77（含空目录早退、finally 删临时文件）。
  - `backupAllowlistToUriTar(uri)` ← Next :83-105（命名加 `Tar` 后缀，避开基线
    ToolsUtils.kt 已有裸 cp 版 `backupAllowlistToUri(context,uri)`，不同包不同名，不覆盖）。
  - `restoreModulesFromUri(uri)` ← Next :111-131（解到 modules_update，重启生效）。
  - `restoreAllowlistFromUriTar(uri)` ← Next :137-157。
- tar 命令逐字保留（`BUSYBOX=/data/adb/ksu/bin/busybox`，Next :48）；root 执行 idiom 改用基线
  `Shell.cmd(...).exec().isSuccess`（与基线 ToolsUtils.kt:60 一致）。
- **N1 建议名函数**：`shisuModulesBackupSuggestedName()` = `shisu_modules_backup_<ts>.tar`
  （对齐 Next :299 suggested）、`shisuAllowlistBackupSuggestedName()` = `shisu_allowlist_backup_<ts>.tar`
  （对齐 Next :356），供 SAF `EXTRA_TITLE` 使用。

### 1.5 `manager/.../ui/screen/flash/FlashUtils.kt`（改造）
- 新增 import `com.sukisu.ultra.ksuApp`、`...ui.util.getFileName`。
- `flashModulesSequentially(uris,...)`（基线 :107-120）**改造**为失败收集不中断（现 :112-145）：
  逐个 `flashModule`，失败记录 (名称, err) 进 `failures` 列表、继续队列；全跑完后若有失败，
  返回 `FlashResult(1, summary, anySuccess)`——summary 列每个失败项的首行错误，
  `showReboot = anySuccess`（有成功就提示重启生效）。与 Next Flash.kt:72-78 失败即 return 相反。

### 1.6 `manager/.../ui/screen/settings/hosts/HideHostsPlaceholder.kt`（新建，自研占位）
- 一个自包含 Material3 `@Composable HideHostsPlaceholder()`，渲染禁用态说明卡片（标题 +
  「Phase 4 实现」文案）。用字符串字面量，**不新增 R.string、不接 supercall、不读写 hosts**。
- 注释写明接入点（嵌入设置页 ModuleScreen 工具菜单）与 Phase 4 合并计划。

---

## 2. 静态自查（对照清单第三章 7 项）

| # | 自查项 | 结论 | 证据 |
|---|---|---|---|
| ① | backup/restore 子命令与前缀定案 | ✅ 通过 | ksud `module backup`→`shisu_modules_backup_<ts>.tar`+`shisu_allowlist_backup_<ts>.tar`(defs.rs 常量 + module.rs:1317)；`restore-from-backup` 按文件名自动分流；App 侧 suggested name 同前缀(ModuleBackupRestore.kt)。boot 前缀保持基线 `ksu_backup_` 不品牌化、`stock_image.sha1` 不动 |
| ② | 批量安装失败收集不中断 | ✅ 通过 | ksud `install-batch`(module.rs) 循环收集 failures、全跑完汇总；App `flashModulesSequentially`(FlashUtils.kt:112-145) 失败进列表不 return，返回 summary。两处均不中断队列 |
| ③ | 全家桶函数与 Next 对应 + disable_all 二次确认 | ✅ 通过 | enable/disable/disable_all/uninstall_all/list/run_action 基线已与 Next 逐行等价(§1.2 行号对照)；本阶段把 DisableAll/UninstallAll 接入 cli.rs；`disable-all` 必须 `--yes` 否则 bail(cli.rs:892-904) |
| ④ | 镜像大小参数落点与待实测标注 | ✅ 通过 | `module create [--size N]` 接受容量参数；module.rs:create_module_image 明确标注「无 sparse modules.img、预留桩、不假定 6G、待真机」，未编造 make_ext4 调用 |
| ⑤ | hosts 占位不实现逻辑 | ✅ 通过 | ksud `module hosts` 只打印占位(module.rs:hosts_hide)；App `HideHostsPlaceholder` 只渲染文案卡片；二者均不读/写 /data/adb/ksu/hosts、不接 supercall |
| ⑥ | 兼容层未动 | ✅ 通过 | 未改 metamodule.rs / Magic Mount 挂载链 / module 格式；install_module_to_system 的 metamodule 检查(:716-763)原样保留；defs.rs 只追加常量不改既有路径；未移植 Next 风险检测子系统(见 §3 说明) |
| ⑦ | 自研处均 [自研] 标注 | ✅ 通过 | backup/restore CLI、install-batch 改造、disable_all 二次确认、create 预留桩、hosts 占位、App tar allowlist 函数、.meta sidecar、busybox-tar 选型均在代码注释标 [自研]/[改造]/[待实测] 并引用清单/N1/Next 行号 |

---

## 3. 刻意范围排除（如实记录，非遗漏）

- **Next 风险检测子系统（risk detection / getevent 音量键确认 / `module risk` CLI）未移植**：
  清单 3.1 把它列为「低优先可抄点」，但本任务 7 点实现范围未含；它依赖整套 RiskSeverity/
  contains_risk_in_module/RiskCmd 且与安装强耦合，移植会扩大兼容层触碰面。需要时后续单独补丁。
- **boot 备份前缀品牌化（`ksu_backup_`→`shisu_backup_`）本阶段不改**：属 boot_patch 范畴，
  触碰 OTA 恢复契约(utils.rs:263 starts_with)，不在模块管理便利范围（N1 已记录为推荐项）。
- **App 完整备份恢复 Compose 屏幕（navigation3 接线 + 4 个按钮 + string resources）未整建**：
  基线是 navigation3 + Material/Miuix 双主题，Next 是 composedestinations。整屏移植需新增
  R.string.* 并改 Routes/ModuleScreen；为不臆造不存在的资源 ID 导致不可编译，本阶段交付
  **可独立编译的 util 函数 + 建议名常量**，UI 接线点已在 ModuleBackupRestore.kt 注释写明
  （SAF launcher 代码对应 Next :209-259）。这是与 phase2「集成衔接点显式标注」一致的做法。

---

## 4. 编译 / 构建验证说明（如实）

- **本机无 Android/Rust-android 构建环境**，本阶段为代码交付 + 编译验证方式说明，不臆称「已编译通过」。
- **ksud 侧**（需 Rust aarch64-linux-android 工具链 + 设备内核树）：
  1. 三个 .rs 按相对路径覆盖进 SukiSU fork 的 `userspace/ksud/src/`；
  2. `cargo check --target aarch64-linux-android`（或随 GKI 矩阵常规构建）；
  3. 预期新增符号：`module::backup_modules / restore_modules_from_backup / install_modules_batch /
     create_module_image / hosts_hide`；CLI `ksud module --help` 可见 7 个新子命令；
  4. 无新 cargo 依赖（复用 busybox、chrono 已在 Cargo.toml:23）。
- **App 侧**（需 Android Studio/Gradle）：
  1. 三个 .kt 放入对应包路径；
  2. ModuleBackupRestore.kt 仅依赖已存在符号（ksuApp、Shell.cmd、SuFile、getFileName、FlashResult）；
  3. 把 4 个备份/恢复按钮 + SAF launcher 接到 ModuleScreen（参考 Next BackupRestore.kt:209-259），
     用 `shisuModulesBackupSuggestedName()`/`shisuAllowlistBackupSuggestedName()` 作 EXTRA_TITLE；
  4. HideHostsPlaceholder() 嵌入设置页即可编译。
- **待实测真机项**：① 镜像容量 create --size 是否生效（当前预留桩）；② ksud backup 产出的 tar
  与 App SAF 导出 tar 互相恢复是否一致；③ install-batch 汇总在多 zip 真机行为；④ hosts 占位与
  Phase 4 合并点。

---

## 5. 许可
- ksud(Rust) 与 manager(Kotlin) 均 GPL-3.0；照搬 Next 逻辑已标注仓库+文件+行号；
  ShizuSU 新增行为均标 [自研] 并引用清单第三章 / N1 定案。
