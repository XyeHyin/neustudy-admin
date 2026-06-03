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

export function getEnabledStatusType(enabled: boolean): TagType {
  return enabled ? 'success' : 'warning'
}

export function getEnabledStatusText(enabled: boolean): string {
  return enabled ? '启用' : '禁用'
}
