# ShizuSU 构建备忘（Phase 0 经验）

面向后续改码 / 内核集成者的一份实战备忘，记录 Phase 0 基线验证（commit `7fbbb1f1`）期间踩过的坑与可用做法。

---

## 1. 关键教训：Windows checkout 会摊平 `kernel/include/uapi` 符号链接

SukiSU-Ultra 仓库里 `kernel/include/uapi` 是一个 **git 符号链接**，指向仓库根目录的 `uapi/`。
Windows 上直接用 git 克隆该仓库时，符号链接会被摊平成一个**普通文本文件**（内容是目标路径字符串），而不是目录。
后果：做 GKI 内核集成后编译失败，报错类似 `uapi/app_profile.h: Not found` / `No such file or directory`。

**修复（在 Linux 构建树里）：**

```bash
# 1) 把仓库根的 uapi/ 实体目录放到驱动期望的位置
cp -a uapi/ drivers/uapi/
# 2) 重建符号链接，指回上一级的实体 uapi
ln -s ../../uapi drivers/kernelsu/include/uapi
```

**在 Windows 上操作该仓库时的注意事项：**
- 用 **WSL 里的 git** 克隆 / 提交，或开启 Windows 的开发者模式 + `git config --global core.symlinks true`；
- 一旦符号链接已被摊平成文本文件，先 `git checkout -- kernel/include/uapi`（或对应路径）还原成链接，再继续；
- 不要在 Windows 原生工作区里直接做 GKI 内核编译——路径 / 行尾 / 符号链接三处都会出问题。

---

## 2. 官方内核集成动作（三行）

把 KernelSU 集成进一个 GKI 内核树时，标准动作是：

```make
# 1. 复制驱动源码到内核树
cp -a <this-repo>/kernel/ <kernel-tree>/drivers/kernelsu/

# 2. drivers/Makefile 注入
obj-$(CONFIG_KSU) += kernelsu/

# 3. drivers/Kconfig 注入
source "drivers/kernelsu/Kconfig"
```

之后在内核配置里打开 `CONFIG_KSU=m`（模块）或 `=y`（内建），按目标 KMI 正常构建即可。

---

## 3. 15G 内存下的 LTO 备忘

GKI 默认可能开启 full-LTO，在约 15G 内存的机器 / runner 上链接阶段容易 **OOM**。
验证 / 基线构建时建议关掉或降级 LTO：

```bash
# 在 .config 里（或 scripts/config）
scripts/config -d LTO_CLANG -e LTO_NONE
# 若仍想保留 LTO 收益，可改用 thin：
# scripts/config -d LTO_CLANG -d LTO_FULL -e LTO_CLANG_THIN -e THINLTO
make olddefconfig
```

---

## 4. 构建源与网络备注

- 内核树（GKI / AOSP 通用树）建议直接拉 **tarball** 而非整仓 clone，省时省流量：
  `aosp-mirror/kernel_common` 对应分支，经 GitHub codeload 下载 zip/tar.gz。
- 本机 git 直连 github.com 在大包传输时易被 RST（`RPC failed / Connection reset`）；
  小改动可用 GitHub REST API（`contents` PUT）推送，或在 git 命令上显式覆盖代理。
- Phase 0 基线 CI：见 `.github/workflows/phase0-build-matrix.yml`，GKI LKM 矩阵（5.10 / 5.15 / 6.1）在
  `ghcr.io/ylarod/ddk-min` 容器内以 `CONFIG_KSU=m CC=clang make` 验证。
