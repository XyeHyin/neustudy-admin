<template>
  <div class="users-page">
    <n-card :bordered="false" class="users-card">
      <n-h1 class="users-title">用户管理</n-h1>
      <div class="users-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索用户名/邮箱" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态" style="width: 100px" @update:value="handleSearch" />
        <n-button v-permission="'user:create:all'" type="primary" @click="handleAddUserBtn">新增用户</n-button>
        <n-button v-permission="'user:delete:all'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete"> 批量删除 </n-button>
      </div>
      <n-data-table
        v-permission="'user:list:all'"
        remote
        :columns="columns"
        :data="users"
        :loading="loadingUsers"
        :pagination="pagination"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        @update:checked-row-keys="updateSelectedRowKeys"
      />
    </n-card>
    <user-create-modal
      v-model:show="createUserModalShow"
      @update:show="updateCreateUserModalShow"
      :roles="roles"
      :loading="creatingUser"
      :loading-roles="loadingRoles"
      @submit="handleAddUser"
    />
    <user-detail-modal
      v-model:show="updateUserModalShow"
      @update:show="updateUpdateUserModalShow"
      :user="editData"
      :roles="roles"
      :loading="updatingUser"
      @submit="handleUpdateUser"
      @submit-role="handleUpdateRole"
    />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NInput, NSelect, NSwitch, useDialog, useMessage } from 'naive-ui'
import { h, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getRoles } from '@/api/role'
import { batchDeleteUsers, createUser, deleteUser, getUserDetail, getUserPage, updateUser, updateUserRole, updateUserStatus } from '@/api/user'
import UserCreateModal from '@/components/user/user-create-modal.vue'
import userDetailModal from '@/components/user/user-detail-modal.vue'
import { useAuthStore } from '@/store/auth'
import { formatDateTime } from '@/utils/datetime'

import type { DataTableColumns, DataTableRowKey, SelectOption } from 'naive-ui'
import type { CreateUserDTO, RoleVO, UpdateUserDTO, UserDetailVO, UserVO } from '@/api/types'

const auth = useAuthStore()

const message = useMessage()
const dialog = useDialog()
const createUserModalShow = ref(false)
const updateUserModalShow = ref(false)

const rowKey = (row: UserVO) => row.id

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 100,
  showSizePicker: true,
  paginationBehaviorOnFilter: 'first',
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})

const searchKeyword = ref('')
const statusFilter = ref<number>(2)

const selectedRowKeys = ref<DataTableRowKey[]>([])

const statusOptions = ref<SelectOption[]>([
  { label: '全部', value: 2 },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
])

const users = ref<UserVO[]>([])
const roles = ref<RoleVO[]>([])

const { loading: loadingUsers, refresh: fetchUsers } = useRequest(
  () =>
    getUserPage({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value,
      enabled: statusFilter.value
    }),
  {
    debounceWait: 300,
    onSuccess(res) {
      if (res.code === 200) {
        users.value = res.data?.content || []
        pagination.page = res.data?.page || 1
        pagination.itemCount = res.data?.total || 0
      } else {
        message.error(res.message)
      }
    }
  }
)

watch([() => pagination.page, () => pagination.pageSize, searchKeyword, statusFilter], () => {
  fetchUsers()
})

const { loading: loadingRoles } = useRequest(getRoles, {
  onSuccess(res) {
    if (res.code === 200) {
      roles.value = res.data || []
    } else {
      message.error(res.message)
    }
  }
})

const { runAsync: updateUserStatusReq } = useRequest(updateUserStatus, {
  manual: true
})

const { runAsync: createUserReq, loading: creatingUser } = useRequest(createUser, { manual: true })
// 新增用户
async function handleAddUserBtn() {
  if (!auth.hasPermission('user:create:all')) return
  createUserModalShow.value = true
}
// 提交新增用户
async function handleAddUser(user: CreateUserDTO) {
  try {
    const res = await createUserReq(user)
    if (res.code === 200) {
      message.success('用户创建成功')
      createUserModalShow.value = false
      fetchUsers()
    } else {
      message.error(res.message || '创建失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求异常')
  }
}
const updatingUser = ref(false)
const editData = ref<UserDetailVO>({} as UserDetailVO)

// 更新用户信息
async function handleUpdateUserBtn(row: UserVO) {
  try {
    const res = await getUserDetail(row.id)
    if (res.code === 200) {
      editData.value = res.data
      updateUserModalShow.value = true
    } else {
      message.error(res.message || '获取用户详情失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求异常')
  }
}

// 更新用户角色
async function handleUpdateRole({ userId, roleId }: { userId: number; roleId: number }) {
  try {
    const res = await updateUserRole(userId, roleId)
    if (res.code === 200) {
      message.success('用户角色更新成功')
      updateUserModalShow.value = false
      fetchUsers()
    } else {
      message.error(res.message || '更新失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求异常')
  }
}
function updateUpdateUserModalShow() {
  updateUserModalShow.value = false
}

function updateCreateUserModalShow() {
  createUserModalShow.value = false
}

// 更新用户信息
async function handleUpdateUser(user: UpdateUserDTO) {
  updatingUser.value = true
  try {
    const res = await updateUser(editData.value.id, user)
    if (res.code === 200) {
      message.success('更新用户成功')
      updateUserModalShow.value = false
      fetchUsers()
    } else {
      message.error(res.message || '更新失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求异常')
  } finally {
    updatingUser.value = false
  }
}
const { runAsync: deleteUserReq } = useRequest(deleteUser, { manual: true })
const { runAsync: batchDeleteUsersReq } = useRequest(batchDeleteUsers, { manual: true })
async function handleDeleteUser(id: number) {
  try {
    dialog.warning({
      title: '删除此用户',
      content: '确定要删除吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        const res = await deleteUserReq(id)
        if (res.code === 200) {
          message.success('用户删除成功')
          fetchUsers()
        } else {
          message.error(res.message || '删除失败')
        }
      }
    })
  } catch (e: any) {
    message.error(e?.message || '请求异常')
  }
}

// 批量删除用户
async function handleBatchDelete() {
  if (!auth.hasPermission('user:delete:all')) return
  if (!selectedRowKeys.value.length) return
  dialog.warning({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteUsersReq(selectedRowKeys.value.map(Number))
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchUsers()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (e: any) {
        message.error(e?.message || '请求异常')
      }
    }
  })
}

const updateSelectedRowKeys = (rowKeys: DataTableRowKey[]) => {
  selectedRowKeys.value = rowKeys
}

const columns: DataTableColumns<UserVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户名', key: 'username', minWidth: 110 },
  { title: '昵称', key: 'nickname', minWidth: 110 },
  { title: '邮箱', key: 'email', minWidth: 150 },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: (row: UserVO) => {
      if (auth.hasPermission('user:update:status')) {
        return h(
          NSwitch,
          {
            size: 'small',
            value: row.enabled,
            onClick: async () => {
              if (!auth.hasPermission('user:update:status')) return
              if (auth.user.id == row.id) return
              try {
                const res = await updateUserStatusReq(row.id, !row.enabled)
                if (res.code === 200) {
                  users.value[users.value.findIndex(user => user.id === row.id)].enabled = !row.enabled
                } else {
                  message.error(res.message || '操作失败')
                }
              } catch (e: any) {
                message.error(e?.message || '操作失败')
              }
            }
          },
          { default: () => (row.enabled ? '启用中' : '已禁用') }
        )
      } else {
        return row.enabled ? '启用' : '禁用'
      }
    }
  },
  {
    title: '注册时间',
    key: 'createTime',
    minWidth: 120,
    render: (row: UserVO) => {
      return formatDateTime(row.createTime)
    }
  },
  {
    title: '更新时间',
    key: 'updateTime',
    minWidth: 120,
    render: (row: UserVO) => {
      return formatDateTime(row.updateTime)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row: UserVO) => {
      // 只判断当前用户权限
      return [
        auth.hasPermission('user:edit:all') &&
          h(
            NButton,
            {
              size: 'small',
              type: 'info',
              ghost: true,
              style: 'margin-right: 8px',
              onClick: () => handleUpdateUserBtn(row)
            },
            { default: () => '查看' }
          ),
        auth.hasPermission('user:edit:all') &&
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              ghost: true,
              onClick: () => handleDeleteUser(row.id)
            },
            { default: () => '删除' }
          )
      ].filter(Boolean)
    }
  }
]
function handleSearch() {
  pagination.page = 1
}
</script>

<style scoped>
.users-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px 0;
}
.users-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}
.users-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
.users-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
</style>

