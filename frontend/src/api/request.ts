import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'

import router from '@/router'
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

function normalizeStatus(status?: number | string) {
  const normalized = Number(status)
  return Number.isFinite(normalized) ? normalized : undefined
}

function redirectToLogin() {
  const current = router.currentRoute.value
  if (current.name === 'login') return

  router.replace({
    name: 'login',
    query: current.fullPath ? { redirect: current.fullPath } : undefined
  })
}

function redirectToError(status: number) {
  const currentName = router.currentRoute.value.name
  if (status === 403) {
    if (currentName !== 'forbidden') router.replace({ name: 'forbidden' })
    return
  }

  if (status === 404) {
    if (currentName !== 'not-found') router.replace('/not-found')
    return
  }

  if (status >= 500) {
    if (currentName !== 'error') router.replace({ name: 'error', query: { status: String(status) } })
  }
}

function handleHttpError(status: number | undefined, errorMessage?: string, url?: string) {
  if (status === 401) {
    useAuthStore().logout()
    message.error(errorMessage || '登录已失效，请重新登录')
    redirectToLogin()
    return
  }

  if (status === 403) {
    message.error(`${errorMessage || '没有权限访问该资源'}${url ? `: ${url}` : ''}`)
    redirectToError(status)
    return
  }

  if (status === 404) {
    message.error(errorMessage || '请求的资源不存在')
    redirectToError(status)
    return
  }

  if (status && status >= 500) {
    message.error(errorMessage || '服务器异常，请稍后重试')
    redirectToError(status)
    return
  }

  message.error(errorMessage || '网络异常，请检查连接')
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
      redirectToLogin()
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
    const bodyStatus = normalizeStatus(res?.code)
    if (bodyStatus && bodyStatus !== 200) {
      if (bodyStatus === 401 || bodyStatus === 403 || bodyStatus === 404 || bodyStatus >= 500) {
        handleHttpError(bodyStatus, res.message, response.config.url)
      } else {
        message.error(res.message || '请求失败')
      }
    }
    return response
  },
  error => {
    const response = error.response
    const status = normalizeStatus(response?.status ?? response?.data?.code)
    const url = response?.config?.url
    const errorMessage = response?.data?.message

    handleHttpError(status, errorMessage, url)
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
