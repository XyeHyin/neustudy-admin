<template>
  <n-modal :show="show" preset="dialog" title="练习详情" @close="handleClose" style="width: 900px">
    <div v-if="record" style="padding: 16px 24px">
      <!-- 基本信息 -->
      <n-descriptions :column="2" bordered>
        <n-descriptions-item label="试卷标题">{{ record.paperTitle }}</n-descriptions-item>
        <n-descriptions-item label="得分">{{ record.totalScore }} 分</n-descriptions-item>
        <n-descriptions-item label="正确率">{{ (record.correctRate * 100).toFixed(1) }}%</n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="record.submitted ? 'success' : 'warning'">
            {{ record.submitted ? '已提交' : '未提交' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="开始时间">{{ formatDateTime(record.startTime) }}</n-descriptions-item>
        <n-descriptions-item label="提交时间">
          {{ formatDateTime(record.submitTime) }}
        </n-descriptions-item>
      </n-descriptions>

      <!-- 统计信息 -->
      <n-divider v-permission="'practice:history:self'" title-placement="left">练习统计</n-divider>
      <n-descriptions v-permission="'practice:history:self'" :column="3" bordered>
        <n-descriptions-item label="最高分">{{ paperStats?.maxScore }} 分</n-descriptions-item>
        <n-descriptions-item label="最低分">{{ paperStats?.minScore }} 分</n-descriptions-item>
        <n-descriptions-item label="平均分">{{ paperStats?.avgScore }} 分</n-descriptions-item>
        <n-descriptions-item label="练习次数">第 {{ paperStats?.totalAttempts }} 次</n-descriptions-item>
      </n-descriptions>

      <!-- 操作按钮 -->
      <div style="display: flex; justify-content: center; gap: 16px; margin-top: 24px">
        <n-button v-if="record.submitted" type="primary" @click="handleViewResult"> 查看答题结果 </n-button>
        <n-button @click="handleClose">关闭</n-button>
      </div>
    </div>
  </n-modal>

  <!-- 练习结果模态框 -->
  <practice-result-modal :show="showResultModal" :result="practiceResult" :stats="stats" @update:show="updateResultModalShow" />
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import {
  getAvailablePapersWithStats,
  getPracticeHistory,
  getPracticeResult
} from '@/api/practice'
import PracticeResultModal from '@/components/practice/practice-result-modal.vue'
import { useAuthStore } from '@/store/auth'
import { formatDateTime } from '@/utils/datetime'

import type { PracticePaperStatVO, PracticeRecordVO, PracticeResultVO } from '@/api/types'

const props = defineProps<{
  show: boolean
  record: PracticeRecordVO | null
}>()

const emit = defineEmits(['update:show'])

const message = useMessage()

// 统计数据
const stats = reactive({
  maxScore: 0,
  minScore: 0,
  avgScore: 0,
  totalAttempts: 0
})

const { runAsync: fetchStats } = useRequest((studentId: number) => getPracticeHistory(studentId), { manual: true })

const auth = useAuthStore() // 获取当前用户信息

const practiceResult = ref<PracticeResultVO | null>(null)
const showResultModal = ref(false)
const { runAsync: fetchPracticeResult } = useRequest(getPracticeResult, { manual: true })

function handleClose() {
  emit('update:show', false)
}

async function handleViewResult() {
  if (!props.record) return

  try {
    const res = await fetchPracticeResult(props.record.practiceSessionId)
    if (res.code === 200) {
      practiceResult.value = res.data
      showResultModal.value = true
    } else {
      message.error(res.message || '获取练习结果失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取练习结果失败')
  }
}

function updateResultModalShow(show: boolean) {
  showResultModal.value = show
}

// 用于存放单个试卷的统计信息
const paperStats = ref<PracticePaperStatVO | null>(null)

// 手动触发获取所有可用试卷统计列表
const { runAsync: fetchAllPaperStats } = useRequest(getAvailablePapersWithStats, { manual: true })

// 当 record.paperId 变化时，取出对应的统计信息
watch(
  () => props.record?.paperId,
  async paperId => {
    if (!paperId) return
    try {
      const res = await fetchAllPaperStats()
      if (res.code === 200) {
        paperStats.value = res.data.find(item => item.paperId === paperId) || null
      } else {
        message.error(res.message || '获取可用试卷统计失败')
      }
    } catch (err: any) {
      message.error(err.message || '获取可用试卷统计失败')
    }
  },
  { immediate: true }
)
</script>

