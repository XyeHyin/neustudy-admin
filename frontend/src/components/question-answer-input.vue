<template>
  <div class="question-answer-input">
    <!-- 单选题 -->
    <n-radio-group v-if="question.questionType === 'SINGLE_CHOICE'" v-model:value="selectedValue" :disabled="disabled">
      <n-space vertical>
        <n-radio v-for="option in options" :key="option.key" :value="option.key"> {{ option.key }}. {{ option.value }} </n-radio>
      </n-space>
    </n-radio-group>

    <!-- 多选题 -->
    <n-checkbox-group v-else-if="question.questionType === 'MULTIPLE_CHOICE'" v-model:value="selectedValues" :disabled="disabled">
      <n-space vertical>
        <n-checkbox v-for="option in options" :key="option.key" :value="option.key"> {{ option.key }}. {{ option.value }} </n-checkbox>
      </n-space>
    </n-checkbox-group>

    <!-- 判断题 -->
    <n-radio-group v-else-if="question.questionType === 'TRUE_FALSE'" v-model:value="selectedValue" :disabled="disabled">
      <n-space>
        <n-radio value="true">正确</n-radio>
        <n-radio value="false">错误</n-radio>
      </n-space>
    </n-radio-group>

    <!-- 填空/简答/论述 -->
    <n-input
      v-else
      v-model:value="textValue"
      :type="question.questionType === 'FILL_BLANK' ? 'text' : 'textarea'"
      :rows="question.questionType === 'FILL_BLANK' ? 1 : 6"
      :placeholder="getPlaceholder()"
      :disabled="disabled"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, defineEmits, defineProps, ref, watch, withDefaults } from 'vue'

interface Option {
  key: string
  value: string
}

interface Props {
  value: string
  question: { questionType?: string; options?: string | Option[] }
  disabled?: boolean
}

// props + 默认值
const props = withDefaults(defineProps<Props>(), {
  disabled: false
})

// 声明 update:value 事件
const emit = defineEmits<{
  (e: 'update:value', v: string): void
}>()

// 修改：同时支持 string/Option[] 两种格式
const options = computed<Option[]>(() => {
  if (!['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(props.question.questionType || '')) return []
  const raw = props.question.options
  if (Array.isArray(raw)) {
    return raw
  }
  try {
    const arr = JSON.parse(raw ?? '[]')
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

const selectedValue = ref<string>(props.value)
const selectedValues = ref<string[]>(props.value ? props.value.split(',') : [])
const textValue = ref<string>(props.value)

// 同步向外发射
watch(selectedValue, v => emit('update:value', v || ''))
watch(selectedValues, v => emit('update:value', v.join(',')))
watch(textValue, v => emit('update:value', v || ''))

// 支持外部 v-model 同步
watch(
  () => props.value,
  v => {
    selectedValue.value = v
    selectedValues.value = v ? v.split(',') : []
    textValue.value = v
  }
)

// 占位提示
function getPlaceholder() {
  switch (props.question.questionType) {
    case 'FILL_BLANK':
      return '请输入答案'
    case 'SHORT_ANSWER':
      return '请输入简答'
    case 'ESSAY':
      return '请输入论述'
    default:
      return '请输入答案'
  }
}
</script>

<style scoped>
.question-answer-input {
  width: 100%;
}
</style>
