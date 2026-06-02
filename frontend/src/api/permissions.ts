import request from './request'
import type { ApiResponse, PermissionVO } from './types'

// 获取所有权限
export function getPermissions() {
  return request<ApiResponse<PermissionVO[]>>({
    url: '/permissions',
    method: 'GET'
  })
}

// 获取权限详情
export function getPermissionDetail(permissionId: number) {
  return request<ApiResponse<PermissionVO>>({
    url: `/permissions/${permissionId}`,
    method: 'GET'
  })
}