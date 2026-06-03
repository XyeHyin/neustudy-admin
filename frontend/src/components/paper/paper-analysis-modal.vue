<template>
  <n-modal v-model:show="showModal" preset="card" style="width: 800px" title="试卷数据分析" :bordered="false">
    <div v-if="paperData" class="analysis-container">
      <!-- 统计信息卡片 -->
      <div class="stats-grid">
        <n-card size="small" class="stat-card">
          <n-statistic label="最高分" :value="paperData.maxScore || 0" suffix="分">
            <template #prefix>
              <Icon type="materialStar" color="#18a058" />
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="最低分" :value="paperData.minScore || 0" suffix="分">
            <template #prefix>
              <Icon type="materialDocument" color="#d03050" />
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="平均分" :value="Math.round((paperData.avgScore || 0) * 100) / 100" suffix="分">
            <template #prefix>
              <Icon type="materialBarChart" color="#2080f0" />
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="尝试次数" :value="paperData.totalAttempts || 0" suffix="次">
            <template #prefix>
              <Icon type="materialCheckCircle" color="#f0a020" />
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="是否完成" :value="paperData.hasFullScore ? '满分' : '未满分'">
            <template #prefix>
              <Icon :type="paperData.hasFullScore ? 'materialCheckCircle' : 'materialClose'" :color="paperData.hasFullScore ? '#18a058' : '#d03050'" />
            </template>
          </n-statistic>
        </n-card>

        <n-card size="small" class="stat-card">
          <n-statistic label="得分比" :value="scoreRatio" suffix="%">
            <template #prefix>
              <Icon type="materialTrendingUp" :color="getScoreRatioColor(scoreRatio)" />
            </template>
          </n-statistic>
        </n-card>
      </div>

      <!-- 满分标识 -->
      <div class="full-score-indicator" v-if="paperData.hasFullScore">
        <n-tag type="success" size="large">
          <template #icon>
            <Icon type="materialStar" />
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
import { NCard, NModal, NSpin, NStatistic, NTag } from 'naive-ui'
// 组件卸载时清理
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'

import { Icon } from '@/components'

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
