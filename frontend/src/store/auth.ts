import { defineStore } from 'pinia'
import { ref } from 'vue'

export function isJwtToken(token: unknown): token is string {
    if (typeof token !== 'string') return false
    const normalized = token.trim()
    if (!normalized || normalized === 'undefined' || normalized === 'null') return false
    const parts = normalized.split('.')
    return parts.length === 3 && parts.every(Boolean)
}

function normalizeToken(newToken: string | null | undefined) {
    return isJwtToken(newToken) ? newToken.trim() : null
}

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(null)
    const user = ref<any>(null)

    function login(newToken: string | null | undefined, newUser: any) {
        token.value = normalizeToken(newToken)
        user.value = newUser
    }

    function setToken(newToken: string | null | undefined) {
        token.value = normalizeToken(newToken)
    }
    function setUser(newUser: any) {
        user.value = newUser
    }

    function logout() {
        token.value = null
        user.value = null
    }

    function hasPermission(permission: string): boolean {
        const perms = user.value?.role?.permissions?.map((p: any) => p.code) ?? []
        return perms.includes(permission)
    }

    return { token, user, login, logout, setToken, setUser, hasPermission }
}, {
    persist: true
})
