import type { TagProps } from 'naive-ui'

type TagType = NonNullable<TagProps['type']>

const COURSE_STATUS_TYPE: Record<string, TagType> = {
  PUBLISHED: 'success',
  ARCHIVED: 'warning',
  COMPLETED: 'info'
}

const COURSE_STATUS_TEXT: Record<string, string> = {
  PUBLISHED: '已发布',
  ARCHIVED: '已归档',
  COMPLETED: '已完成'
}

const PAPER_STATUS_TYPE: Record<string, TagType> = {
  PUBLISHED: 'success',
  ARCHIVED: 'warning'
}

const PAPER_STATUS_TEXT: Record<string, string> = {
  PUBLISHED: '已发布',
  ARCHIVED: '已归档'
}

const DIFFICULTY_TYPE: Record<string, TagType> = {
  EASY: 'success',
  MEDIUM: 'warning',
  HARD: 'error'
}

const DIFFICULTY_TEXT: Record<string, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难'
}

export function getCourseStatusType(status?: string): TagType {
  return status ? COURSE_STATUS_TYPE[status] ?? 'default' : 'default'
}

export function getCourseStatusText(status?: string): string {
  return status ? COURSE_STATUS_TEXT[status] ?? '草稿' : '草稿'
}

export function getPaperStatusType(status?: string): TagType {
  return status ? PAPER_STATUS_TYPE[status] ?? 'default' : 'default'
}

export function getPaperStatusText(status?: string): string {
  return status ? PAPER_STATUS_TEXT[status] ?? '草稿' : '草稿'
}

export function getDifficultyStatusType(difficulty?: string): TagType {
  return difficulty ? DIFFICULTY_TYPE[difficulty] ?? 'default' : 'default'
}

export function getDifficultyStatusText(difficulty?: string): string {
  return difficulty ? DIFFICULTY_TEXT[difficulty] ?? '未知' : '未知'
}

export function getEnabledStatusType(enabled: boolean): TagType {
  return enabled ? 'success' : 'warning'
}

export function getEnabledStatusText(enabled: boolean): string {
  return enabled ? '启用' : '禁用'
}
