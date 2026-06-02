import request from './request'
import type { ApiResponse } from './types'

export function getEventLogs(params: { page?: number; size?: number }) {
  return request<ApiResponse<any>>({
    url: '/event-logs',
    method: 'GET',
    params
  })
}
 