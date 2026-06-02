<template>
  <div class="auth-page">
    <transition name="fade-slide">
      <n-h1 class="auth-title" v-if="show">NEU智教通</n-h1>
    </transition>
    <transition name="fade-slide" appear>
      <n-card class="auth-card" v-if="show">
        <n-h2 class="auth-subtitle">注册</n-h2>
        <n-form ref="formRef" :model="model" :rules="rules" label-width="80" @submit="handleRegister">
          <div class="form-row">
            <div class="form-col">
              <n-form-item label="用户名" path="username">
                <n-input v-model:value="model.username" placeholder="请输入用户名" />
              </n-form-item>
              <n-form-item label="密码" path="password">
                <n-input v-model:value="model.password" type="password" placeholder="请输入密码" />
              </n-form-item>
              <n-form-item label="确认密码" path="confirm">
                <n-input v-model:value="model.confirm" type="password" placeholder="请再次输入密码" />
              </n-form-item>
              <n-form-item label="昵称" path="nickname">
                <n-input v-model:value="model.nickname" placeholder="请输入昵称" />
              </n-form-item>
            </div>
            <div class="form-col">
              <n-form-item label="邮箱" path="email">
                <n-input v-model:value="model.email" placeholder="请输入邮箱" />
              </n-form-item>
              <n-form-item label="手机号" path="phone">
                <n-input v-model:value="model.phone" placeholder="请输入手机号" />
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
            </div>
          </div>
          <div class="auth-actions">
            <div class="register-btn-wrap">
              <n-button type="primary" block :loading="loading" @click="handleRegister" attr-type="submit">注册</n-button>
            </div>
            <div class="auth-links">
              <router-link to="/login">已有账号？登录</router-link>
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
import { computed, onMounted, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'
import { useRouter } from 'vue-router'

import { register as apiRegister, sendCode } from '@/api/auth'
import { getCurrentUserDetail, getUserDetail } from '@/api/user'
import { isJwtToken, useAuthStore } from '@/store/auth'

const router = useRouter()
const message = useMessage()
const show = ref(false)
const countdown = ref(0)
const auth = useAuthStore()
let timer: any = null

const model = ref({
  username: '',
  password: '',
  confirm: '',
  nickname: '',
  email: '',
  phone: '',
  code: ''
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator(_: any, value: string) {
        if (value !== model.value.password) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' },
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
}

const formRef = ref()

onMounted(() => {
  setTimeout(() => (show.value = true), 80)
})

const { runAsync: runRegister, loading } = useRequest(apiRegister, {
  manual: true
})
const { runAsync: runSendCode, loading: sendingCode } = useRequest(sendCode, {
  manual: true
})
const handleSendCode = async () => {
  if (!model.value.email) {
    message.error('请先输入邮箱')
    return
  }
  if (!/^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]{2,7}$/.test(model.value.email)) {
    message.error('邮箱格式不正确')
    return
  }
  if (countdown.value > 0) {
    message.error('请稍后再试')
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

const { runAsync: runGetUserDetail, loading: loadingUserDetail } = useRequest(getCurrentUserDetail, {
  manual: true
})
const handleRegister = (e: MouseEvent) => {
  e.preventDefault()
  formRef.value?.validate(async (errors: any) => {
    if (errors) return
    try {
      const res = await runRegister({
        username: model.value.username,
        password: model.value.password,
        nickname: model.value.nickname,
        email: model.value.email,
        phone: model.value.phone,
        code: model.value.code
      })
      const token = res.data?.token
      if (!isJwtToken(token)) {
        auth.logout()
        throw new Error(res.message || 'Invalid register token')
      }
      auth.setToken(token)
      const { data: userDetail } = await runGetUserDetail()
      auth.setUser(userDetail)
      message.success('注册成功，已自动登录')
      router.push('/')
    } catch (e: any) {
      message.error(e?.message || '注册请求异常')
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
  max-width: 600px;
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
.form-row {
  display: flex;
  gap: 32px;
}
.form-col {
  flex: 1;
}
.auth-actions {
  margin-top: 18px;
}
.register-btn-wrap {
  display: flex;
  justify-content: center;
}
.register-btn-wrap .n-button {
  width: 100%;
  max-width: 100%;
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
.register-btn-wrap {
  display: flex;
  justify-content: center;
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
