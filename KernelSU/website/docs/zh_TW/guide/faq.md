# 常見問題

## ShizuSU 是否支援我的裝置？

首先，您的裝置應該能解鎖 Bootloader。如果不能，則不支援。

然後在您的裝置上安裝 ShizuSU 管理員並開啟它，如果它顯示 `不支援`，那麼您的裝置沒有官方支援的開箱即用的 Boot 映像；但您可以自行建置核心來源並整合 ShizuSU 以繼續使用。

## ShizuSU 是否需要解鎖 Bootloader？

當然需要。

## ShizuSU 是否支援模組？

支援。ShizuSU 的模組系統基於 Magic Mount（來自 SukiSU-Ultra），Magisk 模組可以直接使用，修改 `/system` 檔案的模組也無需 metamodule。請參閱 [模組指南](module.md)。

## ShizuSU 是否支援 Xposed ？

支援。[Dreamland](https://github.com/canyie/Dreamland) 和 [TaiChi](https://taichi.cool) 可以正常運作。LSPosed 可以在 [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) 的支援下正常運作。

## ShizuSU 支援 Zygisk 嗎？

ShizuSU 沒有內建 Zygisk 支援，但是您可以用 [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) 來使用 Zygisk 模組。

## ShizuSU 與 Magisk 相容嗎？

ShizuSU 的模組系統與 Magisk 的 magic mount 存在衝突，如果在 ShizuSU 中啟用了任何模組，那麼整個 Magisk 將無法正常運作。

但是如果您只使用 ShizuSU 的 `su`，那么它會和 Magisk 一同運作：ShizuSU 修改 `kernel`、Magisk 修改 `ramdisk`，它們可以搭配使用。

## ShizuSU 会取代 Magisk 嗎？

我們不這樣認為，這也不是我們的目標。Magisk 對於使用者空間 Root 解決方案來說已經足夠優秀了，它會存在很長一段時間。ShizuSU 的目標是為使用者提供核心介面，而非取代 Magisk。

## ShizuSU 可以支援非 GKI 裝置嗎？

可以。但是您應該下載核心來源並整合 ShizuSU 至來源樹狀結構並自行編譯核心。

## ShizuSU 支援 Android 12 以下的裝置嗎？

影響 ShizuSU 相容性的是裝置的核心版本，它與 Android 版本並無直接關係。唯一有關聯的是：**原廠** Android 12 的裝置，一定是 5.10 或更高的核心 (GKI 裝置)；因此結論如下：

1. 原廠 Android 12 的裝置必定支援 (GKI 裝置)
2. 舊版核心的裝置 (即使是 Android 12，也可能是舊版核心) 是相容的 (您需要自行建置核心)

## ShizuSU 可以支援舊版核心嗎？

可以，目前最低支援到 4.14；更低的版本您需要手動移植它，歡迎 PR！

## 如何為舊版核心整合 ShizuSU？

請參閱[指南](how-to-integrate-for-non-gki.md)

## 為何我的 Android 版本為 13，但核心版本卻是 "android12-5.10"？

核心版本與 Android 版本無關，如果您要使用 ShizuSU，請一律使用**核心版本**而非 Android 版本，如果你為 "android12-5.10" 的裝置寫入 Android 13 的核心，等候您的將會是開機迴圈。

## 我是 GKI1.0，能用 ShizuSU 嗎？

GKI1 與 GKI2 完全不同，所以您需要自行編譯核心。

## ShizuSU 支援 --mount-master/全域掛接命名空間嗎？

目前沒有 (未來可能會支援)，但實際上有很多種方法手動進入全域命名空間，無需 `su` 內建支援，比如：

1. `nsenter -t 1 -m sh` 可以取得一個全域 mount namespace 的 shell.
2. 在您要執行的命令前新增 `nsenter --mount=/proc/1/ns/mnt` 即可使此命令在全域 mount namespace 下執行。ShizuSU 本身也使用了 [這種方法](https://github.com/tiann/KernelSU/blob/77056a710073d7a5f7ee38f9e77c9fd0b3256576/manager/app/src/main/java/me/weishu/kernelsu/ui/util/KsuCli.kt#L115)

## ShizuSU 可以修改 Hosts 嗎？ 我要怎麼使用 AdAway？
當然。但是 ShizuSU 沒有內建的 Hosts 支持，您可以安裝 [systemless-hosts](https://github.com/symbuzzer/systemless-hosts-KernelSU-module) 來做到這一點。

## 為什麼全新安裝後模組不工作？

請檢查：模組是否已啟用、模組目錄是否存在有效的 `module.prop`、模組是否與您的裝置/核心相容。ShizuSU 的模組系統基於 Magic Mount，修改 `/system` 檔案的模組無需 metamodule。

**解決方案**：參閱[模組開發指南](module.md) 與[模組系統：Magic Mount](metamodule.md)。

## ShizuSU 的模組系統是什麼？

ShizuSU 基於 SukiSU-Ultra 二次開發，模組系統採用 **Magic Mount**（bind mount 將模組的 `system` 目錄疊加到 `/system`），無需 metamodule，Magisk 模組直接相容。官方 KernelSU 使用的 OverlayFS metamodule 架構不適用於 ShizuSU。詳見[模組系統：Magic Mount](metamodule.md)。
