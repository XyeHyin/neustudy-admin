<template>
  <n-result :status="status" :title="title" :description="description" size="huge">
    <template #footer>
      <n-button @click="router.replace('/')">Back to Home →</n-button>
    </template>
  </n-result>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const statusCode = computed(() => Number(route.query.status || 404))
const status = computed(() => (statusCode.value === 500 ? '500' : '404'))
const title = computed(() => (statusCode.value === 500 ? '500 Server Error' : '404 Not Found'))
const description = computed(() => {
  if (statusCode.value >= 500) return '服务器开了个小差，请稍后再试。'
  return 'You know life is always ridiculous.'
})
</script>

<style scoped>
.n-result {
  margin: 20vh auto 0;
}
</style>
