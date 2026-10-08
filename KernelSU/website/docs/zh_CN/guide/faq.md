# 常见问题

## ShizuSU 是否支持我的设备？

首先，您的设备应该能够解锁 bootloader。 如果不能，则不支持。

然后在你的设备上安装 ShizuSU 管理器并打开它，如果它显示 `不支持` ，那么你的设备没有官方支持的开箱即用的 boot image；但你可以自己编译内核集成 ShizuSU 进而使用它。

## ShizuSU 是否需要解锁 Bootloader？

当然需要。

## ShizuSU 是否支持模块？

支持。ShizuSU 的模块系统基于 Magic Mount（来自 SukiSU-Ultra），Magisk 模块可以直接使用，修改 `/system` 文件的模块也无需 metamodule。请查阅 [模块](module.md)。

## ShizuSU 是否支持 Xposed？

支持。LSPosed 可以在 [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) 的支持下正常运行。

## ShizuSU 支持 Zygisk 吗？

ShizuSU 本体不支持 Zygisk，但是你可以用 [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) 来使用 Zygisk 模块。

## ShizuSU 与 Magisk 兼容吗？

ShizuSU 的模块系统与 Magisk 的 magic mount 有冲突，如果 ShizuSU 中启用了任何模块，那么整个 Magisk 将无法工作。

但是如果你只使用 ShizuSU 的 `su`，那么它会和 Magisk 一起工作：ShizuSU 修改 `kernel` 、 Magisk 修改 `ramdisk`，它们可以一起工作。

## ShizuSU 会替代 Magisk 吗？

我们不这么认为，这也不是我们的目标。Magisk 对于用户空间 root 解决方案来说已经足够好了，它会存活很久。ShizuSU 的目标是为用户提供内核接口，而不是替代 Magisk。

## ShizuSU 可以支持非 GKI 设备吗？

可以。但是你应该下载内核源代码并将 ShizuSU 集成到源代码树中并自己编译内核。

## ShizuSU 支持 Android 12 以下的设备吗？

影响 ShizuSU 兼容性的是设备内核的版本，它与设备的 Android 版本没有直接的关系。唯一有关联的是：**出厂** Android 12 的设备，一定是 5.10 或更高的内核（GKI 设备）；因此结论如下：

1. 出厂 Android 12 的设备必定是支持的（GKI 设备）
2. 旧版本内核的设备（即使是 Android 12，也可能是旧内核）是兼容的（你需要自己编译内核）

## ShizuSU 可以支持旧内核吗？

可以，目前最低支持到 4.14；更低的版本你需要手动移植它，欢迎 PR ！

## 如何为旧内核集成 ShizuSU？

参考[教程](how-to-integrate-for-non-gki)

## 为什么我手机系统是 Android 13，但内核版本却是 "android12-5.10"？

内核版本与 Android 版本无关，如果你需要刷入 ShizuSU，请永远使用**内核版本**而非 Android 版本，如果你为 "android12-5.10" 的设备刷入 Android 13 的内核，等待你的将是 bootloop.

## 我是 GKI1.0, 能用 ShizuSU 吗？

GKI1 跟 GKI2 完全是两个东西，所以你需要自行编译内核。

## 如何把 `/system` 变成挂载为可读写？

我们不建议你直接修改系统分区，你应该使用[模块功能](module.md) 来做修改；如果你执意要这么做，可以看看 [magisk_overlayfs](https://github.com/HuskyDG/magic_overlayfs)

## ShizuSU 能修改 hosts 吗，我如何使用 AdAway？

当然可以。但这个功能 ShizuSU 没有内置，你可以安装这个 [systemless-hosts](https://github.com/symbuzzer/systemless-hosts-KernelSU-module)

## 为什么全新安装后模块不工作？

请检查：模块是否已启用、模块目录是否存在有效的 `module.prop`、模块是否与您的设备/内核兼容。ShizuSU 的模块系统基于 Magic Mount，修改 `/system` 文件的模块无需 metamodule。

**解决方案**：参阅[模块开发指南](module.md) 与[模块系统：Magic Mount](metamodule.md)。

## ShizuSU 的模块系统是什么？

ShizuSU 基于 SukiSU-Ultra 二次开发，模块系统采用 **Magic Mount**（bind mount 将模块的 `system` 目录叠加到 `/system`），无需 metamodule，Magisk 模块直接兼容。官方 KernelSU 使用的 OverlayFS metamodule 架构不适用于 ShizuSU。详见[模块系统：Magic Mount](metamodule.md)。
