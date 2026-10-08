# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

# 1) what-is-kernelsu.md：增强特性列表追加 4 条 + 新增「技术来源与融合」小节（锚点 = 各语言“如何使用”标题）
what_is = {
"root": (
"## How to use ShizuSU?",
"""- **Multi-manager support**: One kernel recognizes multiple managers through a built-in signature table (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), with hot registration and persistence (`/data/adb/shizusu/manager`) — no need to reflash the kernel for each manager.
- **Stealth mode**: Just write a flag to `/data/adb/shizusu/stealth`; when enabled, the info report no longer exposes manager status to apps.
- **Module convenience**: Backup / restore of modules and the root allowlist, batch installation that collects failures instead of aborting, and one-tap enable / disable / disable-all / uninstall-all management.
- **Hiding enhancements**: Built-in susfsd query channel, optional KPROBES hook hiding (off by default), and a hosts-hiding entry tied to App Profile.

## Inherited Abilities {#inherited-abilities}

ShizuSU was born by integrating abilities from several mature root solutions and manager ecosystems. Its technical lineage:

| Ability | Source |
|---|---|
| Kernel-level `su` and root authorization management | KernelSU (upstream) |
| Magic Mount module system | Magisk (inherited via MKSU and SukiSU-Ultra) |
| Non-GKI / legacy kernel support | RKSU, SukiSU-Ultra |
| KPM kernel modules | KernelPatch (the APatch implementation) |
| Module management, susfsd, and hiding enhancements | KernelSU-Next |
| Multi-manager signature table | ReSukiSU (reference) |
| Stealth implementation | 7kimisu (reference) |
| Kernel-level hiding patches | susfs |
| APK v2 signature validation | genuine |

"""),
"zh_CN": (
"## 如何使用 {#how-to-use}",
"""- **多管理器支持（Multi-manager）**：一个内核通过内置签名表同时识别多个管理器（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU），支持热注册与持久化（`/data/adb/shizusu/manager`），无需为每个管理器单独刷内核。
- **隐身模式（Stealth）**：在 `/data/adb/shizusu/stealth` 写入标记即可开启；开启后信息报告不再向应用暴露管理器状态。
- **模块管理便利**：支持模块与 root 白名单的备份/恢复、批量安装（收集失败项而不中断）、一键启用/禁用/全部禁用/全部卸载管理。
- **隐藏增强**：内置 susfsd 查询通道、可选的 KPROBES hook 隐藏（默认关闭）、与 App Profile 绑定的 hosts 隐藏入口。

## 技术来源与融合 {#inherited-abilities}

ShizuSU 创立时吸收并整合了多个成熟 root 方案与管理器生态的能力，技术谱系如下：

| 能力 | 来源 |
|---|---|
| 内核级 `su` 与 root 授权管理 | KernelSU（上游项目） |
| Magic Mount 模块系统 | Magisk（经 MKSU 与 SukiSU-Ultra 继承） |
| 非 GKI / 老内核支持 | RKSU、SukiSU-Ultra |
| KPM 内核模块 | KernelPatch（APatch 实现） |
| 模块管理、susfsd 与隐藏增强 | KernelSU-Next |
| 多管理器签名表 | ReSukiSU（参考） |
| 隐身实现 | 7kimisu（参考） |
| 内核级隐藏补丁 | susfs |
| APK v2 签名验证 | genuine |

"""),
"zh_TW": (
"## 如何使用 {#how-to-use}",
"""- **多管理器支援（Multi-manager）**：一個核心透過內建簽名表同時識別多個管理器（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU），支援熱註冊與持久化（`/data/adb/shizusu/manager`），無需為每個管理器單獨刷核心。
- **隱身模式（Stealth）**：在 `/data/adb/shizusu/stealth` 寫入標記即可開啟；開啟後資訊報告不再向應用程式暴露管理器狀態。
- **模組管理便利**：支援模組與 root 白名單的備份/還原、批次安裝（收集失敗項而不中斷）、一鍵啟用/停用/全部停用/全部卸載管理。
- **隱藏增強**：內建 susfsd 查詢通道、可選的 KPROBES hook 隱藏（預設關閉）、與 App Profile 綁定的 hosts 隱藏入口。

## 技術來源與融合 {#inherited-abilities}

ShizuSU 創立時吸收並整合了多個成熟 root 方案與管理器生態的能力，技術譜系如下：

| 能力 | 來源 |
|---|---|
| 核心級 `su` 與 root 授權管理 | KernelSU（上游專案） |
| Magic Mount 模組系統 | Magisk（經 MKSU 與 SukiSU-Ultra 繼承） |
| 非 GKI / 老核心支援 | RKSU、SukiSU-Ultra |
| KPM 核心模組 | KernelPatch（APatch 實作） |
| 模組管理、susfsd 與隱藏增強 | KernelSU-Next |
| 多管理器簽名表 | ReSukiSU（參考） |
| 隱身實作 | 7kimisu（參考） |
| 核心級隱藏修補 | susfs |
| APK v2 簽名驗證 | genuine |

"""),
"ja_JP": (
"## 使用方法",
"""- **マルチマネージャー対応（Multi-manager）**：1つのカーネルが内蔵の署名テーブルで複数のマネージャー（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU）を同時に認識。ホット登録と永続化（`/data/adb/shizusu/manager`）にも対応し、マネージャーごとにカーネルを焼き直す必要はありません。
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

"""),
"vi_VN": (
"## Hướng dẫn sử dụng",
"""- **Hỗ trợ đa trình quản lý (Multi-manager)**：Một kernel nhận diện đồng thời nhiều trình quản lý qua bảng chữ ký tích hợp (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), hỗ trợ đăng ký nóng và lưu trữ bền vững (`/data/adb/shizusu/manager`) — không cần flash lại kernel cho từng trình quản lý.
- **Chế độ ẩn (Stealth)**：Chỉ cần ghi cờ vào `/data/adb/shizusu/stealth`; khi bật, báo cáo thông tin không còn lộ trạng thái trình quản lý cho ứng dụng.
- **Tiện lợi quản lý module**：Sao lưu/khôi phục module và danh sách trắng root, cài đặt hàng loạt (thu thập lỗi mà không dừng), bật/tắt/tắt tất cả/gỡ tất cả chỉ bằng một chạm.
- **Tăng cường ẩn**：Kênh truy vấn susfsd, tùy chọn ẩn hook KPROBES (mặc định tắt), mục ẩn hosts liên kết với App Profile.

## Khả năng kế thừa và tích hợp {#inherited-abilities}

ShizuSU ngay từ khi ra đời đã tích hợp khả năng từ nhiều giải pháp root và hệ sinh thái trình quản lý trưởng thành. Phả hệ kỹ thuật:

| Khả năng | Nguồn |
|---|---|
| `su` cấp kernel và quản lý ủy quyền root | KernelSU (dự án thượng nguồn) |
| Hệ thống module Magic Mount | Magisk (kế thừa qua MKSU và SukiSU-Ultra) |
| Hỗ trợ non-GKI / kernel cũ | RKSU, SukiSU-Ultra |
| Module kernel KPM | KernelPatch (triển khai APatch) |
| Quản lý module, susfsd và ẩn | KernelSU-Next |
| Bảng chữ ký đa trình quản lý | ReSukiSU (tham khảo) |
| Hiện thực ẩn (stealth) | 7kimisu (tham khảo) |
| Bản vá ẩn cấp kernel | susfs |
| Xác thực chữ ký APK v2 | genuine |

"""),
"id_ID": (
"## Bagaimana cara menggunakannya",
"""- **Dukungan multi-manager**：Satu kernel mengenali banyak manajer sekaligus melalui tabel tanda tangan bawaan (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), dengan dukungan registrasi panas dan persistensi (`/data/adb/shizusu/manager`) — tidak perlu flash ulang kernel untuk setiap manajer.
- **Mode Stealth (siluman)**：Cukup tulis penanda ke `/data/adb/shizusu/stealth`; saat aktif, laporan info tidak lagi mengekspos status manajer ke aplikasi.
- **Kenyamanan manajemen modul**：Cadangan/pulihkan modul dan daftar putih root, instalasi massal (mengumpulkan kegagalan tanpa menghentikan), aktif/nonaktif/nonaktifkan semua/hapus semua dalam sekali sentuh.
- **Peningkatan penyembunyian**：Kanal kueri susfsd, penyembunyian hook KPROBES opsional (default nonaktif), entri penyembunyian hosts yang terikat dengan App Profile.

## Kemampuan Warisan dan Integrasi {#inherited-abilities}

Sejak lahir, ShizuSU mengintegrasikan kemampuan dari banyak solusi root dan ekosistem manajer yang matang. Silsilah teknis:

| Kemampuan | Sumber |
|---|---|
| `su` tingkat kernel dan manajemen otorisasi root | KernelSU (proyek hulu) |
| Sistem modul Magic Mount | Magisk (diwarisi melalui MKSU dan SukiSU-Ultra) |
| Dukungan non-GKI / kernel lama | RKSU, SukiSU-Ultra |
| Modul kernel KPM | KernelPatch (implementasi APatch) |
| Manajemen modul, susfsd, dan penyembunyian | KernelSU-Next |
| Tabel tanda tangan multi-manager | ReSukiSU (referensi) |
| Implementasi stealth | 7kimisu (referensi) |
| Patch penyembunyian tingkat kernel | susfs |
| Verifikasi tanda tangan APK v2 | genuine |

"""),
"ru_RU": (
"## Как использовать {#how-to-use}",
"""- **Поддержка нескольких менеджеров (Multi-manager)**：Одно ядро распознаёт несколько менеджеров одновременно через встроенную таблицу подписей (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), с горячей регистрацией и персистентностью (`/data/adb/shizusu/manager`) — не нужно перепрошивать ядро для каждого менеджера.
- **Режим скрытности (Stealth)**：Достаточно записать флаг в `/data/adb/shizusu/stealth`; при включении отчёт о состоянии больше не раскрывает приложениям статус менеджера.
- **Удобство управления модулями**：Резервное копирование/восстановление модулей и белого списка root, пакетная установка (собирает ошибки, не прерываясь), включение/отключение/отключение всех/удаление всех в один тап.
- **Усиление скрытности**：Встроенный канал запросов susfsd, опциональное скрытие хуков KPROBES (выключено по умолчанию), запись скрытия hosts, привязанная к App Profile.

## Наследуемые способности и интеграция {#inherited-abilities}

ShizuSU с момента создания вобрал в себя возможности нескольких зрелых root-решений и экосистем менеджеров. Техническая родословная:

| Возможность | Источник |
|---|---|
| `su` уровня ядра и управление root-правами | KernelSU (вышестоящий проект) |
| Модульная система Magic Mount | Magisk (унаследовано через MKSU и SukiSU-Ultra) |
| Поддержка non-GKI / старых ядер | RKSU, SukiSU-Ultra |
| Ядерные модули KPM | KernelPatch (реализация APatch) |
| Управление модулями, susfsd и скрытность | KernelSU-Next |
| Таблица подписей менеджеров | ReSukiSU (справочно) |
| Реализация stealth | 7kimisu (справочно) |
| Патч скрытности уровня ядра | susfs |
| Проверка подписи APK v2 | genuine |

"""),
"pt_BR": (
"## Como usar o ShizuSU?",
"""- **Suporte multi-manager**：Um kernel reconhece vários gerenciadores ao mesmo tempo através de uma tabela de assinaturas integrada (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), com registro a quente e persistência (`/data/adb/shizusu/manager`) — sem precisar flashar o kernel para cada gerenciador.
- **Modo Stealth (furtividade)**：Basta escrever um sinalizador em `/data/adb/shizusu/stealth`; quando ativado, o relatório de informações não expõe mais o status do gerenciador aos aplicativos.
- **Conveniência de gerenciamento de módulos**：Backup/restauração de módulos e lista de permissões root, instalação em lote (coleta falhas sem interromper), ativar/desativar/desativar todos/desinstalar todos em um toque.
- **Aprimoramentos de ocultação**：Canal de consulta susfsd, ocultação de hooks KPROBES opcional (desligada por padrão), entrada de ocultação de hosts vinculada ao App Profile.

## Capacidades Herdadas e Integração {#inherited-abilities}

Desde seu nascimento, o ShizuSU integrou capacidades de várias soluções root maduras e ecossistemas de gerenciadores. Linhagem técnica:

| Capacidade | Fonte |
|---|---|
| `su` em nível de kernel e gerenciamento de root | KernelSU (projeto upstream) |
| Sistema de módulos Magic Mount | Magisk (herdado via MKSU e SukiSU-Ultra) |
| Suporte a non-GKI / kernels antigos | RKSU, SukiSU-Ultra |
| Módulos de kernel KPM | KernelPatch (implementação do APatch) |
| Gerenciamento de módulos, susfsd e ocultação | KernelSU-Next |
| Tabela de assinaturas multi-manager | ReSukiSU (referência) |
| Implementação stealth | 7kimisu (referência) |
| Patch de ocultação em nível de kernel | susfs |
| Verificação de assinatura APK v2 | genuine |

"""),
}

# 2) hidden-features.md：末尾追加（多管理器 / 隐身 / 隐藏增强）
hidden = {
"root": """
## Multi-manager Support {#multi-manager}

ShizuSU ships with a built-in manager signature table, letting one kernel recognize multiple root managers at the same time: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. In addition to built-in signatures, hot registration and persistence (`/data/adb/shizusu/manager`) are supported — no kernel reflash needed when installing a new manager.

## Stealth Mode {#stealth}

Write a flag to `/data/adb/shizusu/stealth` to enable stealth mode. When enabled, ShizuSU's info report no longer exposes the current manager status to apps, reducing the risk of detection.

## Hiding Enhancements {#hiding-enhancements}

- **susfsd query channel**: built-in susfsd communication channel that works directly with the susfs kernel patches.
- **KPROBES hook hiding**: optional, off by default.
- **hosts hiding entry**: tied to App Profile; can hide module modifications to the hosts file.
""",
"zh_CN": """
## 多管理器支持 {#multi-manager}

ShizuSU 内置一张管理器签名表，让一个内核可以同时识别多个 root 管理器：RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU。除内置签名外，还支持热注册与持久化（`/data/adb/shizusu/manager`），安装新管理器后无需重新刷内核。

## 隐身模式 {#stealth}

在 `/data/adb/shizusu/stealth` 写入标记即可开启隐身模式。开启后，ShizuSU 的信息报告不再向应用暴露当前管理器的状态，降低被检测的风险。

## 隐藏增强 {#hiding-enhancements}

- **susfsd 查询通道**：内置 susfsd 通信通道，可直接与 susfs 内核补丁协同。
- **KPROBES hook 隐藏**：可选的 KPROBES 挂钩隐藏，默认关闭。
- **hosts 隐藏入口**：与 App Profile 绑定，可隐藏模块对 hosts 文件的修改。
""",
"zh_TW": """
## 多管理器支援 {#multi-manager}

ShizuSU 內建一張管理器簽名表，讓一個核心可以同時識別多個 root 管理器：RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU。除內建簽名外，還支援熱註冊與持久化（`/data/adb/shizusu/manager`），安裝新管理器後無需重新刷核心。

## 隱身模式 {#stealth}

在 `/data/adb/shizusu/stealth` 寫入標記即可開啟隱身模式。開啟後，ShizuSU 的資訊報告不再向應用程式暴露目前管理器的狀態，降低被偵測的風險。

## 隱藏增強 {#hiding-enhancements}

- **susfsd 查詢通道**：內建 susfsd 通訊通道，可直接與 susfs 核心修補協同。
- **KPROBES hook 隱藏**：可選的 KPROBES 掛鉤隱藏，預設關閉。
- **hosts 隱藏入口**：與 App Profile 綁定，可隱藏模組對 hosts 檔案的修改。
""",
"ja_JP": """
## マルチマネージャー対応 {#multi-manager}

ShizuSU にはマネージャー署名テーブルが内蔵されており、1つのカーネルで複数の root マネージャー（RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU）を同時に認識できます。内蔵署名に加え、ホット登録と永続化（`/data/adb/shizusu/manager`）にも対応しており、新しいマネージャーをインストールしてもカーネルの焼き直しは不要です。

## ステルスモード {#stealth}

`/data/adb/shizusu/stealth` にフラグを書き込むとステルスモードが有効になります。有効時、ShizuSU の情報レポートはアプリに現在のマネージャー状態を公開しなくなり、検出リスクを低減します。

## 隠蔽の強化 {#hiding-enhancements}

- **susfsd クエリチャネル**：susfsd 通信チャネルを内蔵し、susfs カーネルパッチと直接連携できます。
- **KPROBES フック隠蔽**：オプションの KPROBES フック隠蔽（デフォルトはオフ）。
- **hosts 隠蔽エントリ**：App Profile と連動し、モジュールによる hosts ファイルの変更を隠せます。
""",
"vi_VN": """
## Hỗ trợ đa trình quản lý {#multi-manager}

ShizuSU tích hợp sẵn bảng chữ ký trình quản lý, cho phép một kernel nhận diện đồng thời nhiều trình quản lý root: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Ngoài chữ ký tích hợp, còn hỗ trợ đăng ký nóng và lưu trữ bền vững (`/data/adb/shizusu/manager`) — không cần flash lại kernel khi cài trình quản lý mới.

## Chế độ ẩn {#stealth}

Ghi cờ vào `/data/adb/shizusu/stealth` để bật chế độ ẩn. Khi bật, báo cáo thông tin của ShizuSU không còn lộ trạng thái trình quản lý hiện tại cho ứng dụng, giảm rủi ro bị phát hiện.

## Tăng cường ẩn {#hiding-enhancements}

- **Kênh truy vấn susfsd**：Tích hợp kênh giao tiếp susfsd, phối hợp trực tiếp với bản vá kernel susfs.
- **Ẩn hook KPROBES**：Tùy chọn ẩn hook KPROBES (mặc định tắt).
- **Mục ẩn hosts**：Liên kết với App Profile, có thể ẩn thay đổi tệp hosts của module.
""",
"id_ID": """
## Dukungan Multi-manager {#multi-manager}

ShizuSU menyertakan tabel tanda tangan manajer bawaan, sehingga satu kernel dapat mengenali banyak manajer root sekaligus: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Selain tanda tangan bawaan, juga mendukung registrasi panas dan persistensi (`/data/adb/shizusu/manager`) — tanpa flash ulang kernel saat memasang manajer baru.

## Mode Stealth {#stealth}

Tulis penanda ke `/data/adb/shizusu/stealth` untuk mengaktifkan mode stealth. Saat aktif, laporan info ShizuSU tidak lagi mengekspos status manajer saat ini ke aplikasi, mengurangi risiko deteksi.

## Peningkatan Penyembunyian {#hiding-enhancements}

- **Kanal kueri susfsd**：Kanal komunikasi susfsd bawaan, bekerja langsung dengan patch kernel susfs.
- **Penyembunyian hook KPROBES**：Opsional, default nonaktif.
- **Entri penyembunyian hosts**：Terikat dengan App Profile, dapat menyembunyikan perubahan file hosts oleh modul.
""",
"ru_RU": """
## Поддержка нескольких менеджеров {#multi-manager}

ShizuSU содержит встроенную таблицу подписей менеджеров, позволяя одному ядру одновременно распознавать несколько root-менеджеров: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Помимо встроенных подписей, поддерживаются горячая регистрация и персистентность (`/data/adb/shizusu/manager`) — при установке нового менеджера перепрошивать ядро не нужно.

## Режим скрытности {#stealth}

Запишите флаг в `/data/adb/shizusu/stealth`, чтобы включить режим скрытности. При включении отчёт ShizuSU перестаёт раскрывать приложениям текущий статус менеджера, снижая риск обнаружения.

## Усиление скрытности {#hiding-enhancements}

- **Канал запросов susfsd**：Встроенный канал связи susfsd, работающий напрямую с патчем ядра susfs.
- **Скрытие хуков KPROBES**：Опционально, выключено по умолчанию.
- **Запись скрытия hosts**：Привязана к App Profile, позволяет скрывать изменения файла hosts модулем.
""",
"pt_BR": """
## Suporte Multi-manager {#multi-manager}

O ShizuSU inclui uma tabela de assinaturas de gerenciadores integrada, permitindo que um kernel reconheça vários gerenciadores de root ao mesmo tempo: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Além das assinaturas integradas, também há registro a quente e persistência (`/data/adb/shizusu/manager`) — sem precisar flashar o kernel ao instalar um novo gerenciador.

## Modo Stealth {#stealth}

Escreva um sinalizador em `/data/adb/shizusu/stealth` para ativar o modo stealth. Quando ativado, o relatório de informações do ShizuSU não expõe mais o status atual do gerenciador aos aplicativos, reduzindo o risco de detecção.

## Aprimoramentos de Ocultação {#hiding-enhancements}

- **Canal de consulta susfsd**：Canal de comunicação susfsd integrado, trabalhando diretamente com o patch de kernel susfs.
- **Ocultação de hooks KPROBES**：Opcional, desligada por padrão.
- **Entrada de ocultação de hosts**：Vinculada ao App Profile, pode ocultar alterações no arquivo hosts feitas por módulos.
""",
}

# 3) module.md：末尾追加（模块管理便利）
module_append = {
"root": """
## Module Convenience {#module-convenience}

ShizuSU provides a set of module-management conveniences:

- **Backup & restore**: one-tap backup/restore of installed modules and the root allowlist.
- **Batch installation**: install multiple module zips at once; a single failure does not abort the whole flow — failures are collected and reported at the end.
- **Batch management**: one-tap enable, disable, disable-all, and uninstall-all.
""",
"zh_CN": """
## 模块管理便利 {#module-convenience}

ShizuSU 提供了一系列模块管理上的便利功能：

- **备份与恢复**：一键备份/恢复已安装模块与 root 白名单。
- **批量安装**：一次安装多个模块 zip；单个安装失败不会中断整个流程，失败项会被收集并在结束时汇报。
- **批量管理**：支持一键启用、禁用、全部禁用与全部卸载，方便批量维护模块环境。
""",
"zh_TW": """
## 模組管理便利 {#module-convenience}

ShizuSU 提供了一系列模組管理上的便利功能：

- **備份與還原**：一鍵備份/還原已安裝模組與 root 白名單。
- **批次安裝**：一次安裝多個模組 zip；單一安裝失敗不會中斷整個流程，失敗項會被收集並在結束時回報。
- **批次管理**：支援一鍵啟用、停用、全部停用與全部卸載，方便批次維護模組環境。
""",
"ja_JP": """
## モジュール管理の利便性 {#module-convenience}

ShizuSU はモジュール管理の利便機能を多数提供します：

- **バックアップと復元**：インストール済みモジュールと root ホワイトリストをワンタップでバックアップ/復元。
- **バッチインストール**：複数のモジュール zip を一度にインストール。個別の失敗は中断せず収集され、最後に報告されます。
- **バッチ管理**：ワンタップでの有効化、無効化、すべて無効化、すべてアンインストールに対応。
""",
"vi_VN": """
## Tiện lợi quản lý module {#module-convenience}

ShizuSU cung cấp nhiều tiện ích quản lý module:

- **Sao lưu & khôi phục**：Sao lưu/khôi phục một chạm các module đã cài và danh sách trắng root.
- **Cài đặt hàng loạt**：Cài nhiều zip module cùng lúc; lỗi đơn lẻ không làm dừng quy trình, các lỗi được thu thập và báo cáo khi kết thúc.
- **Quản lý hàng loạt**：Bật, tắt, tắt tất cả, gỡ tất cả chỉ với một chạm.
""",
"id_ID": """
## Kenyamanan Manajemen Modul {#module-convenience}

ShizuSU menyediakan banyak kemudahan manajemen modul:

- **Cadangan & Pulihkan**：Cadangkan/pulihkan modul terpasang dan daftar putih root sekali sentuh.
- **Instalasi massal**：Instal banyak zip modul sekaligus; kegagalan tunggal tidak menghentikan proses, kegagalan dikumpulkan dan dilaporkan di akhir.
- **Manajemen massal**：Aktifkan, nonaktifkan, nonaktifkan semua, hapus semua dalam sekali sentuh.
""",
"ru_RU": """
## Удобство управления модулями {#module-convenience}

ShizuSU предоставляет ряд удобств управления модулями:

- **Резервное копирование и восстановление**：Бэкап/восстановление установленных модулей и белого списка root в один тап.
- **Пакетная установка**：Установка нескольких zip-модулей за раз; сбой одного модуля не прерывает процесс, ошибки собираются и сообщаются в конце.
- **Пакетное управление**：Включение, отключение, отключение всех и удаление всех в один тап.
""",
"pt_BR": """
## Conveniência de Gerenciamento de Módulos {#module-convenience}

O ShizuSU oferece vários recursos de conveniência no gerenciamento de módulos:

- **Backup e restauração**：Backup/restauração de módulos instalados e da lista de permissões root em um toque.
- **Instalação em lote**：Instale vários zips de módulos de uma vez; falhas individuais não interrompem o processo, sendo coletadas e relatadas ao final.
- **Gerenciamento em lote**：Ativar, desativar, desativar todos e desinstalar todos em um toque.
""",
}

for lang, (anchor, block) in what_is.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'guide', 'what-is-kernelsu.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if anchor not in t:
        print('WHAT-IS MISS', lang, anchor[:40])
        continue
    t = t.replace(anchor, block + anchor, 1)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK what-is', lang)

for lang, block in hidden.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'guide', 'hidden-features.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n').rstrip('\n') + '\n' + block
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK hidden', lang)

for lang, block in module_append.items():
    p = os.path.join(ROOT, '' if lang == 'root' else lang, 'guide', 'module.md')
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n').rstrip('\n') + '\n' + block
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK module', lang)
