<template>
 <n-config-provider
    :key="themeStore.themeMode ?? 'default'"
    :theme="theme"
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-global-style />
    <n-loading-bar-provider>
      <n-message-provider>
        <n-dialog-provider>
          <n-modal-provider>
            <slot />
          </n-modal-provider>
        </n-dialog-provider>
      </n-message-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<script lang="ts" setup>
import { darkTheme, dateZhCN, NConfigProvider, useOsTheme, zhCN } from 'naive-ui'
import { computed, watchEffect } from 'vue'

import { useThemeStore } from '@/store/theme'

const osTheme = useOsTheme()
const themeStore = useThemeStore()

const isDarkTheme = computed(() => {
  if (themeStore.themeMode === 'dark') {
    return true
  }
  if (themeStore.themeMode === 'light') {
    return false
  }
  return osTheme.value === 'dark'
})

const theme = computed(() => (isDarkTheme.value ? darkTheme : null))

watchEffect(() => {
  document.documentElement.dataset.theme = isDarkTheme.value ? 'dark' : 'light'
})
</script>
