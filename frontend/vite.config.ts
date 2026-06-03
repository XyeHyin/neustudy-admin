import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { defineConfig } from 'vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), vueJsx()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // 代理 /api 开头的请求到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia', 'pinia-plugin-persistedstate'],
          'vendor-naive': ['naive-ui'],
          'vendor-icons': ['@vicons/ionicons5', '@vicons/material'],
          'vendor-charts': ['echarts'],
          'vendor-mindmap': ['jsmind']
        }
      }
    },
    chunkSizeWarningLimit: 1200
  }
})
