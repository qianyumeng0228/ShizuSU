# FAQ

## ShizuSU có hỗ trợ thiết bị của tôi không?

ShizuSU hỗ trợ các thiết bị chạy Android với bootloader đã mở khóa. Tuy nhiên, hỗ trợ chính thức chỉ dành cho GKI Linux Kernel 5.10+ (trong thực tế, điều này có nghĩa là thiết bị của bạn cần có Android 12 out-of-the-box để được hỗ trợ).

Bạn có thể dễ dàng kiểm tra hỗ trợ cho thiết bị của mình thông qua ứng dụng quản lý ShizuSU, có sẵn [tại đây](https://github.com/qianyumeng0228/ShizuSU/releases).

Nếu ứng dụng hiển thị `Not installed`, điều đó có nghĩa là thiết bị của bạn được ShizuSU hỗ trợ chính thức.

Nếu ứng dụng hiển thị `Unsupported`, điều đó có nghĩa là thiết bị của bạn hiện không được hỗ trợ chính thức. Tuy nhiên, bạn có thể build mã nguồn kernel và tích hợp ShizuSU để làm cho nó hoạt động, hoặc sử dụng [Thiết bị được hỗ trợ không chính thức](unofficially-support-devices).

## ShizuSU có cần mở khóa bootloader không?

Chắc chắn rồi.

## ShizuSU có hỗ trợ module không?

Có. Hệ thống module của ShizuSU dựa trên Magic Mount (từ SukiSU-Ultra); các module Magisk hoạt động trực tiếp và các module sửa đổi tệp `/system` không cần metamodule. Kiểm tra [Hướng dẫn Module](module.md) để biết thêm thông tin.

## ShizuSU có hỗ trợ Xposed không?

Có, bạn có thể sử dụng LSPosed (hoặc các phái sinh Xposed hiện đại khác) với [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext).

## ShizuSU có hỗ trợ Zygisk không?

ShizuSU không có hỗ trợ Zygisk tích hợp sẵn, nhưng bạn có thể sử dụng module như [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) để hỗ trợ nó.

## ShizuSU có tương thích với Magisk không?

Hệ thống module của ShizuSU xung đột với magic mount của Magisk. Nếu có bất kỳ module nào được kích hoạt trong ShizuSU, toàn bộ Magisk sẽ ngừng hoạt động.

Tuy nhiên, nếu bạn chỉ sử dụng `su` của ShizuSU, nó sẽ hoạt động tốt với Magisk. ShizuSU sửa đổi `kernel`, trong khi Magisk sửa đổi `ramdisk`, cho phép cả hai hoạt động cùng nhau.

## ShizuSU sẽ thay thế Magisk?

Chúng tôi tin rằng không, và đó không phải là mục tiêu của chúng tôi. Magisk đã đủ tốt cho giải pháp root userspace và sẽ tồn tại lâu dài. Mục tiêu của ShizuSU là cung cấp giao diện kernel cho người dùng, không phải để thay thế Magisk.

## ShizuSU có thể hỗ trợ các thiết bị không phải GKI không?

Có thể. Nhưng bạn cần tải xuống mã nguồn kernel và tích hợp ShizuSU vào source tree, sau đó tự biên dịch kernel.

## ShizuSU có thể hỗ trợ các thiết bị dưới Android 12 không?

Chính kernel thiết bị ảnh hưởng đến khả năng tương thích của ShizuSU, và nó không liên quan gì đến phiên bản Android. Hạn chế duy nhất là các thiết bị được ra mắt với Android 12 phải có phiên bản kernel 5.10+ (thiết bị GKI). Vì vậy:

1. Các thiết bị được ra mắt với Android 12 phải được hỗ trợ.
2. Các thiết bị có kernel cũ (một số thiết bị với Android 12 cũng có kernel cũ) tương thích (bạn cần tự build kernel).

## ShizuSU có thể hỗ trợ kernel cũ không?

Có thể. ShizuSU hiện đã được backport cho kernel 4.14. Đối với các kernel cũ hơn, bạn cần tự backport, và PR luôn được chào đón!

## Làm cách nào để tích hợp ShizuSU cho kernel cũ?

Vui lòng kiểm tra hướng dẫn [Tích hợp cho thiết bị không phải GKI](how-to-integrate-for-non-gki).

## Tại sao phiên bản Android của tôi là 13, nhưng kernel hiển thị "android12-5.10"?

Phiên bản kernel không liên quan gì đến phiên bản Android. Nếu bạn cần flash kernel, luôn sử dụng phiên bản kernel; phiên bản Android không quan trọng bằng.

## Tôi là GKI 1.0, tôi có thể sử dụng điều này không?

GKI 1.0 hoàn toàn khác với GKI 2.0, bạn phải tự biên dịch kernel.

## Làm cách nào để làm cho `/system` RW?

Chúng tôi không khuyến nghị bạn sửa đổi trực tiếp phân vùng hệ thống. Vui lòng kiểm tra [Hướng dẫn Module](module.md) để sửa đổi nó một cách systemless. Nếu bạn khăng khăng làm điều này, hãy kiểm tra [magisk_overlayfs](https://github.com/HuskyDG/magic_overlayfs).

## ShizuSU có thể sửa đổi hosts không? Làm cách nào để sử dụng AdAway?

Tất nhiên. Nhưng ShizuSU không có hỗ trợ hosts tích hợp sẵn, bạn có thể cài đặt module như [systemless-hosts](https://github.com/symbuzzer/systemless-hosts-KernelSU-module) để thực hiện.

## Tại sao các module của tôi không hoạt động sau khi cài đặt mới?

Hãy kiểm tra: module có được bật không, thư mục module có chứa `module.prop` hợp lệ không, và module có tương thích với thiết bị/kernel của bạn không. Hệ thống module của ShizuSU dựa trên Magic Mount; các module sửa đổi tệp `/system` không cần metamodule.

**Giải pháp**: Xem [Hướng dẫn Module](module.md) và [Hệ thống module: Magic Mount](metamodule.md).

## Hệ thống module của ShizuSU là gì?

ShizuSU là bản fork thế hệ thứ hai của SukiSU-Ultra; hệ thống module sử dụng **Magic Mount** (bind mount thư mục `system` của module lên `/system`), không cần metamodule và tương thích trực tiếp với module Magisk. Kiến trúc OverlayFS metamodule của KernelSU chính thức không áp dụng cho ShizuSU. Xem [Hệ thống module: Magic Mount](metamodule.md).
