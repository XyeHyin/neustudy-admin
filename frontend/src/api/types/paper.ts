import type { ApiResponse } from './common'

export type PaperStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

export interface PaperListVO {
  id: number
  title: string
  teacherName: string
  status: string
  totalScore: number
  createTime: string
}

export interface PageResultPaperListVO {
  content: PaperListVO[]
  total: number
  page: number
  size: number
}

export interface PaperQuestionVO {
  questionId: number
  title: string
  type: string
  orderNum: number
  score: number
}

export interface PaperDetailVO {
  id: number
  title: string
  description: string
  teacherId: number
  teacherName: string
  timeLimit: number
  showAnswer: boolean
  allowRetry: boolean
  questionOrderType: string
  status: string
  totalScore: number
  createTime: string
  updateTime: string
  questions: PaperQuestionVO[]
}

export interface PaperCreateDTO {
  title: string
  description?: string
  teacherId: number
  timeLimit: number
  showAnswer: boolean
  allowRetry: boolean
  questionOrderType: string
}

export interface PaperUpdateDTO {
  title?: string
  description?: string
  timeLimit?: number
  showAnswer?: boolean
  allowRetry?: boolean
  questionOrderType?: string
  status?: string
}

export interface PaperQuestionDTO {
  questionId: number
  orderNum?: number
  score?: number
}

export interface SmartPaperDTO {
  difficulty?: string
  knowledgePointIds?: number[]
  questionTypes?: string[]
  totalQuestions?: number
  totalScore?: number
  timeLimit?: number
  questionTypeDistribution?: Record<string, number>
}

export interface PaperPreviewVO {
  id: number
  title: string
  description: string
  timeLimit: number
  questions: PaperQuestionVO[]
}

export interface PaperStatisticsVO {
  paperId: number
  paperTitle: string
  totalAttempts: number
  averageScore: number
  highestScore: number
  lowestScore: number
  correctRate: number
  passRate: number
  scoreDistribution: Record<string, number>
  questionCorrectRates: QuestionCorrectRate[]
  wrongQuestions: WrongQuestion[]
}

export interface QuestionCorrectRate {
  questionId: number
  questionTitle: string
  correctRate: number
}

export interface WrongQuestion {
  questionId: number
  questionTitle: string
  wrongCount: number
}

export type ApiResponsePaperStatisticsVO = ApiResponse<PaperStatisticsVO>
export type ApiResponsePaperPreviewVO = ApiResponse<PaperPreviewVO>
