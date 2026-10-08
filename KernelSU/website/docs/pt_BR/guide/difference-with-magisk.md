# Diferenças com Magisk

Embora os módulos do ShizuSU e do Magisk tenham muitas semelhanças, existem inevitavelmente algumas diferenças devido aos seus mecanismos de implementação completamente diferentes. Se você deseja que seu módulo funcione tanto no Magisk quanto no ShizuSU, é essencial compreender essas diferenças.

## Semelhanças

- Formato de arquivo do módulo: Ambos usam o formato ZIP para organizar os módulos, e o formato dos módulos é praticamente o mesmo.
- Diretório de instalação do módulo: Ambos estão localizados em `/data/adb/modules`.
- Sem sistema: Ambos suportam a modificação de `/system` de forma sem sistema por meio de módulos.
- post-fs-data.sh: O tempo de execução e a semântica são exatamente os mesmos.
- service.sh: O tempo de execução e a semântica são exatamente os mesmos.
- system.prop: Completamente o mesmo.
- sepolicy.rule: Completamente o mesmo.
- BusyBox: Os scripts são executados no BusyBox com o "Modo Autônomo" ativado em ambos os casos.

## Diferenças

Antes de entender as diferenças, é importante saber como identificar se o seu módulo está sendo executado no ShizuSU ou no Magisk. Você pode usar a variável de ambiente `KSU` para diferenciá-lo em todos os locais onde você pode executar os scripts do módulo (`customize.sh`, `post-fs-data.sh`, `service.sh`). No ShizuSU, essa variável de ambiente será definida como `true`.

Aqui estão algumas diferenças:

- Os módulos ShizuSU não podem ser instalados no modo Recovery.
- Os módulos ShizuSU não oferece suporte nativo ao Zygisk, mas você pode usar módulos Zygisk através do [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext).
- **Arquitetura de montagem de módulos**: o ShizuSU, baseado no SukiSU-Ultra, usa **Magic Mount (bind mount)** para a montagem de módulos, assim como o Magisk; o KernelSU oficial usa a arquitetura OverlayFS metamodule.
- O método para substituir ou excluir arquivos nos módulos do ShizuSU é o mesmo do Magisk: use as variáveis `REMOVE` e `REPLACE` em `customize.sh` para excluir arquivos ou substituir diretórios (veja o [Guia de Módulos](module.md)).
- Os diretórios do BusyBox são diferentes. O BusyBox integrado no ShizuSU está localizado em `/data/adb/ksu/bin/busybox`, enquanto no Magisk está em `/data/adb/magisk/busybox`. **Observe que este é um comportamento interno do ShizuSU e pode mudar no futuro!**
- O ShizuSU não suporta arquivos `.replace`, mas oferece suporte às variáveis ​​`REMOVE` e `REPLACE` para remover ou substituir arquivos e pastas.
- O ShizuSU adiciona um script `boot-completed.sh` para executar tarefas após a conclusão da inicialização do sistema Android.
- O ShizuSU adiciona um script `post-mount.sh` para executar tarefas após a conclusão da montagem do módulo.
