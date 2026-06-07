<template>
  <n-layout-header bordered>
    <n-button class="header-action" text @click="router.go(0)">
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
          <n-a href="https://github.com/XyeHyin/neustudy-admin" target="_blank">
            <icon type="github" size="22" :depth="2" />
          </n-a>
        </template>
        查看 GitHub 仓库
      </n-tooltip>
      <n-tooltip>
        <template #trigger>
          <n-button class="header-action" text @click="themeStore.toggleTheme()">
            <icon :type="themeIcon" :key="themeIcon" size="22" :depth="2" />
          </n-button>
        </template>
        {{ themeTooltip }}
      </n-tooltip>

      <n-popover trigger="hover" placement="bottom-end" :width="360" :show-arrow="false">
        <template #trigger>
          <n-badge :value="badgeValue" :max="99" :show="hasUnread">
            <n-button class="header-action notification-trigger" text :aria-label="notificationLabel">
              <icon type="notifications" size="22" :depth="2" />
            </n-button>
          </n-badge>
        </template>

        <div class="notification-panel">
          <div class="notification-panel__header">
            <div>
              <div class="notification-panel__title">消息通知</div>
              <div class="notification-panel__meta">
                {{ streamConnected ? '实时连接中' : '展示最近动态' }}
              </div>
            </div>
            <n-button text size="small" :disabled="!hasUnread" @click="handleMarkAllRead">
              全部已读
            </n-button>
          </div>

          <n-spin :show="loading">
            <n-empty v-if="!previewNotifications.length" description="暂无消息" size="small" />
            <n-list v-else class="notification-list">
              <transition-group name="notice-list" tag="div">
                <n-list-item
                  v-for="(notification, index) in previewNotifications"
                  :key="notification.eventLogId"
                  :style="{ '--i': index }"
                  class="notification-list__item"
                  @click="handleNotificationClick(notification.eventLogId)"
                >
                  <div class="notification-row" :class="{ 'notification-row--unread': !notification.read }">
                    <span v-if="!notification.read" class="notification-row__dot" />
                    <div class="notification-row__body">
                      <div class="notification-row__top">
                        <span class="notification-row__title">{{ notification.title }}</span>
                        <span class="notification-row__time">{{ formatRelativeTime(notification.createTime) }}</span>
                      </div>
                      <div class="notification-row__content">{{ notification.content }}</div>
                    </div>
                  </div>
                </n-list-item>
              </transition-group>
            </n-list>
          </n-spin>

          <div class="notification-panel__footer">
            <n-button text size="small" @click="openNotificationDialog">查看全部消息</n-button>
          </div>
        </div>
      </n-popover>

      <n-dropdown placement="bottom-end" show-arrow :options="options" @select="handleOptionsSelect">
        <n-avatar v-if="user.avatar" size="small" round :src="user.avatar" />
        <n-avatar v-else size="small" round>{{ user.nickname.substring(0, 1) }}</n-avatar>
      </n-dropdown>
    </n-space>

    <n-modal
      v-model:show="notificationDialogVisible"
      preset="card"
      title="全部消息"
      class="notification-dialog"
      :bordered="false"
      :style="{ width: '720px', maxWidth: 'calc(100vw - 32px)' }"
    >
      <div class="notification-dialog__toolbar">
        <span>未读 {{ unreadCount > 99 ? '99+' : unreadCount }} 条</span>
        <n-space>
          <n-button size="small" secondary @click="router.push('/activities')">查看活动记录</n-button>
          <n-button size="small" type="primary" :disabled="!hasUnread" @click="handleMarkAllRead">
            全部已读
          </n-button>
        </n-space>
      </div>

      <n-scrollbar class="notification-dialog__body">
        <n-empty v-if="!notifications.length" description="暂无消息" />
        <n-list v-else class="notification-list notification-list--dialog">
          <transition-group name="notice-list" tag="div">
            <n-list-item
              v-for="(notification, index) in notifications"
              :key="notification.eventLogId"
              :style="{ '--i': Math.min(index, 8) }"
              class="notification-list__item"
              @click="handleNotificationClick(notification.eventLogId)"
            >
              <div class="notification-row" :class="{ 'notification-row--unread': !notification.read }">
                <span v-if="!notification.read" class="notification-row__dot" />
                <div class="notification-row__body">
                  <div class="notification-row__top">
                    <span class="notification-row__title">{{ notification.title }}</span>
                    <span class="notification-row__time">{{ formatRelativeTime(notification.createTime) }}</span>
                  </div>
                  <div class="notification-row__content">{{ notification.content }}</div>
                  <div class="notification-row__path">{{ notification.uri }}</div>
                </div>
              </div>
            </n-list-item>
          </transition-group>
        </n-list>
      </n-scrollbar>
    </n-modal>
  </n-layout-header>
</template>

<script lang="ts" setup>
import { useDialog, useMessage, useOsTheme } from 'naive-ui'
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/store/auth'
import { useThemeStore } from '@/store/theme'
import { useNotifications } from '@/composables/useNotifications'

import { Icon } from '../components'
import { useMenus } from '../composables/menus'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const auth = useAuthStore()
const themeStore = useThemeStore()
const osTheme = useOsTheme()

const user = computed(() => auth.user ?? { nickname: '用户', avatar: '' })
const menus = useMenus()
const notificationDialogVisible = ref(false)
const {
  notifications,
  unreadCount,
  loading,
  hasUnread,
  streamConnected,
  markRead,
  markAllRead,
  start
} = useNotifications(50)

const previewNotifications = computed(() => notifications.value.slice(0, 5))
const badgeValue = computed(() => (unreadCount.value > 99 ? '99+' : unreadCount.value))
const notificationLabel = computed(() =>
  unreadCount.value > 0 ? `消息通知，${badgeValue.value} 条未读` : '消息通知'
)

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
    label: `Hey, ${user.value.nickname}!`,
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

function formatRelativeTime(time: string) {
  const date = new Date(time)
  const diffMs = Date.now() - date.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  if (!Number.isFinite(diffMinutes) || diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}小时前`

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString('zh-CN')
}

async function handleNotificationClick(eventLogId: number) {
  try {
    await markRead(eventLogId)
  } catch (error) {
    message.error('标记消息已读失败')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllRead()
  } catch (error) {
    message.error('标记全部已读失败')
  }
}

function openNotificationDialog() {
  notificationDialogVisible.value = true
}

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

onMounted(() => {
  start()
})
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

.header-action {
  margin-right: 15px;
}

.notification-trigger {
  margin-right: 0;
}

.n-space {
  margin-left: auto;
}

.header-action :deep(.n-button__icon),
.n-a :deep(.n-icon),
.n-badge :deep(.n-icon) {
  transition: transform var(--motion-duration-base) var(--motion-ease-out-quint);
}

.header-action:hover :deep(.n-button__icon),
.n-a:hover :deep(.n-icon),
.n-badge:hover :deep(.n-icon) {
  transform: rotate(-8deg) scale(1.08);
}

.notification-panel {
  padding: 4px 0;
}

.notification-panel__header,
.notification-panel__footer,
.notification-dialog__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notification-panel__header {
  padding: 4px 4px 10px;
}

.notification-panel__title {
  font-size: 15px;
  font-weight: 650;
  line-height: 1.4;
}

.notification-panel__meta {
  margin-top: 2px;
  color: var(--text-color-3);
  font-size: 12px;
}

.notification-list {
  margin: 0;
}

.notification-list__item {
  cursor: pointer;
  --notice-delay: calc(min(var(--i), 8) * 28ms);
}

.notification-list :deep(.n-list-item) {
  padding: 0;
}

.notification-row {
  position: relative;
  display: flex;
  gap: 10px;
  min-height: 68px;
  padding: 10px 8px 10px 10px;
  border-radius: 8px;
  transition:
    background-color 180ms var(--motion-ease-out-quint),
    transform 180ms var(--motion-ease-out-quint);
}

.notification-row:hover {
  background: var(--hover-color, rgba(24, 160, 88, 0.08));
  transform: translateX(2px);
}

.notification-row--unread {
  background: color-mix(in srgb, var(--primary-color, var(--brand-link)) 9%, transparent);
}

.notification-row__dot {
  width: 7px;
  height: 7px;
  flex: 0 0 7px;
  margin-top: 8px;
  border-radius: 999px;
  background: var(--primary-color, var(--brand-link));
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--primary-color, var(--brand-link)) 14%, transparent);
  animation: notice-dot 1200ms var(--motion-ease-out-quint) infinite;
}

.notification-row__body {
  min-width: 0;
  flex: 1;
}

.notification-row__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.notification-row__title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-color-1);
  font-size: 13px;
  font-weight: 600;
}

.notification-row__time {
  flex: 0 0 auto;
  color: var(--text-color-3);
  font-size: 12px;
}

.notification-row__content,
.notification-row__path {
  margin-top: 4px;
  overflow: hidden;
  color: var(--text-color-2);
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-row__path {
  color: var(--text-color-3);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.notification-panel__footer {
  padding: 10px 4px 2px;
}

.notification-dialog__toolbar {
  margin-bottom: 12px;
  color: var(--text-color-2);
  font-size: 13px;
}

.notification-dialog__body {
  max-height: min(62vh, 560px);
}

.notification-list--dialog .notification-row {
  min-height: 82px;
}

.notice-list-enter-active {
  animation: notice-enter 220ms var(--motion-ease-out-quint) both;
  animation-delay: var(--notice-delay);
}

.notice-list-leave-active {
  transition: opacity 120ms var(--motion-ease-out-quint), transform 120ms var(--motion-ease-out-quint);
}

.notice-list-enter-from,
.notice-list-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@keyframes notice-enter {
  from {
    opacity: 0;
    transform: translateY(-6px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes notice-dot {
  0%,
  100% {
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary-color, var(--brand-link)) 10%, transparent);
  }
  50% {
    box-shadow: 0 0 0 5px color-mix(in srgb, var(--primary-color, var(--brand-link)) 18%, transparent);
  }
}

@media (prefers-reduced-motion: reduce) {
  .header-action :deep(.n-button__icon),
  .n-a :deep(.n-icon),
  .n-badge :deep(.n-icon),
  .notification-row,
  .notice-list-enter-active,
  .notice-list-leave-active,
  .notification-row__dot {
    animation: none !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
