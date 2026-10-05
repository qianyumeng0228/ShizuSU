# ShizuSU
<img align='right' src='ShizuSU-logo.png' width='220px' alt="ShizuSU logo">

**English** | [简体中文](./zh/README.md) | [日本語](./ja/README.md) | [Türkçe](./tr/README.md) | [Русский](./ru/README.md)

A kernel-based root solution for Android devices, forked from [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) (which itself is forked from [`tiann/KernelSU`](https://github.com/tiann/KernelSU)), with stealth, multi-manager and module-convenience enhancements.

[![Latest release](https://img.shields.io/github/v/release/qianyumeng0228/ShizuSU?label=Release&logo=github)](https://github.com/qianyumeng0228/ShizuSU/releases/latest)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/qianyumeng0228/ShizuSU?logo=gnu)](/LICENSE)

## Features

1. Kernel-based `su` and root access management
2. [App Profile](https://kernelsu.org/guide/app-profile.html): Lock up the root power in a cage
3. **Multi-manager support**: one kernel recognizes multiple managers through a built-in signature table (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), plus hot registration and persistence (`/data/adb/shizusu/manager`)
4. **Stealth mode**: toggle at `/data/adb/shizusu/stealth`; when enabled, the info report no longer exposes manager status to apps
5. **Module convenience**: backup / restore of modules and allowlist, batch installation that collects failures instead of aborting, enable / disable / disable-all / uninstall-all management
6. **Hiding enhancements**: susfsd query channel, optional KPROBES hook hiding (default off), hosts hiding entry tied to App Profile
7. Support non-GKI and GKI 1.0
8. KPM Support

## Compatibility Status

- KernelSU (before v1.0.0) officially supports Android GKI 2.0 devices (kernel 5.10+).

- Older kernels (4.4+) are also compatible, but the kernel will have to be built manually.

- With more backports, KernelSU can supports 3.x kernel (3.4-3.18).

- Currently, only `arm64-v8a`, `armeabi-v7a (bare)` and `X86_64`(some) are supported.

## Installation

See [`guide/installation.md`](guide/installation.md)

## Integration

See [`guide/how-to-integrate.md`](guide/how-to-integrate.md)

## Translation

The bundled `zh` / `ja` / `tr` / `ru` translations are inherited from the upstream project and may lag behind. If you would like to contribute translations, feel free to open a pull request.

## KPM Support

- Based on KernelPatch, we removed features redundant with KSU and retained only KPM support.
- Work in Progress: Expanding APatch compatibility by integrating additional functions to ensure compatibility across different implementations.

**Open-source repository**: [https://github.com/ShirkNeko/SukiSU_KernelPatch_patch](https://github.com/ShirkNeko/SukiSU_KernelPatch_patch)

**KPM template**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. Requires `CONFIG_KPM=y`
> 2. Non-GKI devices requires `CONFIG_KALLSYMS=y` and `CONFIG_KALLSYMS_ALL=y`
> 3. For kernels below `4.19`, backporting from `set_memory.h` from `4.19` is required.

## Troubleshooting

1. Device stuck upon manager app uninstallation?
   Uninstall _com.sony.playmemories.mobile_

## License

- The file in the “kernel” directory is under [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html) license.
- Except for the files or directories mentioned above, all other parts are under [GPL-3.0 or later](https://www.gnu.org/licenses/gpl-3.0.html) license.

## Credit

- [KernelSU](https://github.com/tiann/KernelSU): upstream
- [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra): base project
- [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next): reference for module management, susfsd and hiding enhancements
- ReSukiSU: reference for the multi-manager signature table
- 7kimisu: reference for the stealth implementation
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount
- [RKSU](https://github.com/rsuntk/KernelsU): support non-GKI
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): An addon root hiding kernel patches and userspace module for KernelSU.
- [KernelPatch](https://github.com/bmax121/KernelPatch): KernelPatch is a key part of the APatch implementation of the kernel module

<details>
<summary>KernelSU's credit</summary>

- [Kernel-Assisted Superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/): The KernelSU idea.
- [Magisk](https://github.com/topjohnwu/Magisk): The powerful root tool.
- [genuine](https://github.com/brevent/genuine/): APK v2 signature validation.
- [Diamorphine](https://github.com/m0nad/Diamorphine): Some rootkit skills.
</details>