# Recursos ocultos

## .ksurc

Por padrão, `/system/bin/sh` carrega `/system/etc/mkshrc`.

Você pode fazer su carregar um arquivo rc personalizado criando um arquivo `/data/adb/ksu/.ksurc`.

## Personalização {#customization}

- **Fundo personalizado**: Altere a imagem de fundo nas configurações do Gerenciador ShizuSU para personalizar a interface.
- **Gerenciamento susfs**: Gerencie alguns recursos susfs diretamente no Gerenciador, sem módulo susfsforksu extra.
- **Ajuste de DPI**: Ajuste a exibição de DPI do Gerenciador para diferentes telas.

## WebUI X {#webui-x}

Suporta a implementação de WebUI de nova geração (WebUI X) do MMRL, para interação de módulos mais rica.

## Suporte Multi-manager {#multi-manager}

O ShizuSU inclui uma tabela de assinaturas de gerenciadores integrada, permitindo que um kernel reconheça vários gerenciadores de root ao mesmo tempo: RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU. Além das assinaturas integradas, também há registro a quente e persistência (`/data/adb/shizusu/manager`) — sem precisar flashar o kernel ao instalar um novo gerenciador.

## Modo Stealth {#stealth}

Escreva um sinalizador em `/data/adb/shizusu/stealth` para ativar o modo stealth. Quando ativado, o relatório de informações do ShizuSU não expõe mais o status atual do gerenciador aos aplicativos, reduzindo o risco de detecção.

## Aprimoramentos de Ocultação {#hiding-enhancements}

- **Canal de consulta susfsd**：Canal de comunicação susfsd integrado, trabalhando diretamente com o patch de kernel susfs.
- **Ocultação de hooks KPROBES**：Opcional, desligada por padrão.
- **Entrada de ocultação de hosts**：Vinculada ao App Profile, pode ocultar alterações no arquivo hosts feitas por módulos.
