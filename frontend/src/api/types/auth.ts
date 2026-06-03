import type { ApiResponse } from './common'

export interface LoginVO {
  id: number
  token: string
  username: string
  nickname: string
  email: string | null
  phone: string | null
  avatar: string | null
}

export interface RegisterVO extends LoginVO {}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  nickname: string
  email: string
  phone: string
  code: string
}

export interface SendCodeDTO {
  email: string
}

export interface ResetPasswordDTO {
  email: string
  code: string
  newPassword: string
}

export type ApiResponseLoginVO = ApiResponse<LoginVO>
export type ApiResponseRegisterVO = ApiResponse<RegisterVO>
