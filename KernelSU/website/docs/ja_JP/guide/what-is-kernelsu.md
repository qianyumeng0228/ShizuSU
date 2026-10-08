# ShizuSU とは?

ShizuSU は Android GKI デバイスのための root ソリューションです。カーネルモードで動作し、カーネル空間で直接ユーザー空間アプリに root 権限を付与します。

## 機能

ShizuSU の最大の特徴は、**カーネルベース**であることです。ShizuSU はカーネルモードで動作するため、今までにないカーネルインターフェイスを提供できます。例えば、カーネルモードで任意のプロセスにハードウェアブレークポイントを追加できる、誰にも気づかれずに任意のプロセスの物理メモリにアクセスできる、カーネル空間で任意のシステムコールを傍受できる、などです。

さらに、ShizuSU は **SukiSU-Ultra** の二次開発版であり、モジュールシステムは **Magic Mount**（5ec1cff の Magisk 実装由来）を採用しています：モジュールの `system` ディレクトリを bind mount で systemless に `/system` へ重ね合わせるため、**metamodule のインストールは不要**です。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。

## 拡張機能

カーネルレベル root の基本機能に加えて、ShizuSU には以下の拡張機能があります。

- **非 GKI / 旧カーネルサポート**：ShizuSU は非 GKI および GKI 1.0 デバイスのサポートを復活し、4.x - 5.4 LTS カーネル（3.x は実験的）をカバーします。アーキテクチャ対応：`arm64-v8a` 完全サポート、`armeabi-v7a` 基本サポート、`x86_64` 一部サポート。
- **Magic Mount ベースのモジュールシステム**：ShizuSU は SukiSU-Ultra の二次開発版であり、モジュールマウントは 5ec1cff の Magic Mount 技術に基づいて構築され、より安定・信頼性の高い基盤を提供し、Magisk モジュールも直接利用できます。ShizuSU は公式 KernelSU の OverlayFS metamodule アーキテクチャを使用せず、モジュールシステムの説明は全サイト Magic Mount に統一されています。
- **KPM カーネルモジュール**：KernelPatch Module（KPM、Apatch から移植）を完全サポートし、カーネルレベルでの高度な改変と拡張が可能です。
- **App Profile**：アプリケーションプロファイルで root 権限を管理された環境にロックします。詳細は [App Profile](app-profile.md) をご覧ください。
- **幅広いカスタマイズ**：カスタム背景、susfs 機能の直接管理（追加の susfsforksu モジュール不要）、DPI 調整など、自分好みに設計できます。

- **マルチマネージャー対応（Multi-manager）**：1つのカーネルが内蔵の署名テーブルで複数のマネージャー（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU）を同時に認識。ホット登録と永続化（`/data/adb/shizusu/manager`）にも対応し、マネージャーごとにカーネルを焼き直す必要はありません。
- **ステルスモード（Stealth）**：`/data/adb/shizusu/stealth` にフラグを書き込むだけで有効化。有効時は情報レポートがアプリにマネージャーの状態を公開しなくなり、検出リスクを下げます。
- **モジュール管理の利便性**：モジュールと root ホワイトリストのバックアップ/復元、バッチインストール（失敗を収集して中断しない）、ワンタップでの有効化/無効化/すべて無効化/すべてアンインストールに対応。
- **隠蔽の強化**：susfsd クエリチャネル、オプションの KPROBES フック隠蔽（デフォルトはオフ）、App Profile と連動する hosts 隠蔽エントリを内蔵。

## 技術の継承と融合 {#inherited-abilities}

ShizuSU は誕生時に、複数の成熟した root ソリューションとマネージャーエコシステムの能力を統合しました。技術系譜：

| 能力 | 提供元 |
|---|---|
| カーネルレベルの `su` と root 認可管理 | KernelSU（アップストリーム） |
| Magic Mount モジュールシステム | Magisk（MKSU と SukiSU-Ultra 経由で継承） |
| 非 GKI / 旧カーネルサポート | RKSU、SukiSU-Ultra |
| KPM カーネルモジュール | KernelPatch（APatch 実装） |
| モジュール管理、susfsd、隠蔽強化 | KernelSU-Next |
| マルチマネージャー署名テーブル | ReSukiSU（参考） |
| ステルス実装 | 7kimisu（参考） |
| カーネルレベル隠蔽パッチ | susfs |
| APK v2 署名検証 | genuine |

## 使用方法

こちらをご覧ください: [インストール方法](installation)

## ビルド方法

[ビルドするには](../../guide/how-to-build)

## ディスカッション

- Telegram: [@KernelSU](https://t.me/KernelSU)
