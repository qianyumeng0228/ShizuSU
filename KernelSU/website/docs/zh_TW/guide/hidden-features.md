# 隱藏功能 {#hidden-features}

## .ksurc

預設狀況下，`/system/bin/sh` 會載入 `/system/etc/mkshrc`。

可以透過建立 `/data/adb/ksu/.ksurc` 檔案來讓 `su` 載入此檔案而非 `/system/etc/mkshrc`。

## 自訂選項 {#customization}

- **自訂背景**：在 ShizuSU 管理員的設定中更換背景圖片，打造個人化介面。
- **susfs 管理**：直接在管理員內管理部分 susfs 功能，無需額外安裝 susfsforksu 模組。
- **DPI 調整**：調整管理員的 DPI 顯示，以適應不同螢幕。

## WebUI X {#webui-x}

支援由 MMRL 提供的新一代 WebUI 實作（WebUI X），帶來更豐富的模組互動體驗。

## 多管理器支援 {#multi-manager}

ShizuSU 內建一張管理器簽名表，讓一個核心可以同時識別多個 root 管理器：RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU。除內建簽名外，還支援熱註冊與持久化（`/data/adb/shizusu/manager`），安裝新管理器後無需重新刷核心。

## 隱身模式 {#stealth}

在 `/data/adb/shizusu/stealth` 寫入標記即可開啟隱身模式。開啟後，ShizuSU 的資訊報告不再向應用程式暴露目前管理器的狀態，降低被偵測的風險。

## 隱藏增強 {#hiding-enhancements}

- **susfsd 查詢通道**：內建 susfsd 通訊通道，可直接與 susfs 核心修補協同。
- **KPROBES hook 隱藏**：可選的 KPROBES 掛鉤隱藏，預設關閉。
- **hosts 隱藏入口**：與 App Profile 綁定，可隱藏模組對 hosts 檔案的修改。
