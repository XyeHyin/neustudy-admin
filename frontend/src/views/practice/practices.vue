<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">练习</n-h1>
      <div class="admin-toolbar">
        <n-select v-model:value="paperFilter" :options="paperOptions" clearable placeholder="选择试卷" style="width: 200px" @update:value="handleSearch" />
        <n-select v-model:value="submittedFilter" :options="submittedOptions" clearable placeholder="提交状态" style="width: 120px" @update:value="handleSearch" />
        <n-button type="primary" :loading="startingPractice" @click="handleStartPractice" :disabled="!paperFilter || startingPractice">开始练习</n-button>
      </div>

      <n-tabs v-model:value="activeTab" type="line">
        <n-tab-pane name="available" tab="可用试卷">
          <n-data-table
            v-permission="'practice:list:self'"
            remote
            :columns="paperColumns"
            :data="availablePapers"
            :pagination="paperPagination"
            :loading="loadingPapers"
            :bordered="false"
            :row-key="paperRowKey"
            @update:page="handlePaperPageChange"
            @update:page-size="handlePaperPageSizeChange"
          />
        </n-tab-pane>

        <n-tab-pane name="records" tab="练习记录">
          <n-data-table
            v-permission="'practice:record:list:self'"
            remote
            :columns="recordColumns"
            :data="practiceRecords"
            :pagination="recordPagination"
            :loading="loadingRecords"
            :bordered="false"
            :row-key="recordRowKey"
            @update:page="handleRecordPageChange"
            @update:page-size="handleRecordPageSizeChange"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <!-- 练习会话模态框 -->
    <practice-session-modal :show="showPracticeModal" :session-id="currentSessionId" :submitting="submittingPractice" @update:show="updatePracticeModalShow" @submit="handlePracticeSubmit" />

    <n-modal :show="submittingPractice" :mask-closable="false" :close-on-esc="false" transform-origin="center">
      <div class="grading-feedback-panel">
        <n-spin size="large" />
        <div class="grading-feedback-title">正在提交并判分</div>
        <div class="grading-feedback-text">如果包含主观题，AI 判题可能需要等待一小会儿，请不要关闭页面。</div>
      </div>
    </n-modal>

    <!-- 练习结果模态框 -->
    <practice-result-modal :show="showResultModal" :result="practiceResult" :stats="currentRecordStats" @update:show="updateResultModalShow" />

    <!-- 试卷数据分析模态框 -->
    <paper-analysis-modal :show="showAnalysisModal" :paper-data="selectedPaperData" @update:show="updateAnalysisModalShow" />
  </div>
</template>

<script lang="ts" setup>
import { getPaperStatusText as getStatusText, getPaperStatusType as getStatusType } from '@/utils/status'
import { NButton, NProgress, NTag, useMessage } from 'naive-ui'
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getAvailablePapersWithStats, getMyPracticeSessions, getPracticeResult, startPractice, submitPractice } from '@/api/practice'
import PaperAnalysisModal from '@/components/paper/paper-analysis-modal.vue'
import PracticeResultModal from '@/components/practice/practice-result-modal.vue'
import PracticeSessionModal from '@/components/practice/practice-session-modal.vue'
import { Icon } from '@/components'
import { SUBMITTED_FILTER_OPTIONS as submittedOptions } from '@/constants/options'
import { formatDateTime } from '@/utils/datetime'

import type { PracticePaperStatVO, PracticeRecordVO, PracticeResultVO, PracticeSubmitDTO } from '@/api/types'

const message = useMessage()

// 筛选条件
const paperFilter = ref<number | null>(null)
const submittedFilter = ref<boolean | null>(null)
const activeTab = ref('available')

// 数据
const availablePapers = ref<PracticePaperStatVO[]>([])
const practiceRecords = ref<PracticeRecordVO[]>([])
const practiceResult = ref<PracticeResultVO | null>(null)

// 分页
const paperPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const recordPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 模态框状态
const showPracticeModal = ref(false)
const showResultModal = ref(false)
const showAnalysisModal = ref(false)
const currentSessionId = ref<number | null>(null)
const selectedPaperData = ref<PracticePaperStatVO | null>(null)
const startingPaperId = ref<number | null>(null)

// 选项


const paperOptions = computed(() =>
  availablePapers.value.map(paper => ({
    label: paper.paperTitle || '',
    value: paper.paperId || 0
  }))
)

// 试卷表格列
const paperColumns = [
  {
    title: '分数',
    key: 'maxScore',
    width: 100,
    render: (row: PracticePaperStatVO) => {
      // 全对则显示绿色对勾
      if (row.maxScore === row.totalScore) {
        return h(Icon, { type: 'materialCheckCircle', color: 'green' })
      }
      if (row.maxScore === 0) {
        return h(Icon, { type: 'materialClose', color: 'red' })
      }
      const percent = Math.floor(((row.maxScore || 0) / (row.totalScore || 1)) * 100)
      let color = ''
      if (percent < 60) {
        color = 'red'
      } else if (percent < 85) {
        color = 'gold'
      } else {
        color = 'green'
      }
      return h('span', { style: { color } }, `${percent}`)
    }
  },
  {
    title: '试卷',
    key: 'paperTitle',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '教师',
    key: 'teacherName',
    width: 120
  },
  {
    title: '总分',
    key: 'totalScore',
    width: 80
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: PracticePaperStatVO) => h(NTag, { type: getStatusType(row.status ?? '') }, { default: () => getStatusText(row.status ?? '') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row: PracticePaperStatVO) => {
      const actions = []

      if (row.status === 'PUBLISHED') {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              loading: startingPractice.value && startingPaperId.value === row.paperId,
              disabled: startingPractice.value,
              onClick: () => handleStartPracticeWithPaper(row.paperId!)
            },
            { default: () => '开始练习' }
          )
        )
      }

      // 添加数据分析按钮
      if (row.totalAttempts && row.totalAttempts > 0) {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'info',
              onClick: () => handleShowAnalysis(row)
            },
            { default: () => '数据分析' }
          )
        )
      }

      return h('div', { style: 'display: flex; gap: 8px;' }, actions)
    }
  }
]

// 练习记录表格列
const recordColumns = [
  {
    title: '试卷',
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
        showIndicator: false,
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
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row: PracticeRecordVO) => {
      const actions = []

      if (row.submitted) {
        actions.push(h(NButton, { size: 'small', type: 'primary', onClick: () => handleViewResult(row.practiceSessionId) }, { default: () => '查看结果' }))
      } else {
        actions.push(h(NButton, { size: 'small', type: 'info', onClick: () => handleContinuePractice(row.practiceSessionId) }, { default: () => '继续练习' }))
      }

      return h('div', { style: 'display: flex; gap: 8px;' }, actions)
    }
  }
]

// 获取可用试卷
const { loading: loadingPapers, run: fetchPapers } = useRequest(getAvailablePapersWithStats, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      availablePapers.value = res.data
    } else {
      message.error(res.message || '获取可用试卷失败')
    }
  }
})

// 获取练习记录
const { loading: loadingRecords, run: fetchRecords } = useRequest(
  () =>
    getMyPracticeSessions({
      page: recordPagination.page,
      size: recordPagination.pageSize,
      submitted: submittedFilter.value ?? undefined
    }),
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        practiceRecords.value = res.data.content
        recordPagination.itemCount = res.data.total
      } else {
        message.error(res.message || '获取练习记录失败')
      }
    }
  }
)

// 开始练习
const { loading: startingPractice, runAsync: runStartPractice } = useRequest(startPractice, { manual: true })

// 提交练习
const { loading: submittingPractice, runAsync: runSubmitPractice } = useRequest(submitPractice, { manual: true })

// 新增：当前记录的统计
const currentRecordStats = reactive({ maxScore: 0, minScore: 0 })

function paperRowKey(row: PracticePaperStatVO) {
  return row.paperId
}

function recordRowKey(row: PracticeRecordVO) {
  return row.practiceSessionId
}

function syncSubmittedPaperScore(result: PracticeResultVO) {
  const index = availablePapers.value.findIndex(paper => paper.paperId === result.paperId)
  if (index < 0) return

  const paper = availablePapers.value[index]
  const previousAttempts = paper.totalAttempts ?? 0
  const nextAttempts = previousAttempts + 1
  const previousMax = paper.maxScore
  const previousMin = paper.minScore
  const nextMaxScore = previousMax == null ? result.totalScore : Math.max(previousMax, result.totalScore)
  const nextMinScore = previousMin == null ? result.totalScore : Math.min(previousMin, result.totalScore)
  const previousAvg = paper.avgScore ?? 0
  const nextAvgScore = previousAttempts > 0 ? (previousAvg * previousAttempts + result.totalScore) / nextAttempts : result.totalScore

  availablePapers.value[index] = {
    ...paper,
    totalAttempts: nextAttempts,
    maxScore: nextMaxScore,
    minScore: nextMinScore,
    avgScore: nextAvgScore,
    hasFullScore: paper.totalScore != null ? nextMaxScore >= paper.totalScore : paper.hasFullScore,
    scoreTrend: [...(paper.scoreTrend || []), result.totalScore]
  }

  currentRecordStats.maxScore = nextMaxScore
  currentRecordStats.minScore = nextMinScore
}

function refreshPracticeData() {
  fetchPapers()
  fetchRecords()
}

// 事件处理
function handleSearch() {
  if (activeTab.value === 'records') {
    recordPagination.page = 1
    fetchRecords()
  } else {
    paperPagination.page = 1
    fetchPapers()
  }
}

function handlePaperPageChange(page: number) {
  paperPagination.page = page
  fetchPapers()
}
function handlePaperPageSizeChange(size: number) {
  paperPagination.pageSize = size
  paperPagination.page = 1
  fetchPapers()
}

function handleRecordPageChange(page: number) {
  recordPagination.page = page
  fetchRecords()
}
function handleRecordPageSizeChange(size: number) {
  recordPagination.pageSize = size
  recordPagination.page = 1
  fetchRecords()
}
async function handleStartPractice() {
  if (!paperFilter.value) {
    message.warning('请选择试卷')
    return
  }

  await handleStartPracticeWithPaper(paperFilter.value)
}

async function handleStartPracticeWithPaper(paperId: number) {
  startingPaperId.value = paperId
  try {
    const res = await runStartPractice({ paperId })
    if (res.code === 200) {
      currentSessionId.value = res.data.id
      showPracticeModal.value = true
    } else {
      message.error(res.message || '开始练习失败')
    }
  } catch (error: any) {
    // 409 冲突，已有未提交练习
    if (error.response?.status === 409) {
      message.warning('已有未提交练习，自动继续中...')
      // 重新拉取记录，找到该试卷未提交的会话
      await fetchRecords()
      const record = practiceRecords.value.find(r => (r as any).paperId === paperId && !r.submitted)
      if (record) {
        handleContinuePractice((record as any).practiceSessionId)
      } else {
        message.error('未找到未提交的练习记录')
      }
    } else {
      message.error(error.message || '开始练习失败')
    }
  } finally {
    startingPaperId.value = null
  }
}

async function handlePracticeSubmit(data: PracticeSubmitDTO) {
  try {
    const res = await runSubmitPractice(data)
    if (res.code === 200) {
      message.success('提交成功')
      practiceResult.value = res.data
      syncSubmittedPaperScore(res.data)
      showPracticeModal.value = false
      showResultModal.value = true
      refreshPracticeData()
    } else {
      message.error(res.message || '提交失败')
    }
  } catch (error: any) {
    message.error(error.message || '提交失败')
  }
}

function handleContinuePractice(sessionId: number) {
  currentSessionId.value = sessionId
  showPracticeModal.value = true
}

async function handleViewResult(sessionId: number) {
  try {
    // 拿当前会话的结果
    const res = await getPracticeResult(sessionId)
    if (res.code === 200) {
      practiceResult.value = res.data
      // 找到对应的练习记录，取 max/min
      const rec = practiceRecords.value.find(r => r.practiceSessionId === sessionId)
      currentRecordStats.maxScore = rec?.maxScore ?? 0
      currentRecordStats.minScore = rec?.minScore ?? 0

      showResultModal.value = true
    } else {
      message.error(res.message || '获取练习结果失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取练习结果失败')
  }
}

// 将原来的 displayAnalysisModal 重命名为 handleShowAnalysis
function handleShowAnalysis(paperData: PracticePaperStatVO) {
  selectedPaperData.value = paperData
  showAnalysisModal.value = true
}

function updatePracticeModalShow(show: boolean) {
  showPracticeModal.value = show
}

function updateResultModalShow(show: boolean) {
  showResultModal.value = show
}

function updateAnalysisModalShow(show: boolean) {
  showAnalysisModal.value = show
}

watch(activeTab, tab => {
  if (tab === 'available') {
    paperPagination.page = 1
    fetchPapers()
  } else if (tab === 'records') {
    recordPagination.page = 1
    fetchRecords()
  }
})

onMounted(() => {
  fetchPapers()
  fetchRecords()
})
</script>

<style scoped>
.grading-feedback-panel {
  width: min(360px, calc(100vw - 48px));
  padding: 28px 24px;
  border-radius: 8px;
  background: var(--n-color, #fff);
  box-shadow: var(--n-box-shadow, 0 12px 40px rgba(0, 0, 0, 0.16));
  text-align: center;
}

.grading-feedback-title {
  margin-top: 16px;
  font-size: var(--text-subtitle, 18px);
  font-weight: var(--weight-semibold, 600);
}

.grading-feedback-text {
  margin-top: 8px;
  color: var(--n-text-color-2, #606266);
  line-height: var(--leading-body, 1.6);
}
</style>
