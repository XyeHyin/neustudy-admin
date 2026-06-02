<template>
  <div class="options-editor">
    <div v-for="(option, index) in options" :key="index" class="option-item">
      <n-input-group>
        <n-input v-model:value="option.key" :disabled="disabled" placeholder="选项标识" style="width: 80px" @update:value="updateOptions" />
        <n-input v-model:value="option.value" :disabled="disabled" placeholder="选项内容" style="flex: 1" @update:value="updateOptions" />
        <n-button v-if="!disabled && options.length > 1" @click="removeOption(index)" quaternary type="error"> 删除 </n-button>
      </n-input-group>
    </div>
    <n-button v-if="!disabled" @click="addOption" dashed block style="margin-top: 8px"> 添加选项 </n-button>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'

// 选项类型定义
interface Option {
  key: string
  value: string
}

// 组件属性定义
const props = defineProps<{
  value?: string
  disabled?: boolean
  questionType?: string
}>()

const emit = defineEmits(['update:value'])

// 选项数组
const options = ref<Option[]>([
  { key: 'A', value: '' },
  { key: 'B', value: '' }
])

// 根据题型自动设置选项
const initOptions = () => {
  switch (props.questionType) {
    case 'TRUE_FALSE':
      options.value = [
        { key: 'A', value: '正确' },
        { key: 'B', value: '错误' }
      ]
      break
    case 'SINGLE_CHOICE':
    case 'MULTIPLE_CHOICE':
      if (options.value.length < 2) {
        options.value = [
          { key: 'A', value: '' },
          { key: 'B', value: '' }
        ]
      }
      break
    default:
      options.value = []
  }
}

// 添加选项
const addOption = () => {
  const nextKey = String.fromCharCode(65 + options.value.length) // A, B, C, D...
  options.value.push({ key: nextKey, value: '' })
  updateOptions()
}

// 删除选项
const removeOption = (index: number) => {
  options.value.splice(index, 1)
  // 重新分配键值
  options.value.forEach((option, idx) => {
    option.key = String.fromCharCode(65 + idx)
  })
  updateOptions()
}

// 更新选项（同步到父组件）
const updateOptions = () => {
  const jsonString = JSON.stringify(options.value)
  emit('update:value', jsonString)
}

// 解析传入的选项
watch(
  () => props.value,
  newValue => {
    if (newValue) {
      try {
        const parsed = JSON.parse(newValue)
        if (Array.isArray(parsed)) {
          options.value = parsed
        }
      } catch (e) {
        console.warn('Failed to parse options:', e)
      }
    } else {
      initOptions()
    }
  },
  { immediate: true }
)

// 监听题型变化，自动初始化选项
watch(
  () => props.questionType,
  () => {
    if (!props.value) {
      initOptions()
      updateOptions()
    }
  }
)
</script>

<style scoped>
.options-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
}
</style>
