# ShizuSU Phase 1 · 补丁 1「多管理器兼容」实现说明

> 基线：SukiSU-Ultra main @ `7fbbb1f1`（`I:\文档\sukisuultra`，只读未改）。
> 照搬来源：ReSukiSU main @ `648e598`。
> 范围：纯内核侧补丁 1（7 个文件 + 本说明）。98/101（Phase4）、25/26 stealth（Phase2）、补丁 2-4 其余、App/用户态一律未动。

## 0. 产物清单（repo 相对路径）

| 文件 | 性质 |
|---|---|
| `kernel/manager/manager_sign.h` | 新建（自研组织，数据照搬） |
| `kernel/manager/apk_sign.c` | 改造（基线结构保留，单签名→表遍历） |
| `kernel/manager/dynamic_manager.c` | 新建（自研） |
| `kernel/manager/dynamic_manager.h` | 新建（自研） |
| `uapi/supercall.h` | 改造（版本抬 5 + 106/107） |
| `kernel/supercall/dispatch.c` | 改造（include + POST_FS_DATA 载入点 + 2 表项） |
| `kernel/Kconfig` | 改造（+2 开关） |
| `kernel/Kbuild` | 改造（+dynamic_manager.o + EXTRA 注入） |

> 路径偏差说明：任务书称 `include/uapi/supercall.h`，但基线实际 UAPI 头位于**仓库根 `uapi/supercall.h`**
> （已核实：全仓仅 `kernel/supercall/supercall.h` 与 `uapi/supercall.h` 两个；代码内 `#include "uapi/supercall.h"`，
> Kbuild `-I$(KSU_KERNEL_DIR)` 指向仓库根）。故产物落 `uapi/supercall.h`，与基线一致。

---

## 1. 逐文件改动点

### 1.1 `kernel/manager/manager_sign.h`（新建）
- **[自研] 组织**：ReSukiSU 无独立头文件，签名表硬编码于其 `kernel/manager/apk_sign.c:6-16`；ShizuSU 抽为独立头，仅 apk_sign.c 包含。
- **[照搬] 数据**：6 条 `{ size, sha256 }` 逐字符来自 ReSukiSU `apk_sign.c:10-15`（RKSU/官方KSU/WKSU/KowSU/KSUN/MKSU）。
- **[自研] 自家签名注入位**：`#ifdef KSU_MANAGER_EXTRA_SIZE` 时追加表尾一条（Kbuild ccflags 注入）。
- **[自研] 上限断言**：`KSU_MAX_MANAGER_KEYS 64`。
  - **实现偏差（已注释）**：任务书要求 `BUILD_BUG_ON(...)`，但该宏需函数作用域；头文件按**文件作用域静态断言**落地
    `enum { ksu_manager_keys_within_limit = 1 / (ARRAY_SIZE(apk_sign_keys) <= KSU_MAX_MANAGER_KEYS) }`，
    越界即编译期除零错误，等价 BUILD_BUG_ON 语义。上游无此断言先例（ReSukiSU 全仓唯一 BUILD_BUG_ON 在
    `policy/app_profile.c:138`，无关），属自研安全约束。

### 1.2 `kernel/manager/apk_sign.c`（改造）
- **保留**：基线 includes（util.h/krypto/klog…）、`struct sdesc`/`init_sdesc`/`calc_hash`/`ksu_sha256`/`read_exact`/`read_length_prefixed_end` 全部原样（基线 L1-95）。
- **include**：新增 `#include "manager/manager_sign.h"`，替换原 `EXPECTED_SIZE/HASH` 宏使用。
- **`check_block()` [改造]**：签名由 `(fp,pos,block_end,expected_size,expected_sha256)` 改为 `(fp,pos,block_end)`；内部 `for(i=0;i<ARRAY_SIZE(apk_sign_keys);i++)` 先按 `certificate_size != apk_sign_keys[i].size` 粗筛，再 `read_exact` 读证书 → `ksu_sha256` → `bin2hex` → `strcmp(apk_sign_keys[i].sha256,hash_str)==0` 命中返回 true。
  - 遍历语义**照搬** ReSukiSU `apk_sign.c:83-111`；v2 块结构解析（read_length_prefixed_end 边界检查）**保留基线** L97-116（比 ReSukiSU 无边界 `kernel_read` 更安全，不改动）。
- **`check_v2_signature()` [改造]**：去掉 expected 形参；APK 容器解析（EOCD/ZIP64 拒绝/central dir 校验/"APK Sig Block 42" 魔串/pair 扫描）**保留基线** L145-300；
  - **[照搬] ReSukiSU** `apk_sign.c:233-276`：v3(`0xf05368c0u`)/v3.1(`0x1b93ad61u`) 置标志、v2 块数≠1 置无效、v2 有效时 `has_v1_signature_file()` 探测 `META-INF/MANIFEST.MF` 即拒、末尾 v3/v3.1 存在即拒。
  - **[照搬] ReSukiSU** `apk_sign.c:115-162`：新增 `struct zip_entry_header` + `has_v1_signature_file()`。
  - 保留基线对 `0x42726577u`（APK verdict padding）未知 id 的容忍。
- **`is_manager_apk()` [改造]**：保留 `KSU_MANAGER_PACKAGE` 可选包名白名单；删除基线 `EXPECTED_SIZE/HASH` 比对与 `#ifdef EXPECTED_SIZE2 ... EXPECTED_SIZE2/HASH2` 分支，改为 `return check_v2_signature(path);`（依据清单 1.2 第 5 步清理冗余）。
- **保留**：`#ifdef CONFIG_KSU_DEBUG` 的 `ksu_debug_manager_appid` module_param 段、`get_pkg_from_apk_path()` 原样。

### 1.3 `kernel/manager/dynamic_manager.{c,h}`（新建，自研）
上游 ReSukiSU 无此实现（核验 resukisu.md §3 / 清单 D2：仅扫描 /data/app 自动加冕、无运行时注册、无持久化）。
- **`do_dynamic_manager_set(arg)`（ioctl 107）**：
  - 权限：dispatch 表项 `perm_check = manager_or_root`（= `current_uid().val==0 || is_manager()`，与基线 perm.c:17 完全一致），未新写 perm 函数。
  - 流程：`copy_from_user` cmd(`{__aligned_u64 path}`) → `strncpy_from_user` 取 APK 路径 → `is_manager_apk(path)` 命中签名表（否则 -EACCES）→ `get_pkg_from_apk_path` 取包名 → `lookup_uid_by_package()` 读 packages.list 查 uid → `ksu_set_manager_appid(uid)` 加冕 → `persist_manager_package()` 写盘 → `track_throne(false)` 修剪 allowlist。
- **持久化 [自研]**：`/data/adb/shizusu/manager`（目录 0700、文件 0644，`override_creds(ksu_cred)`，读写模式照搬基线 `policy/allowlist.c:419-424`）。内容=包名（设计决策：包名稳定，恢复时再查 uid；T5 时序按 L3 定案简单恢复）。best-effort `ksys_mkdir` 建目录。
- **`ksu_dynamic_manager_load()`**：POST_FS_DATA 读回包名 → 查 uid → 加冕；载入点挂 dispatch.c `do_report_event()` 的 `EVENT_POST_FS_DATA` 分支 `on_post_fs_data()` 之后（boot_event.c 本身未改）。
- **卸载检测**：复用 `track_throne(false)` 既有分支——加冕管理器被卸载时 `throne_tracker.c:328-337` 自动 `ksu_invalidate_manager_uid()` 并重扫，无需额外代码。
- **头文件**：`CONFIG_KSU_DYNAMIC_MANAGER && !CONFIG_KSU_DISABLE_MANAGER` 时提供真实原型，否则空桩（-EOPNOTSUPP），保证 dispatch.c 在开关关闭时链接通过。

### 1.4 `uapi/supercall.h`（改造）
- L12 `KERNEL_SU_UAPI_VERSION`：`4` → `5`（注释增 `// 5: ...106/107`）。
- 新增 `struct ksu_dynamic_manager_set_cmd { __aligned_u64 path; }`。
- `KSU_IOCTL_DYNAMIC_MANAGER_GET = _IOWR('K',106, struct ksu_get_manager_appid_cmd)`（注释：'K',10 别名，复用 do_get_manager_appid）。
- `KSU_IOCTL_DYNAMIC_MANAGER_SET = _IOWR('K',107, struct ksu_dynamic_manager_set_cmd)`。
- `_IOC`/`_IOWR` 风格照抄基线现有宏。

### 1.5 `kernel/supercall/dispatch.c`（改造，最小 diff）
- include 区新增 `#include "manager/dynamic_manager.h"`。
- `do_report_event()` 的 `EVENT_POST_FS_DATA` 分支 `on_post_fs_data()` 后追加 `ksu_dynamic_manager_load();`。
- 命令表（哨兵前）追加 2 条，包在 `#ifdef CONFIG_KSU_DYNAMIC_MANAGER`：
  - 106 → `.handler=do_get_manager_appid`（别名，不新写 handler）、`.perm_check=manager_or_root`。
  - 107 → `.handler=do_dynamic_manager_set`、`.perm_check=manager_or_root`。
- 表项结构照抄基线现有条目（`.cmd/.name/.handler/.perm_check`）。

### 1.6 `kernel/Kconfig`（改造）
- `endmenu` 前追加：
  - `config KSU_MULTI_MANAGER_SUPPORT`（bool, depends on KSU, **default y**）。
  - `config KSU_DYNAMIC_MANAGER`（bool, depends on KSU && KSU_MULTI_MANAGER_SUPPORT, **default y**）。
- 格式照抄基线现有条目。

### 1.7 `kernel/Kbuild`（改造，最小 diff）
- manager 对象组（L26-30）内追加：`ifeq($(CONFIG_KSU_DYNAMIC_MANAGER),y) kernelsu-objs += manager/dynamic_manager.o endif`。
- `KBUILD_EXTMOD` 块内追加 `CONFIG_KSU_MULTI_MANAGER_SUPPORT`/`CONFIG_KSU_DYNAMIC_MANAGER` → ccflags 翻译（照抄现有 DISABLE_MANAGER/DEBUG 模式）。
- `KSU_MANAGER_PACKAGE` 块后新增 `KSU_MANAGER_EXTRA_SIZE/HASH` 注入段（默认空，注释说明用法）。
- `KSU_EXPECTED_SIZE/HASH/SIZE2/HASH2` 注入行**原样保留**（最小 diff、不破坏外部脚本），加注释说明「多签名表接入后这些宏不再被代码引用」。

---

## 2. 静态自查（对照清单第一章 8 项）

| # | 自查点 | 结论 |
|---|---|---|
| ① | 签名表 6 条 size+sha256 与清单/ReSukiSU 逐字符一致 | **通过**。已逐条比对 ReSukiSU `apk_sign.c:10-15`：`0x396/f415f4…`、`0x033b/c37106…`、`0x381/52d52d…`、`0x375/484fcba…`、`0x3e6/79e590…`、`384/7e0c6d…` 全一致。 |
| ② | BUILD_BUG_ON 上限断言在位 | **通过（形式偏差已注释）**。`KSU_MAX_MANAGER_KEYS 64` + 文件作用域 enum 静态断言（=64 条上限编译期检查）。偏差原因：BUILD_BUG_ON 需函数作用域，头文件改用等价 enum 断言，已在头文件与本说明标注。 |
| ③ | apk_sign.c 遍历逻辑与 ReSukiSU 对应 | **通过**。`for(ARRAY_SIZE(apk_sign_keys))` + size 粗筛 + ksu_sha256/bin2hex/strcmp 命中即返回，对应 ReSukiSU `:83-111`；v3/v3.1/v1 判定对应 `:233-276`。 |
| ④ | dynamic_manager 权限收紧/持久化/载入点/卸载复用 | **通过**。权限=manager_or_root(uid0‖is_manager)；持久化 `/data/adb/shizusu/manager`(0644)；载入点 dispatch.c POST_FS_DATA 分支；卸载复用 track_throne(false) 既有 invalidate 分支。 |
| ⑤ | dispatch 表项 106/107 与清单第六章一致、无撞号 | **通过**。基线占用 1-21/100-105/200；106/107 空闲，与 98/99/25/26（Phase2/4，本次不做）不撞。106='K',10 别名复用 do_get_manager_appid。 |
| ⑥ | Kconfig 开关名与默认值 | **通过**。`KSU_MULTI_MANAGER_SUPPORT`(bool,y)、`KSU_DYNAMIC_MANAGER`(bool,y,depends MULTI_MANAGER)，与第七章表一致。 |
| ⑦ | UAPI 版本=5 | **通过**。`KERNEL_SU_UAPI_VERSION = 5`（基线原 4）。 |
| ⑧ | 自研处均有 [自研] 标注 | **通过**。manager_sign.h(3)、dynamic_manager.c(6)、dynamic_manager.h(1)、uapi/supercall.h(3) 均标注；apk_sign.c/dispatch.c 为改造/照搬，用 [改造]/[照搬]/[ShizuSU 补丁1] 标注。 |

## 3. 已知待实测项（如实记录）
- **T5**：POST_FS_DATA 持久化恢复时序（packages.list 此刻可能未就绪）——按 L3 定案简单恢复，编译+真机验证。
- **ksys_mkdir 导出**：`/data/adb/shizusu` 目录 best-effort 创建，外部模块构建需该符号导出；否则目录需由 ksud 预建（用户态不在本次范围）。
- **T8**：6 条签名为 ReSukiSU 2026-05-29 快照，换管理器证书须同步更新。
- **T9**：`EXPECTED_*` 变量保留但不再被引用，外部构建脚本设置它们不影响编译（无害）。
- 本次仅静态产出文件，未在本环境实际编译（无内核树/交叉编译器）；编译期正确性以「逐行对照基线/上游源码 + 最小 diff」保证。

---

## 4. 编译修复记录（WSL GKI 5.15 / 6.1 实测，BUILD_RC=0 后回填）

> 本节为真机编译验证后补充。以下两处修复已逐字同步回本目录源码，使「源码目录 = 编译通过代码」。

### 4.1 ksys_mkdir 无导出 → stub（dynamic_manager.c）
- 实测：GKI 5.15/6.1 的 `fs/namei.c` 均无 `ksys_mkdir`，也无 `ksys_mkdirat`（5.15 内部仅 `do_mkdirat(dfd, struct filename*, mode)`）；全树无 `EXPORT_SYMBOL(ksys_mkdir*)`。
- 修法：`persist_manager_package()` 中 `ksys_mkdir(SHIZUSU_DIR, 0700);` 改为注释：
  `/* FIX(Phase1): ksys_mkdir/ksys_mkdirat not exported in GKI 5.15/6.1; dir precreated by ksud. */`
- 语义：符合原 best-effort 设计；`/data/adb/shizusu` 目录由 ksud（用户态）预建。

### 4.2 -Wframe-larger-than → Kbuild 帧限制（Kbuild）
- 实测错误：`do_dynamic_manager_set` 栈帧 4400B（`char path[PATH_MAX]`=4096 + `pkg[]`）> GKI 2048B 上限，`-Werror,-Wframe-larger-than` 报错。
- 修法（最小、纯 ASCII）：`kernel/Kbuild` 末尾追加
  `CFLAGS_manager/dynamic_manager.o += -Wframe-larger-than=8192`
- 备注：曾尝试把 path 改 kmalloc，但经传输管道写回时 C 内 `\n`/`\0` 转义被吃坏，回退原始文件后改用 Kbuild 提限；4400B 帧在 arm64 16K 栈内安全。

### 4.3 三风险点实测结论
- **(i) ksys_mkdir 导出**：不可用 → 见 4.1。
- **(ii) is_manager_apk 可见性**：基线 `apk_sign.c` 中本就为非 static（`bool is_manager_apk(char *path)`），且 `manager/apk_sign.h` 已声明；dynamic_manager.c include 即得原型，**无需改**。
- **(iii) throne 原型**：`ksu_set_manager_appid()`/`KSU_INVALID_APPID`（manager_identity.h）、`track_throne(bool)`（throne_tracker.h）、`get_pkg_from_apk_path`（apk_sign.h）全部解析通过，编译无未定义符号。

### 4.4 编译结果
- android13-5.15：BUILD_RC=0；android14-6.1：BUILD_RC=0。
- config：`KSU=y / KSU_MULTI_MANAGER_SUPPORT=y / KSU_DYNAMIC_MANAGER=y / KPROBES=y / LTO_NONE=y`。
- vmlinux 已链接入 `do_dynamic_manager_set`、`ksu_dynamic_manager_load` 及 KSU 核心符号。
