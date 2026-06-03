import type { ApiResponse, PageResult } from './common'

export interface PermissionVO {
  id: number
  code: string
  name: string
}

export interface RoleVO {
  id: number
  name: string
  description: string
  createTime: string
  updateTime: string
}

export interface RoleDetailVO extends RoleVO {
  permissions: PermissionVO[]
}

export interface CreateRoleDTO {
  name: string
  description: string
  permissionIds: number[]
}

export type UpdateRoleDTO = CreateRoleDTO
export type PageResultRoleVO = PageResult<RoleVO>

export type ApiResponseRoleVO = ApiResponse<RoleVO>
export type ApiResponseRoleDetailVO = ApiResponse<RoleDetailVO>
export type ApiResponseListRoleVO = ApiResponse<RoleVO[]>
export type ApiResponsePageResultRoleVO = ApiResponse<PageResultRoleVO>
export type ApiResponsePermissionVO = ApiResponse<PermissionVO>
export type ApiResponseListPermissionVO = ApiResponse<PermissionVO[]>
