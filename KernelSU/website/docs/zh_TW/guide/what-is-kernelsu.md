# 什麼是 ShizuSU？ {#what-is-kernelsu}

ShizuSU 是 Android GKI 裝置的 Root 解決方案，它以核心模式運作，並直接在核心空間中為使用者空間應用程式授予 Root 權限。

## 功能 {#features}

ShizuSU 的主要功能是它是**基於核心的**。 ShizuSU 在核心空間中執行，所以它可以向我們提供從未有過的核心介面。例如，我們可以在核心模式中為任何處理程序新增硬體中斷點；我們可以在任何處理程序的實體記憶體中存取，而無人知曉；我們可以在核心空間攔截任何系統呼叫；等等。

此外，ShizuSU 基於 **SukiSU-Ultra** 二次開發，模組系統採用 **Magic Mount**（源自 5ec1cff 的 Magisk 實作）：模組的 `system` 目錄透過 bind mount 以 systemless 方式疊加到 `/system`，**無需安裝 metamodule**。詳見[模組系統：Magic Mount](metamodule.md)。

## 增強特性 {#extended-features}

除了核心級 root 的基礎能力之外，ShizuSU 還帶來了一系列增強特性：

- **非 GKI / 舊核心支援**：ShizuSU 恢復了對非 GKI 與 GKI 1.0 裝置的支援，涵蓋 4.x - 5.4 LTS 核心（3.x 為實驗性支援）。架構支援情況：`arm64-v8a` 完全支援、`armeabi-v7a` 基礎支援、`x86_64` 部分支援。
- **基於 Magic Mount 的模組系統**：ShizuSU 基於 SukiSU-Ultra 二改，模組掛載採用 5ec1cff 的 Magic Mount 技術，提供更穩定、更可靠的基礎，Magisk 模組可直接使用。ShizuSU 不使用官方 KernelSU 的 OverlayFS metamodule 架構，全站模組系統口徑統一為 Magic Mount。
- **KPM 核心模組**：完整支援 KernelPatch Module（KPM，移植自 Apatch），可在核心層面進行進階修改與增強。
- **App Profile**：透過應用程式設定檔把 root 權限鎖進受控環境，詳見 [App Profile](app-profile.md)。
- **廣泛的自訂選項**：自訂管理員背景、直接管理部分 susfs 功能（無需額外的 susfsforksu 模組）、調整 DPI 等，以你自己的方式設計。

- **多管理器支援（Multi-manager）**：一個核心透過內建簽名表同時識別多個管理器（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU），支援熱註冊與持久化（`/data/adb/shizusu/manager`），無需為每個管理器單獨刷核心。
- **隱身模式（Stealth）**：在 `/data/adb/shizusu/stealth` 寫入標記即可開啟；開啟後資訊報告不再向應用程式暴露管理器狀態。
- **模組管理便利**：支援模組與 root 白名單的備份/還原、批次安裝（收集失敗項而不中斷）、一鍵啟用/停用/全部停用/全部卸載管理。
- **隱藏增強**：內建 susfsd 查詢通道、可選的 KPROBES hook 隱藏（預設關閉）、與 App Profile 綁定的 hosts 隱藏入口。

## 技術來源與融合 {#inherited-abilities}

ShizuSU 創立時吸收並整合了多個成熟 root 方案與管理器生態的能力，技術譜系如下：

| 能力 | 來源 |
|---|---|
| 核心級 `su` 與 root 授權管理 | KernelSU（上游專案） |
| Magic Mount 模組系統 | Magisk（經 MKSU 與 SukiSU-Ultra 繼承） |
| 非 GKI / 老核心支援 | RKSU、SukiSU-Ultra |
| KPM 核心模組 | KernelPatch（APatch 實作） |
| 模組管理、susfsd 與隱藏增強 | KernelSU-Next |
| 多管理器簽名表 | ReSukiSU（參考） |
| 隱身實作 | 7kimisu（參考） |
| 核心級隱藏修補 | susfs |
| APK v2 簽名驗證 | genuine |

## 如何使用 {#how-to-use}

請參閱：[安裝](installation)

## 如何建置 {#how-to-build}

请參閱：[如何建置](how-to-build)

## 討論 {#discussion}

- Telegram: [@KernelSU](https://t.me/KernelSU)
