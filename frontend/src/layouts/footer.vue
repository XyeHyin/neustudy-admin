<template>
  <n-layout-footer>
    <n-p>
      <n-a >&lt;/&gt;</n-a> with <n-text type="error">♥</n-text> by <n-a >XyeHyin</n-a>. Licensed under the
      <n-a >MIT</n-a> License.
    </n-p>
    <n-p>&copy; {{ new Date().getFullYear() }} Lei’s. All rights reserved.</n-p>
  </n-layout-footer>
</template>

<style scoped>
.n-layout-footer {
  opacity: 0.6;
  padding: 30px;
  text-align: center;
}
.n-a {
  text-decoration: none;
}
</style>
