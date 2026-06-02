import request from './request'

import type {
  ApiResponse,
  ApiResponsePageResultQuestionHistoryVO,
  ApiResponseQuestionVO,
  CreateQuestionDTO,
  Difficulty,
  PageResultQuestionVO,
  QuestionDetailVO,
  QuestionType,
  QuestionVO,
  UpdateQuestionDTO
} from './types'

// 获取所有题目
export function getQuestions() {
  return request<ApiResponse<QuestionVO[]>>({
    url: '/questions',
    method: 'GET'
  })
}

// 分页获取所有题目
export function getQuestionPage(params: {
  page?: number
  size?: number
  keyword?: string
  enabled?: boolean
  type?: QuestionType
  difficulty?: Difficulty
  knowledgePointId?: number
}) {
  return request<ApiResponse<PageResultQuestionVO>>({
    url: '/questions/page',
    method: 'GET',
    params
  })
}

// 获取我的题目
export function getMyQuestions() {
  return request<ApiResponse<QuestionVO[]>>({
    url: '/questions/my',
    method: 'GET'
  })
}

// 分页获取我的题目
export function getMyQuestionPage(params: {
  page?: number
  size?: number
  keyword?: string
  enabled?: boolean
  type?: QuestionType
  difficulty?: Difficulty
  knowledgePointId?: number
}) {
  return request<ApiResponse<PageResultQuestionVO>>({
    url: '/questions/my/page',
    method: 'GET',
    params
  })
}

// 获取题目详情
export function getQuestionDetail(questionId: number) {
  return request<ApiResponse<QuestionDetailVO>>({
    url: `/questions/${questionId}`,
    method: 'GET'
  })
}

// 创建题目
export function createQuestion(data: CreateQuestionDTO) {
  return request<ApiResponse<QuestionVO>>({
    url: '/questions',
    method: 'POST',
    data
  })
}

// 管理员创建题目
export function adminCreateQuestion(data: CreateQuestionDTO) {
  return request<ApiResponse<QuestionVO>>({
    url: '/questions/admin',
    method: 'POST',
    data
  })
}

// 更新题目
export function updateQuestion(questionId: number, data: UpdateQuestionDTO) {
  return request<ApiResponse<QuestionVO>>({
    url: `/questions/${questionId}`,
    method: 'PUT',
    data
  })
}

// 管理员更新题目
export function adminUpdateQuestion(questionId: number, data: UpdateQuestionDTO) {
  return request<ApiResponse<QuestionVO>>({
    url: `/questions/admin/${questionId}`,
    method: 'PUT',
    data
  })
}

// 更新题目状态
export function updateQuestionStatus(questionId: number, enabled: boolean) {
  return request<ApiResponse<QuestionVO>>({
    url: `/questions/${questionId}/status`,
    method: 'PUT',
    params: { enabled }
  })
}

// 删除题目
export function deleteQuestion(questionId: number) {
  return request<ApiResponse<boolean>>({
    url: `/questions/${questionId}`,
    method: 'DELETE'
  })
}

// 管理员删除题目
export function adminDeleteQuestion(questionId: number) {
  return request<ApiResponse<boolean>>({
    url: `/questions/admin/${questionId}`,
    method: 'DELETE'
  })
}

// 批量删除题目
export function batchDeleteQuestions(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/questions/batch',
    method: 'DELETE',
    data: ids
  })
}

// 根据知识点获取题目
export function getQuestionsByKnowledgePoint(knowledgePointId: number, enabled?: boolean) {
  return request<ApiResponse<QuestionVO[]>>({
    url: `/questions/knowledge-point/${knowledgePointId}`,
    method: 'GET',
    params: { enabled }
  })
}

// 获取题目类型列表
export function getQuestionTypes() {
  return request<ApiResponse<QuestionType[]>>({
    url: '/questions/types',
    method: 'GET'
  })
}

// 获取难度列表
export function getDifficulties() {
  return request<ApiResponse<Difficulty[]>>({
    url: '/questions/difficulties',
    method: 'GET'
  })
}

// 获取题目统计
export function getQuestionStatistics() {
  return request<ApiResponse<Record<string, any>>>({
    url: '/questions/statistics',
    method: 'GET'
  })
}

// 批量创建题目
export function batchCreateQuestions(data: { questions: CreateQuestionDTO[]; isAiGenerated?: boolean }) {
  return request<ApiResponse<QuestionVO[]>>({
    url: '/questions/batch',
    method: 'POST',
    data
  })
}

// 批量删除我的题目
export function batchDeleteMyQuestions(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/questions/batch',
    method: 'DELETE',
    data: ids
  })
}

// 回退题目
export function revertQuestion(questionId: number, revision: number) {
  return request<ApiResponseQuestionVO>({
    url: `/questions/${questionId}/history/${revision}`,
    method: 'POST'
  })
}

// 获取题目历史版本
export function getQuestionHistory(questionId: number, params: { page?: number; size?: number }) {
  return request<ApiResponsePageResultQuestionHistoryVO>({
    url: `/questions/${questionId}/history`,
    method: 'GET',
    params
  })
}
