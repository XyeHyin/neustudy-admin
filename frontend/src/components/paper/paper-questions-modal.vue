<template>
  <n-modal :show="show" preset="card" title="管理试卷题目" @close="handleClose" style="width: 95vw; max-width: 1400px; height: 85vh">
    <div class="questions-manager">
      <div class="left-panel">
        <n-card title="题库" size="small">
          <template #header-extra>
            <n-input v-model:value="searchKeyword" placeholder="搜索题目" clearable size="small" style="width: 200px" />
          </template>

          <div class="filters">
            <n-space>
              <n-select v-model:value="typeFilter" :options="typeOptions" clearable placeholder="题型" size="small" style="width: 120px" />
              <n-select v-model:value="difficultyFilter" :options="difficultyOptions" clearable placeholder="难度" size="small" style="width: 100px" />
              <n-select v-model:value="knowledgePointFilter" :options="knowledgePointOptions" clearable placeholder="知识点" size="small" style="width: 180px" />
            </n-space>
          </div>

          <n-data-table
            remote
            :columns="questionColumns"
            :data="availableQuestions"
            :pagination="questionPagination"
            :loading="loadingQuestions"
            :row-key="questionRowKey"
            :checked-row-keys="selectedQuestionIds"
            @update:checked-row-keys="updateSelectedQuestions"
            @update:page="handleQuestionPageChange"
            size="small"
            style="margin-top: 12px"
          />

          <div class="action-buttons">
            <n-button type="primary" :disabled="!selectedQuestionIds.length" @click="addSelectedQuestions"> 添加选中题目 ({{ selectedQuestionIds.length }}) </n-button>
          </div>
        </n-card>
      </div>

      <div class="right-panel">
        <n-card title="试卷题目" size="small" style="display: flex; flex-direction: column; height: 100%">
          <template #header-extra>
            <n-space justify="space-between" style="width: 100%">
              <n-text depth="3">总分：{{ totalScore }} 分</n-text>
              <n-button type="success" @click="saveQuestions" :loading="saving">保存</n-button>
            </n-space>
          </template>

          <!-- 新增的滚动列表区域 -->
          <div class="question-list-wrapper">
            <n-list bordered size="small">
              <draggable tag="div" v-model="paperQuestions" handle=".drag-handle" :animation="200" @end="updateOrderNumbers">
                <!-- draggable 的直接子节点 -->
                <n-list-item v-for="(row, idx) in paperQuestions" :key="paperQuestionRowKey(row)">
                  <n-space justify="space-between" align="center" style="width: 100%">
                    <n-space align="center">
                      <span class="drag-handle">☰</span>
                      <n-text>{{ row.orderNum }}.</n-text>
                      <n-text ellipsis style="max-width: 400px">{{ row.title }}</n-text>
                      <n-tag type="info">{{ getQuestionTypeText(row.type) }}</n-tag>
                    </n-space>
                    <n-space align="center" size="small">
                      <n-input-number v-model:value="row.score" size="small" min="1" max="100" style="width: 80px" />
                      <n-button
                        size="small"
                        type="error"
                        quaternary
                        @click="removeQuestion(row.questionId, idx)"
                        :loading="row.questionId != null && removingIds.has(row.questionId)"
                        :disabled="row.questionId != null && removingIds.has(row.questionId)"
                      >
                        移除
                      </n-button>
                    </n-space>
                  </n-space>
                </n-list-item>
              </draggable>
            </n-list>
          </div>
        </n-card>
      </div>
    </div>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { VueDraggable as draggable } from 'vue-draggable-plus'
import { useRequest } from 'vue-hooks-plus'

import { getKnowledgePoints } from '@/api/knowledge-point'
import { addQuestionsToPaper, getPaperQuestions, removePaperQuestion, updatePaperQuestions } from '@/api/paper'
import { getQuestionPage } from '@/api/question'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'

import type { KnowledgePointVO, PaperQuestionVO, QuestionVO } from '@/api/types'

const props = defineProps<{
  show: boolean
  paperId: number | null
}>()

const emit = defineEmits(['update:show', 'success'])

const message = useMessage()

// 搜索和筛选
const searchKeyword = ref('')
const typeFilter = ref<string | null>(null)
const difficultyFilter = ref<string | null>(null)
const knowledgePointFilter = ref<number | null>(null)

// 数据
const availableQuestions = ref<QuestionVO[]>([])
const paperQuestions = ref<PaperQuestionVO[]>([])
const knowledgePoints = ref<KnowledgePointVO[]>([])
const selectedQuestionIds = ref<number[]>([])
const selectedQuestionSet = ref<Set<number>>(new Set())

// 分页
const questionPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: false,
  pageSizes: [10, 20, 50]
})

const saving = ref(false)

const knowledgePointOptions = computed(() =>
  knowledgePoints.value.map(kp => ({
    label: kp.name,
    value: kp.id
  }))
)

// 计算总分
const totalScore = computed(() => paperQuestions.value.reduce((sum, q) => sum + q.score, 0))

// 题库表格列
const questionColumns = [
  { type: 'selection' },
  {
    title: '题目',
    key: 'title',
    ellipsis: { tooltip: true },
    width: 300
  },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render: (row: QuestionVO) => getQuestionTypeText(row.type)
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 60,
    render: (row: QuestionVO) => getDifficultyText(row.difficulty)
  },
  {
    title: '分值',
    key: 'score',
    width: 60
  }
]

// 获取题库题目
const { loading: loadingQuestions, run: fetchQuestions } = useRequest(
  () =>
    getQuestionPage({
      page: questionPagination.page,
      size: questionPagination.pageSize,
      keyword: searchKeyword.value || undefined,
      type: typeFilter.value as any,
      difficulty: difficultyFilter.value as any,
      knowledgePointId: knowledgePointFilter.value || undefined,
      enabled: true
    }),
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        availableQuestions.value = res.data.content
        questionPagination.itemCount = res.data.total
      } else {
        message.error(res.message || '获取题目失败')
      }
    }
  }
)

// 拉取试卷已有题目
const { run: fetchPaperQuestions } = useRequest((paperId: number) => getPaperQuestions(paperId, false), {
  manual: true,
  onSuccess: res => {
    if (res.code === 200) {
      paperQuestions.value = res.data.map((q, idx) => ({
        ...q,
        orderNum: idx + 1
      }))
    } else {
      message.error(res.message || '获取试卷题目失败')
    }
  }
})

// 获取知识点
const { run: fetchKnowledgePoints } = useRequest(getKnowledgePoints, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      knowledgePoints.value = res.data
    }
  }
})

// 工具函数
function questionRowKey(row: QuestionVO) {
  return row.id
}

// 新题目（questionId 为空）用 orderNum 作 key，保证唯一
function paperQuestionRowKey(row: PaperQuestionVO) {
  return row.questionId ?? row.orderNum
}

function getQuestionTypeText(type: string) {
  const typeMap: Record<string, string> = {
    SINGLE_CHOICE: '单选',
    MULTIPLE_CHOICE: '多选',
    TRUE_FALSE: '判断',
    FILL_BLANK: '填空',
    SHORT_ANSWER: '简答',
    ESSAY: '论述'
  }
  return typeMap[type] || type
}

function getDifficultyText(difficulty: string) {
  const difficultyMap: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

// 事件处理
function handleQuestionPageChange(page: number) {
  questionPagination.page = page
  fetchQuestions()
}

function updateSelectedQuestions(keys: number[]) {
  // 当前页所有记录的 key
  const currentPageIds = availableQuestions.value.map(q => q.id)
  // 本页新选中的，加入 Set；取消的，从 Set 删除
  currentPageIds.forEach(id => {
    if (keys.includes(id)) {
      selectedQuestionSet.value.add(id)
    } else {
      selectedQuestionSet.value.delete(id)
    }
  })
  selectedQuestionIds.value = Array.from(selectedQuestionSet.value)
}

async function addSelectedQuestions() {
  if (!selectedQuestionIds.value.length || props.paperId == null) return

  const toAdd = availableQuestions.value
    .filter(q => selectedQuestionIds.value.includes(q.id) && !paperQuestions.value.some(pq => pq.questionId === q.id))
    .map(q => ({
      questionId: q.id,
      orderNum: paperQuestions.value.length + 1,
      score: q.score || 0
    }))

  if (!toAdd.length) return

  try {
    const res = await addQuestionsToPaper(props.paperId, toAdd)
    if (res.code === 200) {
      message.success('添加题目成功')
      selectedQuestionIds.value = []
      // 重新拉取最新列表
      fetchPaperQuestions(props.paperId)
    } else {
      message.error(res.message || '添加题目失败')
    }
  } catch (error: any) {
    message.error(error.message || '添加题目失败')
  }
}

// 添加一组正在删除的 id
const removingIds = ref<Set<number>>(new Set())

// 改造 removeQuestion：标记 loading，等请求完再清除
async function removeQuestion(questionId: number | null, index: number) {
  if (props.paperId != null && questionId != null) {
    removingIds.value.add(questionId)
    try {
      const res = await removePaperQuestion(props.paperId, questionId)
      if (res.code === 200) {
        message.success('移除题目成功')
      } else {
        message.error(res.message || '移除题目失败')
        return
      }
    } catch (error: any) {
      message.error(error.message || '移除题目失败')
      return
    } finally {
      removingIds.value.delete(questionId)
    }
  }
  paperQuestions.value.splice(index, 1)
  updateOrderNumbers()
}

function updateOrderNumbers() {
  paperQuestions.value.forEach((q, i) => (q.orderNum = i + 1))
}

async function saveQuestions() {
  if (props.paperId == null) return
  saving.value = true
  try {
    const dtos = paperQuestions.value.map(q => ({
      questionId: q.questionId,
      orderNum: q.orderNum,
      score: q.score
    }))
    const res = await updatePaperQuestions(props.paperId, dtos)
    if (res.code === 200) {
      message.success('保存成功')
      emit('success')
      handleClose()
    } else {
      message.error(res.message || '保存失败')
    }
  } catch (error: any) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleClose() {
  emit('update:show', false)
  // 重置状态
  selectedQuestionIds.value = []
  searchKeyword.value = ''
  typeFilter.value = null
  difficultyFilter.value = null
  knowledgePointFilter.value = null
}

// 监听搜索条件变化
watch(
  [searchKeyword, typeFilter, difficultyFilter, knowledgePointFilter],
  () => {
    questionPagination.page = 1
    fetchQuestions()
  },
  { deep: true }
)

// 监听试卷ID变化
watch(
  () => props.paperId,
  newId => {
    if (newId && props.show) {
      fetchPaperQuestions(newId)
    }
  }
)

// 监听弹窗显示状态
watch(
  () => props.show,
  show => {
    if (show) {
      fetchKnowledgePoints()
      fetchQuestions()
      if (props.paperId) {
        fetchPaperQuestions(props.paperId)
      }
    }
  }
)

onMounted(() => {
  if (props.show) {
    fetchKnowledgePoints()
    fetchQuestions()
    if (props.paperId) {
      fetchPaperQuestions(props.paperId)
    }
  }
})
</script>

<style scoped>
.questions-manager {
  display: flex;
  gap: 16px;
  height: 100%;
}

.left-panel {
  flex: 1;
}

.right-panel {
  flex: 1;
  min-height: 0;
}

.right-panel :deep(.n-card__content) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  padding: 0;
}

.filters {
  margin-bottom: 12px;
}

.action-buttons {
  margin-top: 12px;
  text-align: center;
}

.question-list-wrapper {
  flex: 1 1 0;
  min-height: 0;
  padding: 8px 0;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #888 transparent;
}

.drag-handle {
  cursor: move;
  margin-right: 8px;
  color: #909399;
}
</style>
