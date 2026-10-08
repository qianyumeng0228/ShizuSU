# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

pages = {
"root": """# Module System: Magic Mount {#introduction}

ShizuSU is a second-generation development based on **SukiSU-Ultra**, and its module system directly inherits SukiSU-Ultra's **Magic Mount** implementation (derived from 5ec1cff's Magisk implementation).

**Magic Mount is the only module mounting mechanism in ShizuSU** — ShizuSU does not use the official KernelSU OverlayFS metamodule architecture, so there is no coexistence of "two module systems". After installing ShizuSU, modules that modify `/system` files work out of the box — no metamodule installation is required.

## The KernelSU Framework and the Magic Mount Module System {#two-subsystems}

ShizuSU consists of two **parallel** core subsystems with distinct, non-overlapping responsibilities:

| Subsystem | Responsibility | Description |
|---|---|---|
| **KernelSU kernel framework** | Root authorization and management | Runs in kernel space: `su` authorization, whitelist access control, restricted root privileges (uid / gid / groups / capabilities / SELinux), kernel-level interfaces |
| **Magic Mount module system** | Module installation and systemless mounting | Overlays the module's `system` directory onto the system partition via **bind mount**, achieving systemless modification |

The division of labor can be summarized as:

- **The KernelSU framework answers "who gets root"**: the granting, isolation, and auditing of `su` all happen in kernel space and cannot be bypassed from userspace.
- **Magic Mount answers "how modules modify the system"**: the mounting, overlaying, merging, and hiding of module files are handled by Magic Mount without touching physical partitions.

They are in a **parallel relationship**, not a containment one: the KernelSU framework provides root capability, Magic Mount provides module mounting capability, and together they form the complete ShizuSU root experience.

## Why Magic Mount? {#why-magic-mount}

SukiSU-Ultra (and ShizuSU, which inherits it) chose Magic Mount over the official KernelSU's OverlayFS metamodule architecture because:

- **More stable foundation**: Magic Mount originates from Magisk's mature implementation (5ec1cff), long-validated across a large device and module ecosystem.
- **Better module compatibility**: modules in the Magisk ecosystem that rely on `system` directory mounting can be used **directly** — no metamodule, no conversion.
- **Smaller detection surface**: mounting is done via bind mount; ShizuSU itself does not depend on OverlayFS characteristics, making it harder for apps to detect.
- **Simpler deployment**: no need to install metamodules like meta-overlayfs; modules work right after installing ShizuSU.

## How Magic Mount Works {#how-it-works}

Magic Mount uses **bind mount** to "overlay" module content onto system directories:

1. Modules are placed in `/data/adb/modules/<module-id>/`, where the `system/` directory corresponds to the system partition.
2. At boot, ShizuSU iterates over all enabled modules and bind mounts each module's `system/` directory to the corresponding path under `/system`.
3. **Same-name files**: module files override system files.
4. **Same-name directories**: module directories merge with system directories (module files are stacked on top).
5. **Deleting system files**: by mounting the corresponding path from the module directory as an empty directory (whiteout), the system file is "hidden".
6. **Replacing system directories**: by mounting the corresponding path as an empty directory, the whole directory is replaced.

The entire process only reads module and system directories and **does not modify physical partitions** — this is what systemless means.

## Differences from Official KernelSU {#difference}

| | Official KernelSU | ShizuSU (based on SukiSU-Ultra) |
|---|---|---|
| Module mounting mechanism | OverlayFS (requires metamodule, e.g. meta-overlayfs) | **Magic Mount** (built-in, no metamodule needed) |
| Modules modifying `/system` | Require installing a metamodule first | Work directly |
| Magisk module compatibility | Partial (depends on metamodule) | Directly compatible |

::: info Migration notes
If you previously used official KernelSU and installed a metamodule (e.g. meta-overlayfs), you no longer need it after migrating to ShizuSU: just install or use ordinary modules directly.
:::

## Module Development {#module-dev}

ShizuSU's module structure is fully identical to Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, etc.), so Magisk module developers can get started directly. See the [Module Development Guide](module.md) for details.
""",

"zh_TW": """# 模組系統：Magic Mount {#introduction}

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
""",

"ja_JP": """# モジュールシステム：Magic Mount {#introduction}

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
""",

"vi_VN": """# Hệ thống module: Magic Mount {#introduction}

ShizuSU là bản fork thế hệ thứ hai của **SukiSU-Ultra**; hệ thống module của nó kế thừa trực tiếp giải pháp **Magic Mount** của SukiSU-Ultra (từ triển khai Magisk của 5ec1cff).

**Magic Mount là cơ chế mount module duy nhất của ShizuSU** — ShizuSU không sử dụng kiến trúc OverlayFS metamodule của KernelSU chính thức, vì vậy không tồn tại sự song song của "hai hệ thống module". Sau khi cài đặt ShizuSU, các module sửa đổi tệp `/system` hoạt động ngay lập tức mà không cần cài đặt metamodule.

## Khung KernelSU và hệ thống module Magic Mount {#two-subsystems}

ShizuSU gồm hai hệ thống con cốt lõi **song song**, trách nhiệm rõ ràng và không chồng chéo:

| Hệ thống con | Trách nhiệm | Mô tả |
|---|---|---|
| **Khung kernel KernelSU** | Ủy quyền và quản lý root | Chạy trong không gian kernel: ủy quyền `su`, kiểm soát truy cập danh sách trắng, quyền root giới hạn (uid / gid / groups / capabilities / SELinux), giao diện cấp kernel |
| **Hệ thống module Magic Mount** | Cài đặt module và mount systemless | Overlay thư mục `system` của module lên phân vùng hệ thống bằng **bind mount**, đạt được sửa đổi không chạm hệ thống |

Sự phân công có thể tóm tắt:

- **Khung KernelSU trả lời "ai có thể nhận root"**: việc cấp, cô lập và giám sát `su` đều diễn ra trong không gian kernel, không thể bị vượt qua từ không gian người dùng.
- **Magic Mount trả lời "module sửa đổi hệ thống như thế nào"**: việc mount, overlay, gộp và ẩn tệp module do Magic Mount thực hiện, không chạm vào phân vùng vật lý.

Chúng là **quan hệ song song** chứ không phải bao hàm: khung KernelSU đảm nhiệm khả năng root, Magic Mount đảm nhiệm khả năng mount module, cùng nhau tạo nên trải nghiệm root trọn vẹn của ShizuSU.

## Tại sao chọn Magic Mount? {#why-magic-mount}

SukiSU-Ultra (và ShizuSU kế thừa nó) chọn Magic Mount thay vì kiến trúc OverlayFS metamodule của KernelSU chính thức, vì:

- **Nền tảng ổn định hơn**: Magic Mount bắt nguồn từ triển khai trưởng thành của Magisk (5ec1cff), được kiểm chứng lâu dài trên nhiều thiết bị và hệ sinh thái module.
- **Khả năng tương thích module tốt hơn**: các module trong hệ sinh thái Magisk phụ thuộc việc mount thư mục `system` có thể **dùng trực tiếp** — không cần metamodule, không cần chuyển đổi.
- **Bề mặt phát hiện nhỏ hơn**: mount được thực hiện bằng bind mount; ShizuSU không phụ thuộc đặc trưng OverlayFS, khó bị ứng dụng phát hiện hơn.
- **Triển khai đơn giản hơn**: không cần cài thêm meta-overlayfs hay metamodule khác; module hoạt động ngay sau khi cài ShizuSU.

## Cách Magic Mount hoạt động {#how-it-works}

Magic Mount dùng **bind mount** để "overlay" nội dung module lên các thư mục hệ thống:

1. Module được đặt tại `/data/adb/modules/<ID-module>/`, trong đó thư mục `system/` tương ứng với phân vùng hệ thống.
2. Khi khởi động, ShizuSU duyệt tất cả module đã bật và bind mount thư mục `system/` của mỗi module vào đường dẫn tương ứng trong `/system`.
3. **Tệp cùng tên**: tệp module ghi đè tệp hệ thống.
4. **Thư mục cùng tên**: thư mục module gộp với thư mục hệ thống (tệp module nằm ở lớp trên).
5. **Xóa tệp hệ thống**: bằng cách mount đường dẫn tương ứng từ thư mục module như một thư mục rỗng (whiteout), tệp hệ thống bị "ẩn".
6. **Thay thế thư mục hệ thống**: bằng cách mount đường dẫn tương ứng như một thư mục rỗng, thay thế toàn bộ thư mục.

Toàn bộ quá trình chỉ đọc thư mục module và thư mục hệ thống, **không sửa phân vùng vật lý** — đó chính là ý nghĩa của systemless (không chạm hệ thống).

## Khác biệt với KernelSU chính thức {#difference}

| | KernelSU chính thức | ShizuSU (dựa trên SukiSU-Ultra) |
|---|---|---|
| Cơ chế mount module | OverlayFS (cần metamodule, như meta-overlayfs) | **Magic Mount** (tích hợp sẵn, không cần metamodule) |
| Module sửa đổi `/system` | Cần cài metamodule trước | Dùng trực tiếp |
| Tương thích module Magisk | Một phần (phụ thuộc metamodule) | Tương thích trực tiếp |

::: info Ghi chú chuyển đổi
Nếu bạn từng dùng KernelSU chính thức và đã cài metamodule (như meta-overlayfs), sau khi chuyển sang ShizuSU không cần nó nữa: hãy cài và dùng các module thông thường trực tiếp.
:::

## Phát triển module {#module-dev}

Cấu trúc module của ShizuSU hoàn toàn giống Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, v.v.), nhà phát triển module Magisk có thể bắt đầu ngay. Xem [Hướng dẫn phát triển module](module.md) để biết chi tiết.
""",

"id_ID": """# Sistem Modul: Magic Mount {#introduction}

ShizuSU adalah fork generasi kedua dari **SukiSU-Ultra**; sistem modulnya mewarisi langsung solusi **Magic Mount** dari SukiSU-Ultra (dari implementasi Magisk 5ec1cff).

**Magic Mount adalah satu-satunya mekanisme pemasangan modul di ShizuSU** — ShizuSU tidak menggunakan arsitektur OverlayFS metamodule dari KernelSU resmi, sehingga tidak ada "dua sistem modul" yang berdampingan. Setelah menginstal ShizuSU, modul yang memodifikasi file `/system` langsung berfungsi tanpa perlu menginstal metamodule.

## Kerangka KernelSU dan Sistem Modul Magic Mount {#two-subsystems}

ShizuSU terdiri dari dua subsistem inti yang **berdampingan**, dengan tanggung jawab yang jelas dan tidak tumpang tindih:

| Subsistem | Tanggung jawab | Deskripsi |
|---|---|---|
| **Kerangka kernel KernelSU** | Otorisasi dan manajemen root | Berjalan di ruang kernel: otorisasi `su`, kontrol akses daftar putih, hak root terbatas (uid / gid / groups / capabilities / SELinux), antarmuka tingkat kernel |
| **Sistem modul Magic Mount** | Instalasi modul dan mount systemless | Menimpa direktori `system` modul ke partisi sistem dengan **bind mount**, mewujudkan modifikasi tanpa sistem |

Pembagian kerja dapat diringkas:

- **Kerangka KernelSU menjawab "siapa yang bisa mendapatkan root"**: pemberian, isolasi, dan audit `su` semuanya terjadi di ruang kernel dan tidak dapat dilewati dari ruang pengguna.
- **Magic Mount menjawab "bagaimana modul memodifikasi sistem"**: pemasangan, penimpaan, penggabungan, dan penyembunyian file modul ditangani oleh Magic Mount tanpa menyentuh partisi fisik.

Keduanya dalam **hubungan berdampingan**, bukan berlapis: kerangka KernelSU menyediakan kemampuan root, Magic Mount menyediakan kemampuan pemasangan modul, bersama-sama membentuk pengalaman root lengkap ShizuSU.

## Mengapa Magic Mount? {#why-magic-mount}

SukiSU-Ultra (dan ShizuSU yang mewarisinya) memilih Magic Mount alih-alih arsitektur OverlayFS metamodule KernelSU resmi karena:

- **Fondasi lebih stabil**: Magic Mount berasal dari implementasi Magisk yang matang (5ec1cff), teruji lama di banyak perangkat dan ekosistem modul.
- **Kompatibilitas modul lebih baik**: modul di ekosistem Magisk yang bergantung pada pemasangan direktori `system` dapat **langsung digunakan** — tanpa metamodule, tanpa konversi.
- **Permukaan deteksi lebih kecil**: pemasangan dilakukan dengan bind mount; ShizuSU tidak bergantung pada ciri OverlayFS, sehingga lebih sulit dideteksi aplikasi.
- **Deploy lebih sederhana**: tidak perlu menginstal metamodule seperti meta-overlayfs; modul langsung berfungsi setelah menginstal ShizuSU.

## Cara Kerja Magic Mount {#how-it-works}

Magic Mount menggunakan **bind mount** untuk "menimpa" konten modul ke direktori sistem:

1. Modul ditempatkan di `/data/adb/modules/<ID-modul>/`, dengan direktori `system/` di dalamnya berkorespondensi dengan partisi sistem.
2. Saat boot, ShizuSU memindai semua modul yang diaktifkan dan bind mount direktori `system/` setiap modul ke jalur yang sesuai di `/system`.
3. **File senama**: file modul menimpa file sistem.
4. **Direktori senama**: direktori modul digabung dengan direktori sistem (file modul menumpuk di lapisan atas).
5. **Menghapus file sistem**: dengan me-mount jalur yang sesuai dari direktori modul sebagai direktori kosong (whiteout), file sistem "disembunyikan".
6. **Mengganti direktori sistem**: dengan me-mount jalur yang sesuai sebagai direktori kosong, seluruh direktori diganti.

Seluruh proses hanya membaca direktori modul dan direktori sistem, **tidak mengubah partisi fisik** — inilah arti systemless (tanpa modifikasi sistem).

## Perbedaan dengan KernelSU Resmi {#difference}

| | KernelSU resmi | ShizuSU (berbasis SukiSU-Ultra) |
|---|---|---|
| Mekanisme pemasangan modul | OverlayFS (perlu metamodule, seperti meta-overlayfs) | **Magic Mount** (bawaan, tanpa metamodule) |
| Modul yang memodifikasi `/system` | Perlu menginstal metamodule dulu | Langsung berfungsi |
| Kompatibilitas modul Magisk | Sebagian (bergantung metamodule) | Kompatibel langsung |

::: info Catatan migrasi
Jika Anda sebelumnya menggunakan KernelSU resmi dan menginstal metamodule (seperti meta-overlayfs), setelah bermigrasi ke ShizuSU tidak diperlukan lagi: cukup instal dan gunakan modul biasa secara langsung.
:::

## Pengembangan Modul {#module-dev}

Struktur modul ShizuSU sepenuhnya sama dengan Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, dll.), sehingga pengembang modul Magisk dapat langsung memulai. Lihat [Panduan Pengembangan Modul](module.md) untuk detail.
""",

"ru_RU": """# Модульная система: Magic Mount {#introduction}

ShizuSU — форк второго поколения **SukiSU-Ultra**; его модульная система напрямую наследует решение **Magic Mount** от SukiSU-Ultra (из реализации Magisk от 5ec1cff).

**Magic Mount — единственный механизм монтирования модулей в ShizuSU**: ShizuSU не использует архитектуру OverlayFS metamodule официального KernelSU, поэтому не существует сосуществования «двух модульных систем». После установки ShizuSU модули, изменяющие файлы `/system`, работают из коробки — устанавливать metamodule не требуется.

## Каркас KernelSU и модульная система Magic Mount {#two-subsystems}

ShizuSU состоит из двух **параллельных** основных подсистем с чёткими, непересекающимися обязанностями:

| Подсистема | Обязанность | Описание |
|---|---|---|
| **Каркас ядра KernelSU** | Авторизация и управление root | Работает в пространстве ядра: авторизация `su`, контроль доступа по белому списку, ограниченные root-права (uid / gid / groups / capabilities / SELinux), интерфейсы уровня ядра |
| **Модульная система Magic Mount** | Установка модулей и systemless-монтирование | Накладывает каталог `system` модуля на системный раздел через **bind mount**, обеспечивая модификацию без изменения системы |

Разделение труда можно резюмировать так:

- **Каркас KernelSU отвечает на вопрос «кому доступен root»**: выдача, изоляция и аудит `su` происходят в пространстве ядра и не могут быть обойдены из пользовательского пространства.
- **Magic Mount отвечает на вопрос «как модули изменяют систему»**: монтирование, наложение, объединение и скрытие файлов модулей выполняет Magic Mount, не касаясь физических разделов.

Они находятся в **параллельных отношениях**, а не во вложенных: каркас KernelSU обеспечивает root-возможности, Magic Mount — возможности монтирования модулей, и вместе они формируют полный root-опыт ShizuSU.

## Почему Magic Mount? {#why-magic-mount}

SukiSU-Ultra (и наследующий его ShizuSU) выбрал Magic Mount вместо архитектуры OverlayFS metamodule официального KernelSU, потому что:

- **Более стабильная основа**: Magic Mount происходит из зрелой реализации Magisk (5ec1cff), долго проверенной на множестве устройств и в экосистеме модулей.
- **Лучшая совместимость модулей**: модули экосистемы Magisk, зависящие от монтирования каталога `system`, можно использовать **напрямую** — без metamodule и без конвертации.
- **Меньше поверхность обнаружения**: монтирование выполняется через bind mount; сам ShizuSU не зависит от особенностей OverlayFS, поэтому приложениям сложнее его детектировать.
- **Проще развертывание**: не нужно устанавливать meta-overlayfs и другие metamodule — модули работают сразу после установки ShizuSU.

## Как работает Magic Mount {#how-it-works}

Magic Mount использует **bind mount** для «наложения» содержимого модуля на системные каталоги:

1. Модули размещаются в `/data/adb/modules/<ID-модуля>/`, где каталог `system/` соответствует системному разделу.
2. При загрузке ShizuSU обходит все включённые модули и bind mount каталог `system/` каждого модуля в соответствующий путь внутри `/system`.
3. **Файлы с одинаковыми именами**: файлы модуля переопределяют системные файлы.
4. **Каталоги с одинаковыми именами**: каталог модуля объединяется с системным каталогом (файлы модуля накладываются сверху).
5. **Удаление системных файлов**: монтируя соответствующий путь из каталога модуля как пустой каталог (whiteout), системный файл «скрывается».
6. **Замена системных каталогов**: монтируя соответствующий путь как пустой каталог, заменяется весь каталог.

Весь процесс лишь читает каталоги модулей и системные каталоги и **не изменяет физические разделы** — в этом и заключается смысл systemless (без изменения системы).

## Отличия от официального KernelSU {#difference}

| | Официальный KernelSU | ShizuSU (на основе SukiSU-Ultra) |
|---|---|---|
| Механизм монтирования модулей | OverlayFS (требуется metamodule, например meta-overlayfs) | **Magic Mount** (встроенный, metamodule не нужен) |
| Модули, изменяющие `/system` | Требуется сначала установить metamodule | Работают напрямую |
| Совместимость с модулями Magisk | Частичная (зависит от metamodule) | Прямая совместимость |

::: info Примечание о миграции
Если вы использовали официальный KernelSU и установили metamodule (например, meta-overlayfs), после перехода на ShizuSU он больше не нужен: просто устанавливайте и используйте обычные модули напрямую.
:::

## Разработка модулей {#module-dev}

Структура модулей ShizuSU полностью совпадает с Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh` и т.д.), поэтому разработчики модулей Magisk могут начать сразу. Подробнее см. [Руководство по разработке модулей](module.md).
""",

"pt_BR": """# Sistema de Módulos: Magic Mount {#introduction}

O ShizuSU é um fork de segunda geração do **SukiSU-Ultra**; seu sistema de módulos herda diretamente a solução **Magic Mount** do SukiSU-Ultra (da implementação do Magisk por 5ec1cff).

**Magic Mount é o único mecanismo de montagem de módulos do ShizuSU** — o ShizuSU não usa a arquitetura OverlayFS metamodule do KernelSU oficial, portanto não existe coexistência de "dois sistemas de módulos". Após instalar o ShizuSU, módulos que modificam arquivos `/system` funcionam imediatamente, sem necessidade de instalar metamodule.

## O Framework KernelSU e o Sistema de Módulos Magic Mount {#two-subsystems}

O ShizuSU consiste em dois subsistemas centrais **paralelos**, com responsabilidades claras e não sobrepostas:

| Subsistema | Responsabilidade | Descrição |
|---|---|---|
| **Framework de kernel KernelSU** | Autorização e gerenciamento de root | Roda no espaço do kernel: autorização `su`, controle de acesso por lista de permissões, privilégios root restritos (uid / gid / groups / capabilities / SELinux), interfaces de nível de kernel |
| **Sistema de módulos Magic Mount** | Instalação de módulos e montagem systemless | Sobrepoõe o diretório `system` do módulo à partição do sistema via **bind mount**, alcançando modificação sem tocar no sistema |

A divisão de trabalho pode ser resumida:

- **O framework KernelSU responde "quem pode obter root"**: a concessão, o isolamento e a auditoria de `su` acontecem no espaço do kernel e não podem ser contornados do espaço do usuário.
- **Magic Mount responde "como os módulos modificam o sistema"**: a montagem, sobreposição, mesclagem e ocultação de arquivos de módulos são feitas pelo Magic Mount, sem tocar em partições físicas.

Eles estão em **relação paralela**, não de contenção: o framework KernelSU fornece a capacidade de root, o Magic Mount fornece a capacidade de montagem de módulos, e juntos formam a experiência de root completa do ShizuSU.

## Por que Magic Mount? {#why-magic-mount}

O SukiSU-Ultra (e o ShizuSU, que o herda) escolheu o Magic Mount em vez da arquitetura OverlayFS metamodule do KernelSU oficial porque:

- **Base mais estável**: o Magic Mount vem da implementação madura do Magisk (5ec1cff), validada por muito tempo em muitos dispositivos e no ecossistema de módulos.
- **Melhor compatibilidade de módulos**: módulos do ecossistema Magisk que dependem da montagem do diretório `system` podem ser usados **diretamente** — sem metamodule, sem conversão.
- **Menor superfície de detecção**: a montagem é feita via bind mount; o ShizuSU não depende de características do OverlayFS, sendo mais difícil de ser detectado por apps.
- **Implantação mais simples**: não é necessário instalar meta-overlayfs ou outros metamodules; os módulos funcionam logo após a instalação do ShizuSU.

## Como o Magic Mount Funciona {#how-it-works}

O Magic Mount usa **bind mount** para "sobrepor" o conteúdo dos módulos aos diretórios do sistema:

1. Os módulos ficam em `/data/adb/modules/<ID-do-módulo>/`, onde o diretório `system/` corresponde à partição do sistema.
2. Na inicialização, o ShizuSU percorre todos os módulos habilitados e faz bind mount do diretório `system/` de cada módulo no caminho correspondente dentro de `/system`.
3. **Arquivos de mesmo nome**: os arquivos do módulo sobrescrevem os arquivos do sistema.
4. **Diretórios de mesmo nome**: o diretório do módulo é mesclado com o diretório do sistema (arquivos do módulo ficam na camada superior).
5. **Excluir arquivos do sistema**: montando o caminho correspondente do diretório do módulo como um diretório vazio (whiteout), o arquivo do sistema fica "oculto".
6. **Substituir diretórios do sistema**: montando o caminho correspondente como um diretório vazio, todo o diretório é substituído.

Todo o processo apenas lê os diretórios dos módulos e do sistema e **não modifica partições físicas** — esse é o significado de systemless (sem modificação do sistema).

## Diferenças do KernelSU Oficial {#difference}

| | KernelSU oficial | ShizuSU (baseado no SukiSU-Ultra) |
|---|---|---|
| Mecanismo de montagem de módulos | OverlayFS (requer metamodule, como meta-overlayfs) | **Magic Mount** (embutido, sem metamodule) |
| Módulos que modificam `/system` | Requerem instalar metamodule antes | Funcionam diretamente |
| Compatibilidade com módulos Magisk | Parcial (depende de metamodule) | Compatibilidade direta |

::: info Nota de migração
Se você usava o KernelSU oficial e instalou um metamodule (como meta-overlayfs), após migrar para o ShizuSU ele não é mais necessário: basta instalar e usar módulos comuns diretamente.
:::

## Desenvolvimento de Módulos {#module-dev}

A estrutura de módulos do ShizuSU é totalmente idêntica à do Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, etc.), então desenvolvedores de módulos Magisk podem começar imediatamente. Veja o [Guia de Desenvolvimento de Módulos](module.md) para detalhes.
""",
}

for lang, body in pages.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'guide', 'metamodule.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    # 保留 frontmatter（--- ... ---）
    lines = t.split('\n')
    if lines and lines[0].strip() == '---':
        end = 1
        for i in range(1, len(lines)):
            if lines[i].strip() == '---':
                end = i
                break
        fm = '\n'.join(lines[:end + 1])
    else:
        fm = ''
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write((fm + '\n\n' if fm else '') + body.strip() + '\n')
    print('OK', lang, '| fm lines:', fm.count('\n') + 1 if fm else 0)
