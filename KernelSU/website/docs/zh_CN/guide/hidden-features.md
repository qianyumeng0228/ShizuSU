# 隐藏功能

## ksurc

默认情况下，`/system/bin/sh` 会加载 `/system/etc/mkshrc`。

可以通过创建 `/data/adb/ksu/.ksurc` 文件来让 su 加载该文件而不是 `/system/etc/mkshrc`。

## 自定义选项 {#customization}

- **自定义背景**：在 ShizuSU 管理器的设置中更换背景图片，打造个性化界面。
- **susfs 管理**：直接在管理器内管理部分 susfs 功能，无需额外安装 susfsforksu 模块。
- **DPI 调整**：调整管理器的 DPI 显示，适配不同的屏幕。

## WebUI X {#webui-x}

支持由 MMRL 提供的新一代 WebUI 实现（WebUI X），带来更丰富的模块交互体验。

## 多管理器支持 {#multi-manager}

ShizuSU 内置一张管理器签名表，让一个内核可以同时识别多个 root 管理器：RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU。除内置签名外，还支持热注册与持久化（`/data/adb/shizusu/manager`），安装新管理器后无需重新刷内核。

## 隐身模式 {#stealth}

在 `/data/adb/shizusu/stealth` 写入标记即可开启隐身模式。开启后，ShizuSU 的信息报告不再向应用暴露当前管理器的状态，降低被检测的风险。

## 隐藏增强 {#hiding-enhancements}

- **susfsd 查询通道**：内置 susfsd 通信通道，可直接与 susfs 内核补丁协同。
- **KPROBES hook 隐藏**：可选的 KPROBES 挂钩隐藏，默认关闭。
- **hosts 隐藏入口**：与 App Profile 绑定，可隐藏模块对 hosts 文件的修改。
