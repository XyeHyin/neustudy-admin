import request from './request'
import type { ApiResponse } from './types'

export interface NotificationItem {
  id: number
  eventLogId: number
  userId: number
  username: string
  httpMethod: string
  uri: string
  description?: string
  title: string
  content: string
  createTime: string
  read: boolean
}

export interface NotificationSummary {
  notifications: NotificationItem[]
  unreadCount: number
  latestReadEventLogId: number
}

export function getNotificationSummary(params: { limit?: number } = {}) {
  return request<ApiResponse<NotificationSummary>>({
    url: '/notifications',
    method: 'GET',
    params
  })
}

export function markAllNotificationsRead(params: { limit?: number } = {}) {
  return request<ApiResponse<NotificationSummary>>({
    url: '/notifications/read-all',
    method: 'POST',
    params
  })
}

export function markNotificationRead(eventLogId: number, params: { limit?: number } = {}) {
  return request<ApiResponse<NotificationSummary>>({
    url: `/notifications/${eventLogId}/read`,
    method: 'POST',
    params
  })
}
