# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

pairs = [
# ============ zh_TW ============
("zh_TW/index.md",
"""  - title: Metamodule 系統
    details: 可插拔的模組基礎架構,允許 systemless 方式修改 /system。安裝 metamodule(如 meta-overlayfs)以啟用模組掛載功能。""",
"""  - title: Magic Mount 模組系統
    details: 基於 SukiSU-Ultra 繼承的 Magic Mount（5ec1cff）技術，模組掛載開箱即用，Magisk 模組直接相容。"""),
("zh_TW/index.md",
"""  - title: 基於 Magic Mount 的模組系統
    details: 基於 5ec1cff 的 Magic Mount 技術建置，提供更穩定、更可靠的模組掛載基礎。""",
"""  - title: 基於 SukiSU-Ultra 二改
    details: 在成熟社群方案上二次開發，繼承非 GKI 支援、Magic Mount、KPM 等增強特性。"""),
("zh_TW/guide/what-is-kernelsu.md",
"此外，ShizuSU 提供了 [metamodule 系統](metamodule.md)，這是一個用於模組管理的可插拔架構。與傳統 root 解決方案將掛載邏輯內建於核心的做法不同，ShizuSU 將此工作委託給 metamodule。這允許您安裝 metamodule(如 [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs))來提供對 `/system` 分區和其他分區的 systemless 修改。",
"此外，ShizuSU 基於 **SukiSU-Ultra** 二次開發，模組系統採用 **Magic Mount**（源自 5ec1cff 的 Magisk 實作）：模組的 `system` 目錄透過 bind mount 以 systemless 方式疊加到 `/system`，**無需安裝 metamodule**。詳見[模組系統：Magic Mount](metamodule.md)。"),
("zh_TW/guide/what-is-kernelsu.md",
"- **基於 Magic Mount 的模組系統**：模組掛載基於 5ec1cff 的 Magic Mount 技術建置，提供更穩定、更可靠的基礎，同時保留了可插拔的 [metamodule](metamodule.md) 架構。",
"- **基於 Magic Mount 的模組系統**：ShizuSU 基於 SukiSU-Ultra 二改，模組掛載採用 5ec1cff 的 Magic Mount 技術，提供更穩定、更可靠的基礎，Magisk 模組可直接使用。ShizuSU 不使用官方 KernelSU 的 OverlayFS metamodule 架構，全站模組系統口徑統一為 Magic Mount。"),
("zh_TW/guide/module.md",
"""ShizuSU 提供了一個模組機制，它可以在保持系統分割區完整性的同時達到修改系統分割區的效果；這種機制一般被稱為 systemless (無系統修改)。

ShizuSU 的模組運作機制與 Magisk 幾乎相同，如果您熟悉 Magisk 模組的開發，那麼開發 ShizuSU 的模組大同小異，您可以跳過下列有關模組的介紹，只需要瞭解 [ShizuSU 模組與 Magisk 模組的差異](difference-with-magisk.md)。

::: warning METAMODULE 僅在修改系統檔案時需要
ShizuSU 使用 [metamodule](metamodule.md) 架構來掛載 `system` 目錄。**僅當您的模組需要修改 `/system` 檔案時**(透過 `system` 目錄)，您才需要安裝 metamodule(如 [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases))。其他模組功能如腳本、sepolicy 規則和 system.prop 無需 metamodule 即可運作。
:::""",
"""ShizuSU 提供了一個模組機制，它可以在保持系統分割區完整性的同時達到修改系統分割區的效果；這種機制一般被稱為 systemless (無系統修改)。ShizuSU 的模組系統基於 **Magic Mount**（源自 SukiSU-Ultra），無需安裝 metamodule，模組開箱即用。

ShizuSU 的模組運作機制與 Magisk 幾乎相同，如果您熟悉 Magisk 模組的開發，那麼開發 ShizuSU 的模組大同小異，您可以跳過下列有關模組的介紹，只需要瞭解 [ShizuSU 模組與 Magisk 模組的差異](difference-with-magisk.md)。

::: info 模組系統基於 Magic Mount
ShizuSU 的模組系統基於 **Magic Mount**（來自 SukiSU-Ultra），無需安裝 metamodule 即可掛載 `system` 目錄。修改 `/system` 檔案的模組開箱即用。詳見[模組系統：Magic Mount](metamodule.md)。
:::"""),
("zh_TW/guide/module.md",
"如果你想在掛載 overlayfs 後做一些事情，請使用 `post-mount.sh`。",
"如果你想在模組掛載完成後做一些事情，請使用 `post-mount.sh`。"),
("zh_TW/guide/module.md",
"""### `system` 目錄 {#system-directories}

這個目錄的內容會在系統啟動後，以 `overlayfs` 的方式覆疊在系統的 `/system` 分區之上，這表示：

1. 系統中對應目錄的相同名稱的檔案會被此目錄中的檔案覆寫。
2. 系統中對應目錄的相同名稱的檔案會與此目錄的檔案合併。

如果您想要刪除系統先前的目錄中的某個檔案或資料夾，您需要在模組目錄中透過 `mknod filename c 0 0` 以建立一個 `filename` 的相同名稱的檔案；這樣 overlayfs 系統會自動「whiteout」等效刪除這個檔案 (`/system` 分割區並未被變更)。

您也可以在 `customize.sh` 中宣告一個名為 `REMOVE` 並且包含一系列目錄的變數以執行移除作業，ShizuSU 會自動為您在模組對應目錄執行 `mknod <TARGET> c 0 0`。例如：

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

上方的清單將會執行：`mknod $MODPATH/system/app/YouTuBe c 0 0` 和 `mknod $MODPATH/system/app/Bloatware c 0 0`；並且 `/system/app/YouTube` 和 `/system/app/Bloatware` 將會在模組生效前移除。

如果您想要取代系統的某個目錄，您需要在模組目錄中建立一個相同路徑的目錄，然後為此目錄設定此屬性：`setfattr -n trusted.overlay.opaque -v y <TARGET>`；這樣 overlayfs 系統會自動將對應目錄取代 (`/system` 分割區並未被變更)。

您可以在 `customize.sh` 中宣告一個名為 `REMOVE` 並且包含一系列目錄的變數以執行移除作業，ShizuSU 會自動為您在模組對應目錄執行相關作業。例如：

```sh
REPLACE="
/system/app/YouTube
/system/app/Bloatware
"
```

上方的清單將會執行：自動建立目錄 `$MODPATH/system/app/YouTube` 和 `$MODPATH//system/app/Bloatware`，然後執行 `setfattr -n trusted.overlay.opaque -v y $$MODPATH/system/app/YouTube` 和 `setfattr -n trusted.overlay.opaque -v y $$MODPATH/system/app/Bloatware`；並且 `/system/app/YouTube` 和 `/system/app/Bloatware` 將會在模組生效後被取代為空白目錄。

::: tip 與 Magisk 的差異

ShizuSU 的 systemless 機制透過核心的 overlayfs 實作，而 Magisk 目前則是透過 magic mount (bind mount)，兩者的實作方式有很大的差別，但最終的目標是一致的：不修改實際的 `/system` 分區但修改 `/system` 檔案。
:::

如果您對 overlayfs 感興趣，建議閱讀 Linux Kernel 關於 [overlayfs 的文檔](https://docs.kernel.org/filesystems/overlayfs.html)""",
"""### `system` 目錄 {#system-directories}

這個目錄的內容會在系統啟動後，以 **Magic Mount（bind mount）** 的方式疊加在系統的 `/system` 分區之上，這表示：

1. 系統中對應目錄的相同名稱的檔案會被此目錄中的檔案覆寫。
2. 系統中對應目錄的相同名稱的檔案會與此目錄的檔案合併。

如果您想要刪除系統先前的目錄中的某個檔案或資料夾，您可以在 `customize.sh` 中宣告一個名為 `REMOVE` 並且包含一系列目錄的變數以執行移除作業，ShizuSU 會透過 Magic Mount 自動隱藏這些檔案（`/system` 分割區並未被變更）。例如：

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` 和 `/system/app/Bloatware` 將會在模組生效後被隱藏。

如果您想要取代系統的某個目錄，您可以在 `customize.sh` 中宣告一個名為 `REPLACE` 並且包含一系列目錄的變數以執行取代作業，ShizuSU 會把模組目錄中對應路徑掛載為空目錄，實現整目錄取代。例如：

```sh
REPLACE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` 和 `/system/app/Bloatware` 將會在模組生效後被取代為空目錄。

::: tip 與官方 KernelSU 的差異

官方 KernelSU 使用 OverlayFS（metamodule）機制實作 systemless，而 ShizuSU 基於 SukiSU-Ultra，與 Magisk 一樣使用 **Magic Mount（bind mount）** 實作。兩種方案的最終目標一致：不修改實際的 `/system` 分區但修改 `/system` 檔案。ShizuSU 只使用 Magic Mount，不存在兩種模組系統。詳見[模組系統：Magic Mount](metamodule.md)。
:::"""),
("zh_TW/guide/module.md",
"  - `post-fs-data.sh` 以 post-fs-data 模式運行，`post-mount.sh` 在 overlayfs 掛載後運行，而 `service.sh` 則以 late_start 服務模式運行，`boot-completed` 在 Android 系統啟動完畢後以服務模式運作。",
"  - `post-fs-data.sh` 以 post-fs-data 模式運行，`post-mount.sh` 在模組掛載完成後運行，而 `service.sh` 則以 late_start 服務模式運行，`boot-completed` 在 Android 系統啟動完畢後以服務模式運作。"),
("zh_TW/guide/module.md",
"| OverlayFS 掛載（metamodule） | 是 | 是 |",
"| Magic Mount 模組掛載 | 是 | 是 |"),
("zh_TW/guide/module.md",
"  5. 執行 metamodule mount 指令碼（OverlayFS 掛載）",
"  5. 執行 Magic Mount 模組掛載"),
("zh_TW/guide/module.md",
"該指令碼在 OverlayFS 掛載之前執行，與標準流程中的 `post-fs-data.sh` 時機類似。",
"該指令碼在模組掛載之前執行，與標準流程中的 `post-fs-data.sh` 時機類似。"),
("zh_TW/guide/installation.md",
"""## 安裝後：模組支援 {#post-installation}

::: warning 用於系統檔案修改的 METAMODULE
如果您想使用修改 `/system` 檔案的模組，您需要在安裝 ShizuSU 後安裝 **metamodule**。僅使用腳本、sepolicy 或 system.prop 的模組無需 metamodule 即可運作。
:::

**若需 `/system` 修改支援**，請參閱 [Metamodule 指南](metamodule.md)以：
- 了解什麼是 metamodule 以及為何需要它
- 安裝官方 `meta-overlayfs` metamodule
- 了解其他 metamodule 選項""",
"""## 安裝後：模組支援 {#post-installation}

::: info 模組系統基於 Magic Mount
ShizuSU 的模組系統基於 **Magic Mount**（來自 SukiSU-Ultra），安裝 ShizuSU 後模組即可直接運作，修改 `/system` 檔案的模組也無需額外安裝 metamodule。
:::

關於模組系統的工作原理與模組開發，請參閱[模組系統：Magic Mount](metamodule.md) 與[模組開發指南](module.md)。"""),
("zh_TW/guide/faq.md",
"支援，大多數 Magisk 模組都可以在 ShizuSU 上運作。但是，如果您的模組需要修改 `/system` 檔案，您需要安裝 [metamodule](metamodule.md)(如 `meta-overlayfs`)。其他模組功能無需 metamodule 即可運作。請參閱 [模組指南](module.md) 以獲取更多資訊。",
"支援。ShizuSU 的模組系統基於 Magic Mount（來自 SukiSU-Ultra），Magisk 模組可以直接使用，修改 `/system` 檔案的模組也無需 metamodule。請參閱 [模組指南](module.md)。"),
("zh_TW/guide/faq.md",
"""如果您的模組需要修改 `/system` 檔案，您需要安裝 [metamodule](metamodule.md) 來掛載 `system` 目錄。其他模組功能(腳本、sepolicy、system.prop)無需 metamodule 即可運作。

**解決方案**：參閱 [Metamodule 指南](metamodule.md) 獲取安裝說明。""",
"""請檢查：模組是否已啟用、模組目錄是否存在有效的 `module.prop`、模組是否與您的裝置/核心相容。ShizuSU 的模組系統基於 Magic Mount，修改 `/system` 檔案的模組無需 metamodule。

**解決方案**：參閱[模組開發指南](module.md) 與[模組系統：Magic Mount](metamodule.md)。"""),
("zh_TW/guide/faq.md",
"""## 什麼是 metamodule，為什麼需要它？

Metamodule 是一個特殊模組，為掛載常規模組提供基礎架構。請參閱 [Metamodule 指南](metamodule.md) 獲取完整說明。""",
"""## ShizuSU 的模組系統是什麼？

ShizuSU 基於 SukiSU-Ultra 二次開發，模組系統採用 **Magic Mount**（bind mount 將模組的 `system` 目錄疊加到 `/system`），無需 metamodule，Magisk 模組直接相容。官方 KernelSU 使用的 OverlayFS metamodule 架構不適用於 ShizuSU。詳見[模組系統：Magic Mount](metamodule.md)。"""),
("zh_TW/guide/app-profile.md",
"ShizuSU 提供了一種無須直接修改系統分區的方式 (systemless) 來修改系統分區，這是透過掛載 overlayfs 來實現的。但有些情況下，App 可能會對這種行為比較敏感；因此，我們可以透過設定「卸載模組」來卸載掛載在這些應用程式上的模組。",
"ShizuSU 提供了一種無須直接修改系統分區的方式 (systemless) 來修改系統分區，這是透過 Magic Mount（bind mount）來實現的。但有些情況下，App 可能會對這種行為比較敏感；因此，我們可以透過設定「卸載模組」來卸載掛載在這些應用程式上的模組。"),
("zh_TW/guide/difference-with-magisk.md",
"3. **模組掛載架構**：ShizuSU 使用 [metamodule 系統](metamodule.md)，將掛載委託給可插拔的 metamodule(例如 `meta-overlayfs`)，而 Magisk 將掛載內建於其核心中。ShizuSU 需要安裝 metamodule 才能啟用模組掛載。",
"3. **模組掛載架構**：ShizuSU 基於 SukiSU-Ultra，與 Magisk 一樣使用 **Magic Mount（bind mount）** 實現模組掛載；官方 KernelSU 則使用 OverlayFS metamodule 架構。"),
("zh_TW/guide/difference-with-magisk.md",
"4. ShizuSU 模組取代或刪除檔案與 Magisk 完全不同。ShizuSU 不支援 `.replace` 方法，相反，您需要透過 `mknod filename c 0 0` 建立相同名稱的資料夾以刪除對應檔案。",
"4. ShizuSU 模組取代或刪除檔案的方式與 Magisk 相同：在 `customize.sh` 中使用 `REMOVE` 和 `REPLACE` 變數來刪除檔案或取代目錄（詳見[模組開發指南](module.md)）。"),

# ============ ja_JP ============
("ja_JP/index.md",
"""  - title: Metamodule システム
    details: プラグ可能なモジュールインフラストラクチャにより、systemless方式で/systemを変更可能。metamodule(meta-overlayfsなど)をインストールすることでモジュールのマウント機能を有効化。""",
"""  - title: Magic Mount モジュールシステム
    details: SukiSU-Ultra 由来の Magic Mount（5ec1cff）技術により、モジュールマウントがすぐに動作し、Magisk モジュールにも直接対応。"""),
("ja_JP/index.md",
"""  - title: Magic Mount ベースのモジュールシステム
    details: 5ec1cff の Magic Mount テクノロジーに基づき、より安定・信頼性の高いモジュールマウント基盤を提供します。""",
"""  - title: SukiSU-Ultra ベース
    details: 成熟したコミュニティプロジェクトの二次開発版。非 GKI サポート、Magic Mount、KPM などの拡張機能を継承。"""),
("ja_JP/guide/what-is-kernelsu.md",
"さらに、ShizuSU は [metamodule システム](metamodule.md) を提供しています。これはモジュール管理のためのプラグ可能なアーキテクチャです。従来の root ソリューションがマウントロジックをコアに組み込むのとは異なり、ShizuSU はこの作業を metamodule に委任します。これにより、metamodule ([meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs)など) をインストールして、`/system`パーティションや他のパーティションへのsystemless変更を提供できます。",
"さらに、ShizuSU は **SukiSU-Ultra** の二次開発版であり、モジュールシステムは **Magic Mount**（5ec1cff の Magisk 実装由来）を採用しています：モジュールの `system` ディレクトリを bind mount で systemless に `/system` へ重ね合わせるため、**metamodule のインストールは不要**です。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。"),
("ja_JP/guide/what-is-kernelsu.md",
"- **Magic Mount ベースのモジュールシステム**：モジュールマウントは 5ec1cff の Magic Mount 技術に基づいて構築され、より安定・信頼性の高い基盤を提供しつつ、プラグ可能な [metamodule](metamodule.md) アーキテクチャも維持します。",
"- **Magic Mount ベースのモジュールシステム**：ShizuSU は SukiSU-Ultra の二次開発版であり、モジュールマウントは 5ec1cff の Magic Mount 技術に基づいて構築され、より安定・信頼性の高い基盤を提供し、Magisk モジュールも直接利用できます。ShizuSU は公式 KernelSU の OverlayFS metamodule アーキテクチャを使用せず、モジュールシステムの説明は全サイト Magic Mount に統一されています。"),
("ja_JP/guide/module.md",
"""ShizuSU はシステムパーティションの整合性を維持しながら、システムディレクトリを変更する効果を実現するモジュール機構を提供します。この機構は一般に「システムレス」と呼ばれています。

ShizuSU のモジュール機構は、Magisk とほぼ同じです。Magisk のモジュール開発に慣れている方であれば、ShizuSU のモジュール開発も簡単でしょう。その場合は以下のモジュールの紹介は読み飛ばして、[Magisk との違い](difference-with-magisk.md)の内容だけ読めばOKです。

::: warning METAMODULE はシステムファイル変更時のみ必要
ShizuSU は [metamodule](metamodule.md) アーキテクチャを使用して `system` ディレクトリをマウントします。**モジュールが `/system` ファイルを変更する必要がある場合のみ**（`system` ディレクトリ経由で）、metamodule ([meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)など) をインストールする必要があります。スクリプト、sepolicy ルール、system.propなどの他のモジュール機能は metamodule なしで動作します。
:::""",
"""ShizuSU はシステムパーティションの整合性を維持しながら、システムディレクトリを変更する効果を実現するモジュール機構を提供します。この機構は一般に「システムレス」と呼ばれています。ShizuSU のモジュールシステムは **Magic Mount**（SukiSU-Ultra 由来）に基づいており、metamodule は不要でモジュールはすぐに動作します。

ShizuSU のモジュール機構は、Magisk とほぼ同じです。Magisk のモジュール開発に慣れている方であれば、ShizuSU のモジュール開発も簡単でしょう。その場合は以下のモジュールの紹介は読み飛ばして、[Magisk との違い](difference-with-magisk.md)の内容だけ読めばOKです。

::: info モジュールシステムは Magic Mount ベース
ShizuSU のモジュールシステムは **Magic Mount**（SukiSU-Ultra 由来）に基づいており、metamodule をインストールしなくても `system` ディレクトリをマウントできます。`/system` ファイルを変更するモジュールもそのまま動作します。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。
:::"""),
("ja_JP/guide/module.md",
"""### `system` ディレクトリ

このディレクトリの内容は、システムの起動後に OverlayFS を使用してシステムの /system パーティションの上にオーバーレイされます：

1. システム内の対応するディレクトリにあるファイルと同名のファイルは、このディレクトリにあるファイルで上書きされます。
2. システム内の対応するディレクトリにあるフォルダと同じ名前のフォルダは、このディレクトリにあるフォルダと統合されます。

元のシステムディレクトリにあるファイルやフォルダを削除したい場合は、`mknod filename c 0 0` を使ってモジュールディレクトリにそのファイル/フォルダと同じ名前のファイルを作成する必要があります。こうすることで、OverlayFS システムはこのファイルを削除したかのように自動的に「ホワイトアウト」します（/system パーティションは実際には変更されません）。

また、`customize.sh` 内で `REMOVE` という変数に削除操作を実行するディレクトリのリストを宣言すると、ShizuSU は自動的にそのモジュールの対応するディレクトリで `mknod <TARGET> c 0 0` を実行します。例えば

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

上記の場合は、`mknod $MODPATH/system/app/YouTuBe c 0 0`と`mknod $MODPATH/system/app/Bloatware c 0 0`を実行し、`/system/app/YouTube`と`/system/app/Bloatware`はモジュール有効化後に削除されます。

システム内のディレクトリを置き換えたい場合は、モジュールディレクトリに同じパスのディレクトリを作成し、このディレクトリに `setfattr -n trusted.overlay.opaque -v y <TARGET>` という属性を設定する必要があります。こうすることで、OverlayFS システムは（/system パーティションを変更することなく）システム内の対応するディレクトリを自動的に置き換えることができます。

`customize.sh` ファイル内に `REPLACE` という変数を宣言し、その中に置換するディレクトリのリストを入れておけば、ShizuSU は自動的にモジュールディレクトリに対応した処理を行います。例えば：

REPLACE="
/system/app/YouTube
/system/app/Bloatware
"

このリストは、自動的に `$MODPATH/system/app/YouTube` と `$MODPATH/system/app/Bloatware` というディレクトリを作成し、 `setfattr -n trusted.overlay.opaque -v y $MODPATH/system/app/YouTube` と `setfattr -n trusted.overlay.opaque -v y $MODPATH/system/app/Bloatware` を実行します。モジュールが有効になると、`/system/app/YouTube` と `/system/app/Bloatware` は空のディレクトリに置き換えられます。

::: tip Magisk との違い

ShizuSU のシステムレスメカニズムはカーネルの OverlayFS によって実装され、Magisk は現在マジックマウント（bind mount）を使用しています。この2つの実装方法には大きな違いがありますが最終的な目的は同じで、/system パーティションを物理的に変更することなく、/system のファイルを変更できます。
:::

OverlayFS に興味があれば、Linux カーネルの [OverlayFS のドキュメンテーション](https://docs.kernel.org/filesystems/overlayfs.html) を読んでみてください。""",
"""### `system` ディレクトリ

このディレクトリの内容は、システムの起動後に **Magic Mount（bind mount）** を使用してシステムの /system パーティションの上にオーバーレイされます：

1. システム内の対応するディレクトリにあるファイルと同名のファイルは、このディレクトリにあるファイルで上書きされます。
2. システム内の対応するディレクトリにあるフォルダと同じ名前のフォルダは、このディレクトリにあるフォルダと統合されます。

元のシステムディレクトリにあるファイルやフォルダを削除したい場合は、`customize.sh` 内で `REMOVE` という変数に削除操作を実行するディレクトリのリストを宣言してください。ShizuSU は Magic Mount によってこれらのファイルを自動的に隠します（/system パーティションは実際には変更されません）。例えば：

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` と `/system/app/Bloatware` はモジュール有効化後に非表示になります。

システム内のディレクトリを置き換えたい場合は、`customize.sh` ファイル内に `REPLACE` という変数を宣言し、その中に置換するディレクトリのリストを入れてください。ShizuSU は対象パスに空のディレクトリを bind mount し、ディレクトリ全体を置き換えます。例えば：

REPLACE="
/system/app/YouTube
/system/app/Bloatware
"

`/system/app/YouTube` と `/system/app/Bloatware` はモジュール有効化後に空のディレクトリに置き換えられます。

::: tip 公式 KernelSU との違い

公式 KernelSU は OverlayFS（metamodule）メカニズムで systemless を実装しますが、ShizuSU は SukiSU-Ultra ベースで、Magisk と同じく **Magic Mount（bind mount）** を使用します。両者の最終的な目的は同じで、/system パーティションを物理的に変更することなく、/system のファイルを変更できます。ShizuSU は Magic Mount のみを使用し、2つのモジュールシステムは存在しません。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。
:::"""),
("ja_JP/guide/module.md",
"| OverlayFS マウント（metamodule） | はい | はい |",
"| Magic Mount モジュールマウント | はい | はい |"),
("ja_JP/guide/module.md",
"  5. metamodule マウントスクリプトを実行（OverlayFS マウント）",
"  5. Magic Mount モジュールマウントを実行"),
("ja_JP/guide/module.md",
"このスクリプトは OverlayFS マウント前に実行され、標準フローの `post-fs-data.sh` と同様のタイミングです。",
"このスクリプトはモジュールマウント前に実行され、標準フローの `post-fs-data.sh` と同様のタイミングです。"),
("ja_JP/guide/installation.md",
"""## インストール後：モジュールサポート {#post-installation}

::: warning システムファイル変更用の METAMODULE
`/system` ファイルを変更するモジュールを使用したい場合は、ShizuSU インストール後に **metamodule** をインストールする必要があります。スクリプト、sepolicy、または system.prop のみを使用するモジュールは metamodule なしで動作します。
:::

**`/system` 変更サポートが必要な場合**、[Metamodule ガイド](metamodule.md)を参照して：
- metamodule とは何か、なぜ必要なのかを理解する
- 公式の `meta-overlayfs` metamodule をインストールする
- 他の metamodule オプションについて学ぶ""",
"""## インストール後：モジュールサポート {#post-installation}

::: info モジュールシステムは Magic Mount ベース
ShizuSU のモジュールシステムは **Magic Mount**（SukiSU-Ultra 由来）を採用しており、ShizuSU インストール後すぐにモジュールが動作します。`/system` ファイルを変更するモジュールも metamodule の追加インストールは不要です。
:::

モジュールシステムの仕組みとモジュール開発については、[モジュールシステム：Magic Mount](metamodule.md) と [モジュールガイド](module.md) をご覧ください。"""),
("ja_JP/guide/faq.md",
"はい。ほとんどの Magisk モジュールは ShizuSU で動作します。ただし、モジュールが `/system` ファイルを変更する必要がある場合は、[metamodule](metamodule.md) (`meta-overlayfs`など) をインストールする必要があります。他のモジュール機能は metamodule なしで動作します。詳細は [モジュールガイド](module.md) をご覧ください。",
"はい。ShizuSU のモジュールシステムは Magic Mount（SukiSU-Ultra 由来）に基づいており、Magisk モジュールは直接動作し、`/system` ファイルを変更するモジュールも metamodule は不要です。詳細は [モジュールガイド](module.md) をご覧ください。"),
("ja_JP/guide/faq.md",
"""モジュールが `/system` ファイルを変更する必要がある場合は、`system` ディレクトリをマウントするために [metamodule](metamodule.md) をインストールする必要があります。他のモジュール機能（スクリプト、sepolicy、system.prop）は metamodule なしで動作します。

**解決策**：インストール手順については [Metamodule ガイド](metamodule.md) をご覧ください。""",
"""モジュールが有効かどうか、モジュールディレクトリに有効な `module.prop` があるかどうか、モジュールがデバイス/カーネルと互換性があるかどうかを確認してください。ShizuSU のモジュールシステムは Magic Mount ベースであり、`/system` ファイルを変更するモジュールも metamodule は不要です。

**解決策**：[モジュールガイド](module.md) と [モジュールシステム：Magic Mount](metamodule.md) をご覧ください。"""),
("ja_JP/guide/faq.md",
"""## metamodule とは何ですか？なぜ必要なのですか？

Metamodule は、通常のモジュールをマウントするためのインフラストラクチャを提供する特殊なモジュールです。完全な説明については [Metamodule ガイド](metamodule.md) をご覧ください。""",
"""## ShizuSU のモジュールシステムとは？

ShizuSU は SukiSU-Ultra の二次開発版であり、モジュールシステムは **Magic Mount**（モジュールの `system` ディレクトリを `/system` に bind mount）を採用し、metamodule は不要で Magisk モジュールと直接互換です。公式 KernelSU の OverlayFS metamodule アーキテクチャは ShizuSU には適用されません。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。"""),
("ja_JP/guide/app-profile.md",
"ShizuSU は OverlayFS をマウントすることで systemless な形でシステムパーティションを変更します。しかしこの挙動に敏感なアプリもあります。そのような場合は「Umount modules」オプションを設定して、対象アプリではモジュールをアンマウントさせることができます。",
"ShizuSU は Magic Mount（bind mount）によって systemless な形でシステムパーティションを変更します。しかしこの挙動に敏感なアプリもあります。そのような場合は「Umount modules」オプションを設定して、対象アプリではモジュールをアンマウントさせることができます。"),
("ja_JP/guide/difference-with-magisk.md",
"- **モジュールマウントアーキテクチャ**：ShizuSU は [metamodule システム](metamodule.md) を使用し、マウントをプラグ可能な metamodule (`meta-overlayfs`など) に委任します。一方、Magisk はマウントをコアに組み込んでいます。ShizuSU はモジュールマウントを有効にするために metamodule のインストールが必要です。",
"- **モジュールマウントアーキテクチャ**：ShizuSU は SukiSU-Ultra ベースで、Magisk と同じく **Magic Mount（bind mount）** を使用します。公式 KernelSU は代わりに OverlayFS metamodule アーキテクチャを使用します。"),
("ja_JP/guide/difference-with-magisk.md",
"- ShizuSU モジュールにおけるファイルの置換や削除の方法は、Magisk とは全く異なります。ShizuSU は `.replace` メソッドをサポートしていません。その代わり、`mknod filename c 0 0` で同名のファイルを作成し、対応するファイルを削除する必要があります。",
"- ShizuSU モジュールにおけるファイルの置換や削除の方法は Magisk と同じです：`customize.sh` で `REMOVE` と `REPLACE` 変数を使用してファイルを削除したりディレクトリを置き換えます（[モジュールガイド](module.md)を参照）。"),
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
