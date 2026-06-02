import { useAuthStore } from '@/store/auth';



import request from './request';



import type { ApiResponse, PageResultPaperListVO, PaperCreateDTO, PaperDetailVO, PaperPreviewVO, PaperQuestionDTO, PaperQuestionVO, PaperStatisticsVO, PaperUpdateDTO, SmartPaperDTO } from './types';


const auth = useAuthStore()

// 分页查询试卷
export function getPapers(params: { page?: number; size?: number; title?: string; status?: string; teacherId?: number }) {
  return request<ApiResponse<PageResultPaperListVO>>({
    url: '/paper/page',
    method: 'GET',
    params
  })
}

// 获取试卷详情
export function getPaperDetail(id: number) {
  return request<ApiResponse<PaperDetailVO>>({
    url: `/paper/${id}`,
    method: 'GET',
    params: {
      currentUserId: auth.user?.id,
      isAdmin: auth.hasPermission('paper:view:all')
    }
  })
}

// 创建试卷
export function createPaper(data: PaperCreateDTO) {
  return request<ApiResponse<PaperDetailVO>>({
    url: '/paper/create',
    method: 'POST',
    data
  })
}

// 编辑试卷
export function updatePaper(id: number, data: PaperUpdateDTO) {
  return request<ApiResponse<PaperDetailVO>>({
    url: `/paper/${id}`,
    method: 'PUT',
    data
  })
}

// 删除试卷
export function deletePaper(id: number) {
  return request<ApiResponse<void>>({
    url: `/paper/${id}`,
    method: 'DELETE'
  })
}

// 发布试卷
export function publishPaper(id: number) {
  return request<ApiResponse<void>>({
    url: `/paper/${id}/publish`,
    method: 'POST'
  })
}

// 归档试卷
export function archivePaper(id: number) {
  return request<ApiResponse<void>>({
    url: `/paper/${id}/archive`,
    method: 'POST'
  })
}

// 试卷预览
export function previewPaper(id: number) {
  return request<ApiResponse<PaperPreviewVO>>({
    url: `/paper/${id}/preview`,
    method: 'GET'
  })
}

// 试卷统计
export function getPaperStatistics(id: number) {
  return request<ApiResponse<PaperStatisticsVO>>({
    url: `/paper/${id}/statistics`,
    method: 'GET'
  })
}

// 智能组卷
export function smartGenerate(data: SmartPaperDTO) {
  return request<ApiResponse<PaperDetailVO>>({
    url: '/paper/smart-generate',
    method: 'POST',
    data
  })
}

// 查询试卷题目列表
export function getPaperQuestions(paperId: number, randomOrder = false) {
  return request<ApiResponse<PaperQuestionVO[]>>({
    url: `/paper/${paperId}/questions`,
    method: 'GET',
    params: {
      randomOrder,
      currentUserId: auth.user?.id,
      isAdmin: auth.hasPermission('paper:view:all')
    }
  })
}

/** 批量添加题目到试卷 */
export function addQuestionsToPaper(paperId: number, questions: PaperQuestionDTO[]) {
  return request<ApiResponse<void>>({
    url: `/paper/${paperId}/questions`,
    method: 'POST',
    params: {
      currentUserId: auth.user?.id,
      isAdmin: auth.hasPermission('paper:view:all')
    },
    data: questions
  })
}

/** 批量调整题目顺序和分值 */
export function updatePaperQuestions(paperId: number, questions: PaperQuestionDTO[]) {
  return request<ApiResponse<void>>({
    url: `/paper/${paperId}/questions`,
    method: 'PUT',
    params: {
      currentUserId: auth.user?.id,
      isAdmin: auth.hasPermission('paper:view:all')
    },
    data: questions
  })
}

/** 移除试卷题目 */
export function removePaperQuestion(paperId: number, questionId: number) {
  return request<ApiResponse<void>>({
    url: `/paper/${paperId}/questions/${questionId}`,
    method: 'DELETE',
    params: {
      currentUserId: auth.user?.id,
      isAdmin: auth.hasPermission('paper:view:all')
    }
  })
}