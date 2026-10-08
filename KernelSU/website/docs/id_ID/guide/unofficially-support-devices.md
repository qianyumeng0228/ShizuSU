# Perangkat Yang Didukung Tidak Resmi

::: info
ShizuSU telah mengembalikan dukungan resmi untuk perangkat non-GKI: mencakup kernel 4.x - 5.4 LTS (mode LTS), dan kernel 3.x (3.4 - 3.18) didukung secara eksperimental melalui backport tambahan.
:::

:::peringatan

di halaman ini, terdapat kernel untuk perangkat non-GKI yang mendukung ShizuSU yang dikelola oleh pengembang lain.

:::

:::peringatan

Halaman ini hanya untuk Anda yang ingin menemukan kode sumber yang sesuai dengan perangkat Anda, itu **BUKAN** berarti kode sumber telah ditinjau oleh _ShizuSU Developers_. Anda harus menggunakannya dengan risiko Anda sendiri.

:::

<script setup>
import data from '../../repos.json'
</script>

<table>
   <thead>
      <tr>
         <th>Pengelola</th>
         <th>Repository</th>
         <th>Perangkat yang didukung</th>
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
