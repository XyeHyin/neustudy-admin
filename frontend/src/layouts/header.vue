<template>
  <n-layout-header bordered>
    <n-button text @click="router.go(0)">
      <icon type="refresh" size="20" :depth="2" />
    </n-button>
    <n-breadcrumb>
      <n-breadcrumb-item v-for="(item, idx) in breadcrumbs" :key="idx">
        <RouterLink v-if="item.to" :to="item.to">{{ item.label }}</RouterLink>
        <span v-else>{{ item.label }}</span>
      </n-breadcrumb-item>
    </n-breadcrumb>
    <n-space :size="20" align="center" style="line-height: 1">
      <n-tooltip>
        <template #trigger>
          <n-a href="https://www.neusoft.edu.cn/" target="_blank">
            <icon type="help" size="22" :depth="2" />
          </n-a>
        </template>
        关于我们
      </n-tooltip>
      <n-tooltip>
        <template #trigger>
          <n-a href="https://gitee.com" target="_blank">
            <icon type="gitee" size="22" :depth="2" />
          </n-a>
        </template>
        查看Gitee仓库
      </n-tooltip>
      <!-- 主题切换按钮 -->
      <n-tooltip>
        <template #trigger>
          <n-button text @click="themeStore.toggleTheme()">
            <icon :type="themeIcon" :key="themeIcon" size="22" :depth="2" />
          </n-button>
        </template>
        {{ themeTooltip }}
      </n-tooltip>
      <n-popover trigger="click" placement="bottom-end" :width="300">
        <template #trigger>
          <n-badge dot processing>
            <icon type="notifications" size="22" :depth="2" />
          </n-badge>
        </template>
        <n-tabs type="line" justify-content="space-evenly" style="--pane-padding: 0">
          <n-tab-pane name="notifications" tab="通知">
            <n-list style="margin: 0">
              <n-list-item v-for="notification in notifications" :key="notification.id">
                {{ notification.content }} <span style="float: right">{{ notification.time }}</span>
              </n-list-item>
            </n-list>
          </n-tab-pane>
          <n-tab-pane name="messages" tab="消息">
            <n-list style="margin: 0">
              <n-list-item v-for="message in messages" :key="message.id">
                {{ message.content }} <span style="float: right">{{ message.time }}</span>
              </n-list-item>
            </n-list>
          </n-tab-pane>
        </n-tabs>
      </n-popover>
      <n-dropdown placement="bottom-end" show-arrow :options="options" @select="handleOptionsSelect">
        <n-avatar v-if="user.avatar" size="small" round :src="user.avatar" />
        <n-avatar v-else size="small" round>{{ user.nickname.substring(0, 1) }}</n-avatar>
      </n-dropdown>
    </n-space>
  </n-layout-header>
</template>

<script lang="ts" setup>
import { useDialog, useMessage, useOsTheme } from 'naive-ui'
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/store/auth'
import { useThemeStore } from '@/store/theme'

import { Icon } from '../components'
import { useMenus } from '../composables/menus'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const auth = useAuthStore()
const themeStore = useThemeStore()
const osTheme = useOsTheme()

const user = auth.user
const menus = useMenus()

// 主题相关计算属性
const currentTheme = computed(() => {
  if (themeStore.themeMode === 'dark') {
    return 'dark'
  } else if (themeStore.themeMode === 'light') {
    return 'light'
  }
  return osTheme.value === 'dark' ? 'dark' : 'light'
})

const themeIcon = computed(() => {
  return currentTheme.value === 'dark' ? 'materialWeatherSunny' : 'moon'
})

const themeTooltip = computed(() => {
  return currentTheme.value === 'dark' ? '切换到明亮模式' : '切换到暗黑模式'
})

// 新增：通知和消息数据
const notifications = ref([
  { id: 1, content: '欢迎登录NEU智教通！', time: '刚刚' },
  { id: 2, content: '您的资料已完善。', time: '1小时前' },
  { id: 3, content: '有新题目待处理。', time: '昨天' }
])
const messages = ref([
  { id: 1, content: '管理员：请及时更新课程信息。', time: '2分钟前' },
  { id: 2, content: '系统：本周将进行系统维护。', time: '3天前' }
])

function findMenuPathByName(menus: any[], name: string, path: any[] = []): any[] | null {
  for (const menu of menus) {
    const newPath = [...path, menu]
    if (menu.name === name) {
      return newPath
    }
    if (menu.children) {
      const found = findMenuPathByName(menu.children, name, newPath)
      if (found) return found
    }
  }
  return null
}

const breadcrumbs = computed(() => {
  const path = findMenuPathByName(menus.value, route.name as string)
  if (!path) return []
  return path.map((menu, idx) => ({
    label: menu.label,
    to: idx < path.length - 1 && menu.name ? { name: menu.name } : null
  }))
})

const options = computed(() => [
  {
    key: 'main',
    type: 'group',
    label: `Hey, ${user.nickname}!`,
    children: [
      {
        key: 'profile',
        label: '个人中心'
      },
      { key: 'divider', type: 'divider' },
      {
        key: 'logout',
        label: '退出登录'
      }
    ]
  }
])

const handleLogout = async (): Promise<void> => {
  dialog.warning({
    title: '退出登录',
    content: '确定要退出登录吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      auth.logout()
      message.success('退出登录成功')
      router.push('/login')
    }
  })
}

const handleOptionsSelect = async (key: unknown): Promise<void> => {
  switch (key) {
    case 'profile':
      router.push('/profile')
      break
    case 'divider':
      break
    case 'logout':
      await handleLogout()
      break
    default:
      message.error('未知操作')
  }
}
</script>

<style scoped>
.n-layout-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  padding: 9px 18px;
}

.n-button {
  margin-right: 15px;
}

.n-space {
  margin-left: auto;
}
</style>
