<template>
  <div class="categories-page">
    <n-card :bordered="false" class="categories-card">
      <n-h1 class="categories-title">分类管理</n-h1>
      <div class="categories-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索分类名称/描述" clearable style="width: 240px" @input="handleSearch" />
        <n-button v-permission="'category:create:all'" type="primary" @click="handleAddCategory">新增分类</n-button>
        <n-button v-permission="'category:delete:all'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</n-button>
        <n-button quaternary circle @click="handleToggleView" style="margin-left: 8px">
          <template #icon>
            <Icon :type="treeMode ? 'list' : 'grid'" />
          </template>
        </n-button>
      </div>
      <n-data-table
        v-if="!treeMode"
        :columns="columns"
        :data="filteredCategories"
        :loading="loadingCategories"
        :pagination="pagination"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        :checked-row-keys="selectedRowKeys"
        @update:checked-row-keys="updateSelectedRowKeys"
        @update:page="handlePageChange"
      />
      <n-tree v-else :data="categoryTree" :key-field="'id'" :label-field="'name'" :default-expand-all="true" />
    </n-card>
  </div>
</template>

<script lang="ts" setup>
import { NButton, NForm, NFormItemRow, NInput, NSelect, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { batchDeleteCategories, createCategory, deleteCategory, getCategories, getCategoryTree, updateCategory } from '@/api/categories'
import { Icon } from '@/components'
import { formatDate } from '@/utils/datetime'
import { useAuthStore } from '@/store/auth'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CategoryFlatVO, CategoryTreeVO } from '@/api/types'

const message = useMessage()
const modal = useModal()
const rowKey = (row: CategoryFlatVO) => row.id

const categories = ref<CategoryFlatVO[]>([])
const searchKeyword = ref('')
const selectedRowKeys = ref<DataTableRowKey[]>([])

const treeMode = ref(false)
const categoryTree = ref<CategoryTreeVO[]>([])

const auth = useAuthStore()

async function fetchCategoryTree() {
  const res = await getCategoryTree()
  if (res.code === 200) {
    categoryTree.value = res.data || []
  }
}

// 切换树/表格视图
function handleToggleView() {
  treeMode.value = !treeMode.value
  if (treeMode.value) fetchCategoryTree()
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})

const { loading: loadingCategories, refresh: fetchCategories } = useRequest(() => getCategories(), {
  onSuccess(res) {
    if (res.code === 200) {
      categories.value = res.data || []
      pagination.itemCount = res.data?.length || 0
    } else {
      message.error(res.message)
    }
  }
})

onMounted(() => {
  fetchCategories()
})

const filteredCategories = computed(() => {
  if (!searchKeyword.value) return categories.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return categories.value.filter(
    c => (c.name && c.name.toLowerCase().includes(kw)) || (c.description && c.description.toLowerCase().includes(kw)) || (c.id && String(c.id).includes(kw))
  )
})

const parentOptions = computed(() =>
  categories.value.map(c => ({
    label: c.name,
    value: c.id
  }))
)

// 搜索
function handleSearch() {
  pagination.page = 1
}

// 分页切换
function handlePageChange(page: number) {
  pagination.page = page
}

// 选中行变化
function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

// 表格列定义，包含操作按钮
const columns: DataTableColumns<CategoryFlatVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  { title: '分类名称', key: 'name', minWidth: 120 },
  { title: '描述', key: 'description', minWidth: 200 },
  { title: '创建时间', key: 'createTime', minWidth: 120, render: (row: CategoryFlatVO) => formatDate(row.createTime) },
  { title: '更新时间', key: 'updateTime', minWidth: 120, render: (row: CategoryFlatVO) => formatDate(row.updateTime) },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row: CategoryFlatVO) =>
      [
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
              value: 'category:view:all'
            },
            onClick: () => handleView(row)
          },
          { default: () => '查看' }
        ),
        // 删除按钮（有权限才显示）
        auth.hasPermission('category:delete:all') &&
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              ghost: true,
              onClick: () => handleDelete(row)
            },
            { default: () => '删除' }
          )
      ].filter(Boolean)
  }
]

function getParentIdByParent(parent: CategoryFlatVO | undefined) {
  if (!parent) return null
  return parent.id ?? null
}

// 新增分类
function handleAddCategory() {
  const addData = ref<{ name: string; description: string; parent?: CategoryFlatVO }>({
    name: '',
    description: '',
    parent: undefined
  })
  modal.create({
    title: '新增分类',
    preset: 'dialog',
    style: { width: '420px' },
    content: () =>
      h(NForm, { model: addData.value, labelWidth: 80 }, () => [
        h(NFormItemRow, { label: '分类名称', path: 'name' }, () =>
          h(NInput, {
            value: addData.value.name,
            placeholder: '请输入分类名称',
            onUpdateValue: (v: string) => (addData.value.name = v)
          })
        ),
        h(NFormItemRow, { label: '描述', path: 'description' }, () =>
          h(NInput, {
            value: addData.value.description,
            placeholder: '请输入描述',
            onUpdateValue: (v: string) => (addData.value.description = v)
          })
        ),
        h(NFormItemRow, { label: '父节点', path: 'parent' }, () =>
          h(NSelect, {
            value: addData.value.parent ? addData.value.parent.id : null,
            options: [{ label: '无（根节点）', value: null as unknown as number }, ...parentOptions.value],
            placeholder: '请选择父节点',
            clearable: true,
            onUpdateValue: (v: number | null) => {
              addData.value.parent = categories.value.find(c => c.id === v) ?? undefined
            }
          })
        )
      ]),
    positiveText: '保存',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const payload = {
          name: addData.value.name,
          description: addData.value.description,
          parentId: getParentIdByParent(addData.value.parent)
        }
        const res = await createCategory(payload)
        if (res.code === 200) {
          message.success('新增分类成功')
          fetchCategories()
        } else {
          message.error(res.message || '新增分类失败')
        }
      } catch (e) {
        message.error('请求异常')
      }
    }
  })
}

// 查看分类
function handleView(row: CategoryFlatVO) {
  const viewData = ref<{ name?: string; description?: string; parent?: CategoryFlatVO }>({
    ...row,
    parent: typeof row.parent === 'object' && row.parent !== null ? categories.value.find(c => c.id === (row.parent as CategoryFlatVO).id) : undefined
  })
  const editable = ref(false)
  modal.create({
    title: '查看分类',
    preset: 'dialog',
    style: { width: '420px' },
    content: () =>
      h(NForm, { model: viewData.value, labelWidth: 80 }, () => [
        h(NFormItemRow, { label: '分类名称', path: 'name' }, () =>
          h(NInput, {
            value: viewData.value.name,
            disabled: !editable.value,
            placeholder: '请输入分类名称',
            onUpdateValue: (v: string) => (viewData.value.name = v)
          })
        ),
        h(NFormItemRow, { label: '描述', path: 'description' }, () =>
          h(NInput, {
            value: viewData.value.description,
            disabled: !editable.value,
            placeholder: '请输入描述',
            onUpdateValue: (v: string) => (viewData.value.description = v)
          })
        ),
        h(NFormItemRow, { label: '父节点', path: 'parent' }, () =>
          h(NSelect, {
            value: viewData.value.parent ? viewData.value.parent.id : null,
            options: [{ label: '无（根节点）', value: null as unknown as number }, ...parentOptions.value],
            placeholder: '请选择父节点',
            clearable: true,
            disabled: !editable.value,
            onUpdateValue: (v: number | null) => {
              viewData.value.parent = categories.value.find(c => c.id === v)
            }
          })
        )
      ]),
    action: () =>
      editable.value
        ? [
            h(
              NButton,
              {
                type: 'primary',
                disabled: !auth.hasPermission('category:edit:all'),
                onClick: async () => {
                  try {
                    const payload = {
                      name: viewData.value.name,
                      description: viewData.value.description,
                      parentId: getParentIdByParent(viewData.value.parent)
                    }
                    const res = await updateCategory(row.id, payload)
                    if (res.code === 200) {
                      message.success('编辑分类成功')
                      fetchCategories()
                      editable.value = false
                      modal.destroyAll() // 编辑成功后关闭弹窗
                    } else {
                      message.error(res.message || '编辑分类失败')
                    }
                  } catch (e) {
                    message.error('请求异常')
                  }
                }
              },
              { default: () => '保存' }
            ),
            h(
              NButton,
              {
                style: 'margin-left: 8px',
                onClick: () => (editable.value = false)
              },
              { default: () => '取消' }
            )
          ]
        : [
            auth.hasPermission('category:edit:all') &&
              h(
                NButton,
                {
                  type: 'primary',
                  onClick: () => (editable.value = true)
                },
                { default: () => '编辑' }
              )
          ].filter(Boolean),
    showIcon: false
  })
}

// 删除分类
function handleDelete(row: CategoryFlatVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除分类 ${row.name} 吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await deleteCategory(row.id)
        if (res.code === 200) {
          message.success('删除成功')
          fetchCategories()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (e) {
        message.error('请求异常')
      }
    }
  })
}

// 批量删除分类
function handleBatchDelete() {
  if (!selectedRowKeys.value.length) return
  modal.create({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个分类吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteCategories(selectedRowKeys.value as number[])
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchCategories()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (e) {
        message.error('请求异常')
      }
    }
  })
}
</script>

<style scoped>
.categories-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}
.categories-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}
.categories-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
.categories-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
</style>
