import request from './request'

import type { ApiResponse, CreateRoleDTO, RoleDetailVO, RoleVO } from './types'

// 获取所有角色
export function getRoles() {
  return request<ApiResponse<RoleVO[]>>({
    url: '/roles',
    method: 'GET'
  })
}

// 获取角色详情
export function getRoleDetail(roleId: number) {
  return request<ApiResponse<RoleDetailVO>>({
    url: `/roles/${roleId}`,
    method: 'GET'
  })
}

// 创建角色
export function createRole(data: CreateRoleDTO) {
  return request<ApiResponse<RoleVO>>({
    url: '/roles',
    method: 'POST',
    data
  })
}

// 编辑角色
export function updateRole(roleId: number, data: CreateRoleDTO) {
  return request<ApiResponse<RoleVO>>({
    url: `/roles/${roleId}`,
    method: 'PUT',
    data
  })
}

// 删除角色
export function deleteRole(roleId: number) {
  return request<ApiResponse<boolean>>({
    url: `/roles/${roleId}`,
    method: 'DELETE'
  })
}

// 批量删除角色
export function batchDeleteRoles(roleIds: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/roles/batch',
    method: 'DELETE',
    data: roleIds
  })
}

// 分配角色权限
export function assignRolePermissions(roleId: number, permissionIds: number[]) {
  return request<ApiResponse<RoleVO>>({
    url: `/roles/${roleId}/permissions`,
    method: 'POST',
    data:  permissionIds 
  })
}
