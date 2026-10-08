# ShizuSU 與 Magisk 的差異 {#difference-with-magisk}

儘管 ShizuSU 模組和 Magisk 模組之間有許多相似之處，但由於它們完全不同的實作機制，不可避免地存在一些差異；如果您想讓您的模組同時在 Magisk 和 ShizuSU 上運作，那麼您必須瞭解這些差異。

## 相同之處 {#similarities}

- 模組檔案格式：都以 Zip 的格式組織模組，並且模組的格式幾乎相同
- 模組安裝目錄：都位於 `/data/adb/modules`
- 無系統修改：都支援透過模組以無系統修改的方式來更改 `/system`
- `post-fs-data.sh`：執行階段和語義完全相同
- `service.sh`：執行階段和語義完全相同
- `system.prop`：完全相同
- `sepolicy.rule`：完全相同
- BusyBox：指令碼在 BusyBox 中以「獨立模式」執行

## 不同之處 {#differences}

在瞭解不同之處之前，您需要知道如何區分您的模組是在 ShizuSU 還是 Magisk 中執行；在所有可以執行模組指令碼的位置 (`customize.sh`, `post-fs-data.sh`, `service.sh`)，您都可以使用環境變數 `KSU` 來區分，在 ShizuSU 中，這個環境變數將被設定為 `true`。

以下是一些不同之處：

1. ShizuSU 的模組無法在 Recovery 中安裝。
2. ShizuSU 的模組沒有內建的 Zygisk 支援 (但您可以透過 [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) 來使用 Zygisk 模組)。
3. **模組掛載架構**：ShizuSU 基於 SukiSU-Ultra，與 Magisk 一樣使用 **Magic Mount（bind mount）** 實現模組掛載；官方 KernelSU 則使用 OverlayFS metamodule 架構。
4. ShizuSU 模組取代或刪除檔案的方式與 Magisk 相同：在 `customize.sh` 中使用 `REMOVE` 和 `REPLACE` 變數來刪除檔案或取代目錄（詳見[模組開發指南](module.md)）。
5. BusyBox 的目錄不同。ShizuSU 內建的 BusyBox 在 `/data/adb/ksu/bin/busybox`，而 Magisk 在 `/data/adb/magisk/busybox`。**注意此為 ShizuSU 內部行為，未來可能會變更！**
6. ShizuSU 不支援 `.replace` 檔案；但 ShizuSU 支援 `REPLACE` 和 `REMOVE` 變數以移除或取代檔案與資料夾。
7. ShizuSU 新增了 `boot-completed.sh` 腳本，以便在 Android 系統啟動完成後執行某些任務。
8. ShizuSU 新增了 `post-mount.sh` 腳本，以便在模組掛載完成後執行某些任務。
