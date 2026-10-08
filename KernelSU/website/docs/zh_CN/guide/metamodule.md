# 模块系统：Magic Mount {#introduction}

ShizuSU 基于 **SukiSU-Ultra** 二次开发，其模块系统直接继承了 SukiSU-Ultra 的 **Magic Mount** 方案（源自 5ec1cff 的 Magisk 实现）。

**Magic Mount 是 ShizuSU 唯一的模块挂载机制**——ShizuSU 不使用官方 KernelSU 的 OverlayFS metamodule 架构，因此不存在"两种模块系统"的并存。安装 ShizuSU 后，修改 `/system` 文件的模块开箱即用，无需安装任何 metamodule。

## KernelSU 框架与 Magic Mount 模块系统 {#two-subsystems}

ShizuSU 由两个**并列**的核心子系统组成，它们职责分明、互不重叠：

| 子系统 | 职责 | 说明 |
|---|---|---|
| **KernelSU 内核框架** | root 授权与管理 | 运行在内核空间：`su` 授权、白名单访问控制、受限 root 权限（uid / gid / groups / capabilities / SELinux）、内核级接口 |
| **Magic Mount 模块系统** | 模块安装与 systemless 挂载 | 把模块的 `system` 目录以 **bind mount** 方式叠加到系统分区，实现无系统修改 |

两者的分工可以概括为：

- **KernelSU 框架回答"谁可以拿到 root"**：`su` 的授予、隔离与审计全部发生在内核空间，用户空间无法绕过。
- **Magic Mount 回答"模块如何修改系统"**：模块文件的挂载、覆盖、合并与隐藏由 Magic Mount 完成，不触碰物理分区。

它们是**并列关系**而非包含关系：KernelSU 框架负责 root 能力，Magic Mount 负责模块挂载能力，二者共同构成 ShizuSU 的完整 root 体验。

## 为什么采用 Magic Mount？ {#why-magic-mount}

SukiSU-Ultra（以及继承它的 ShizuSU）选择 Magic Mount 而非官方 KernelSU 的 OverlayFS metamodule 架构，原因如下：

- **更稳定的基础**：Magic Mount 源自 Magisk 的成熟实现（5ec1cff），经过大量设备与模块生态的长期验证。
- **更好的模块兼容性**：Magisk 生态中依赖 `system` 目录挂载的模块可以**直接使用**，无需 metamodule、无需转换。
- **更少的检测面**：挂载逻辑以 bind mount 完成，ShizuSU 本体不依赖 OverlayFS 特征，更难被应用检测。
- **更简单的部署**：无需额外安装 meta-overlayfs 等 metamodule，安装 ShizuSU 后模块即可正常工作。

## Magic Mount 工作原理 {#how-it-works}

Magic Mount 使用 **bind mount**（绑定挂载）把模块内容"叠加"到系统目录上：

1. 模块放置在 `/data/adb/modules/<模块ID>/`，其中的 `system/` 目录对应系统分区。
2. 启动时，ShizuSU 遍历所有已启用模块，把每个模块的 `system/` 目录 bind mount 到 `/system` 的对应路径。
3. **同名文件**：模块文件覆盖系统文件。
4. **同名目录**：模块目录与系统目录合并（模块文件叠加在上层）。
5. **删除系统文件**：通过把模块目录中对应路径挂载为一个空目录（whiteout），从而"隐藏"系统文件。
6. **替换系统目录**：通过把模块目录中对应路径挂载为一个空目录，实现整目录替换。

整个过程只读取模块目录与系统目录，**不修改物理分区**——这正是 systemless（无系统修改）的含义。

## 与官方 KernelSU 的差异 {#difference}

| | 官方 KernelSU | ShizuSU（基于 SukiSU-Ultra） |
|---|---|---|
| 模块挂载机制 | OverlayFS（需安装 metamodule，如 meta-overlayfs） | **Magic Mount**（内置，无需 metamodule） |
| 修改 `/system` 的模块 | 需要先安装 metamodule | 直接可用 |
| Magisk 模块兼容性 | 部分兼容（依赖 metamodule） | 直接兼容 |

::: info 迁移说明
如果你之前使用官方 KernelSU 并安装了 metamodule（如 meta-overlayfs），迁移到 ShizuSU 后不再需要它：直接安装或使用普通模块即可。
:::

## 模块开发 {#module-dev}

ShizuSU 的模块结构与 Magisk 完全一致（`module.prop`、`system/`、`post-fs-data.sh`、`service.sh` 等），Magisk 模块开发者可以直接上手。详见[模块开发指南](module.md)。
