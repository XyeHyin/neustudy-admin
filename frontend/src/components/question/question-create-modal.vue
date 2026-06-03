<template>
  <n-modal :show="show" preset="dialog" title="创建题目" @close="handleCancel" style="width: 900px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="题目标题" path="title">
            <n-input v-model:value="form.title" placeholder="请输入题目标题" />
          </n-form-item>
          <n-form-item label="题型" path="type">
            <n-select v-model:value="form.type" :options="typeOptions" placeholder="请选择题型" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="难度" path="difficulty">
            <n-select v-model:value="form.difficulty" :options="difficultyOptions" placeholder="请选择难度" />
          </n-form-item>
          <n-form-item label="分值" path="score">
            <n-input-number v-model:value="form.score" :min="1" :max="100" />
          </n-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="知识点" path="knowledgePointId">
            <n-select v-model:value="form.knowledgePointId" :options="knowledgePointOptions" placeholder="请选择知识点" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="标签" path="tags">
            <n-input v-model:value="form.tags" placeholder="请输入标签，用逗号分隔" />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="题目内容" path="content">
        <n-input v-model:value="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
      </n-form-item>
      <n-form-item v-if="showOptionEditor" label="选项" path="options">
        <question-options-editor v-model:value="form.options" :question-type="form.type" />
      </n-form-item>
      <n-form-item v-if="showAnswerOptions" label="正确答案" path="answer">
        <n-radio-group v-if="form.type !== 'MULTIPLE_CHOICE'" v-model:value="answerSingle">
          <n-radio v-for="opt in optionsList" :key="opt.key" :value="opt.key"> {{ opt.key }}. {{ opt.value }} </n-radio>
        </n-radio-group>
        <n-checkbox-group v-else v-model:value="answerMultiple">
          <n-checkbox v-for="opt in optionsList" :key="opt.key" :value="opt.key"> {{ opt.key }}. {{ opt.value }} </n-checkbox>
        </n-checkbox-group>
      </n-form-item>
      <n-form-item v-else label="正确答案" path="answer">
        <n-input v-model:value="form.answer" type="textarea" :rows="2" placeholder="请输入正确答案" />
      </n-form-item>
      <n-form-item label="答案解析" path="explanation">
        <n-input v-model:value="form.explanation" type="textarea" :rows="3" placeholder="请输入答案解析" />
      </n-form-item>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleSubmit">创建</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { NCheckbox, NCheckboxGroup, NRadio, NRadioGroup, useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import QuestionOptionsEditor from './question-options-editor.vue'
import { QUESTION_DIFFICULTY_OPTIONS as difficultyOptions, QUESTION_TYPE_OPTIONS as typeOptions } from '@/constants/question'

import type { FormInst } from 'naive-ui'
import type { CreateQuestionDTO, Difficulty, KnowledgePointVO, QuestionType } from '@/api/types'

// 表单引用
const formRef = ref<FormInst>()

// 题目表单数据
const form = ref<CreateQuestionDTO>({
  title: '',
  content: '',
  type: 'SINGLE_CHOICE',
  difficulty: 'EASY',
  options: '',
  answer: '',
  explanation: '',
  score: 1,
  knowledgePointId: null,
  enabled: true,
  tags: ''
})

// 选项列表（用于单选、多选、判断题）
const optionsList = computed(() => {
  // 判断题固定选项
  if (form.value.type === 'TRUE_FALSE') {
    return [
      { key: 'A', value: '正确' },
      { key: 'B', value: '错误' }
    ]
  }
  // 单选/多选题解析JSON
  if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(form.value.type)) {
    try {
      const arr = JSON.parse(form.value.options || '[]')
      if (!Array.isArray(arr) || arr.length === 0) {
        return [
          { key: 'A', value: '' },
          { key: 'B', value: '' }
        ]
      }
      return arr
    } catch {
      return [
        { key: 'A', value: '' },
        { key: 'B', value: '' }
      ]
    }
  }
  // 其他题型
  try {
    return JSON.parse(form.value.options || '[]')
  } catch {
    return []
  }
})

// 单选题/判断题答案
const answerSingle = ref<string>(form.value.answer)
watch(answerSingle, v => (form.value.answer = v))
// 多选题答案
const answerMultiple = ref<string[]>(form.value.answer ? form.value.answer.split(',') : [])
watch(answerMultiple, v => (form.value.answer = v.join(',')))

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  knowledgePoints: KnowledgePointVO[]
  isAdmin?: boolean
}>()

const emit = defineEmits(['update:show', 'submit'])

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

// 表单校验规则
const rules = {
  title: { required: true, message: '请输入题目标题', trigger: 'blur' },
  type: { required: true, message: '请选择题型', trigger: 'change' },
  difficulty: { required: true, message: '请选择难度', trigger: 'change' },
  answer: { required: true, message: '请输入正确答案', trigger: 'blur' },
  knowledgePointId: {
    required: true,
    validator: (rule: any, value: number) => {
      if (value === undefined || value === null || value === 0) {
        return new Error('请选择知识点')
      }
      return true
    },
    trigger: 'change'
  }
}

const message = useMessage()

// 关闭弹窗并重置表单
function handleCancel() {
  emit('update:show', false)
  resetForm()
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      const submitData = { ...form.value }
      if (!showOptions.value) {
        delete submitData.options
      }
      emit('submit', submitData)
      resetForm()
    }
  })
}

// 重置表单
function resetForm() {
  form.value = {
    title: '',
    content: '',
    type: 'SINGLE_CHOICE',
    difficulty: 'EASY',
    options: '',
    answer: '',
    explanation: '',
    score: 1,
    knowledgePointId: null,
    enabled: true,
    tags: ''
  }
}

// 监听弹窗关闭时重置表单
watch(
  () => props.show,
  val => {
    if (!val) {
      resetForm()
    }
  }
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
