# Module System: Magic Mount {#introduction}

ShizuSU is a second-generation development based on **SukiSU-Ultra**, and its module system directly inherits SukiSU-Ultra's **Magic Mount** implementation (derived from 5ec1cff's Magisk implementation).

**Magic Mount is the only module mounting mechanism in ShizuSU** — ShizuSU does not use the official KernelSU OverlayFS metamodule architecture, so there is no coexistence of "two module systems". After installing ShizuSU, modules that modify `/system` files work out of the box — no metamodule installation is required.

## The KernelSU Framework and the Magic Mount Module System {#two-subsystems}

ShizuSU consists of two **parallel** core subsystems with distinct, non-overlapping responsibilities:

| Subsystem | Responsibility | Description |
|---|---|---|
| **KernelSU kernel framework** | Root authorization and management | Runs in kernel space: `su` authorization, whitelist access control, restricted root privileges (uid / gid / groups / capabilities / SELinux), kernel-level interfaces |
| **Magic Mount module system** | Module installation and systemless mounting | Overlays the module's `system` directory onto the system partition via **bind mount**, achieving systemless modification |

The division of labor can be summarized as:

- **The KernelSU framework answers "who gets root"**: the granting, isolation, and auditing of `su` all happen in kernel space and cannot be bypassed from userspace.
- **Magic Mount answers "how modules modify the system"**: the mounting, overlaying, merging, and hiding of module files are handled by Magic Mount without touching physical partitions.

They are in a **parallel relationship**, not a containment one: the KernelSU framework provides root capability, Magic Mount provides module mounting capability, and together they form the complete ShizuSU root experience.

## Why Magic Mount? {#why-magic-mount}

SukiSU-Ultra (and ShizuSU, which inherits it) chose Magic Mount over the official KernelSU's OverlayFS metamodule architecture because:

- **More stable foundation**: Magic Mount originates from Magisk's mature implementation (5ec1cff), long-validated across a large device and module ecosystem.
- **Better module compatibility**: modules in the Magisk ecosystem that rely on `system` directory mounting can be used **directly** — no metamodule, no conversion.
- **Smaller detection surface**: mounting is done via bind mount; ShizuSU itself does not depend on OverlayFS characteristics, making it harder for apps to detect.
- **Simpler deployment**: no need to install metamodules like meta-overlayfs; modules work right after installing ShizuSU.

## How Magic Mount Works {#how-it-works}

Magic Mount uses **bind mount** to "overlay" module content onto system directories:

1. Modules are placed in `/data/adb/modules/<module-id>/`, where the `system/` directory corresponds to the system partition.
2. At boot, ShizuSU iterates over all enabled modules and bind mounts each module's `system/` directory to the corresponding path under `/system`.
3. **Same-name files**: module files override system files.
4. **Same-name directories**: module directories merge with system directories (module files are stacked on top).
5. **Deleting system files**: by mounting the corresponding path from the module directory as an empty directory (whiteout), the system file is "hidden".
6. **Replacing system directories**: by mounting the corresponding path as an empty directory, the whole directory is replaced.

The entire process only reads module and system directories and **does not modify physical partitions** — this is what systemless means.

## Differences from Official KernelSU {#difference}

| | Official KernelSU | ShizuSU (based on SukiSU-Ultra) |
|---|---|---|
| Module mounting mechanism | OverlayFS (requires metamodule, e.g. meta-overlayfs) | **Magic Mount** (built-in, no metamodule needed) |
| Modules modifying `/system` | Require installing a metamodule first | Work directly |
| Magisk module compatibility | Partial (depends on metamodule) | Directly compatible |

::: info Migration notes
If you previously used official KernelSU and installed a metamodule (e.g. meta-overlayfs), you no longer need it after migrating to ShizuSU: just install or use ordinary modules directly.
:::

## Module Development {#module-dev}

ShizuSU's module structure is fully identical to Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, etc.), so Magisk module developers can get started directly. See the [Module Development Guide](module.md) for details.
