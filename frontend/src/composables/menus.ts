import { AudioFileOutlined, BookOutlined, PlayCircleOutlineOutlined } from '@vicons/material'
import { computed, ref } from 'vue'

import { useAuthStore } from '@/store/auth'

export interface Menu {
  id: string
  label: string
  icon?: string
  name?: string
  params?: { [key: string]: string }
  children?: Menu[]
  meta?: {
    permission?: string
  }
}

export const staticMenus: Menu[] = [
  {
    id: 'dashboard',
    label: '仪表盘',
    icon: 'dashboard',
    name: 'home'
  },
  {
    id: 'user',
    label: '用户管理',
    icon: 'users',
    name: 'users',
    meta: { permission: 'user:list:all' }
  },
  {
    id: 'role',
    label: '角色管理',
    icon: 'role',
    name: 'roles',
    meta: { permission: 'role:list:all' }
  },
  {
    id: 'permission',
    label: '权限管理',
    icon: 'permission',
    name: 'permissions',
    meta: { permission: 'permission:list:all' }
  },
  {
    id: 'category',
    label: '分类管理',
    icon: 'category',
    name: 'categories',
    meta: { permission: 'category:list:all' }
  },
  {
    id: 'course',
    label: '课程管理',
    icon: 'materialSchool',
    children: [
      { id: 'course-list', label: '全部课程', name: 'courses', meta: { permission: 'course:list:all' } },
      { id: 'course-self', label: '我的课程', name: 'course-self', meta: { permission: 'course:list:self' } }
    ]
  },
  {
    id: 'knowledge-points',
    label: '知识点管理',
    icon: 'materialCategory',
    children: [
      { id: 'knowledge-points-list', label: '全部知识点', name: 'knowledge-points', meta: { permission: 'knowledge_point:list:all' } },
      { id: 'knowledge-points-self', label: '我的知识点', name: 'knowledge-points-self', meta: { permission: 'knowledge_point:list:self' } }
    ]
  },
  {
    id: 'question',
    label: '题目管理',
    icon: 'order',
    children: [
      { id: 'question-list', label: '题目列表', name: 'questions', meta: { permission: 'question:list:all' } },
      { id: 'question-self', label: '我的题目', name: 'question-self', meta: { permission: 'question:list:self' } },
      { id: 'question-generate', label: 'AI生成题目', name: 'question-generate', meta: { permission: 'ai:generate:question' } }
    ]
  },
  {
    label: '试卷管理',
    id: 'papers',
    icon: 'materialDocument',
    name: 'papers',
    meta: {
      permission: 'paper:list:all'
    },
  },
  {
    label: '练习管理',
    id: 'practice',
    icon: 'materialQuiz',
    children: [
      { id: 'practice-list', label: '练习列表', name: 'practices', meta: { permission: 'practice:list:self' } },
      { id: 'practice-statistics', label: '练习统计', name: 'PracticeStatistics', meta: { permission: 'practice:statistics:self' } },
      {
        id: 'practice-records',
        label: '练习记录',
        name: 'PracticeRecords',
        meta: { permission: 'practice:result:view:all' }
      },
      {
        label: '判分审核',
        id: 'grading',
        name: 'grading',
        meta: { permission: 'grading:review:list' }
      }
    ]
  }
]

export const useMenus = () => {
  const auth = useAuthStore()
  const menus = ref<Menu[]>(staticMenus)
  function filterMenus(items: Menu[], permissions: string[]): Menu[] {
    return items
      .map(item => {
        const filteredChildren = item.children ? filterMenus(item.children, permissions) : undefined
        const perm = item.meta?.permission
        const hasPermission = !perm || permissions.includes(perm)
        if (item.children && (!filteredChildren || filteredChildren.length === 0)) {
          return null
        }
        if (!hasPermission) return null
        return { ...item, children: filteredChildren }
      })
      .filter(Boolean) as Menu[]
  }

  return computed(() => {
    const permissions: string[] = auth.user?.role?.permissions?.map((p: any) => p.code) ?? []
    return filterMenus(menus.value, permissions)
  })
}
