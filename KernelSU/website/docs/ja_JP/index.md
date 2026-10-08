---
layout: home
title: Android 向けのカーネルベース root ソリューション

hero:
  name: ShizuSU
  text: Android 向けのカーネルベース root ソリューション
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: はじめる
      link: /ja_JP/guide/what-is-kernelsu
    - theme: alt
      text: GitHub で表示
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: ShizuSU をダウンロード
      link: /download
    - theme: sponsor
      text: ShizuSU を支援する
      link: https://zanzhuwang.cc.cd

features:
  - title: カーネルベース
    details: ShizuSU は Linux カーネルモードで動作し、ユーザー空間よりも高度な制御が可能です。
  - title: ホワイトリストの権限管理
    details: root 権限を許可したアプリのみが su にアクセスでき、他のアプリは su を見つけられません。
  - title: Magic Mount モジュールシステム
    details: SukiSU-Ultra 由来の Magic Mount（5ec1cff）技術により、モジュールマウントがすぐに動作し、Magisk モジュールにも直接対応。
  - title: オープンソース
    details: ShizuSU は GPL-3 でライセンスされたオープンソースプロジェクトです。
  - title: 非 GKI 旧カーネルサポート
    details: 非 GKI / GKI 1.0 デバイスのサポートを復活し、4.x - 5.4 LTS カーネル（3.x は実験的）をカバー。古いデバイスでもカーネルレベル root を利用可能に。
  - title: SukiSU-Ultra ベース
    details: 成熟したコミュニティプロジェクトの二次開発版。非 GKI サポート、Magic Mount、KPM などの拡張機能を継承。
  - title: KPM カーネルモジュール
    details: KernelPatch Module（KPM）を完全サポートし、高度なカーネル改変と拡張が可能です。
  - title: 幅広いカスタマイズ
    details: カスタム背景、susfs 機能の直接管理、DPI 調整など、自分好みに設計できます。

