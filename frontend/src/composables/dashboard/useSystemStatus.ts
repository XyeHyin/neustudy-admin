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
  rtt?: number
  saveData?: boolean
  addEventListener?: (type: string, listener: EventListenerOrEventListenerObject) => void
  removeEventListener?: (type: string, listener: EventListenerOrEventListenerObject) => void
}

interface BrowserBattery {
  level: number
  charging: boolean
  addEventListener?: (type: string, listener: EventListenerOrEventListenerObject) => void
  removeEventListener?: (type: string, listener: EventListenerOrEventListenerObject) => void
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

const HEALTHY_COLOR = '#18a058'
const WARNING_COLOR = '#f0a020'
const ERROR_COLOR = '#d03050'

function getStatusItem(items: SystemStatusItem[], key: string) {
  return items.find(status => status.key === key)
}

function setUnsupported(status: SystemStatusItem, detail: string) {
  status.percentage = 0
  status.status = 'warning'
  status.statusText = '不可用'
  status.color = WARNING_COLOR
  status.detail = detail
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
      label: 'JS 内存',
      percentage: 0,
      status: 'warning',
      statusText: '获取中',
      color: WARNING_COLOR,
      detail: '正在读取浏览器内存信息'
    },
    {
      key: 'network',
      label: '网络状态',
      percentage: navigator.onLine ? 100 : 0,
      status: navigator.onLine ? 'healthy' : 'error',
      statusText: navigator.onLine ? '在线' : '离线',
      color: navigator.onLine ? HEALTHY_COLOR : ERROR_COLOR,
      detail: navigator.onLine ? '浏览器报告网络在线' : '浏览器报告网络离线'
    },
    {
      key: 'battery',
      label: '电池电量',
      percentage: 0,
      status: 'warning',
      statusText: '获取中',
      color: WARNING_COLOR,
      detail: '正在读取电池信息'
    }
  ])

  const getMemoryInfo = () => {
    const memoryStatus = getStatusItem(systemStatus.value, 'memory')
    if (!memoryStatus) return

    try {
      const browserPerformance = performance as PerformanceWithMemory
      if (!browserPerformance.memory) {
        setUnsupported(memoryStatus, '当前浏览器不提供 JS Heap 内存信息')
        return
      }

      const { usedJSHeapSize, jsHeapSizeLimit } = browserPerformance.memory
      const percentage = Math.round((usedJSHeapSize / jsHeapSizeLimit) * 100)
      const usedMB = Math.round(usedJSHeapSize / 1048576)
      const limitMB = Math.round(jsHeapSizeLimit / 1048576)

      memoryStatus.percentage = percentage
      memoryStatus.detail = `JS Heap 已用 ${usedMB}MB / 上限 ${limitMB}MB`
      memoryStatus.status = percentage > 80 ? 'error' : percentage > 60 ? 'warning' : 'healthy'
      memoryStatus.statusText = percentage > 80 ? '偏高' : percentage > 60 ? '注意' : '正常'
      memoryStatus.color = percentage > 80 ? ERROR_COLOR : percentage > 60 ? WARNING_COLOR : HEALTHY_COLOR
    } catch (error) {
      console.warn('获取内存信息失败:', error)
      setUnsupported(memoryStatus, '读取内存信息失败')
    }
  }

  const getNetworkInfo = () => {
    const networkStatus = getStatusItem(systemStatus.value, 'network')
    if (!networkStatus) return

    try {
      const browserNavigator = navigator as NavigatorWithStatus
      const connection =
        browserNavigator.connection || browserNavigator.mozConnection || browserNavigator.webkitConnection

      if (!navigator.onLine) {
        networkStatus.percentage = 0
        networkStatus.status = 'error'
        networkStatus.statusText = '离线'
        networkStatus.color = ERROR_COLOR
        networkStatus.detail = '浏览器报告网络连接断开'
        return
      }

      networkStatus.percentage = 100
      networkStatus.status = 'healthy'
      networkStatus.statusText = '在线'
      networkStatus.color = HEALTHY_COLOR

      if (!connection) {
        networkStatus.detail = '浏览器报告网络在线，当前浏览器不提供带宽信息'
        return
      }

      const parts = [
        connection.effectiveType ? `类型 ${connection.effectiveType.toUpperCase()}` : null,
        typeof connection.downlink === 'number' ? `下行约 ${connection.downlink}Mbps` : null,
        typeof connection.rtt === 'number' ? `延迟约 ${connection.rtt}ms` : null,
        connection.saveData ? '省流量模式' : null
      ].filter(Boolean)

      networkStatus.detail = parts.length ? parts.join(' · ') : '浏览器报告网络在线'
    } catch (error) {
      console.warn('获取网络信息失败:', error)
      networkStatus.percentage = navigator.onLine ? 100 : 0
      networkStatus.status = navigator.onLine ? 'warning' : 'error'
      networkStatus.statusText = navigator.onLine ? '在线' : '离线'
      networkStatus.color = navigator.onLine ? WARNING_COLOR : ERROR_COLOR
      networkStatus.detail = '读取网络详细信息失败'
    }
  }

  let batteryManager: BrowserBattery | null = null

  const updateBatteryStatus = (battery: BrowserBattery) => {
    const batteryStatus = getStatusItem(systemStatus.value, 'battery')
    if (!batteryStatus) return

    const percentage = Math.round(battery.level * 100)
    batteryStatus.percentage = percentage
    batteryStatus.status = percentage <= 15 && !battery.charging ? 'error' : percentage <= 30 && !battery.charging ? 'warning' : 'healthy'
    batteryStatus.statusText = battery.charging ? '充电中' : percentage <= 15 ? '电量低' : '正常'
    batteryStatus.color = batteryStatus.status === 'error' ? ERROR_COLOR : batteryStatus.status === 'warning' ? WARNING_COLOR : HEALTHY_COLOR
    batteryStatus.detail = `${percentage}%${battery.charging ? ' · 正在充电' : ''}`
  }

  const getBatteryInfo = async () => {
    const batteryStatus = getStatusItem(systemStatus.value, 'battery')
    if (!batteryStatus) return

    try {
      const browserNavigator = navigator as NavigatorWithStatus
      if (!browserNavigator.getBattery) {
        setUnsupported(batteryStatus, '当前浏览器不支持 Battery Status API')
        return
      }

      batteryManager = await browserNavigator.getBattery()
      updateBatteryStatus(batteryManager)
    } catch (error) {
      console.warn('获取电池信息失败:', error)
      setUnsupported(batteryStatus, '读取电池信息失败')
    }
  }

  const initSystemStatus = async () => {
    getMemoryInfo()
    getNetworkInfo()
    await getBatteryInfo()
  }

  let statusUpdateTimer: number | null = null

  const startStatusUpdates = () => {
    initSystemStatus()

    const browserNavigator = navigator as NavigatorWithStatus
    const connection =
      browserNavigator.connection || browserNavigator.mozConnection || browserNavigator.webkitConnection
    connection?.addEventListener?.('change', getNetworkInfo)

    statusUpdateTimer = window.setInterval(() => {
      getMemoryInfo()
      getNetworkInfo()
      if (batteryManager) {
        updateBatteryStatus(batteryManager)
      }
    }, 30000)
  }

  const stopStatusUpdates = () => {
    if (statusUpdateTimer) {
      window.clearInterval(statusUpdateTimer)
      statusUpdateTimer = null
    }

    const browserNavigator = navigator as NavigatorWithStatus
    const connection =
      browserNavigator.connection || browserNavigator.mozConnection || browserNavigator.webkitConnection
    connection?.removeEventListener?.('change', getNetworkInfo)
  }

  return {
    weatherData,
    systemStatus,
    getNetworkInfo,
    startStatusUpdates,
    stopStatusUpdates
  }
}
