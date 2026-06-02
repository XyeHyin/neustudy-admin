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
import { computed } from 'vue'

import { useThemeStore } from '@/store/theme'

const osTheme = useOsTheme()
const themeStore = useThemeStore()

const theme = computed(() => {
  // 如果用户手动设置了主题，使用用户设置
  if (themeStore.themeMode === 'dark') {
    return darkTheme
  } else if (themeStore.themeMode === 'light') {
    return null
  }
  // 否则跟随系统主题
  return osTheme.value === 'dark' ? darkTheme : null
})
</script>
