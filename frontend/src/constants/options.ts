export type SelectOption<T = string | number | boolean | null> = {
  label: string
  value: T
}

export const ALL_OPTION: SelectOption<null> = { label: '全部', value: null }

export const DIFFICULTY_OPTIONS: SelectOption<string>[] = [
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

export const DIFFICULTY_FILTER_OPTIONS: SelectOption<string | null>[] = [ALL_OPTION, ...DIFFICULTY_OPTIONS]

export const ENABLED_OPTIONS: SelectOption<boolean>[] = [
  { label: '启用', value: true },
  { label: '禁用', value: false }
]

export const ENABLED_FILTER_OPTIONS: SelectOption<boolean | null>[] = [ALL_OPTION, ...ENABLED_OPTIONS]

export const SUBMITTED_FILTER_OPTIONS: SelectOption<boolean | null>[] = [
  ALL_OPTION,
  { label: '已提交', value: true },
  { label: '未提交', value: false }
]

export const COURSE_STATUS_FILTER_OPTIONS: SelectOption<string | null>[] = [
  ALL_OPTION,
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已归档', value: 'ARCHIVED' }
]

export const PAPER_STATUS_FILTER_OPTIONS: SelectOption<string | null>[] = [
  ALL_OPTION,
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

export const PAPER_QUESTION_ORDER_OPTIONS: SelectOption<string>[] = [
  { label: '固定顺序', value: 'FIXED' },
  { label: '随机顺序', value: 'RANDOM' }
]

export const SEMESTER_OPTIONS: SelectOption<string>[] = [
  { label: '第一学期', value: '第一学期' },
  { label: '第二学期', value: '第二学期' },
  { label: '第三学期', value: '第三学期' }
]
