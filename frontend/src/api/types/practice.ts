import type { ApiResponse } from './common'

export interface PracticeSessionVO {
  id: number
  paperId: number
  paperTitle: string
  studentId: number
  studentName: string
  attempt: number
  submitted: boolean
  startTime: string
  submitTime?: string
  answers: PracticeAnswerVO[]
}

export interface PracticeStartDTO {
  paperId: number
}

export interface PracticeAnswerVO {
  questionId: number
  questionTitle: string
  questionType: string
  answerContent: string
  score: number
  correct: boolean
  aiComment?: string
  marked: boolean
}

export interface AnswerDTO {
  questionId: number
  answerContent: string
  marked?: boolean
}

export interface PracticeSubmitDTO {
  practiceSessionId: number
  answers: AnswerDTO[]
}

export interface PracticeResultVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  totalScore: number
  correctRate: number
  submitTime: string
  answers: PracticeAnswerVO[]
}

export interface PracticeOverviewVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  totalQuestions: number
  answeredQuestions: number
  markedQuestions: number
  submitted: boolean
  questionProgressList: QuestionProgress[]
}

export interface PracticeDetailVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  paperDescription: string
  timeLimit: number
  totalScore: number
  startTime: string
  submitted: boolean
  questions: PracticeQuestionVO[]
}

export interface PracticeQuestionVO {
  questionId: number
  title: string
  content: string
  type: string
  orderNum: number
  score: number
  options: string
  difficulty: string
  studentAnswer?: string
  marked: boolean
}

export interface QuestionProgress {
  questionId: number
  questionTitle: string
  answered: boolean
  marked: boolean
}

export interface PracticeRecordVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  totalScore: number
  correctRate: number
  submitted: boolean
  startTime: string
  submitTime?: string
  maxScore: number
  minScore: number
  avgScore: number
  totalAttempts: number
}

export interface PracticeMarkDTO {
  practiceSessionId: number
  questionId: number
  marked: boolean
}

export interface PageResultPracticeRecordVO {
  content: PracticeRecordVO[]
  total: number
  page: number
  size: number
}

export interface PracticePaperStatVO {
  avgScore?: number
  createTime?: Date
  hasFullScore?: boolean
  maxScore?: number
  minScore?: number
  paperDescription?: string
  paperId?: number
  paperTitle?: string
  scoreTrend?: number[]
  status?: string
  teacherName?: string
  timeLimit?: number
  totalAttempts?: number
  totalScore?: number
  [property: string]: any
}

export type ApiResponsePracticeMarkDTO = ApiResponse<void>
export type ApiResponsePracticeOverviewVO = ApiResponse<PracticeOverviewVO>
export type ApiResponseListPracticeRecordVO = ApiResponse<PracticeRecordVO[]>
export type ApiResponsePageResultPracticeRecordVO = ApiResponse<PageResultPracticeRecordVO>
export type ApiResponseListPracticePaperStatVO = ApiResponse<PracticePaperStatVO[]>
