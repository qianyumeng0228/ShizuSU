---
layout: home
title: Giải pháp root dựa trên kernel dành cho Android

hero:
  name: ShizuSU
  text: Giải pháp root dựa trên kernel dành cho Android
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: Bắt Đầu
      link: /guide/what-is-kernelsu
    - theme: alt
      text: Xem trên GitHub
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: Tải ShizuSU
      link: /download
    - theme: sponsor
      text: Ủng hộ ShizuSU
      link: https://zanzhuwang.cc.cd

features:
  - title: Dựa trên Kernel
    details: ShizuSU đang hoạt động ở chế độ kernel Linux, nó có nhiều quyền kiểm soát hơn đối với các ứng dụng userspace.
  - title: Kiểm soát truy cập bằng whitelist
    details: Chỉ ứng dụng được cấp quyền root mới có thể truy cập `su`, các ứng dụng khác không thể nhận được su.
  - title: Quyền root bị hạn chế
    details: ShizuSU cho phép bạn tùy chỉnh uid, gid, group, capabilities và các quy tắc SELinux của su. Giới hạn sức mạnh của root.
  - title: Hệ thống module Magic Mount
    details: Dựa trên Magic Mount (5ec1cff) kế thừa từ SukiSU-Ultra, mount module hoạt động ngay lập tức và tương thích trực tiếp với module Magisk.
  - title: Hỗ trợ kernel cũ Non-GKI
    details: Khôi phục hỗ trợ cho thiết bị Non-GKI / GKI 1.0, bao phủ kernel 4.x - 5.4 LTS (3.x thử nghiệm), đưa root cấp kernel đến các thiết bị cũ.
  - title: Dựa trên SukiSU-Ultra
    details: Fork thế hệ thứ hai của một dự án cộng đồng trưởng thành, kế thừa hỗ trợ Non-GKI, Magic Mount, KPM và hơn thế nữa.
  - title: Module kernel KPM
    details: Hỗ trợ đầy đủ KernelPatch Module (KPM) cho các sửa đổi và nâng cao kernel ở mức cao.
  - title: Tùy biến rộng rãi
    details: Tùy chỉnh nền trình quản lý, quản lý trực tiếp các tính năng susfs, điều chỉnh DPI... thiết kế theo cách của riêng bạn.

