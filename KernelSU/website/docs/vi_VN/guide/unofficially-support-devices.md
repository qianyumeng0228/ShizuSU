# Thiết bị hỗ trợ không chính thức

::: info
ShizuSU đã khôi phục hỗ trợ chính thức cho các thiết bị không phải GKI: bao phủ kernel 4.x - 5.4 LTS (chế độ LTS), kernel 3.x (3.4 - 3.18) được hỗ trợ thử nghiệm thông qua backport bổ sung.
:::

::: warning
Đây là trang liệt kê kernel cho các thiết bị không dùng GKI được hỗ trợ bởi các lập trình viên khác.

:::

::: warning
Trang này chỉ để cho bạn tìm thấy source cho thiết bị của bạn, nó **HOÀN TOÀN KHÔNG** được review bởi _lập trình viên của ShizuSU_. Vậy nên hãy chấp nhận rủi ro khi sử dụng chúng.

:::

<script setup>
import data from '../../repos.json'
</script>

<table>
   <thead>
      <tr>
         <th>Người bảo trì</th>
         <th>Kho lưu trữ</th>
         <th>Thiết bị hỗ trợ</th>
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
