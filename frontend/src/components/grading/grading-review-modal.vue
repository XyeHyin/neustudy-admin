<template>
  <n-modal :show="show" preset="card" title="人工复核" @close="handleClose" style="width: 800px">
    <template v-if="gradingResult">
      <div class="review-content">
        <!-- 基本信息 -->
        <n-descriptions title="基本信息" :column="2" bordered>
          <n-descriptions-item label="学生姓名">{{ gradingResult.studentName }}</n-descriptions-item>
          <n-descriptions-item label="题目标题">{{ gradingResult.questionTitle }}</n-descriptions-item>
          <n-descriptions-item label="AI判分时间">{{ formatTime(gradingResult.aiGradingTime) }}</n-descriptions-item>
          <n-descriptions-item label="AI评分">{{ gradingResult.aiScore }} 分</n-descriptions-item>
        </n-descriptions>

        <!-- 学生答案 -->
        <n-divider title-placement="left">学生答案</n-divider>
        <n-card>
          <pre class="answer-content">{{ gradingResult.answerContent }}</pre>
        </n-card>

        <!-- AI评语和理由 -->
        <n-divider title-placement="left">AI评价</n-divider>
        <n-space vertical>
          <n-card title="AI评语">
            <p>{{ gradingResult.aiComment }}</p>
          </n-card>
          <n-card title="AI判分理由">
            <p>{{ gradingResult.aiReason }}</p>
          </n-card>
        </n-space>

        <!-- 人工复核表单 -->
        <n-divider title-placement="left">人工复核</n-divider>
        <n-form ref="formRef" :model="form" :rules="rules" label-width="100">
          <n-form-item label="最终得分" path="finalScore">
            <n-input-number v-model:value="form.finalScore" :min="0" :max="100" :precision="1" style="width: 200px" />
          </n-form-item>

          <n-form-item label="教师评语" path="teacherComment">
            <n-input v-model:value="form.teacherComment" type="textarea" :rows="4" placeholder="请输入您的评语和改进建议" />
          </n-form-item>
        </n-form>
      </div>
    </template>

    <template #action>
      <n-space justify="end">
        <n-button @click="handleClose">取消</n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">确认复核</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { defineEmits, defineProps, ref, watch } from 'vue'

import { formatDateTime } from '@/utils/datetime'

import type { GradingReviewVO, ManualGradingDTO } from '@/api/types'

const props = defineProps<{
  show: boolean
  gradingResult: GradingReviewVO | null
  loading?: boolean
}>()

const emit = defineEmits(['update:show', 'submit'])

const message = useMessage()
const formRef = ref()

// 表单数据
const form = ref<ManualGradingDTO>({
  gradingResultId: 0,
  finalScore: 0,
  teacherComment: ''
})

// 表单校验规则
const rules = {
  finalScore: {
    required: true,
    type: 'number',
    message: '请输入最终得分',
    trigger: 'blur'
  }
}

// 工具函数
function formatTime(timeStr: string) {
  return formatDateTime(timeStr)
}

// 事件处理
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit', { ...form.value })
    }
  })
}

function handleClose() {
  emit('update:show', false)
}

// 监听判分结果变化
watch(
  () => props.gradingResult,
  newResult => {
    if (newResult) {
      form.value = {
        gradingResultId: newResult.gradingResultId,
        finalScore: newResult.aiScore, // 默认使用AI评分
        teacherComment: ''
      }
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.review-content {
  max-height: 70vh;
  overflow-y: auto;
}

.answer-content {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  font-family: inherit;
}
</style>
