<template>
  <router-view v-slot="{ Component, route }">
    <transition name="app-motion" mode="out-in">
      <component :is="Component" :key="route.matched[0]?.name ?? route.path" />
    </transition>
  </router-view>
</template>

<script lang="ts" setup>
import { useLoadingBar } from 'naive-ui'

import router from '@/router'

const loadingBar = useLoadingBar()

router.beforeEach(() => loadingBar?.start())

router.afterEach(() => loadingBar?.finish())
</script>

<!-- <template>
  <suspense @pending="loadingBar?.start" @resolve="loadingBar.finish">
    <template #default>
      <router-view v-slot="{ Component, route }">
        <transition :name="route.meta.transition || 'fade'" mode="out-in">
          <keep-alive>
            <component :is="Component" :key="route.meta.usePathKey ? route.path : undefined" />
          </keep-alive>
        </transition>
      </router-view>
    </template>
    <template #fallback> Loading... </template>
  </suspense>
</template>

<script lang="ts" setup>
import { useLoadingBar } from 'naive-ui'
const loadingBar = useLoadingBar()
</script> -->
