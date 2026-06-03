import request from './request'

import type {
  AIGradingDTO,
  ApiResponse,
  ApiResponsePageResultGradingResultVO,
  ApiResponsePageResultGradingReviewVO,
  GradingRequestDTO,
  GradingResultVO,
  ManualGradingDTO
} from './types'


// 开始练习（判分专用）
export function startGradingPractice(data: any) {
  return request<ApiResponse<any>>({
    url: '/grading/start',
    method: 'POST',
    data
  })
}

// AI判分
export function aiGrading(data: AIGradingDTO) {
  return request<ApiResponse<GradingResultVO>>({
    url: '/grading/ai',
    method: 'POST',
    data
  })
}

// 自动判分
export function autoGrading(data: GradingRequestDTO) {
  return request<ApiResponse<GradingResultVO>>({
    url: '/grading/auto',
    method: 'POST',
    data
  })
}

// 人工批阅
export function manualGrading(data: ManualGradingDTO, teacherId: number) {
  return request<ApiResponse<GradingResultVO>>({
    url: `/grading/manual?teacherId=${teacherId}`,
    method: 'POST',
    data
  })
}

// 获取判分结果
export function getGradingResult(id: number) {
  return request<ApiResponse<GradingResultVO>>({
    url: `/grading/${id}`,
    method: 'GET'
  })
}

// 获取待复核列表（支持分页、试卷ID、题目ID筛选）
export function listGradingReview(params?: { page?: number; size?: number; paperId?: number; questionTitle?: string }) {
  return request<ApiResponsePageResultGradingReviewVO>({
    url: '/grading/review/list',
    method: 'GET',
    params
  })
}

// 获取已批改列表（支持分页、试卷ID、题目ID筛选）
export function listReviewedGradingResult(params?: { page?: number; size?: number; paperId?: number; questionTitle?: string }) {
  return request<ApiResponsePageResultGradingResultVO>({
    url: '/grading/reviewed/list',
    method: 'GET',
    params
  })
}
