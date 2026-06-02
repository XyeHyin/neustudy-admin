<template>
  <n-layout position="absolute" has-sider>
    <sidebar />
    <n-layout>
      <Header />
      <Content />
      <!-- <Footer /> -->
    </n-layout>
  </n-layout>
</template>

<script lang="ts" setup>
import Content from './content.vue'
import Footer from './footer.vue'
import Header from './header.vue'
import Sidebar from './sidebar.vue'
import { useSidebarStore } from '@/store/sidebar'
import { useSize } from 'vue-hooks-plus'
import { onMounted, watch } from 'vue'

const sidebar = useSidebarStore()
const { width } = useSize(document.body)

onMounted(() => {
  watch(
    width,
    val => {
      if (val < 768 && !sidebar.collapsed) {
        sidebar.collapsed = true
      }
      // else if (val >= 768 && sidebar.collapsed) {
      //   sidebar.collapsed = false
      // }
    },
    { immediate: false }
  )
})

// workbench
</script>
