# Perguntas frequentes

## ShizuSU oferece suporte ao meu dispositivo?

O ShizuSU suporta dispositivos rodando Android com bootloader desbloqueado. No entanto, o suporte oficial é apenas para kernels Linux GKI 5.10+ (na prática isso significa que seu dispositivo precisa ter Android 12 de fábrica para ser compatível).

Você pode verificar facilmente o suporte para o seu dispositivo através do gerenciador do ShizuSU, que está disponível [aqui](https://github.com/qianyumeng0228/ShizuSU/releases). 

Se o app mostrar `Não instalado`, significa que seu dispositivo é oficialmente suportado pelo ShizuSU.

Se o app mostrar `Sem suporte`, significa que seu dispositivo não é oficialmente suportado no momento. No entanto, você pode compilar o código-fonte do kernel e integrar o ShizuSU para fazê-lo funcionar, ou usar [Dispositivos com suporte não oficial](unofficially-support-devices).

## Para usar o ShizuSU precisa desbloquear o bootloader?

Certamente, sim.

## ShizuSU suporta módulos?

Sim. O sistema de módulos do ShizuSU é baseado no Magic Mount (do SukiSU-Ultra); módulos Magisk funcionam diretamente e módulos que modificam arquivos `/system` não precisam de metamodule. Confira o [Guia de módulos](module.md) para mais informações.

## ShizuSU suporta Xposed?

Sim, você pode usar LSPosed (ou outro derivado moderno do Xposed) com [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext).

## ShizuSU suporta Zygisk?

ShizuSU não tem suporte integrado ao Zygisk, mas você pode usar um módulo como [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) para suportá-lo.

## Por que meus módulos não funcionam após uma instalação nova?

Verifique: o módulo está habilitado, seu diretório contém um `module.prop` válido e o módulo é compatível com seu dispositivo/kernel. O sistema de módulos do ShizuSU é baseado no Magic Mount; módulos que modificam arquivos `/system` não precisam de metamodule.

**Solução**: Consulte o [Guia de Módulos](module.md) e [Sistema de Módulos: Magic Mount](metamodule.md).

## Qual é o sistema de módulos do ShizuSU?

O ShizuSU é um fork de segunda geração do SukiSU-Ultra; seu sistema de módulos usa **Magic Mount** (bind mount do diretório `system` do módulo sobre `/system`), sem metamodule e com compatibilidade direta com módulos Magisk. A arquitetura OverlayFS metamodule do KernelSU oficial não se aplica ao ShizuSU. Veja [Sistema de Módulos: Magic Mount](metamodule.md).

## ShizuSU é compatível com o Magisk?

O sistema de módulos do ShizuSU está em conflito com a montagem mágica do Magisk. Se houver algum módulo ativado no ShizuSU, todo o Magisk deixará de funcionar.

No entanto, se você usar apenas o `su` do ShizuSU, ele funcionará bem com o Magisk. O ShizuSU modifica o `kernel`, enquanto o Magisk modifica o `ramdisk`, permitindo que ambos trabalhem juntos.

## ShizuSU substituirá o Magisk?

Acreditamos que não, e esse não é o nosso objetivo. O Magisk é bom o suficiente para solução root do espaço do usuário e terá uma longa vida. O objetivo do ShizuSU é fornecer uma interface de kernel aos usuários, não substituindo o Magisk.

## ShizuSU oferece suporte a dispositivos não-GKI?

É possível. Mas você deve baixar o código-fonte do kernel e integrar o ShizuSU à árvore do código-fonte e compilar o kernel você mesmo.

## ShizuSU oferece suporte a dispositivos abaixo do Android 12?

É o kernel do dispositivo que afeta a compatibilidade do ShizuSU e não tem nada a ver com a versão do Android. A única restrição é que os dispositivos lançados com Android 12 devem ser kernel 5.10+ (dispositivos GKI). Então:

1. Os dispositivos lançados com Android 12 devem ser compatíveis.
2. Dispositivos com kernel antigo (alguns dispositivos com Android 12 também têm o kernel antigo) são compatíveis (você mesmo deve compilar o kernel).

## ShizuSU suporta kernel antigo?

É possível, o ShizuSU é portado para o kernel 4.14 agora. Para kernels mais antigo, você precisa portar manualmente e PRs são sempre bem-vindas!

## Como integrar o ShizuSU para um kernel antigo?

Por favor, verifique o guia [Integração para dispositivos não-GKI](how-to-integrate-for-non-gki).

## Por que a minha versão do Android é 13 e o kernel mostra "android12-5.10"?

A versão do Kernel não tem nada a ver com a versão do Android. Se você precisar fazer o flash do kernel, use sempre a versão do kernel, a versão do Android não é tão importante.

## Eu sou GKI 1.0, posso usar isso?

GKI 1.0 é completamente diferente do GKI 2.0, você deve compilar o kernel sozinho.

## Como posso fazer `/system` RW?

Não recomendamos que você modifique a partição do sistema diretamente. Por favor, verifique [Guias de módulo](module.md) para modificá-lo sem sistema. Se você insiste em fazer isso, verifique [magisk_overlayfs](https://github.com/HuskyDG/magic_overlayfs).

## ShizuSU pode modificar hosts? Como posso usar AdAway?

Claro. Mas o ShizuSU não tem suporte a hosts integrados, você pode instalar um módulo como [systemless-hosts](https://github.com/symbuzzer/systemless-hosts-KernelSU-module) para fazer isso.
