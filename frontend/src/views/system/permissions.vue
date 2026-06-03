<template>
  <div class="permissions-page">
    <n-card :bordered="false" class="permissions-card">
      <n-h1 class="permissions-title">权限管理</n-h1>
      <div class="permissions-toolbar" v-permission="'permission:list:all'">
        <n-input v-model:value="searchKeyword" placeholder="搜索权限标识/名称" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="moduleFilter" :options="moduleOptions" clearable placeholder="模块" style="width: 140px" />
        <n-button :disabled="loadingPermissions" quaternary circle @click="treeMode = !treeMode" style="margin-left: 8px">
          <template #icon>
            <Icon :type="treeMode ? 'list' : 'grid'" />
          </template>
        </n-button>
      </div>
      <n-data-table
        v-if="!treeMode"
        ref="dataTableRef"
        :columns="columns"
        :data="filteredPermissions"
        :bordered="false"
        :pagination="pagination"
        style="margin-top: 24px"
        :loading="loadingPermissions"
        :row-key="rowKey"
      />
      <n-tree
        v-else
        ref="treeRef"
        class="permission-tree"
        :data="permissionTree"
        block-line
        style="margin-top: 24px"
        :key-field="'key'"
        :label-field="'label'"
        :default-expand-all="true"
      />
    </n-card>
  </div>
</template>

<script lang="ts" setup>
import { NButton, NCard, NDataTable, NH1, NInput, NSelect, NTree, useMessage } from 'naive-ui'
import { computed, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getPermissions } from '@/api/permissions'
import { Icon } from '@/components'
import { buildPermissionTree } from '@/composables'

import type { DataTableColumns } from 'naive-ui'
import type { PermissionVO } from '@/api/types'

const message = useMessage()
const permissions = ref<PermissionVO[]>([])
const permissionTree = ref<any[]>([])
const searchKeyword = ref('')
const moduleFilter = ref<string | null>(null)
const pagination = reactive({
  page: 1,
  pageSize: 10,
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})
const treeMode = ref(false)
const dataTableRef = ref()
const treeRef = ref()
const { loading: loadingPermissions } = useRequest(getPermissions, {
  onSuccess: res => {
    if (res.code === 200) {
      permissions.value = res.data
    } else {
      message.error(res.message || '获取权限失败')
    }
  }
})

watch([permissions, searchKeyword, moduleFilter], () => {
  permissionTree.value = buildPermissionTree(filteredPermissions.value)
})

// 获取模块名
function getModuleByCode(code: string): string {
  if (code.startsWith('user:')) return '用户管理'
  if (code.startsWith('role:')) return '角色管理'
  if (code.startsWith('permission:')) return '权限管理'
  if (code.startsWith('category:')) return '分类管理'
  if (code.startsWith('file:')) return '文件管理'
  if (code.startsWith('dashboard')) return '仪表盘'
  if (code.startsWith('ai:')) return 'AI 管理'
  if (code.startsWith('course:')) return '课程管理'
  if (code.startsWith('practice:')) return '练习管理'
  if (code.startsWith('knowledge_point:')) return '知识点管理'
  if (code.startsWith('paper:')) return '试卷管理'
  if (code.startsWith('question:')) return '题目管理'
  if (code.startsWith('statistics:')) return '统计管理'
  if (code.startsWith('grading:')) return '判分管理'
  return '其它'
}

const moduleOptions = computed(() => {
  const modules = Array.from(new Set(permissions.value.map(p => getModuleByCode(p.code))))
  return modules.map(m => ({ label: m, value: m }))
})

const filteredPermissions = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  return permissions.value
    .filter(p => {
      const matchKw = !kw || p.code.toLowerCase().includes(kw) || p.name.toLowerCase().includes(kw)
      const matchModule = !moduleFilter.value || getModuleByCode(p.code) === moduleFilter.value
      return matchKw && matchModule
    })
    .map(p => ({
      ...p,
      module: getModuleByCode(p.code)
    }))
})

// 表格列定义
const columns: DataTableColumns<PermissionVO> = [
  { title: '权限标识', key: 'code', width: 180 },
  { title: '名称', key: 'name', width: 160 },
  { title: '模块', key: 'module', width: 120 }
]

const rowKey = (row: PermissionVO) => row.id

// 搜索
function handleSearch() {
  pagination.page = 1
}
</script>

<style scoped>
.permissions-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px 0;
}
.permissions-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}
.permissions-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
.permissions-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
.permissions-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  gap: 8px;
}
.permissions-tree {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px #0001;
  padding: 18px 24px;
  min-height: 320px;
  font-size: 15px;
}
.permissions-tree .n-tree-node {
  border-radius: 6px;
  transition: background 0.2s;
}
.permissions-tree .n-tree-node:hover {
  background: #f5f7fa;
}
.permissions-tree .n-tree-node--selected {
  background: #e6f7ff;
  color: #409eff;
}
.permissions-tree .n-tree-node-switcher {
  color: #bbb;
}
.permissions-tree .n-tree-node-content {
  padding: 4px 0;
}
</style>
