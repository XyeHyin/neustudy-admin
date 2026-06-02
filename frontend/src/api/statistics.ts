import request from './request'
import type { ApiResponse } from './types'

// 仪表盘统计概览
export function getDashboardStats() {
  return request<ApiResponse<any>>({
    url: '/statistics/dashboard/stats',
    method: 'GET'
  })
}

// 题目生成趋势图
export function getDashboardTrend(period: string) {
  return request<ApiResponse<any>>({
    url: '/statistics/dashboard/trend',
    method: 'GET',
    params: { period }
  })
}

// 题型分布饼图
export function getDashboardDistribution() {
  return request<ApiResponse<any>>({
    url: '/statistics/dashboard/distribution',
    method: 'GET'
  })
}

// 总览统计
export function getOverviewStatistics() {
  return request<ApiResponse<any>>({
    url: '/statistics/overview',
    method: 'GET'
  })
} 