# What is ShizuSU?

ShizuSU is a root solution for Android GKI devices. It works in kernel mode and grants root permission to userspace apps directly in kernel space.

## Features

The main feature of ShizuSU is that it's **kernel-based**. ShizuSU works in kernel mode, enabling it to provide a kernel interface that we never had before. For example, it's possible to add hardware breakpoints to any process in kernel mode, access the physical memory of any process invisibly, intercept any system call (syscall) within the kernel space, among other functionalities.

Additionally, ShizuSU is a second-generation fork of **SukiSU-Ultra**, and its module system is built on **Magic Mount** (from 5ec1cff's Magisk implementation): the module's `system` directory is overlaid onto `/system` via bind mount in a systemless way, **with no metamodule required**. See [Module System: Magic Mount](metamodule.md).

## Extended Features

Beyond the kernel-level root foundation, ShizuSU brings a set of extended features:

- **Non-GKI / legacy kernel support**: ShizuSU restores support for non-GKI and GKI 1.0 devices, covering 4.x - 5.4 LTS kernels (3.x is experimental). Architecture support: `arm64-v8a` full support, `armeabi-v7a` basic support, `x86_64` partial support.
- **Magic Mount based module system**: ShizuSU is a second-generation fork of SukiSU-Ultra; module mounting is built on 5ec1cff's Magic Mount technology for a more stable and reliable foundation, and Magisk modules work directly. ShizuSU does not use official KernelSU's OverlayFS metamodule architecture — the site-wide module system description is unified as Magic Mount.
- **KPM kernel modules**: Full KernelPatch Module (KPM) support (ported from Apatch) for advanced kernel modifications and enhancements.
- **App Profile**: Lock root privileges in a controlled environment via application profiles; see [App Profile](app-profile.md).
- **Extensive customization**: Custom manager background, manage susfs features directly (no extra susfsforksu module needed), adjust DPI, and design it in your own way.

- **Multi-manager support**: One kernel recognizes multiple managers through a built-in signature table (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), with hot registration and persistence (`/data/adb/shizusu/manager`) — no need to reflash the kernel for each manager.
- **Stealth mode**: Just write a flag to `/data/adb/shizusu/stealth`; when enabled, the info report no longer exposes manager status to apps.
- **Module convenience**: Backup / restore of modules and the root allowlist, batch installation that collects failures instead of aborting, and one-tap enable / disable / disable-all / uninstall-all management.
- **Hiding enhancements**: Built-in susfsd query channel, optional KPROBES hook hiding (off by default), and a hosts-hiding entry tied to App Profile.

## Inherited Abilities {#inherited-abilities}

ShizuSU was born by integrating abilities from several mature root solutions and manager ecosystems. Its technical lineage:

| Ability | Source |
|---|---|
| Kernel-level `su` and root authorization management | KernelSU (upstream) |
| Magic Mount module system | Magisk (inherited via MKSU and SukiSU-Ultra) |
| Non-GKI / legacy kernel support | RKSU, SukiSU-Ultra |
| KPM kernel modules | KernelPatch (the APatch implementation) |
| Module management, susfsd, and hiding enhancements | KernelSU-Next |
| Multi-manager signature table | ReSukiSU (reference) |
| Stealth implementation | 7kimisu (reference) |
| Kernel-level hiding patches | susfs |
| APK v2 signature validation | genuine |

## How to use ShizuSU?

See [Installation](installation.md).

## How to build ShizuSU?

See [How to build](how-to-build.md).

## Discussion

- Telegram: [@KernelSU](https://t.me/KernelSU)
