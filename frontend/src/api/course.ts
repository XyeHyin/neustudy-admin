import request from './request'

import type { ApiResponse, CourseDetailVO, CourseStatisticsVO, CourseVO, CreateCourseDTO, PageResultCourseVO, UpdateCourseDTO } from './types'

// 获取所有课程
export function getCourses() {
  return request<ApiResponse<CourseVO[]>>({
    url: '/courses',
    method: 'GET'
  })
}

// 分页获取所有课程
export function getCoursePage(params: { page?: number; size?: number; keyword?: string; subject?: string; grade?: string; status?: string; enabled?: boolean }) {
  return request<ApiResponse<PageResultCourseVO>>({
    url: '/courses/page',
    method: 'GET',
    params
  })
}

// 获取我的课程
export function getMyCourses() {
  return request<ApiResponse<CourseVO[]>>({
    url: '/courses/my',
    method: 'GET'
  })
}

// 分页获取我的课程
export function getMyCoursePage(params: { page?: number; size?: number; keyword?: string; subject?: string; grade?: string; status?: string; enabled?: boolean }) {
  return request<ApiResponse<PageResultCourseVO>>({
    url: '/courses/my/page',
    method: 'GET',
    params
  })
}

// 获取课程详情
export function getCourseDetail(courseId: number) {
  return request<ApiResponse<CourseDetailVO>>({
    url: `/courses/${courseId}`,
    method: 'GET'
  })
}

// 创建课程
export function createCourse(data: CreateCourseDTO) {
  return request<ApiResponse<CourseVO>>({
    url: '/courses',
    method: 'POST',
    data
  })
}

// 管理员创建课程
export function adminCreateCourse(data: CreateCourseDTO, teacherId: number) {
  return request<ApiResponse<CourseVO>>({
    url: '/courses/admin',
    method: 'POST',
    data,
    params: { teacherId }
  })
}

// 更新课程
export function updateCourse(courseId: number, data: UpdateCourseDTO) {
  return request<ApiResponse<CourseVO>>({
    url: `/courses/${courseId}`,
    method: 'PUT',
    data
  })
}

// 管理员更新课程
export function adminUpdateCourse(courseId: number, data: UpdateCourseDTO) {
  return request<ApiResponse<CourseVO>>({
    url: `/courses/admin/${courseId}`,
    method: 'PUT',
    data
  })
}

// 删除课程
export function deleteCourse(courseId: number) {
  return request<ApiResponse<boolean>>({
    url: `/courses/${courseId}`,
    method: 'DELETE'
  })
}

// 管理员删除课程
export function adminDeleteCourse(courseId: number) {
  return request<ApiResponse<boolean>>({
    url: `/courses/admin/${courseId}`,
    method: 'DELETE'
  })
}

// 批量删除课程
export function batchDeleteCourses(ids: number[]) {
  return request<ApiResponse<boolean>>({
    url: '/courses/batch',
    method: 'DELETE',
    data: ids
  })
}

// 发布课程
export function publishCourse(courseId: number) {
  return request<ApiResponse<CourseVO>>({
    url: `/courses/${courseId}/publish`,
    method: 'PUT'
  })
}

// 更新课程进度
export function updateCourseProgress(courseId: number, completedHours: number) {
  return request<ApiResponse<CourseVO>>({
    url: `/courses/${courseId}/progress`,
    method: 'PUT',
    params: { completedHours }
  })
}

// 归档课程
export function archiveCourse(courseId: number) {
  return request<ApiResponse<CourseVO>>({
    url: `/courses/${courseId}/archive`,
    method: 'PUT'
  })
}

// 获取学科列表 - 修正返回类型为字符串数组
export function getSubjects() {
  return request<ApiResponse<string[]>>({
    url: '/courses/subjects',
    method: 'GET'
  })
}

// 获取年级列表 - 修正返回类型为字符串数组
export function getGrades() {
  return request<ApiResponse<string[]>>({
    url: '/courses/grades',
    method: 'GET'
  })
}

// 获取课程统计 - 修正返回类型
export function getCourseStatistics() {
  return request<ApiResponse<CourseStatisticsVO>>({
    url: '/courses/statistics',
    method: 'GET'
  })
}
