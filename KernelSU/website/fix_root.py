# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

pairs = [
# ============ root (en) ============
("index.md",
"""  - title: Metamodule system
    details: Pluggable module infrastructure allows systemless /system modifications. Install a metamodule like meta-overlayfs to enable module mounting.""",
"""  - title: Magic Mount module system
    details: Built on SukiSU-Ultra's Magic Mount (5ec1cff), module mounting works out of the box and Magisk modules are directly compatible."""),
("index.md",
"""  - title: Magic Mount based module system
    details: Built on 5ec1cff's Magic Mount technology for a more stable and reliable module mounting foundation.""",
"""  - title: Based on SukiSU-Ultra
    details: A second-generation fork of a mature community project, inheriting Non-GKI support, Magic Mount, KPM and more."""),
("guide/what-is-kernelsu.md",
"Additionally, ShizuSU provides a [metamodule system](metamodule.md), which is a pluggable architecture for module management. Unlike traditional root solutions that bake mounting logic into their core, ShizuSU delegates this to metamodules. This allows you to install metamodules like [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs) to provide systemless modifications to the `/system` partition and other partitions.",
"Additionally, ShizuSU is a second-generation fork of **SukiSU-Ultra**, and its module system is built on **Magic Mount** (from 5ec1cff's Magisk implementation): the module's `system` directory is overlaid onto `/system` via bind mount in a systemless way, **with no metamodule required**. See [Module System: Magic Mount](metamodule.md)."),
("guide/what-is-kernelsu.md",
"- **Magic Mount based module system**: Module mounting is built on 5ec1cff's Magic Mount technology for a more stable and reliable foundation, while keeping the pluggable [metamodule](metamodule.md) architecture.",
"- **Magic Mount based module system**: ShizuSU is a second-generation fork of SukiSU-Ultra; module mounting is built on 5ec1cff's Magic Mount technology for a more stable and reliable foundation, and Magisk modules work directly. ShizuSU does not use official KernelSU's OverlayFS metamodule architecture — the site-wide module system description is unified as Magic Mount."),
("guide/module.md",
"""ShizuSU provides a module mechanism that achieves the effect of modifying the system directory while maintaining the integrity of the system partition. This mechanism is commonly known as "systemless".

The module mechanism of ShizuSU is almost the same as that of Magisk. If you're familiar with Magisk module development, developing ShizuSU modules is very similar. You can skip the introduction of modules below and just read [Difference with Magisk](difference-with-magisk.md).

::: warning METAMODULE ONLY NEEDED FOR SYSTEM FILE MODIFICATION
ShizuSU uses a [metamodule](metamodule.md) architecture for mounting the `system` directory. **Only if your module needs to modify `/system` files** (via the `system` directory) do you need to install a metamodule (such as [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)). Other module features like scripts, sepolicy rules, and system.prop work without a metamodule.
:::""",
"""ShizuSU provides a module mechanism that achieves the effect of modifying the system directory while maintaining the integrity of the system partition. This mechanism is commonly known as "systemless". ShizuSU's module system is based on **Magic Mount** (from SukiSU-Ultra) — no metamodule required, modules work out of the box.

The module mechanism of ShizuSU is almost the same as that of Magisk. If you're familiar with Magisk module development, developing ShizuSU modules is very similar. You can skip the introduction of modules below and just read [Difference with Magisk](difference-with-magisk.md).

::: info MODULE SYSTEM BASED ON MAGIC MOUNT
ShizuSU's module system is based on **Magic Mount** (from SukiSU-Ultra). No metamodule is required to mount the `system` directory — modules that modify `/system` files work out of the box. See [Module System: Magic Mount](metamodule.md).
:::"""),
("guide/module.md",
"If you want to do something after mounting OverlayFS, please use `post-mount.sh`.",
"If you want to do something after module mounting, please use `post-mount.sh`."),
("guide/module.md",
"""### `system` directory

The contents of this directory will be overlaid on top of the system's `/system` partition after the system is booted. This means that:

::: tip METAMODULE REQUIREMENT
The `system` directory is only mounted if you have a metamodule installed that provides mounting functionality (such as `meta-overlayfs`). The metamodule handles how modules are mounted. See the [Metamodule Guide](metamodule.md) for more information.
:::

1. Files with the same name as those in the corresponding directory in the system will be overwritten by the files in this directory.
2. Folders with the same name as those in the corresponding directory in the system will be merged with the folders in this directory.

If you want to delete a file or folder in the original system directory, you need to create a file with the same name as the file/folder in the module directory using `mknod filename c 0 0`. This way, the OverlayFS system will automatically "whiteout" this file as if it has been deleted (the /system partition isn't actually changed).

You can also declare a variable named `REMOVE` containing a list of directories in `customize.sh` to execute removal operations, and ShizuSU will automatically execute `mknod <TARGET> c 0 0` in the corresponding directories of the module. For example:

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

The above list will execute `mknod $MODPATH/system/app/YouTube c 0 0` and `mknod $MODPATH/system/app/Bloatware c 0 0`, `/system/app/YouTube` and `/system/app/Bloatware` will be removed after the module takes effect.

If you want to replace a directory in the system, you need to create a directory with the same path in your module directory, and then set the attribute `setfattr -n trusted.overlay.opaque -v y <TARGET>` for this directory. This way, the OverlayFS system will automatically replace the corresponding directory in the system (without changing the /system partition).

You can declare a variable named `REPLACE` in your `customize.sh` file, which includes a list of directories to be replaced, and ShizuSU will automatically perform the corresponding operations in your module directory. For example:

```sh
REPLACE="
/system/app/YouTube
/system/app/Bloatware
"
```

This list will automatically create the directories `$MODPATH/system/app/YouTube` and `$MODPATH/system/app/Bloatware`, and then execute `setfattr -n trusted.overlay.opaque -v y $MODPATH/system/app/YouTube` and `setfattr -n trusted.overlay.opaque -v y $MODPATH/system/app/Bloatware`. After the module takes effect, `/system/app/YouTube` and `/system/app/Bloatware` will be replaced with empty directories.

::: tip DIFFERENCE WITH MAGISK
ShizuSU uses a [metamodule architecture](metamodule.md) where mounting is delegated to pluggable metamodules. The official `meta-overlayfs` metamodule uses the kernel's OverlayFS for systemless modifications, while Magisk uses magic mount (bind mount) built directly into its core. Both achieve the same goal: modifying `/system` files without physically modifying the `/system` partition. ShizuSU's approach provides more flexibility and reduces detection surface.
:::

If you're interested in OverlayFS, it's recommended to read the Linux Kernel's [documentation on OverlayFS](https://docs.kernel.org/filesystems/overlayfs.html). For details on ShizuSU's metamodule system, see the [Metamodule Guide](metamodule.md).""",
"""### `system` directory

The contents of this directory will be overlaid on top of the system's `/system` partition via **Magic Mount (bind mount)** after the system is booted. This means that:

1. Files with the same name as those in the corresponding directory in the system will be overwritten by the files in this directory.
2. Folders with the same name as those in the corresponding directory in the system will be merged with the folders in this directory.

If you want to delete a file or folder in the original system directory, you can declare a variable named `REMOVE` containing a list of directories in `customize.sh`. ShizuSU will hide these files via Magic Mount (the /system partition isn't actually changed). For example:

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` and `/system/app/Bloatware` will be hidden after the module takes effect.

If you want to replace a directory in the system, you can declare a variable named `REPLACE` in your `customize.sh` file, which includes a list of directories to be replaced. ShizuSU will bind-mount an empty directory over the target path, replacing it entirely. For example:

```sh
REPLACE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` and `/system/app/Bloatware` will be replaced with empty directories after the module takes effect.

::: tip DIFFERENCE WITH OFFICIAL KernelSU
Official KernelSU uses an OverlayFS (metamodule) mechanism for systemless modifications, while ShizuSU, based on SukiSU-Ultra, uses **Magic Mount (bind mount)** just like Magisk. Both approaches share the same goal: modifying `/system` files without physically modifying the `/system` partition. ShizuSU uses Magic Mount only — there are no two module systems. See [Module System: Magic Mount](metamodule.md).
:::"""),
("guide/module.md",
"  - `post-fs-data.sh` runs in post-fs-data mode, `service.sh` runs in late_start service mode, `boot-completed.sh` runs on boot completed, `post-mount.sh` runs on OverlayFS mounted.",
"  - `post-fs-data.sh` runs in post-fs-data mode, `service.sh` runs in late_start service mode, `boot-completed.sh` runs on boot completed, `post-mount.sh` runs on module mounting complete."),
("guide/module.md",
"  *execute metamodule's post-fs-data.sh (if exists)",
"  *execute module-mounting post-fs-data script"),
("guide/module.md",
"  *execute metamodule's metamount.sh (mounts all modules)",
"  *execute Magic Mount metamount.sh (mounts all modules)"),
("guide/module.md",
"  *execute metamodule's post-mount.sh (if exists)",
"  *execute module-mounting post-mount script"),
("guide/module.md",
"*execute metamodule's service.sh (if exists)",
"*execute module service script"),
("guide/module.md",
"*execute metamodule's boot-completed.sh (if exists)",
"*execute module boot-completed script"),
("guide/module.md",
"| OverlayFS mount (metamodule) | Yes | Yes |",
"| Magic Mount module mounting | Yes | Yes |"),
("guide/module.md",
"  5. Execute metamodule mount script (OverlayFS)",
"  5. Execute Magic Mount module mounting"),
("guide/module.md",
"This script runs before OverlayFS mounting, similar to `post-fs-data.sh` in the standard flow.",
"This script runs before module mounting, similar to `post-fs-data.sh` in the standard flow."),
("guide/installation.md",
"""## Post-Installation: Module Support

::: warning METAMODULE FOR SYSTEM FILE MODIFICATION
If you want to use modules that modify `/system` files, you need to install a **metamodule** after installing ShizuSU. Modules that only use scripts, sepolicy, or system.prop work without a metamodule.
:::

**For `/system` modification support**, please see the [Metamodule Guide](metamodule.md) to:
- Understand what metamodules are and why they're needed
- Install the official `meta-overlayfs` metamodule
- Learn about other metamodule options""",
"""## Post-Installation: Module Support

::: info MODULE SYSTEM BASED ON MAGIC MOUNT
ShizuSU's module system is based on **Magic Mount** (from SukiSU-Ultra). Modules work right after installing ShizuSU — modules that modify `/system` files need no extra metamodule.
:::

For how the module system works and module development, see [Module System: Magic Mount](metamodule.md) and the [Module Guide](module.md)."""),
("guide/faq.md",
"Yes, most Magisk modules work on ShizuSU. However, if your module needs to modify `/system` files, you need to install a [metamodule](metamodule.md) (such as `meta-overlayfs`). Other module features work without a metamodule. Check [Module guide](module.md) for more info.",
"Yes. ShizuSU's module system is based on Magic Mount (from SukiSU-Ultra); Magisk modules work directly, and modules that modify `/system` files need no metamodule. Check [Module guide](module.md) for more info."),
("guide/faq.md",
"""If your modules need to modify `/system` files, you need to install a [metamodule](metamodule.md) to mount the `system` directory. Other module features (scripts, sepolicy, system.prop) work without a metamodule.

**Solution**: See the [Metamodule Guide](metamodule.md) for installation instructions.""",
"""Please check: is the module enabled, does its directory contain a valid `module.prop`, and is the module compatible with your device/kernel? ShizuSU's module system is based on Magic Mount; modules that modify `/system` files need no metamodule.

**Solution**: See the [Module Guide](module.md) and [Module System: Magic Mount](metamodule.md)."""),
("guide/faq.md",
"""## What is a metamodule and why do I need one?

A metamodule is a special module that provides infrastructure for mounting regular modules. See the [Metamodule Guide](metamodule.md) for a complete explanation.""",
"""## What is ShizuSU's module system?

ShizuSU is a second-generation fork of SukiSU-Ultra; its module system uses **Magic Mount** (bind-mounting the module's `system` directory onto `/system`), with no metamodule required and direct Magisk-module compatibility. The official KernelSU OverlayFS metamodule architecture does not apply to ShizuSU. See [Module System: Magic Mount](metamodule.md)."""),
("guide/app-profile.md",
"ShizuSU provides a systemless mechanism to modify system partitions, achieved through the mounting of OverlayFS. However, some apps may be sensitive to this behavior. In this case, we can unload modules mounted in these apps by setting the \"Umount modules\" option.",
"ShizuSU provides a systemless mechanism to modify system partitions, achieved through Magic Mount (bind mount). However, some apps may be sensitive to this behavior. In this case, we can unload modules mounted in these apps by setting the \"Umount modules\" option."),
("guide/difference-with-magisk.md",
"- **Module mounting architecture**: ShizuSU uses a [metamodule system](metamodule.md) where mounting is delegated to pluggable metamodules (e.g., `meta-overlayfs`), while Magisk has mounting built into its core. ShizuSU requires installing a metamodule to enable module mounting.",
"- **Module mounting architecture**: ShizuSU, based on SukiSU-Ultra, uses **Magic Mount (bind mount)** for module mounting, just like Magisk; official KernelSU uses the OverlayFS metamodule architecture instead."),
("guide/difference-with-magisk.md",
"- The method for replacing or deleting files in ShizuSU modules is completely different from Magisk. ShizuSU doesn't support the `.replace` method. Instead, you need to create a same-named file with `mknod filename c 0 0` to delete the corresponding file.",
"- ShizuSU modules replace or delete files in the same way as Magisk: use the `REMOVE` and `REPLACE` variables in `customize.sh` to delete files or replace directories (see the [Module Guide](module.md))."),
]

missed = []
for rel, old, new in pairs:
    p = os.path.join(ROOT, rel)
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if old not in t:
        missed.append(rel + ' :: ' + old[:70])
        continue
    t = t.replace(old, new)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', rel)

print('MISSED:', len(missed))
for m in missed:
    print(m)
