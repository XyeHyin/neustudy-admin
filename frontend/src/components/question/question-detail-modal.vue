<template>
  <n-modal :show="show" preset="dialog" title="题目详情" @close="handleCancel" style="width: 900px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="题目标题" path="title">
            <n-input v-model:value="form.title" :disabled="!editMode" placeholder="请输入题目标题" />
          </n-form-item>
          <n-form-item label="题型" path="type">
            <n-select v-model:value="form.type" :disabled="!editMode" :options="typeOptions" placeholder="请选择题型" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="难度" path="difficulty">
            <n-select v-model:value="form.difficulty" :disabled="!editMode" :options="difficultyOptions" placeholder="请选择难度" />
          </n-form-item>
          <n-form-item label="分值" path="score">
            <n-input-number v-model:value="form.score" :disabled="!editMode" :min="1" :max="100" />
          </n-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="知识点" path="knowledgePoint.id">
            <n-select v-model:value="knowledgePointId" :disabled="!editMode" :options="knowledgePointOptions" placeholder="请选择知识点" clearable />
          </n-form-item>
          <n-form-item label="状态" path="enabled">
            <n-tag :type="getStatusType(form.enabled)">{{ getStatusText(form.enabled) }}</n-tag>
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="标签" path="tags">
            <n-input v-model:value="form.tags" :disabled="!editMode" placeholder="请输入标签，用逗号分隔" />
          </n-form-item>
          <n-form-item label="创建时间" path="createTime">
            <n-input v-model:value="formattedCreateTime" disabled />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="题目内容" path="content">
        <n-input v-model:value="form.content" :disabled="!editMode" type="textarea" :rows="3" placeholder="请输入题目内容" />
      </n-form-item>
      <!-- 选项编辑（仅单选/多选） -->
      <n-form-item v-if="showOptionEditor" label="选项" path="options">
        <question-options-editor v-model:value="form.options" :disabled="!editMode" :question-type="form.type" />
      </n-form-item>
      <!-- 正确答案（单选/多选/判断题） -->
      <n-form-item v-if="showAnswerOptions" label="正确答案" path="answer">
        <template v-if="editMode">
          <n-radio-group v-if="form.type !== 'MULTIPLE_CHOICE'" v-model:value="answerSingle">
            <n-radio v-for="opt in optionsList" :key="opt.key" :value="opt.key"> {{ opt.key }}. {{ opt.value }} </n-radio>
          </n-radio-group>
          <n-checkbox-group v-else v-model:value="answerMultiple">
            <n-checkbox v-for="opt in optionsList" :key="opt.key" :value="opt.key"> {{ opt.key }}. {{ opt.value }} </n-checkbox>
          </n-checkbox-group>
        </template>
        <template v-else>
          <n-tag v-if="form.type !== 'MULTIPLE_CHOICE'">{{ form.answer }}</n-tag>
          <n-tag v-else>{{ form.answer }}</n-tag>
        </template>
      </n-form-item>
      <!-- 其他题型显示纯文本答案 -->
      <n-form-item v-if="!showAnswerOptions" label="正确答案" path="answer">
        <n-input v-model:value="form.answer" :disabled="!editMode" type="textarea" :rows="2" placeholder="请输入正确答案" />
      </n-form-item>
      <n-form-item label="答案解析" path="explanation">
        <n-input v-model:value="form.explanation" :disabled="!editMode" type="textarea" :rows="3" placeholder="请输入答案解析" />
      </n-form-item>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button v-if="!editMode && canEdit" type="primary" @click="editMode = true">编辑</n-button>
          <template v-else-if="editMode">
            <n-button @click="editMode = false">取消</n-button>
            <n-button type="primary" :loading="loading" @click="handleSubmit">保存</n-button>
          </template>
          <n-button v-if="canUpdateStatus" :type="form.enabled ? 'warning' : 'success'" :loading="updatingStatus" @click="handleToggleStatus">
            {{ form.enabled ? '禁用' : '启用' }}
          </n-button>
          <n-button v-if="!editMode" type="info" @click="showHistoryModal = true">
            查看历史
          </n-button>
        </n-space>
      </n-form-item>
    </n-form>

    <!-- 历史弹窗 -->
    <question-history-modal
      :show="showHistoryModal"
      :question-id="form.id"
      @update:show="val => (showHistoryModal = val)"
    />
  </n-modal>
</template>

<script lang="ts" setup>
import { getEnabledStatusText as getStatusText, getEnabledStatusType as getStatusType } from '@/utils/status'
import { NCheckbox, NCheckboxGroup, NRadio, NRadioGroup, useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import QuestionHistoryModal from './question-history-modal.vue'
import QuestionOptionsEditor from './question-options-editor.vue'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'

import type { Difficulty, KnowledgePointVO, QuestionDetailVO, QuestionType, UpdateQuestionDTO } from '@/api/types'

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  updatingStatus?: boolean
  knowledgePoints: KnowledgePointVO[]
  question: QuestionDetailVO | null
  canEdit?: boolean
  canUpdateStatus?: boolean
}>()
const emit = defineEmits(['update:show', 'submit', 'toggle-status'])

const formRef = ref()
const editMode = ref(false)
const showHistoryModal = ref(false)

// 题目表单数据
const form = ref<QuestionDetailVO>({
  id: 0,
  title: '',
  content: '',
  type: 'SINGLE_CHOICE',
  typeDescription: '',
  difficulty: 'EASY',
  difficultyDescription: '',
  options: '',
  answer: '',
  explanation: '',
  score: 1,
  enabled: true,
  tags: '',
  knowledgePoint: {
    id: 0,
    name: '',
    description: '',
    course: {
      id: 0,
      name: '',
      description: '',
      subject: '',
      grade: '',
      semester: '',
      enabled: true,
      coverImage: '',
      totalHours: 0,
      completedHours: 0,
      status: 'DRAFT',
      teacherName: '',
      teacherId: 0,
      createTime: '',
      updateTime: ''
    },
    difficulty: 'EASY',
    orderNum: 0,
    enabled: true,
    content: '',
    keywords: '',
    createTime: '',
    updateTime: ''
  },
  createTime: '',
  updateTime: ''
})

// 知识点ID双向绑定
const knowledgePointId = computed({
  get: () => form.value.knowledgePoint?.id || 0,
  set: (value: number) => {
    const kp = props.knowledgePoints.find(k => k.id === value)
    if (kp) {
      form.value.knowledgePoint = {
        id: kp.id,
        name: kp.name,
        description: kp.description,
        course: {
          id: kp.courseId || 0,
          name: kp.courseName || '',
          description: '',
          subject: '',
          grade: '',
          semester: '',
          enabled: true,
          coverImage: '',
          totalHours: 0,
          completedHours: 0,
          status: 'DRAFT',
          teacherId: 0,
          teacherName: '',
          createTime: kp.createTime,
          updateTime: kp.updateTime
        },
        difficulty: kp.difficulty,
        orderNum: kp.orderNum,
        enabled: kp.enabled,
        content: kp.content || '',
        keywords: kp.keywords || '',
        createTime: kp.createTime,
        updateTime: kp.updateTime
      }
    }
  }
})

// 知识点下拉选项
const knowledgePointOptions = computed(() => [...props.knowledgePoints.filter(kp => kp.enabled).map(kp => ({ label: kp.name, value: kp.id }))])

// 是否显示选项编辑器
const showOptions = computed(() => {
  return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(form.value.type)
})
// 是否显示选项编辑器（仅单选/多选）
const showOptionEditor = computed(() => ['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(form.value.type))
// 是否显示答案选项（单选/多选/判断）
const showAnswerOptions = computed(() => ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(form.value.type))

// 选项列表
const optionsList = computed(() => {
  if (form.value.type === 'TRUE_FALSE') {
    return [
      { key: 'A', value: '正确' },
      { key: 'B', value: '错误' }
    ]
  }
  try {
    return JSON.parse(form.value.options || '[]')
  } catch {
    return []
  }
})

// 单选题/判断题答案
const answerSingle = ref(form.value.answer)
watch(answerSingle, v => (form.value.answer = v))

// 多选题答案
const answerMultiple = ref(form.value.answer ? form.value.answer.split(',') : [])
watch(answerMultiple, v => (form.value.answer = v.join(',')))

// 校验规则
const rules = {
  title: { required: true, message: '请输入题目标题', trigger: 'blur' },
  type: { required: true, message: '请选择题型', trigger: 'change' },
  difficulty: { required: true, message: '请选择难度', trigger: 'change' },
  answer: { required: true, message: '请输入正确答案', trigger: 'blur' },
  knowledgePointId: { required: true, message: '请选择知识点', trigger: 'change' }
}

const message = useMessage()

// 创建时间格式化
const formattedCreateTime = computed(() => {
  if (!form.value.createTime) return ''

  const date = new Date(form.value.createTime)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  // 如果是今天
  if (diffDays === 0) {
    return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  // 如果是昨天
  else if (diffDays === 1) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  // 如果是本年
  else if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  // 其他情况显示完整日期
  else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
})

// 关闭弹窗
function handleCancel() {
  emit('update:show', false)
  editMode.value = false
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      const updateData: UpdateQuestionDTO = {
        title: form.value.title,
        content: form.value.content,
        type: form.value.type as QuestionType,
        difficulty: form.value.difficulty as Difficulty,
        options: showOptions.value ? form.value.options : undefined,
        answer: form.value.answer,
        explanation: form.value.explanation,
        score: form.value.score,
        knowledgePointId: knowledgePointId.value,
        tags: form.value.tags,
        enabled: form.value.enabled
      }
      emit('submit', { id: form.value.id, data: updateData })
      editMode.value = false
    }
  })
}

// 启用/禁用题目
function handleToggleStatus() {
  const newEnabled = !form.value.enabled
  emit('toggle-status', { id: form.value.id, enabled: newEnabled })
}

// 查看历史
function handleViewHistory() {
  showHistoryModal.value = true
}

// 控制历史模态框显示
function updateHistoryModalShow(show: boolean) {
  showHistoryModal.value = show
}

// 处理版本回退
function handleReverted() {
  // 重新获取题目详情以显示最新数据
  message.success('版本回退成功，请重新查看题目详情')
  emit('update:show', false)
}

// 监听题目数据变化，更新表单
watch(
  () => props.question,
  val => {
    if (val) {
      form.value = { ...val }
      editMode.value = false
    }
  },
  { deep: true, immediate: true }
)
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 32px;
}
.form-col {
  flex: 1;
}
</style>
