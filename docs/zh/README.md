# ShizuSU
<img align='right' src='ShizuSU-logo.png' width='220px' alt="ShizuSU logo">


[English](../README.md) | **简体中文** | [日本語](../ja/README.md) | [Türkçe](../tr/README.md)

一个 Android 上基于内核的 root 方案，由 [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) 分叉而来（其上游为 [`tiann/KernelSU`](https://github.com/tiann/KernelSU)），并添加了 stealth、多管理器与模块便利增强。

[![最新发行](https://img.shields.io/github/v/release/qianyumeng0228/ShizuSU?label=Release&logo=github)](https://github.com/qianyumeng0228/ShizuSU/releases/latest)
[![协议: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub 协议](https://img.shields.io/github/license/qianyumeng0228/ShizuSU?logo=gnu)](/LICENSE)

## 特性

1. 基于内核的 `su` 和权限管理。
2. [App Profile](https://kernelsu.org/zh_CN/guide/app-profile.html): 把 Root 权限关进笼子里。
3. **多管理器支持**：一个内核通过内置签名表识别多个管理器（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU），并支持热注册与持久化（`/data/adb/shizusu/manager`）。
4. **隐身模式**：开关位于 `/data/adb/shizusu/stealth`；开启后信息上报不再向应用暴露管理器状态。
5. **模块便利**：模块与授权名单的备份 / 恢复、批量安装（失败收集而非中断）、enable / disable / disable-all / uninstall-all 管理。
6. **隐藏增强**：susfsd 查询通道、可选 KPROBES 钩子隐藏（默认关闭）、与 App Profile 联动的 hosts 隐藏入口。
7. 支持 non-GKI 与 GKI 1.0。
8. KPM 支持。

## 兼容状态

- KernelSU 官方支持 GKI 2.0 的设备（内核版本 5.10 以上）。

- 旧内核也是兼容的（最低 4.14+），不过需要自己编译内核。

- 通过更多的反向移植，KernelSU 可以支持 3.x 内核（3.4-3.18）。

- 目前支持架构 : `arm64-v8a`、`armeabi-v7a (bare)`、`X86_64`。

## 安装指导

查看 [`guide/installation.md`](guide/installation.md)

## 集成指导

查看 [`guide/how-to-integrate.md`](guide/how-to-integrate.md)

## 参与翻译

同捆的简体中文版翻译继承自上游项目，可能滞后。欢迎通过 Pull Request 提交翻译。

## KPM 支持

- 基于 KernelPatch 开发，移除了与 KernelSU 重复的功能。
- 正在进行（WIP）：通过集成附加功能来扩展 APatch 兼容性，以确保跨不同实现的兼容性。

**开源仓库**: [https://github.com/ShirkNeko/SukiSU_KernelPatch_patch](https://github.com/ShirkNeko/SukiSU_KernelPatch_patch)

**KPM 模板**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. 需要 `CONFIG_KPM=y`
> 2. Non-GKI 设备需要 `CONFIG_KALLSYMS=y` and `CONFIG_KALLSYMS_ALL=y`
> 3. 对于低于 `4.19` 的内核，需要从 `4.19` 的 `set_memory.h` 进行反向移植。

## 故障排除

1. 卸载管理器后系统卡住？
   卸载 _com.sony.playmemories.mobile_

## 许可证

- 目录 `kernel` 下所有文件为 [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)。
- 除上述文件及目录的其他部分均为 [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)。


## 鸣谢

- [KernelSU](https://github.com/tiann/KernelSU): 上游
- [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): 基底项目
- [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next): 模块管理、susfsd 与隐藏增强参考
- ReSukiSU: 多管理器签名表参考
- 7kimisu: stealth 实现参考
- [MKSU](https://github.com/5ec1cff/KernelSU): 魔法坐骑支持
- [RKSU](https://github.com/rsuntk/KernelsU): non-GKI 支持
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): 隐藏内核补丁以及用户空间模组的 KernelSU 附件
- [KernelPatch](https://github.com/bmax121/KernelPatch): KernelPatch 是内核模块 APatch 实现的关键部分

<details>
<summary>KernelSU 的鸣谢</summary>

- [kernel-assisted-superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/)：KernelSU 的灵感。
- [Magisk](https://github.com/topjohnwu/Magisk)：强大的 root 工具箱。
- [genuine](https://github.com/brevent/genuine/)：apk v2 签名验证。
- [Diamorphine](https://github.com/m0nad/Diamorphine)：一些 rootkit 技巧。
</details>
