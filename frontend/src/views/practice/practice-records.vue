<template>
  <div class="practice-records-page">
    <n-card :bordered="false" class="practice-records-card">
      <n-h1 class="practice-records-title">练习记录管理</n-h1>

      <!-- 筛选工具栏 -->
      <div class="practice-records-toolbar">
        <n-select v-model:value="paperFilter" :options="paperOptions" clearable placeholder="选择试卷" style="width: 200px" @update:value="handleSearch" />
        <n-select v-model:value="submittedFilter" :options="submittedOptions" clearable placeholder="提交状态" style="width: 120px" @update:value="handleSearch" />
        <n-button type="primary" @click="handleSearch">
          <template #icon>
            <Icon type="materialSearch" />
          </template>
          搜索
        </n-button>
        <n-button @click="handleReset">重置</n-button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <n-card size="small" class="stat-card">
          <n-statistic label="总记录数" :value="totalRecords" />
        </n-card>
        <n-card size="small" class="stat-card">
          <n-statistic label="已提交" :value="submittedRecords" />
        </n-card>
        <n-card size="small" class="stat-card">
          <n-statistic label="未提交" :value="unsubmittedRecords" />
        </n-card>
        <n-card size="small" class="stat-card">
          <n-statistic label="平均得分" :value="averageScore.toFixed(1)" suffix="分" />
        </n-card>
      </div>
      <n-data-table
        remote
        :columns="columns"
        :data="practiceRecords"
        :pagination="pagination"
        :loading="loading"
        :bordered="false"
        :row-key="rowKey"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 练习详情模态框 -->
    <practice-detail-modal :show="showDetailModal" :record="selectedRecord" @update:show="updateDetailModalShow" />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NProgress, NTag, useMessage } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getPapers } from '@/api/paper'
import { listPracticeRecords } from '@/api/practice'
import { Icon } from '@/components'
import { SUBMITTED_FILTER_OPTIONS as submittedOptions } from '@/constants/options'
import PracticeDetailModal from '@/components/practice/practice-detail-modal.vue'
import { formatDateTime } from '@/utils/datetime'

import type { PaperListVO, PracticeRecordVO } from '@/api/types'

const message = useMessage()

// 筛选条件
const paperFilter = ref<number | null>(null)
const submittedFilter = ref<boolean | null>(null)
const studentNameFilter = ref('')
const dateRange = ref<[number, number] | null>(null)

// 数据
const practiceRecords = ref<PracticeRecordVO[]>([])
const availablePapers = ref<PaperListVO[]>([])
const selectedRecord = ref<PracticeRecordVO | null>(null)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

// 模态框状态
const showDetailModal = ref(false)

// 选项


const paperOptions = computed(() => [
  { label: '全部试卷', value: null },
  ...availablePapers.value.map(paper => ({
    label: paper.title,
    value: paper.id
  }))
])

// 统计数据
const totalRecords = computed(() => practiceRecords.value.length)
const submittedRecords = computed(() => practiceRecords.value.filter(r => r.submitted).length)
const unsubmittedRecords = computed(() => practiceRecords.value.filter(r => !r.submitted).length)
const averageScore = computed(() => {
  const submitted = practiceRecords.value.filter(r => r.submitted)
  if (submitted.length === 0) return 0
  return submitted.reduce((sum, r) => sum + r.totalScore, 0) / submitted.length
})

// 表格列定义
const columns = [
  {
    title: '试卷标题',
    key: 'paperTitle',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '得分',
    key: 'totalScore',
    width: 100,
    render: (row: PracticeRecordVO) => `${row.totalScore ?? 0} 分`
  },
  {
    title: '正确率',
    key: 'correctRate',
    width: 120,
    render: (row: PracticeRecordVO) => {
      const percentage = Math.round(row.correctRate * 100)
      return h(NProgress, {
        type: 'line',
        percentage,
        showIndicator: true,
        height: 18,
        borderRadius: 9
      })
    }
  },
  {
    title: '状态',
    key: 'submitted',
    width: 100,
    render: (row: PracticeRecordVO) =>
      h(
        NTag,
        {
          type: row.submitted ? 'success' : 'warning'
        },
        {
          default: () => (row.submitted ? '已提交' : '未提交')
        }
      )
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 180,
    render: (row: PracticeRecordVO) => formatDateTime(row.startTime)
  },
  {
    title: '提交时间',
    key: 'submitTime',
    width: 180,
    render: (row: PracticeRecordVO) => formatDateTime(row.submitTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row: PracticeRecordVO) => {
      const actions = []

      actions.push(
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => handleViewDetail(row)
          },
          { default: () => '查看详情' }
        )
      )

      return h('div', { style: 'display: flex; gap: 8px;' }, actions)
    }
  }
]

// 获取练习记录
const { loading, run: fetchRecords } = useRequest(
  () => {
    const params: any = {
      page: pagination.page,
      size: pagination.pageSize
    }

    if (paperFilter.value) params.paperId = paperFilter.value
    if (submittedFilter.value !== null) params.submitted = submittedFilter.value
    if (studentNameFilter.value) params.studentName = studentNameFilter.value
    if (dateRange.value) {
      params.startDate = new Date(dateRange.value[0]).toISOString()
      params.endDate = new Date(dateRange.value[1]).toISOString()
    }

    return listPracticeRecords(params)
  },
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        if (res.data.content) {
          // 分页数据
          practiceRecords.value = res.data.content
          pagination.itemCount = res.data.total
        } else {
          // 非分页数据
          practiceRecords.value = res.data
          pagination.itemCount = res.data.length
        }
      } else {
        message.error(res.message || '获取练习记录失败')
      }
    }
  }
)

// 获取试卷列表
const { run: fetchPapers } = useRequest(() => getPapers({ page: 1, size: 1000 }), {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      availablePapers.value = res.data.content || res.data
    }
  }
})

function rowKey(row: PracticeRecordVO) {
  return row.practiceSessionId
}

// 事件处理
function handleSearch() {
  pagination.page = 1
  fetchRecords()
}

function handleReset() {
  paperFilter.value = null
  submittedFilter.value = null
  studentNameFilter.value = ''
  dateRange.value = null
  pagination.page = 1
  fetchRecords()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchRecords()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchRecords()
}

function handleViewDetail(record: PracticeRecordVO) {
  selectedRecord.value = record
  showDetailModal.value = true
}

function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

onMounted(() => {
  fetchPapers()
  fetchRecords()
})
</script>

<style scoped>
.practice-records-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.practice-records-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.practice-records-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}

.practice-records-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}
</style>

