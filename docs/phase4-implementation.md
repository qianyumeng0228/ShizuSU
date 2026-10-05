# ShizuSU Phase 4 · 补丁 4「Root 隐藏增强」实现交付

> 依据：`master/KernelSUX-migration.md` **第四章（补丁 4 · Root 隐藏增强：4.1 susfsd 通道 / 4.2 KPROBES 钩子隐藏 / 4.3 App Profile hosts）** + 第六章命令号分配表 + 第七章 Kconfig 清单。
> 上游照搬来源：KernelSU-Next = `upstreams/next` @ dev `3b1e46b`（notes/next.md 已核验行号）。
> 基线（只读未改）：`I:\文档\sukisuultra` (SukiSU-Ultra @ 7fbbb1f1)。
> 续作基础：`work/phase1/`（多管理器，uapi 已抬到 5）、`work/phase2/`（stealth，dispatch.c 已含 stealth/dynamic_manager handler）、`work/phase3/`（模块便利，ksud/App hosts 占位）。
>
> 标注口径：**照搬**=逐段抄写改路径/包名；**改造**=同逻辑改行为；**自研**=上游无实现、ShizuSU 新增；**待实测**=源码无法证实，须编译/真机验证（不编造结果）。

---

## 0. 本阶段交付文件清单（相对 work/phase4/）

| 文件 | 类型 | 说明 |
|---|---|---|
| `uapi/supercall.h` | 改造(phase2 累积) | +2 结构体 +98/99 命令号；UAPI 6→7（含 phase2 的 25/26） |
| `kernel/Kconfig` | 改造(phase2 累积) | +CONFIG_KSU_KPROBES_HIDE(bool, default n) |
| `kernel/supercall/dispatch.c` | 改造(phase2 累积) | +do_get_hook_mode/do_get_version_tag +2 表项 |
| `userspace/ksud/src/susfsd.rs` | 新建(照搬 façade) | Next susfsd.rs 查询形状，复用基线 susfs::abi 通道 |
| `userspace/ksud/src/main.rs` | 改造(基线) | +`mod susfsd;`（aarch64 门控） |
| `userspace/ksud/src/cli.rs` | 改造(phase3 累积) | Susfs 4 查询臂改走 susfsd；Hosts 变体加 trailing hosts |
| `userspace/ksud/src/defs.rs` | 改造(phase3 累积) | +HOSTS_FILE 常量 |
| `userspace/ksud/src/module.rs` | 改造(phase3 累积) | hosts_hide() 占位→真实 hosts 文件读写 |
| `manager/.../hosts/HostsHidePanel.kt` | 新建(自研) | 完成 Phase3 占位：hosts 隐藏 root shell 面板 |

> 说明：
> - **Cargo.toml 不改**：susfsd façade 复用基线 `susfs` 模块既有依赖（libc/anyhow），零新 crate。
> - **supercall.c / ksud_integration.c / extras.c / syscall_hook_manager.c 不改**：核验（notes/next.md §8 + 本阶段实读）确认基线 kprobe 基础设施与 Next 同构，无 diff 可搬（详见 §3）。

---

## 1. 逐文件改动点与源码依据

### 1.1 `uapi/supercall.h`（改造，**以 phase2 累积版为底**合并 98/99）
> **[修复记录]** 初版误以 phase1 版 uapi 为底，丢失 phase2 的 `KSU_IOCTL_STEALTH_GET/SET(25/26)` 与 `struct ksu_stealth_cmd`（dispatch.c 已引用 25/26 会致 undefined macro 编译失败）。已改正：以 `phase2/uapi/supercall.h`（版本 6，含 25/26 STEALTH、106/107 DYNAMIC_MANAGER）为底，合并 98/99。
- **UAPI 版本 6→7**（:15）：追加注释 `// 7: [ShizuSU 补丁4] add GET_HOOK_MODE(98)/GET_VERSION_TAG(99)`。
  - 依据清单第六章：增命令必须抬版本（不学 7kimisu 故意不抬）。phase1 为 106/107 抬到 5、phase2 为 25/26 抬到 6；本阶段再加 98/99 故抬到 **7**。kernel 上报值与 ksud bindings 同源生成（`ksu_uapi::KERNEL_SU_UAPI_VERSION`），自动同步，**无需手改 ksucalls.rs**（基线 ksucalls.rs:195-204 为精确匹配比较，同源同版本号）。
  - **本头现含全部四补丁命令**（逐宏已核验）：25/26 STEALTH_GET/SET（:260-261，phase2）、98/99 GET_HOOK_MODE/GET_VERSION_TAG（:264-265，本阶段）、101 HOOK_TYPE legacy（:269）、106/107 DYNAMIC_MANAGER_GET/SET（:277-279，phase1）；结构体 `ksu_stealth_cmd`(:210)/`ksu_dynamic_manager_set_cmd`(:203)/`ksu_get_hook_mode_cmd`(:191)/`ksu_get_version_tag_cmd`(:197) 齐全。
- **+2 结构体**（紧随 `ksu_hook_type_cmd`）：
  - `struct ksu_get_hook_mode_cmd { char mode[16]; }` —— **照搬** Next uapi/supercall.h:139-141。
  - `struct ksu_get_version_tag_cmd { char tag[32]; }` —— **照搬** Next :143-145。
- **+2 命令号**（紧随 `KSU_IOCTL_DISABLE_ESCAPE_TO_ROOT=21`，对齐 Next 位置）：
  - `KSU_IOCTL_GET_HOOK_MODE = _IOC(_IOC_READ,'K',98,0)` —— **照搬** Next :198。
  - `KSU_IOCTL_GET_VERSION_TAG = _IOC(_IOC_READ,'K',99,0)` —— **照搬** Next :199。
- **不动**：`KSU_IOCTL_HOOK_TYPE='K',101`（基线 uapi:232，char[32]）保留 legacy 别名；注释写明 98(char[16]) 与 101(char[32]) ABI 不同、勿双写 handler（清单 4.2 定案 M2）。

### 1.2 `kernel/Kconfig`（改造，phase2 累积版上追加）
- 在 `endmenu` 前追加 **[自研]** `config KSU_KPROBES_HIDE`（bool, `depends on KSU`, `default n`）。
- help 文本写明：暴露 kprobes 后端与 GET_HOOK_MODE(98) 上报面给 App；**RISK**：与 CONFIG_KPM 共用 ftrace/kprobe/tracepoint 槽位，二者同开**未验证(待实测)**，可能加载失败/不稳；默认关，KPM 共存矩阵验证通过前不得开；关时行为与上游逐字一致。
- 依据清单 4.2② + 第七章表（CONFIG_KSU_KPROBES_HIDE default n）。**Next Kconfig 无此选项**（notes/next.md §8.5：Next 仅 KSU/DEBUG/DISABLE_MANAGER/DISABLE_POLICY/X86_PATCH），故为 ShizuSU 自研护栏开关。
- **共存**：本项 `depends on KSU`，与 `CONFIG_KPM`（:28 `depends on KSU && 64BIT`）无 Kconfig 依赖冲突、可同时 =y（编译层）；运行期抢钩子位 = 待实测（见 §4 T4）。

### 1.3 `kernel/supercall/dispatch.c`（改造，phase2 累积版上追加）
- **+2 handler**（紧随 `#endif // CONFIG_KSU_STEALTH`，现 :913/:939）：
  - `do_get_hook_mode()` —— **照搬** Next dispatch.c:699-715 逐字：
    ```c
    #ifdef CONFIG_HAVE_SYSCALL_TRACEPOINTS
        strscpy(cmd.mode, "Tracepoint", sizeof(cmd.mode));
    #else
        strscpy(cmd.mode, "Kprobes", sizeof(cmd.mode));
    #endif
    ```
    + copy_to_user 错误处理。与基线 `syscall_hook_manager.c` 后端选择同构（tracepoint 走 CONFIG_HAVE_SYSCALL_TRACEPOINTS :94-151）。
  - `do_get_version_tag()` —— **照搬** Next :717-729；**改造**宏：Next 用 `KERNEL_SU_VERSION_TAG`，基线无此宏（grep 0 命中），改用基线现有 `KSU_VERSION_FULL`（kernel/Kbuild:130 注入、ksu.h:21 兜底；do_get_full_version 已在用），strscpy 自动截断到 tag[32]。
- **+2 表项**（sentinel 前，现 :1180-1192）：
  ```c
  { .cmd = KSU_IOCTL_GET_HOOK_MODE, .name="GET_HOOK_MODE", .handler=do_get_hook_mode, .perm_check=manager_or_root },
  { .cmd = KSU_IOCTL_GET_VERSION_TAG, .name="GET_VERSION_TAG", .handler=do_get_version_tag, .perm_check=manager_or_root },
  ```
  **照搬** Next dispatch.c:936-947（perm=manager_or_root）。
- **不动**：do_get_hook_type(101)（基线 :854，硬编码 "Tracepoint Syscall Redirect" char[32]）原样保留——清单 M2 明令「101 保留 legacy 别名、勿双写 handler」。

### 1.4 `userspace/ksud/src/susfsd.rs`（新建，照搬 façade）
- **照搬** Next susfsd.rs 的公共 API 形状与错误语义：`show_version()`/`show_variant()`/`show_features(check_only)`/`check_unsupported(err,cmd)`。
- **关键复用（最小 diff，不重复造通道）**：核验发现基线 ksud **本就有**该 reboot 通道，只是组织在 `susfs/` 树：
  - 常量逐值一致：`susfs/abi/consts.rs` KSU_INSTALL_MAGIC1=0xDEAD_BEEF(:8)、SUSFS_MAGIC=0xFAFA_FAFA(:11)、CMD_SUSFS_SHOW_VERSION=0x0005_55E1(:38)/FEATURES=:39/VARIANT=:40、ERR=126(:15) ↔ Next susfsd.rs:6-17。
  - reboot 胶水：`susfs/abi/syscall.rs::send()`(:33 `syscall(SYS_reboot, magic1, magic2, cmd, payload)`) ↔ Next :44/65/86。
  - repr(C) 结构体：`susfs/abi/types.rs` SusfsVersion/SusfsFeatures/SusfsVariant ↔ Next :20-35（同布局）。
- 故本文件**不重复** syscall/结构体，而是 façade：调 `crate::susfs::abi::{send, SusfsVersion, SusfsVariant, SusfsFeatures}` + `consts::{...}` + `util::cstr_buf_to_string`，复刻 Next 的 print/err==126 降级语义。
- **[待实测]**：内核消费端（0xFAFAFAFA/0x555e1-3）不在本树（notes/next.md §9），由外部 susfs4ksu 补丁提供；未打补丁时查询 err=126，报 Next 原文 "SUSFS operation not supported, please enable it in kernel"。**本阶段内核侧不向 supercall.c reboot_handler_pre 加 SUSFS 消费**——那属外部补丁，不臆造。

### 1.5 `userspace/ksud/src/main.rs`（改造，基线追加 2 行）
- `mod susfs;`(:57) 后追加 `#[cfg(target_arch="aarch64")] mod susfsd;`。与 susfs 同 aarch64 门控（susfsd 依赖 susfs::abi）。

### 1.6 `userspace/ksud/src/cli.rs`（改造，phase3 累积版上追加）
- **Susfs 4 查询臂改走 susfsd façade**（:1246-1253）：
  - `Status => susfsd::show_features(true)` / `Version => susfsd::show_version()` / `Variant => susfsd::show_variant()` / `Features => susfsd::show_features(false)`。
  - **照搬** Next cli.rs:823-827 分发映射（Support→show_features(true) 等）。
  - **[改造]** 输出文案：`susfs status` 由基线的 `true/false`(bool) 改为 Next 形状的 `Supported/Unsupported`。通道其余 set_* 臂不动。
  - 该臂已 `#[cfg(target_arch="aarch64")]`(:1243)，与 mod susfsd 门控一致，非 aarch64 无 unresolved reference。
- **Hosts 变体加 trailing 参数**（:436-445）：`Hosts { action, #[arg(trailing_values=true)] hosts: Vec<String> }`；分发 `Module::Hosts{action,hosts} => module::hosts_hide(&action,&hosts)`（:910）。

### 1.7 `userspace/ksud/src/defs.rs`（改造，phase3 累积版上追加）
- +`HOSTS_FILE = concatcp!(WORKING_DIR,"hosts")`（/data/adb/ksu/hosts）。**[自研]**（Next 无 hosts，notes/next.md §6）。

### 1.8 `userspace/ksud/src/module.rs`（改造，phase3 累积版上替换占位）
- `hosts_hide(action, hosts)` 由 Phase3 占位（只打印 no-op）**补完**为真实 hosts 文件管理器：
  - `status`(默认)：报文件存在与否 + 非注释条目数；
  - `list`/`show`：打印 /data/adb/ksu/hosts 全文；
  - `add <host> [ip]`：去重写 `ip host`（默认 0.0.0.0），已存在同 host 则更新 IP；
  - `remove <host>`：按最后一个 token==host 过滤删除。
- **[自研]**：不内置任何「检测域名清单」（条目由用户/模块决定，禁止臆造屏蔽常量）；只读写文件，是否被模块挂载到 /system/etc/hosts 生效 = **[待实测]**（真机，取决于 Magic Mount 模块）。

### 1.9 `manager/.../hosts/HostsHidePanel.kt`（新建，自研，完成 Phase3 占位）
- Phase3 `HideHostsPlaceholder.kt` 占位 → 本文件为功能面板：root shell 调 `ksud module hosts list/add/remove`，OutlinedTextField 输入域名 + 添加(→0.0.0.0)/删除按钮 + 列表展示。
- **复用基线 idiom**：`com.topjohnwu.superuser.Shell.cmd(...).exec()`（与 ui/util/ModuleBackupRestore.kt:52 同源）；`Dispatchers.IO` 后台执行。
- **[自研][待实测]**：自包含、不新增 R.string、不接 supercall；接入点（嵌入设置页 hosts 区 / App Profile 详情页）注释写明。是否对检测 App 生效取决于模块挂载。

---

## 2. 静态自查（对照清单第四章 7 项）

| # | 自查项 | 结论 | 证据 |
|---|---|---|---|
| ① | susfsd 通道常量/命令/reboot 逻辑与 Next 一致 | ✅ 通过 | susfsd.rs façade 复用基线 `susfs/abi`：魔数 0xDEADBEEF+0xFAFAFAFA（consts.rs:8/11）、cmd 0x555e1/2/3（:38-40）、err=126（:15）逐值=Next susfsd.rs:6-17；reboot 形 syscall.rs:33=Next:44/65/86；三函数+check_unsupported 复刻 Next:37-114。未重复造通道（最小 diff） |
| ② | KPROBES_HIDE 默认 n + 操作开关落点与 Next 对应 + 与 KPM 无 Kconfig 冲突 | ✅ 通过 | Kconfig `CONFIG_KSU_KPROBES_HIDE default n`（depends on KSU，不与 KPM depends on KSU&&64BIT 冲突）；98=GET_HOOK_MODE 照搬 Next dispatch.c:699-715 + 表项:936-947；101=HOOK_TYPE legacy 保留不动。两开关可同时存在（无 Kconfig/符号冲突）；运行期共存=待实测 |
| ③ | hosts 隐藏逻辑完成（App Profile 联动）+ Phase3 占位补完 | ✅ 通过 | ksud `module hosts status/list/add/remove` 真实读写 /data/adb/ksu/hosts（module.rs hosts_hide）；App `HostsHidePanel.kt` root shell 调同命令；defs.rs +HOSTS_FILE。占位 no-op 已替换为逻辑。挂载生效=待实测 |
| ④ | 四补丁共存无冲突 | ✅ 通过 | dispatch.c = phase2(stealth+dynamic_manager) 累积 + 本阶段 98/99（实测四 handler 全在）；Kconfig = phase1/2 + KPROBES_HIDE；uapi = phase2(6) + 本阶段抬 **7**。新增 98/99 落在基线空闲段（21<nr<100），不撞 100-105/106/107/200。无符号重定义。**已复核 uapi 头含 25/26 STEALTH（修复初版回退）** |
| ⑤ | 不学项未引入 | ✅ 通过 | 未移植 7kimisu 断代闸门(_G 27/28)/写死签名/KSU_MANAGER_MIN_GEN；未造 susfs 内核消费端（属外部补丁）；未内置检测域名清单；未双写 98/101 handler；boot 备份前缀 ksu_backup_ 不品牌化 |
| ⑥ | 待实测处均标注 | ✅ 通过 | ① susfsd 端到端（外部 susfs4ksu 补丁，err=126 降级）；② KPROBES_HIDE 与 KPM 运行期共存；③ hosts 文件挂载到 /system/etc/hosts 生效；④ UAPI 7 与老 ksud 拒配——均在代码注释/本文标注，未编造验证结果 |
| ⑦ | 自研处均 [自研] 标注 | ✅ 通过 | KSU_KPROBES_HIDE 开关、HOSTS_FILE、hosts_hide 读写、HostsHidePanel、façade 复用决策、UAPI 7 抬版均在代码注释标 [自研]/[改造]/[待实测] 并引用清单章节/Next 行号 |

---

## 3. 刻意不改的文件（如实记录，非遗漏）

- **kernel/supercall/supercall.c、kernel/runtime/ksud_integration.c、kernel/extras.c、kernel/hook/syscall_hook_manager.c**：本阶段实读确认与 Next 基础设施同构（reboot kprobe supercall.c:117-146；syscall_hook_manager.c tracepoint+ kretprobe；extras.c avc_spoof；ksud_integration.c input_event kprobe），**无 diff 可搬**。do_get_hook_mode 的上报分支直接复用其 `CONFIG_HAVE_SYSCALL_TRACEPOINTS` 编译态。
- **supercall.c reboot_handler_pre 不加 SUSFS 消费**：0xFAFAFAFA/0x555e1-3 属外部 susfs4ksu 补丁（notes/next.md §9），本仓库内核树不消费；加了即臆造外部补丁行为。
- **101=HOOK_TYPE handler 不动**：清单 M2 明令保留 legacy、勿双写。
- **未移植 Next getevent 音量键确认/risk detection**（Phase3 已记录范围外）。

---

## 4. 待实测项汇总（不编造结果）

| # | 项 | 验证方式 |
|---|---|---|
| T1 | susfsd 三查询端到端（0xFAFAFAFA/0x555e1-3 内核消费端） | 真机打 susfs4ksu 补丁内核；未打则 err=126 明确降级 |
| T4 | KPROBES_HIDE 与 KPM 运行期共存（同抢 ftrace/kprobe 槽位） | 编译矩阵 + 真机：CONFIG_KSU_KPROBES_HIDE=y 且 CONFIG_KPM=y，验 KPM 模块加载与钩子共存。CI 路径：GKI 5.10/5.15/6.x × {KPROBES_HIDE on/off} × {KPM on/off}；若 KPM 需 KernelPatch 外部补丁致本环境无法验证，如实标注「共存待实测」 |
| T-hosts | /data/adb/ksu/hosts 是否被模块挂载/合并到 /system/etc/hosts 生效 | 真机 + Magic Mount 模块 |
| T-uapi | UAPI 7 与老 ksud(6) 拒配、新 ksud(7) 通配 | 编译后 ensure_uapi_version_matched |

---

## 5. 编译 / 构建验证方式（如实）

- **本机无 Android/Rust-android 构建环境**，本阶段为代码交付 + 编译验证方式说明，不臆称「已编译通过」。
- **内核侧**（需设备内核树）：
  1. `uapi/supercall.h`、`kernel/Kconfig`、`kernel/supercall/dispatch.c` 按相对路径覆盖进 SukiSU fork；
  2. GKI 常规构建；预期：新增符号 `do_get_hook_mode`/`do_get_version_tag` 入命令表；`CONFIG_KSU_KPROBES_HIDE` 出现且默认 n；UAPI 头=**7**（含 25/26/98/99/106/107）。
  3. 共存编译验证：`CONFIG_KSU_KPROBES_HIDE=y CONFIG_KPM=y` 应编译通过（无 Kconfig/符号冲突）；运行期加载 = T4 待实测。
- **ksud 侧**（沿用 Phase3 口径，需 Rust aarch64-linux-android 工具链）：
  1. `susfsd.rs`/`main.rs`/`cli.rs`/`defs.rs`/`module.rs` 覆盖进 `userspace/ksud/src/`；
  2. `cargo check --target aarch64-linux-android`；
  3. 预期新增 `mod susfsd` 及其 3 pub fn；CLI `ksud susfs {status,version,variant,features}` 走 façade；`ksud module hosts {status,list,add,remove}`；无新 cargo 依赖。
- **App 侧**（沿用本机 Gradle 口径）：`HostsHidePanel.kt` 放入对应包路径，仅依赖已存在的 Shell/Compose，嵌入设置页/App Profile 即可编译。

---

## 6. 许可
- ksud(Rust) 与 manager(Kotlin) GPL-3.0；内核侧 GPL-2.0。照搬 Next 逻辑已标仓库+文件+行号；ShizuSU 新增均标 [自研] 并引用清单第四章 / notes/next.md。

---

## 7. 修复记录

### 7.1 uapi/supercall.h 回退修复（构建前自查发现）
- **bug**：初版 uapi 误以 phase1 版为底（版本 5），丢失 phase2 的 `KSU_IOCTL_STEALTH_GET/SET(25/26)` 与 `struct ksu_stealth_cmd`；而 dispatch.c 已引用 25/26 → undefined macro 编译失败。
- **修法**：改以 `phase2/uapi/supercall.h`（版本 6，含 25/26 STEALTH、106/107 DYNAMIC_MANAGER）为底合并 98/99，UAPI 6→7。逐宏核验四补丁命令齐全（25/26/98/99/101/106/107）。

### 7.2 HostsHidePanel.kt shellOut 编译报错（App compileDebugKotlin 发现）
- **根因**：libsu `Shell.cmd(cmd).exec()` 返回 `Shell.Result`（stdout 在 `.out: List<String>`、stderr 在 `.err`）；初版对 Result 直接 `.joinToString("\n") { it }` → `Cannot infer type parameter R` / `Unresolved reference 'joinToString'/'it'`。
- **修法**（:43-45）：
  ```kotlin
  private fun shellOut(cmd: String): String = runCatching {
      Shell.cmd(cmd).exec().out.joinToString("\n")
  }.getOrDefault("(shell error)")
  ```
  其余 `Shell.cmd(...).exec()`（:89 add / :102 remove）为独立执行、不取返回值，无此问题，未改。复验由构建侧重跑 `compileDebugKotlin`。

