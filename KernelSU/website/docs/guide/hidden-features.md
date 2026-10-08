# Hidden features

## .ksurc

By default, `/system/bin/sh` loads `/system/etc/mkshrc`.

You can make su load customized rc file by creating a `/data/adb/ksu/.ksurc` file.

## Customization

- **Custom background**: Change the background image in the ShizuSU Manager settings to personalize the interface.
- **susfs management**: Manage some susfs features directly in the Manager, no extra susfsforksu module needed.
- **DPI adjustment**: Adjust the Manager's DPI to fit different screens.

## WebUI X

Supports the next-generation WebUI implementation (WebUI X) powered by MMRL, for richer module interaction.

## Multi-manager Support {#multi-manager}

ShizuSU ships with a built-in manager signature table, letting one kernel recognize multiple root managers at the same time: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. In addition to built-in signatures, hot registration and persistence (`/data/adb/shizusu/manager`) are supported — no kernel reflash needed when installing a new manager.

## Stealth Mode {#stealth}

Write a flag to `/data/adb/shizusu/stealth` to enable stealth mode. When enabled, ShizuSU's info report no longer exposes the current manager status to apps, reducing the risk of detection.

## Hiding Enhancements {#hiding-enhancements}

- **susfsd query channel**: built-in susfsd communication channel that works directly with the susfs kernel patches.
- **KPROBES hook hiding**: optional, off by default.
- **hosts hiding entry**: tied to App Profile; can hide module modifications to the hosts file.
