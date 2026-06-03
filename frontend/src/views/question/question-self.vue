<template>
  <div class="question-self-page">
    <n-card :bordered="false" class="question-self-card">
      <n-h1 class="question-self-title">我的题目</n-h1>
      <div class="question-self-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索题目标题/内容" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="typeFilter" :options="typeOptions" clearable placeholder="题型" style="width: 120px" @update:value="handleSearch" />
        <n-select v-model:value="difficultyFilter" :options="difficultyOptions" clearable placeholder="难度" style="width: 100px" @update:value="handleSearch" />
        <n-select v-model:value="enabledFilter" :options="enabledOptions" clearable placeholder="状态" style="width: 100px" @update:value="handleSearch" />
        <n-select v-model:value="knowledgePointFilter" :options="knowledgePointOptions" clearable placeholder="知识点" style="width: 200px" @update:value="handleSearch" />
        <n-button v-permission="'question:create:self'" type="primary" @click="handleCreateQuestion">新增题目</n-button>
        <n-button v-permission="'question:create:self'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <n-data-table
        v-permission="'question:list:self'"
        remote
        :columns="columns"
        :data="questions"
        :pagination="pagination"
        :loading="loadingQuestions"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        :checked-row-keys="selectedRowKeys"
        @update:checked-row-keys="updateSelectedRowKeys"
        @update:page="handlePageChange"
      />
    </n-card>

    <!-- 题目详情模态框 -->
    <question-detail-modal
      :show="showDetailModal"
      :loading="updatingQuestion"
      :updating-status="updatingStatus"
      :knowledge-points="knowledgePoints"
      :question="selectedQuestion"
      :can-edit="true"
      :can-update-status="true"
      @update:show="updateDetailModalShow"
      @submit="handleUpdateQuestion"
      @toggle-status="handleToggleStatus"
    />

    <!-- 题目创建模态框 -->
    <question-create-modal
      :show="showCreateModal"
      :loading="creatingQuestion"
      :knowledge-points="knowledgePoints"
      :is-admin="false"
      @update:show="updateCreateModalShow"
      @submit="handleCreateQuestionSubmit"
    />

    <!-- 题目历史模态框 -->
    <question-history-modal
    v-model:show="showHistoryModal"
    :question-id="selectedQuestion?.id ?? null"
    @update:show="val => (showHistoryModal = val)"
  />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NSwitch, NTag, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getKnowledgePoints } from '@/api/knowledge-point'
import { batchDeleteQuestions, createQuestion, deleteQuestion, getMyQuestionPage, getQuestionDetail, updateQuestion, updateQuestionStatus } from '@/api/question'
import QuestionCreateModal from '@/components/question/question-create-modal.vue'
import QuestionDetailModal from '@/components/question/question-detail-modal.vue'
import QuestionHistoryModal from '@/components/question/question-history-modal.vue'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_ENABLED_OPTIONS as enabledOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CreateQuestionDTO, Difficulty, KnowledgePointVO, QuestionDetailVO, QuestionType, QuestionVO, UpdateQuestionDTO } from '@/api/types'

const message = useMessage()
const modal = useModal()

const questions = ref<QuestionVO[]>([])
const knowledgePoints = ref<KnowledgePointVO[]>([])
const searchKeyword = ref('')
const typeFilter = ref<string | null>(null)
const difficultyFilter = ref<string | null>(null)
const enabledFilter = ref<boolean | null>(null)
const knowledgePointFilter = ref<number | null>(null)
const selectedRowKeys = ref<DataTableRowKey[]>([])
const selectedQuestion = ref<QuestionDetailVO | null>(null)
const showDetailModal = ref(false)
const showCreateModal = ref(false)
const showHistoryModal = ref(false)

const rowKey = (row: QuestionVO) => row.id

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
    fetchQuestions()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchQuestions()
  }
})

const knowledgePointOptions = computed(() => [{ label: '全部知识点', value: null }, ...knowledgePoints.value.map(kp => ({ label: kp.name, value: kp.id }))])

// 获取题目数据
const { loading: loadingQuestions, run: fetchQuestions } = useRequest(
  () =>
    getMyQuestionPage({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value || undefined,
      type: (typeFilter.value as QuestionType) || undefined,
      difficulty: (difficultyFilter.value as Difficulty) || undefined,
      enabled: enabledFilter.value ?? undefined,
      knowledgePointId: knowledgePointFilter.value || undefined
    }),
  {
    onSuccess: res => {
      if (res.code === 200) {
        questions.value = res.data?.content || []
        pagination.itemCount = res.data?.total || 0
      } else {
        message.error(res.message)
      }
    }
  }
)

// 获取知识点数据
const { run: fetchKnowledgePoints } = useRequest(() => getKnowledgePoints(), {
  onSuccess: res => {
    if (res.code === 200) {
      knowledgePoints.value = res.data || []
    }
  }
})

// 更新题目
const { loading: updatingQuestion, run: runUpdateQuestion } = useRequest(({ id, data }: { id: number; data: UpdateQuestionDTO }) => updateQuestion(id, data), {
  manual: true,
  onSuccess: res => {
    if (res.code === 200) {
      message.success('更新题目成功')
      showDetailModal.value = false
      fetchQuestions()
    } else {
      message.error(res.message || '更新题目失败')
    }
  }
})

// 创建题目
const { loading: creatingQuestion, run: runCreateQuestion } = useRequest((data: CreateQuestionDTO) => createQuestion(data), {
  manual: true,
  onSuccess: res => {
    if (res.code === 200) {
      message.success('创建题目成功')
      showCreateModal.value = false
      fetchQuestions()
    } else {
      message.error(res.message || '创建题目失败')
    }
  }
})

// 更新状态
const { loading: updatingStatus, run: runUpdateStatus } = useRequest(({ id, enabled }: { id: number; enabled: boolean }) => updateQuestionStatus(id, enabled), {
  manual: true,
  onSuccess: res => {
    if (res.code === 200) {
      message.success('更新状态成功')
      if (selectedQuestion.value) {
        selectedQuestion.value.enabled = res.data.enabled
      }
      fetchQuestions()
    } else {
      message.error(res.message || '更新状态失败')
    }
  }
})

// 表格列定义
const columns: DataTableColumns<QuestionVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  {
    title: '题型',
    key: 'typeDescription',
    width: 80,
    render: row =>
      h(
        NTag,
        {
          type: 'info',
          size: 'small',
          style: { cursor: 'pointer' },
          onClick: () => handleTypeTagClick(row.type)
        },
        { default: () => row.typeDescription }
      )
  },
  {
    title: '难度',
    key: 'difficultyDescription',
    width: 60,
    render: row => {
      const tagType = row.difficulty === 'EASY' ? 'success' : row.difficulty === 'MEDIUM' ? 'warning' : 'error'
      return h(
        NTag,
        {
          type: tagType,
          size: 'small',
          style: { cursor: 'pointer' },
          onClick: () => handleDifficultyTagClick(row.difficulty)
        },
        { default: () => row.difficultyDescription }
      )
    }
  },
  { title: '分值', key: 'score', width: 60 },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: row =>
      h(NSwitch, {
        size: 'small',
        value: row.enabled,
        'onUpdate:value': (v: boolean) => handleToggleStatus({ id: row.id, enabled: v })
      })
  },
  { title: '知识点', key: 'knowledgePoint', width: 150, render: (row: QuestionVO) => row.knowledgePoint?.name || '-' },
  { title: '创建时间', key: 'createTime', width: 120, render: (row: QuestionVO) => row.createTime?.slice(0, 10) || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row: QuestionVO) => [
      h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          ghost: true,
          style: 'margin-right: 8px',
          onClick: () => handleView(row)
        },
        { default: () => '查看' }
      ),
      h(
        NButton,
        {
          size: 'small',
          type: 'error',
          ghost: true,
          onClick: () => handleDelete(row)
        },
        { default: () => '删除' }
      )
    ]
  }
]

/**
 * 搜索/筛选题目
 */
function handleSearch() {
  pagination.page = 1
  fetchQuestions()
}

/**
 * 分页切换
 * @param page 当前页码
 */
function handlePageChange(page: number) {
  pagination.page = page
}

/**
 * 更新表格选中的行
 * @param keys 选中的行key数组
 */
function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

/**
 * 控制详情模态框显示
 * @param show 是否显示
 */
function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

/**
 * 控制创建题目模态框显示
 * @param show 是否显示
 */
function updateCreateModalShow(show: boolean) {
  showCreateModal.value = show
}

/**
 * 查看题目详情
 * @param row 题目行数据
 */
async function handleView(row: QuestionVO) {
  try {
    const res = await getQuestionDetail(row.id)
    if (res.code === 200) {
      selectedQuestion.value = res.data
      showDetailModal.value = true
    } else {
      message.error(res.message || '获取题目详情失败')
    }
  } catch (error) {
    message.error('获取题目详情失败')
  }
}

/**
 * 删除单个题目
 * @param row 题目行数据
 */
function handleDelete(row: QuestionVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除题目 "${row.title}" 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await deleteQuestion(row.id)
        if (res.code === 200) {
          message.success('删除题目成功')
          fetchQuestions()
        } else {
          message.error(res.message || '删除题目失败')
        }
      } catch (error) {
        message.error('删除题目失败')
      }
    }
  })
}

/**
 * 批量删除题目
 */
function handleBatchDelete() {
  if (!selectedRowKeys.value.length) return
  modal.create({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个题目吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteQuestions(selectedRowKeys.value as number[])
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchQuestions()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (error) {
        message.error('批量删除失败')
      }
    }
  })
}

/**
 * 打开创建题目模态框
 */
function handleCreateQuestion() {
  showCreateModal.value = true
}

/**
 * 更新题目信息
 * @param param0 包含题目id和更新数据
 */
function handleUpdateQuestion({ id, data }: { id: number; data: UpdateQuestionDTO }) {
  runUpdateQuestion({ id, data })
}

/**
 * 创建题目提交
 * @param data 创建题目数据
 */
function handleCreateQuestionSubmit(data: CreateQuestionDTO) {
  runCreateQuestion(data)
}

/**
 * 切换题目启用状态
 * @param param0 包含题目id和目标状态
 */
function handleToggleStatus({ id, enabled }: { id: number; enabled: boolean }) {
  runUpdateStatus({ id, enabled })
}

/**
 * 点击题型标签进行筛选
 * @param type 题型
 */
function handleTypeTagClick(type: string) {
  if (typeFilter.value === type) {
    typeFilter.value = null
    message.info('已清除题型筛选')
  } else {
    typeFilter.value = type
    const label = typeOptions.find(o => o.value === type)?.label
    message.info(`已筛选题型：${label}`)
  }
  handleSearch()
}
/**
 * 点击难度标签进行筛选
 * @param d 难度
 */
function handleDifficultyTagClick(d: string) {
  if (difficultyFilter.value === d) {
    difficultyFilter.value = null
    message.info('已清除难度筛选')
  } else {
    difficultyFilter.value = d
    const label = difficultyOptions.find(o => o.value === d)?.label
    message.info(`已筛选难度：${label}`)
  }
  handleSearch()
}

onMounted(() => {
  fetchQuestions()
  fetchKnowledgePoints()
})
</script>

<style scoped>
.question-self-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.question-self-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.question-self-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}

.question-self-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.questions-self-card > .n-data-table {
  margin-top: 24px;
}
</style>

