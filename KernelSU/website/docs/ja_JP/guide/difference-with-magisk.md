# Magisk との違い

ShizuSU モジュールと Magisk モジュールには多くの共通点がありますが、実装の仕組みが全く異なるため、必然的にいくつかの相違点が存在します。Magisk と ShizuSU の両方でモジュールを動作させたい場合、これらの違いを理解する必要があります。

## 似ているところ

- モジュールファイルの形式：どちらもzip形式でモジュールを整理しており、モジュールの形式はほぼ同じです。
- モジュールのインストールディレクトリ：どちらも `/data/adb/modules` に配置されます。
- システムレス：どちらもモジュールによるシステムレスな方法で /system を変更できます。
- post-fs-data.sh: 実行時間と意味は全く同じです。
- service.sh: 実行時間と意味は全く同じです。
- system.prop：全く同じです。
- sepolicy.rule：全く同じです。
- BusyBox：スクリプトは BusyBox で実行され、どちらの場合も「スタンドアロンモード」が有効です。

## 違うところ

違いを理解する前に、モジュールが ShizuSU で動作しているか Magisk で動作しているかを区別する方法を知っておく必要があります。環境変数 `KSU` を使うとモジュールスクリプトを実行できるすべての場所 (`customize.sh`, `post-fs-data.sh`, `service.sh`) で区別できます。ShizuSU では、この環境変数に `true` が設定されます。

以下は違いです：

- ShizuSU モジュールは、リカバリーモードではインストールできません。
- ShizuSU モジュールには Zygisk のサポートが組み込まれていません（ただし[ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext)を使うと Zygisk モジュールを使用できます）。
- **モジュールマウントアーキテクチャ**：ShizuSU は SukiSU-Ultra ベースで、Magisk と同じく **Magic Mount（bind mount）** を使用します。公式 KernelSU は代わりに OverlayFS metamodule アーキテクチャを使用します。
- ShizuSU モジュールにおけるファイルの置換や削除の方法は Magisk と同じです：`customize.sh` で `REMOVE` と `REPLACE` 変数を使用してファイルを削除したりディレクトリを置き換えます（[モジュールガイド](module.md)を参照）。
- BusyBox 用のディレクトリが違います。ShizuSU の組み込み BusyBox は `/data/adb/ksu/bin/busybox` に、Magisk では `/data/adb/magisk/busybox` に配置されます。**これは ShizuSU の内部動作であり、将来的に変更される可能性があることに注意してください!**
- ShizuSU は `.replace` ファイルをサポートしていません。しかし、ShizuSU はファイルやフォルダを削除したり置き換えたりするための `REMOVE` と `REPLACE` 変数をサポートしています。
- ShizuSU は `boot-completed.sh` スクリプトを追加し、Android システムの起動完了後にタスクを実行できます。
- ShizuSU は `post-mount.sh` スクリプトを追加し、モジュールマウント完了後にタスクを実行できます。
