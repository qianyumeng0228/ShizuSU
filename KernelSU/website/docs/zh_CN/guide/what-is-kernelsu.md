# 什么是 ShizuSU？ {#introduction}

ShizuSU 是 Android GKI 设备的 root 解决方案，它工作在内核模式，并直接在内核空间中为用户空间应用程序授予 root 权限。

## 功能 {#features}

ShizuSU 的主要特点是它是**基于内核的**。ShizuSU 运行在内核空间，所以它可以提供我们以前从未有过的内核接口。例如，我们可以在内核模式下为任何进程添加硬件断点；我们可以在任何进程的物理内存中访问，而无人知晓；我们可以在内核空间拦截任何系统调用; 等等。

此外，ShizuSU 基于 **SukiSU-Ultra** 二次开发，模块系统采用 **Magic Mount**（源自 5ec1cff 的 Magisk 实现）：模块的 `system` 目录通过 bind mount 以 systemless 方式叠加到 `/system`，**无需安装 metamodule**。详见[模块系统：Magic Mount](metamodule.md)。

## 增强特性 {#extended-features}

除了内核级 root 的基础能力之外，ShizuSU 还带来了一系列增强特性：

- **非 GKI / 老内核支持**：ShizuSU 恢复了对非 GKI 与 GKI 1.0 设备的支持，覆盖 4.x - 5.4 LTS 内核（3.x 为实验性支持）。架构支持情况：`arm64-v8a` 完全支持、`armeabi-v7a` 基础支持、`x86_64` 部分支持。
- **基于 Magic Mount 的模块系统**：ShizuSU 基于 SukiSU-Ultra 二改，模块挂载采用 5ec1cff 的 Magic Mount 技术，提供更稳定、更可靠的基础，Magisk 模块可直接使用。ShizuSU 不使用官方 KernelSU 的 OverlayFS metamodule 架构，全站模块系统口径统一为 Magic Mount。
- **KPM 内核模块**：完整支持 KernelPatch Module（KPM，移植自 Apatch），可在内核层面进行高级修改与增强。
- **App Profile**：通过应用配置文件把 root 权限锁进受控环境，详见 [App Profile](app-profile.md)。
- **广泛自定义**：自定义管理器背景、直接管理部分 susfs 功能（无需额外的 susfsforksu 模块）、调整 DPI 等，按你自己的方式设计。

- **多管理器支持（Multi-manager）**：一个内核通过内置签名表同时识别多个管理器（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU），支持热注册与持久化（`/data/adb/shizusu/manager`），无需为每个管理器单独刷内核。
- **隐身模式（Stealth）**：在 `/data/adb/shizusu/stealth` 写入标记即可开启；开启后信息报告不再向应用暴露管理器状态。
- **模块管理便利**：支持模块与 root 白名单的备份/恢复、批量安装（收集失败项而不中断）、一键启用/禁用/全部禁用/全部卸载管理。
- **隐藏增强**：内置 susfsd 查询通道、可选的 KPROBES hook 隐藏（默认关闭）、与 App Profile 绑定的 hosts 隐藏入口。

## 技术来源与融合 {#inherited-abilities}

ShizuSU 创立时吸收并整合了多个成熟 root 方案与管理器生态的能力，技术谱系如下：

| 能力 | 来源 |
|---|---|
| 内核级 `su` 与 root 授权管理 | KernelSU（上游项目） |
| Magic Mount 模块系统 | Magisk（经 MKSU 与 SukiSU-Ultra 继承） |
| 非 GKI / 老内核支持 | RKSU、SukiSU-Ultra |
| KPM 内核模块 | KernelPatch（APatch 实现） |
| 模块管理、susfsd 与隐藏增强 | KernelSU-Next |
| 多管理器签名表 | ReSukiSU（参考） |
| 隐身实现 | 7kimisu（参考） |
| 内核级隐藏补丁 | susfs |
| APK v2 签名验证 | genuine |

## 如何使用 {#how-to-use}

请参考: [安装](installation)

## 如何构建 {#how-to-build}

请参考: [如何构建](how-to-build)

## 讨论 {#discussion}

- Telegram: [@KernelSU](https://t.me/KernelSU)
