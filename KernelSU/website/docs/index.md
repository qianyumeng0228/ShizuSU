---
layout: home
title: Home

hero:
  name: ShizuSU
  text: A kernel-based root solution for Android
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: Get started
      link: /guide/what-is-kernelsu
    - theme: alt
      text: View on GitHub
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: Download ShizuSU
      link: /download
    - theme: sponsor
      text: Sponsor ShizuSU
      link: https://zanzhuwang.cc.cd

features:
  - title: Kernel-based
    details: As the name suggests, ShizuSU runs inside the Linux kernel, giving it more control over userspace apps.
  - title: Root access control
    details: Only permitted apps can access or see su; all other apps remain unaware of it.
  - title: Customizable root privileges
    details: ShizuSU allows customization of su's uid, gid, groups, capabilities, and SELinux rules, hardening root privileges.
  - title: Magic Mount module system
    details: Built on SukiSU-Ultra's Magic Mount (5ec1cff), module mounting works out of the box and Magisk modules are directly compatible.
  - title: Non-GKI legacy kernel support
    details: Restores support for non-GKI / GKI 1.0 devices across 4.x - 5.4 LTS kernels (3.x experimental), bringing kernel-level root to older devices.
  - title: Based on SukiSU-Ultra
    details: A second-generation fork of a mature community project, inheriting Non-GKI support, Magic Mount, KPM and more.
  - title: KPM kernel modules
    details: Full KernelPatch Module (KPM) support for advanced kernel modifications and enhancements.
  - title: Extensive customization
    details: Custom background, manage susfs features directly, adjust DPI, and design it in your own way.
