import type { ApiResponse, PageResult } from './common'
import type { CourseVO } from './course'
import type { Difficulty } from './question'

export interface KnowledgePointVO {
  id: number
  name: string
  description: string
  courseId: number
  courseName: string
  difficulty: Difficulty
  orderNum: number
  enabled: boolean
  content?: string
  keywords?: string
  createTime: string
  updateTime: string
}

export interface KnowledgePointDetailVO {
  id: number
  name: string
  description: string
  course?: CourseVO
  difficulty: Difficulty
  orderNum: number
  enabled: boolean
  content?: string
  keywords?: string
  createTime: string
  updateTime: string
}

export interface KnowledgePointSimpleVO {
  id: number
  name: string
  description: string
  difficulty: Difficulty
  difficultyDescription: string
  orderNum: number
  enabled: boolean
}

export interface CreateKnowledgePointDTO {
  name: string
  description: string
  courseId: number
  difficulty: Difficulty
  orderNum?: number
  content?: string
  keywords?: string
}

export interface UpdateKnowledgePointDTO {
  name?: string
  description?: string
  difficulty?: Difficulty
  orderNum?: number
  content?: string
  keywords?: string
}

export interface KnowledgePointStatisticsVO {
  totalKnowledgePoints: number
  enabledKnowledgePoints: number
  disabledKnowledgePoints: number
  myKnowledgePoints: number
  myEnabledKnowledgePoints: number
}

export type PageResultKnowledgePointVO = PageResult<KnowledgePointVO>

export type ApiResponseKnowledgePointVO = ApiResponse<KnowledgePointVO>
export type ApiResponseKnowledgePointDetailVO = ApiResponse<KnowledgePointDetailVO>
export type ApiResponseListKnowledgePointVO = ApiResponse<KnowledgePointVO[]>
export type ApiResponsePageResultKnowledgePointVO = ApiResponse<PageResultKnowledgePointVO>
export type ApiResponseKnowledgePointStatisticsVO = ApiResponse<KnowledgePointStatisticsVO>
