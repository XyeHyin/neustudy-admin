import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSidebarStore = defineStore('sidebar', () => {
  const collapsed = ref(false)
  function toggle() {
    collapsed.value = !collapsed.value
  }
  return { collapsed, toggle }
}, {
  persist: true
})
