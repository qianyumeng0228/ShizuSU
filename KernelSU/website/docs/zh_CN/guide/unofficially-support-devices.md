# 非官方支持设备

::: info
ShizuSU 已重新提供对非 GKI 设备的官方支持：覆盖 4.x - 5.4 LTS 内核（LTS 模式），3.x（3.4 - 3.18）内核通过额外 backport 提供实验性支持。
:::

::: warning
本文档列出由其他开发者维护的支持 ShizuSU 的非 GKI 设备内核
:::

::: warning
本文档仅方便查找设备对应源码，这并不意味该源码**被** ShizuSU 开发者**审查**，你应自行承担使用风险。
:::

<script setup>
import data from '../../repos.json'
</script>

<table>
   <thead>
      <tr>
         <th>维护者</th>
         <th>仓库地址</th>
         <th>支持设备</th>
      </tr>
   </thead>
   <tbody>
    <tr v-for="repo in data" :key="repo.devices">
        <td><a :href="repo.maintainer_link" target="_blank" rel="noreferrer">{{ repo.maintainer }}</a></td>
        <td><a :href="repo.kernel_link" target="_blank" rel="noreferrer">{{ repo.kernel_name }}</a></td>
        <td>{{ repo.devices }}</td>
    </tr>
   </tbody>
</table>