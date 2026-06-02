<template>
  <n-result status="403" title="403 Forbidden" description="你没有权限访问此页面。" size="huge">
    <template #footer>
      <n-button @click="router.replace('/')">返回首页 →</n-button>
    </template>
  </n-result>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.n-result {
  margin: 20vh auto 0;
}
</style>
