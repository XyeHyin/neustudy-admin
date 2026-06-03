<template>
  <div class="question-generate-page">
    <n-card :bordered="false" class="question-generate-card">
      <n-h1 class="question-generate-title">智能题目生成</n-h1>
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100" class="question-generate-form" @submit.prevent="handleGenerate">
        <div class="form-row">
          <div class="form-col">
            <n-form-item label="知识点" path="knowledgePointId">
              <n-select v-model:value="form.knowledgePointId" :options="knowledgePointOptions" placeholder="请选择知识点" filterable clearable :loading="loadingKnowledgePoints" />
            </n-form-item>
            <n-form-item label="题型" path="type">
              <n-select v-model:value="form.type" :options="typeOptions" placeholder="请选择题型" clearable />
            </n-form-item>
          </div>
          <div class="form-col">
            <n-form-item label="难度" path="difficulty">
              <n-select v-model:value="form.difficulty" :options="difficultyOptions" placeholder="请选择难度" clearable />
            </n-form-item>
            <n-form-item label="数量" path="count">
              <n-input-number v-model:value="form.count" :min="1" :max="20" placeholder="生成题目数量" />
            </n-form-item>
          </div>
        </div>
        <n-form-item label="额外要求" path="extraRequirement">
          <n-input v-model:value="form.extraRequirement" type="textarea" :rows="3" placeholder="可选：描述您对题目的具体要求，如侧重点、应用场景等" maxlength="500" show-count />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" :loading="generating" :disabled="generating" attr-type="submit">
              <template #icon>
                <icon type="materialAutoAwesome" />
              </template>
              AI智能生成
            </n-button>
            <n-button @click="resetForm" :disabled="generating">重置</n-button>
          </n-space>
        </n-form-item>
      </n-form>

      <!-- 生成结果展示 -->
      <div v-if="generatedQuestions.length" class="generated-results">
        <n-divider>
          <n-text type="success">
            <icon type="materialCheckCircle" style="margin-right: 8px" />
            已生成 {{ generatedQuestions.length }} 道题目
          </n-text>
        </n-divider>

        <n-space class="question-generate-toolbar">
          <n-input v-model:value="filterKeyword" placeholder="搜索题目标题/内容" style="width: 240px" clearable />
          <n-select v-model:value="filterType" :options="typeOptions" clearable placeholder="题型" style="width: 120px" />
          <n-select v-model:value="filterDifficulty" :options="difficultyOptions" clearable placeholder="难度" style="width: 100px" />
          <n-button type="success" :disabled="!selectedRowKeys.length" :loading="saving" @click="handleSaveSelected">
            <template #icon>
              <icon type="materialSave" />
            </template>
            保存选中 ({{ selectedRowKeys.length }})
          </n-button>
          <n-button type="info" @click="handleExportQuestions">
            <template #icon>
              <icon type="materialDownload" />
            </template>
            导出全部
          </n-button>
          <n-button @click="handleSelectAll" v-if="!allSelected">全选</n-button>
          <n-button @click="handleDeselectAll" v-else>取消全选</n-button>
          <n-button type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">
            <template #icon><icon type="materialDeleteForever" /></template>
            删除选中 ({{ selectedRowKeys.length }})
          </n-button>
        </n-space>

        <n-data-table
          :columns="columns"
          :data="filteredQuestions"
          :pagination="pagination"
          :bordered="false"
          :row-key="rowKey"
          :checked-row-keys="selectedRowKeys"
          @update:checked-row-keys="updateSelectedRowKeys"
          style="margin-top: 16px"
        />
      </div>
    </n-card>

    <!-- 题目预览模态框 -->
    <n-modal v-model:show="showPreviewModal" preset="dialog" title="题目预览" style="width: 700px">
      <div v-if="previewQuestion" class="question-preview">
        <n-divider>题目内容</n-divider>
        <div class="question-content">
          <n-text strong>{{ previewQuestion.title }}</n-text>
          <div v-if="previewQuestion.content" style="margin-top: 8px">
            <n-text depth="2">{{ previewQuestion.content }}</n-text>
          </div>
        </div>

        <!-- 选择题选项 -->
        <div v-if="showOptions(previewQuestion.type)" style="margin-top: 16px">
          <n-divider>选项</n-divider>
          <div v-for="option in getOptions(previewQuestion.options)" :key="option.key" class="option-item">
            <n-text>{{ option.key }}. {{ option.value }}</n-text>
          </div>
        </div>

        <n-divider>答案解析</n-divider>
        <div class="answer-section">
          <n-text strong>正确答案：</n-text>
          <n-tag type="success" style="margin-left: 8px">{{ previewQuestion.answer }}</n-tag>
        </div>
        <div v-if="previewQuestion.explanation" style="margin-top: 12px; margin-left: 8px">
          <n-text strong>解析：</n-text>
          <div style="margin-top: 4px">
            <n-text>{{ previewQuestion.explanation }}</n-text>
          </div>
        </div>
        <n-divider></n-divider>
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="题目类型">
            <n-tag :type="getTypeTagType(previewQuestion.type)">{{ getTypeLabel(previewQuestion.type) }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="难度等级">
            <n-tag :type="getDifficultyTagType(previewQuestion.difficulty)">{{ getDifficultyLabel(previewQuestion.difficulty) }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="分值">{{ previewQuestion.score || 1 }} 分</n-descriptions-item>
          <n-descriptions-item label="AI生成">
            <n-tag type="info">{{ previewQuestion.isAiGenerated ? 'AI生成' : '手动创建' }}</n-tag>
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>

    <!-- 编辑模态框 -->
    <n-modal v-model:show="showEditModal" title="编辑题目" preset="dialog" style="width: 600px">
      <n-form ref="editFormRef" :model="editForm" label-width="80">
        <n-form-item label="题目标题" path="title">
          <n-input v-model:value="editForm.title" />
        </n-form-item>
        <n-form-item label="题目内容" path="content">
          <n-input v-model:value="editForm.content" type="textarea" :rows="2" />
        </n-form-item>
        <n-form-item label="答案" path="answer">
          <n-input v-model:value="editForm.answer" />
        </n-form-item>
        <n-form-item label="解析" path="explanation">
          <n-input v-model:value="editForm.explanation" type="textarea" :rows="2" />
        </n-form-item>
        <n-form-item label="分值" path="score">
          <n-input-number v-model:value="editForm.score" :min="1" />
        </n-form-item>
        <n-form-item>
          <n-space justify="end">
            <n-button @click="showEditModal = false">取消</n-button>
            <n-button type="primary" @click="handleSaveLocalEdit">保存</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script lang="ts" setup>
import { NButton, NForm, NFormItem, NInput, NInputNumber, NModal, NSpace, NTag, useDialog, useMessage } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { generateQuestions } from '@/api/ai'
import { getKnowledgePoints } from '@/api/knowledge-point'
import { batchCreateQuestions } from '@/api/question'
import { Icon } from '@/components'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'

import type { DataTableColumns, DataTableRowKey, FormRules } from 'naive-ui'
import type { AIGeneratedQuestionVO, AIGenerateQuestionDTO, CreateQuestionDTO, Difficulty, KnowledgePointVO, QuestionType } from '@/api/types'

const message = useMessage()
const dialog = useDialog()

const showEditModal = ref(false)
const editFormRef = ref()
const editForm = reactive<Partial<AIGeneratedQuestionVO>>({})

const formRef = ref()
const form = ref<AIGenerateQuestionDTO>({
  knowledgePointId: null,
  type: null,
  difficulty: null,
  count: 5,
  extraRequirement: ''
})

const rules: FormRules = {
  knowledgePointId: [
    {
      required: true,
      // 自定义校验：必须选择非 0 值
      validator(rule, value) {
        if (!value || value === 0) {
          return new Error('请选择知识点')
        }
        return true
      },
      trigger: 'change'
    }
  ],
  type: [
    {
      required: true,
      message: '请选择题型',
      trigger: 'change'
    }
  ],
  count: [
    {
      required: true,
      type: 'number' as const, // 明确 literal 类型
      min: 1,
      max: 20,
      message: '生成数量必须在1-20之间',
      trigger: 'blur'
    }
  ],
  difficulty: [
    {
      required: true,
      message: '请选择题目难度',
      trigger: 'change'
    }
  ]
}

// 新增：记录当前编辑的行索引
const editIndex = ref<number>(-1)

/** 打开编辑弹窗，复制待编辑题目到 editForm */
function handleEditQuestion(row: AIGeneratedQuestionVO, index: number) {
  editIndex.value = index
  Object.assign(editForm, row)
  showEditModal.value = true
}

/** 将 editForm 的修改同步回 generatedQuestions */
function handleSaveLocalEdit() {
  if (editIndex.value < 0) return
  Object.assign(generatedQuestions.value[editIndex.value], editForm)
  showEditModal.value = false
}

/** 删除单条题目 */
function handleDeleteQuestion(row: AIGeneratedQuestionVO) {
  generatedQuestions.value = generatedQuestions.value.filter(q => q !== row)
  selectedRowKeys.value = []
}

/** 批量删除选中 */
function handleBatchDelete() {
  generatedQuestions.value = generatedQuestions.value.filter((_, i) => !(selectedRowKeys.value as number[]).includes(i))
  selectedRowKeys.value = []
}

// 知识点数据
const knowledgePoints = ref<KnowledgePointVO[]>([])
const { loading: loadingKnowledgePoints } = useRequest(getKnowledgePoints, {
  onSuccess(res) {
    if (res.code === 200) {
      knowledgePoints.value = res.data || []
    } else {
      message.error(res.message || '获取知识点失败')
    }
  },
  onError(error) {
    message.error('获取知识点失败: ' + error.message)
  }
})

const knowledgePointOptions = computed(() => [
  ...knowledgePoints.value
    .filter(kp => kp.enabled)
    .map(kp => ({
      label: `${kp.name} (${kp.courseName})`,
      value: kp.id
    }))
])

// 生成题目
const generatedQuestions = ref<AIGeneratedQuestionVO[]>([])
const { runAsync: generateQuestionsAPI, loading: generating } = useRequest(generateQuestions, { manual: true })

async function handleGenerate() {
  formRef.value?.validate(async (errors: any) => {
    if (!errors) {
      try {
        const res = await generateQuestionsAPI(form.value)
        if (res.code === 200) {
          generatedQuestions.value = [...generatedQuestions.value, ...(res.data || [])]
          selectedRowKeys.value = []
          message.success(`成功生成 ${res.data?.length || 0} 道题目`)
        } else {
          message.error(res.message || 'AI生成失败')
        }
      } catch (error: any) {
        message.error('生成失败: ' + (error.message || '网络错误'))
      }
    }
  })
}

// 题目管理
const selectedRowKeys = ref<DataTableRowKey[]>([])
const filterKeyword = ref('')
const filterType = ref<QuestionType | null>(null)
const filterDifficulty = ref<Difficulty | null>(null)

const rowKey = (row: AIGeneratedQuestionVO) => filteredQuestions.value.findIndex(q => q === row)

const filteredQuestions = computed(() => {
  return generatedQuestions.value.filter(q => {
    const matchKeyword =
      !filterKeyword.value || q.title?.toLowerCase().includes(filterKeyword.value.toLowerCase()) || q.content?.toLowerCase().includes(filterKeyword.value.toLowerCase())
    const matchType = !filterType.value || q.type === filterType.value
    const matchDifficulty = !filterDifficulty.value || q.difficulty === filterDifficulty.value
    return matchKeyword && matchType && matchDifficulty
  })
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: computed(() => filteredQuestions.value.length),
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (size: number) => {
    pagination.pageSize = size
    pagination.page = 1
  }
})

const allSelected = computed(() => {
  return filteredQuestions.value.length > 0 && selectedRowKeys.value.length === filteredQuestions.value.length
})

function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

function handleSelectAll() {
  selectedRowKeys.value = filteredQuestions.value.map((_, i) => i)
}

function handleDeselectAll() {
  selectedRowKeys.value = []
}

// 题目预览
const showPreviewModal = ref(false)
const previewQuestion = ref<AIGeneratedQuestionVO | null>(null)

function handlePreviewQuestion(question: AIGeneratedQuestionVO) {
  previewQuestion.value = question
  showPreviewModal.value = true
}

// 保存题目
const { runAsync: saveQuestionsAPI, loading: saving } = useRequest(batchCreateQuestions, { manual: true })

async function handleSaveSelected() {
  if (!selectedRowKeys.value.length) {
    message.warning('请选择要保存的题目')
    return
  }

  // 改为基于 filteredQuestions 过滤
  const selectedQuestions = filteredQuestions.value.filter((_, idx) => (selectedRowKeys.value as number[]).includes(idx))

  dialog.info({
    title: '确认保存',
    content: `确定要保存选中的 ${selectedQuestions.length} 道题目到题库吗？`,
    positiveText: '确定保存',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const createDTOs: CreateQuestionDTO[] = selectedQuestions.map(q => ({
          title: q.title,
          content: q.content,
          type: q.type,
          difficulty: q.difficulty,
          options: q.options,
          answer: q.answer,
          explanation: q.explanation,
          score: q.score || 1,
          knowledgePointId: q.knowledgePointId,
          enabled: q.enabled !== false,
          tags: q.tags
        }))

        const res = await saveQuestionsAPI({
          questions: createDTOs,
          isAiGenerated: true
        })
        if (res.code === 200) {
          message.success(`成功保存 ${selectedQuestions.length} 道题目`)
          // 从 generatedQuestions 中移除已保存的题目
          generatedQuestions.value = generatedQuestions.value.filter(q => !selectedQuestions.includes(q))
          selectedRowKeys.value = []
        } else {
          message.error(res.message || '保存失败')
        }
      } catch (error: any) {
        message.error('保存失败: ' + (error.message || '网络错误'))
      }
    }
  })
}

function handleExportQuestions() {
  if (!generatedQuestions.value.length) {
    message.warning('没有题目可导出')
    return
  }

  try {
    const exportData = generatedQuestions.value.map((question, index) => ({
      序号: index + 1,
      题目标题: question.title || '',
      题目内容: question.content || '',
      题目类型: getTypeLabel(question.type),
      难度: getDifficultyLabel(question.difficulty),
      正确答案: question.answer || '',
      解析: question.explanation || '',
      分值: question.score || 1,
      标签: question.tags || '',
      AI生成: question.isAiGenerated ? '是' : '否'
    }))

    const headers = Object.keys(exportData[0])
    const csvContent = [headers.join(','), ...exportData.map(row => headers.map(header => `"${String(row[header as keyof typeof row]).replace(/"/g, '""')}"`).join(','))].join('\n')

    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `AI生成题目_${new Date().toISOString().slice(0, 10)}.csv`
    link.click()
    window.URL.revokeObjectURL(url)

    message.success('题目导出成功')
  } catch (error: any) {
    message.error('导出失败：' + error.message)
  }
}

function resetForm() {
  form.value = {
    knowledgePointId: null,
    type: null,
    difficulty: null,
    count: 5,
    extraRequirement: ''
  }
  generatedQuestions.value = []
  selectedRowKeys.value = []
  filterKeyword.value = ''
  filterType.value = null
  filterDifficulty.value = null
}

// 辅助函数
function getTypeLabel(type: QuestionType): string {
  const option = typeOptions.find(opt => opt.value === type)
  return option?.label || type
}

function getDifficultyLabel(difficulty: Difficulty): string {
  const option = difficultyOptions.find(opt => opt.value === difficulty)
  return option?.label || difficulty
}

function getTypeTagType(type: QuestionType): 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error' | undefined {
  const typeMap: Record<QuestionType, 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'> = {
    SINGLE_CHOICE: 'info',
    MULTIPLE_CHOICE: 'warning',
    FILL_BLANK: 'success',
    SHORT_ANSWER: 'default',
    TRUE_FALSE: 'error',
    ESSAY: 'default'
  }
  return typeMap[type] || 'default'
}

function getDifficultyTagType(difficulty: Difficulty): 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error' | undefined {
  const difficultyMap: Record<Difficulty, 'success' | 'warning' | 'error'> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'error'
  }
  return difficultyMap[difficulty] || 'default'
}

function showOptions(type: QuestionType): boolean {
  return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(type)
}

function getOptions(optionsStr?: string) {
  if (!optionsStr) return []
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}

// 表格列定义
const columns: DataTableColumns<AIGeneratedQuestionVO> = [
  { type: 'selection' },
  {
    title: '题目标题',
    key: 'title',
    minWidth: 300,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: (row: AIGeneratedQuestionVO) =>
      h(
        NTag,
        {
          type: getTypeTagType(row.type)
        },
        { default: () => getTypeLabel(row.type) }
      )
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 80,
    render: (row: AIGeneratedQuestionVO) =>
      h(
        NTag,
        {
          type: getDifficultyTagType(row.difficulty)
        },
        { default: () => getDifficultyLabel(row.difficulty) }
      )
  },
  {
    title: '分值',
    key: 'score',
    width: 70,
    render: (row: AIGeneratedQuestionVO) => row.score || 1
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row: AIGeneratedQuestionVO, rowIndex: number) =>
      // 操作按钮：预览、编辑、删除
      h(
        NSpace,
        { size: 'small' },
        {
          default: () => [
            h(NButton, { size: 'small', onClick: () => handlePreviewQuestion(row) }, '预览'),
            h(NButton, { size: 'small', onClick: () => handleEditQuestion(row, rowIndex) }, '编辑'),
            h(NButton, { size: 'small', type: 'error', onClick: () => handleDeleteQuestion(row) }, '删除')
          ]
        }
      )
  }
]

onMounted(() => {
  // 页面加载时获取知识点数据
})
</script>

<style scoped>
.question-generate-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.question-generate-card {
  width: 100%;
  box-shadow: var(--n-shadow-2);
  border-radius: 12px;
  padding: 24px 32px;
}

.question-generate-title {
  margin-bottom: 24px;
  font-size: 2.2rem;
  letter-spacing: 2px;
  color: var(--n-color-primary);
}

.question-generate-form {
  max-width: 800px;
  margin-bottom: 32px;
}

.form-row {
  display: flex;
  gap: 32px;
}

.form-col {
  flex: 1;
}

.generated-results {
  margin-top: 32px;
}

.question-generate-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding: 16px;
  background: var(--n-color-fill-2);
  border-radius: 8px;
}

.question-preview {
  max-height: 60vh;
  overflow-y: auto;
}

.question-content {
  padding: 16px;
  background: var(--n-popover-color);
  border-left: 4px solid var(--n-color-primary);
  border-radius: 8px;
}
.answer-section n-tag {
  display: inline-block; /* 让 n-tag 能按块级元素换行 */
  white-space: pre-wrap; /* 保留原始换行符并自动换行 */
  word-break: break-word; /* 单词超出时自动断行 */
  max-width: 100%; /* 根据容器宽度折行 */
}
.option-item {
  padding: 8px 12px;
  margin: 4px 0;
  background: var(--n-divider-color);
  border-radius: 4px;
}

.answer-section {
  padding: 12px;
  background: var(--n-popover-color);
  border-radius: 8px;
  border-left: 4px solid var(--n-color-info);
}
</style>
