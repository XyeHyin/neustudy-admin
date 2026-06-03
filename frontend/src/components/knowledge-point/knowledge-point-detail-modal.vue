<template>
  <n-modal :show="show" preset="dialog" title="知识点详情" @close="handleCancel" style="width: 800px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="知识点名称" path="name">
            <n-input v-model:value="form.name" :disabled="!editMode" placeholder="请输入知识点名称" />
          </n-form-item>
          <n-form-item label="所属课程" path="courseName">
            <n-input v-model:value="courseName" disabled />
          </n-form-item>
          <n-form-item label="难度等级" path="difficulty">
            <n-select v-model:value="form.difficulty" :disabled="!editMode" :options="difficultyOptions" placeholder="请选择难度" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="状态" path="enabled">
            <n-tag :type="getStatusType(form.enabled)">{{ getStatusText(form.enabled) }}</n-tag>
          </n-form-item>
          <n-form-item label="创建时间" path="createTime">
            <n-input :value="formattedCreateTime" disabled />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="知识点描述" path="description">
        <n-input v-model:value="form.description" :disabled="!editMode" type="textarea" :rows="3" placeholder="请输入知识点描述" />
      </n-form-item>
      <n-form-item label="详细内容" path="content">
        <n-input v-model:value="form.content" :disabled="!editMode" type="textarea" :rows="4" placeholder="请输入详细内容" />
      </n-form-item>
      <n-form-item label="关键词" path="keywords">
        <n-input v-model:value="form.keywords" :disabled="!editMode" placeholder="请输入关键词，用逗号分隔" />
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
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { getEnabledStatusText as getStatusText, getEnabledStatusType as getStatusType } from '@/utils/status'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import type { CourseVO, KnowledgePointDetailVO, UpdateKnowledgePointDTO } from '@/api/types'

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  updatingStatus?: boolean
  courses: CourseVO[]
  knowledgePoint: KnowledgePointDetailVO | null
  canEdit?: boolean
  canUpdateStatus?: boolean
}>()

const emit = defineEmits(['update:show', 'submit', 'toggle-status'])

const formRef = ref()
const editMode = ref(false)

// 知识点表单数据
const form = ref<KnowledgePointDetailVO>({
  id: 0,
  name: '',
  description: '',
  difficulty: 'EASY',
  enabled: true,
  orderNum: 0,
  createTime: '',
  updateTime: '',
  content: '',
  keywords: ''
})

// 所属课程名称
const courseName = computed(() => {
  return form.value.course?.name || ''
})

// 表单校验规则
const rules = {
  name: { required: true, message: '请输入知识点名称', trigger: 'blur' },
  description: { required: true, message: '请输入知识点描述', trigger: 'blur' },
  difficulty: { required: true, message: '请选择难度', trigger: 'change' }
}

// 难度选项
const difficultyOptions = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

// 关闭弹窗
function handleCancel() {
  emit('update:show', false)
  editMode.value = false
}

// 格式化创建时间
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

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      const updateData: UpdateKnowledgePointDTO = {
        name: form.value.name,
        description: form.value.description,
        difficulty: form.value.difficulty,
        content: form.value.content,
        orderNum: form.value.orderNum,
        keywords: form.value.keywords
      }
      emit('submit', { id: form.value.id, data: updateData })
      editMode.value = false
    }
  })
}

// 启用/禁用知识点
function handleToggleStatus() {
  const newEnabled = !form.value.enabled
  emit('toggle-status', { id: form.value.id, enabled: newEnabled })
}

// 监听知识点数据变化，更新表单
watch(
  () => props.knowledgePoint,
  val => {
    if (val) {
      form.value = {
        ...val
      }
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
