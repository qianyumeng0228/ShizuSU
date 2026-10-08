# Fitur tersembunyi

## .ksurc

Secara bawaan, `/system/bin/sh` akan memuat `/system/etc/mkshrc`.

Anda dapat membuat `su` memuat berkas rc khusus dengan membuat berkas `/data/adb/ksu/.ksurc`.

## Kustomisasi {#customization}

- **Latar belakang kustom**: Ubah gambar latar belakang di pengaturan Manajer ShizuSU untuk personalisasi antarmuka.
- **Manajemen susfs**: Kelola langsung beberapa fitur susfs di Manajer, tanpa modul susfsforksu tambahan.
- **Penyesuaian DPI**: Sesuaikan tampilan DPI Manajer agar cocok dengan berbagai layar.

## WebUI X {#webui-x}

Mendukung implementasi WebUI generasi baru (WebUI X) oleh MMRL, untuk interaksi modul yang lebih kaya.

## Dukungan Multi-manager {#multi-manager}

ShizuSU menyertakan tabel tanda tangan manajer bawaan, sehingga satu kernel dapat mengenali banyak manajer root sekaligus: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Selain tanda tangan bawaan, juga mendukung registrasi panas dan persistensi (`/data/adb/shizusu/manager`) — tanpa flash ulang kernel saat memasang manajer baru.

## Mode Stealth {#stealth}

Tulis penanda ke `/data/adb/shizusu/stealth` untuk mengaktifkan mode stealth. Saat aktif, laporan info ShizuSU tidak lagi mengekspos status manajer saat ini ke aplikasi, mengurangi risiko deteksi.

## Peningkatan Penyembunyian {#hiding-enhancements}

- **Kanal kueri susfsd**：Kanal komunikasi susfsd bawaan, bekerja langsung dengan patch kernel susfs.
- **Penyembunyian hook KPROBES**：Opsional, default nonaktif.
- **Entri penyembunyian hosts**：Terikat dengan App Profile, dapat menyembunyikan perubahan file hosts oleh modul.
