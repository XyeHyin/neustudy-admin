import type { ApiResponse, PageResult } from './common'
import type { RoleDetailVO } from './role'

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  enabled: boolean
  createTime: string
  updateTime: string
}

export interface UserSimpleVO {
  id: number
  username: string
  nickname: string
}

export interface CreateUserDTO {
  username: string
  password: string
  nickname: string
  email: string
  phone: string
  avatar?: string
  roleId: number
}

export interface UserDetailVO extends UserVO {
  role: RoleDetailVO
}

export interface UpdateOwnPasswordDTO {
  newPassword: string
}

export interface UpdateUserPasswordDTO {
  oldPassword: string
  newPassword: string
}

export type UpdateUserDTO = Pick<CreateUserDTO, 'username' | 'nickname' | 'email' | 'phone' | 'avatar'>
export type PageResultUserVO = PageResult<UserVO>

export type ApiResponseUserVO = ApiResponse<UserVO>
export type ApiResponseUserDetailVO = ApiResponse<UserDetailVO>
export type ApiResponseListUserVO = ApiResponse<UserVO[]>
export type ApiResponsePageResultUserVO = ApiResponse<PageResultUserVO>
