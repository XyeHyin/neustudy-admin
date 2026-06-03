<template>
  <n-modal :show="show" preset="dialog" title="试卷详情" @close="handleCancel" style="width: 800px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="试卷标题" path="title">
            <n-input v-model:value="form.title" :disabled="!editMode" placeholder="请输入试卷标题" />
          </n-form-item>
          <n-form-item label="时间限制" path="timeLimit">
            <n-input-number v-model:value="form.timeLimit" :disabled="!editMode" :min="1" placeholder="分钟" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="状态" path="status">
            <n-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</n-tag>
          </n-form-item>
          <n-form-item label="总分" path="totalScore">
            <n-text>{{ form.totalScore }} 分</n-text>
          </n-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="显示答案">
            <n-switch v-model:value="form.showAnswer" :disabled="!editMode" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="允许重做">
            <n-switch v-model:value="form.allowRetry" :disabled="!editMode" />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="试卷描述" path="description">
        <n-input v-model:value="form.description" :disabled="!editMode" type="textarea" :rows="3" placeholder="请输入试卷描述" />
      </n-form-item>

      <!-- 题目列表 -->
      <n-divider title-placement="left">题目列表</n-divider>
      <div class="questions-section">
        <div class="questions-header">
          <n-text>共 {{ form.questions?.length || 0 }} 道题目</n-text>
          <n-button v-if="canEdit" type="primary" size="small" @click="handleManageQuestions">管理题目</n-button>
        </div>
        <n-list v-if="form.questions?.length">
          <n-list-item v-for="(question, index) in form.questions" :key="question.questionId">
            <template #prefix>
              <n-text>{{ index + 1 }}.</n-text>
            </template>
            <n-thing>
              <template #header>{{ question.title }}</template>
              <template #description>
                <n-space>
                  <n-tag size="small" :type="getQuestionTypeColor(question.type)">{{ getQuestionTypeText(question.type) }}</n-tag>
                  <n-text depth="3">{{ question.score }} 分</n-text>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
        <n-empty v-else description="暂无题目" />
      </div>

      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button v-if="canEdit && !editMode" type="primary" @click="editMode = true">编辑</n-button>
          <template v-if="editMode">
            <n-button @click="editMode = false">取消编辑</n-button>
            <n-button type="primary" :loading="loading" @click="handleSubmit">保存</n-button>
          </template>
          <n-button v-if="canPublish && form.status === 'DRAFT'" type="success" @click="handlePublish">发布</n-button>
          <n-button v-if="canArchive && form.status === 'PUBLISHED'" type="warning" @click="handleArchive">归档</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { getPaperStatusText as getStatusText, getPaperStatusType as getStatusType } from '@/utils/status'
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import type { PaperDetailVO, PaperUpdateDTO } from '@/api/types'

const props = defineProps<{
  show: boolean
  loading?: boolean
  paper: PaperDetailVO | null
  canEdit?: boolean
  canPublish?: boolean
  canArchive?: boolean
}>()

const emit = defineEmits(['update:show', 'submit', 'publish', 'archive', 'manage-questions'])

const formRef = ref()
const editMode = ref(false)

// 试卷表单数据
const form = ref<PaperDetailVO>({
  id: 0,
  title: '',
  description: '',
  teacherId: 0,
  teacherName: '',
  timeLimit: 60,
  showAnswer: false,
  allowRetry: false,
  questionOrderType: 'FIXED',
  status: 'DRAFT',
  totalScore: 0,
  createTime: '',
  updateTime: '',
  questions: []
})

// 表单校验规则
const rules = {
  title: { required: true, message: '请输入试卷标题', trigger: 'blur' },
  timeLimit: { required: true, type: 'number', message: '请输入时间限制', trigger: 'blur' }
}

const message = useMessage()

// 获取题目类型颜色
function getQuestionTypeColor(type: string) {
  switch (type) {
    case 'SINGLE_CHOICE':
    case 'MULTIPLE_CHOICE':
      return 'info'
    case 'TRUE_FALSE':
      return 'success'
    case 'FILL_BLANK':
      return 'warning'
    default:
      return 'default'
  }
}

// 获取题目类型文本
function getQuestionTypeText(type: string) {
  switch (type) {
    case 'SINGLE_CHOICE':
      return '单选题'
    case 'MULTIPLE_CHOICE':
      return '多选题'
    case 'TRUE_FALSE':
      return '判断题'
    case 'FILL_BLANK':
      return '填空题'
    case 'SHORT_ANSWER':
      return '简答题'
    case 'ESSAY':
      return '论述题'
    default:
      return type
  }
}

// 关闭弹窗
function handleCancel() {
  emit('update:show', false)
  editMode.value = false
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      const updateData: PaperUpdateDTO = {
        title: form.value.title,
        description: form.value.description,
        timeLimit: form.value.timeLimit,
        showAnswer: form.value.showAnswer,
        allowRetry: form.value.allowRetry,
        questionOrderType: form.value.questionOrderType,
        status: form.value.status
      }
      emit('submit', form.value.id, updateData)
    }
  })
}

// 发布试卷
function handlePublish() {
  emit('publish', form.value.id)
}

// 归档试卷
function handleArchive() {
  emit('archive', form.value.id)
}

// 管理题目
function handleManageQuestions() {
  emit('manage-questions')
}

// 监听试卷数据变化，更新表单
watch(
  () => props.paper,
  newPaper => {
    if (newPaper) {
      form.value = { ...newPaper }
    }
  },
  { immediate: true }
)

watch(
  () => props.show,
  show => {
    if (!show) {
      editMode.value = false
    }
  }
)
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
}

.questions-section {
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
  max-height: 300px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #888 transparent;
}

.questions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
