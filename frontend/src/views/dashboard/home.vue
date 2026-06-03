<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <n-card class="welcome-card" :bordered="false">
        <div class="welcome-content">
          <div class="logo-section">
            <n-image src="/logo.svg" width="80" preview-disabled class="logo-img" />
          </div>
          <div class="welcome-text">
            <n-h1 class="welcome-title">{{ getGreeting }}，{{ userInfo.nickname }}！</n-h1>
            <n-p class="welcome-desc">今天是 {{ currentDate }}，开始您的智慧教学之旅</n-p>
            <div class="weather-info" v-if="weatherData">
              <icon type="materialWeatherSunny" class="weather-icon" />
              <span>{{ weatherData.city }} · {{ weatherData.weather }} · {{ weatherData.temperature }}°C</span>
            </div>
          </div>
          <div class="welcome-action">
            <n-button type="primary" size="large" @click="handleQuickStart">
              <template #icon>
                <icon type="materialPlayArrow" />
              </template>
              快速开始
            </n-button>
          </div>
        </div>
      </n-card>
    </div>

    <!-- 数据统计区域 -->
    <div class="stats-section">
      <n-h2 class="section-title">数据概览</n-h2>
      <n-grid :cols="4" :x-gap="20" :y-gap="20" responsive="screen">
        <n-grid-item v-for="stat in statsData" :key="stat.key">
          <n-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-icon" :class="`stat-icon-${stat.type}`">
                <icon :type="stat.icon" size="32" />
              </div>
              <div class="stat-info">
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-value">
                  <n-number-animation :from="0" :to="stat.value" :duration="1500" />
                  <span class="stat-unit">{{ stat.unit }}</span>
                </div>
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 图表分析区域 -->
    <div class="charts-section">
      <n-grid :cols="2" :x-gap="20" :y-gap="20" responsive="screen">
        <n-grid-item>
          <n-card title="题目生成趋势" :bordered="false" class="chart-card">
            <template #header-extra>
              <n-button-group size="small">
                <n-button @click="chartPeriod = '7d'" :type="chartPeriod === '7d' ? 'primary' : 'default'">7天</n-button>
                <n-button @click="chartPeriod = '30d'" :type="chartPeriod === '30d' ? 'primary' : 'default'">30天</n-button>
                <n-button @click="chartPeriod = '90d'" :type="chartPeriod === '90d' ? 'primary' : 'default'">90天</n-button>
              </n-button-group>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card title="题型分布" :bordered="false" class="chart-card">
            <div ref="pieChartRef" class="chart-container"></div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 快捷操作区域 -->
    <div class="actions-section">
      <n-h2 class="section-title">快捷操作</n-h2>
      <n-grid :cols="4" :x-gap="20" :y-gap="20" responsive="screen">
        <n-grid-item v-for="action in quickActions" :key="action.key">
          <n-card class="action-card" hoverable @click="handleQuickAction(action)">
            <div class="action-content">
              <div class="action-icon" :class="`action-${action.type}`">
                <icon :type="action.icon" size="24" />
              </div>
              <div class="action-text">
                <div class="action-title">{{ action.title }}</div>
                <div class="action-desc">{{ action.desc }}</div>
              </div>
              <div class="action-arrow">
                <icon type="materialArrowForward" />
              </div>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 最近活动区域 -->
    <div class="activity-section">
      <n-h2 class="section-title">最近活动</n-h2>
      <n-card :bordered="false" class="activity-card">
        <n-timeline>
          <n-timeline-item v-for="activity in recentActivities" :key="activity.id" :color="activity.color">
            <template #header>
              <span class="activity-time">{{ activity.time }}</span>
            </template>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-desc">{{ activity.desc }}</div>
            </div>
          </n-timeline-item>
        </n-timeline>
        <div class="activity-more">
          <n-button text @click="$router.push('/activities')">查看更多活动 →</n-button>
        </div>
      </n-card>
    </div>

    <!-- 系统状态区域 -->
    <div class="status-section">
      <n-h2 class="section-title">系统状态</n-h2>
      <n-grid :cols="3" :x-gap="20" responsive="screen">
        <n-grid-item v-for="status in systemStatus" :key="status.key">
          <n-card :bordered="false" class="status-card">
            <div class="status-header">
              <span class="status-label">{{ status.label }}</span>
              <n-tag :type="status.status === 'healthy' ? 'success' : status.status === 'warning' ? 'warning' : 'error'" size="small">
                {{ status.statusText }}
              </n-tag>
            </div>
            <n-progress :percentage="status.percentage" :color="status.color" :show-indicator="false" />
            <div class="status-detail">{{ status.detail }}</div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </div>
  </div>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import { useMessage, useThemeVars } from 'naive-ui'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { getEventLogs } from '@/api/event-log'
import { getDashboardDistribution, getDashboardStats, getDashboardTrend } from '@/api/statistics'
import { Icon } from '@/components'
import { useSystemStatus } from '@/composables/dashboard/useSystemStatus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

// 用户信息
const userInfo = computed(() => auth.user || { nickname: '用户' })

// 当前日期
const currentDate = computed(() => {
  const date = new Date()
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 根据时间返回问候语
const getGreeting = computed(() => {
  const hour = new Date().getHours()

  if (hour >= 6 && hour < 12) {
    return '上午好'
  } else if (hour >= 12 && hour < 14) {
    return '中午好'
  } else if (hour >= 14 && hour < 18) {
    return '下午好'
  } else {
    return '晚上好'
  }
})

// 天气信息（模拟数据）
const { weatherData, systemStatus, getNetworkInfo, startStatusUpdates, stopStatusUpdates } = useSystemStatus()

const statsData = ref<any[]>([])
const trendData = ref<{ categories: string[]; values: number[] }>({ categories: [], values: [] })
const pieData = ref<any[]>([])

// 快捷操作
const quickActions = ref([
  {
    key: 'generate',
    title: '智能生成题目',
    desc: '使用AI快速生成题目',
    type: 'primary',
    icon: 'materialAdd',
    route: '/question-generate'
  },
  {
    key: 'questions',
    title: '题目管理',
    desc: '管理和编辑题目库',
    type: 'info',
    icon: 'list',
    route: '/questions'
  },
  {
    key: 'knowledge',
    title: '知识点管理',
    desc: '管理课程知识点',
    type: 'success',
    icon: 'materialCategory',
    route: '/knowledge-points'
  },
  {
    key: 'courses',
    title: '课程管理',
    desc: '管理课程信息',
    type: 'warning',
    icon: 'materialSchool',
    route: '/courses'
  }
])

// 最近活动
const recentActivities = ref<any[]>([])

// 图表相关
const trendChartRef = ref()
const pieChartRef = ref()
const chartPeriod = ref('7d')
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 使用 Naive UI 的主题变量
const themeVars = useThemeVars()

// 获取主题色彩的辅助函数
const getThemeColors = () => {
  // 直接使用 Naive UI 提供的主题变量
  return {
    primaryColor: themeVars.value.primaryColor || '#18a058',
    infoColor: themeVars.value.infoColor || '#2080f0',
    warningColor: themeVars.value.warningColor || '#f0a020',
    errorColor: themeVars.value.errorColor || '#d03050',
    successColor: '#13c2c2', // 改为青色，替换原来的亮绿色
    purpleColor: '#722ed1', // 紫色
    textColor: themeVars.value.textColorBase || '#333333',
    textColorDisabled: themeVars.value.textColorDisabled || '#999999',
    borderColor: themeVars.value.borderColor || '#e0e0e6',
    dividerColor: themeVars.value.dividerColor || '#f0f0f0',
    popoverColor: themeVars.value.popoverColor || '#ffffff'
  }
}

// 如果 useThemeVars 不可用，使用备用方案
const getFallbackColors = () => {
  return {
    primaryColor: '#18a058',
    infoColor: '#2080f0',
    warningColor: '#f0a020',
    errorColor: '#d03050',
    successColor: '#13c2c2', // 改为青色
    purpleColor: '#722ed1',
    textColor: '#333333',
    textColorDisabled: '#999999',
    borderColor: '#e0e0e6',
    dividerColor: '#f0f0f0',
    popoverColor: '#ffffff'
  }
}

// 获取实际颜色的函数
const getActualColors = () => {
  try {
    // 首先尝试使用 themeVars
    if (themeVars.value && themeVars.value.primaryColor) {
      return getThemeColors()
    }

    // 然后尝试从 CSS 变量获取
    const computedStyle = getComputedStyle(document.documentElement)
    const primaryFromCSS = computedStyle.getPropertyValue('--n-color-primary')?.trim()

    if (primaryFromCSS) {
      return {
        primaryColor: primaryFromCSS || '#18a058',
        infoColor: computedStyle.getPropertyValue('--n-color-info')?.trim() || '#2080f0',
        warningColor: computedStyle.getPropertyValue('--n-color-warning')?.trim() || '#f0a020',
        errorColor: computedStyle.getPropertyValue('--n-color-error')?.trim() || '#d03050',
        successColor: '#13c2c2', // 改为青色
        purpleColor: '#722ed1',
        textColor: computedStyle.getPropertyValue('--n-text-color-base')?.trim() || '#333333',
        textColorDisabled: computedStyle.getPropertyValue('--n-text-color-disabled')?.trim() || '#999999',
        borderColor: computedStyle.getPropertyValue('--n-border-color')?.trim() || '#e0e0e6',
        dividerColor: computedStyle.getPropertyValue('--n-divider-color')?.trim() || '#f0f0f0',
        popoverColor: computedStyle.getPropertyValue('--n-popover-color')?.trim() || '#ffffff'
      }
    }

    // 最后使用备用颜色
    return getFallbackColors()
  } catch (error) {
    console.warn('获取主题颜色失败，使用备用颜色:', error)
    return getFallbackColors()
  }
}

// 窗口大小变化处理函数
const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

// 快速开始
const handleQuickStart = () => {
  router.push('/practices')
}

// 快捷操作点击
const handleQuickAction = (action: any) => {
  router.push(action.route)
}

const fetchDashboardStats = async () => {
  try {
    const res = await getDashboardStats()

    // 定义图标映射
    const iconMapping: Record<number, string> = {
      0: 'order', // 第一个统计项使用 order 图标
      1: 'materialSchool', // 第二个统计项使用 materialSchool 图标
      2: 'materialPeople', // 第三个统计项使用 materialPeople 图标
      3: 'materialCategory' // 第四个统计项使用 materialCategory 图标
    }

    statsData.value = (res.data || []).map((item: any, index: number) => ({
      key: item.key,
      label: item.label,
      value: item.value,
      unit: item.unit,
      type: item.key,
      icon: iconMapping[index] || 'order' // 根据索引分配图标，超出范围时使用默认图标
    }))
  } catch (e) {
    message.error('获取统计数据失败')
  }
}

const fetchTrendData = async () => {
  try {
    const res = await getDashboardTrend(chartPeriod.value)
    trendData.value = res.data || { categories: [], values: [] }
    updateTrendChart()
  } catch (e) {
    message.error('获取趋势数据失败')
  }
}

const fetchPieData = async () => {
  try {
    const res = await getDashboardDistribution()
    pieData.value = res.data || []
    updatePieChart()
  } catch (e) {
    message.error('获取题型分布失败')
  }
}

const fetchRecentActivities = async () => {
  try {
    const res = await getEventLogs({ page: 1, size: 4 })
    const colorArr = ['#18A058', '#2080F0', '#F0A020', '#D03050']
    if (res.data && res.data.content) {
      recentActivities.value = res.data.content.slice(0, 4).map((item: any, idx: number) => ({
        id: item.id,
        time: formatActivityTime(item.createTime),
        title: item.description || item.uri,
        desc: `${item.username} ${item.httpMethod} ${item.uri}`,
        color: colorArr[idx % colorArr.length]
      }))
    }
  } catch (e) {
    message.error('获取最近活动失败')
  }
}

function formatActivityTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  if (diffHours < 1) return '刚刚'
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  return date.toLocaleDateString('zh-CN')
}

const updateTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const colors = getActualColors()
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', lineStyle: { color: colors.textColor } },
      backgroundColor: colors.popoverColor,
      borderColor: colors.borderColor,
      textStyle: { color: colors.textColor }
    },
    grid: { left: '5%', right: '5%', top: '5%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: trendData.value.categories,
      axisLabel: { color: colors.textColor, fontSize: 12 },
      axisLine: { show: true, lineStyle: { color: colors.borderColor } },
      axisTick: { lineStyle: { color: colors.borderColor } }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: colors.textColor, fontSize: 12 },
      axisLine: { show: true, lineStyle: { color: colors.borderColor } },
      axisTick: { lineStyle: { color: colors.borderColor } },
      splitLine: { lineStyle: { color: colors.dividerColor } }
    },
    series: [
      {
        name: '题目生成数量',
        type: 'line',
        smooth: true,
        data: trendData.value.values,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${colors.primaryColor}80` },
            { offset: 1, color: `${colors.primaryColor}20` }
          ])
        },
        itemStyle: { color: colors.primaryColor },
        lineStyle: { width: 3, color: colors.primaryColor }
      }
    ]
  }
  trendChart.setOption(option)
}

const updatePieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const colors = getActualColors()

  // 定义6种不同的颜色
  const pieColors = [
    colors.primaryColor, // 绿色 #18a058
    colors.infoColor, // 蓝色 #2080f0
    colors.warningColor, // 橙色 #f0a020
    colors.errorColor, // 红色 #d03050
    colors.successColor, // 青色 #13c2c2 (替换了原来的亮绿色)
    colors.purpleColor // 紫色 #722ed1
  ]

  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: colors.popoverColor,
      borderColor: colors.borderColor,
      textStyle: { color: colors.textColor }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      textStyle: { color: colors.textColor, fontSize: 12 }
    },
    series: [
      {
        name: '题型分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['65%', '50%'],
        data: pieData.value.map((item, idx) => ({
          ...item,
          itemStyle: { color: pieColors[idx % pieColors.length] }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: { color: colors.textColor, fontSize: 12 },
        labelLine: { lineStyle: { color: colors.textColorDisabled } }
      }
    ]
  }
  pieChart.setOption(option)
}

// 生命周期钩子必须在 setup 顶层调用
onMounted(async () => {
  await nextTick()
  await fetchDashboardStats()
  await fetchTrendData()
  await fetchPieData()
  await fetchRecentActivities()
  setTimeout(() => {
    updateTrendChart()
    updatePieChart()
  }, 300)
  startStatusUpdates()
  window.addEventListener('resize', handleResize)
  window.addEventListener('online', getNetworkInfo)
  window.addEventListener('offline', getNetworkInfo)
})

// 组件卸载时的清理工作
onBeforeUnmount(() => {
  // 移除事件监听器
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('online', getNetworkInfo)
  window.removeEventListener('offline', getNetworkInfo)

  // 停止状态更新
  stopStatusUpdates()

  // 销毁图表实例
  trendChart?.dispose()
  pieChart?.dispose()
})

// 监听图表周期变化（如果需要）
watch(chartPeriod, () => {
  fetchTrendData()
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 32px;
}

.welcome-card {
  border-radius: 16px;
  overflow: hidden;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 32px;
}

.logo-section {
  flex-shrink: 0;
}

.logo-img {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  margin: 0 0 8px 0;
  font-size: 2.5rem;
  font-weight: 600;
  color: var(--n-text-color-base);
}

.welcome-desc {
  margin: 0 0 16px 0;
  font-size: 1.1rem;
  opacity: 0.9;
  color: var(--n-text-color-base);
}

.weather-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  opacity: 0.8;
  color: var(--n-text-color-base);
}

.weather-icon {
  color: #ffd700;
}

.welcome-action {
  flex-shrink: 0;
}

/* 统计卡片 */
.stats-section,
.charts-section,
.actions-section,
.activity-section,
.status-section {
  margin-bottom: 32px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--n-text-color-base);
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 统计图标样式 */
/* .stat-icon-primary {
  background: linear-gradient(135deg, #63e2b7, #70c0e8);
  color: white;
}

.stat-icon-info {
  background: linear-gradient(135deg, #70c0e8, #63e2b7);
  color: white;
}

.stat-icon-success {
  background: linear-gradient(135deg, #63e2b7, #52c41a);
  color: white;
}

.stat-icon-warning {
  background: linear-gradient(135deg, #f2c97d, #ffd93d);
  color: white;
} */

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--n-text-color-disabled);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 600;
  color: var(--n-text-color-base);
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-unit {
  font-size: 1rem;
  color: var(--n-text-color-disabled);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  margin-top: 4px;
}

.trend-up {
  color: #63e2b7;
}
.trend-down {
  color: #e88080;
}

/* 图表卡片 */
.chart-card {
  border-radius: 12px;
  min-height: 400px;
}

.chart-container {
  height: 300px;
  width: 100%;
  min-height: 250px;
}

/* 快捷操作 */
.action-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.action-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 快捷操作图标样式 */
/* .action-primary {
  background: linear-gradient(135deg, #63e2b7, #70c0e8);
  color: white;
}

.action-info {
  background: linear-gradient(135deg, #70c0e8, #63e2b7);
  color: white;
}

.action-success {
  background: linear-gradient(135deg, #63e2b7, #52c41a);
  color: white;
}

.action-warning {
  background: linear-gradient(135deg, #f2c97d, #ffd93d);
  color: white;
} */

.action-text {
  flex: 1;
}

.action-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--n-text-color-base);
  margin-bottom: 4px;
}

.action-desc {
  font-size: 0.9rem;
  color: var(--n-text-color-disabled);
}

.action-arrow {
  color: var(--n-text-color-disabled);
  transition: transform 0.3s ease;
}

.action-card:hover .action-arrow {
  transform: translateX(4px);
}

/* 活动区域 */
.activity-card {
  border-radius: 12px;
}

.activity-content {
  padding: 8px 0;
}

.activity-time {
  font-size: 0.85rem;
  color: var(--n-text-color-disabled);
}

.activity-title {
  font-weight: 600;
  color: var(--n-text-color-base);
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 0.9rem;
  color: var(--n-text-color-disabled);
}

.activity-more {
  text-align: center;
  padding: 16px 0 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.09);
  margin-top: 16px;
}

/* 系统状态 */
.status-card {
  border-radius: 12px;
  padding: 20px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-label {
  font-weight: 600;
  color: var(--n-text-color-base);
}

.status-detail {
  margin-top: 8px;
  font-size: 0.85rem;
  color: var(--n-text-color-disabled);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .chart-container {
    height: 280px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }

  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .welcome-title {
    font-size: 2rem;
  }

  .stat-content,
  .action-content {
    padding: 16px;
  }

  .chart-container {
    height: 250px;
  }

  .chart-card {
    min-height: 320px;
  }

  .stat-card:hover,
  .action-card:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  }
}

@media (max-width: 480px) {
  .chart-container {
    height: 200px;
  }

  .chart-card {
    min-height: 280px;
  }
}
</style>
