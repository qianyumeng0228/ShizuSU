# ShizuSU Phase 2 · 补丁 2「stealth 内核隐身」实现交付

> 依据：`master/KernelSUX-migration.md` **第二章（补丁 2 · stealth）** 定案逐项实现。
> 上游照搬来源：7kimisu = `upstreams/7kimisu` @ v2.29 (0246ca0)。
> 基线（只读未改）：`I:\文档\sukisuultra` (SukiSU-Ultra @ 7fbbb1f1)。
> 续作基础：`work/phase1/`（多管理器补丁1）。本阶段所有改动均在 Phase 1 版本上继续，未回退。
>
> 标注口径：**照搬**=逐段抄写改路径/包名；**改造**=同逻辑改行为；**自研**=上游无实现、ShizuSU 新增。

---

## 0. 本阶段交付文件清单（相对 work/phase2/）

| 文件 | 类型 | 说明 |
|---|---|---|
| `kernel/manager/stealth.c` | 新建(照搬+改造) | 隐身状态机，路径改 shizusu |
| `kernel/manager/stealth.h` | 新建(照搬+自研桩) | 三函数原型 + CONFIG 关闭时内联桩 |
| `kernel/supercall/dispatch.c` | 改造(在 Phase 1 版上) | GET_INFO 门控 + stealth handler/表项 |
| `kernel/supercall/perm.c` | 改造(在基线版上) | stealth_valve_allowed 收紧版 |
| `kernel/supercall/internal.h` | 改造 | 新增 valve 声明 |
| `kernel/runtime/boot_event.c` | 改造(在基线版上) | POST_FS_DATA 调 ksu_stealth_load() |
| `uapi/supercall.h` | 改造(在 Phase 1 版上) | ksu_stealth_cmd + 命令号 25/26，UAPI 5→6 |
| `kernel/Kconfig` | 改造(在 Phase 1 版上) | 新增 CONFIG_KSU_STEALTH |
| `kernel/Kbuild` | 改造(在 Phase 1 版上) | manager/stealth.o + ccflags |
| `manager/.../ui/security/StealthReceiver.kt` | 新建(照搬 v2.29 收紧版) | 拨号密令广播接收 |
| `manager/.../ui/security/Stealth.kt` | 新建(照搬) | 三态封装 + native 桥 |
| `manager/.../ui/security/StealthCodeStore.kt` | 新建(照搬+改造路径) | 密令跨卸载持久化 |
| `manager/.../ui/security/StealthBootReceiver.kt` | 新建(照搬) | 开机恢复图标 |
| `manager/.../AndroidManifest.stealth-snippet.xml` | 片段(照搬) | 两个 receiver 注册 |
| `manager/.../Natives.stealth-additions.kt.txt` | 片段(自研说明) | Natives 新增两 external 方法 |

---

## 1. 逐文件改动点与源码依据

### 1.1 `kernel/manager/stealth.c`（新建）
- **照搬** 7kimisu `kernel/manager/stealth.c` 全文（v2.29，98 行）：
  - `ksu_stealth_write()`（上游 :34-53）：`override_creds(ksu_cred)` 后 `filp_open(O_WRONLY|O_CREAT|O_TRUNC, 0644)` + `kernel_write`。
  - `ksu_stealth_load()`（上游 :55-74）：先置 `loaded=true`；`filp_open(O_RDONLY)` 失败 → `enabled=false`（**默认关闭**）；成功则读 1 字节，`enabled = (read==1 && c=='1')`。
  - `ksu_stealth_is_enabled()`（上游 :76-82）：懒加载。
  - `ksu_stealth_set()`（上游 :84-97）：写盘 `'0'/'1'` + 置内存态。
- **保留** GPL-2.0 头与上游作者注释（文件头 :1-16）。
- **[自研] 路径改造**：`#define KSU_STEALTH_PATH "/data/adb/shizusu/stealth"`（上游 :29 原值 `/data/adb/sevenk/stealth`）。依据清单 2.1②。
- **[自研] 裁剪**：整体包 `#ifdef CONFIG_KSU_STEALTH`。
- **[自研] 日志加固**：上游两处 `pr_info("stealth: ...")`（:73 loaded、:95 set）与 short-write `pr_warn`（:48）全部降为 `pr_debug`。理由：见 §4 加固说明。

### 1.2 `kernel/manager/stealth.h`（新建）
- **照搬** 7kimisu `kernel/manager/stealth.h`（:6-8 三原型：load/is_enabled/set）。
- **[自研] 追加** `#else`（CONFIG_KSU_STEALTH=n）内联桩：`load(){}` / `is_enabled(){return false;}` / `set(){return -ENODEV;}`。使 dispatch.c 门控在隐身被裁剪时仍可无条件编译、行为退化为「恒不隐身」。

### 1.3 `kernel/supercall/dispatch.c`（在 Phase 1 版上改造）
- ① **include**（Phase 1 :18 之后）：新增 `#include "manager/stealth.h"`（现 :19）。
- ② **GET_INFO 门控**（照搬 7kimisu `dispatch.c:53` 与 `:84`）：
  - `do_get_info()`：现 :63 `if (is_manager() && !ksu_stealth_is_enabled()) {`（原 Phase 1 :59）。
  - `do_get_info_legacy()`：现 :97 同行（原 Phase 1 :90）。
  - 内核 `is_manager()` 保持真实，stealth 只压制上报 flag —— 与 7kimisu 逐字一致。
- ③ **handler**（照搬 7kimisu `dispatch.c:739-761`，现 :888 `do_stealth_get`、:901 `do_stealth_set`，包 `#ifdef CONFIG_KSU_STEALTH`）：
  - `do_stealth_get`：`cmd.enabled = ksu_stealth_is_enabled()?1:0; copy_to_user`。
  - `do_stealth_set`：`copy_from_user; return ksu_stealth_set(cmd.enabled != 0);`。
- ④ **命令表**（照搬 7kimisu `dispatch.c:988-999`，现 :1122 起，包 `#ifdef CONFIG_KSU_STEALTH`）：
  - `KSU_IOCTL_STEALTH_GET`(25) → `do_stealth_get`，`.perm_check = only_manager`。
  - `KSU_IOCTL_STEALTH_SET`(26) → `do_stealth_set`，`.perm_check = only_manager`。
  - **不学** `_G` 变体（27/28，断代闸门安全阀），未建。

### 1.4 `kernel/supercall/perm.c`（在基线版上改造）
- 基线 31 行五谓词原样保留（only_manager/only_root/manager_or_root/always_allow/allowed_for_su）。
- 新增 `stealth_valve_allowed()`（**照搬收紧版**：7kimisu `perm.c:70-81` 删第三判据）：
  ```c
  if (current_uid().val == 0) return true;   // root 救砖
  if (is_manager()) return true;              // 已认主管理器
  return false;                               // 第三判据 ksu_is_signed_manager_uid 不学
  ```
- **[自研] 剪点说明**：7kimisu 第三判据 `ksu_is_signed_manager_uid(uid)` 依赖 `ksu_signed_manager_appid`，与「断代闸门」(KSU_MANAGER_MIN_GEN=2) 耦合，属清单 2.5 不学项；基线 manager_identity.h 也无此符号。首版 valve 预留给未来扩展，**不接线**（首版 STEALTH_GET/SET 仍用 only_manager）。

### 1.5 `kernel/supercall/internal.h`（在基线版上改造）
- 基线 :12 之后新增声明 `bool stealth_valve_allowed(void);`（对齐现有 perm 声明模式）。

### 1.6 `kernel/runtime/boot_event.c`（在基线版上改造）
- 新增 `#include "manager/stealth.h"`。
- `on_post_fs_data()`（基线 :17）内、`ksu_load_allow_list()`（基线 :29）之后，新增 `ksu_stealth_load();`（现 :36）。
- **落点依据**：清单 2.1② —— 7kimisu 在 `dispatch.c:117 do_report_event` 的 EVENT_POST_FS_DATA 分支直接调；ShizuSU 以自身 `on_post_fs_data()` 为统一落点（与 allowlist 同生命周期，/data/adb 已挂载）。CONFIG 关闭时走内联空桩，无条件调用即可。

### 1.7 `uapi/supercall.h`（在 Phase 1 版上改造）
- 新增 `struct ksu_stealth_cmd { __u8 enabled; };`（照搬 7kimisu `uapi/supercall.h:209-211`）。
- 新增命令号（照搬 7kimisu :213-214）：
  - `KSU_IOCTL_STEALTH_GET = _IOR('K', 25, struct ksu_stealth_cmd);`
  - `KSU_IOCTL_STEALTH_SET = _IOW('K', 26, struct ksu_stealth_cmd);`
- **[自研] UAPI 版本 5→6**：Phase 1 为 5（动态管理器 106/107）；本阶段新增命令 25/26，按清单第六章「增命令必须抬版本」抬为 6（注释行已记 `6: add KSU_IOCTL_STEALTH_GET(25)/SET(26)`）。
  - 注：7kimisu 故意不抬 `_G` 版本的做法**不学**（那是为兼容老内核的特例，且 `_G` 本身不移植）。

### 1.8 `kernel/Kconfig`（在 Phase 1 版上改造）
- 新增 `config KSU_STEALTH`（bool，depends on KSU，**default y**），挂在 menu "KernelSU" 内、endmenu 前。依据清单第七章。

### 1.9 `kernel/Kbuild`（在 Phase 1 版上改造）
- 独立门控（现 :37）`ifeq ($(CONFIG_KSU_STEALTH),y) kernelsu-objs += manager/stealth.o endif`（与多管理器正交，不依赖 DISABLE_MANAGER）。
- KBUILD_EXTMOD 段（现 :88-90）`ccflags-y += -DCONFIG_KSU_STEALTH=1`（外部模块构建时使 .c 内 `#ifdef` 可见）。
- Phase 1 的 `KSU_MULTI_MANAGER_SUPPORT`/`KSU_DYNAMIC_MANAGER` 开关、签名注入变量**原样保留**，无冲突。

### 1.10 App 侧（manager/，包 `com.sukisu.ultra.ui.security`）
- `StealthReceiver.kt`：**照搬 7kimisu v2.29 收紧版**（`.../com/sevenk/core/ui/security/StealthReceiver.kt`，353 行）——`senderTrusted()`(:115) 先验发送方再看内容、API<34 fail-open、uid==1000/1001/myUid 放行、其余须有系统包身份（默认拨号器/FLAG_SYSTEM）、adb(2000) 无包身份→拒；连错 5 次锁 30s（纯内存）；密令内容绝不进日志。包名/import 改 `com.sukisu.ultra.*`，TAG 改 `shizusu-stealth`，默认密令引用 `StealthCodeStore.DEFAULT_CODE`。
- `Stealth.kt`：**照搬**（三态 State、looksStealthy、setEnabledReporting、ensureLauncherVisible）；`STEALTH_FLAG_PATH="/data/adb/shizusu/stealth"`（[自研] 路径）；`UAPI_WITH_STEALTH=6`（[自研] 对齐本仓 UAPI）；LAUNCHER_ALIAS 指向基线 `.ui.MainActivityAlias`。
- `StealthCodeStore.kt`：**照搬+[自研]路径** `/data/adb/shizusu/stealth_code`，兜底旧路径 `/data/adb/ksu/stealth_code`；临时文件+回读核验+600 权限的写盘逻辑原样。
- `StealthBootReceiver.kt`：**照搬**（开机/锁屏开机后 ensureLauncherVisible）。
- `AndroidManifest.stealth-snippet.xml`：**照搬** 7kimisu manifest:91-110 两个 receiver 注册（SECRET_CODE + `android_secret_code` scheme，只声明 scheme 不声明 host）。
- **签名校验落点**：按清单 2.4/2.2 定案，stealth 写入的签名校验**在内核侧**——STEALTH_SET 的 `perm_check=only_manager`，即由 Phase 1 多签名表加冕出的 `is_manager()` 把关；App 侧不另做签名校验。密令发送方可信判定才在 App 层（senderTrusted）。

---

## 2. 关于「密令常量 70707 编译期可配」的落点说明（重要）

任务书 perm.c 一条提到「密令常量 70707 编译期可配（Kconfig/Kbuild 变量）」。**经回源码核验，内核侧不存在任何密令逻辑**：
- 核验笔记 `notes/7kimisu.md` §3 明确：7kimisu 内核 `perm.c` 是纯 uid 判据，**无密令/拨号器/预装应用判断**；密令 `*#*#70707#*#*`、`DEFAULT_SECRET_CODE="70707"`（`StealthReceiver.kt:324`）、`StealthCodeStore.DEFAULT_CODE`（:53）**全在 App 层 Kotlin**。
- 若在 kernel Kbuild 塞一个 `70707` 宏，内核代码永远不会引用它 = 死常量，违反铁律「禁止臆造常量/行为」。

**故 [自研] 把「密令编译期可配」落到 App 层 `BuildConfig.STEALTH_SECRET_CODE`**（清单 2.4 原议）：`StealthCodeStore.DEFAULT_CODE` 当前回落 `"70707"`，并在文件内 TODO 标注需在 `app/build.gradle.kts` 配 `buildConfigField("STEALTH_SECRET_CODE", "\"70707\"")` 后改为引用 `com.sukisu.ultra.BuildConfig`。内核侧不做、也不存密令常量。

---

## 3. 静态自查逐项结论（对照清单第二章）

| # | 自查项 | 结论 | 证据 |
|---|---|---|---|
| ① | stealth.c 状态读写 / 载入点 / 默认关闭 | ✅ 通过 | 单字节 '0'/'1' 写盘；`ksu_stealth_load()` 在 boot_event.c:36 POST_FS_DATA 调；文件打不开 → enabled=false（stealth.c load） |
| ② | GET_INFO 门控落基线行号 + stealth 时 flag 清除 | ✅ 通过 | dispatch.c:63 与 :97 = `is_manager() && !ksu_stealth_is_enabled()`（原基线 58-60/89-91，Phase 1 续作行号 59/90 已平移） |
| ③ | perm 收紧版 + 密令可配 | ✅ 通过 | perm.c valve 两判据（删签名候选）；密令可配落 App BuildConfig（见 §2） |
| ④ | App StealthReceiver + manifest | ✅ 通过 | 四 Kotlin 文件 + manifest 片段已产出；v2.29 收紧版逻辑逐字保留 |
| ⑤ | 日志分级在点位 | ✅ 通过 | stealth.c 三处内核日志降 pr_debug（见 §4） |
| ⑥ | 不学项未实现 | ✅ 通过 | 全仓 grep 无 STEALTH_GET_G/SET_G、manager_gen、app_lock、KSU_MANAGER_MIN_GEN、写死签名 0x034d/490c66（perm.c 中出现仅为「为何不学」的注释文字） |
| ⑦ | 与 Phase 1 共存无冲突 | ✅ 通过 | stealth 全程不碰 ksu_manager_appid（正交）；Kbuild/Kconfig 保留 Phase 1 全部开关与签名注入；UAPI 仅递增；dispatch.c 表项追加于 sentinel 前 |
| ⑧ | 自研处均有 [自研] 标注 | ✅ 通过 | 路径适配、内联桩、日志降级、UAPI 抬 6、密令 BuildConfig、裁剪门控均标注 |

---

## 4. 日志加固（[自研]）说明

- 清单第二章验收要求「logcat 无泄露（su 拒绝日志按开关分级）」。
- **回基线源码核实**：su 授权/拒绝事件经 `sulog/event.c` 的 fd 事件队列送**用户态**管理器（`ksu_sulog_emit_grant_root`），**不**经内核 `pr_info` 进 logcat；基线也**无 ksu.c**，perm.c 仅谓词无日志。故内核侧**不存在**可降级的 su grant/deny pr_info。
- 真正会向 logcat 暴露「内核/隐身存在」的内核日志是 **stealth.c 自身**的 `pr_info("stealth: loaded, %s")` / `pr_info("stealth: set to %d")`（上游原样）。
- **[自研] 处置**：这三处降为 `pr_debug`（生产内核默认不输出），使隐身开关与加载不在 kernel log 留痕。已在 stealth.c 内注释说明。

---

## 5. 「点 5 下内核版本号」恢复通道的处理

- 核验笔记 §1.2：该手势在 7kimisu v2.29 源码里**仅作为理由出现在注释**（stealth.c:10-12），**未定位到独立计数器** → 属清单 T6「待实测」，本阶段**不实现**。
- 内核 is_manager **保持真实**（stealth 只压 GET_INFO flag），故已认主管理器即使在隐身开启下仍可凭 only_manager 调 STEALTH_SET 关隐身；叠加 App 拨号密令通道（*#*#70707#*#*），恢复路径完整。已在 IMPLEMENTATION 与代码注释中注明。

---

## 6. 编译 / 构建验证说明（如实）

- **本机无 Android 构建环境、无内核树编译链**，故本阶段交付为**代码交付 + 编译验证方式说明**，不臆造任何「已编译通过」结果。
- **内核侧**（可在设备内核树验证）：
  1. 以上 9 个内核/uapi 文件按相对路径覆盖进 SukiSU fork；
  2. defconfig 中 `CONFIG_KSU_STEALTH=y`（默认 y），随 GKI/non-GKI 矩阵常规编译；
  3. 预期符号：`ksu_stealth_load/is_enabled/set`、`stealth_valve_allowed`；命令 25/26 在 `ksu_supercall_dump_commands` 输出中可见；
  4. 验收点：隐身开时 GET_INFO 不置 MANAGER flag；写 `/data/adb/shizusu/stealth`='1' 重启后生效；删掉该文件即默认关。
- **App 侧**（需 Android Studio / Gradle）：
  1. Kotlin 四文件放入 `com.sukisu.ultra.ui.security`；
  2. 按 `Natives.stealth-additions.kt.txt` 给 Natives 补 `stealthState()/stealthSet()` 两个 external，并在 cpp 层实现对应 ioctl(25/26)；
  3. 把 `AndroidManifest.stealth-snippet.xml` 两 receiver 并入 AndroidManifest.xml；
  4. 在 strings.xml 补 5 个 `stealth_restore_*` 文案；在 SettingsRepositoryImpl 加 `stealthCode` 属性；在 build.gradle 配 `BuildConfig.STEALTH_SECRET_CODE`；
  5. **上述 2-4 为基线集成衔接点**（基线原本无隐身功能），代码内以 TODO 标注，需 fork 侧补齐后才能编译——这是未在 untouched 基线上臆称可编译的原因。

---

## 7. 许可

- 内核侧移植遵循上游 GPL-2.0（保留 7kimisu 版权头与作者署名）；App 侧遵循 GPL-3.0。
- 所有照搬均标注了上游仓库 + 文件 + 行号；所有 ShizuSU 新增行为均标 [自研] 并引用清单决策。
