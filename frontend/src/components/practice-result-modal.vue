<template>
  <n-modal :show="props.show" preset="card" title="练习结果" @close="handleClose" style="width: 90vw; max-width: 1000px">
    <template #default>
      <div v-if="props.result">
        <!-- 成绩概览 -->
        <div class="result-overview">
          <n-card>
            <n-space size="large">
              <n-statistic label="总分" :value="props.result.totalScore" suffix="分" />
              <n-statistic label="正确率" :value="(props.result.correctRate * 100).toFixed(1) + '%'" />
              <n-statistic label="提交时间" :value="formatTime(props.result.submitTime)" />
            </n-space>
          </n-card>
        </div>

        <!-- 答题详情 -->
        <n-divider title-placement="left">答题详情</n-divider>
        <div class="answers-detail">
          <n-list>
            <n-list-item v-for="(answer, index) in props.result.answers" :key="answer.questionId">
              <n-thing>
                <template #header>
                  <n-space align="center">
                    <span>第 {{ index + 1 }} 题</span>
                    <n-tag :type="answer.correct ? 'success' : 'error'">
                      {{ answer.correct ? '正确' : '错误' }}
                    </n-tag>
                    <n-tag size="small" :type="getQuestionTypeColor(answer.questionType)">
                      {{ getQuestionTypeText(answer.questionType) }}
                    </n-tag>
                  </n-space>
                </template>
                <template #description>
                  <div class="answer-detail">
                    <div class="question-title">{{ answer.questionTitle }}</div>
                    <div class="answer-content"><strong>您的答案：</strong>{{ answer.answerContent || '未作答' }}</div>
                    <div class="score-info"><strong>得分：</strong>{{ answer.score }} 分</div>
                    <div v-if="answer.aiComment" class="ai-comment"><strong>AI评语：</strong>{{ answer.aiComment }}</div>
                  </div>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </div>
      </div>
    </template>

    <template #action>
      <n-space justify="end">
        <n-button @click="handleClose">关闭</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script lang="ts" setup>
import { defineEmits, defineProps } from 'vue'

import type { PracticeResultVO } from '@/api/types'

const props = defineProps<{
  show: boolean
  result: PracticeResultVO | null
  stats: { maxScore: number; minScore: number }
}>()

const emit = defineEmits(['update:show'])

// 工具函数
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

function formatTime(timeStr: string) {
  return new Date(timeStr).toLocaleString()
}

function handleClose() {
  emit('update:show', false)
}
</script>

<style scoped>
.result-overview {
  margin-bottom: 24px;
}

.answers-detail {
  margin-top: 16px;
}

.answer-detail {
  margin-top: 8px;
}

.question-title {
  font-weight: 500;
  margin-bottom: 8px;
}

.answer-content,
.score-info,
.ai-comment {
  margin: 4px 0;
  color: var(--n-text-color-2);
}
</style>
