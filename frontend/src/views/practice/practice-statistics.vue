<template>
  <div class="practice-statistics-page">
    <n-card :bordered="false" class="overview-card" title="练习统计概览">
      <n-grid :cols="4" :x-gap="16">
        <n-grid-item>
          <n-statistic label="总练习次数" :value="statistics.totalPractices || 0" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="完成次数" :value="statistics.completedPractices || 0" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="及格次数" :value="statistics.passedPractices || 0" />
        </n-grid-item>
        <n-grid-item>
          <n-statistic label="平均得分" :value="statistics.averageScore || 0" suffix="分" />
        </n-grid-item>
      </n-grid>
    </n-card>
    <!-- 详细统计 -->
    <n-grid :cols="2" :x-gap="16" style="margin-top: 16px">
      <!-- 按难度统计 -->
      <n-grid-item>
        <n-card title="练习试卷统计">
          <n-grid :cols="3" :x-gap="16">
            <n-grid-item>
              <n-statistic label="总试卷数" :value="statistics.totalPapers || 0" />
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="已练习" :value="statistics.practicedPapers || 0" />
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="获得满分" :value="statistics.fullScorePapers || 0" />
            </n-grid-item>
          </n-grid>
        </n-card>
      </n-grid-item>

      <!-- 成绩分布 -->
      <n-grid-item>
        <n-card title="成绩分布">
          <n-grid :cols="3" :x-gap="16">
            <n-grid-item>
              <n-statistic label="优秀(90+)" :value="statistics.excellentCount || 0" />
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="良好(80-89)" :value="statistics.goodCount || 0" />
            </n-grid-item>
            <n-grid-item>
              <n-statistic label="及格(60-79)" :value="statistics.passCount || 0" />
            </n-grid-item>
          </n-grid>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 试卷练习详情 -->
    <n-card :bordered="false" class="course-statistics" title="试卷练习详情" style="margin-top: 16px">
      <n-data-table :columns="paperColumns" :data="paperStatistics" :pagination="false" :bordered="false" :loading="loadingStatistics" />
    </n-card>

    <!-- 近期练习记录 -->
    <n-card :bordered="false" class="recent-records" title="近期练习记录" style="margin-top: 16px">
      <n-data-table :columns="recordColumns" :data="recentRecords" :pagination="{ pageSize: 5 }" :bordered="false" />
      <div style="text-align: center; margin-top: 16px">
        <n-button @click="goToAllRecords">查看全部记录</n-button>
      </div>
    </n-card>

  </div>
</template>

<script lang="ts" setup>
import { NButton, NTag, useMessage } from 'naive-ui'
import { computed, h, nextTick, onMounted, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import { useRouter } from 'vue-router'

import { getAvailablePapersWithStats, listPracticeRecords } from '@/api/practice'
import { useAuthStore } from '@/store/auth'

import type { PracticePaperStatVO, PracticeRecordVO } from '@/api/types'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

// 数据
const statistics = ref<Record<string, any>>({})
const paperStatistics = ref<PracticePaperStatVO[]>([])
const recentRecords = ref<PracticeRecordVO[]>([])

// 图表引用
const scoreChartRef = ref<HTMLDivElement>()
const trendChartRef = ref<HTMLDivElement>()
const chartContainer = ref<HTMLElement>()
// 试卷统计表格列
const paperColumns = computed(() => [
  {
    title: '试卷名称',
    key: 'paperTitle',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '教师',
    key: 'teacherName',
    width: 120
  },
  {
    title: '练习次数',
    key: 'totalAttempts',
    width: 100,
    render: (row: PracticePaperStatVO) => row.totalAttempts || 0
  },
  {
    title: '最高分',
    key: 'maxScore',
    width: 100,
    render: (row: PracticePaperStatVO) => `${row.maxScore || 0}分`
  },
  {
    title: '平均分',
    key: 'avgScore',
    width: 100,
    render: (row: PracticePaperStatVO) => `${(row.avgScore || 0).toFixed(1)}分`
  },
  {
    title: '是否满分',
    key: 'hasFullScore',
    width: 100,
    render: (row: PracticePaperStatVO) => h(NTag, { type: row.hasFullScore ? 'success' : 'default' }, { default: () => (row.hasFullScore ? '已满分' : '未满分') })
  }
])

// 练习记录表格列
const recordColumns = computed(() => [
  {
    title: '试卷标题',
    key: 'paperTitle',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '得分',
    key: 'totalScore',
    width: 120,
    render: (row: PracticeRecordVO) => `${row.totalScore || 0}分`
  },
  {
    title: '是否及格',
    key: 'isPassed',
    width: 100,
    render: (row: PracticeRecordVO) => {
      const score = row.totalScore || 0
      const totalScore = 100 // 假设总分为100，实际应该从试卷信息获取
      const isPassed = (score / totalScore) >= 0.6
      return h(NTag, { type: isPassed ? 'success' : 'error' }, { default: () => isPassed ? '及格' : '不及格' })
    }
  },
  {
    title: '练习时间',
    key: 'startTime',
    width: 160,
    render: (row: PracticeRecordVO) => new Date(row.startTime).toLocaleString()
  }
])

// API请求
const { loading: loadingStatistics, run: fetchStatistics } = useRequest(getAvailablePapersWithStats, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      paperStatistics.value = res.data || []
      calculateStatistics(res.data || [])
    } else {
      message.error(res.message || '获取统计数据失败')
    }
  }
})

const { run: fetchRecentRecords } = useRequest(() => listPracticeRecords({ page: 1, size: 5 }), {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      recentRecords.value = res.data.content || []
    }
  }
})




// 计算统计数据
function calculateStatistics(papers: PracticePaperStatVO[]) {
  const stats = {
    totalPapers: papers.length,
    practicedPapers: papers.filter(p => (p.totalAttempts || 0) > 0).length,
    fullScorePapers: papers.filter(p => p.hasFullScore).length,
    totalPractices: papers.reduce((sum, p) => sum + (p.totalAttempts || 0), 0),
    completedPractices: papers.filter(p => (p.totalAttempts || 0) > 0).length,
    passedPractices: 0,
    averageScore: 0,
    excellentCount: 0,
    goodCount: 0,
    passCount: 0
  }

  // 计算成绩分布和平均分
  const allMaxScores = papers
    .filter(p => (p.maxScore || 0) > 0)
    .map(p => ({
      maxScore: p.maxScore || 0,
      totalScore: p.totalScore || 100
    }))

  if (allMaxScores.length > 0) {
    const totalScoreSum = allMaxScores.reduce((sum, item) => sum + item.maxScore, 0)
    stats.averageScore = Math.round((totalScoreSum / allMaxScores.length) * 100) / 100

    // 计算成绩分布（基于得分率）
    allMaxScores.forEach(item => {
      const scoreRate = (item.maxScore / item.totalScore) * 100
      if (scoreRate >= 90) {
        stats.excellentCount++
      } else if (scoreRate >= 80) {
        stats.goodCount++
      } else if (scoreRate >= 60) {
        stats.passCount++
      }
    })

    stats.passedPractices = stats.excellentCount + stats.goodCount + stats.passCount
  }

  statistics.value = stats
}

onMounted(() => {
  fetchStatistics()
  fetchRecentRecords()
})

// 跳转到全部练习记录页面
function goToAllRecords() {
  router.push('/records')
}
</script>

<style scoped>
.practice-statistics-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.overview-card {
  margin-bottom: 16px;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.course-statistics,
.recent-records {
  margin-top: 16px;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.n-card[style] {
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}
</style>
