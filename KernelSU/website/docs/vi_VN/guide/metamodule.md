# Hệ thống module: Magic Mount {#introduction}

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
