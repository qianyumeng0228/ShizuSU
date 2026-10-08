# 非公式の対応デバイス

::: info 情報
ShizuSU は非 GKI デバイスの公式サポートを復活しました：4.x - 5.4 LTS カーネル（LTS モード）をカバーし、3.x（3.4 - 3.18）カーネルは追加のバックポートにより実験的にサポートされます。
:::

::: warning 警告
このページでは他の開発者が管理している、ShizuSU をサポートする GKI 以外のデバイス用のカーネルを紹介しています。
:::

::: warning 警告
このページはあなたのデバイスに対応するソースコードを見つけるためのものであり、そのソースコードが _ShizuSU 開発者_ によってレビューされたことを意味するものではありません。ご自身の責任においてご利用ください。
:::

<script setup>
import data from '../../repos.json'
</script>

<table>
   <thead>
      <tr>
         <th>メンテナー</th>
         <th>リポジトリ</th>
         <th>対応デバイス</th>
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