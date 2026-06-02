<template>
  <n-card style="max-width: 500px; margin: 40px auto">
    <n-h2>个人档案</n-h2>
    <div style="display: flex; align-items: flex-start">
      <!-- 头像区 -->
      <div style="display: flex; flex-direction: column; align-items: center; margin-right: 32px">
        <n-avatar :src="form.avatar" size="80" round style="border: 1px solid #eee; margin-bottom: 12px; object-fit: cover; width: 80px; height: 80px">
          <template #default>
            <span v-if="!form.avatar" style="font-size: 32px"></span>
          </template>
        </n-avatar>
        <n-upload v-if="editable" :show-file-list="false" :custom-request="handleAvatarUpload" :before-upload="beforeAvatarUpload" accept="image/*" style="width: 80px">
          <n-button type="primary" size="small" style="width: 80px">上传头像</n-button>
        </n-upload>
      </div>
      <!-- 表单区 -->
      <n-form :model="form" label-width="80" :rules="rules" ref="formRef" style="flex: 1">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" :disabled="!editable" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="form.nickname" :disabled="!editable" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="form.email" :disabled="!editable" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="form.phone" :disabled="!editable" placeholder="请输入手机号" />
        </n-form-item>
        <n-space justify="end">
          <n-button v-if="!editable && auth.hasPermission('user:edit:self')" type="primary" @click="editable = true">修改</n-button>
          <template v-else-if="editable">
            <n-button type="primary" @click="handleSave" :loading="saving" :disabled="saving">保存</n-button>
            <n-button @click="handleCancel" :disabled="saving">取消</n-button>
          </template>
        </n-space>
      </n-form>
    </div>
  </n-card>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { uploadFile } from '@/api/file'
import { getCurrentUserDetail, updateOwnProfile } from '@/api/user'
import { useAuthStore } from '@/store/auth'

const auth = useAuthStore()
const router = useRouter()
const message = useMessage()
const formRef = ref()
const saving = ref(false)
const editable = ref(false)
const originalForm = ref<any>(null)

const form = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  avatar: [
    {
      validator(_: any, value: string) {
        if (!value) return true
        // 只允许 jpg/jpeg/png/gif，和接口一致
        if (!/^https?:\/\/.+\.(jpg|jpeg|png|gif)$/i.test(value)) {
          return new Error('头像URL格式不正确，仅支持jpg/jpeg/png/gif')
        }
        return true
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  // 权限校验
  if (!auth.hasPermission('user:view:self')) {
    message.error('无权访问')
    router.replace('/')
    return
  }
  const res = await getCurrentUserDetail()
  if (res.code === 200) {
    Object.assign(form.value, res.data)
    originalForm.value = { ...form.value }
  } else {
    message.error(res.message || '获取信息失败')
  }
})

// 头像上传前校验
function beforeAvatarUpload(file: File) {
  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('头像文件不能超过2MB')
    return false
  }
  return true
}

// 头像上传
async function handleAvatarUpload(options: any) {
  const file = options.file.file as File
  if (!file) {
    message.error('请选择图片文件')
    options.onError()
    return
  }
  try {
    const res = await uploadFile(file)
    if (res.code === 200 && res.data) {
      form.value.avatar = res.data
      message.success('上传成功，请点击保存以应用更改')
      options.onFinish()
    } else {
      message.error(res.message || '上传失败')
      options.onError()
    }
  } catch {
    message.error('上传异常')
    options.onError()
  }
}

// 保存个人信息
async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const submitData = { ...form.value }
    const res = await updateOwnProfile(submitData)
    if (res.code === 200) {
      const detailRes = await getCurrentUserDetail()
      if (detailRes.code === 200) {
        auth.setUser(detailRes.data)
        Object.assign(form.value, detailRes.data)
        originalForm.value = { ...form.value }
        window.location.reload()
      }
      message.success('保存成功')
      editable.value = false
    } else {
      message.error(res.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 取消编辑
function handleCancel() {
  if (originalForm.value) {
    Object.assign(form.value, originalForm.value)
  }
  formRef.value?.restoreValidation()
  editable.value = false
}
</script>
