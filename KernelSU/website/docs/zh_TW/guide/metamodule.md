# 模組系統：Magic Mount {#introduction}

ShizuSU 基於 **SukiSU-Ultra** 二次開發，其模組系統直接繼承了 SukiSU-Ultra 的 **Magic Mount** 方案（源自 5ec1cff 的 Magisk 實作）。

**Magic Mount 是 ShizuSU 唯一的模組掛載機制**——ShizuSU 不使用官方 KernelSU 的 OverlayFS metamodule 架構，因此不存在「兩種模組系統」的並存。安裝 ShizuSU 後，修改 `/system` 檔案的模組開箱即用，無需安裝任何 metamodule。

## KernelSU 框架與 Magic Mount 模組系統 {#two-subsystems}

ShizuSU 由兩個**並列**的核心子系統組成，它們職責分明、互不重疊：

| 子系統 | 職責 | 說明 |
|---|---|---|
| **KernelSU 核心框架** | root 授權與管理 | 運行在核心空間：`su` 授權、白名單存取控制、受限 root 權限（uid / gid / groups / capabilities / SELinux）、核心級介面 |
| **Magic Mount 模組系統** | 模組安裝與 systemless 掛載 | 把模組的 `system` 目錄以 **bind mount** 方式疊加到系統分割區，實現無系統修改 |

兩者的分工可以概括為：

- **KernelSU 框架回答「誰可以拿到 root」**：`su` 的授予、隔離與審計全部發生在核心空間，使用者空間無法繞過。
- **Magic Mount 回答「模組如何修改系統」**：模組檔案的掛載、覆蓋、合併與隱藏由 Magic Mount 完成，不觸碰實體分割區。

它們是**並列關係**而非包含關係：KernelSU 框架負責 root 能力，Magic Mount 負責模組掛載能力，二者共同構成 ShizuSU 的完整 root 體驗。

## 為什麼採用 Magic Mount？ {#why-magic-mount}

SukiSU-Ultra（以及繼承它的 ShizuSU）選擇 Magic Mount 而非官方 KernelSU 的 OverlayFS metamodule 架構，原因如下：

- **更穩定的基礎**：Magic Mount 源自 Magisk 的成熟實作（5ec1cff），經過大量裝置與模組生態的長期驗證。
- **更好的模組相容性**：Magisk 生態中依賴 `system` 目錄掛載的模組可以**直接使用**，無需 metamodule、無需轉換。
- **更少的偵測面**：掛載邏輯以 bind mount 完成，ShizuSU 本體不依賴 OverlayFS 特徵，更難被應用程式偵測。
- **更簡單的部署**：無需額外安裝 meta-overlayfs 等 metamodule，安裝 ShizuSU 後模組即可正常運作。

## Magic Mount 工作原理 {#how-it-works}

Magic Mount 使用 **bind mount**（綁定掛載）把模組內容「疊加」到系統目錄上：

1. 模組放置在 `/data/adb/modules/<模組ID>/`，其中的 `system/` 目錄對應系統分割區。
2. 啟動時，ShizuSU 遍歷所有已啟用模組，把每個模組的 `system/` 目錄 bind mount 到 `/system` 的對應路徑。
3. **同名檔案**：模組檔案覆蓋系統檔案。
4. **同名目錄**：模組目錄與系統目錄合併（模組檔案疊加在上層）。
5. **刪除系統檔案**：透過把模組目錄中對應路徑掛載為一個空目錄（whiteout），從而「隱藏」系統檔案。
6. **替換系統目錄**：透過把模組目錄中對應路徑掛載為一個空目錄，實現整目錄替換。

整個過程只讀取模組目錄與系統目錄，**不修改實體分割區**——這正是 systemless（無系統修改）的含義。

## 與官方 KernelSU 的差異 {#difference}

| | 官方 KernelSU | ShizuSU（基於 SukiSU-Ultra） |
|---|---|---|
| 模組掛載機制 | OverlayFS（需安裝 metamodule，如 meta-overlayfs） | **Magic Mount**（內建，無需 metamodule） |
| 修改 `/system` 的模組 | 需要先安裝 metamodule | 直接可用 |
| Magisk 模組相容性 | 部分相容（依賴 metamodule） | 直接相容 |

::: info 遷移說明
如果你之前使用官方 KernelSU 並安裝了 metamodule（如 meta-overlayfs），遷移到 ShizuSU 後不再需要它：直接安裝或使用一般模組即可。
:::

## 模組開發 {#module-dev}

ShizuSU 的模組結構與 Magisk 完全一致（`module.prop`、`system/`、`post-fs-data.sh`、`service.sh` 等），Magisk 模組開發者可以直接上手。詳見[模組開發指南](module.md)。
