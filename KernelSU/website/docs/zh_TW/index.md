---
layout: home
title: 基於核心的 Android Root 解決方案

hero:
  name: ShizuSU
  text: 基於核心的 Android root 解決方案
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: 開始瞭解
      link: /zh_TW/guide/what-is-kernelsu
    - theme: alt
      text: 在 GitHub 中檢視
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: 下載 ShizuSU
      link: /download
    - theme: sponsor
      text: 贊助 ShizuSU
      link: https://zanzhuwang.cc.cd

features:
  - title: 以核心為基礎
    details: ShizuSU 以 Linux 核心模式運作，對使用者空間有更強的掌控。
  - title: 白名單存取控制
    details: 僅有被授予 Root 權限的應用程式才可存取 su，而其他應用程式完全無法知悉。
  - title: 可定制的 Root 權限
    details: ShizuSU 能夠對 su 的使用者ID（uid）、群組ID（gid）、群組、權限，以及 SELinux 規則進行客製化管理，以此加強 root 權限的安全性。
  - title: Magic Mount 模組系統
    details: 基於 SukiSU-Ultra 繼承的 Magic Mount（5ec1cff）技術，模組掛載開箱即用，Magisk 模組直接相容。
  - title: 非 GKI 舊核心支援
    details: 恢復對非 GKI / GKI 1.0 裝置的支援，涵蓋 4.x - 5.4 LTS 核心（3.x 實驗性），讓舊裝置也能享受核心級 root。
  - title: 基於 SukiSU-Ultra 二改
    details: 在成熟社群方案上二次開發，繼承非 GKI 支援、Magic Mount、KPM 等增強特性。
  - title: KPM 核心模組
    details: 完整支援 KernelPatch Module（KPM），可進行進階核心修改與增強。
  - title: 廣泛的自訂選項
    details: 自訂管理員背景、直接管理 susfs 功能、調整 DPI 等，以你自己的方式設計。

