---
layout: home
title: Основанное на ядре root-решение для Android

hero:
  name: ShizuSU
  text: Основанное на ядре root-решение для Android
  tagline: ""
  image:
    src: /logo.png
    alt: ShizuSU
  actions:
    - theme: brand
      text: Начало работы
      link: /ru_RU/guide/what-is-kernelsu
    - theme: alt
      text: Посмотр на GitHub
      link: https://github.com/qianyumeng0228/ShizuSU
    - theme: alt
      text: Скачать ShizuSU
      link: /download
    - theme: sponsor
      text: Поддержать ShizuSU
      link: https://zanzhuwang.cc.cd

features:
  - title: Основанный на ядре
    details: ShizuSU работает в режиме ядра Linux, он имеет больше контроля над пользовательскими приложениями.
  - title: Контроль доступа по белому списку
    details: Только приложение, которому предоставлено разрешение root, может получить доступ к `su`, другие приложения не могут воспринимать su.
  - title: Ограниченные root-права
    details: ShizuSU позволяет вам настраивать uid, gid, группы, возможности и правила SELinux для su. Заприте root-власть в клетке.
  - title: Модульная система Magic Mount
    details: Основана на Magic Mount (5ec1cff) от SukiSU-Ultra — монтирование модулей работает из коробки, модули Magisk совместимы напрямую.
  - title: Поддержка старых ядер Non-GKI
    details: Возвращает поддержку устройств Non-GKI / GKI 1.0, охватывая ядра 4.x - 5.4 LTS (3.x экспериментально), предоставляя root на уровне ядра старым устройствам.
  - title: На основе SukiSU-Ultra
    details: Форк второго поколения зрелого проекта сообщества, наследуя поддержку Non-GKI, Magic Mount, KPM и другое.
  - title: Ядерные модули KPM
    details: Полная поддержка KernelPatch Module (KPM) для продвинутых модификаций и улучшений ядра.
  - title: Широкая кастомизация
    details: Пользовательский фон, прямое управление функциями susfs, настройка DPI — проектируйте по-своему.

