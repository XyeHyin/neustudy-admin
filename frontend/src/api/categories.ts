import request from './request'

import type { ApiResponse, CategoryFlatVO, CategoryTreeVO } from './types'

// 获取所有分类（扁平化）
export function getCategories() {
  return request<ApiResponse<CategoryFlatVO[]>>({
    url: '/categories',
    method: 'GET'
  })
}

// 创建分类
export function createCategory(data: Partial<CategoryFlatVO>) {
  return request<ApiResponse<CategoryFlatVO>>({
    url: '/categories',
    method: 'POST',
    data
  })
}

// 修改分类
export function updateCategory(categoryId: number, data: Partial<CategoryFlatVO>) {
  return request<ApiResponse<CategoryFlatVO>>({
    url: `/categories/${categoryId}`,
    method: 'PUT',
    data
  })
}

// 删除分类
export function deleteCategory(categoryId: number) {
  return request<ApiResponse<boolean>>({
    url: `/categories/${categoryId}`,
    method: 'DELETE'
  })
}

// 批量删除分类
export function batchDeleteCategories(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/categories/batch',
    method: 'DELETE',
    data: ids
  })
}

// 获取分类树
export function getCategoryTree() {
  return request<ApiResponse<CategoryTreeVO[]>>({
    url: '/categories/tree',
    method: 'GET'
  })
}
