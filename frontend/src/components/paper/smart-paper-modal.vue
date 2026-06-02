<template>
  <n-modal :show="show" preset="dialog" title="智能组卷" @close="handleCancel" style="width: 700px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="120" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="难度要求" path="difficulty">
            <n-select v-model:value="form.difficulty" :options="difficultyOptions" placeholder="选择难度" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="总题数" path="totalQuestions">
            <n-input-number v-model:value="form.totalQuestions" :min="1" :max="100" style="width: 100%" />
          </n-form-item>
        </div>
      </div>

      <div class="form-row">
        <div class="form-col">
          <n-form-item label="时间限制" path="timeLimit">
            <n-input-number v-model:value="form.timeLimit" :min="1" :max="600" style="width: 100%" suffix="分钟" />
          </n-form-item>
        </div>
      </div>

      <n-form-item label="知识点" path="knowledgePointIds">
        <n-select v-model:value="form.knowledgePointIds" :options="knowledgePointOptions" multiple placeholder="选择知识点（可多选）" :loading="loadingKnowledgePoints" />
      </n-form-item>

      <n-form-item label="题型要求" path="questionTypes">
        <n-select v-model:value="form.questionTypes" :options="questionTypeOptions" multiple placeholder="选择题型（可多选）" />
      </n-form-item>

      <!-- 题型分布 -->
      <n-form-item label="题型分布">
        <div class="type-distribution">
          <div v-for="type in selectedTypes" :key="type" class="distribution-item">
            <span class="type-label">{{ getTypeLabel(type) }}</span>
            <n-input-number v-model:value="form.questionTypeDistribution[type]" :min="0" :max="form.totalQuestions || 20" size="small" style="width: 80px" />
            <span class="unit">题</span>
          </div>
        </div>
      </n-form-item>

      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleSubmit">开始组卷</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getKnowledgePoints } from '@/api/knowledge-point'
import { useAuthStore } from '@/store/auth'

import type { KnowledgePointVO, SmartPaperDTO } from '@/api/types'

interface SmartPaperForm extends Omit<SmartPaperDTO, 'questionTypeDistribution'> {
  timeLimit: number
  questionTypeDistribution: Record<string, number>
}

const props = defineProps<{
  show: boolean
  loading?: boolean
}>()

const emit = defineEmits(['update:show', 'submit'])

const auth = useAuthStore()
const message = useMessage()
const formRef = ref()

// 数据
const knowledgePoints = ref<KnowledgePointVO[]>([])

// 表单数据
const form = ref<SmartPaperForm>({
  difficulty: 'MEDIUM',
  knowledgePointIds: [],
  questionTypes: [],
  totalQuestions: 10,
  totalScore: 100,
  timeLimit: 90,
  questionTypeDistribution: {}
})

// 表单校验规则
const rules = {
  totalQuestions: { required: true, type: 'number', message: '请输入总题数', trigger: 'blur' },
  timeLimit: { required: true, type: 'number', message: '请输入时间限制', trigger: 'blur' }
}

// 选项
const difficultyOptions = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

const questionTypeOptions = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '填空题', value: 'FILL_BLANK' },
  { label: '简答题', value: 'SHORT_ANSWER' },
  { label: '论述题', value: 'ESSAY' }
]

const knowledgePointOptions = computed(() =>
  knowledgePoints.value.map(kp => ({
    label: kp.name,
    value: kp.id
  }))
)

const selectedTypes = computed(() => form.value.questionTypes || [])

// 获取知识点
const { loading: loadingKnowledgePoints, run: fetchKnowledgePoints } = useRequest(getKnowledgePoints, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      knowledgePoints.value = res.data
    }
  }
})

// 工具函数
function getTypeLabel(type: string) {
  const typeMap: Record<string, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题',
    ESSAY: '论述题'
  }
  return typeMap[type] || type
}

// 事件处理
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      // 验证题型分布总数
      const distributionTotal = Object.values(form.value.questionTypeDistribution || {}).reduce((sum: number, count: any) => sum + (Number(count) || 0), 0)

      if (distributionTotal !== form.value.totalQuestions) {
        message.warning(`题型分布总数（${distributionTotal}）应等于总题数（${form.value.totalQuestions}）`)
        return
      }

      const submitData: SmartPaperDTO & {teacherId: number; timeLimit: number } = {
        ...form.value,
        teacherId: auth.user?.id || 0
      }

      emit('submit', submitData)
    }
  })
}

function handleCancel() {
  emit('update:show', false)
  resetForm()
}

function resetForm() {
  form.value = {
    difficulty: 'MEDIUM',
    knowledgePointIds: [],
    questionTypes: [],
    totalQuestions: 10,
    totalScore: 100,
    timeLimit: 90,
    questionTypeDistribution: {}
  }
}

// 监听题型选择变化，自动分配题型分布
watch(
  () => form.value.questionTypes,
  (newTypes: string[] = []) => {
    const distribution: Record<string, number> = {}
    const totalQuestions = form.value.totalQuestions || 10
    const avgPerType = Math.floor(totalQuestions / newTypes.length)
    const remainder = totalQuestions % newTypes.length

    newTypes.forEach((type, index) => {
      distribution[type] = avgPerType + (index < remainder ? 1 : 0)
    })

    form.value.questionTypeDistribution = distribution
  },
  { deep: true }
)

// 监听总题数变化，重新分配题型分布
watch(
  () => form.value.totalQuestions,
  (newTotal: number = 0) => {
    if (selectedTypes.value.length > 0) {
      const distribution: Record<string, number> = {}
      const avgPerType = Math.floor(newTotal / selectedTypes.value.length)
      const remainder = newTotal % selectedTypes.value.length

      selectedTypes.value.forEach((type, index) => {
        distribution[type] = avgPerType + (index < remainder ? 1 : 0)
      })

      form.value.questionTypeDistribution = distribution
    }
  }
)

onMounted(() => {
  if (props.show) {
    fetchKnowledgePoints()
  }
})

watch(
  () => props.show,
  show => {
    if (show) {
      fetchKnowledgePoints()
    } else {
      resetForm()
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

.type-distribution {
  width: 100%;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.type-label {
  width: 80px;
  text-align: right;
}

.unit {
  color: var(--n-text-color-3);
}
</style>
