<template>
  <n-modal :show="show" preset="dialog" title="新建试卷" @close="handleCancel" style="width: 600px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="100" style="padding: 16px 24px">
      <n-form-item label="试卷标题" path="title">
        <n-input v-model:value="form.title" placeholder="请输入试卷标题" />
      </n-form-item>

      <n-form-item label="试卷描述" path="description">
        <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入试卷描述" />
      </n-form-item>

      <div class="form-row">
        <div class="form-col">
          <n-form-item label="时间限制" path="timeLimit">
            <n-input-number v-model:value="form.timeLimit" :min="1" :max="600" placeholder="分钟" style="width: 100%" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="题目顺序" path="questionOrderType">
            <n-select v-model:value="form.questionOrderType" :options="orderTypeOptions" />
          </n-form-item>
        </div>
      </div>

      <div class="form-row">
        <div class="form-col">
          <n-form-item label="显示答案">
            <n-switch v-model:value="form.showAnswer" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="允许重做">
            <n-switch v-model:value="form.allowRetry" />
          </n-form-item>
        </div>
      </div>

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
import { defineEmits, defineProps, ref } from 'vue'

import { PAPER_QUESTION_ORDER_OPTIONS as orderTypeOptions } from '@/constants/options'
import { useAuthStore } from '@/store/auth'

import type { PaperCreateDTO } from '@/api/types'

defineProps<{
  show: boolean
  loading?: boolean
}>()

const emit = defineEmits(['update:show', 'submit'])

const auth = useAuthStore()
const formRef = ref()

// 表单数据
const form = ref<PaperCreateDTO>({
  title: '',
  description: '',
  teacherId: auth.user?.id || 0,
  timeLimit: 90,
  showAnswer: false,
  allowRetry: true,
  questionOrderType: 'FIXED'
})

// 表单校验规则
const rules = {
  title: { required: true, message: '请输入试卷标题', trigger: 'blur' },
  timeLimit: { required: true, type: 'number', message: '请输入时间限制', trigger: 'blur' }
}

// 事件处理
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit', { ...form.value })
    }
  })
}

function handleCancel() {
  emit('update:show', false)
  // 重置表单
  form.value = {
    title: '',
    description: '',
    teacherId: auth.user?.id || 0,
    timeLimit: 90,
    showAnswer: false,
    allowRetry: true,
    questionOrderType: 'FIXED'
  }
}
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
}
</style>
