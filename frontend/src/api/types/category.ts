import type { ApiResponse } from './common'

export interface CreateCategoryDTO {
  name: string
  parentId?: number
  description?: string
}

export interface UpdateCategoryDTO {
  name?: string
  parentId?: number
  description?: string
}

export interface CategoryFlatVO {
  id: number
  name: string
  description: string
  parent?: CategoryFlatVO
  enabled: boolean
  createTime: string
  updateTime: string
}

export interface CategoryTreeVO {
  id: number
  name: string
  description: string
  enabled: boolean
  createTime: string
  updateTime: string
}

export type ApiResponseCategoryFlatVO = ApiResponse<CategoryFlatVO>
export type ApiResponseListCategoryFlatVO = ApiResponse<CategoryFlatVO[]>
export type ApiResponseListCategoryTreeVO = ApiResponse<CategoryTreeVO[]>
