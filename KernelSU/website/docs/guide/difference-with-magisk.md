# Difference with Magisk

Although ShizuSU and Magisk modules have many similarities, there are inevitably some differences due to their completely different implementation mechanisms. If you want your module to work on both Magisk and ShizuSU, it's essential to understand these differences.

## Similarities

- Module file format: Both use the ZIP format to organize modules, and the module format is practically the same.
- Module installation directory: Both are located at `/data/adb/modules`.
- Systemless: Both support modifying `/system` in a systemless way through modules.
- post-fs-data.sh: Execution time and semantics are exactly the same.
- service.sh: Execution time and semantics are exactly the same.
- system.prop: Completely the same.
- sepolicy.rule: Completely the same.
- BusyBox: Scripts are run in BusyBox with "Standalone Mode" enabled in both cases.

## Differences

Before understanding the differences, it's important to know how to identify whether your module is running in ShizuSU or Magisk. You can use the environment variable `KSU` to differentiate it in all places where you can run module scripts (`customize.sh`, `post-fs-data.sh`, `service.sh`). In ShizuSU, this environment variable will be set to `true`.

Here are some differences:

- ShizuSU modules cannot be installed in Recovery mode.
- ShizuSU modules don't have built-in support for Zygisk, but you can use Zygisk modules through [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext).
- **Module mounting architecture**: ShizuSU, based on SukiSU-Ultra, uses **Magic Mount (bind mount)** for module mounting, just like Magisk; official KernelSU uses the OverlayFS metamodule architecture instead.
- ShizuSU modules replace or delete files in the same way as Magisk: use the `REMOVE` and `REPLACE` variables in `customize.sh` to delete files or replace directories (see the [Module Guide](module.md)).
- The directories for BusyBox are different. The built-in BusyBox in ShizuSU is located at `/data/adb/ksu/bin/busybox`, while in Magisk it is at `/data/adb/magisk/busybox`. **Note that this is an internal behavior of ShizuSU and may change in the future!**
- ShizuSU doesn't support `.replace` files, but it supports the `REMOVE` and `REPLACE` variables to remove or replace files and folders.
- ShizuSU adds the `boot-completed` stage to run scripts after the boot process is finished.
- ShizuSU adds the `post-mount` stage to run scripts after module mounting is complete.
