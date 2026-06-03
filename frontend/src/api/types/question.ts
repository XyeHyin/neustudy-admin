import type { ApiResponse, PageResult } from './common'
import type { CourseSimpleVO } from './course'
import type { KnowledgePointDetailVO, KnowledgePointSimpleVO } from './knowledge-point'

export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'FILL_BLANK' | 'SHORT_ANSWER' | 'TRUE_FALSE' | 'ESSAY'
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'

export interface QuestionVO {
  id: number
  title: string
  content: string
  type: QuestionType
  typeDescription: string
  difficulty: Difficulty
  difficultyDescription: string
  options: string
  answer: string
  explanation: string
  score: number
  enabled: boolean
  tags?: string
  knowledgePoint: KnowledgePointSimpleVO
  createTime: string
  updateTime: string
}

export interface QuestionDetailVO {
  id: number
  title: string
  content: string
  type: QuestionType
  typeDescription: string
  difficulty: Difficulty
  difficultyDescription: string
  options: string
  answer: string
  explanation: string
  score: number
  enabled: boolean
  tags?: string
  knowledgePoint: KnowledgePointDetailVO
  createTime: string
  updateTime: string
}

export interface QuestionHistoryVO {
  question?: QuestionDetailVO
  revision?: number
  revisionDate?: Date
  [property: string]: any
}

export interface PageResultQuestionHistoryVO {
  content?: QuestionHistoryVO[]
  page?: number
  size?: number
  total?: number
  [property: string]: any
}

export interface CreateQuestionDTO {
  title: string
  content?: string
  type: QuestionType
  difficulty: Difficulty
  options?: string
  answer: string
  explanation?: string
  score?: number
  knowledgePointId: number | null
  enabled?: boolean
  tags?: string
}

export interface UpdateQuestionDTO {
  title?: string
  content?: string
  type?: QuestionType
  difficulty?: Difficulty
  options?: string
  answer?: string
  explanation?: string
  score?: number
  knowledgePointId?: number
  enabled?: boolean
  tags?: string
}

export interface QuestionStatisticsVO {
  totalQuestions: number
  enabledQuestions: number
  disabledQuestions: number
  myQuestions: number
  myEnabledQuestions: number
}

export interface AIGenerateQuestionDTO {
  knowledgePointId: number | null
  type?: QuestionType | null
  difficulty?: Difficulty | null
  count: number
  extraRequirement?: string
}

export interface AIGeneratedQuestionVO {
  title: string
  content?: string
  type: QuestionType
  difficulty: Difficulty
  options?: string
  answer: string
  explanation?: string
  score?: number
  isAiGenerated?: boolean
  tags?: string
  knowledgePointId: number
  enabled?: boolean
}

export type PageResultQuestionVO = PageResult<QuestionVO>

export type ApiResponseQuestionVO = ApiResponse<QuestionVO>
export type ApiResponseQuestionDetailVO = ApiResponse<QuestionDetailVO>
export type ApiResponseListQuestionVO = ApiResponse<QuestionVO[]>
export type ApiResponsePageResultQuestionVO = ApiResponse<PageResultQuestionVO>
export type ApiResponseQuestionStatisticsVO = ApiResponse<QuestionStatisticsVO>
export type ApiResponseListQuestionType = ApiResponse<QuestionType[]>
export type ApiResponseListDifficulty = ApiResponse<Difficulty[]>
export type ApiResponsePageResultQuestionHistoryVO = ApiResponse<PageResultQuestionHistoryVO>
export type ApiResponseListAIGeneratedQuestionVO = ApiResponse<AIGeneratedQuestionVO[]>
export type ApiResponseListCreateQuestionDTO = ApiResponse<CreateQuestionDTO[]>
