import request from './request';



import type { ApiResponse, ApiResponseListPracticePaperStatVO, ApiResponsePageResultPracticeRecordVO, PracticeDetailVO, PracticeMarkDTO, PracticeOverviewVO, PracticeRecordVO, PracticeResultVO, PracticeSessionVO, PracticeStartDTO, PracticeSubmitDTO } from './types';


// 开始练习
export function startPractice(data: PracticeStartDTO) {
  return request<ApiResponse<PracticeSessionVO>>({
    url: '/practice/start',
    method: 'POST',
    data
  })
}

// 获取练习会话详情
export function getPracticeSession(id: number) {
  return request<ApiResponse<PracticeSessionVO>>({
    url: `/practice/${id}`,
    method: 'GET'
  })
}

// 提交练习答案（提交本次练习所有答题并自动判分）
export function submitPractice(data: PracticeSubmitDTO) {
  return request<ApiResponse<PracticeResultVO>>({
    url: '/practice/submit',
    method: 'POST',
    data
  })
}

// 获取练习结果
export function getPracticeResult(id: number) {
  return request<ApiResponse<PracticeResultVO>>({
    url: `/practice/${id}/result`,
    method: 'GET'
  })
}

// 获取练习进度概览
export function getPracticeOverview(id: number) {
  return request<ApiResponse<PracticeOverviewVO>>({
    url: `/practice/${id}/overview`,
    method: 'GET'
  })
}

// 查询练习记录（分页）
export function listPracticeRecords(params: { studentId?: number; paperId?: number; submitted?: boolean; page?: number; size?: number }) {
  return request<ApiResponsePageResultPracticeRecordVO>({
    url: '/practice/records',
    method: 'GET',
    params
  })
}

// 题目标记（如收藏、疑问等）
export function markPracticeQuestion(data: PracticeMarkDTO) {
  return request<ApiResponse<void>>({
    url: '/practice/mark',
    method: 'POST',
    data
  })
}

// 获取练习题目详情
export function getPracticeDetail(id: number) {
  return request<ApiResponse<PracticeDetailVO>>({
    url: `/practice/${id}/questions`,
    method: 'GET'
  })
}

// 获取学生试卷练习统计
export function getPracticeStatistics(studentId: number, paperId: number) {
  return request<ApiResponse<PracticeRecordVO>>({
    url: `/practice/statistics/${studentId}/${paperId}`,
    method: 'GET'
  })
}

// 获取当前用户的练习会话列表
export function getMyPracticeSessions(params: { paperId?: number; submitted?: boolean; page?: number; size?: number }) {
  return request<ApiResponsePageResultPracticeRecordVO>({
    url: '/practice/my-sessions',
    method: 'GET',
    params
  })
}

// 获取学生练习历史
export function getPracticeHistory(studentId: number) {
  return request<ApiResponseListPracticePaperStatVO>({
    url: `/practice/history/${studentId}`,
    method: 'GET'
  })
}

// 获取所有可用试卷及用户练习统计
export function getAvailablePapersWithStats() {
  return request<ApiResponseListPracticePaperStatVO>({
    url: '/practice/available-papers',
    method: 'GET'
  })
}