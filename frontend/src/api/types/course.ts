import type { ApiResponse, PageResult } from './common'
import type { CategoryFlatVO } from './category'
import type { UserSimpleVO, UserVO } from './user'

export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'

export interface CourseVO {
  id: number
  name: string
  description: string
  subject: string
  grade: string
  semester?: string
  enabled: boolean
  coverImage?: string
  totalHours?: number
  completedHours?: number
  status: CourseStatus
  teacherName?: string
  teacherId?: number
  categoryName?: string
  categoryId?: number
  createTime: string
  updateTime: string
}

export interface CourseDetailVO {
  id: number
  name: string
  description: string
  subject: string
  grade: string
  semester?: string
  enabled: boolean
  coverImage?: string
  totalHours?: number
  completedHours?: number
  status: CourseStatus
  teacher?: UserVO
  category?: CategoryFlatVO
  createTime: string
  updateTime: string
  progress: number
}

export interface CourseSimpleVO {
  id: number
  name: string
  subject: string
  grade: string
  semester: string
  status: CourseStatus
  teacher: UserSimpleVO
}

export interface CreateCourseDTO {
  name: string
  description: string
  subject: string
  grade: string
  semester?: string
  enabled?: boolean
  categoryId?: number
  coverImage?: string
  totalHours?: number
  status?: CourseStatus
}

export interface UpdateCourseDTO {
  name?: string
  description?: string
  subject?: string
  grade?: string
  semester?: string
  enabled?: boolean
  categoryId?: number
  coverImage?: string
  totalHours?: number
  completedHours?: number
  status?: CourseStatus
}

export interface CourseStatisticsVO {
  totalCourses: number
  publishedCourses: number
  draftCourses: number
  archivedCourses: number
  myCourses: number
  myPublishedCourses: number
}

export interface SubjectVO {
  id: number
  name: string
  code: string
}

export interface GradeVO {
  id: number
  name: string
  code: string
}

export type PageResultCourseVO = PageResult<CourseVO>

export type ApiResponseCourseVO = ApiResponse<CourseVO>
export type ApiResponseCourseDetailVO = ApiResponse<CourseDetailVO>
export type ApiResponseListCourseVO = ApiResponse<CourseVO[]>
export type ApiResponsePageResultCourseVO = ApiResponse<PageResultCourseVO>
export type ApiResponseCourseStatisticsVO = ApiResponse<CourseStatisticsVO>
export type ApiResponseListSubjectVO = ApiResponse<SubjectVO[]>
export type ApiResponseListGradeVO = ApiResponse<GradeVO[]>
