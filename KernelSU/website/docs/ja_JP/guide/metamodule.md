# モジュールシステム：Magic Mount {#introduction}

ShizuSU は **SukiSU-Ultra** の二次開発版であり、そのモジュールシステムは SukiSU-Ultra の **Magic Mount** 方式（5ec1cff の Magisk 実装由来）を直接継承しています。

**Magic Mount は ShizuSU における唯一のモジュールマウント機構です** — ShizuSU は公式 KernelSU の OverlayFS metamodule アーキテクチャを使用しないため、「2つのモジュールシステム」の併存は存在しません。ShizuSU をインストールすると、`/system` ファイルを変更するモジュールは追加の metamodule なしでそのまま動作します。

## KernelSU フレームワークと Magic Mount モジュールシステム {#two-subsystems}

ShizuSU は**並列**の2つの中核サブシステムで構成され、それぞれの責務は明確で重複しません：

| サブシステム | 責務 | 説明 |
|---|---|---|
| **KernelSU カーネルフレームワーク** | root 認可と管理 | カーネル空間で動作：`su` 認可、ホワイトリストアクセス制御、制限付き root 権限（uid / gid / groups / capabilities / SELinux）、カーネルレベルインターフェース |
| **Magic Mount モジュールシステム** | モジュールのインストールと systemless マウント | モジュールの `system` ディレクトリを **bind mount** でシステムパーティションに重ね合わせ、システム無改変を実現 |

役割分担は次のようにまとめられます：

- **KernelSU フレームワークは「誰が root を得られるか」に答える**：`su` の付与・隔離・監査はすべてカーネル空間で行われ、ユーザー空間からは迂回できません。
- **Magic Mount は「モジュールがどのようにシステムを変更するか」に答える**：モジュールファイルのマウント・オーバーレイ・統合・非表示は Magic Mount が行い、物理パーティションには触れません。

両者は**包含関係ではなく並列関係**です：KernelSU フレームワークが root 機能を、Magic Mount がモジュールマウント機能を担い、合わせて ShizuSU の完全な root 体験を構成します。

## なぜ Magic Mount なのか？ {#why-magic-mount}

SukiSU-Ultra（およびそれを継承する ShizuSU）が公式 KernelSU の OverlayFS metamodule アーキテクチャではなく Magic Mount を選んだ理由：

- **より安定した基盤**：Magic Mount は Magisk の成熟した実装（5ec1cff）に由来し、多くのデバイスとモジュールエコシステムで長期検証されています。
- **優れたモジュール互換性**：`system` ディレクトリのマウントに依存する Magisk エコシステムのモジュールを**直接**利用できます。metamodule も変換も不要です。
- **検出面が少ない**：マウントは bind mount で行われ、ShizuSU 自体は OverlayFS の特性に依存しないため、アプリによる検出がより困難です。
- **導入が簡単**：meta-overlayfs などの metamodule を追加インストールする必要がなく、ShizuSU インストール後すぐにモジュールが動作します。

## Magic Mount の仕組み {#how-it-works}

Magic Mount は **bind mount**（バインドマウント）でモジュール内容をシステムディレクトリに「重ね合わせ」ます：

1. モジュールは `/data/adb/modules/<モジュールID>/` に配置され、その中の `system/` ディレクトリがシステムパーティションに対応します。
2. 起動時に ShizuSU は有効な全モジュールを走査し、各モジュールの `system/` ディレクトリを `/system` の対応パスに bind mount します。
3. **同名ファイル**：モジュールのファイルがシステムのファイルを上書きします。
4. **同名ディレクトリ**：モジュールのディレクトリはシステムのディレクトリと統合されます（モジュールファイルが上層に重なります）。
5. **システムファイルの削除**：モジュールディレクトリの対応パスを空ディレクトリとしてマウント（whiteout）することで、システムファイルを「非表示」にします。
6. **システムディレクトリの置き換え**：対応パスを空ディレクトリとしてマウントすることで、ディレクトリ全体を置き換えます。

このプロセス全体はモジュールディレクトリとシステムディレクトリを読むだけで、**物理パーティションを変更しません** — これが systemless（システム無改変）の意味です。

## 公式 KernelSU との違い {#difference}

| | 公式 KernelSU | ShizuSU（SukiSU-Ultra ベース） |
|---|---|---|
| モジュールマウント機構 | OverlayFS（metamodule が必要、例：meta-overlayfs） | **Magic Mount**（内蔵、metamodule 不要） |
| `/system` を変更するモジュール | 事前に metamodule のインストールが必要 | 直接動作 |
| Magisk モジュール互換性 | 部分的（metamodule 依存） | 直接互換 |

::: info 移行について
公式 KernelSU で metamodule（例：meta-overlayfs）をインストールしていた場合、ShizuSU への移行後は不要になります：通常のモジュールを直接インストール・利用してください。
:::

## モジュール開発 {#module-dev}

ShizuSU のモジュール構造は Magisk と完全に同じです（`module.prop`、`system/`、`post-fs-data.sh`、`service.sh` など）。Magisk モジュール開発者はそのまま始められます。詳しくは[モジュール開発ガイド](module.md)をご覧ください。
