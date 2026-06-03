import { DIFFICULTY_OPTIONS, ENABLED_OPTIONS } from './options'

export const QUESTION_TYPE_OPTIONS = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '填空题', value: 'FILL_BLANK' },
  { label: '简答题', value: 'SHORT_ANSWER' },
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '论述题', value: 'ESSAY' }
]

export const QUESTION_DIFFICULTY_OPTIONS = DIFFICULTY_OPTIONS

export const QUESTION_ENABLED_OPTIONS = ENABLED_OPTIONS
