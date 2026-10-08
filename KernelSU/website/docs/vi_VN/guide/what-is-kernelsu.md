# ShizuSU là gì?

ShizuSU là một giải pháp root cho các thiết bị Android GKI, nó hoạt động ở chế độ kernel và cấp quyền root cho ứng dụng không gian người dùng trực tiếp trong không gian kernel.

## Tính năng

Tính năng chính của ShizuSU là **Kernel-based** (dựa trên Kernel). ShizuSU hoạt động ở chế độ kernel nên nó có thể cung cấp giao diện kernel mà chúng ta chưa từng có trước đây. Ví dụ: chúng ta có thể thêm điểm dừng phần cứng vào bất kỳ quy trình nào ở chế độ kernel; Chúng ta có thể truy cập bộ nhớ vật lý của bất kỳ quy trình nào mà không bị phát hiện; Chúng ta còn có thể chặn bất kỳ syscall nào trong không gian kernel; v.v.

Ngoài ra, ShizuSU là bản fork thế hệ thứ hai của **SukiSU-Ultra**, hệ thống module được xây dựng trên **Magic Mount** (từ triển khai Magisk của 5ec1cff): thư mục `system` của module được overlay lên `/system` bằng bind mount theo cách systemless, **không cần cài đặt metamodule**. Xem [Hệ thống module: Magic Mount](metamodule.md).

## Tính năng mở rộng

Bên cạnh nền tảng root cấp kernel, ShizuSU còn mang đến một loạt tính năng mở rộng:

- **Hỗ trợ kernel cũ / Non-GKI**: ShizuSU khôi phục hỗ trợ cho thiết bị Non-GKI và GKI 1.0, bao phủ kernel 4.x - 5.4 LTS (3.x là thử nghiệm). Hỗ trợ kiến trúc: `arm64-v8a` hỗ trợ đầy đủ, `armeabi-v7a` hỗ trợ cơ bản, `x86_64` hỗ trợ một phần.
- **Hệ thống module dựa trên Magic Mount**: ShizuSU là bản fork của SukiSU-Ultra; việc mount module được xây dựng trên công nghệ Magic Mount của 5ec1cff, mang lại nền tảng ổn định và đáng tin cậy hơn, và các module Magisk hoạt động trực tiếp. ShizuSU không sử dụng kiến trúc OverlayFS metamodule của KernelSU chính thức — mô tả hệ thống module trên toàn trang được thống nhất là Magic Mount.
- **Module kernel KPM**: Hỗ trợ đầy đủ KernelPatch Module (KPM, được port từ Apatch) cho các sửa đổi và nâng cao ở cấp kernel.
- **App Profile**: Khóa quyền root trong môi trường được kiểm soát thông qua hồ sơ ứng dụng; xem [App Profile](app-profile.md).
- **Tùy biến rộng rãi**: Tùy chỉnh nền trình quản lý, quản lý trực tiếp các tính năng susfs (không cần module susfsforksu), điều chỉnh DPI... thiết kế theo cách của riêng bạn.

- **Hỗ trợ đa trình quản lý (Multi-manager)**：Một kernel nhận diện đồng thời nhiều trình quản lý qua bảng chữ ký tích hợp (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), hỗ trợ đăng ký nóng và lưu trữ bền vững (`/data/adb/shizusu/manager`) — không cần flash lại kernel cho từng trình quản lý.
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

## Hướng dẫn sử dụng

Xin hãy xem: [Cách cài đặt](installation)

## Cách để build

[Cách để build](how-to-build)

## Thảo luận

- Telegram: [@KernelSU](https://t.me/KernelSU)
