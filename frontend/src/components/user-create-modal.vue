<template>
  <n-modal :show="show" preset="dialog" title="创建用户" @close="handleCancel" style="width: 480px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" @submit="handleSubmit" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="form.username" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
          </n-form-item>
          <n-form-item label="昵称" path="nickname">
            <n-input v-model:value="form.nickname" placeholder="请输入昵称" />
          </n-form-item>
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="form.email" placeholder="请输入邮箱" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="手机号" path="phone">
            <n-input v-model:value="form.phone" placeholder="请输入手机号" />
          </n-form-item>
          <n-form-item label="头像" path="avatar">
            <n-upload :show-file-list="false" :custom-request="handleAvatarUpload" accept="image/*">
              <n-button type="primary" size="small">上传头像</n-button>
            </n-upload>
            <n-image v-if="form.avatar" :src="form.avatar" style="width: 40px; height: 40px; border-radius: 50%; margin-left: 10px; object-fit: cover" />
          </n-form-item>
          <n-form-item label="角色" path="roleId">
            <n-select v-model:value="form.roleId" :options="roleOptions" :loading="loadingRoles" placeholder="请选择角色" />
          </n-form-item>
        </div>
      </div>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button type="primary" :loading="loading" attr-type="submit">提交</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import { uploadFile } from '@/api/file'

import type { CreateUserDTO, RoleVO } from '@/api/types'

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  loadingRoles?: boolean
  roles: RoleVO[]
}>()
const emit = defineEmits(['update:show', 'submit'])

const formRef = ref()
// 用户表单数据
const form = ref<CreateUserDTO>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  roleId: null as any
})

// 表单校验规则
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
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
  roleId: [
    {
      validator(_: any, value: any) {
        if (value === undefined || value === null || value === '') {
          return new Error('请选择角色')
        }
        return true
      },
      trigger: 'change'
    }
  ]
}

// 角色下拉选项
const roleOptions = computed(() => props.roles.map(role => ({ label: role.description || role.name, value: role.id })))

const message = useMessage()

// 关闭弹窗并重置表单
function handleCancel() {
  emit('update:show', false)
  form.value = {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    avatar: '',
    roleId: null as any
  }
  formRef.value?.clearValidate()
}

// 提交表单
function handleSubmit(e: MouseEvent) {
  e.preventDefault()
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit', { ...form.value })
    }
  })
}

// 头像上传处理
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
      options.onFinish()
      message.success('上传成功')
    } else {
      message.error(res.message || '上传失败')
      options.onError()
    }
  } catch {
    message.error('上传异常')
    options.onError()
  }
}

// 监听弹窗显示，重置表单
watch(
  () => props.show,
  val => {
    if (val) {
      // 重置表单
      form.value = {
        username: '',
        password: '',
        nickname: '',
        email: '',
        phone: '',
        avatar: '',
        roleId: null as any
      }
    }
  }
)
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 32px;
}
.form-col {
  flex: 1;
}
</style>
