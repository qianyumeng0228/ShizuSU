# Sistem Modul: Magic Mount {#introduction}

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
