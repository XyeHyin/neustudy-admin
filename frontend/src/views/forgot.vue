<template>
  <div class="auth-page">
    <transition name="fade-slide">
      <n-h1 class="auth-title" v-if="show">NEU智教通</n-h1>
    </transition>
    <transition name="fade-slide" appear>
      <n-card class="auth-card" v-if="show">
        <n-h2 class="auth-subtitle">找回密码</n-h2>
        <n-form ref="formRef" :model="model" :rules="rules" label-width="80">
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="model.email" placeholder="请输入注册邮箱" />
          </n-form-item>
          <n-form-item label="邮箱验证码" path="code">
            <n-input-group>
              <n-input v-model:value="model.code" placeholder="请输入验证码" />
              <n-button type="primary" :loading="sendingCode" ghost :disabled="countdown > 0 || !model.email" @click="handleSendCode">
                <span v-if="countdown > 0">{{ `${countdown}s后重试` }} </span>
                <span v-else-if="sendingCode"></span>
                <span v-else>获取验证码</span>
              </n-button>
            </n-input-group>
          </n-form-item>
          <n-form-item label="新密码" path="newPassword">
            <n-input v-model:value="model.newPassword" type="password" placeholder="请输入新密码" />
          </n-form-item>
          <div class="auth-actions">
            <n-button type="primary" block :loading="loading" @click="handleForgot">重置密码</n-button>
            <div class="auth-links-between">
              <router-link to="/login">登录</router-link>
              <router-link to="/register">还没账号？去注册</router-link>
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

import { resetPassword, sendCode } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const show = ref(false)
const countdown = ref(0)
let timer: any = null

const model = ref({
  email: '',
  code: '',
  newPassword: ''
})

const rules = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
  ]
}

const formRef = ref()

onMounted(() => {
  setTimeout(() => (show.value = true), 80)
})

// 发送验证码
const { runAsync: runSendCode, loading: sendingCode } = useRequest(sendCode, { manual: true })
const handleSendCode = async () => {
  if (!model.value.email) {
    message.error('请先输入注册邮箱')
    return
  }
  try {
    await runSendCode({ email: model.value.email })
    message.success('验证码已发送，请查收邮箱')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e: any) {
    message.error(e?.message || '验证码发送失败')
  }
}

// 重置密码
const { runAsync: runResetPassword, loading } = useRequest(resetPassword, { manual: true })
const handleForgot = () => {
  formRef.value?.validate(async (errors: any) => {
    if (errors) return
    try {
      const res = await runResetPassword({
        email: model.value.email,
        code: model.value.code,
        newPassword: model.value.newPassword
      })
      if (res.code === 200) {
        message.success('密码重置成功，请登录')
        router.push('/login')
      } else {
        message.error(res.message || '重置失败')
      }
    } catch (e: any) {
      message.error(e?.message || '请求异常')
    }
  })
}
</script>

<style scoped>
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
  justify-content: flex-end;
  margin-top: 12px;
  font-size: 14px;
}
.auth-links a {
  color: #409eff;
  text-decoration: none;
}
.auth-links-between {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 14px;
}
.auth-links-between a {
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
