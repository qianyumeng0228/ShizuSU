# Sistema de Módulos: Magic Mount {#introduction}

O ShizuSU é um fork de segunda geração do **SukiSU-Ultra**; seu sistema de módulos herda diretamente a solução **Magic Mount** do SukiSU-Ultra (da implementação do Magisk por 5ec1cff).

**Magic Mount é o único mecanismo de montagem de módulos do ShizuSU** — o ShizuSU não usa a arquitetura OverlayFS metamodule do KernelSU oficial, portanto não existe coexistência de "dois sistemas de módulos". Após instalar o ShizuSU, módulos que modificam arquivos `/system` funcionam imediatamente, sem necessidade de instalar metamodule.

## O Framework KernelSU e o Sistema de Módulos Magic Mount {#two-subsystems}

O ShizuSU consiste em dois subsistemas centrais **paralelos**, com responsabilidades claras e não sobrepostas:

| Subsistema | Responsabilidade | Descrição |
|---|---|---|
| **Framework de kernel KernelSU** | Autorização e gerenciamento de root | Roda no espaço do kernel: autorização `su`, controle de acesso por lista de permissões, privilégios root restritos (uid / gid / groups / capabilities / SELinux), interfaces de nível de kernel |
| **Sistema de módulos Magic Mount** | Instalação de módulos e montagem systemless | Sobrepoõe o diretório `system` do módulo à partição do sistema via **bind mount**, alcançando modificação sem tocar no sistema |

A divisão de trabalho pode ser resumida:

- **O framework KernelSU responde "quem pode obter root"**: a concessão, o isolamento e a auditoria de `su` acontecem no espaço do kernel e não podem ser contornados do espaço do usuário.
- **Magic Mount responde "como os módulos modificam o sistema"**: a montagem, sobreposição, mesclagem e ocultação de arquivos de módulos são feitas pelo Magic Mount, sem tocar em partições físicas.

Eles estão em **relação paralela**, não de contenção: o framework KernelSU fornece a capacidade de root, o Magic Mount fornece a capacidade de montagem de módulos, e juntos formam a experiência de root completa do ShizuSU.

## Por que Magic Mount? {#why-magic-mount}

O SukiSU-Ultra (e o ShizuSU, que o herda) escolheu o Magic Mount em vez da arquitetura OverlayFS metamodule do KernelSU oficial porque:

- **Base mais estável**: o Magic Mount vem da implementação madura do Magisk (5ec1cff), validada por muito tempo em muitos dispositivos e no ecossistema de módulos.
- **Melhor compatibilidade de módulos**: módulos do ecossistema Magisk que dependem da montagem do diretório `system` podem ser usados **diretamente** — sem metamodule, sem conversão.
- **Menor superfície de detecção**: a montagem é feita via bind mount; o ShizuSU não depende de características do OverlayFS, sendo mais difícil de ser detectado por apps.
- **Implantação mais simples**: não é necessário instalar meta-overlayfs ou outros metamodules; os módulos funcionam logo após a instalação do ShizuSU.

## Como o Magic Mount Funciona {#how-it-works}

O Magic Mount usa **bind mount** para "sobrepor" o conteúdo dos módulos aos diretórios do sistema:

1. Os módulos ficam em `/data/adb/modules/<ID-do-módulo>/`, onde o diretório `system/` corresponde à partição do sistema.
2. Na inicialização, o ShizuSU percorre todos os módulos habilitados e faz bind mount do diretório `system/` de cada módulo no caminho correspondente dentro de `/system`.
3. **Arquivos de mesmo nome**: os arquivos do módulo sobrescrevem os arquivos do sistema.
4. **Diretórios de mesmo nome**: o diretório do módulo é mesclado com o diretório do sistema (arquivos do módulo ficam na camada superior).
5. **Excluir arquivos do sistema**: montando o caminho correspondente do diretório do módulo como um diretório vazio (whiteout), o arquivo do sistema fica "oculto".
6. **Substituir diretórios do sistema**: montando o caminho correspondente como um diretório vazio, todo o diretório é substituído.

Todo o processo apenas lê os diretórios dos módulos e do sistema e **não modifica partições físicas** — esse é o significado de systemless (sem modificação do sistema).

## Diferenças do KernelSU Oficial {#difference}

| | KernelSU oficial | ShizuSU (baseado no SukiSU-Ultra) |
|---|---|---|
| Mecanismo de montagem de módulos | OverlayFS (requer metamodule, como meta-overlayfs) | **Magic Mount** (embutido, sem metamodule) |
| Módulos que modificam `/system` | Requerem instalar metamodule antes | Funcionam diretamente |
| Compatibilidade com módulos Magisk | Parcial (depende de metamodule) | Compatibilidade direta |

::: info Nota de migração
Se você usava o KernelSU oficial e instalou um metamodule (como meta-overlayfs), após migrar para o ShizuSU ele não é mais necessário: basta instalar e usar módulos comuns diretamente.
:::

## Desenvolvimento de Módulos {#module-dev}

A estrutura de módulos do ShizuSU é totalmente idêntica à do Magisk (`module.prop`, `system/`, `post-fs-data.sh`, `service.sh`, etc.), então desenvolvedores de módulos Magisk podem começar imediatamente. Veja o [Guia de Desenvolvimento de Módulos](module.md) para detalhes.
