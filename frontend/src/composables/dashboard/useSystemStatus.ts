import { ref } from 'vue'

type HealthStatus = 'healthy' | 'warning' | 'error'

interface SystemStatusItem {
  key: string
  label: string
  percentage: number
  status: HealthStatus
  statusText: string
  color: string
  detail: string
}

interface BrowserConnection {
  downlink?: number
  effectiveType?: string
}

interface BrowserBattery {
  level: number
  charging: boolean
}

interface NavigatorWithStatus extends Navigator {
  connection?: BrowserConnection
  mozConnection?: BrowserConnection
  webkitConnection?: BrowserConnection
  getBattery?: () => Promise<BrowserBattery>
}

interface PerformanceWithMemory extends Performance {
  memory?: {
    usedJSHeapSize: number
    totalJSHeapSize: number
    jsHeapSizeLimit: number
  }
}

export function useSystemStatus() {
  const weatherData = ref({
    city: '大连',
    weather: '晴',
    temperature: 22
  })

  const systemStatus = ref<SystemStatusItem[]>([
    {
      key: 'memory',
      label: '内存使用率',
      percentage: 0,
      status: 'healthy',
      statusText: '正常',
      color: '#18a058',
      detail: '正在获取...'
    },
    {
      key: 'network',
      label: '网络状态',
      percentage: 100,
      status: 'healthy',
      statusText: '正常',
      color: '#18a058',
      detail: '连接正常'
    }
  ])

  const getMemoryInfo = async () => {
    try {
      const browserPerformance = performance as PerformanceWithMemory
      if (browserPerformance.memory) {
        const memory = browserPerformance.memory
        const used = memory.usedJSHeapSize
        const total = memory.totalJSHeapSize

        const percentage = Math.round((used / total) * 100)
        const usedMB = Math.round(used / 1048576)
        const totalMB = Math.round(total / 1048576)

        const memoryStatus = systemStatus.value.find(status => status.key === 'memory')
        if (memoryStatus) {
          memoryStatus.percentage = percentage
          memoryStatus.detail = `已使用 ${usedMB}MB / ${totalMB}MB`
          memoryStatus.status = percentage > 80 ? 'error' : percentage > 60 ? 'warning' : 'healthy'
          memoryStatus.statusText = percentage > 80 ? '危险' : percentage > 60 ? '注意' : '正常'
          memoryStatus.color = percentage > 80 ? '#d03050' : percentage > 60 ? '#f0a020' : '#18a058'
        }
      }
    } catch (error) {
      console.warn('获取内存信息失败:', error)
    }
  }

  const getNetworkInfo = () => {
    try {
      const browserNavigator = navigator as NavigatorWithStatus
      const connection =
        browserNavigator.connection || browserNavigator.mozConnection || browserNavigator.webkitConnection
      const networkStatus = systemStatus.value.find(status => status.key === 'network')

      if (networkStatus) {
        if (navigator.onLine) {
          networkStatus.percentage = 100
          networkStatus.status = 'healthy'
          networkStatus.statusText = '在线'
          networkStatus.color = '#18a058'

          if (connection) {
            const speed = connection.downlink || 0
            const type = connection.effectiveType || 'unknown'
            networkStatus.detail = `${type.toUpperCase()} - ${speed}Mbps`
          } else {
            networkStatus.detail = '网络连接正常'
          }
        } else {
          networkStatus.percentage = 0
          networkStatus.status = 'error'
          networkStatus.statusText = '离线'
          networkStatus.color = '#d03050'
          networkStatus.detail = '网络连接断开'
        }
      }
    } catch (error) {
      console.warn('获取网络信息失败:', error)
    }
  }

  const getBatteryInfo = async () => {
    try {
      const browserNavigator = navigator as NavigatorWithStatus
      if (browserNavigator.getBattery) {
        const battery = await browserNavigator.getBattery()

        const batteryStatus: SystemStatusItem = {
          key: 'battery',
          label: '电池电量',
          percentage: Math.round(battery.level * 100),
          status: battery.level > 0.2 ? 'healthy' : 'warning',
          statusText: battery.charging ? '充电中' : battery.level > 0.2 ? '正常' : '低电量',
          color: battery.level > 0.2 ? '#18a058' : '#f0a020',
          detail: `${Math.round(battery.level * 100)}% ${battery.charging ? '(充电中)' : ''}`
        }

        const existingBatteryIndex = systemStatus.value.findIndex(status => status.key === 'battery')
        if (existingBatteryIndex >= 0) {
          systemStatus.value[existingBatteryIndex] = batteryStatus
        } else {
          systemStatus.value.push(batteryStatus)
        }
      }
    } catch (error) {
      console.warn('获取电池信息失败:', error)
    }
  }

  const initSystemStatus = async () => {
    await Promise.all([getMemoryInfo(), getBatteryInfo()])
    getNetworkInfo()
  }

  let statusUpdateTimer: number | null = null

  const startStatusUpdates = () => {
    initSystemStatus()

    statusUpdateTimer = window.setInterval(() => {
      initSystemStatus()
    }, 30000)
  }

  const stopStatusUpdates = () => {
    if (statusUpdateTimer) {
      window.clearInterval(statusUpdateTimer)
      statusUpdateTimer = null
    }
  }

  return {
    weatherData,
    systemStatus,
    getNetworkInfo,
    startStatusUpdates,
    stopStatusUpdates
  }
}
