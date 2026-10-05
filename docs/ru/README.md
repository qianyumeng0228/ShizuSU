# ShizuSU
<p align="center"><img src="ShizuSU-banner.png" width="220px" alt="логотип shizusu"></p>


[English](../README.md) | [简体中文](./zh/README.md) | [日本語](./ja/README.md) | [Türkçe](./tr/README.md) | **Русский**

Решение для получения root-прав на уровне ядра для устройств Android. Форк [SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra) (сам является форком [`tiann/KernelSU`](https://github.com/tiann/KernelSU)) с добавлением stealth, мульти-менеджеров и удобства модулей.

[![Latest release](https://img.shields.io/github/v/release/qianyumeng0228/ShizuSU?label=Release&logo=github)](https://github.com/qianyumeng0228/ShizuSU/releases/latest)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/qianyumeng0228/ShizuSU?logo=gnu)](/LICENSE)

## Особенности

1. Управление доступом `su` и root на уровне ядра.
2. [App Profile](https://kernelsu.org/guide/app-profile.html): закройте root-права для конкретных приложений.
3. **Поддержка нескольких менеджеров**: одно ядро распознаёт несколько менеджеров через встроенную таблицу подписей (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), плюс горячая регистрация и сохранение (`/data/adb/shizusu/manager`).
4. **Режим скрытности**: переключатель `/data/adb/shizusu/stealth`; при включении информационный отчёт больше не раскрывает статус менеджера приложениям.
5. **Удобство модулей**: резервное копирование / восстановление модулей и allowlist, пакетная установка со сбором ошибок (без прерывания), управление enable / disable / disable-all / uninstall-all.
6. **Улучшения скрытия**: канал запросов susfsd, опциональное скрытие KPROBES-хуков (по умолчанию выключено), вход для скрытия hosts, связанный с App Profile.
7. Поддержка non-GKI и GKI 1.0.
8. Поддержка KPM.

## Статус совместимости

- KernelSU (до v1.0.0) официально поддерживает устройства Android GKI 2.0 (ядро 5.10+).

- Более старые ядра (4.4+) также совместимы, но ядро придется собирать вручную.

- С дополнительными бэкпортами KernelSU может поддерживать ядра серии 3.x (3.4–3.18).

- На данный момент поддерживаются только архитектуры `arm64-v8a`, `armeabi-v7a (bare)` и некоторые `X86_64`.

## Установка

См. [`guide/installation.md`](guide/installation.md)

## Интеграция

См. [`guide/how-to-integrate.md`](guide/how-to-integrate.md)

## Перевод

Встроенные переводы унаследованы от вышестоящего проекта и могут отставать. Вы можете предложить перевод через pull request.

## Поддержка KPM

- На базе KernelPatch: мы удалили функции, дублирующие возможности KSU, оставив только поддержку KPM.
- В разработке: расширение совместимости с APatch путем интеграции дополнительных функций для обеспечения работы в различных реализациях.

**Open-source репозиторий**: [https://github.com/ShirkNeko/SukiSU_KernelPatch_patch](https://github.com/ShirkNeko/SukiSU_KernelPatch_patch)

**Шаблон KPM**: [https://github.com/udochina/KPM-Build-Anywhere](https://github.com/udochina/KPM-Build-Anywhere)

> [!Note]
>
> 1. Требуется `CONFIG_KPM=y`
> 2. Для non-GKI устройств требуются `CONFIG_KALLSYMS=y` и `CONFIG_KALLSYMS_ALL=y`
> 3. Для ядер ниже `4.19` требуется бэкпорт `set_memory.h` из версии `4.19`.

## Устранение неполадок

1. Если устройство зависает при удалении менеджера (sukisu) 
   Удалите com.sony.playmemories.mobile


## Список спонсоров ShirkNeko

- [Ktouls](https://github.com/Ktouls) Большое спасибо за поддержку.
- [zaoqi123](https://github.com/zaoqi123) Спасибо за чай с молоком.
- [wswzgdg](https://github.com/wswzgdg) Огромное спасибо за поддержку проекта.
- [yspbwx2010](https://github.com/yspbwx2010) Большое спасибо.
- [DARKWWEE](https://github.com/DARKWWEE) 100 USDT
- [Saksham Singla](https://github.com/TypeFlu) Предоставление и поддержка сайта.
- [OukaroMF](https://github.com/OukaroMF) Пожертвование доменного имени для сайта.

## Лицензия

- Файлы в директории «kernel» находятся под лицензией [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
- За исключением вышеуказанных файлов и директорий, все остальные части находятся под лицензией [GPL-3.0 or later](https://www.gnu.org/licenses/gpl-3.0.html)

## Благодарности

- [KernelSU](https://github.com/tiann/KernelSU): база (основа).
- [MKSU](https://github.com/5ec1cff/KernelSU): Magic Mount.
- [RKSU](https://github.com/rsuntk/KernelsU): поддержка non-GKI.
- [susfs](https://gitlab.com/simonpunk/susfs4ksu): дополнение для скрытия root в ядре и модуль пространства пользователя для KernelSU.
- [KernelPatch](https://github.com/bmax121/KernelPatch): ключевая часть реализации модулей ядра в APatch.

<details>
<summary>Благодарности команды KernelSU</summary>

- [Kernel-Assisted Superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/): Идея KernelSU.
- [Magisk](https://github.com/topjohnwu/Magisk): Мощный инструмент для получения root-прав.
- [genuine](https://github.com/brevent/genuine/): Проверка подписи APK v2.
- [Diamorphine](https://github.com/m0nad/Diamorphine): Некоторые техники руткитов.
</details>
