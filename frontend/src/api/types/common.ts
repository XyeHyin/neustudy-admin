export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

export interface PageResult<T> {
  content: T[]
  total: number
  page: number
  size: number
}
