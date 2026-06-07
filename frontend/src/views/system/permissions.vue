<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">权限管理</n-h1>
      <div class="admin-toolbar" v-permission="'permission:list:all'">
        <n-input v-model:value="searchKeyword" placeholder="搜索权限标识/名称" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="moduleFilter" :options="moduleOptions" clearable placeholder="模块" style="width: 140px" />
        <div class="permission-view-switch" role="group" aria-label="权限展示方式">
          <n-button size="small" :type="!treeMode ? 'primary' : 'default'" secondary @click="treeMode = false">
            <template #icon>
              <Icon type="list" />
            </template>
            表格
          </n-button>
          <n-button size="small" :type="treeMode ? 'primary' : 'default'" secondary :disabled="loadingPermissions" @click="treeMode = true">
            <template #icon>
              <Icon type="grid" />
            </template>
            树形
          </n-button>
        </div>
      </div>
      <div class="permission-overview" v-permission="'permission:list:all'">
        <div class="permission-metric">
          <span>权限总数</span>
          <strong>{{ filteredPermissions.length }}</strong>
        </div>
        <div class="permission-metric">
          <span>模块数量</span>
          <strong>{{ activeModuleCount }}</strong>
        </div>
        <div class="permission-metric permission-metric--mode">
          <span>当前视图</span>
          <strong>{{ treeMode ? '树形分组' : '表格明细' }}</strong>
        </div>
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
        class="permissions-tree"
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
import { NButton, NCard, NDataTable, NH1, NInput, NSelect, NTag, NTree, useMessage } from 'naive-ui'
import { computed, h, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getPermissions } from '@/api/permissions'
import { Icon } from '@/components'
import { buildPermissionTree } from '@/composables'

import type { DataTableColumns } from 'naive-ui'
import type { PermissionVO } from '@/api/types'

type PermissionRow = PermissionVO & { module: string }

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

const activeModuleCount = computed(() => new Set(filteredPermissions.value.map(p => p.module)).size)

// 表格列定义
function getModuleTone(module: string) {
  if (module.includes('用户')) return 'user'
  if (module.includes('角色')) return 'role'
  if (module.includes('权限')) return 'permission'
  if (module.includes('分类')) return 'category'
  if (module.includes('课程')) return 'course'
  if (module.includes('题')) return 'question'
  if (module.includes('统计')) return 'statistics'
  return 'default'
}

const columns: DataTableColumns<PermissionRow> = [
  {
    title: '权限标识',
    key: 'code',
    minWidth: 220,
    render: row => h('span', { class: 'permission-code' }, row.code)
  },
  { title: '名称', key: 'name', minWidth: 170 },
  {
    title: '模块',
    key: 'module',
    width: 150,
    render: row =>
      h(
        NTag,
        {
          bordered: false,
          round: true,
          class: ['permission-module-tag', `permission-module-tag--${getModuleTone(row.module)}`]
        },
        { default: () => row.module }
      )
  }
]

const rowKey = (row: PermissionRow) => row.id

// 搜索
function handleSearch() {
  pagination.page = 1
}
</script>

<style scoped>
.permission-view-switch {
  display: inline-flex;
  gap: 6px;
  padding: 4px;
  border-radius: var(--surface-radius-sm);
  background: color-mix(in srgb, var(--primary-color, var(--brand-link)) 7%, transparent);
}

.permission-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  margin: 12px 0 4px;
  border: 1px solid color-mix(in srgb, var(--app-border-color) 82%, transparent);
  border-radius: var(--surface-radius-sm);
  background: color-mix(in srgb, var(--app-border-color) 58%, transparent);
}

.permission-metric {
  min-width: 0;
  padding: 12px 14px;
  background: var(--app-surface-color);
}

.permission-metric span {
  display: block;
  color: var(--app-text-muted);
  font-size: 12px;
  line-height: 1.4;
}

.permission-metric strong {
  display: block;
  margin-top: 3px;
  overflow: hidden;
  color: var(--app-text-color);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.permission-metric--mode strong {
  color: var(--primary-color, var(--brand-link));
  font-size: 15px;
}

.permission-code {
  display: inline-flex;
  max-width: 100%;
  align-items: center;
  padding: 4px 8px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--app-text-color) 8%, transparent);
  color: var(--app-text-color);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.permission-module-tag {
  --permission-module-color: var(--primary-color, var(--brand-link));
  --n-color: color-mix(in srgb, var(--permission-module-color) 18%, var(--app-surface-color));
  --n-border: 1px solid color-mix(in srgb, var(--permission-module-color) 32%, transparent);
  --n-text-color: var(--app-text-color);
  color: var(--app-text-color);
  background: color-mix(in srgb, var(--permission-module-color) 18%, var(--app-surface-color));
  font-weight: 600;
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--permission-module-color) 32%, transparent);
}

.permission-module-tag--user {
  --permission-module-color: #14b8a6;
}

.permission-module-tag--role {
  --permission-module-color: #8b5cf6;
}

.permission-module-tag--permission {
  --permission-module-color: #3b82f6;
}

.permission-module-tag--category {
  --permission-module-color: #22c55e;
}

.permission-module-tag--course,
.permission-module-tag--question {
  --permission-module-color: #f59e0b;
}

.permission-module-tag--statistics {
  --permission-module-color: #f43f5e;
}

.permissions-tree {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--primary-color, var(--brand-link)) 5%, transparent), transparent 160px),
    var(--app-surface-color);
  border-radius: var(--surface-radius);
  border: 1px solid color-mix(in srgb, var(--app-border-color) 82%, transparent);
  padding: 18px 24px;
  min-height: 320px;
  font-size: var(--text-sm);
  line-height: 1.55;
}

.permissions-tree :deep(.n-tree-node) {
  min-height: 32px;
  border-radius: 8px;
  transition:
    background-color var(--motion-duration-base) var(--motion-ease-out-quart),
    transform var(--motion-duration-fast) var(--motion-ease-out-quint);
}

.permissions-tree :deep(.n-tree-node:hover) {
  background: color-mix(in srgb, var(--primary-color, var(--brand-link)) 8%, transparent);
  transform: translateX(2px);
}

.permissions-tree :deep(.n-tree-node--selected) {
  background: color-mix(in srgb, var(--primary-color, var(--brand-link)) 13%, transparent);
  color: var(--primary-color, var(--brand-link));
}

.permissions-tree :deep(.n-tree-node-switcher) {
  color: var(--app-text-muted);
}

.permissions-tree :deep(.n-tree-node-content) {
  padding: 5px 0;
}

@media (max-width: 768px) {
  .permission-overview {
    grid-template-columns: 1fr;
  }
}
</style>
