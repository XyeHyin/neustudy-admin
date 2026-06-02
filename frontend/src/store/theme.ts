import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 从本地存储读取主题设置，默认为null（跟随系统）
  const getStoredTheme = (): 'light' | 'dark' | null => {
    const stored = localStorage.getItem('theme-mode')
    if (stored === 'light' || stored === 'dark') {
      return stored
    }
    return null // 跟随系统
  }

  const themeMode = ref<'light' | 'dark' | null>(getStoredTheme())

  // 设置主题模式
  const setThemeMode = (mode: 'light' | 'dark' | null) => {
    themeMode.value = mode
    if (mode === null) {
      localStorage.removeItem('theme-mode')
    } else {
      localStorage.setItem('theme-mode', mode)
    }
  }

  // 切换主题模式
  const toggleTheme = () => {
    if (themeMode.value === null || themeMode.value === 'light') {
      setThemeMode('dark')
    } else {
      setThemeMode('light')
    }
  }

  return {
    themeMode,
    setThemeMode,
    toggleTheme
  }
})
