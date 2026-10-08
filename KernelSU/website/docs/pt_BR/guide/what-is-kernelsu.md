# O que é ShizuSU?

O ShizuSU é uma solução root para dispositivos Android GKI, funciona no modo kernel e concede privilégios root para apps do espaço do usuário diretamente no espaço do kernel.

## Características

A principal característica do ShizuSU é que ele é **baseado em kernel**. O ShizuSU funciona no modo kernel, portanto pode fornecer uma interface de kernel que nunca tivemos antes. Por exemplo, é possível adicionar pontos de interrupção de hardware a qualquer processo no modo kernel, acessar a memória física de qualquer processo de forma invisível, interceptar qualquer chamada de sistema (syscall) no espaço do kernel, entre outras funcionalidades.

Além disso, o ShizuSU é um fork de segunda geração do **SukiSU-Ultra**, e seu sistema de módulos é construído sobre o **Magic Mount** (da implementação do Magisk por 5ec1cff): o diretório `system` do módulo é sobreposto ao `/system` via bind mount de forma systemless, **sem necessidade de metamodule**. Veja [Sistema de Módulos: Magic Mount](metamodule.md).

## Recursos Estendidos

Além da base de root em nível de kernel, o ShizuSU traz uma série de recursos estendidos:

- **Suporte a Non-GKI / kernels antigos**: O ShizuSU restaura o suporte para dispositivos Non-GKI e GKI 1.0, cobrindo kernels 4.x - 5.4 LTS (3.x é experimental). Suporte de arquiteturas: `arm64-v8a` suporte total, `armeabi-v7a` suporte básico, `x86_64` suporte parcial.
- **Sistema de módulos baseado em Magic Mount**: o ShizuSU é um fork do SukiSU-Ultra; a montagem de módulos é construída na tecnologia Magic Mount do 5ec1cff, oferecendo uma base mais estável e confiável, e módulos Magisk funcionam diretamente. O ShizuSU não usa a arquitetura OverlayFS metamodule do KernelSU oficial — a descrição do sistema de módulos em todo o site é unificada como Magic Mount.
- **Módulos de kernel KPM**: Suporte total ao KernelPatch Module (KPM, portado do Apatch) para modificações e melhorias em nível de kernel.
- **App Profile**: Bloqueie privilégios root em um ambiente controlado via perfis de aplicativos; veja [App Profile](app-profile.md).
- **Personalização extensa**: Fundo do gerenciador personalizado, gerencie recursos susfs diretamente (sem módulo susfsforksu extra), ajuste o DPI — projete do seu jeito.

- **Suporte multi-manager**：Um kernel reconhece vários gerenciadores ao mesmo tempo através de uma tabela de assinaturas integrada (RKSU / KernelSU / WKSU / KowSU / KSUN / MKSU), com registro a quente e persistência (`/data/adb/shizusu/manager`) — sem precisar flashar o kernel para cada gerenciador.
- **Modo Stealth (furtividade)**：Basta escrever um sinalizador em `/data/adb/shizusu/stealth`; quando ativado, o relatório de informações não expõe mais o status do gerenciador aos aplicativos.
- **Conveniência de gerenciamento de módulos**：Backup/restauração de módulos e lista de permissões root, instalação em lote (coleta falhas sem interromper), ativar/desativar/desativar todos/desinstalar todos em um toque.
- **Aprimoramentos de ocultação**：Canal de consulta susfsd, ocultação de hooks KPROBES opcional (desligada por padrão), entrada de ocultação de hosts vinculada ao App Profile.

## Capacidades Herdadas e Integração {#inherited-abilities}

Desde seu nascimento, o ShizuSU integrou capacidades de várias soluções root maduras e ecossistemas de gerenciadores. Linhagem técnica:

| Capacidade | Fonte |
|---|---|
| `su` em nível de kernel e gerenciamento de root | KernelSU (projeto upstream) |
| Sistema de módulos Magic Mount | Magisk (herdado via MKSU e SukiSU-Ultra) |
| Suporte a non-GKI / kernels antigos | RKSU, SukiSU-Ultra |
| Módulos de kernel KPM | KernelPatch (implementação do APatch) |
| Gerenciamento de módulos, susfsd e ocultação | KernelSU-Next |
| Tabela de assinaturas multi-manager | ReSukiSU (referência) |
| Implementação stealth | 7kimisu (referência) |
| Patch de ocultação em nível de kernel | susfs |
| Verificação de assinatura APK v2 | genuine |

## Como usar o ShizuSU?

Por favor, consulte: [Instalação](installation)

## Como compilar o ShizuSU?

Por favor, consulte: [Como compilar](how-to-build)

## Discussão

- Telegram: [@KernelSU](https://t.me/KernelSU)
