import request from './request'
import type { LoginVO, RegisterVO, ApiResponse } from './types'

// 登录
export function login(data: { username: string; password: string }) {
    return request<ApiResponse<LoginVO>>({
        url: '/auth/login',
        method: 'POST',
        data
    })
}

// 注册
export function register(data: {
    username: string
    password: string
    nickname: string
    email: string
    phone: string
    code: string
}) {
    return request<ApiResponse<RegisterVO>>({
        url: '/auth/register',
        method: 'POST',
        data
    })
}

// 重置密码
export function resetPassword(data: {
    email: string
    code: string
    newPassword: string
}) {
    return request<ApiResponse<void>>({
        url: '/auth/reset-password',
        method: 'POST',
        data
    })
}

// 发送验证码
export function sendCode(data: { email: string }) {
    return request<ApiResponse<void>>({
        url: '/auth/code',
        method: 'POST',
        data
    })
}