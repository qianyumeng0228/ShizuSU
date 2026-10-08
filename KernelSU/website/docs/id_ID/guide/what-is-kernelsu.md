# Apa itu ShizuSU?

ShizuSU adalah solusi root untuk perangkat GKI Android, ia bekerja dalam mode kernel dan memberikan izin root ke aplikasi userspace secara langsung di ruang kernel.

## Fitur

Fitur utama dari ShizuSU adalah **berbasis kernel**. ShizuSU bekerja dalam mode kernel, sehingga dapat menyediakan antarmuka kernel yang belum pernah kita miliki sebelumnya. Sebagai contoh, kita dapat menambahkan breakpoint perangkat keras ke proses apa pun dalam mode kernel; Kita dapat mengakses memori fisik dari proses apa pun tanpa diketahui oleh siapa pun; Kita dapat mencegat syscall apa pun di ruang kernel; dll.

Selain itu, ShizuSU adalah fork generasi kedua dari **SukiSU-Ultra**, dan sistem modulnya dibangun di atas **Magic Mount** (dari implementasi Magisk 5ec1cff): direktori `system` modul di-overlay ke `/system` melalui bind mount secara systemless, **tanpa perlu metamodule**. Lihat [Sistem Modul: Magic Mount](metamodule.md).

## Fitur Lanjutan

Selain fondasi root level kernel, ShizuSU membawa sejumlah fitur lanjutan:

- **Dukungan kernel lama / Non-GKI**: ShizuSU mengembalikan dukungan untuk perangkat Non-GKI dan GKI 1.0, mencakup kernel 4.x - 5.4 LTS (3.x eksperimental). Dukungan arsitektur: `arm64-v8a` dukungan penuh, `armeabi-v7a` dukungan dasar, `x86_64` dukungan sebagian.
- **Sistem modul berbasis Magic Mount**: ShizuSU adalah fork dari SukiSU-Ultra; pemasangan modul dibangun di atas teknologi Magic Mount dari 5ec1cff, memberikan fondasi yang lebih stabil dan andal, dan modul Magisk berfungsi langsung. ShizuSU tidak menggunakan arsitektur OverlayFS metamodule dari KernelSU resmi — deskripsi sistem modul di seluruh situs disatukan sebagai Magic Mount.
- **Modul kernel KPM**: Dukungan penuh KernelPatch Module (KPM, di-port dari Apatch) untuk modifikasi dan penguatan tingkat kernel.
- **App Profile**: Kunci izin root dalam lingkungan yang terkontrol melalui profil aplikasi; lihat [App Profile](app-profile.md).
- **Kustomisasi luas**: Latar belakang kustom, kelola langsung beberapa fitur susfs (tanpa modul susfsforksu), sesuaikan DPI... desain sesuai keinginan Anda.

- **Dukungan multi-manager**：Satu kernel mengenali banyak manajer sekaligus melalui tabel tanda tangan bawaan (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), dengan dukungan registrasi panas dan persistensi (`/data/adb/shizusu/manager`) — tidak perlu flash ulang kernel untuk setiap manajer.
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

## Bagaimana cara menggunakannya

Silakan merujuk ke: [Installation](installation)

## Bagaimana cara men-buildnya

[How to build](how-to-build)

## Diskusi

- Telegram: [@KernelSU](https://t.me/KernelSU)
