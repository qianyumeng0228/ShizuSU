# Tính Năng Ẩn

## .ksurc

Theo mặc định, `/system/bin/sh` tải `/system/etc/mkshrc`.

Bạn có thể tạo su tải tệp rc tùy chỉnh bằng cách tạo tệp `/data/adb/ksu/.ksurc`.

## Tùy biến {#customization}

- **Nền tùy chỉnh**: Thay đổi hình nền trong cài đặt Trình quản lý ShizuSU để cá nhân hóa giao diện.
- **Quản lý susfs**: Quản lý trực tiếp một số tính năng susfs trong Trình quản lý, không cần module susfsforksu.
- **Điều chỉnh DPI**: Điều chỉnh hiển thị DPI của Trình quản lý để phù hợp với các màn hình khác nhau.

## WebUI X {#webui-x}

Hỗ trợ triển khai WebUI thế hệ mới (WebUI X) do MMRL cung cấp, mang lại trải nghiệm tương tác module phong phú hơn.

## Hỗ trợ đa trình quản lý {#multi-manager}

ShizuSU tích hợp sẵn bảng chữ ký trình quản lý, cho phép một kernel nhận diện đồng thời nhiều trình quản lý root: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Ngoài chữ ký tích hợp, còn hỗ trợ đăng ký nóng và lưu trữ bền vững (`/data/adb/shizusu/manager`) — không cần flash lại kernel khi cài trình quản lý mới.

## Chế độ ẩn {#stealth}

Ghi cờ vào `/data/adb/shizusu/stealth` để bật chế độ ẩn. Khi bật, báo cáo thông tin của ShizuSU không còn lộ trạng thái trình quản lý hiện tại cho ứng dụng, giảm rủi ro bị phát hiện.

## Tăng cường ẩn {#hiding-enhancements}

- **Kênh truy vấn susfsd**：Tích hợp kênh giao tiếp susfsd, phối hợp trực tiếp với bản vá kernel susfs.
- **Ẩn hook KPROBES**：Tùy chọn ẩn hook KPROBES (mặc định tắt).
- **Mục ẩn hosts**：Liên kết với App Profile, có thể ẩn thay đổi tệp hosts của module.
