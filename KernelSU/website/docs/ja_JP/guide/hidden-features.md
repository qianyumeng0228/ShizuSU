# 隠し機能

## .ksurc

デフォルトでは `/system/bin/sh` は `/system/etc/mkshrc` を読み込みます。

`/data/adb/ksu/.ksurc` ファイルを作成することで、カスタマイズした rc ファイルを su に読み込ませられます。

## カスタマイズ {#customization}

- **カスタム背景**：ShizuSU マネージャーの設定で背景画像を変更し、インターフェースをカスタマイズできます。
- **susfs 管理**：マネージャー内で一部の susfs 機能を直接管理できます（追加の susfsforksu モジュールは不要）。
- **DPI 調整**：マネージャーの DPI 表示を調整し、さまざまな画面に対応できます。

## WebUI X {#webui-x}

MMRL による次世代 WebUI 実装（WebUI X）をサポートし、より豊かなモジュールインタラクションを提供します。

## マルチマネージャー対応 {#multi-manager}

ShizuSU にはマネージャー署名テーブルが内蔵されており、1つのカーネルで複数の root マネージャー（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU）を同時に認識できます。内蔵署名に加え、ホット登録と永続化（`/data/adb/shizusu/manager`）にも対応しており、新しいマネージャーをインストールしてもカーネルの焼き直しは不要です。

## ステルスモード {#stealth}

`/data/adb/shizusu/stealth` にフラグを書き込むとステルスモードが有効になります。有効時、ShizuSU の情報レポートはアプリに現在のマネージャー状態を公開しなくなり、検出リスクを低減します。

## 隠蔽の強化 {#hiding-enhancements}

- **susfsd クエリチャネル**：susfsd 通信チャネルを内蔵し、susfs カーネルパッチと直接連携できます。
- **KPROBES フック隠蔽**：オプションの KPROBES フック隠蔽（デフォルトはオフ）。
- **hosts 隠蔽エントリ**：App Profile と連動し、モジュールによる hosts ファイルの変更を隠せます。
