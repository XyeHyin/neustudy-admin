<template>
  <n-modal v-model:show="showModal" preset="card" style="width: 800px" title="试卷数据分析" :bordered="false">
    <div v-if="paperData" class="analysis-container">
      <!-- 统计信息卡片 -->
      <div class="stats-grid">
        <n-card size="small" class="stat-card">
          <n-statistic label="最高分" :value="paperData.maxScore || 0" suffix="分">
            <template #prefix>
              <n-icon color="#18a058">
                <svg viewBox="0 0 24 24">
                  <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="最低分" :value="paperData.minScore || 0" suffix="分">
            <template #prefix>
              <n-icon color="#d03050">
                <svg viewBox="0 0 24 24">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="平均分" :value="Math.round((paperData.avgScore || 0) * 100) / 100" suffix="分">
            <template #prefix>
              <n-icon color="#2080f0">
                <svg viewBox="0 0 24 24">
                  <path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-7H5v2h14V4zM3 2h18v2H3V2zm2 18h14v2H5v-2z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="尝试次数" :value="paperData.totalAttempts || 0" suffix="次">
            <template #prefix>
              <n-icon color="#f0a020">
                <svg viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="是否完成" :value="paperData.hasFullScore ? '满分' : '未满分'">
            <template #prefix>
              <n-icon :color="paperData.hasFullScore ? '#18a058' : '#d03050'">
                <svg viewBox="0 0 24 24">
                  <path v-if="paperData.hasFullScore" d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z" />
                  <path v-else d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="得分比" :value="scoreRatio" suffix="%">
            <template #prefix>
              <n-icon :color="getScoreRatioColor(scoreRatio)">
                <svg viewBox="0 0 24 24">
                  <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z" />
                </svg>
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </div>

      <!-- 满分标识 -->
      <div class="full-score-indicator" v-if="paperData.hasFullScore">
        <n-tag type="success" size="large">
          <template #icon>
            <n-icon>
              <svg viewBox="0 0 24 24">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
            </n-icon>
          </template>
          已获得满分
        </n-tag>
      </div>

      <!-- 得分趋势图表 -->
      <n-card size="small" style="margin-top: 16px">
        <template #header>
          <span>得分趋势</span>
        </template>
        <div ref="chartRef" style="height: 300px"></div>
      </n-card>
    </div>

    <div v-else class="loading-container">
      <n-spin size="large">
        <template #description>加载中...</template>
      </n-spin>
    </div>
  </n-modal>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import { NCard, NIcon, NModal, NSpin, NStatistic, NTag } from 'naive-ui'
// 组件卸载时清理
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'

import type { PracticePaperStatVO } from '@/api/types'

interface Props {
  show: boolean
  paperData: PracticePaperStatVO | null
}

interface Emits {
  (e: 'update:show', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const showModal = ref(false)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 计算得分比（最高分/总分的百分比）
const scoreRatio = computed(() => {
  if (!props.paperData || !props.paperData.totalScore || props.paperData.totalScore === 0) {
    return 0
  }
  const ratio = ((props.paperData.maxScore || 0) / props.paperData.totalScore) * 100
  return Math.round(ratio * 100) / 100 // 保留两位小数
})

// 根据得分比返回颜色
function getScoreRatioColor(ratio: number): string {
  if (ratio >= 90) return '#18a058' // 绿色 - 优秀
  if (ratio >= 80) return '#2080f0' // 蓝色 - 良好
  if (ratio >= 70) return '#f0a020' // 橙色 - 中等
  if (ratio >= 60) return '#f7ba1e' // 黄色 - 及格
  return '#d03050' // 红色 - 不及格
}

// 同步显示状态
watch(
  () => props.show,
  newVal => {
    showModal.value = newVal
  }
)

watch(showModal, newVal => {
  emit('update:show', newVal)
})

// 监听数据变化，更新图表
watch(
  () => props.paperData,
  async newData => {
    if (newData && showModal.value) {
      await nextTick()
      initChart()
    }
  },
  { immediate: true }
)

// 监听模态框显示状态
watch(showModal, async newVal => {
  if (newVal && props.paperData) {
    await nextTick()
    initChart()
  } else if (!newVal && chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

function initChart() {
  if (!chartRef.value || !props.paperData) return

  // 销毁现有图表实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const scoreData = props.paperData.scoreTrend || []
  const xAxisData = scoreData.map((_, index) => `第${index + 1}次`)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex
        return `${params[0].axisValue}<br/>得分: ${scoreData[dataIndex]}分`
      }
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '得分',
      min: 0,
      max: props.paperData.totalScore || 100
    },
    series: [
      {
        name: '得分',
        type: 'line',
        data: scoreData,
        smooth: true,
        lineStyle: {
          color: '#2080f0',
          width: 3
        },
        itemStyle: {
          color: '#2080f0',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(32, 128, 240, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(32, 128, 240, 0.1)'
              }
            ]
          }
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高分' },
            { type: 'min', name: '最低分' }
          ],
          itemStyle: {
            color: '#18a058'
          }
        },
        markLine: {
          data: [
            {
              type: 'average',
              name: '平均分',
              lineStyle: {
                color: '#f0a020',
                type: 'dashed'
              }
            }
          ]
        }
      }
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    }
  }

  chartInstance.setOption(option)

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.analysis-container {
  padding: 16px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.full-score-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}
</style>
