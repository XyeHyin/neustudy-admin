<template>
  <n-modal :show="show" preset="dialog" title="创建知识点" @close="handleCancel" style="width: 800px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="知识点名称" path="name">
            <n-input v-model:value="form.name" placeholder="请输入知识点名称" />
          </n-form-item>
          <n-form-item label="所属课程" path="courseId">
            <n-select v-model:value="form.courseId" :options="courseOptions" placeholder="请选择课程" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="难度等级" path="difficulty">
            <n-select v-model:value="form.difficulty" :options="difficultyOptions" placeholder="请选择难度" />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="知识点描述" path="description">
        <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入知识点描述" />
      </n-form-item>
      <n-form-item label="详细内容" path="content">
        <n-input v-model:value="form.content" type="textarea" :rows="4" placeholder="请输入详细内容" />
      </n-form-item>
      <n-form-item label="关键词" path="keywords">
        <n-input v-model:value="form.keywords" placeholder="请输入关键词，用逗号分隔" />
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
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import type { FormInst } from 'naive-ui'
import type { CourseVO, CreateKnowledgePointDTO } from '@/api/types'

const props = defineProps<{
  show: boolean
  loading?: boolean
  courses: CourseVO[]
}>()

const emit = defineEmits(['update:show', 'submit'])

const formRef = ref<FormInst>()

// 知识点表单数据
const form = ref<CreateKnowledgePointDTO>({
  name: '',
  description: '',
  courseId: 0,
  difficulty: 'EASY',
  content: '',
  orderNum: 0,
  keywords: ''
})

// 校验规则
const rules = {
  name: { required: true, message: '请输入知识点名称', trigger: 'blur' },
  description: { required: true, message: '请输入知识点描述', trigger: 'blur' },
  courseId: {
    required: true,
    validator: (rule: any, value: number) => {
      if (value === undefined || value === null || value === 0) {
        return new Error('请选择课程')
      }
      return true
    },
    trigger: 'change'
  },
  difficulty: { required: true, message: '请选择难度', trigger: 'change' }
}

// 课程选项
const courseOptions = computed(() => [{ label: '请选择课程', value: 0 }, ...props.courses.map(c => ({ label: c.name, value: c.id }))])

// 难度选项
const difficultyOptions = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

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
      console.log('提交的表单数据:', form.value) // 添加调试日志
      emit('submit', form.value)
      resetForm()
    } else {
      console.log('验证错误:', errors) // 添加错误日志
    }
  })
}

// 重置表单
function resetForm() {
  form.value = {
    name: '',
    description: '',
    courseId: 0,
    difficulty: 'EASY',
    content: '',
    orderNum: 0,
    keywords: ''
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
