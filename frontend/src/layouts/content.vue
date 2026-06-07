<template>
  <n-layout-content content-style="padding: var(--app-content-padding); min-height: 85vh">
    <router-view v-slot="{ Component, route }">
      <transition name="page-motion" mode="out-in">
        <div :key="route.fullPath" class="motion-scope">
          <component :is="Component" />
        </div>
      </transition>
    </router-view>
  </n-layout-content>
</template>
