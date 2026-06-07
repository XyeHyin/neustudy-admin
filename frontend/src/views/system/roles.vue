<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">角色管理</n-h1>
      <div class="admin-toolbar">
        <n-input v-permission="'role:list:all'" v-model:value="searchKeyword" placeholder="搜索 ID/角色名称/描述" clearable style="width: 240px" @input="handleSearch" />
        <n-button v-permission="'role:create:all'" type="primary" @click="handleCreateRoleBtn">新增角色</n-button>
        <n-button v-permission="'role:delete:all'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <n-data-table
        v-permission="'role:list:all'"
        :columns="columns"
        :data="roles"
        :pagination="pagination"
        :loading="loadingRoles"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        :checked-row-keys="selectedRowKeys"
        @update:checked-row-keys="updateSelectedRowKeys"
        @update:page="handlePageChange"
      />
    </n-card>
    <role-create-modal :show="showCreateRoleModal" :loading="creatingRole" :permissions="permissions" @update:show="updateCreateRoleModalShow" @submit="handleCreateRole" />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NInput, NTree, useMessage, useModal } from 'naive-ui'
import { h, onMounted, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getPermissions } from '@/api/permissions'
import { assignRolePermissions, batchDeleteRoles, createRole, deleteRole, getRoleDetail, getRoles } from '@/api/role'
import { getCurrentUserDetail } from '@/api/user'
import RoleCreateModal from '@/components/role/role-create-modal.vue'
import { formatDate } from '@/utils/datetime'
import { buildPermissionTree } from '@/composables'
import { useAuthStore } from '@/store/auth'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CreateRoleDTO, PermissionVO, RoleVO } from '@/api/types'

const message = useMessage()
const modal = useModal()
const rowKey = (row: RoleVO) => row.id

const roles = ref<RoleVO[]>([])
const pagination = ref({ page: 1, pageSize: 10 })
const searchKeyword = ref('')
const selectedRowKeys = ref<DataTableRowKey[]>([])
const permissions = ref<PermissionVO[]>([])
const permissionTree = ref<any[]>([])
const { loading: loadingRoles } = useRequest(getRoles, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      roles.value = res.data
    } else {
      message.error(res.message || '获取角色列表失败')
    }
  }
})

useRequest(getPermissions, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      permissions.value = res.data
      permissionTree.value = buildPermissionTree(res.data)
    } else {
      message.error(res.message || '获取权限列表失败')
    }
  }
})

const { loading: creatingRole, runAsync: runCreateRole } = useRequest(createRole, {
  manual: true
})

const auth = useAuthStore()

onMounted(async () => {
  await updateRoles()
})

// 加载角色列表
async function updateRoles() {
  try {
    const res = await getRoles()
    if (res.code === 200) {
      let allRoles = res.data
      if (searchKeyword.value) {
        const kw = searchKeyword.value.trim().toLowerCase()
        allRoles = allRoles.filter(
          r => (r.name && r.name.toLowerCase().includes(kw)) || (r.description && r.description.toLowerCase().includes(kw)) || (r.id && String(r.id).includes(kw))
        )
      }
      roles.value = allRoles
    } else {
      message.error(res.message || '获取角色列表失败')
    }
  } catch (e: any) {
    message.error(e.message || '请求异常')
  }
}

// 搜索角色
function handleSearch() {
  pagination.value.page = 1
  updateRoles()
}

// 分页切换
function handlePageChange(page: number) {
  pagination.value.page = page
}

// 更新选中行
function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

// 表格列定义
const columns: DataTableColumns<RoleVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  { title: '角色名称', key: 'name', minWidth: 120 },
  { title: '描述', key: 'description', minWidth: 200 },
  { title: '创建时间', key: 'createTime', minWidth: 120, render: (row: RoleVO) => formatDate(row.createTime) },
  { title: '更新时间', key: 'updateTime', minWidth: 120, render: (row: RoleVO) => formatDate(row.updateTime) },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row: RoleVO) => [
      // 查看按钮
      h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          ghost: true,
          style: 'margin-right: 8px',
          directives: {
            name: 'permission',
            value: 'role:view:all'
          },
          onClick: () => handleView(row)
        },
        { default: () => '查看' }
      ),
      // 删除按钮（有权限才显示）
      h(
        NButton,
        {
          size: 'small',
          type: 'error',
          ghost: true,
          directives: {
            name: 'permission',
            value: 'role:delete:all'
          },
          onClick: () => handleDelete(row)
        },
        { default: () => '删除' }
      )
    ]
  }
]

// 新增角色
const showCreateRoleModal = ref(false)
function handleCreateRoleBtn() {
  if (!auth.hasPermission('role:create:all')) return
  showCreateRoleModal.value = true
}

async function handleCreateRole(role: CreateRoleDTO) {
  try {
    const res = await runCreateRole(role)
    if (res.code === 200) {
      message.success('角色创建成功')
      showCreateRoleModal.value = false
      updateRoles()
    } else {
      message.error(res.message || '创建角色失败')
    }
  } catch (e: any) {
    message.error(e.message || '请求异常')
  }
}

function updateCreateRoleModalShow(show: boolean) {
  showCreateRoleModal.value = show
}

// 查看/编辑角色权限
async function handleView(row: RoleVO) {
  try {
    const res = await getRoleDetail(row.id)
    if (res.code !== 200) {
      message.error(res.message || '获取角色详情失败')
      return
    }
    const detail = res.data
    const checked = ref<number[]>(detail.permissions?.map(p => p.id) || [])
    modal.create({
      title: `角色权限：${row.name}`,
      preset: 'dialog',
      style: { width: '420px' },
      content: () =>
        h(NTree, {
          cascade: true,
          keyField: 'key',
          labelField: 'label',
          data: permissionTree.value,
          checkable: true,
          checkOnClick: true,
          blockLine: true,
          defaultCheckedKeys: checked.value,
          'onUpdate:checkedKeys': (val: number[]) => (checked.value = val)
        }),
      positiveText: '保存',
      negativeText: '取消',
      onPositiveClick: async () => {
        const allPermissionIds = permissions.value.map(p => p.id)
        const onlyIds = checked.value.filter(id => allPermissionIds.includes(id))
        try {
          const res = await assignRolePermissions(row.id, onlyIds)
          if (res.code === 200) {
            message.success('权限已更新')
            // 刷新当前用户信息
            const userRes = await getCurrentUserDetail()
            if (userRes.code === 200) {
              auth.setUser(userRes.data)
            }
            updateRoles()
          } else {
            message.error(res.message || '更新权限失败')
          }
        } catch (e) {
          message.error('请求异常')
        }
      }
    })
  } catch (e) {
    message.error('请求异常')
  }
}

// 删除角色
function handleDelete(row: RoleVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除角色 ${row.name} 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await deleteRole(row.id)
        if (res.code === 200) {
          message.success('删除成功')
          updateRoles()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '请求异常')
      }
    }
  })
}

// 批量删除角色（无批量接口，循环调用）
function handleBatchDelete() {
  if (!selectedRowKeys.value.length) return
  modal.create({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个角色吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteRoles(selectedRowKeys.value.map(Number))
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          updateRoles()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '请求异常')
      }
    }
  })
}
</script>
