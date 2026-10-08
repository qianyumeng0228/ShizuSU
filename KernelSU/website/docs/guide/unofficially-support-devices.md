# Unofficially supported devices

::: info
ShizuSU has restored official support for non-GKI devices: 4.x - 5.4 LTS kernels (LTS mode), with experimental support for 3.x (3.4 - 3.18) via additional backports.
:::

::: warning
In this page, there are kernels for non-GKI devices supporting ShizuSU maintained by other developers.
:::

::: warning
This page is intended only to help you find the source code corresponding to your device. It **DOES NOT** mean that the source code has been reviewed by ShizuSU developers. You should use it at your own risk.
:::

<script setup>
import data from '../repos.json'
</script>

<table>
   <thead>
      <tr>
         <th>Maintainer</th>
         <th>Repository</th>
         <th>Support devices</th>
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
