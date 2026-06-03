<template>
  <n-modal :show="show" preset="card" title="题目历史版本" @close="handleClose" style="width: 95vw; max-width: 1200px; height: 85vh">
    <div class="history-container">
      <!-- 历史版本列表 -->
      <div class="history-list">
        <n-card title="历史版本" size="small" style="height: 100%">
          <n-data-table
            remote
            :columns="historyColumns"
            :data="historyList"
            :pagination="historyPagination"
            :loading="loadingHistory"
            :row-key="historyRowKey"
            size="small"
            style="height: 100%"
            @update:page="handleHistoryPageChange"
            @update:page-size="handleHistoryPageSizeChange"
          />
        </n-card>
      </div>

      <!-- 版本详情预览 -->
      <div class="history-detail">
        <n-card title="版本详情" size="small" style="height: 100%">
          <template #header-extra>
            <n-space>
              <n-button v-if="selectedHistory" type="warning" size="small" :loading="reverting" @click="handleRevert"> 回退到此版本 </n-button>
              <n-button size="small" @click="handleClose">关闭</n-button>
            </n-space>
          </template>

          <div v-if="selectedHistory" class="history-content">
            <n-scrollbar style="max-height: 600px">
              <n-descriptions :column="2" bordered>
                <n-descriptions-item label="版本号">
                  {{ selectedHistory.revision }}
                </n-descriptions-item>
                <n-descriptions-item label="修改时间">
                  {{ formatDate(selectedHistory.revisionDate) }}
                </n-descriptions-item>
                <n-descriptions-item label="题目标题">
                  {{ selectedHistory.question?.title }}
                </n-descriptions-item>
                <n-descriptions-item label="题型">
                  <n-tag type="info" size="small">
                    {{ selectedHistory.question?.typeDescription }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="难度">
                  <n-tag :type="getDifficultyType(selectedHistory.question?.difficulty)" size="small">
                    {{ selectedHistory.question?.difficultyDescription }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="分值">
                  {{ selectedHistory.question?.score }}
                </n-descriptions-item>
                <n-descriptions-item label="状态">
                  <n-tag :type="selectedHistory.question?.enabled ? 'success' : 'warning'" size="small">
                    {{ selectedHistory.question?.enabled ? '启用' : '禁用' }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="知识点">
                  {{ selectedHistory.question?.knowledgePoint?.name || '-' }}
                </n-descriptions-item>
                <n-descriptions-item label="题目内容" span="2">
                  <div style="white-space: pre-wrap; max-height: 120px; overflow-y: auto">
                    {{ selectedHistory.question?.content }}
                  </div>
                </n-descriptions-item>
                <n-descriptions-item v-if="showOptions(selectedHistory.question?.type)" label="选项" span="2">
                  <div class="options-display">
                    <div v-for="option in parseOptions(selectedHistory.question?.options)" :key="option.key" class="option-item">
                      <n-tag size="small" style="margin-right: 8px">{{ option.key }}</n-tag>
                      {{ option.value }}
                    </div>
                  </div>
                </n-descriptions-item>
                <n-descriptions-item label="正确答案" span="2">
                  <n-tag type="success" size="small">
                    {{ selectedHistory.question?.answer }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="答案解析" span="2">
                  <div style="white-space: pre-wrap; max-height: 120px; overflow-y: auto">
                    {{ selectedHistory.question?.explanation || '-' }}
                  </div>
                </n-descriptions-item>
                <n-descriptions-item label="标签" span="2">
                  <div v-if="selectedHistory.question?.tags">
                    <n-tag v-for="tag in selectedHistory.question.tags.split(',')" :key="tag" size="small" style="margin-right: 4px">
                      {{ tag.trim() }}
                    </n-tag>
                  </div>
                  <span v-else>-</span>
                </n-descriptions-item>
              </n-descriptions>
            </n-scrollbar>
          </div>

          <div v-else class="empty-state">
            <n-empty description="请选择一个历史版本查看详情" />
          </div>
        </n-card>
      </div>
    </div>
  </n-modal>
</template>

<script lang="ts" setup>
import { NButton, NTag, useMessage, useModal } from 'naive-ui'
import { h, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getQuestionHistory, revertQuestion } from '@/api/question'

import type { DataTableColumns } from 'naive-ui'
import type { QuestionHistoryVO, QuestionType } from '@/api/types'

const props = defineProps<{
  show: boolean
  questionId: number | null
}>()

const emit = defineEmits(['update:show', 'reverted'])

const message = useMessage()
const modal = useModal()

const historyList = ref<QuestionHistoryVO[]>([])
const selectedHistory = ref<QuestionHistoryVO | null>(null)

const historyRowKey = (row: QuestionHistoryVO) => row.revision || 0

const historyPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: false,
  pageSizes: [10, 20, 30],
  onChange: (page: number) => {
    historyPagination.page = page
    fetchHistory()
  },
  onUpdatePageSize: (pageSize: number) => {
    historyPagination.pageSize = pageSize
    historyPagination.page = 1
    fetchHistory()
  }
})

// 获取历史版本列表
const { loading: loadingHistory, run: fetchHistory } = useRequest(
  () => {
    if (!props.questionId) return Promise.reject('questionId is required')
    return getQuestionHistory(props.questionId, {
      page: historyPagination.page,
      size: historyPagination.pageSize
    })
  },
  {
    manual: true,
    onSuccess: res => {
      if (res.code === 200) {
        historyList.value = res.data?.content || []
        historyPagination.itemCount = res.data?.total || 0

        // 自动选择第一个版本
        if (historyList.value.length > 0 && !selectedHistory.value) {
          selectedHistory.value = historyList.value[0]
        }
      } else {
        message.error(res.message || '获取历史版本失败')
      }
    },
    onError: () => {
      message.error('获取历史版本失败')
    }
  }
)

// 回退版本
const { loading: reverting, run: runRevert } = useRequest(({ questionId, revision }: { questionId: number; revision: number }) => revertQuestion(questionId, revision), {
  manual: true,
  onSuccess: res => {
    if (res.code === 200) {
      message.success('版本回退成功')
      emit('reverted')
      handleClose()
    } else {
      message.error(res.message || '版本回退失败')
    }
  },
  onError: () => {
    message.error('版本回退失败')
  }
})

// 历史版本表格列定义
const historyColumns: DataTableColumns<QuestionHistoryVO> = [
  {
    title: '版本号',
    key: 'revision',
    width: 80,
    render: row => h('span', { style: 'font-weight: bold' }, `v${row.revision}`)
  },
  {
    title: '修改时间',
    key: 'revisionDate',
    width: 160,
    render: row => formatDate(row.revisionDate)
  },
  {
    title: '题目标题',
    key: 'question.title',
    ellipsis: {
      tooltip: true
    },
    render: row => row.question?.title || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: row => [
      h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          ghost: true,
          onClick: () => handleSelectHistory(row)
        },
        { default: () => '查看' }
      )
    ]
  }
]

// 处理分页变化
function handleHistoryPageChange(page: number) {
  historyPagination.page = page
}

function handleHistoryPageSizeChange(pageSize: number) {
  historyPagination.pageSize = pageSize
  historyPagination.page = 1
}

// 选择历史版本
function handleSelectHistory(history: QuestionHistoryVO) {
  selectedHistory.value = history
}

// 回退版本
function handleRevert() {
  if (!selectedHistory.value || !props.questionId) return

  modal.create({
    title: '确认回退',
    content: `确定要回退到版本 v${selectedHistory.value.revision} 吗？此操作将创建一个新版本。`,
    positiveText: '确认回退',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: () => {
      if (selectedHistory.value && props.questionId) {
        runRevert({
          questionId: props.questionId,
          revision: selectedHistory.value.revision!
        })
      }
    }
  })
}

// 关闭模态框
function handleClose() {
  emit('update:show', false)
  selectedHistory.value = null
  historyList.value = []
  historyPagination.page = 1
}

// 格式化日期
function formatDate(date: Date | string | undefined) {
  if (!date) return '-'

  const d = new Date(date)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    return `今天 ${d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  } else if (diffDays === 1) {
    return `昨天 ${d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  } else if (d.getFullYear() === now.getFullYear()) {
    return d.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } else {
    return d.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 获取难度标签类型
function getDifficultyType(difficulty: string | undefined) {
  switch (difficulty) {
    case 'EASY':
      return 'success'
    case 'MEDIUM':
      return 'warning'
    case 'HARD':
      return 'error'
    default:
      return 'default'
  }
}

// 是否显示选项
function showOptions(type: QuestionType | undefined) {
  return type && ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(type)
}

// 解析选项
function parseOptions(options: string | undefined) {
  if (!options) return []

  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

// 监听弹窗显示状态
watch(
  () => props.show,
  show => {
    if (show && props.questionId) {
      fetchHistory()
    } else if (!show) {
      handleClose()
    }
  }
)
</script>

<style scoped>
.history-container {
  display: flex;
  gap: 16px;
  height: 100%;
}

.history-list {
  width: 40%;
  min-width: 400px;
}

.history-detail {
  flex: 1;
}

.history-content {
  padding: 16px 0;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.options-display {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 4px 0;
}
</style>
