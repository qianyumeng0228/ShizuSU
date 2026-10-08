# よくある質問

## 私のデバイスは ShizuSU に対応していますか?

まず、お使いのデバイスがブートローダーのロックを解除できる必要があります。もしできないのであれば、サポート外です。

もし ShizuSU アプリで「非対応」と表示されたら、そのデバイスは最初からサポートされていないことになりますが、カーネルソースをビルドして ShizuSU を組み込むか、[非公式の対応デバイス](unofficially-support-devices)で動作させることが可能です。

## ShizuSU を使うにはブートローダーのロックを解除する必要がありますか？

はい。

## ShizuSU はモジュールに対応していますか?

はい。ShizuSU のモジュールシステムは Magic Mount（SukiSU-Ultra 由来）に基づいており、Magisk モジュールは直接動作し、`/system` ファイルを変更するモジュールも metamodule は不要です。詳細は [モジュールガイド](module.md) をご覧ください。

## ShizuSU は Xposed に対応していますか?

はい。[Dreamland](https://github.com/canyie/Dreamland) や [TaiChi](https://taichi.cool) が動作します。LSPosed については、[ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) を使うと動作するようにできます。

## ShizuSU は Zygisk に対応していますか?

ShizuSU は Zygisk サポートを内蔵していません。[ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) を使ってください。

## ShizuSU は Magisk と互換性がありますか?

ShizuSU のモジュールシステムは Magisk のマジックマウントと競合しており、ShizuSU で有効になっているモジュールがある場合、Magisk 全体が動作しなくなります。

しかし、ShizuSU の `su` だけを使うのであれば、Magisk とうまく連携することができます。ShizuSU は `kernel` を、Magisk は `ramdisk` を修正するため、両者は共存できます。

## ShizuSU は Magisk の代わりになりますか？

私たちはそうは思っていませんし、それが目標でもありません。Magisk はユーザ空間の root ソリューションとして十分であり、長く使われ続けるでしょう。ShizuSU の目標は、ユーザーにカーネルインターフェースを提供することであり、Magisk の代用ではありません。

## ShizuSU は GKI 以外のデバイスに対応できますか？

可能です。ただしカーネルソースをダウンロードし、ShizuSU をソースツリーに統合して、自分でカーネルをビルドする必要があります。

## ShizuSU は Android 12 以下のデバイスに対応できますか？

ShizuSU の互換性に影響を与えるのはデバイスのカーネルであり、Android のバージョンとは無関係です。唯一の制限は、Android 12 で発売されたデバイスはカーネル5.10以上（GKI デバイス）でなければならないことです：

1. Android 12 をプリインストールして発売された端末は対応しているはずです。
2. カーネルが古い端末（一部の Android 12 端末はカーネルも古い）は対応可能ですが、カーネルは自分でビルドする必要があります。

## ShizuSU は古いカーネルに対応できますか？

ShizuSU は現在カーネル4.14にバックポートされていますが、それ以前のカーネルについては手動でバックポートする必要があります。プルリクエスト歓迎です！

## 古いカーネルに ShizuSU を組み込むには？

[ガイド](../../guide/how-to-integrate-for-non-gki) を参考にしてください。

## Android のバージョンが13なのに、カーネルは「android12-5.10」と表示されるのはなぜ？

カーネルのバージョンは Android のバージョンと関係ありません。カーネルを書き込む必要がある場合は、常にカーネルのバージョンを使用してください。Android のバージョンはそれほど重要ではありません。

## ShizuSU に-mount-master/global のマウント名前空間はありますか？

今はまだありませんが（将来的にはあるかもしれません）、グローバルマウントの名前空間に手動で切り替える方法は、以下のようにたくさんあります：

1. `nsenter -t 1 -m sh` でシェルをグローバル名前空間にします。
2. `nsenter --mount=/proc/1/ns/mnt` を実行したいコマンドに追加すればグローバル名前空間で実行されます。 ShizuSU は [このような使い方](https://github.com/tiann/KernelSU/blob/77056a710073d7a5f7ee38f9e77c9fd0b3256576/manager/app/src/main/java/me/weishu/kernelsu/ui/util/KsuCli.kt#L115) もできます。

## GKI 1.0 なのですが、使えますか？

GKI1 は GKI2 と全く異なるため、カーネルは自分でビルドする必要があります。

## 新規インストール後にモジュールが動作しないのはなぜですか？

モジュールが有効かどうか、モジュールディレクトリに有効な `module.prop` があるかどうか、モジュールがデバイス/カーネルと互換性があるかどうかを確認してください。ShizuSU のモジュールシステムは Magic Mount ベースであり、`/system` ファイルを変更するモジュールも metamodule は不要です。

**解決策**：[モジュールガイド](module.md) と [モジュールシステム：Magic Mount](metamodule.md) をご覧ください。

## ShizuSU のモジュールシステムとは？

ShizuSU は SukiSU-Ultra の二次開発版であり、モジュールシステムは **Magic Mount**（モジュールの `system` ディレクトリを `/system` に bind mount）を採用し、metamodule は不要で Magisk モジュールと直接互換です。公式 KernelSU の OverlayFS metamodule アーキテクチャは ShizuSU には適用されません。詳しくは[モジュールシステム：Magic Mount](metamodule.md)をご覧ください。
