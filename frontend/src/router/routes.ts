import { type RouteRecordRaw } from 'vue-router'

const mainRoutes: RouteRecordRaw[] = [
  {
    name: 'home',
    path: '/',
    component: () => import('@/views/dashboard/home.vue'),
    meta: {
      title: '仪表盘',
      requiresAuth: true
    }
  },
  {
    name: 'profile',
    path: '/profile',
    component: () => import('@/views/system/profile.vue'),
    meta: {
      title: '个人中心',
      requiresAuth: true,
      permission: 'user:view:self'
    }
  },
  {
    name: 'users',
    path: '/users/:page?',
    component: () => import('@/views/system/users.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      permission: 'user:list:all'
    }
  },
  {
    name: 'roles',
    path: '/roles',
    component: () => import('@/views/system/roles.vue'),
    meta: {
      title: '角色管理',
      requiresAuth: true,
      permission: 'role:list:all'
    }
  },
  {
    name: 'permissions',
    path: '/permissions',
    component: () => import('@/views/system/permissions.vue'),
    meta: {
      title: '权限管理',
      requiresAuth: true,
      permission: 'permission:list:all'
    }
  },
  {
    name: 'categories',
    path: '/categories',
    component: () => import('@/views/learning/categories.vue'),
    meta: {
      title: '全部分类',
      requiresAuth: true,
      permission: 'category:list:all'
    }
  },
  {
    name: 'courses',
    path: '/courses',
    component: () => import('@/views/learning/courses.vue'),
    meta: {
      title: '全部课程',
      requiresAuth: true,
      permission: 'course:list:all'
    }
  },
  {
    name: 'course-self',
    path: '/course-self',
    component: () => import('@/views/learning/course-own.vue'),
    meta: {
      title: '我的课程',
      requiresAuth: true,
      permission: 'course:list:self'
    }
  },
  {
    name: 'questions',
    path: '/questions',
    component: () => import('@/views/question/questions.vue'),
    meta: {
      title: '题目列表',
      requiresAuth: true,
      permission: 'question:list:all'
    }
  },
  {
    name: 'question-self',
    path: '/question-self',
    component: () => import('@/views/question/question-self.vue'),
    meta: {
      title: '我的题目',
      requiresAuth: true,
      permission: 'question:list:self'
    }
  },
  {
    name: 'question-generate',
    path: '/question-generate',
    component: () => import('@/views/question/question-generate.vue'),
    meta: {
      title: '智能题目生成',
      requiresAuth: true,
      permission: 'ai:generate:question'
    }
  },
  {
    name: 'knowledge-points',
    path: '/knowledge-points',
    component: () => import('@/views/learning/knowledge-points.vue'),
    meta: {
      title: '全部知识点',
      requiresAuth: true,
      permission: 'knowledge_point:list:all'
    }
  },
  {
    name: 'knowledge-points-self',
    path: '/knowledge-points-self',
    component: () => import('@/views/learning/knowledge-points-self.vue'),
    meta: {
      title: '我的知识点',
      requiresAuth: true,
      permission: 'knowledge_point:list:self'
    }
  },
  {
    name: 'practices',
    path: '/practices',
    component: () => import('@/views/practice/practices.vue'),
    meta: {
      title: '练习管理',
      requiresAuth: true
    }
  },
  // 判分审核
  {
    name: 'grading',
    path: '/grading',
    component: () => import('@/views/practice/grading.vue'),
    meta: {
      title: '判分审核',
      requiresAuth: true
    }
  },
  // 试卷管理
  {
    name: 'papers',
    path: '/papers',
    component: () => import('@/views/practice/papers.vue'),
    meta: {
      title: '试卷管理',
      requiresAuth: true
    }
  },
  {
    path: '/practice-statistics',
    name: 'PracticeStatistics',
    component: () => import('@/views/practice/practice-statistics.vue'),
    meta: {
      title: '练习统计',
      requiresAuth: true
    },
  },
  {
    path: 'records',
    name: 'PracticeRecords',
    meta: {
      title: '练习记录',
      requiresAuth: true
    },
    component: () => import('@/views/practice/practice-records.vue'),
  },
  {
    name: 'activities',
    path: '/activities',
    component: () => import('@/views/activity/activities.vue'),
    meta: {
      title: '活动记录',
      requiresAuth: true
    }
  },
  {
    name: 'forbidden',
    path: '/forbidden',
    component: () => import('@/views/errors/forbidden.vue'),
    meta: {
      title: 'Oops!'
    }
  },
]

const routes: RouteRecordRaw[] = [
  {
    name: 'login',
    path: '/login',
    component: () => import('@/views/auth/login.vue'),
    meta: {
      title: '登录'
    }
  },
  {
    name: 'register',
    path: '/register',
    component: () => import('@/views/auth/register.vue'),
    meta: {
      title: '注册'
    }
  },
  {
    name: 'forgot',
    path: '/forgot',
    component: () => import('@/views/auth/forgot.vue'),
    meta: {
      title: '忘记密码'
    }
  },
  {
    name: 'layout',
    path: '/',
    component: () => import('../layouts/index.vue'),
    children: [...mainRoutes]
  },
  {
    name: 'not-found',
    path: '/:path*',
    component: () => import('@/views/errors/error.vue'),
    meta: {
      title: 'Oh no!'
    }
  }
]

export default routes
