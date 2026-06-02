import request from './request'

import type { ApiResponse, CreateUserDTO, PageResultUserVO, UpdateUserDTO, UserDetailVO, UserVO } from './types'

// 获取所有用户
export function getUsers() {
  return request<ApiResponse<UserVO[]>>({
    url: '/users',
    method: 'GET'
  })
}

// 分页获取用户列表
export function getUserPage(params: { page?: number; size?: number; keyword?: string, enabled?: number }) {
  return request<ApiResponse<PageResultUserVO>>({
    url: '/users/page',
    method: 'GET',
    params
  })
}

// 获取用户详情
export function getUserDetail(userId: number) {
  return request<ApiResponse<UserDetailVO>>({
    url: `/users/${userId}`,
    method: 'GET'
  })
}

// 获取当前用户详情
export function getCurrentUserDetail() {
  return request<ApiResponse<UserDetailVO>>({
    url: '/users/me',
    method: 'GET'
  })
}

// 修改个人信息
export function updateOwnProfile(data: UpdateUserDTO) {
  return request<ApiResponse<UserVO>>({
    url: '/users',
    method: 'PUT',
    data
  })
}

// 创建用户
export function createUser(data: CreateUserDTO) {
  return request<ApiResponse<UserVO>>({
    url: '/users',
    method: 'POST',
    data
  })
}

// 修改用户信息
export function updateUser(userId: number, data: UpdateUserDTO) {
  return request<ApiResponse<UserVO>>({
    url: `/users/${userId}`,
    method: 'PUT',
    data
  })
}

// 删除用户
export function deleteUser(userId: number) {
  return request<ApiResponse<boolean>>({
    url: `/users/${userId}`,
    method: 'DELETE'
  })
}

// 批量删除用户
export function batchDeleteUsers(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/users/batch',
    method: 'DELETE',
    data: ids
  })
}

// 修改用户状态（启用/禁用）
export function updateUserStatus(userId: number, enabled: boolean) {
  return request<ApiResponse<boolean>>({
    url: `/users/${userId}/status`,
    method: 'PUT',
    params: { enabled }
  })
}

// 修改用户角色
export function updateUserRole(userId: number, roleId: number) {
  return request<ApiResponse<UserDetailVO>>({
    url: `/users/${userId}/role/${roleId}`,
    method: 'PUT'
  })
}

// 修改用户密码（管理员操作）
export function updateUserPassword(userId: number, data: { newPassword: string }) {
  return request<ApiResponse<boolean>>({
    url: `/users/${userId}/password`,
    method: 'PUT',
    data
  })
}

// 修改个人密码
export function updateOwnPassword(data: { oldPassword: string; newPassword: string }) {
  return request<ApiResponse<boolean>>({
    url: '/users/password',
    method: 'PUT',
    data
  })
}
