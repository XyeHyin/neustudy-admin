<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">题目管理</n-h1>
      <div class="admin-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索题目标题/内容" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="enabledFilter" :options="enabledOptions" clearable placeholder="状态" style="width: 100px" @update:value="handleSearch" />
        <n-select v-model:value="knowledgePointFilter" :options="knowledgePointOptions" clearable placeholder="知识点" style="width: 200px" @update:value="handleSearch" />
        <n-button v-permission="'question:create:all'" type="primary" @click="handleCreateQuestion">新增题目</n-button>
        <n-button v-permission="'question:delete:all'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <n-data-table
        v-permission="'question:list:all'"
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
      :can-edit="auth.hasPermission('question:edit:all')"
      :can-update-status="auth.hasPermission('question:status:all')"
      @update:show="updateDetailModalShow"
      @submit="handleUpdateQuestion"
      @toggle-status="handleToggleStatus"
    />

    <!-- 题目创建模态框 -->
    <question-create-modal
      :show="showCreateModal"
      :loading="creatingQuestion"
      :knowledge-points="knowledgePoints"
      :is-admin="auth.hasPermission('question:create:all')"
      @update:show="updateCreateModalShow"
      @submit="handleCreateQuestionSubmit"
    />

    <!-- 题目历史模态框 -->
    <question-history-modal
    :show="showHistoryModal"
    :question-id="selectedQuestion?.id ?? null"
    @update:show="updateHistoryModalShow"
  />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NSwitch, NTag, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getKnowledgePoints } from '@/api/knowledge-point'
import {
  adminCreateQuestion,
  adminDeleteQuestion,
  adminUpdateQuestion,
  batchDeleteQuestions,
  createQuestion,
  deleteQuestion,
  getQuestionDetail,
  getQuestionPage,
  updateQuestionStatus
} from '@/api/question'
import QuestionCreateModal from '@/components/question/question-create-modal.vue'
import QuestionDetailModal from '@/components/question/question-detail-modal.vue'
import QuestionHistoryModal from '@/components/question/question-history-modal.vue'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_ENABLED_OPTIONS as enabledOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'
import { useAuthStore } from '@/store/auth'
import { formatDate } from '@/utils/datetime'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CreateQuestionDTO, Difficulty, KnowledgePointVO, QuestionDetailVO, QuestionType, QuestionVO, UpdateQuestionDTO } from '@/api/types'

const message = useMessage()
const modal = useModal()
const auth = useAuthStore()

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
    getQuestionPage({
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
const { loading: updatingQuestion, run: runUpdateQuestion } = useRequest(({ id, data }: { id: number; data: UpdateQuestionDTO }) => adminUpdateQuestion(id, data), {
  manual: true,
  async onSuccess(res) {
    if (res.code === 200) {
      message.success('更新题目成功')
      const detailRes = await getQuestionDetail(selectedQuestion.value!.id)
      if (detailRes.code === 200) {
        selectedQuestion.value = detailRes.data
      }
      fetchQuestions()
      showDetailModal.value = false
    } else {
      message.error(res.message || '更新题目失败')
    }
  }
})

// 创建题目
const { loading: creatingQuestion, run: runCreateQuestion } = useRequest(
  (data: CreateQuestionDTO) => (auth.hasPermission('question:create:all') ? adminCreateQuestion(data) : createQuestion(data)),
  {
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
  }
)

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
    title: '题目标题',
    key: 'title',
    width: 200,
    ellipsis: {
      tooltip: true
    },
  },
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
  { title: '知识点', key: 'knowledgePoint', width: 150, render: row => row.knowledgePoint?.name || '-' },
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
  { title: '创建时间', key: 'createTime', width: 120, render: row => formatDate(row.createTime) },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row: QuestionVO) => [
      // 查看按钮
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
      // 删除按钮（有权限才显示）
      h(
        NButton,
        {
          size: 'small',
          type: 'error',
          ghost: true,
          directives: {
            name: 'permission',
            value: 'question:delete:all'
          },
          onClick: () => handleDelete(row)
        },
        { default: () => '删除' }
      )
    ]
  }
]

function handleSearch() {
  pagination.page = 1
  fetchQuestions()
}

function handlePageChange(page: number) {
  pagination.page = page
}

function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

function updateCreateModalShow(show: boolean) {
  showCreateModal.value = show
}

function updateHistoryModalShow(show: boolean) {
  showHistoryModal.value = show
}

// 查看题目
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

// 删除题目
function handleDelete(row: QuestionVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除题目 "${row.title}" 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const deleteFunc = auth.hasPermission('question:delete:all') ? adminDeleteQuestion : deleteQuestion
        const res = await deleteFunc(row.id)
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

// 批量删除
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

// 创建题目
function handleCreateQuestion() {
  showCreateModal.value = true
}

// 更新题目
function handleUpdateQuestion({ id, data }: { id: number; data: UpdateQuestionDTO }) {
  runUpdateQuestion({ id, data })
}

// 创建题目提交
function handleCreateQuestionSubmit(data: CreateQuestionDTO) {
  runCreateQuestion(data)
}

// 切换状态
function handleToggleStatus({ id, enabled }: { id: number; enabled: boolean }) {
  runUpdateStatus({ id, enabled })
}

// 筛选回调
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
