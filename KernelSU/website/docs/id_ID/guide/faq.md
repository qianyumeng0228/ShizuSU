# FAQ

## Apakah ShizuSU mendukung perangkat saya?

ShizuSU mendukung perangkat yang menjalankan Android dengan bootloader yang tidak terkunci. Namun, dukungan resmi hanya untuk GKI Linux Kernel 5.10+ (dalam praktiknya, ini berarti perangkat Anda harus memiliki Android 12 out-of-the-box agar didukung).

Anda dapat dengan mudah memeriksa dukungan untuk perangkat Anda melalui aplikasi manajer ShizuSU, yang tersedia [di sini](https://github.com/qianyumeng0228/ShizuSU/releases).

Jika aplikasi menunjukkan `Not installed`, berarti perangkat Anda secara resmi didukung oleh ShizuSU.

Jika aplikasi menunjukkan `Unsupported`, berarti perangkat Anda tidak didukung secara resmi saat ini. Namun, Anda dapat membangun kode sumber kernel dan mengintegrasikan ShizuSU untuk membuatnya bekerja, atau gunakan [Perangkat yang didukung tidak resmi](unofficially-support-devices).

## Apakah ShizuSU membutuhkan buka bootloader?

Ya, tentu saja.

## Apakah ShizuSU mendukung modul?

Ya. Sistem modul ShizuSU didasarkan pada Magic Mount (dari SukiSU-Ultra); modul Magisk bekerja langsung, dan modul yang memodifikasi file `/system` tidak memerlukan metamodule. Periksa [Panduan modul](module.md) untuk info lebih lanjut.

## Apakah ShizuSU mendukung Xposed?

Ya, Anda dapat menggunakan LSPosed (atau turunan Xposed modern lainnya) dengan [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext).

## Apakah ShizuSU mendukung Zygisk?

ShizuSU tidak memiliki dukungan Zygisk bawaan, tetapi Anda dapat menggunakan modul seperti [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) untuk mendukungnya.

## Apakah ShizuSU kompatibel dengan Magisk?

Sistem modul ShizuSU bertentangan dengan magic mount Magisk. Jika ada modul yang diaktifkan di ShizuSU, maka seluruh Magisk akan berhenti bekerja.

Namun, jika Anda hanya menggunakan `su` dari ShizuSU, ini akan bekerja dengan baik dengan Magisk. ShizuSU memodifikasi `kernel`, sedangkan Magisk memodifikasi `ramdisk`, memungkinkan keduanya bekerja bersama.

## Akankah ShizuSU menggantikan Magisk?

Kami percaya tidak, dan itu bukan tujuan kami. Magisk sudah cukup baik untuk solusi root userspace dan akan memiliki umur yang panjang. Tujuan ShizuSU adalah untuk menyediakan antarmuka kernel kepada pengguna, bukan untuk menggantikan Magisk.

## Dapatkah ShizuSU mendukung perangkat non-GKI?

Ada kemungkinan. Tetapi Anda harus mengunduh sumber kernel dan mengintegrasikan ShizuSU ke dalam source tree, dan mengkompilasi kernel sendiri.

## Dapatkah ShizuSU mendukung perangkat di bawah Android 12?

Kernel perangkat yang mempengaruhi kompatibilitas ShizuSU, dan tidak ada hubungannya dengan versi Android. Satu-satunya batasan adalah bahwa perangkat yang diluncurkan dengan Android 12 harus memiliki versi kernel 5.10+ (perangkat GKI). Jadi:

1. Perangkat yang diluncurkan dengan Android 12 harus didukung.
2. Perangkat dengan kernel lama (beberapa perangkat dengan Android 12 juga memiliki kernel lama) kompatibel (Anda harus membangun kernel sendiri).

## Dapatkah ShizuSU mendukung kernel lama?

Ada kemungkinan. ShizuSU sekarang telah di-backport ke kernel 4.14. Untuk kernel yang lebih lama, Anda perlu melakukan backport secara manual, dan PR selalu diterima!

## Bagaimana cara mengintegrasikan ShizuSU untuk kernel lama?

Silakan periksa panduan [Integrasi untuk perangkat non-GKI](how-to-integrate-for-non-gki).

## Mengapa versi Android saya 13, dan kernel menunjukkan "android12-5.10"?

Versi kernel tidak ada hubungannya dengan versi Android. Jika Anda perlu mem-flash kernel, selalu gunakan versi kernel; versi Android tidak sepenting itu.

## Saya GKI 1.0, bisakah saya menggunakan ini?

GKI 1.0 sama sekali berbeda dari GKI 2.0, Anda harus mengkompilasi kernel sendiri.

## Bagaimana cara membuat `/system` RW?

Kami tidak merekomendasikan Anda memodifikasi partisi sistem secara langsung. Silakan periksa [Panduan modul](module.md) untuk memodifikasinya secara systemless. Jika Anda bersikeras melakukan ini, periksa [magisk_overlayfs](https://github.com/HuskyDG/magic_overlayfs).

## Bisakah ShizuSU memodifikasi hosts? Bagaimana cara menggunakan AdAway?

Tentu saja. Tetapi ShizuSU tidak memiliki dukungan hosts bawaan, Anda dapat menginstal modul seperti [systemless-hosts](https://github.com/symbuzzer/systemless-hosts-KernelSU-module) untuk melakukannya.

## Mengapa modul saya tidak bekerja setelah instalasi baru?

Silakan periksa: apakah modul diaktifkan, apakah direktori modul berisi `module.prop` yang valid, dan apakah modul kompatibel dengan perangkat/kernel Anda. Sistem modul ShizuSU didasarkan pada Magic Mount; modul yang memodifikasi file `/system` tidak memerlukan metamodule.

**Solusi**: Lihat [Panduan Modul](module.md) dan [Sistem Modul: Magic Mount](metamodule.md).

## Apa sistem modul ShizuSU?

ShizuSU adalah fork generasi kedua dari SukiSU-Ultra; sistem modulnya menggunakan **Magic Mount** (bind mount direktori `system` modul ke `/system`), tanpa metamodule dan kompatibel langsung dengan modul Magisk. Arsitektur OverlayFS metamodule dari KernelSU resmi tidak berlaku untuk ShizuSU. Lihat [Sistem Modul: Magic Mount](metamodule.md).
