import { useAuthStore } from '@/store/auth'
import type { Directive } from 'vue'

const permission: Directive = {
  mounted(el, binding) {
    const auth = useAuthStore()
    const permissions: string[] = auth.user?.role?.permissions?.map((p: any) => p.code) ?? []
    const required = binding.value
    if (required && !permissions.includes(required)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export default permission
