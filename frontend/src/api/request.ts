import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

import { isJwtToken, useAuthStore } from '@/store/auth'

import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 定义通用返回数据格式
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

const AUTH_FREE_URLS = new Set(['/auth/login', '/auth/register', '/auth/code', '/auth/reset-password'])

function getRequestPath(url?: string) {
  if (!url) return ''
  try {
    return new URL(url, window.location.origin).pathname
  } catch {
    return url.split('?')[0]
  }
}

function isAuthFreeRequest(url?: string) {
  return AUTH_FREE_URLS.has(getRequestPath(url))
}

// 请求拦截器
service.interceptors.request.use(
  config => {
    const auth = useAuthStore()
    if (!config.headers) return config

    if (isAuthFreeRequest(config.url)) {
      delete config.headers['Authorization']
      delete config.headers['authorization']
      return config
    }

    if (isJwtToken(auth.token)) {
      config.headers['Authorization'] = `Bearer ${auth.token.trim()}`
    } else if (auth.token) {
      auth.logout()
      delete config.headers['Authorization']
      delete config.headers['authorization']
    }
    return config
  },
  error => Promise.reject(error)
)

const { message } = createDiscreteApi(['message'])

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    // 如果是blob类型，直接返回响应对象
    if (response.config.responseType === 'blob') {
      return response
    }
    if (res.code !== 200) {
      message.error(res.message || '请求失败')
    }
    return response
  },
  error => {
    const response = error.response
    const status = response?.status ?? response?.data?.code
    const url = response?.config?.url

    if (status === 401) {
      useAuthStore().logout()
      message.error(response?.data?.message ?? '登录已失效，请重新登录')
    } else if (status === 403) {
      message.error(`${response?.data?.message ?? '没有权限访问该资源'}${url ? `: ${url}` : ''}`)
    }
    return Promise.reject(response ? response.data : error)
  }
)

// 封装一个 request 方法
async function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  const res = await service.request<T>(config)
  return res.data
}

export default request
export type { ApiResponse }
