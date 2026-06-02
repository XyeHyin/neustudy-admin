export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

export interface LoginVO {
  id: number
  token: string
  username: string
  nickname: string
  email: string | null
  phone: string | null
  avatar: string | null
}

export interface RegisterVO extends LoginVO {}

export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  enabled: boolean
  createTime: string
  updateTime: string
}

export interface CreateUserDTO {
  username: string // >= 1 characters
  password: string // [6, 2147483647] characters
  nickname: string // >= 1 characters
  email: string // >= 1 characters
  phone: string // >= 1 characters, 正则见下
  avatar?: string // 可选，正则见下
  roleId: number // int64
}

export interface UserDetailVO extends UserVO {
  role: RoleDetailVO
}

// 登录 DTO
export interface LoginDTO {
  username: string // >= 1 characters
  password: string // [6, 20] characters
}

// 注册 DTO
export interface RegisterDTO {
  username: string // >= 1 characters
  password: string // [6, 20] characters
  nickname: string // >= 1 characters
  email: string // >= 1 characters
  phone: string // >= 1 characters, 手机号正则
  code: string // >= 1 characters
}

// 发送验证码 DTO
export interface SendCodeDTO {
  email: string // >= 1 characters
}

// 重置密码 DTO
export interface ResetPasswordDTO {
  email: string // >= 1 characters
  code: string // >= 1 characters
  newPassword: string // [6, 20] characters
}

// 修改个人密码 DTO
export interface UpdateOwnPasswordDTO {
  newPassword: string // [6, 20] characters
}

// 修改用户密码 DTO
export interface UpdateUserPasswordDTO {
  oldPassword: string // [6, 20] characters
  newPassword: string // [6, 20] characters
}

// 修改/创建用户 DTO（UpdateUserDTO 与 CreateUserDTO 字段一致，CreateUserDTO 已有）
export type UpdateUserDTO = Pick<CreateUserDTO, 'username' | 'nickname' | 'email' | 'phone' | 'avatar'>
export interface CreateRoleDTO {
  name: string // >= 1 characters
  description: string // >= 1 characters
  permissionIds: number[]
}

// 编辑角色 DTO（与 CreateRoleDTO 相同）
export type UpdateRoleDTO = CreateRoleDTO

// 创建分类 DTO
export interface CreateCategoryDTO {
  name: string // >= 1 characters
  parentId?: number // int64
  description?: string // 0-255 characters
}

// 编辑分类 DTO
export interface UpdateCategoryDTO {
  name?: string // >= 1 characters
  parentId?: number // int64
  description?: string // 0-255 characters
}

// VO/Pagination

export interface PageResult<T> {
  content: T[]
  total: number
  page: number
  size: number
}

// 角色分页
export type PageResultRoleVO = PageResult<RoleVO>

// 用户分页
export type PageResultUserVO = PageResult<UserVO>

// 课程分页
export type PageResultCourseVO = PageResult<CourseVO>

export interface RoleVO {
  id: number
  name: string
  description: string
  createTime: string
  updateTime: string
}

export interface RoleDetailVO extends RoleVO {
  permissions: PermissionVO[]
}

export interface PermissionVO {
  id: number
  code: string
  name: string
}

export interface CategoryFlatVO {
  id: number
  name: string
  description: string
  parent?: CategoryFlatVO
  enabled: boolean
  createTime: string
  updateTime: string
}

// 分类树 VO
export interface CategoryTreeVO {
  id: number
  name: string
  description: string
  enabled: boolean
  createTime: string
  updateTime: string
}

// 课程相关类型定义
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
  status: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'
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
  status: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'
  teacher?: UserVO
  category?: CategoryFlatVO
  createTime: string
  updateTime: string
  progress: number
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
  status?: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'
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
  status?: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'
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

// ========== 响应类型补全 ==========

// 用户相关
export type ApiResponseUserVO = ApiResponse<UserVO>
export type ApiResponseUserDetailVO = ApiResponse<UserDetailVO>
export type ApiResponseListUserVO = ApiResponse<UserVO[]>
export type ApiResponsePageResultUserVO = ApiResponse<PageResultUserVO>

// 角色相关
export type ApiResponseRoleVO = ApiResponse<RoleVO>
export type ApiResponseRoleDetailVO = ApiResponse<RoleDetailVO>
export type ApiResponseListRoleVO = ApiResponse<RoleVO[]>
export type ApiResponsePageResultRoleVO = ApiResponse<PageResultRoleVO>

// 权限相关
export type ApiResponsePermissionVO = ApiResponse<PermissionVO>
export type ApiResponseListPermissionVO = ApiResponse<PermissionVO[]>

// 分类相关
export type ApiResponseCategoryFlatVO = ApiResponse<CategoryFlatVO>
export type ApiResponseListCategoryFlatVO = ApiResponse<CategoryFlatVO[]>
export type ApiResponseListCategoryTreeVO = ApiResponse<CategoryTreeVO[]>

// 认证相关
export type ApiResponseLoginVO = ApiResponse<LoginVO>
export type ApiResponseRegisterVO = ApiResponse<RegisterVO>

// 课程相关
export type ApiResponseCourseVO = ApiResponse<CourseVO>
export type ApiResponseCourseDetailVO = ApiResponse<CourseDetailVO>
export type ApiResponseListCourseVO = ApiResponse<CourseVO[]>
export type ApiResponsePageResultCourseVO = ApiResponse<PageResultCourseVO>
export type ApiResponseCourseStatisticsVO = ApiResponse<CourseStatisticsVO>
export type ApiResponseListSubjectVO = ApiResponse<SubjectVO[]>
export type ApiResponseListGradeVO = ApiResponse<GradeVO[]>

// 知识点相关类型定义
export interface KnowledgePointVO {
  id: number
  name: string
  description: string
  courseId: number
  courseName: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
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
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  orderNum: number
  enabled: boolean
  content?: string
  keywords?: string
  createTime: string
  updateTime: string
}

export interface CreateKnowledgePointDTO {
  name: string
  description: string
  courseId: number
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  orderNum?: number
  content?: string
  keywords?: string
}

export interface UpdateKnowledgePointDTO {
  name?: string
  description?: string
  difficulty?: 'EASY' | 'MEDIUM' | 'HARD'
  orderNum?: number
  content?: string
  keywords?: string
}

// 知识点分页结果
export type PageResultKnowledgePointVO = PageResult<KnowledgePointVO>

// 知识点统计
export interface KnowledgePointStatisticsVO {
  totalKnowledgePoints: number
  enabledKnowledgePoints: number
  disabledKnowledgePoints: number
  myKnowledgePoints: number
  myEnabledKnowledgePoints: number
}

// 知识点相关响应类型
export type ApiResponseKnowledgePointVO = ApiResponse<KnowledgePointVO>
export type ApiResponseKnowledgePointDetailVO = ApiResponse<KnowledgePointDetailVO>
export type ApiResponseListKnowledgePointVO = ApiResponse<KnowledgePointVO[]>
export type ApiResponsePageResultKnowledgePointVO = ApiResponse<PageResultKnowledgePointVO>
export type ApiResponseKnowledgePointStatisticsVO = ApiResponse<KnowledgePointStatisticsVO>

// 题目相关类型定义
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
export interface PageResultQuestionHistoryVO {
  content?: QuestionHistoryVO[]
  page?: number
  size?: number
  total?: number
  [property: string]: any
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

export interface KnowledgePointSimpleVO {
  id: number
  name: string
  description: string
  difficulty: Difficulty
  difficultyDescription: string
  orderNum: number
  enabled: boolean
  course: CourseSimpleVO
}

export interface CourseSimpleVO {
  id: number
  name: string
  subject: string
  grade: string
  semester: string
  status: 'DRAFT' | 'PUBLISHED' | 'COMPLETED' | 'ARCHIVED'
  teacher: UserSimpleVO
}

export interface UserSimpleVO {
  id: number
  username: string
  nickname: string
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
export interface QuestionHistoryVO {
  question?: QuestionDetailVO
  revision?: number
  revisionDate?: Date
  [property: string]: any
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

// 题目分页结果
export type PageResultQuestionVO = PageResult<QuestionVO>

// 题目统计
export interface QuestionStatisticsVO {
  totalQuestions: number
  enabledQuestions: number
  disabledQuestions: number
  myQuestions: number
  myEnabledQuestions: number
}

// 题目相关响应类型
export type ApiResponseQuestionVO = ApiResponse<QuestionVO>
export type ApiResponseQuestionDetailVO = ApiResponse<QuestionDetailVO>
export type ApiResponseListQuestionVO = ApiResponse<QuestionVO[]>
export type ApiResponsePageResultQuestionVO = ApiResponse<PageResultQuestionVO>
export type ApiResponseQuestionStatisticsVO = ApiResponse<QuestionStatisticsVO>
export type ApiResponseListQuestionType = ApiResponse<QuestionType[]>
export type ApiResponseListDifficulty = ApiResponse<Difficulty[]>
export type ApiResponsePageResultQuestionHistoryVO = ApiResponse<PageResultQuestionHistoryVO>

// AI题目生成相关类型定义
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

// AI生成题目响应类型
export type ApiResponseListAIGeneratedQuestionVO = ApiResponse<AIGeneratedQuestionVO[]>

// 新增：批量创建题目响应类型
export type ApiResponseListCreateQuestionDTO = ApiResponse<CreateQuestionDTO[]>

// 试卷状态
export type PaperStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'

// ================== 试卷相关类型 ==================
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

// ================== 练习相关类型 ==================
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
  score: number // double
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
  totalScore: number // double
  correctRate: number // double
  submitTime: string
  answers: PracticeAnswerVO[]
}

export interface PracticeOverviewVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  totalQuestions: number
  answeredQuestions: number
  markedQuestions: number // 新增字段
  submitted: boolean
  questionProgressList: QuestionProgress[]
}
// 练习详情
export interface PracticeDetailVO {
  practiceSessionId: number
  paperId: number
  paperTitle: string
  paperDescription: string
  timeLimit: number
  totalScore: number
  startTime: string // 与 OpenAPI 的 date-time 保持一致
  submitted: boolean
  questions: PracticeQuestionVO[]
}
// 练习题目详情
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
  totalScore: number // double
  correctRate: number // double
  submitted: boolean
  startTime: string
  submitTime?: string
  maxScore: number // double
  minScore: number // double
  avgScore: number // double
  totalAttempts: number // int
}

// 练习标记 DTO
export interface PracticeMarkDTO {
  practiceSessionId: number
  questionId: number
  marked: boolean
}

// 判分相关类型
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

export interface PageResultPracticeRecordVO {
  content: PracticeRecordVO[]
  total: number
  page: number
  size: number
}
export interface PracticePaperStatVO {
  avgScore?: number;
  createTime?: Date;
  hasFullScore?: boolean;
  maxScore?: number;
  minScore?: number;
  paperDescription?: string;
  paperId?: number;
  paperTitle?: string;
  scoreTrend?: number[];
  status?: string;
  teacherName?: string;
  timeLimit?: number;
  totalAttempts?: number;
  totalScore?: number;
  [property: string]: any;
}

// 添加缺失的响应类型
export type ApiResponsePracticeMarkDTO = ApiResponse<void>
export type ApiResponseListGradingReviewVO = ApiResponse<GradingReviewVO[]>
export type ApiResponseGradingResultVO = ApiResponse<GradingResultVO>
export type ApiResponsePracticeOverviewVO = ApiResponse<PracticeOverviewVO>
export type ApiResponseListPracticeRecordVO = ApiResponse<PracticeRecordVO[]>
export type ApiResponsePaperStatisticsVO = ApiResponse<PaperStatisticsVO>
export type ApiResponsePaperPreviewVO = ApiResponse<PaperPreviewVO>

export type ApiResponsePageResultPracticeRecordVO = ApiResponse<PageResultPracticeRecordVO>

// 所有可用试卷及用户练习统计响应类型
export type ApiResponseListPracticePaperStatVO = ApiResponse<PracticePaperStatVO[]>

// 针对待复核列表的分页结果
export type PageResultGradingReviewVO = PageResult<GradingReviewVO>
export type ApiResponsePageResultGradingReviewVO = ApiResponse<PageResultGradingReviewVO>

// 判分结果分页
export interface PageResultGradingResultVO {
  content: GradingResultVO[]
  total: number
  page: number
  size: number
}

// 判分结果分页响应
export type ApiResponsePageResultGradingResultVO = ApiResponse<PageResultGradingResultVO>

// 判分结果列表响应
export type ApiResponseListGradingResultVO = ApiResponse<GradingResultVO[]>
