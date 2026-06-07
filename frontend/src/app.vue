<template>
  <provider>
    <viewer />
  </provider>
  <SplashScreen v-if="showSplash" @done="showSplash = false" />
</template>

<script lang="ts" setup>
import { Provider, Viewer } from './components'
import SplashScreen from './components/shared/splash-screen.vue'
import { ref } from 'vue'

const showSplash = ref(true)
</script>
