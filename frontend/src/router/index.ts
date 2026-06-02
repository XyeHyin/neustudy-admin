import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { isJwtToken, useAuthStore } from '@/store/auth'

const history = createWebHistory()
const router = createRouter({ history, routes })

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  const isLogin = isJwtToken(auth.token)

  if (auth.token && !isLogin) {
    auth.logout()
  }

  if (to.meta.requiresAuth) {
    if (!isLogin) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
    const requiredPermission = to.meta.permission as string | undefined
    if (requiredPermission && !auth.hasPermission(requiredPermission)) {
      return next({ name: 'forbidden', replace: true })
    }
  }
  next()
})

router.afterEach(to => {
  const items = [import.meta.env.VITE_TITLE]
  to.meta.title != null && items.unshift(to.meta.title)
  document.title = items.join(' · ')
})

export default router
