# -*- coding: utf-8 -*-
import os

ROOT = r'I:\文档\sukisuultra\KernelSU\website\docs'

pairs = [
# ============ ru_RU ============
("ru_RU/index.md",
"""  - title: Система Metamodule
    details: Подключаемая модульная инфраструктура позволяет модифицировать /system без изменения системы. Установите metamodule (например meta-overlayfs) для включения монтирования модулей.""",
"""  - title: Модульная система Magic Mount
    details: Основана на Magic Mount (5ec1cff) от SukiSU-Ultra: монтирование модулей работает из коробки, модули Magisk совместимы напрямую."""),
("ru_RU/index.md",
"""  - title: Модульная система на основе Magic Mount
    details: Построена на технологии Magic Mount от 5ec1cff, обеспечивая более стабильную и надёжную основу для монтирования модулей.""",
"""  - title: На основе SukiSU-Ultra
    details: Форк второго поколения зрелого проекта сообщества, наследуя поддержку Non-GKI, Magic Mount, KPM и другое."""),
("ru_RU/guide/what-is-kernelsu.md",
"Кроме того, ShizuSU предоставляет [систему metamodule](metamodule.md), которая является подключаемой архитектурой для управления модулями. В отличие от традиционных root-решений, которые встраивают логику монтирования в свое ядро, ShizuSU делегирует это metamodules. Это позволяет устанавливать metamodules (например [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs)) для обеспечения бессистемных модификаций раздела `/system` и других разделов.",
"Кроме того, ShizuSU — это форк второго поколения **SukiSU-Ultra**, а его модульная система построена на **Magic Mount** (из реализации Magisk от 5ec1cff): каталог `system` модуля накладывается на `/system` через bind mount бессистемным способом, **без установки metamodule**. См. [Модульная система: Magic Mount](metamodule.md)."),
("ru_RU/guide/what-is-kernelsu.md",
"- **Модульная система на основе Magic Mount**: Монтирование модулей построено на технологии Magic Mount от 5ec1cff, обеспечивая более стабильную и надёжную основу, при этом сохраняя подключаемую архитектуру [metamodule](metamodule.md).",
"- **Модульная система на основе Magic Mount**: ShizuSU — форк SukiSU-Ultra; монтирование модулей построено на технологии Magic Mount от 5ec1cff, обеспечивая более стабильную и надёжную основу, и модули Magisk работают напрямую. ShizuSU не использует архитектуру OverlayFS metamodule официального KernelSU — описание модульной системы на всём сайте унифицировано как Magic Mount."),
("ru_RU/guide/module.md",
"ShizuSU использует архитектуру [метамодулей](metamodule.md) для монтирования директории `system`. **Только если вашему модулю нужно модифицировать файлы `/system`** (через директорию `system`), вам необходимо установить метамодуль (например, [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)). Другие функции модулей, такие как скрипты, правила sepolicy и system.prop, работают без метамодуля.",
"Модульная система ShizuSU основана на **Magic Mount** (из SukiSU-Ultra); для монтирования директории `system` не требуется устанавливать metamodule. Модули, изменяющие файлы `/system`, работают из коробки. См. [Модульная система: Magic Mount](metamodule.md)."),
("ru_RU/guide/module.md",
"Директория `system` монтируется только если у вас установлен метамодуль, предоставляющий функциональность монтирования (например, `meta-overlayfs`). Метамодуль обрабатывает способ монтирования модулей. См. [Руководство по метамодулям](metamodule.md) для получения дополнительной информации.",
"Модульная система ShizuSU основана на **Magic Mount** (из SukiSU-Ultra); каталог `system` монтируется напрямую без метамодуля. См. [Модульная система: Magic Mount](metamodule.md)."),
("ru_RU/guide/module.md",
"Если вы хотите удалить файл или папку в исходном каталоге системы, необходимо создать файл с тем же именем, что и файл/папка, в каталоге модуля с помощью команды `mknod filename c 0 0`. Таким образом, система overlayfs автоматически \"забелит\" этот файл, как если бы он был удален (раздел /system при этом фактически не изменится).",
"Если вы хотите удалить файл или папку в исходном каталоге системы, объявите в `customize.sh` переменную `REMOVE`, содержащую список каталогов. ShizuSU скроет эти файлы через Magic Mount (раздел /system при этом фактически не изменится)."),
("ru_RU/guide/module.md",
"Если вы хотите заменить каталог в системе, то необходимо создать каталог с тем же путем в каталоге модуля, а затем установить для этого каталога атрибут `setfattr -n trusted.overlay.opaque -v y <TARGET>`. Таким образом, система overlayfs автоматически заменит соответствующий каталог в системе (без изменения раздела /system).",
"Если вы хотите заменить каталог в системе, объявите переменную `REPLACE` в `customize.sh`. ShizuSU выполнит bind mount пустого каталога поверх целевого пути, полностью заменив его (без изменения раздела /system)."),
("ru_RU/guide/module.md",
"ShizuSU использует архитектуру [метамодулей](metamodule.md), где монтирование делегируется подключаемым метамодулям. Официальный метамодуль `meta-overlayfs` использует OverlayFS ядра для бессистемных модификаций, в то время как Magisk использует magic mount (bind mount), встроенный непосредственно в его ядро. Оба достигают одной цели: модификация файлов `/system` без физического изменения раздела `/system`. Подход ShizuSU обеспечивает большую гибкость и уменьшает поверхность обнаружения.",
"Официальный KernelSU использует механизм OverlayFS (metamodule) для бессистемных модификаций, тогда как ShizuSU, основанный на SukiSU-Ultra, использует **Magic Mount (bind mount)**, как и Magisk. Оба подхода достигают одной цели: модификация файлов `/system` без физического изменения раздела `/system`. ShizuSU использует только Magic Mount — двух модульных систем не существует. См. [Модульная система: Magic Mount](metamodule.md)."),
("ru_RU/guide/module.md",
"Если вы заинтересованы в использовании overlayfs, рекомендуется прочитать [документацию по overlayfs](https://docs.kernel.org/filesystems/overlayfs.html) ядра Linux.",
""),
("ru_RU/guide/module.md",
"| Монтирование OverlayFS (metamodule) | Да | Да |",
"| Монтирование модулей Magic Mount | Да | Да |"),
("ru_RU/guide/module.md",
"  5. Выполнить скрипт монтирования metamodule (OverlayFS)",
"  5. Выполнить монтирование модулей Magic Mount"),
("ru_RU/guide/module.md",
"Этот скрипт выполняется до монтирования OverlayFS, аналогично `post-fs-data.sh` в стандартном процессе.",
"Этот скрипт выполняется до монтирования модулей, аналогично `post-fs-data.sh` в стандартном процессе."),
("ru_RU/guide/installation.md",
"""::: warning МЕТАМОДУЛЬ ДЛЯ МОДИФИКАЦИИ СИСТЕМНЫХ ФАЙЛОВ
Если вы хотите использовать модули, которые модифицируют файлы `/system`, вам необходимо установить **метамодуль** после установки ShizuSU. Модули, которые используют только скрипты, sepolicy или system.prop, работают без метамодуля.
:::

**Для поддержки модификации `/system`**, пожалуйста, см. [Руководство по метамодулям](metamodule.md), чтобы:
- Понять, что такое метамодули и зачем они нужны
- Установить официальный метамодуль `meta-overlayfs`
- Узнать о других вариантах метамодулей""",
"""::: info МОДУЛЬНАЯ СИСТЕМА ОСНОВАНА НА MAGIC MOUNT
Модульная система ShizuSU основана на **Magic Mount** (из SukiSU-Ultra). Модули работают сразу после установки ShizuSU — модули, изменяющие файлы `/system`, не требуют дополнительного метамодуля.
:::

О принципах работы модульной системы и разработке модулей см. [Модульная система: Magic Mount](metamodule.md) и [Руководство по модулям](module.md)."""),
("ru_RU/guide/faq.md",
"Да, большинство модулей Magisk работают с ShizuSU. Однако, если вашему модулю нужно модифицировать файлы `/system`, вам необходимо установить [метамодуль](metamodule.md) (например, `meta-overlayfs`). Другие функции модулей работают без метамодуля. Проверьте [Руководство по модулям](module.md) для получения дополнительной информации.",
"Да. Модульная система ShizuSU основана на Magic Mount (из SukiSU-Ultra); модули Magisk работают напрямую, а модули, изменяющие файлы `/system`, не требуют метамодуля. Проверьте [Руководство по модулям](module.md) для получения дополнительной информации."),
("ru_RU/guide/faq.md",
"""Если вашим модулям нужно модифицировать файлы `/system`, вам необходимо установить [метамодуль](metamodule.md) для монтирования директории `system`. Другие функции модулей (скрипты, sepolicy, system.prop) работают без метамодуля.

**Решение**: См. [Руководство по метамодулям](metamodule.md) для инструкций по установке.""",
"""Проверьте: включен ли модуль, содержит ли его каталог корректный `module.prop`, совместим ли модуль с вашим устройством/ядром. Модульная система ShizuSU основана на Magic Mount; модули, изменяющие файлы `/system`, не требуют метамодуля.

**Решение**: См. [Руководство по модулям](module.md) и [Модульная система: Magic Mount](metamodule.md)."""),
("ru_RU/guide/faq.md",
"""## Что такое метамодуль и зачем он мне нужен?

Метамодуль - это специальный модуль, который предоставляет инфраструктуру для монтирования обычных модулей. См. [Руководство по метамодулям](metamodule.md) для полного объяснения.""",
"""## Что такое модульная система ShizuSU?

ShizuSU — форк второго поколения SukiSU-Ultra; его модульная система использует **Magic Mount** (bind mount каталога `system` модуля на `/system`), без метамодуля и с прямой совместимостью с модулями Magisk. Архитектура OverlayFS metamodule официального KernelSU не применяется к ShizuSU. См. [Модульная система: Magic Mount](metamodule.md)."""),
("ru_RU/guide/app-profile.md",
"ShizuSU предоставляет бессистемный механизм модификации системных разделов, реализуемый через монтирование overlayfs. Однако некоторые приложения могут быть чувствительны к такому поведению. Поэтому мы можем выгрузить модули, смонтированные в этих приложениях, установив опцию \"размонтирование модулей\".",
"ShizuSU предоставляет бессистемный механизм модификации системных разделов, реализуемый через Magic Mount (bind mount). Однако некоторые приложения могут быть чувствительны к такому поведению. Поэтому мы можем выгрузить модули, смонтированные в этих приложениях, установив опцию \"размонтирование модулей\"."),
("ru_RU/guide/difference-with-magisk.md",
"- Метод замены или удаления файлов в модулях ShizuSU полностью отличается от Magisk. ShizuSU не поддерживает метод `.replace`. Вместо этого необходимо создать одноименный файл с помощью команды `mknod filename c 0 0` для удаления соответствующего файла.",
"- Метод замены или удаления файлов в модулях ShizuSU такой же, как у Magisk: используйте переменные `REMOVE` и `REPLACE` в `customize.sh`, чтобы удалять файлы или заменять каталоги (см. [Руководство по модулям](module.md))."),

# ============ pt_BR ============
("pt_BR/index.md",
"""  - title: Sistema Metamodule
    details: Infraestrutura de módulos plugável permite modificações systemless em /system. Instale um metamodule como meta-overlayfs para habilitar a montagem de módulos.""",
"""  - title: Sistema de módulos Magic Mount
    details: Construído sobre o Magic Mount (5ec1cff) herdado do SukiSU-Ultra, a montagem de módulos funciona imediatamente e módulos Magisk são diretamente compatíveis."""),
("pt_BR/index.md",
"""  - title: Sistema de módulos baseado em Magic Mount
    details: Construído na tecnologia Magic Mount do 5ec1cff, oferecendo uma base de montagem de módulos mais estável e confiável.""",
"""  - title: Baseado no SukiSU-Ultra
    details: Fork de segunda geração de um projeto comunitário maduro, herdando suporte a Non-GKI, Magic Mount, KPM e mais."""),
("pt_BR/guide/what-is-kernelsu.md",
"Além disso, o ShizuSU fornece um [sistema metamodule](metamodule.md), que é uma arquitetura plugável para gerenciamento de módulos. Diferente das soluções root tradicionais que integram a lógica de montagem em seu núcleo, o ShizuSU delega isso aos metamodules. Isso permite que você instale metamodules como [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/tree/main/userspace/meta-overlayfs) para fornecer modificações systemless na partição `/system` e outras partições.",
"Além disso, o ShizuSU é um fork de segunda geração do **SukiSU-Ultra**, e seu sistema de módulos é construído sobre o **Magic Mount** (da implementação do Magisk por 5ec1cff): o diretório `system` do módulo é sobreposto ao `/system` via bind mount de forma systemless, **sem necessidade de metamodule**. Veja [Sistema de Módulos: Magic Mount](metamodule.md)."),
("pt_BR/guide/what-is-kernelsu.md",
"- **Sistema de módulos baseado em Magic Mount**: A montagem de módulos é construída na tecnologia Magic Mount do 5ec1cff, oferecendo uma base mais estável e confiável, mantendo a arquitetura [metamodule](metamodule.md) plugável.",
"- **Sistema de módulos baseado em Magic Mount**: o ShizuSU é um fork do SukiSU-Ultra; a montagem de módulos é construída na tecnologia Magic Mount do 5ec1cff, oferecendo uma base mais estável e confiável, e módulos Magisk funcionam diretamente. O ShizuSU não usa a arquitetura OverlayFS metamodule do KernelSU oficial — a descrição do sistema de módulos em todo o site é unificada como Magic Mount."),
("pt_BR/guide/module.md",
"""::: warning METAMODULE NECESSÁRIO APENAS PARA MODIFICAÇÃO DE ARQUIVOS DO SISTEMA
ShizuSU usa uma arquitetura [metamodule](metamodule.md) para montar o diretório `system`. **Somente se seu módulo precisar modificar arquivos `/system`** (via diretório `system`), você precisa instalar um metamodule (como [meta-overlayfs](https://github.com/qianyumeng0228/ShizuSU/releases)). Outros recursos de módulos como scripts, regras sepolicy e system.prop funcionam sem um metamodule.
:::""",
"""::: info SISTEMA DE MÓDULOS BASEADO EM MAGIC MOUNT
O sistema de módulos do ShizuSU é baseado no **Magic Mount** (do SukiSU-Ultra); não é necessário instalar metamodule para montar o diretório `system`. Módulos que modificam arquivos `/system` funcionam imediatamente. Veja [Sistema de Módulos: Magic Mount](metamodule.md).
:::"""),
("pt_BR/guide/module.md",
"O conteúdo deste diretório será sobreposto à partição `/system` do sistema usando OverlayFS após a inicialização do sistema. Isso significa que:",
"O conteúdo deste diretório será sobreposto à partição `/system` do sistema usando **Magic Mount (bind mount)** após a inicialização do sistema. Isso significa que:"),
("pt_BR/guide/module.md",
"Se você deseja excluir um arquivo ou pasta no diretório original do sistema, você precisa criar um arquivo com o mesmo nome do arquivo/pasta no diretório do módulo usando `mknod filename c 0 0`. Dessa forma, o sistema OverlayFS irá automaticamente \"branquear\" este arquivo como se ele tivesse sido excluído (a partição /system não foi realmente alterada).",
"Se você deseja excluir um arquivo ou pasta no diretório original do sistema, você pode declarar uma variável `REMOVE` contendo uma lista de diretórios em `customize.sh`. O ShizuSU ocultará esses arquivos via Magic Mount (a partição /system não é realmente alterada)."),
("pt_BR/guide/module.md",
"Se você deseja substituir um diretório no sistema, você precisa criar um diretório com o mesmo caminho no diretório do módulo e, em seguida, definir o atributo `setfattr -n trusted.overlay.opaque -v y <TARGET>` para este diretório. Desta forma, o sistema OverlayFS substituirá automaticamente o diretório correspondente no sistema (sem alterar a partição /system).",
"Se você deseja substituir um diretório no sistema, você pode declarar uma variável `REPLACE` em `customize.sh`. O ShizuSU fará bind mount de um diretório vazio sobre o caminho de destino, substituindo-o por completo (sem alterar a partição /system)."),
("pt_BR/guide/module.md",
"O mecanismo sem sistema do ShizuSU é implementado através do OverlayFS do kernel, enquanto o Magisk atualmente usa montagem mágica (montagem de ligação). Os dois métodos de implementação têm diferenças significativas, mas o objetivo final é o mesmo: modificar os arquivos `/system` sem modificar fisicamente a partição `/system`.",
"O KernelSU oficial usa um mecanismo OverlayFS (metamodule) para modificações systemless, enquanto o ShizuSU, baseado no SukiSU-Ultra, usa **Magic Mount (bind mount)**, como o Magisk. Ambas as abordagens têm o mesmo objetivo: modificar os arquivos `/system` sem modificar fisicamente a partição `/system`. O ShizuSU usa apenas Magic Mount — não existem dois sistemas de módulos. Veja [Sistema de Módulos: Magic Mount](metamodule.md)."),
("pt_BR/guide/module.md",
"Se você estiver interessado em OverlayFS, é recomendável ler a [documentação sobre OverlayFS](https://docs.kernel.org/filesystems/overlayfs.html) do kernel Linux.",
""),
("pt_BR/guide/module.md",
"| Montagem OverlayFS (metamodule) | Sim | Sim |",
"| Montagem de módulos Magic Mount | Sim | Sim |"),
("pt_BR/guide/module.md",
"  5. Executar script de montagem do metamodule (OverlayFS)",
"  5. Executar montagem de módulos Magic Mount"),
("pt_BR/guide/module.md",
"Este script é executado antes da montagem do OverlayFS, similar ao `post-fs-data.sh` no fluxo padrão.",
"Este script é executado antes da montagem dos módulos, similar ao `post-fs-data.sh` no fluxo padrão."),
("pt_BR/guide/module.md",
"`post-mount.sh` é executado no OverlayFS montado.",
"`post-mount.sh` é executado quando a montagem dos módulos é concluída."),
("pt_BR/guide/installation.md",
"""## Pós-instalação: Suporte a Módulos {#post-installation}

::: warning METAMODULE PARA MODIFICAÇÃO DE ARQUIVOS DO SISTEMA
Se você deseja usar módulos que modificam arquivos `/system`, você precisa instalar um **metamodule** após instalar o ShizuSU. Módulos que usam apenas scripts, sepolicy ou system.prop funcionam sem um metamodule.
:::

**Para suporte à modificação de `/system`**, consulte o [Guia de Metamodule](metamodule.md) para:
- Entender o que são metamodules e por que são necessários
- Instalar o metamodule oficial `meta-overlayfs`
- Conhecer outras opções de metamodule""",
"""## Pós-instalação: Suporte a Módulos {#post-installation}

::: info SISTEMA DE MÓDULOS BASEADO EM MAGIC MOUNT
O sistema de módulos do ShizuSU é baseado no **Magic Mount** (do SukiSU-Ultra). Os módulos funcionam logo após a instalação do ShizuSU — módulos que modificam arquivos `/system` não precisam de metamodule extra.
:::

Para o funcionamento do sistema de módulos e desenvolvimento de módulos, consulte [Sistema de Módulos: Magic Mount](metamodule.md) e o [Guia de Módulos](module.md)."""),
("pt_BR/guide/faq.md",
"Sim, a maioria dos módulos Magisk funcionam no ShizuSU. No entanto, se seu módulo precisar modificar arquivos `/system`, você precisa instalar um [metamodule](metamodule.md) (como `meta-overlayfs`). Outros recursos de módulos funcionam sem um metamodule. Confira o [Guia de módulos](module.md) para mais informações.",
"Sim. O sistema de módulos do ShizuSU é baseado no Magic Mount (do SukiSU-Ultra); módulos Magisk funcionam diretamente e módulos que modificam arquivos `/system` não precisam de metamodule. Confira o [Guia de módulos](module.md) para mais informações."),
("pt_BR/guide/faq.md",
"""Se seus módulos precisam modificar arquivos `/system`, você precisa instalar um [metamodule](metamodule.md) para montar o diretório `system`. Outros recursos de módulos (scripts, sepolicy, system.prop) funcionam sem um metamodule.

**Solução**: Consulte o [Guia de Metamodule](metamodule.md) para instruções de instalação.""",
"""Verifique: o módulo está habilitado, seu diretório contém um `module.prop` válido e o módulo é compatível com seu dispositivo/kernel. O sistema de módulos do ShizuSU é baseado no Magic Mount; módulos que modificam arquivos `/system` não precisam de metamodule.

**Solução**: Consulte o [Guia de Módulos](module.md) e [Sistema de Módulos: Magic Mount](metamodule.md)."""),
("pt_BR/guide/faq.md",
"""## O que é um metamodule e por que preciso dele?

Um metamodule é um módulo especial que fornece infraestrutura para montar módulos regulares. Consulte o [Guia de Metamodule](metamodule.md) para uma explicação completa.""",
"""## Qual é o sistema de módulos do ShizuSU?

O ShizuSU é um fork de segunda geração do SukiSU-Ultra; seu sistema de módulos usa **Magic Mount** (bind mount do diretório `system` do módulo sobre `/system`), sem metamodule e com compatibilidade direta com módulos Magisk. A arquitetura OverlayFS metamodule do KernelSU oficial não se aplica ao ShizuSU. Veja [Sistema de Módulos: Magic Mount](metamodule.md)."""),
("pt_BR/guide/app-profile.md",
"O ShizuSU fornece um mecanismo sem sistema para modificar partições do sistema, obtido através da montagem do OverlayFS. No entanto, alguns apps podem ser sensíveis a esse comportamento. Nesse caso, podemos descarregar módulos montados nesses apps configurando a opção \"Desmontar módulos\".",
"O ShizuSU fornece um mecanismo sem sistema para modificar partições do sistema, obtido através do Magic Mount (bind mount). No entanto, alguns apps podem ser sensíveis a esse comportamento. Nesse caso, podemos descarregar módulos montados nesses apps configurando a opção \"Desmontar módulos\"."),
("pt_BR/guide/difference-with-magisk.md",
"- **Arquitetura de montagem de módulos**: ShizuSU usa um [sistema metamodule](metamodule.md), delegando a montagem a metamodules plugáveis (por exemplo, `meta-overlayfs`), enquanto o Magisk tem a montagem integrada em seu núcleo. ShizuSU requer instalar um metamodule para habilitar a montagem de módulos.",
"- **Arquitetura de montagem de módulos**: o ShizuSU, baseado no SukiSU-Ultra, usa **Magic Mount (bind mount)** para a montagem de módulos, assim como o Magisk; o KernelSU oficial usa a arquitetura OverlayFS metamodule."),
("pt_BR/guide/difference-with-magisk.md",
"- O método para substituir ou excluir arquivos nos módulos do ShizuSU é completamente diferente do Magisk. O ShizuSU não suporta o método `.replace`. Em vez disso, você deve criar um arquivo com o comando `mknod filename c 0 0` para excluir o arquivo correspondente.",
"- O método para substituir ou excluir arquivos nos módulos do ShizuSU é o mesmo do Magisk: use as variáveis `REMOVE` e `REPLACE` em `customize.sh` para excluir arquivos ou substituir diretórios (veja o [Guia de Módulos](module.md))."),
]

missed = []
for rel, old, new in pairs:
    p = os.path.join(ROOT, rel)
    with open(p, encoding='utf-8', newline='') as f:
        t = f.read()
    t = t.replace('\r\n', '\n')
    if old not in t:
        missed.append(rel + ' :: ' + old[:70])
        continue
    t = t.replace(old, new)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('OK', rel)

print('MISSED:', len(missed))
for m in missed:
    print(m)
