import request from './request'

import type {
  ApiResponse,
  CreateKnowledgePointDTO,
  KnowledgePointDetailVO,
  KnowledgePointStatisticsVO,
  KnowledgePointVO,
  PageResultKnowledgePointVO,
  UpdateKnowledgePointDTO
} from './types'

// 获取所有知识点
export function getKnowledgePoints() {
  return request<ApiResponse<KnowledgePointVO[]>>({
    url: '/knowledge-points',
    method: 'GET'
  })
}

// 分页获取所有知识点
export function getKnowledgePointPage(params: { page?: number; size?: number; keyword?: string; courseId?: number; difficulty?: string; enabled?: boolean }) {
  return request<ApiResponse<PageResultKnowledgePointVO>>({
    url: '/knowledge-points/page',
    method: 'GET',
    params
  })
}

// 根据课程ID获取知识点
export function getKnowledgePointsByCourse(courseId: number) {
  return request<ApiResponse<KnowledgePointVO[]>>({
    url: `/knowledge-points/course/${courseId}`,
    method: 'GET'
  })
}

// 获取知识点详情
export function getKnowledgePointDetail(knowledgePointId: number) {
  return request<ApiResponse<KnowledgePointDetailVO>>({
    url: `/knowledge-points/${knowledgePointId}`,
    method: 'GET'
  })
}

// 创建知识点
export function createKnowledgePoint(data: CreateKnowledgePointDTO) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: '/knowledge-points',
    method: 'POST',
    data
  })
}

// 管理员创建知识点
export function adminCreateKnowledgePoint(data: CreateKnowledgePointDTO & { createdBy: number }) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: '/knowledge-points/admin',
    method: 'POST',
    data
  })
}

// 更新知识点
export function updateKnowledgePoint(knowledgePointId: number, data: UpdateKnowledgePointDTO) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: `/knowledge-points/${knowledgePointId}`,
    method: 'PUT',
    data
  })
}

// 管理员更新知识点
export function adminUpdateKnowledgePoint(knowledgePointId: number, data: UpdateKnowledgePointDTO) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: `/knowledge-points/admin/${knowledgePointId}`,
    method: 'PUT',
    data
  })
}

// 删除知识点
export function deleteKnowledgePoint(knowledgePointId: number) {
  return request<ApiResponse<boolean>>({
    url: `/knowledge-points/${knowledgePointId}`,
    method: 'DELETE'
  })
}

// 管理员删除知识点
export function adminDeleteKnowledgePoint(knowledgePointId: number) {
  return request<ApiResponse<boolean>>({
    url: `/knowledge-points/admin/${knowledgePointId}`,
    method: 'DELETE'
  })
}

// 批量删除知识点
export function batchDeleteKnowledgePoints(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/knowledge-points/batch',
    method: 'DELETE',
    data: ids
  })
}

// 更新知识点状态
export function updateKnowledgePointStatus(knowledgePointId: number, enabled: boolean) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: `/knowledge-points/${knowledgePointId}/status`,
    method: 'PUT',
    params: { enabled }
  })
}

// 更新知识点排序
export function updateKnowledgePointOrder(knowledgePointId: number, orderNum: number) {
  return request<ApiResponse<KnowledgePointVO>>({
    url: `/knowledge-points/${knowledgePointId}/order`,
    method: 'PUT',
    params: { orderNum }
  })
}

// 获取知识点统计
export function getKnowledgePointStatistics() {
  return request<ApiResponse<KnowledgePointStatisticsVO>>({
    url: '/knowledge-points/statistics',
    method: 'GET'
  })
}

// 导出知识点
export function exportKnowledgePoints(params: { courseId?: number; enabled?: boolean; difficulty?: string; format?: string }) {
  return request<Blob>({
    url: '/knowledge-points/export',
    method: 'GET',
    params,
    responseType: 'blob'
  })
}

// 导入知识点
export function importKnowledgePoints(file: File, courseId: number) {
  const formData = new FormData()
  formData.append('file', file)

  return request<
    ApiResponse<{
      successCount: number
      failCount: number
      errorMessages?: string[]
      parseWarnings?: string[]
      hasParseWarnings?: boolean
    }>
  >({
    url: '/knowledge-points/import',
    method: 'POST',
    data: formData,
    params: { courseId },
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载导入模板
export function downloadImportTemplate() {
  return request<Blob>({
    url: '/knowledge-points/template',
    method: 'GET',
    responseType: 'blob'
  })
}

// 获取我的知识点
export function getMyKnowledgePoints() {
  return request<ApiResponse<KnowledgePointVO[]>>({
    url: '/knowledge-points/my',
    method: 'GET'
  })
}

// 分页获取我的知识点
export function getMyKnowledgePointPage(params: { page?: number; size?: number; keyword?: string; courseId?: number; difficulty?: string; enabled?: boolean }) {
  return request<ApiResponse<PageResultKnowledgePointVO>>({
    url: '/knowledge-points/my/page',
    method: 'GET',
    params
  })
}

// 导出我的知识点
export function exportMyKnowledgePoints(params: { courseId?: number; enabled?: boolean; difficulty?: string }) {
  return request<Blob>({
    url: '/knowledge-points/export/my',
    method: 'GET',
    params,
    responseType: 'blob'
  })
}
