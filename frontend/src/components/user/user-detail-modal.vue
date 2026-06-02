<template>
  <n-modal :show="show" preset="dialog" title="用户详情" @close="handleCancel" style="width: 520px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="用户名" path="username">
            <n-input v-model:value="form.username" placeholder="请输入用户名" disabled />
          </n-form-item>
          <n-form-item label="昵称" path="nickname">
            <n-input v-model:value="form.nickname" :disabled="!editMode" placeholder="请输入昵称" />
          </n-form-item>
          <n-form-item label="邮箱" path="email">
            <n-input v-model:value="form.email" :disabled="!editMode" placeholder="请输入邮箱" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="手机号" path="phone">
            <n-input v-model:value="form.phone" :disabled="!editMode" placeholder="请输入手机号" />
          </n-form-item>
          <n-form-item label="头像" path="avatar">
            <n-upload :show-file-list="false" :custom-request="handleAvatarUpload" accept="image/*" :disabled="!editMode">
              <n-button type="primary" size="small" :disabled="!editMode">上传头像</n-button>
            </n-upload>
            <n-image v-if="form.avatar" :src="form.avatar" style="width: 48px; height: 48px; border-radius: 50%; margin-left: 10px; object-fit: cover" />
          </n-form-item>
        </div>
      </div>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button v-if="!editMode" type="primary" @click="editMode = true">编辑</n-button>
          <template v-else>
            <n-button @click="editMode = !editMode">取消</n-button>
            <n-button type="primary" :loading="loading" @click="handleSubmit">保存</n-button>
          </template>
        </n-space>
      </n-form-item>
    </n-form>
    <n-divider style="margin: 18px 0 10px 0">角色变更</n-divider>
    <n-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="80" style="padding: 0 24px 8px 24px">
      <n-form-item label="角色" path="roleId">
        <n-select v-model:value="roleForm.roleId" :loading="loadingRoles" :options="roleOptions" placeholder="请选择角色" />
      </n-form-item>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button type="primary" :loading="savingRole" @click="handleRoleSubmit">保存角色</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import { uploadFile } from '@/api/file'

import type { RoleVO, UpdateUserDTO, UserDetailVO } from '@/api/types'

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  loadingRoles?: boolean
  savingRole?: boolean
  roles: RoleVO[]
  user: UserDetailVO | null
}>()
const emit = defineEmits(['update:show', 'submit', 'submit-role'])

const formRef = ref()
const roleFormRef = ref()
const editMode = ref(false)
// 用户表单数据
const form = ref<UpdateUserDTO>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})
// 角色表单数据
const roleForm = ref({ roleId: null as any })

// 用户信息表单校验规则
const rules = {
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' },
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 角色表单校验规则
const roleRules = {
  roleId: [
    {
      validator(_: any, value: any) {
        if (value === null || value === undefined || value === '') {
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

// 关闭弹窗
function handleCancel() {
  emit('update:show', false)
  editMode.value = false
}

// 提交用户信息表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit', { ...form.value })
      editMode.value = false
    }
  })
}

// 提交角色变更表单
function handleRoleSubmit() {
  roleFormRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit-role', { userId: props.user?.id, roleId: roleForm.value.roleId })
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
  } catch (e: any) {
    message.error(e.message || '上传异常')
    options.onError()
  }
}

// 监听用户数据变化，更新表单
watch(
  () => props.user,
  val => {
    if (!val) return
    form.value = {
      username: val.username,
      nickname: val.nickname,
      email: val.email,
      phone: val.phone,
      avatar: val.avatar
    }
    roleForm.value.roleId = val.role?.id ?? null
    editMode.value = false
  },
  { deep: true, immediate: true }
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
