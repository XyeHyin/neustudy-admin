<template>
  <n-modal :show="show" preset="dialog" title="新增角色" @close="handleCancel" style="width: 480px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" @submit.prevent="handleSubmit" style="padding: 16px 24px">
      <n-form-item label="角色名" path="name">
        <n-input v-model:value="form.name" placeholder="请输入角色名" />
      </n-form-item>
      <n-form-item label="描述" path="description">
        <n-input v-model:value="form.description" placeholder="请输入角色描述" />
      </n-form-item>
      <n-form-item label="权限" path="permissionIds">
        <n-tree
          :data="permissionTree"
          checkable
          cascade
          block-line
          :checked-keys="form.permissionIds"
          @update:checked-keys="(val: number[]) => (form.permissionIds = val)"
          :key-field="'key'"
          :label-field="'label'"
          style="width: 100%; border-radius: 6px; padding: 8px 12px; min-height: 180px"
        />
      </n-form-item>
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
import { type FormInst } from 'naive-ui'
import { computed, defineEmits, defineProps, ref, watch } from 'vue'

import { buildPermissionTree } from '@/composables/permissions'

import type { CreateRoleDTO, PermissionVO } from '@/api/types'

// 组件属性定义
const props = defineProps<{
  show: boolean
  loading?: boolean
  permissions: PermissionVO[]
}>()
const emit = defineEmits(['update:show', 'submit'])

const formRef = ref<FormInst | null>()
// 角色表单数据
const form = ref<CreateRoleDTO>({
  name: '',
  description: '',
  permissionIds: []
})

// 表单校验规则
const rules = {
  name: { required: true, message: '请输入角色名', trigger: 'blur' },
  description: { required: true, message: '请输入角色描述', trigger: 'blur' },
  permissionIds: [{ required: true, type: 'array', min: 1, message: '请选择至少一个权限', trigger: 'change' }]
}

// 权限树结构
const permissionTree = computed(() => buildPermissionTree(props.permissions))

// 关闭弹窗并重置表单
function handleCancel() {
  emit('update:show', false)
  form.value = { name: '', description: '', permissionIds: [] }
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      // 只保留数字类型的 id
      const onlyIds = form.value.permissionIds.filter((id: any) => typeof id === 'number')
      emit('submit', {
        ...form.value,
        permissionIds: onlyIds
      })
    }
  })
}

// 监听弹窗显示，重置表单
watch(
  () => props.show,
  val => {
    if (val) {
      form.value = { name: '', description: '', permissionIds: [] }
    }
  }
)
</script>
