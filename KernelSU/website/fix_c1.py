# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

pairs = [
# ============ vi_VN ============
("vi_VN/index.md",
"""  - title: Hệ thống Metamodule
    details: Cơ sở hạ tầng module có thể cắm cho phép sửa đổi /system theo cách systemless. Cài đặt metamodule như meta-overlayfs để bật tính năng mount module.""",
"""  - title: Hệ thống module Magic Mount
    details: Dựa trên Magic Mount (5ec1cff) kế thừa từ SukiSU-Ultra, mount module hoạt động ngay lập tức và tương thích trực tiếp với module Magisk."""),
("vi_VN/index.md",
"""  - title: Hệ thống module dựa trên Magic Mount
    details: Được xây dựng trên công nghệ Magic Mount của 5ec1cff, mang lại nền tảng mount module ổn định và đáng tin cậy hơn.""",
"""  - title: Dựa trên SukiSU-Ultra
    details: Fork thế hệ thứ hai của một dự án cộng đồng trưởng thành, kế thừa hỗ trợ Non-GKI, Magic Mount, KPM và hơn thế nữa."""),
("vi_VN/guide/what-is-kernelsu.md",
"Ngoài ra, ShizuSU cung cấp [hệ thống metamodule](metamodule.md), đây là một kiến trúc có thể cắm để quản lý module. Không giống như các giải pháp root truyền thống tích hợp logic mount vào lõi, ShizuSU ủy thác điều này cho metamodules. Điều này cho phép bạn cài đặt metamodules (như [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs)) để cung cấp các sửa đổi systemless cho phân vùng `/system` và các phân vùng khác.",
"Ngoài ra, ShizuSU là bản fork thế hệ thứ hai của **SukiSU-Ultra**, hệ thống module được xây dựng trên **Magic Mount** (từ triển khai Magisk của 5ec1cff): thư mục `system` của module được overlay lên `/system` bằng bind mount theo cách systemless, **không cần cài đặt metamodule**. Xem [Hệ thống module: Magic Mount](metamodule.md)."),
("vi_VN/guide/what-is-kernelsu.md",
"- **Hệ thống module dựa trên Magic Mount**: Việc mount module được xây dựng trên công nghệ Magic Mount của 5ec1cff, mang lại nền tảng ổn định và đáng tin cậy hơn, đồng thời vẫn giữ kiến trúc [metamodule](metamodule.md) có thể cắm.",
"- **Hệ thống module dựa trên Magic Mount**: ShizuSU là bản fork của SukiSU-Ultra; việc mount module được xây dựng trên công nghệ Magic Mount của 5ec1cff, mang lại nền tảng ổn định và đáng tin cậy hơn, và các module Magisk hoạt động trực tiếp. ShizuSU không sử dụng kiến trúc OverlayFS metamodule của KernelSU chính thức — mô tả hệ thống module trên toàn trang được thống nhất là Magic Mount."),
("vi_VN/guide/module.md",
"""::: warning METAMODULE CHỈ CẦN THIẾT ĐỂ SỬA ĐỔI TỆP HỆ THỐNG
ShizuSU sử dụng kiến trúc [metamodule](metamodule.md) để mount thư mục `system`. **Chỉ khi module của bạn cần sửa đổi tệp `/system`** (thông qua thư mục `system`), bạn mới cần cài đặt metamodule (như [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)). Các tính năng module khác như scripts, quy tắc sepolicy và system.prop hoạt động mà không cần metamodule.
:::""",
"""::: info HỆ THỐNG MODULE DỰA TRÊN MAGIC MOUNT
Hệ thống module của ShizuSU dựa trên **Magic Mount** (từ SukiSU-Ultra), không cần cài đặt metamodule để mount thư mục `system`. Các module sửa đổi tệp `/system` hoạt động ngay lập tức. Xem [Hệ thống module: Magic Mount](metamodule.md).
:::"""),
("vi_VN/guide/module.md",
"""### thư mục `system`

Nội dung của thư mục này sẽ được phủ lên trên phân vùng /system của hệ thống sau khi hệ thống được khởi động. Điều này có nghĩa rằng:

::: tip YÊU CẦU METAMODULE
Thư mục `system` chỉ được mount nếu bạn đã cài đặt metamodule cung cấp chức năng mounting (như `meta-overlayfs`). Metamodule xử lý cách các module được mount. Xem [Hướng dẫn Metamodule](metamodule.md) để biết thêm thông tin.
:::

1. Các file có cùng tên với các file trong thư mục tương ứng trong hệ thống sẽ bị ghi đè bởi các file trong thư mục này.
2. Các thư mục có cùng tên với thư mục tương ứng trong hệ thống sẽ được gộp với các thư mục trong thư mục này.

Nếu bạn muốn xóa một tập tin hoặc thư mục trong thư mục hệ thống gốc, bạn cần tạo một tập tin có cùng tên với tập tin/thư mục trong thư mục mô-đun bằng cách sử dụng `mknod filename c 0 0`. Bằng cách này, hệ thống lớp phủ sẽ tự động "whiteout" (Xóa trắng) tệp này như thể nó đã bị xóa (phân vùng /system không thực sự bị thay đổi).

Bạn cũng có thể khai báo một biến có tên `REMOVE` chứa danh sách các thư mục trong `customize.sh` để thực hiện các thao tác xóa và ShizuSU sẽ tự động thực thi `mknod <TARGET> c 0 0` trong các thư mục tương ứng của mô-đun. Ví dụ:

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

Danh sách trên sẽ thực thi `mknod $MODPATH/system/app/YouTuBe c 0 0` và `mknod $MODPATH/system/app/Bloatware c 0 0`; và `/system/app/YouTube` và `/system/app/Bloatware` sẽ bị xóa sau khi mô-đun này có hiệu lực.

Nếu bạn muốn thay thế một thư mục trong hệ thống, bạn cần tạo một thư mục có cùng đường dẫn trong thư mục mô-đun của mình, sau đó đặt thuộc tính `setfattr -ntrust.overlay.opaque -v y <TARGET>` cho thư mục này. Bằng cách này, hệ thống Overlayfs sẽ tự động thay thế thư mục tương ứng trong hệ thống (mà không thay đổi phân vùng /system).

Bạn có thể khai báo một biến có tên `REPLACE` trong tệp `customize.sh` của mình, bao gồm danh sách các thư mục sẽ được thay thế và ShizuSU sẽ tự động thực hiện các thao tác tương ứng trong thư mục mô-đun của bạn. Ví dụ:

REPLACE="
/system/app/YouTube
/system/app/Bloatware
"

Danh sách này sẽ tự động tạo các thư mục `$MODPATH/system/app/YouTube` và `$MODPATH/system/app/Bloatware`, sau đó thực thi `setfattr -ntrusted.overlay.opaque -v y $MODPATH/system/app/ YouTube` và `setfattr -n Trust.overlay.opaque -v y $MODPATH/system/app/Bloatware`. Sau khi mô-đun có hiệu lực, `/system/app/YouTube` và `/system/app/Bloatware` sẽ được thay thế bằng các thư mục trống.

::: tip sự khác biệt với Magisk

ShizuSU sử dụng kiến trúc [metamodule](metamodule.md) trong đó việc mounting được ủy thác cho các metamodule có thể cắm được. Metamodule `meta-overlayfs` chính thức sử dụng OverlayFS của kernel cho các sửa đổi systemless, trong khi Magisk sử dụng magic mount (bind mount) được tích hợp trực tiếp vào lõi của nó. Cả hai đều đạt được cùng một mục tiêu: sửa đổi tệp `/system` mà không sửa đổi vật lý phân vùng `/system`. Cách tiếp cận của ShizuSU mang lại tính linh hoạt cao hơn và giảm bề mặt phát hiện.
:::

Nếu bạn quan tâm đến overlayfs, bạn nên đọc [documentation on overlayfs](https://docs.kernel.org/filesystems/overlayfs.html) của Kernel Linux.""",
"""### thư mục `system`

Nội dung của thư mục này sẽ được phủ lên trên phân vùng /system của hệ thống bằng **Magic Mount (bind mount)** sau khi hệ thống được khởi động. Điều này có nghĩa rằng:

1. Các file có cùng tên với các file trong thư mục tương ứng trong hệ thống sẽ bị ghi đè bởi các file trong thư mục này.
2. Các thư mục có cùng tên với thư mục tương ứng trong hệ thống sẽ được gộp với các thư mục trong thư mục này.

Nếu bạn muốn xóa một tập tin hoặc thư mục trong thư mục hệ thống gốc, bạn có thể khai báo một biến có tên `REMOVE` chứa danh sách các thư mục trong `customize.sh`. ShizuSU sẽ ẩn các tệp này bằng Magic Mount (phân vùng /system không thực sự bị thay đổi). Ví dụ:

```sh
REMOVE="
/system/app/YouTube
/system/app/Bloatware
"
```

`/system/app/YouTube` và `/system/app/Bloatware` sẽ bị ẩn sau khi mô-đun có hiệu lực.

Nếu bạn muốn thay thế một thư mục trong hệ thống, bạn có thể khai báo một biến có tên `REPLACE` trong tệp `customize.sh` của mình. ShizuSU sẽ bind mount một thư mục trống lên đường dẫn đích, thay thế toàn bộ thư mục. Ví dụ:

REPLACE="
/system/app/YouTube
/system/app/Bloatware
"

`/system/app/YouTube` và `/system/app/Bloatware` sẽ được thay thế bằng các thư mục trống sau khi mô-đun có hiệu lực.

::: tip Khác biệt với KernelSU chính thức

KernelSU chính thức sử dụng cơ chế OverlayFS (metamodule) để thực hiện systemless, còn ShizuSU dựa trên SukiSU-Ultra, giống Magisk sử dụng **Magic Mount (bind mount)**. Cả hai đều đạt được cùng một mục tiêu: sửa đổi tệp `/system` mà không sửa đổi vật lý phân vùng `/system`. ShizuSU chỉ sử dụng Magic Mount — không tồn tại hai hệ thống module. Xem [Hệ thống module: Magic Mount](metamodule.md).
:::"""),
("vi_VN/guide/module.md",
"| Gắn kết OverlayFS (metamodule) | Có | Có |",
"| Gắn kết module Magic Mount | Có | Có |"),
("vi_VN/guide/module.md",
"  5. Thực thi tập lệnh mount của metamodule (OverlayFS)",
"  5. Thực thi gắn kết module Magic Mount"),
("vi_VN/guide/module.md",
"Tập lệnh này chạy trước khi gắn kết OverlayFS, tương tự như `post-fs-data.sh` trong luồng tiêu chuẩn.",
"Tập lệnh này chạy trước khi gắn kết module, tương tự như `post-fs-data.sh` trong luồng tiêu chuẩn."),
("vi_VN/guide/faq.md",
"Có, hầu hết các module Magisk hoạt động trên ShizuSU. Tuy nhiên, nếu module của bạn cần sửa đổi các tệp `/system`, bạn cần cài đặt [metamodule](metamodule.md) (chẳng hạn như `meta-overlayfs`). Các tính năng module khác hoạt động mà không cần metamodule. Kiểm tra [Hướng dẫn Module](module.md) để biết thêm thông tin.",
"Có. Hệ thống module của ShizuSU dựa trên Magic Mount (từ SukiSU-Ultra); các module Magisk hoạt động trực tiếp và các module sửa đổi tệp `/system` không cần metamodule. Kiểm tra [Hướng dẫn Module](module.md) để biết thêm thông tin."),
("vi_VN/guide/faq.md",
"""Nếu các module của bạn cần sửa đổi các tệp `/system`, bạn cần cài đặt [metamodule](metamodule.md) để mount thư mục `system`. Các tính năng module khác (scripts, sepolicy, system.prop) hoạt động mà không cần metamodule.

**Giải pháp**: Xem [Hướng dẫn Metamodule](metamodule.md) để biết hướng dẫn cài đặt.""",
"""Hãy kiểm tra: module có được bật không, thư mục module có chứa `module.prop` hợp lệ không, và module có tương thích với thiết bị/kernel của bạn không. Hệ thống module của ShizuSU dựa trên Magic Mount; các module sửa đổi tệp `/system` không cần metamodule.

**Giải pháp**: Xem [Hướng dẫn Module](module.md) và [Hệ thống module: Magic Mount](metamodule.md)."""),
("vi_VN/guide/faq.md",
"""## Metamodule là gì và tại sao tôi cần nó?

Metamodule là một module đặc biệt cung cấp cơ sở hạ tầng để mount các module thông thường. Xem [Hướng dẫn Metamodule](metamodule.md) để biết giải thích đầy đủ.""",
"""## Hệ thống module của ShizuSU là gì?

ShizuSU là bản fork thế hệ thứ hai của SukiSU-Ultra; hệ thống module sử dụng **Magic Mount** (bind mount thư mục `system` của module lên `/system`), không cần metamodule và tương thích trực tiếp với module Magisk. Kiến trúc OverlayFS metamodule của KernelSU chính thức không áp dụng cho ShizuSU. Xem [Hệ thống module: Magic Mount](metamodule.md)."""),
("vi_VN/guide/app-profile.md",
"ShizuSU cung cấp một cơ chế systemless để sửa đổi các phân vùng hệ thống, đạt được thông qua việc gắn overlayfs. Tuy nhiên, một số ứng dụng có thể nhạy cảm với hành vi đó. Do đó, chúng ta có thể dỡ bỏ các mô-đun được gắn trên các ứng dụng này bằng cách đặt tùy chọn \"umount modules\".",
"ShizuSU cung cấp một cơ chế systemless để sửa đổi các phân vùng hệ thống, đạt được thông qua Magic Mount (bind mount). Tuy nhiên, một số ứng dụng có thể nhạy cảm với hành vi đó. Do đó, chúng ta có thể dỡ bỏ các mô-đun được gắn trên các ứng dụng này bằng cách đặt tùy chọn \"umount modules\"."),
("vi_VN/guide/difference-with-magisk.md",
"- Phương pháp thay thế hoặc xóa file trong module ShizuSU hoàn toàn khác với Magisk. ShizuSU không hỗ trợ phương thức `.replace`. Thay vào đó, bạn cần tạo một file cùng tên với `mknod filename c 0 0` để xóa file tương ứng.",
"- Phương pháp thay thế hoặc xóa file trong module ShizuSU giống với Magisk: sử dụng biến `REMOVE` và `REPLACE` trong `customize.sh` để xóa file hoặc thay thế thư mục (xem [Hướng dẫn Module](module.md))."),

# ============ id_ID ============
("id_ID/index.md",
"""  - title: Sistem Metamodule
    details: Infrastruktur modul yang dapat dipasang memungkinkan modifikasi systemless pada /system. Pasang metamodule seperti meta-overlayfs untuk mengaktifkan pemasangan modul.""",
"""  - title: Sistem modul Magic Mount
    details: Dibangun di atas Magic Mount (5ec1cff) yang diwarisi dari SukiSU-Ultra, pemasangan modul langsung berfungsi dan modul Magisk kompatibel langsung."""),
("id_ID/index.md",
"""  - title: Sistem modul berbasis Magic Mount
    details: Dibangun di atas teknologi Magic Mount dari 5ec1cff, memberikan fondasi pemasangan modul yang lebih stabil dan andal.""",
"""  - title: Berbasis SukiSU-Ultra
    details: Fork generasi kedua dari proyek komunitas yang matang, mewarisi dukungan Non-GKI, Magic Mount, KPM, dan lainnya."""),
("id_ID/guide/what-is-kernelsu.md",
"Selain itu, ShizuSU menyediakan [sistem metamodule](metamodule.md), yang merupakan arsitektur yang dapat dipasang untuk manajemen modul. Tidak seperti solusi root tradisional yang mengintegrasikan logika mount ke dalam intinya, ShizuSU mendelegasikan ini ke metamodules. Ini memungkinkan Anda untuk memasang metamodules (seperti [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs)) untuk menyediakan modifikasi systemless pada partisi `/system` dan partisi lainnya.",
"Selain itu, ShizuSU adalah fork generasi kedua dari **SukiSU-Ultra**, dan sistem modulnya dibangun di atas **Magic Mount** (dari implementasi Magisk 5ec1cff): direktori `system` modul di-overlay ke `/system` melalui bind mount secara systemless, **tanpa perlu metamodule**. Lihat [Sistem Modul: Magic Mount](metamodule.md)."),
("id_ID/guide/what-is-kernelsu.md",
"- **Sistem modul berbasis Magic Mount**: Pemasangan modul dibangun di atas teknologi Magic Mount dari 5ec1cff, memberikan fondasi yang lebih stabil dan andal, sambil tetap mempertahankan arsitektur [metamodule](metamodule.md) yang dapat dipasang.",
"- **Sistem modul berbasis Magic Mount**: ShizuSU adalah fork dari SukiSU-Ultra; pemasangan modul dibangun di atas teknologi Magic Mount dari 5ec1cff, memberikan fondasi yang lebih stabil dan andal, dan modul Magisk berfungsi langsung. ShizuSU tidak menggunakan arsitektur OverlayFS metamodule dari KernelSU resmi — deskripsi sistem modul di seluruh situs disatukan sebagai Magic Mount."),
("id_ID/guide/module.md",
"""::: warning METAMODULE HANYA DIPERLUKAN UNTUK MODIFIKASI FILE SISTEM
ShizuSU menggunakan arsitektur [metamodule](metamodule.md) untuk me-mount direktori `system`. **Hanya jika modul Anda perlu memodifikasi file `/system`** (melalui direktori `system`) Anda perlu menginstal metamodule (seperti [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)). Fitur modul lainnya seperti skrip, aturan sepolicy, dan system.prop bekerja tanpa metamodule.
:::""",
"""::: info SISTEM MODUL BERBASIS MAGIC MOUNT
Sistem modul ShizuSU didasarkan pada **Magic Mount** (dari SukiSU-Ultra), tidak perlu menginstal metamodule untuk me-mount direktori `system`. Modul yang memodifikasi file `/system` langsung berfungsi. Lihat [Sistem Modul: Magic Mount](metamodule.md).
:::"""),
("id_ID/guide/module.md",
"::: tip PERSYARATAN METAMODULE\nDirektori `system` hanya di-mount jika Anda telah menginstal metamodule yang menyediakan fungsionalitas mounting (seperti `meta-overlayfs`). Metamodule menangani bagaimana modul di-mount. Lihat [Panduan Metamodule](metamodule.md) untuk informasi lebih lanjut.\n:::",
"::: info SISTEM MODUL BERBASIS MAGIC MOUNT\nSistem modul ShizuSU didasarkan pada **Magic Mount** (dari SukiSU-Ultra); direktori `system` di-mount secara langsung tanpa metamodule. Lihat [Sistem Modul: Magic Mount](metamodule.md) untuk informasi lebih lanjut.\n:::"),
("id_ID/guide/module.md",
"Jika Anda ingin menghapus file atau folder di direktori sistem asli, Anda perlu membuat file dengan nama yang sama dengan file/folder di direktori modul menggunakan `mknod filename c 0 0`. Dengan cara ini, sistem overlayfs akan secara otomatis \"memutihkan\" file ini seolah-olah telah dihapus (partisi / sistem sebenarnya tidak diubah).",
"Jika Anda ingin menghapus file atau folder di direktori sistem asli, Anda dapat mendeklarasikan variabel `REMOVE` yang berisi daftar direktori di `customize.sh`. ShizuSU akan menyembunyikan file-file ini melalui Magic Mount (partisi /system sebenarnya tidak diubah)."),
("id_ID/guide/module.md",
"Jika Anda ingin mengganti direktori di sistem, Anda perlu membuat direktori dengan jalur yang sama di direktori modul Anda, lalu atur atribut `setfattr -n trusted.overlay.opaque -v y <TARGET>` untuk direktori ini. Dengan cara ini, sistem overlayfs akan secara otomatis mengganti direktori terkait di sistem (tanpa mengubah partisi /sistem).",
"Jika Anda ingin mengganti direktori di sistem, Anda dapat mendeklarasikan variabel `REPLACE` di `customize.sh`. ShizuSU akan bind mount direktori kosong ke jalur target, mengganti seluruh direktori (tanpa mengubah partisi /system)."),
("id_ID/guide/module.md",
"ShizuSU menggunakan arsitektur [metamodule](metamodule.md) di mana mounting didelegasikan ke metamodule yang dapat dipasang. Metamodule `meta-overlayfs` resmi menggunakan OverlayFS kernel untuk modifikasi systemless, sedangkan Magisk menggunakan magic mount (bind mount) yang dibangun langsung ke dalam intinya. Keduanya mencapai tujuan yang sama: memodifikasi file `/system` tanpa memodifikasi partisi `/system` secara fisik. Pendekatan ShizuSU memberikan lebih banyak fleksibilitas dan mengurangi permukaan deteksi.",
"KernelSU resmi menggunakan mekanisme OverlayFS (metamodule) untuk systemless, sedangkan ShizuSU, berbasis SukiSU-Ultra, menggunakan **Magic Mount (bind mount)** seperti Magisk. Keduanya mencapai tujuan yang sama: memodifikasi file `/system` tanpa memodifikasi partisi `/system` secara fisik. ShizuSU hanya menggunakan Magic Mount — tidak ada dua sistem modul. Lihat [Sistem Modul: Magic Mount](metamodule.md)."),
("id_ID/guide/module.md",
"Jika Anda tertarik dengan overlayfs, disarankan untuk membaca [dokumentasi overlayfs](https://docs.kernel.org/filesystems/overlayfs.html) Kernel Linux.",
""),
("id_ID/guide/module.md",
"| Mount OverlayFS (metamodule) | Ya | Ya |",
"| Pemasangan modul Magic Mount | Ya | Ya |"),
("id_ID/guide/module.md",
"  5. Jalankan skrip mount metamodule (OverlayFS)",
"  5. Jalankan pemasangan modul Magic Mount"),
("id_ID/guide/module.md",
"Skrip ini berjalan sebelum mount OverlayFS, mirip dengan `post-fs-data.sh` dalam alur standar.",
"Skrip ini berjalan sebelum pemasangan modul, mirip dengan `post-fs-data.sh` dalam alur standar."),
("id_ID/guide/installation.md",
"""## Pasca-Instalasi: Dukungan Modul

::: warning METAMODULE UNTUK MODIFIKASI FILE SISTEM
Jika Anda ingin menggunakan modul yang memodifikasi file `/system`, Anda perlu menginstal **metamodule** setelah menginstal ShizuSU. Modul yang hanya menggunakan skrip, sepolicy, atau system.prop bekerja tanpa metamodule.
:::

**Untuk dukungan modifikasi `/system`**, silakan lihat [Panduan Metamodule](metamodule.md) untuk:
- Memahami apa itu metamodule dan mengapa diperlukan
- Menginstal metamodule `meta-overlayfs` resmi
- Pelajari tentang opsi metamodule lainnya""",
"""## Pasca-Instalasi: Dukungan Modul

::: info SISTEM MODUL BERBASIS MAGIC MOUNT
Sistem modul ShizuSU didasarkan pada **Magic Mount** (dari SukiSU-Ultra). Modul langsung berfungsi setelah menginstal ShizuSU — modul yang memodifikasi file `/system` tidak memerlukan metamodule tambahan.
:::

Untuk cara kerja sistem modul dan pengembangan modul, lihat [Sistem Modul: Magic Mount](metamodule.md) dan [Panduan Modul](module.md)."""),
("id_ID/guide/faq.md",
"Ya, sebagian besar modul Magisk bekerja di ShizuSU. Namun, jika modul Anda perlu memodifikasi file `/system`, Anda perlu menginstal [metamodule](metamodule.md) (seperti `meta-overlayfs`). Fitur modul lainnya bekerja tanpa metamodule. Periksa [Panduan modul](module.md) untuk info lebih lanjut.",
"Ya. Sistem modul ShizuSU didasarkan pada Magic Mount (dari SukiSU-Ultra); modul Magisk bekerja langsung, dan modul yang memodifikasi file `/system` tidak memerlukan metamodule. Periksa [Panduan modul](module.md) untuk info lebih lanjut."),
("id_ID/guide/faq.md",
"""Jika modul Anda perlu memodifikasi file `/system`, Anda perlu menginstal [metamodule](metamodule.md) untuk me-mount direktori `system`. Fitur modul lainnya (skrip, sepolicy, system.prop) bekerja tanpa metamodule.

**Solusi**: Lihat [Panduan Metamodule](metamodule.md) untuk instruksi instalasi.""",
"""Silakan periksa: apakah modul diaktifkan, apakah direktori modul berisi `module.prop` yang valid, dan apakah modul kompatibel dengan perangkat/kernel Anda. Sistem modul ShizuSU didasarkan pada Magic Mount; modul yang memodifikasi file `/system` tidak memerlukan metamodule.

**Solusi**: Lihat [Panduan Modul](module.md) dan [Sistem Modul: Magic Mount](metamodule.md)."""),
("id_ID/guide/faq.md",
"""## Apa itu metamodule dan mengapa saya membutuhkannya?

Metamodule adalah modul khusus yang menyediakan infrastruktur untuk me-mount modul reguler. Lihat [Panduan Metamodule](metamodule.md) untuk penjelasan lengkap.""",
"""## Apa sistem modul ShizuSU?

ShizuSU adalah fork generasi kedua dari SukiSU-Ultra; sistem modulnya menggunakan **Magic Mount** (bind mount direktori `system` modul ke `/system`), tanpa metamodule dan kompatibel langsung dengan modul Magisk. Arsitektur OverlayFS metamodule dari KernelSU resmi tidak berlaku untuk ShizuSU. Lihat [Sistem Modul: Magic Mount](metamodule.md)."""),
("id_ID/guide/app-profile.md",
"ShizuSU menyediakan mekanisme systemless untuk memodifikasi partisi sistem dengan memasang OverlayFS. Namun beberapa aplikasi peka terhadap perilaku ini. Dalam kasus tersebut, kita dapat membongkar (umount) modul yang dimuat di aplikasi tertentu dengan mengaktifkan opsi “Umount modules”.",
"ShizuSU menyediakan mekanisme systemless untuk memodifikasi partisi sistem dengan Magic Mount (bind mount). Namun beberapa aplikasi peka terhadap perilaku ini. Dalam kasus tersebut, kita dapat membongkar (umount) modul yang dimuat di aplikasi tertentu dengan mengaktifkan opsi “Umount modules”."),
("id_ID/guide/difference-with-magisk.md",
"- Metode untuk mengganti atau menghapus file dalam modul ShizuSU sama sekali berbeda dari Magisk. ShizuSU tidak mendukung metode `.replace`. Sebagai gantinya, Anda perlu membuat file dengan nama yang sama dengan `mknod filename c 0 0` untuk menghapus file terkait.",
"- Metode untuk mengganti atau menghapus file dalam modul ShizuSU sama dengan Magisk: gunakan variabel `REMOVE` dan `REPLACE` di `customize.sh` untuk menghapus file atau mengganti direktori (lihat [Panduan Modul](module.md))."),
]

missed = []
for rel, old, new in pairs:
    p = os.path.join(ROOT, rel)
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if old not in t:
        missed.append(rel + ' :: ' + old[:70])
        continue
    t = t.replace(old, new)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', rel)

print('MISSED:', len(missed))
for m in missed:
    print(m)
