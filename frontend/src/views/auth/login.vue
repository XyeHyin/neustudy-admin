<template>
  <div class="auth-page">
    <transition name="fade-slide">
      <n-h1 class="auth-title" v-if="show">NEU智教通</n-h1>
    </transition>
    <transition name="fade-slide" appear>
      <n-card class="auth-card" v-if="show">
        <n-h2 class="auth-subtitle">登录</n-h2>
        <n-form :rules="rules" :model="model" ref="formRef" @submit="handleLogin">
          <n-form-item-row label="用户名" path="username">
            <n-input v-model:value="model.username" placeholder="请输入用户名" />
          </n-form-item-row>
          <n-form-item-row label="密码" path="password">
            <n-input v-model:value="model.password" type="password" placeholder="请输入密码" />
          </n-form-item-row>
          <div class="auth-actions">
            <n-button type="primary" block :loading="loading" attr-type="submit">登录</n-button>
            <div class="auth-links">
              <router-link to="/register">注册账号</router-link>
              <router-link to="/forgot">忘记密码？</router-link>
            </div>
          </div>
        </n-form>
      </n-card>
    </transition>
  </div>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { onMounted, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import { useRouter } from 'vue-router'

import { login as apiLogin } from '@/api/auth'
import { getCurrentUserDetail } from '@/api/user'
import { isJwtToken, useAuthStore } from '@/store/auth'

import type { FormInst } from 'naive-ui'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const rules = {
  username: { required: true, message: '需要输入用户名', trigger: 'input' },
  password: [
    { required: true, message: '需要输入密码', trigger: 'input' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'input' }
  ]
}
const formRef = ref<FormInst | null>(null)
const model = ref({ username: '', password: '' })
const show = ref(false)

onMounted(() => {
  setTimeout(() => (show.value = true), 80)
})

const { runAsync: runLogin, loading } = useRequest((params: { username: string; password: string }) => apiLogin(params), {
  manual: true
})

const { runAsync: runGetUserDetail } = useRequest(getCurrentUserDetail, {
  manual: true
})

const handleLogin = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate(async errors => {
    if (errors) return
    try {
      const res = await runLogin(model.value)
      const token = res.data?.token
      if (!isJwtToken(token)) {
        auth.logout()
        throw new Error(res.message || 'Invalid login token')
      }
      auth.setToken(token)
      const { data: userDetail } = await runGetUserDetail()
      message.success('登录成功')
      auth.setUser(userDetail)
      router.push('/')
    } catch (err: any) {
      if (err?.code === 403) {
        auth.logout()
      }
      message.error(err?.message || '登录异常，请稍后重试')
    }
  })
}
</script>

<style scoped>
@import '@/styles/auth.css';

.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.auth-title {
  margin-bottom: 24px;
  font-size: 2.8rem;
  letter-spacing: 5px;
  opacity: 0.8;
  text-align: center;
}
.auth-card {
  max-width: 380px;
  width: 100%;
  box-shadow: var(--box-shadow);
  padding: 32px 32px 24px 32px;
  border-radius: 12px;
}
.auth-subtitle {
  text-align: center;
  margin-bottom: 18px;
  font-weight: 400;
  font-size: 1.5rem;
}
.auth-actions {
  margin-top: 18px;
}
.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 14px;
}
.auth-links a {
  color: #409eff;
  text-decoration: none;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(30px) scale(0.98);
}
.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}
</style>
