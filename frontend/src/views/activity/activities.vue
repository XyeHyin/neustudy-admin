<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">活动记录</n-h1>
      <n-data-table
        ref="dataTableRef"
        :columns="columns"
        :data="activities"
        :bordered="false"
        :pagination="pagination"
        style="margin-top: 24px"
        :loading="loading"
        :row-key="rowKey"
        remote
      />
    </n-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getEventLogs } from '@/api/event-log'

const message = useMessage()
const activities = ref<any[]>([])
const loading = ref(false)
const dataTableRef = ref()
const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [5, 10, 20, 50],
  onChange: (page: number) => {
    pagination.value.page = page
    fetchActivities()
  },
  onUpdatePageSize: (size: number) => {
    pagination.value.pageSize = size
    pagination.value.page = 1
    fetchActivities()
  }
})

const columns = [
  { title: '时间', key: 'createTime', render(row: any) { return formatActivityTime(row.createTime) }, width: 160 },
  { title: '用户', key: 'username', width: 120 },
  { title: '操作', key: 'description', ellipsis: { tooltip: true } },
  { title: '方法', key: 'httpMethod', width: 80 },
  { title: '路径', key: 'uri', ellipsis: { tooltip: true } }
]

const rowKey = (row: any) => row.id

const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await getEventLogs({ page: pagination.value.page, size: pagination.value.pageSize })
    if (res.data && res.data.content) {
      activities.value = res.data.content
      pagination.value.itemCount = res.data.total
    }
  } catch (e) {
    message.error('获取活动记录失败')
  } finally {
    loading.value = false
  }
}

function formatActivityTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  if (diffHours < 1) return '刚刚'
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  fetchActivities()
})
</script>
