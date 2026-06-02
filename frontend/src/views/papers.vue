<template>
  <div class="papers-page">
    <n-card :bordered="false" class="papers-card">
      <n-h1 class="papers-title">试卷管理</n-h1>
      <div class="papers-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索试卷" clearable style="width: 200px" @input="handleSearch" />
        <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态" style="width: 120px" @update:value="handleSearch" />
        <n-button v-permission="'paper:create'" type="primary" @click="handleCreatePaper">新建试卷</n-button>
        <n-button v-permission="'paper:create'" type="info" @click="handleSmartGenerate">智能组卷</n-button>
      </div>
      <n-data-table
        v-permission="'paper:list:all'"
        remote
        :columns="columns"
        :data="papers"
        :pagination="pagination"
        :loading="loadingPapers"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 试卷详情模态框 -->
    <paper-detail-modal
      :show="showDetailModal"
      :loading="updatingPaper"
      :paper="selectedPaper"
      :can-edit="canEditPaper"
      @update:show="updateDetailModalShow"
      @submit="handleUpdatePaper"
      @publish="handlePublishPaper"
      @archive="handleArchivePaper"
      @manage-questions="handleManageQuestions"
    />

    <!-- 试卷创建模态框 -->
    <paper-create-modal :show="showCreateModal" :loading="creatingPaper" @update:show="updateCreateModalShow" @submit="handleCreatePaperSubmit" />

    <!-- 智能组卷模态框 -->
    <smart-paper-modal :show="showSmartModal" :loading="smartGenerating" @update:show="updateSmartModalShow" @submit="handleSmartGenerateSubmit" />

    <!-- 题目管理模态框 -->
    <paper-questions-modal :show="showQuestionsModal" :paper-id="selectedPaper?.id ?? null" @update:show="updateQuestionsModalShow" @success="handleQuestionsUpdated" />
  </div>
</template>

<script lang="ts" setup>
import { NButton, NTag, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { archivePaper, createPaper, deletePaper, getPaperDetail, getPapers, publishPaper, smartGenerate, updatePaper } from '@/api/paper'
import PaperCreateModal from '@/components/paper/paper-create-modal.vue'
import PaperDetailModal from '@/components/paper/paper-detail-modal.vue'
import PaperQuestionsModal from '@/components/paper/paper-questions-modal.vue'
import SmartPaperModal from '@/components/paper/smart-paper-modal.vue'
import { useAuthStore } from '@/store/auth'

import type { DataTableColumns } from 'naive-ui'
import type { PaperCreateDTO, PaperDetailVO, PaperListVO, PaperUpdateDTO, SmartPaperDTO } from '@/api/types'

const auth = useAuthStore()
const message = useMessage()
const modal = useModal()

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref<string | null>(null)

// 表格数据
const papers = ref<PaperListVO[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

// 模态框状态
const showDetailModal = ref(false)
const showCreateModal = ref(false)
const showSmartModal = ref(false)
const showQuestionsModal = ref(false)
const selectedPaper = ref<PaperDetailVO | null>(null)

// 状态选项
const statusOptions = [
  { label: '全部', value: null },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

// 表格列定义
const columns: DataTableColumns<PaperListVO> = [
  {
    title: '试卷标题',
    key: 'title',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '教师',
    key: 'teacherName',
    width: 120
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => h(NTag, { type: getStatusType(row.status) }, { default: () => getStatusText(row.status) })
  },
  {
    title: '总分',
    key: 'totalScore',
    width: 80
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: row => new Date(row.createTime).toLocaleString()
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    render: row => {
      const actions = []

      if (auth.hasPermission('paper:view:all') || auth.hasPermission('paper:view:self')) {
        actions.push(h(NButton, { size: 'small', onClick: () => handleViewPaper(row.id) }, { default: () => '查看' }))
      }

      if ((auth.hasPermission('paper:publish:all') || auth.hasPermission('paper:publish:self')) && row.status === 'DRAFT') {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'success',
              onClick: () => handlePublishPaper(row.id)
            },
            { default: () => '发布' }
          )
        )
      }

      if ((auth.hasPermission('paper:archive:all') || auth.hasPermission('paper:archive:self')) && row.status === 'PUBLISHED') {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'warning',
              onClick: () => handleArchivePaper(row.id)
            },
            { default: () => '归档' }
          )
        )
      }

      if (auth.hasPermission('paper:delete:all') || auth.hasPermission('paper:delete:self')) {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => handleDeletePaper(row.id)
            },
            { default: () => '删除' }
          )
        )
      }

      return h('div', { style: 'display: flex; gap: 8px; flex-wrap: wrap;' }, actions)
    }
  }
]

// 权限计算
const canEditPaper = computed(() => auth.hasPermission('paper:edit:all') || auth.hasPermission('paper:edit:self'))

// 获取试卷数据
const { loading: loadingPapers, run: fetchPapers } = useRequest(
  () =>
    getPapers({
      page: pagination.page,
      size: pagination.pageSize,
      title: searchKeyword.value || undefined,
      status: statusFilter.value || undefined
    }),
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        papers.value = res.data.content
        pagination.itemCount = res.data.total
      } else {
        message.error(res.message || '获取试卷列表失败')
      }
    }
  }
)

// 创建试卷
const { loading: creatingPaper, runAsync: runCreatePaper } = useRequest(createPaper, { manual: true })

// 更新试卷
const { loading: updatingPaper, runAsync: runUpdatePaper } = useRequest(updatePaper, { manual: true })

// 智能组卷
const { loading: smartGenerating, runAsync: runSmartGenerate } = useRequest(smartGenerate, { manual: true })

// 工具函数
function rowKey(row: PaperListVO) {
  return row.id
}

function getStatusType(status: string) {
  switch (status) {
    case 'PUBLISHED':
      return 'success'
    case 'ARCHIVED':
      return 'warning'
    default:
      return 'default'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'PUBLISHED':
      return '已发布'
    case 'ARCHIVED':
      return '已归档'
    default:
      return '草稿'
  }
}

// 事件处理
function handleSearch() {
  pagination.page = 1
  fetchPapers()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchPapers()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchPapers()
}

function handleCreatePaper() {
  showCreateModal.value = true
}

function handleSmartGenerate() {
  showSmartModal.value = true
}

async function handleCreatePaperSubmit(data: PaperCreateDTO) {
  try {
    const res = await runCreatePaper(data)
    if (res.code === 200) {
      message.success('创建试卷成功')
      showCreateModal.value = false
      fetchPapers()
      if (auth.hasPermission('paper:edit:all') || auth.hasPermission('paper:edit:self')) {
        await handleViewPaper(res.data.id)
      }
    } else {
      message.error(res.message || '创建试卷失败')
    }
  } catch (error: any) {
    message.error(error.message || '创建试卷失败')
  }
}

async function handleSmartGenerateSubmit(data: SmartPaperDTO) {
  try {
    const res = await runSmartGenerate(data)
    if (res.code === 200) {
      message.success('智能组卷成功')
      showSmartModal.value = false
      fetchPapers()
    } else {
      message.error(res.message || '智能组卷失败')
    }
  } catch (error: any) {
    message.error(error.message || '智能组卷失败')
  }
}

async function handleViewPaper(id: number) {
  try {
    const res = await getPaperDetail(id)
    if (res.code === 200) {
      selectedPaper.value = res.data
      showDetailModal.value = true
    } else {
      message.error(res.message || '获取试卷详情失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取试卷详情失败')
  }
}

async function handleEditPaper(id: number) {
  await handleViewPaper(id)
}

async function handleUpdatePaper(id: number, data: PaperUpdateDTO) {
  try {
    const res = await runUpdatePaper(id, data)
    if (res.code === 200) {
      message.success('更新试卷成功')
      selectedPaper.value = res.data
      showDetailModal.value = false
      fetchPapers()
    } else {
      message.error(res.message || '更新试卷失败')
    }
  } catch (error: any) {
    message.error(error.message || '更新试卷失败')
  }
}

async function handlePublishPaper(id: number) {
  try {
    const res = await publishPaper(id)
    if (res.code === 200) {
      message.success('发布试卷成功')
      fetchPapers()
    } else {
      message.error(res.message || '发布试卷失败')
    }
  } catch (error: any) {
    message.error(error.message || '发布试卷失败')
  }
}

async function handleArchivePaper(id: number) {
  try {
    const res = await archivePaper(id)
    if (res.code === 200) {
      message.success('归档试卷成功')
      fetchPapers()
    } else {
      message.error(res.message || '归档试卷失败')
    }
  } catch (error: any) {
    message.error(error.message || '归档试卷失败')
  }
}

async function handleDeletePaper(id: number) {
  modal.create({
    title: '删除试卷',
    content: '确定要删除这份试卷吗？删除后无法恢复。',
    positiveText: '删除',
    negativeText: '取消',
    type: 'error',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await deletePaper(id)
        if (res.code === 200) {
          message.success('删除试卷成功')
          fetchPapers()
        } else {
          message.error(res.message || '删除试卷失败')
        }
      } catch (error: any) {
        message.error(error.message || '删除试卷失败')
      }
    }
  })
}

function handleManageQuestions() {
  showQuestionsModal.value = true
}

function handleQuestionsUpdated() {
  fetchPapers()
  if (selectedPaper.value) {
    handleViewPaper(selectedPaper.value.id)
  }
}

// 模态框状态更新
function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

function updateCreateModalShow(show: boolean) {
  showCreateModal.value = show
}

function updateSmartModalShow(show: boolean) {
  showSmartModal.value = show
}

function updateQuestionsModalShow(show: boolean) {
  showQuestionsModal.value = show
}

onMounted(() => {
  fetchPapers()
})

// // 自动监听关键词 / 状态变动
// watch([searchKeyword, statusFilter], () => {
//   pagination.page = 1
//   fetchPapers()
// })
</script>

<style scoped>
.papers-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 0 24px;
}

.papers-card {
  width: 100%;
  box-shadow: 0 2px 12px #0001;
  border-radius: 12px;
  padding: 24px 32px;
}

.papers-title {
  margin-bottom: 16px;
  font-size: 2.2rem;
  letter-spacing: 2px;
}

.papers-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>

