import { computed, onBeforeUnmount, ref, watch } from 'vue'

import {
  getNotificationSummary,
  markNotificationRead,
  markAllNotificationsRead,
  type NotificationItem
} from '@/api/notification'
import { isJwtToken, useAuthStore } from '@/store/auth'

type SseMessage = {
  event: string
  data: string
}

const FALLBACK_POLL_INTERVAL = 15000

function getStreamUrl() {
  const baseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  return `${baseUrl}/notifications/stream`
}

function parseSseBlock(block: string): SseMessage | null {
  const lines = block.split(/\r?\n/)
  let event = 'message'
  const data: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      data.push(line.slice(5).trimStart())
    }
  }

  if (!data.length) {
    return null
  }
  return { event, data: data.join('\n') }
}

export function useNotifications(limit = 8) {
  const auth = useAuthStore()
  const notifications = ref<NotificationItem[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const streamConnected = ref(false)
  const streamError = ref(false)

  let abortController: AbortController | null = null
  let reconnectTimer: ReturnType<typeof window.setTimeout> | null = null
  let pollTimer: ReturnType<typeof window.setInterval> | null = null
  let stopped = false

  const hasUnread = computed(() => unreadCount.value > 0)

  async function loadNotifications(nextLimit = limit) {
    if (!isJwtToken(auth.token)) {
      notifications.value = []
      unreadCount.value = 0
      return
    }

    loading.value = true
    try {
      const res = await getNotificationSummary({ limit: nextLimit })
      notifications.value = res.data.notifications
      unreadCount.value = res.data.unreadCount
    } finally {
      loading.value = false
    }
  }

  async function markRead(eventLogId: number) {
    if (!isJwtToken(auth.token)) {
      return
    }

    const target = notifications.value.find(item => item.eventLogId === eventLogId)
    const wasUnread = target && !target.read
    if (target) {
      target.read = true
    }
    if (wasUnread) {
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }

    try {
      const res = await markNotificationRead(eventLogId, { limit })
      notifications.value = res.data.notifications
      unreadCount.value = res.data.unreadCount
    } catch (error) {
      if (target && wasUnread) {
        target.read = false
        unreadCount.value += 1
      }
      throw error
    }
  }

  async function markAllRead() {
    if (!isJwtToken(auth.token) || unreadCount.value === 0) {
      return
    }

    const res = await markAllNotificationsRead({ limit })
    notifications.value = res.data.notifications
    unreadCount.value = res.data.unreadCount
  }

  function upsertNotification(notification: NotificationItem) {
    const exists = notifications.value.some(item => item.eventLogId === notification.eventLogId)
    if (!exists) {
      notifications.value = [notification, ...notifications.value].slice(0, limit)
      if (!notification.read) {
        unreadCount.value += 1
      }
    }
  }

  function clearReconnectTimer() {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  function clearPollTimer() {
    if (pollTimer) {
      window.clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function startFallbackPolling() {
    clearPollTimer()
    pollTimer = window.setInterval(() => {
      if (stopped || !isJwtToken(auth.token)) return
      if (!streamConnected.value || streamError.value) {
        void loadNotifications().catch(() => undefined)
      }
    }, FALLBACK_POLL_INTERVAL)
  }

  function scheduleReconnect() {
    if (stopped || reconnectTimer || !isJwtToken(auth.token)) {
      return
    }
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      void connectStream()
    }, 5000)
  }

  async function connectStream() {
    if (!isJwtToken(auth.token) || abortController) {
      return
    }

    abortController = new AbortController()
    let buffer = ''
    try {
      const response = await fetch(getStreamUrl(), {
        headers: {
          Accept: 'text/event-stream',
          Authorization: `Bearer ${auth.token.trim()}`
        },
        signal: abortController.signal
      })

      if (!response.ok || !response.body) {
        throw new Error(`Notification stream failed: ${response.status}`)
      }

      streamConnected.value = true
      streamError.value = false
      void loadNotifications().catch(() => undefined)
      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const parts = buffer.split(/\r?\n\r?\n/)
        buffer = parts.pop() ?? ''

        for (const part of parts) {
          const message = parseSseBlock(part)
          if (!message || message.event !== 'notification') continue
          upsertNotification(JSON.parse(message.data) as NotificationItem)
        }
      }
    } catch (error) {
      if (!stopped) {
        streamError.value = true
      }
    } finally {
      abortController = null
      streamConnected.value = false
      scheduleReconnect()
    }
  }

  function start() {
    stopped = false
    clearReconnectTimer()
    startFallbackPolling()
    void loadNotifications()
    void connectStream()
  }

  function stop() {
    stopped = true
    clearReconnectTimer()
    clearPollTimer()
    abortController?.abort()
    abortController = null
    streamConnected.value = false
  }

  watch(
    () => auth.token,
    token => {
      stop()
      if (isJwtToken(token)) {
        start()
      }
    }
  )

  onBeforeUnmount(stop)

  return {
    notifications,
    unreadCount,
    loading,
    hasUnread,
    streamConnected,
    streamError,
    loadNotifications,
    markRead,
    markAllRead,
    start,
    stop
  }
}
