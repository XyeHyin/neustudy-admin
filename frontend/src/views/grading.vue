<template>
  <div class="grading-page">
    <n-card :bordered="false" class="grading-card">
      <n-h1 class="grading-title">判分管理</n-h1>
      <n-space :size="16" style="margin-bottom: 16px">
        <!-- 新：试卷下拉 -->
        <n-select v-model:value="filterPaperId" :options="paperOptions" placeholder="请选择试卷" clearable style="width: 200px" @update:value="handleSearch" />
        <!-- 新：题目关键词 -->
        <n-input v-model:value="searchQuestionTitle" placeholder="搜索题目" clearable style="width: 240px" @input="handleSearch" />
      </n-space>
      <n-tabs v-model:value="activeTab" type="line">
        <n-tab-pane name="review" tab="待复核列表">
          <n-data-table
            remote
            :columns="reviewColumns"
            :data="reviewList"
            :loading="loadingReview"
            :bordered="false"
            :row-key="reviewRowKey"
            :pagination="reviewPagination"
            @update:page="handleReviewPageChange"
            @update:page-size="handleReviewSizeChange"
          />
        </n-tab-pane>

        <n-tab-pane name="results" tab="判分结果">
          <n-data-table
            remote
            :columns="resultColumns"
            :data="gradingResults"
            :loading="loadingResults"
            :bordered="false"
            :row-key="resultRowKey"
            :pagination="pagination"
            @update:page="handlePageChange"
            @update:page-size="handleResultSizeChange"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <!-- 人工复核模态框 -->
    <grading-review-modal :show="showReviewModal" :grading-result="selectedGradingResult" :loading="reviewing" @update:show="updateReviewModalShow" @submit="handleReviewSubmit" />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NTag, useMessage } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getGradingResult, listGradingReview, listReviewedGradingResult, manualGrading } from '@/api/grading'
import { getPapers } from '@/api/paper'
import GradingReviewModal from '@/components/grading/grading-review-modal.vue'
import { useAuthStore } from '@/store/auth'

import type { GradingResultVO, GradingReviewVO, ManualGradingDTO, PaperListVO } from '@/api/types'

const message = useMessage()

// ========== 新增：试卷列表 & 筛选状态 ==========
const filterPaperId = ref<number | null>(null)
const searchQuestionTitle = ref('')

const paperOptions = ref<{ label: string; value: number }[]>([])
const { run: fetchPaperOptions } = useRequest(() => getPapers({ page: 1, size: 1000 }), {
  onSuccess: res => {
    if (res.code === 200) {
      paperOptions.value = res.data.content.map((p: PaperListVO) => ({
        label: p.title,
        value: p.id
      }))
    }
  }
})

// ========== 搜索 & 分页状态 ==========
const reviewPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const activeTab = ref('review')
const reviewList = ref<GradingReviewVO[]>([])
const gradingResults = ref<GradingResultVO[]>([])
const selectedGradingResult = ref<GradingReviewVO | null>(null)
const showReviewModal = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 待复核列表表格列
const reviewColumns = [
  {
    title: '学生姓名',
    key: 'studentName',
    width: 120
  },
  {
    title: '题目',
    key: 'questionTitle',
    ellipsis: { tooltip: true }
  },
  {
    title: 'AI评分',
    key: 'aiScore',
    width: 80,
    render: (row: GradingReviewVO) => `${row.aiScore} 分`
  },
  {
    title: 'AI评语',
    key: 'aiComment',
    ellipsis: { tooltip: true },
    width: 200
  },
  {
    title: 'AI判分时间',
    key: 'aiGradingTime',
    width: 180,
    render: (row: GradingReviewVO) => new Date(row.aiGradingTime).toLocaleString()
  },
  {
    title: '状态',
    key: 'reviewed',
    width: 80,
    render: (row: GradingReviewVO) => h(NTag, { type: row.reviewed ? 'success' : 'warning' }, { default: () => (row.reviewed ? '已复核' : '待复核') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row: GradingReviewVO) => {
      if (row.reviewed) {
        return h(NButton, { size: 'small', onClick: () => handleViewResult(row.gradingResultId) }, { default: () => '查看结果' })
      } else {
        return h(NButton, { size: 'small', type: 'primary', onClick: () => handleReview(row) }, { default: () => '复核' })
      }
    }
  }
]

// 判分结果表格列
const resultColumns = [
  {
    title: '学生答案ID',
    key: 'studentAnswerId',
    width: 120
  },
  {
    title: 'AI评分',
    key: 'aiScore',
    width: 80,
    render: (row: GradingResultVO) => `${row.aiScore} 分`
  },
  {
    title: '最终得分',
    key: 'finalScore',
    width: 80,
    render: (row: GradingResultVO) => `${row.finalScore} 分`
  },
  {
    title: 'AI评语',
    key: 'aiComment',
    ellipsis: { tooltip: true }
  },
  {
    title: '教师评语',
    key: 'teacherComment',
    ellipsis: { tooltip: true }
  },
  {
    title: '复核教师',
    key: 'reviewTeacherName',
    width: 100
  },
  {
    title: '复核时间',
    key: 'reviewTime',
    width: 180,
    render: (row: GradingResultVO) => (row.reviewTime ? new Date(row.reviewTime).toLocaleString() : '-')
  }
]

// 获取待复核列表
const { loading: loadingReview, run: fetchReviewList } = useRequest((params: Record<string, any>) => listGradingReview(params), {
  manual: true,
  onSuccess: (res: any) => {
    if (res.code === 200) {
      // 新：使用分页字段
      reviewList.value = res.data.content
      reviewPagination.itemCount = res.data.total
    } else {
      message.error(res.message || '获取待复核列表失败')
    }
  }
})

// 人工复核
const { loading: reviewing, runAsync: runManualGrading } = useRequest(manualGrading, { manual: true })

// 判分结果分页
const { loading: loadingResults, run: fetchGradingResults } = useRequest((params: Record<string, any>) => listReviewedGradingResult(params), {
  manual: true,
  onSuccess: (res: any) => {
    if (res.code === 200) {
      gradingResults.value = res.data.content
      pagination.itemCount = res.data.total
    } else {
      message.error(res.message || '获取判分结果失败')
    }
  }
})

const authStore = useAuthStore()

function getCurrentTeacherId(): number {
  return authStore.user?.id || 0
}

// 工具函数
function reviewRowKey(row: GradingReviewVO) {
  return row.gradingResultId
}

function resultRowKey(row: GradingResultVO) {
  return row.gradingResultId
}

// 事件处理
function handleReview(row: GradingReviewVO) {
  selectedGradingResult.value = row
  showReviewModal.value = true
}

// 用于处理 grading-review-modal 的 @update:show 事件
function updateReviewModalShow(val: boolean) {
  showReviewModal.value = val
}

async function handleViewResult(gradingResultId: number) {
  try {
    const res = await getGradingResult(gradingResultId)
    if (res.code === 200) {
      // 这里可以弹窗展示详情，或将详情赋值到某个ref用于展示
      message.info('查看判分结果详情')
      // 例如：selectedResultDetail.value = res.data
    } else {
      message.error(res.message || '获取判分结果失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取判分结果失败')
  }
}

// ========== 修改：人工复核提交后重新拉取 ==========
async function handleReviewSubmit(data: ManualGradingDTO) {
  try {
    const teacherId = getCurrentTeacherId()
    const res = await runManualGrading(data, teacherId)
    if (res.code === 200) {
      message.success('复核成功')
      showReviewModal.value = false
      // 带上当前分页和筛选条件重新加载列表
      fetchReviewList(buildReviewParams(reviewPagination.page, reviewPagination.pageSize))
    } else {
      message.error(res.message || '复核失败')
    }
  } catch (error: any) {
    message.error(error.message || '复核失败')
  }
}

// 构造查询参数
function buildReviewParams(page: number, size: number) {
  const params: Record<string, any> = { page, size }
  if (filterPaperId.value) params.paperId = filterPaperId.value
  const kw = searchQuestionTitle.value.trim()
  if (kw) params.questionTitle = kw
  return params
}

// 构造判分结果查询参数
function buildResultParams(page: number, size: number) {
  const params: Record<string, any> = { page, size }
  if (filterPaperId.value) params.paperId = filterPaperId.value
  const kw = searchQuestionTitle.value.trim()
  if (kw) params.questionTitle = kw
  return params
}

// ========== 事件处理 & 初始化 ==========
function handleSearch() {
  // 搜索时两个分页都重置到第一页
  reviewPagination.page = 1
  pagination.page = 1
  fetchReviewList(buildReviewParams(1, reviewPagination.pageSize))
  fetchGradingResults(buildResultParams(1, pagination.pageSize))
}
function handleReviewPageChange(page: number) {
  reviewPagination.page = page
  fetchReviewList(buildReviewParams(page, reviewPagination.pageSize))
}
function handleReviewSizeChange(size: number) {
  reviewPagination.pageSize = size
  reviewPagination.page = 1
  fetchReviewList(buildReviewParams(1, size))
}

// 判分结果分页切换
function handlePageChange(page: number) {
  pagination.page = page
  fetchGradingResults(buildResultParams(page, pagination.pageSize))
}
function handleResultSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  fetchGradingResults(buildResultParams(1, size))
}

onMounted(() => {
  fetchPaperOptions()
  fetchReviewList(buildReviewParams(reviewPagination.page, reviewPagination.pageSize))
  fetchGradingResults(buildResultParams(pagination.page, pagination.pageSize))
})
</script>

<style scoped>
.grading-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.grading-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.grading-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
</style>

