<template>
  <div class="knowledge-points-page">
    <n-card :bordered="false" class="knowledge-points-card">
      <n-h1 class="knowledge-points-title">全部知识点</n-h1>
      <div class="knowledge-points-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索知识点名称/描述" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="courseFilter" :options="courseOptions" clearable placeholder="课程" style="width: 180px" @update:value="handleSearch" />
        <n-select v-model:value="difficultyFilter" :options="difficultyOptions" clearable placeholder="难度" style="width: 120px" @update:value="handleSearch" />
        <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态" style="width: 120px" @update:value="handleSearch" />
        <n-button type="primary" @click="handleCreateKnowledgePoint" v-permission="'knowledge_point:create:all'">新建知识点</n-button>
        <n-button type="info" @click="showImport = true" v-permission="'knowledge_point:import'">导入知识点</n-button>
        <n-button type="warning" @click="showExport = true" v-permission="'knowledge_point:export'">导出知识点</n-button>
        <n-button type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete" v-permission="'knowledge_point:delete:all'">批量删除</n-button>
      </div>
      <n-data-table
        v-permission="'knowledge_point:list:all'"
        remote
        :columns="columns"
        :data="knowledgePoints"
        :pagination="pagination"
        :loading="loadingKnowledgePoints"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        :checked-row-keys="selectedRowKeys"
        @update:checked-row-keys="updateSelectedRowKeys"
        @update:page="handlePageChange"
      />
    </n-card>

    <!-- 知识点详情模态框 -->
    <knowledge-point-detail-modal
      :show="showDetailModal"
      :loading="updatingKnowledgePoint"
      :updating-status="updatingStatus"
      :courses="courses"
      :knowledge-point="selectedKnowledgePoint"
      :can-edit="true"
      :can-update-status="true"
      @update:show="updateDetailModalShow"
      @submit="handleUpdateKnowledgePoint"
      @toggle-status="handleToggleStatus"
    />

    <!-- 知识点创建模态框 -->
    <knowledge-point-create-modal
      :show="showCreateModal"
      :loading="creatingKnowledgePoint"
      :courses="courses"
      @update:show="updateCreateModalShow"
      @submit="handleCreateKnowledgePointSubmit"
    />

    <!-- 导出模态框 -->
    <knowledge-point-export-modal :show="showExport" :data="courses" @update:show="showExport = $event" @success="fetchKnowledgePoints" />

    <!-- 导入模态框 -->
    <knowledge-point-import-modal :show="showImport" :data="courses" @update:show="showImport = $event" @success="fetchKnowledgePoints" />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NSwitch, NTag, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getCourses } from '@/api/course'
import {
  adminCreateKnowledgePoint,
  adminDeleteKnowledgePoint,
  adminUpdateKnowledgePoint,
  batchDeleteKnowledgePoints,
  createKnowledgePoint,
  deleteKnowledgePoint,
  exportKnowledgePoints,
  exportMyKnowledgePoints, // 新增
  getKnowledgePointDetail,
  getKnowledgePointPage,
  getMyKnowledgePointPage, // 新增
  importKnowledgePoints,
  updateKnowledgePoint,
  updateKnowledgePointStatus
} from '@/api/knowledge-point'
import KnowledgePointCreateModal from '@/components/knowledge-point/knowledge-point-create-modal.vue'
import KnowledgePointDetailModal from '@/components/knowledge-point/knowledge-point-detail-modal.vue'
import KnowledgePointExportModal from '@/components/knowledge-point/knowledge-point-export-modal.vue'
import KnowledgePointImportModal from '@/components/knowledge-point/knowledge-point-import-modal.vue'
import { useAuthStore } from '@/store/auth'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CourseVO, CreateKnowledgePointDTO, KnowledgePointDetailVO, KnowledgePointVO, UpdateKnowledgePointDTO, UserVO } from '@/api/types'

const message = useMessage()
const modal = useModal()
const auth = useAuthStore()

const knowledgePoints = ref<KnowledgePointVO[]>([])
const courses = ref<CourseVO[]>([])
const searchKeyword = ref('')
const courseFilter = ref<number | null>(null)
const difficultyFilter = ref<string | null>(null)
const statusFilter = ref<boolean | null>(null)
const selectedRowKeys = ref<DataTableRowKey[]>([])
const selectedKnowledgePoint = ref<KnowledgePointDetailVO | null>(null)
const showDetailModal = ref(false)
const showCreateModal = ref(false)
const showExport = ref(false)
const showImport = ref(false)

const rowKey = (row: KnowledgePointVO) => row.id

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
    fetchKnowledgePoints()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchKnowledgePoints()
  }
})

const courseOptions = computed(() => [{ label: '全部', value: null }, ...courses.value.map(c => ({ label: c.name, value: c.id }))])

const difficultyOptions = [
  { label: '全部', value: null },
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

const statusOptions = [
  { label: '全部', value: null },
  { label: '启用', value: true },
  { label: '禁用', value: false }
]

// 加载数据
const { loading: loadingKnowledgePoints, run: fetchKnowledgePoints } = useRequest(
  () => {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value,
      courseId: courseFilter.value || undefined,
      difficulty: difficultyFilter.value || undefined,
      enabled: statusFilter.value !== null ? statusFilter.value : undefined
    }
    return getKnowledgePointPage(params)
  },
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        knowledgePoints.value = res.data.content
        pagination.itemCount = res.data.total
      } else {
        message.error(res.message || '获取知识点列表失败')
      }
    }
  }
)

const { run: fetchCourses } = useRequest(getCourses, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      courses.value = res.data
    }
  }
})

const { loading: creatingKnowledgePoint, runAsync: runCreateKnowledgePoint } = useRequest(adminCreateKnowledgePoint, { manual: true })
const { loading: updatingKnowledgePoint, runAsync: runUpdateKnowledgePoint } = useRequest(adminUpdateKnowledgePoint, { manual: true })
const { loading: updatingStatus, runAsync: runUpdateStatus } = useRequest(updateKnowledgePointStatus, { manual: true })

onMounted(() => {
  fetchKnowledgePoints()
  fetchCourses()
})

function getDifficultyType(difficulty: string) {
  switch (difficulty) {
    case 'EASY':
      return 'success'
    case 'MEDIUM':
      return 'warning'
    case 'HARD':
      return 'error'
    default:
      return 'default'
  }
}

function getDifficultyText(difficulty: string) {
  switch (difficulty) {
    case 'EASY':
      return '简单'
    case 'MEDIUM':
      return '中等'
    case 'HARD':
      return '困难'
    default:
      return '未知'
  }
}

function getStatusType(enabled: boolean) {
  return enabled ? 'success' : 'warning'
}

function getStatusText(enabled: boolean) {
  return enabled ? '启用' : '禁用'
}

// 表格列定义
const columns: DataTableColumns<KnowledgePointVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  { title: '知识点名称', key: 'name', minWidth: 200 },
  { title: '所属课程', key: 'courseName', width: 150 },
  {
    title: '难度',
    key: 'difficulty',
    width: 100,
    render: (row: KnowledgePointVO) =>
      h(
        NTag,
        {
          type: getDifficultyType(row.difficulty),
          style: { cursor: 'pointer' },
          onClick: () => handleDifficultyTagClick(row.difficulty)
        },
        { default: () => getDifficultyText(row.difficulty) }
      )
  },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render: (row: KnowledgePointVO) => {
      // 有权限可切换状态，否则显示标签
      if (auth.hasPermission('knowledge_point:edit:all')) {
        return h(
          NSwitch,
          {
            size: 'small',
            value: row.enabled,
            onClick: async () => {
              if (!auth.hasPermission('knowledge_point:edit:all')) return
              try {
                const res = await runUpdateStatus(row.id, !row.enabled)
                if (res.code === 200) {
                  // 直接更新本地数据，避免重新请求
                  const index = knowledgePoints.value.findIndex(kp => kp.id === row.id)
                  if (index !== -1) {
                    knowledgePoints.value[index].enabled = !row.enabled
                  }
                  message.success(`${row.enabled ? '启用' : '禁用'}知识点成功`)
                } else {
                  message.error(res.message || '操作失败')
                }
              } catch (e: any) {
                message.error(e.message || '操作失败')
              }
            }
          },
          { default: () => (row.enabled ? '启用中' : '已禁用') }
        )
      } else {
        return h(
          NTag,
          {
            type: getStatusType(row.enabled),
            style: { cursor: 'pointer' },
            onClick: () => handleStatusTagClick(row.enabled)
          },
          { default: () => getStatusText(row.enabled) }
        )
      }
    }
  },
  { title: '创建时间', key: 'createTime', width: 120, render: (row: KnowledgePointVO) => row.createTime.slice(0, 10) },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row: KnowledgePointVO) =>
      [
        // 查看按钮
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            ghost: true,
            style: 'margin-right: 8px',
            onClick: () => handleViewKnowledgePoint(row)
          },
          { default: () => '查看' }
        ),
        // 删除按钮（有权限才显示）
        auth.hasPermission('knowledge_point:delete:all') &&
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              ghost: true,
              onClick: () => handleDeleteKnowledgePoint(row)
            },
            { default: () => '删除' }
          )
      ].filter(Boolean)
  }
]

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchKnowledgePoints()
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
}

// 更新选中的行
function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

// 更新详情模态框显示状态
function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

// 更新创建模态框显示状态
function updateCreateModalShow(show: boolean) {
  showCreateModal.value = show
}

// 打开创建知识点模态框
function handleCreateKnowledgePoint() {
  showCreateModal.value = true
}

// 创建知识点提交
async function handleCreateKnowledgePointSubmit(data: CreateKnowledgePointDTO & { createdBy?: number }) {
  try {
    const createFn = data.createdBy ? adminCreateKnowledgePoint : runCreateKnowledgePoint
    const res = await (createFn as any)(data)
    if (res.code === 200) {
      message.success('创建知识点成功')
      showCreateModal.value = false
      fetchKnowledgePoints()
    } else {
      message.error(res.message || '创建知识点失败')
    }
  } catch (e: any) {
    message.error(e.message || '创建知识点异常')
  }
}

// 查看知识点详情
async function handleViewKnowledgePoint(knowledgePoint: KnowledgePointVO) {
  try {
    const res = await getKnowledgePointDetail(knowledgePoint.id)
    if (res.code === 200) {
      selectedKnowledgePoint.value = res.data
      showDetailModal.value = true
    } else {
      message.error(res.message || '获取知识点详情失败')
    }
  } catch (e: any) {
    message.error(e.message || '获取知识点详情异常')
  }
}

// 更新知识点
async function handleUpdateKnowledgePoint({ id, data }: { id: number; data: UpdateKnowledgePointDTO }) {
  try {
    const updateFn = auth.hasPermission('knowledge:edit:admin') ? adminUpdateKnowledgePoint : runUpdateKnowledgePoint
    const res = await updateFn(id, data)
    if (res.code === 200) {
      message.success('更新知识点成功')
      showDetailModal.value = false
      fetchKnowledgePoints()
    } else {
      message.error(res.message || '更新知识点失败')
    }
  } catch (e: any) {
    message.error(e.message || '更新知识点异常')
  }
}

// 切换知识点状态
async function handleToggleStatus({ id, enabled }: { id: number; enabled: boolean }) {
  try {
    const res = await runUpdateStatus(id, enabled)
    if (res.code === 200) {
      message.success(`${enabled ? '启用' : '禁用'}知识点成功`)
      // 更新详情模态框中的数据
      if (selectedKnowledgePoint.value && selectedKnowledgePoint.value.id === id) {
        selectedKnowledgePoint.value.enabled = enabled
      }
      // 刷新列表数据
      fetchKnowledgePoints()
    } else {
      message.error(res.message || '更新状态失败')
    }
  } catch (e: any) {
    message.error(e.message || '更新状态异常')
  }
}

// 删除知识点
function handleDeleteKnowledgePoint(knowledgePoint: KnowledgePointVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除知识点"${knowledgePoint.name}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await adminDeleteKnowledgePoint(knowledgePoint.id)
        if (res.code === 200) {
          message.success('删除成功')
          fetchKnowledgePoints()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '删除异常')
      }
    }
  })
}

// 批量删除知识点
function handleBatchDelete() {
  if (!selectedRowKeys.value.length) return
  modal.create({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个知识点吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteKnowledgePoints(selectedRowKeys.value as number[])
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchKnowledgePoints()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '批量删除异常')
      }
    }
  })
}

async function handleExport() {
  try {
    // 公共查询参数
    const common = {
      courseId: courseFilter.value || undefined,
      enabled: statusFilter.value !== null ? statusFilter.value : undefined,
      difficulty: difficultyFilter.value || undefined
    }
    // 根据权限选择导出接口
    const blob = auth.hasPermission('knowledge_point:export') ? await exportKnowledgePoints({ ...common, format: 'xlsx' }) : await exportMyKnowledgePoints(common)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `知识点列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (e: any) {
    message.error(e.message || '导出失败')
  }
}

async function handleImport(data: { file: { file: File } }) {
  if (!courseFilter.value) {
    message.error('请先选择课程')
    return false
  }
  try {
    const res = await importKnowledgePoints(data.file.file, courseFilter.value)
    if (res.code === 200) {
      message.success(`导入成功：成功 ${res.data.successCount} 条，失败 ${res.data.failCount} 条`)
      if (res.data.errorMessages && res.data.errorMessages.length > 0) {
        console.warn('导入错误详情：', res.data.errorMessages)
      }
      fetchKnowledgePoints()
    } else {
      message.error(res.message || '导入失败')
    }
  } catch (e: any) {
    message.error(e.message || '导入异常')
  }
  return false
}

// 处理难度标签点击事件
function handleDifficultyTagClick(difficulty: string) {
  // 如果当前筛选的难度和点击的难度相同，则清除筛选
  if (difficultyFilter.value === difficulty) {
    difficultyFilter.value = null
    message.info('已清除难度筛选')
  } else {
    // 否则设置新的难度筛选
    difficultyFilter.value = difficulty
    message.info(`已筛选难度：${getDifficultyText(difficulty)}`)
  }

  // 重新搜索
  handleSearch()
}

// 处理状态标签点击事件（针对没有编辑权限的用户）
function handleStatusTagClick(enabled: boolean) {
  // 如果当前筛选的状态和点击的状态相同，则清除筛选
  if (statusFilter.value === enabled) {
    statusFilter.value = null
    message.info('已清除状态筛选')
  } else {
    // 否则设置新的状态筛选
    statusFilter.value = enabled
    message.info(`已筛选状态：${getStatusText(enabled)}`)
  }

  // 重新搜索
  handleSearch()
}
</script>

<style scoped>
.knowledge-points-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}
.knowledge-points-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}
.knowledge-points-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}
.knowledge-points-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}
</style>

