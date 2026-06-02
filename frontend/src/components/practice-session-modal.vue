<template>
  <n-modal :show="show" preset="card" title="练习答题" @close="handleClose" style="width: 90vw; max-width: 1200px; height: 80vh">
    <template v-if="practiceDetail">
      <!-- 练习头部信息 -->
      <div class="practice-header">
        <div class="practice-info">
          <h3>{{ practiceDetail.paperTitle }}</h3>
          <n-space>
            <n-tag type="info">练习中</n-tag>
            <n-tag :type="practiceDetail.submitted ? 'success' : 'warning'">
              {{ practiceDetail.submitted ? '已提交' : '答题中' }}
            </n-tag>
          </n-space>
        </div>
        <div class="practice-actions">
          <n-button v-if="!practiceDetail.submitted" type="primary" @click="handleSubmit" :loading="submitting">提交答案</n-button>
          <n-button @click="handleClose">关闭</n-button>
        </div>
      </div>

      <!-- 练习进度 -->
      <div class="practice-progress">
        <n-progress :percentage="progressPercentage" />
        <n-text depth="3">已答题 {{ answeredCount }} / {{ totalQuestions }} 题</n-text>
      </div>

      <!-- 题目导航和答题区域 -->
      <div class="practice-content">
        <!-- 题目导航 -->
        <div class="question-nav">
          <n-scrollbar style="max-height: 400px">
            <div class="nav-grid">
              <div
                v-for="(question, index) in questions"
                :key="question.questionId!"
                class="nav-item"
                :class="{
                  'nav-item--active': currentQuestionIndex === index,
                  'nav-item--answered': isQuestionAnswered(question.questionId!),
                  'nav-item--marked': isQuestionMarked(question.questionId!)
                }"
                @click="setCurrentQuestion(index)"
              >
                {{ index + 1 }}
              </div>
            </div>
          </n-scrollbar>
        </div>

        <!-- 答题区域 -->
        <div class="question-area">
          <div v-if="currentQuestion" class="question-content">
            <!-- 题目标题 -->
            <div class="question-header">
              <n-space justify="space-between">
                <h4>第 {{ currentQuestionIndex + 1 }} 题 ({{ currentQuestion.score }} 分)</h4>
                <n-space>
                  <n-button size="small" @click="markQuestion" :type="isCurrentQuestionMarked ? 'warning' : 'default'">
                    {{ isCurrentQuestionMarked ? '取消标记' : '标记' }}
                  </n-button>
                </n-space>
              </n-space>
            </div>

            <!-- 题目内容 -->
            <div class="question-body">
              <div class="question-title">{{ currentQuestion.content }}</div>
              <div class="question-type">
                <n-tag size="small" :type="getQuestionTypeColor(currentQuestion.type)">
                  {{ getQuestionTypeText(currentQuestion.type) }}
                </n-tag>
              </div>

              <!-- 答题区域 -->
              <div class="answer-area">
                <!-- 由 formattedQuestion 传入完整的 PracticeAnswerVO -->
                <question-answer-input v-model:value="currentAnswer" :question="formattedQuestion" :disabled="practiceDetail.submitted" @update:value="handleAnswerChange" />
              </div>
            </div>

            <!-- 题目导航按钮 -->
            <div class="question-nav-buttons">
              <n-space justify="space-between">
                <n-button @click="previousQuestion" :disabled="currentQuestionIndex === 0">上一题</n-button>
                <n-button @click="nextQuestion" :disabled="currentQuestionIndex === questions.length - 1">下一题</n-button>
              </n-space>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="loading-container">
      <n-spin size="large" />
    </div>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getPracticeDetail, markPracticeQuestion } from '@/api/practice'
import QuestionAnswerInput from '@/components/question-answer-input.vue'

import type { AnswerDTO, PracticeAnswerVO, PracticeDetailVO, PracticeSubmitDTO } from '@/api/types'

const props = defineProps<{
  show: boolean
  sessionId: number | null
}>()

const emit = defineEmits(['update:show', 'submit'])

const message = useMessage()

// 数据
const practiceDetail = ref<PracticeDetailVO | null>(null)
const currentQuestionIndex = ref(0)
const answers = ref<Map<number, string>>(new Map())
const markStatus = ref<Map<number, boolean>>(new Map())

// 计算属性
const questions = computed(() => practiceDetail.value?.questions || [])
const totalQuestions = computed(() => questions.value.length)
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])
const currentAnswer = computed({
  get: () => answers.value.get(currentQuestion.value!.questionId!) || '',
  set: (value: string) => {
    answers.value.set(currentQuestion.value!.questionId!, value)
  }
})

const formattedQuestion = computed<PracticeAnswerVO & { options?: string }>(() => {
  const q = currentQuestion.value
  if (!q) return { questionId: 0, questionTitle: '', questionType: '', answerContent: '', correct: '', score: 0, marked: false, options: '[]' }
  return {
    questionId: q.questionId!,
    questionTitle: q.content || '',
    questionType: q.type || '',
    answerContent: currentAnswer.value,
    correct: (q as any).correct || '',
    score: q.score || 0,
    marked: q.marked || false,
    // 确保 options 是 string，否则强制 stringify
    options: typeof q.options === 'string' ? q.options : JSON.stringify(q.options || [])
  }
})

const answeredCount = computed(() => {
  return questions.value.filter(q => !!answers.value.get(q.questionId!)).length
})

const progressPercentage = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((answeredCount.value / totalQuestions.value) * 100)
})

const isCurrentQuestionMarked = computed(() => markStatus.value.get(currentQuestion.value!.questionId!) || false)

// 获取练习详情
const { loading: loadingSession, run: fetchPracticeDetail } = useRequest((id: number) => getPracticeDetail(id), {
  manual: true,
  onSuccess: (res: any) => {
    if (res.code === 200) {
      practiceDetail.value = res.data
      // 初始化答案和标记状态 - 使用 PracticeDetailVO 的字段
      res.data.questions?.forEach((question: any) => {
        if (question.studentAnswer) {
          answers.value.set(question.questionId, question.studentAnswer)
        }
        if (question.marked) {
          markStatus.value.set(question.questionId, true)
        }
      })
    } else {
      message.error(res.message || '获取练习详情失败')
    }
  }
})

// 标记题目
const { loading: marking, runAsync: runMarkQuestion } = useRequest(markPracticeQuestion, { manual: true })

const submitting = ref(false)

// 工具函数
function getQuestionTypeColor(type?: string) {
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

function getQuestionTypeText(type?: string) {
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
      return type || ''
  }
}

/** 修改：接收 number | undefined，内部判空 */
function isQuestionAnswered(questionId?: number) {
  return questionId !== undefined && answers.value.has(questionId) && !!answers.value.get(questionId)
}

function isQuestionMarked(questionId?: number) {
  return questionId !== undefined && (markStatus.value.get(questionId) || false)
}

// 事件处理
function setCurrentQuestion(index: number) {
  currentQuestionIndex.value = index
}

function previousQuestion() {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

function nextQuestion() {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

function handleAnswerChange(value: string) {
  currentAnswer.value = value
}

async function markQuestion() {
  if (!practiceDetail.value || !currentQuestion.value) return

  try {
    const isMarked = isCurrentQuestionMarked.value
    await runMarkQuestion({
      practiceSessionId: practiceDetail.value.practiceSessionId!,
      questionId: currentQuestion.value.questionId!,
      marked: !isMarked
    })

    markStatus.value.set(currentQuestion.value.questionId!, !isMarked)
    message.success(isMarked ? '取消标记成功' : '标记成功')
  } catch (error: any) {
    message.error(error.message || '标记失败')
  }
}

async function handleSubmit() {
  if (!practiceDetail.value) return

  // 检查是否有未答题目
  const unanswered = questions.value.filter(q => !answers.value.has(q.questionId!))
  if (unanswered.length > 0) {
    const confirmed = await new Promise(resolve => {
      message.warning(`还有 ${unanswered.length} 道题目未作答，确定要提交吗？`)
      // 这里应该显示确认对话框，简化处理
      resolve(true)
    })

    if (!confirmed) return
  }

  submitting.value = true
  try {
    const submitData: PracticeSubmitDTO = {
      practiceSessionId: practiceDetail.value!.practiceSessionId!,
      answers: questions.value.map(q => ({
        questionId: q.questionId!,
        answerContent: answers.value.get(q.questionId!) || '',
        markStatus: markStatus.value.get(q.questionId!) ? '已标记' : ''
      }))
    }

    emit('submit', submitData)
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('update:show', false)
}

// 监听会话ID变化
watch(
  () => props.sessionId,
  newId => {
    if (newId && props.show) {
      fetchPracticeDetail(newId)
    }
  },
  { immediate: true }
)

watch(
  () => props.show,
  show => {
    if (show && props.sessionId) {
      fetchPracticeDetail(props.sessionId)
    } else if (!show) {
      // 重置状态
      practiceDetail.value = null
      currentQuestionIndex.value = 0
      answers.value.clear()
      markStatus.value.clear()
    }
  }
)
</script>

<style scoped>
.practice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--n-border-color);
}

.practice-progress {
  margin-bottom: 24px;
}

.practice-content {
  display: flex;
  gap: 24px;
  height: 500px;
}

.question-nav {
  width: 200px;
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  padding: 16px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.nav-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--n-border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background-color: var(--n-color-hover);
}

.nav-item--active {
  background-color: var(--n-color-target);
  color: white;
}

.nav-item--answered {
  background-color: var(--n-success-color);
  color: white;
}

.nav-item--marked {
  border-color: var(--n-warning-color);
  border-width: 2px;
}

.question-area {
  flex: 1;
  border: 1px solid var(--n-border-color);
  border-radius: 6px;
  padding: 24px;
  overflow-y: auto;
}

.question-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--n-border-color);
}

.question-body {
  margin-bottom: 24px;
}

.question-title {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.question-type {
  margin-bottom: 16px;
}

.answer-area {
  margin: 20px 0;
}

.question-nav-buttons {
  padding-top: 16px;
  border-top: 1px solid var(--n-border-color);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}
</style>
