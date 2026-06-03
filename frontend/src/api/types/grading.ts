import type { ApiResponse, PageResult } from './common'

export interface GradingResultVO {
  gradingResultId: number
  studentAnswerId: number
  aiScore: number
  aiComment: string
  aiReason: string
  aiGradingTime: string
  finalScore: number
  teacherComment: string
  reviewTeacherName: string
  reviewTime: string
}

export interface ManualGradingDTO {
  gradingResultId: number
  finalScore: number
  teacherComment?: string
}

export interface GradingRequestDTO {
  studentAnswerId: number
  answerContent: string
}

export interface AIGradingDTO {
  studentAnswerId: number
  answerContent: string
  aiScore: number
  aiComment: string
  aiReason: string
}

export interface GradingReviewVO {
  gradingResultId: number
  studentAnswerId: number
  studentName: string
  questionTitle: string
  answerContent: string
  aiScore: number
  aiComment: string
  aiReason: string
  aiGradingTime: string
  reviewed: boolean
}

export interface PageResultGradingResultVO {
  content: GradingResultVO[]
  total: number
  page: number
  size: number
}

export type PageResultGradingReviewVO = PageResult<GradingReviewVO>

export type ApiResponseListGradingReviewVO = ApiResponse<GradingReviewVO[]>
export type ApiResponseGradingResultVO = ApiResponse<GradingResultVO>
export type ApiResponsePageResultGradingReviewVO = ApiResponse<PageResultGradingReviewVO>
export type ApiResponsePageResultGradingResultVO = ApiResponse<PageResultGradingResultVO>
export type ApiResponseListGradingResultVO = ApiResponse<GradingResultVO[]>
