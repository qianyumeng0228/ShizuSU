---
layout: home
title: Android 上的内核级的 root 方案

hero:
  name: ShizuSU
  text: Android 上的内核级的 root 方案
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: 开始了解
      link: /zh_CN/guide/what-is-kernelsu
    - theme: alt
      text: 在 GitHub 中查看
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: 下载 ShizuSU
      link: /download
    - theme: sponsor
      text: 赞助 ShizuSU
      link: https://zanzhuwang.cc.cd

features:
  - title: 基于内核
    details: ShizuSU 运行在内核空间，对用户空间应用有更强的掌控。
  - title: 白名单访问控制
    details: 只有被授权的 App 才可以访问 `su`，而其他 App 无法感知其存在。
  - title: 受限制的 root 权限
    details: ShizuSU 可以自定义 `su` 的 uid, gid, groups, capabilities 和 SELinux 规则：把 root 权限关进笼子里。
  - title: Magic Mount 模块系统
    details: 基于 SukiSU-Ultra 继承的 Magic Mount（5ec1cff）技术，模块挂载开箱即用，Magisk 模块直接兼容。
  - title: 非 GKI 老内核支持
    details: 恢复对非 GKI / GKI 1.0 设备的支持，覆盖 4.x - 5.4 LTS 内核（3.x 实验性），让老设备也能享受内核级 root。
  - title: KPM 内核模块
    details: 完整支持 KernelPatch Module（KPM），可进行高级内核修改与增强。
  - title: 广泛自定义
    details: 自定义管理器背景、管理 susfs 功能、调整 DPI 等，按你自己的方式设计。
  - title: 基于 SukiSU-Ultra 二改
    details: 在成熟社区方案上二次开发，继承非 GKI 支持、Magic Mount、KPM 等增强特性。

