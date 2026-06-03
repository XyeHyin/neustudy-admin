<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-card">
      <n-h1 class="admin-title">我的课程</n-h1>
      <div class="admin-toolbar">
        <n-input v-model:value="searchKeyword" placeholder="搜索课程标题/描述" clearable style="width: 240px" @input="handleSearch" />
        <n-select v-model:value="subjectFilter" :options="subjectOptions" clearable placeholder="学科" style="width: 120px" @update:value="handleSearch" />
        <n-select v-model:value="gradeFilter" :options="gradeOptions" clearable placeholder="年级" style="width: 120px" @update:value="handleSearch" />
        <n-select v-model:value="statusFilter" :options="statusOptions" clearable placeholder="状态" style="width: 120px" @update:value="handleSearch" />
        <n-button v-permission="'course:create:self'" type="primary" @click="handleCreateCourse">新增课程</n-button>
        <n-button v-permission="'course:delete:self'" type="error" :disabled="!selectedRowKeys.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <n-data-table
        v-permission="'course:list:self'"
        remote
        :columns="columns"
        :data="courses"
        :pagination="pagination"
        :loading="loadingCourses"
        :bordered="false"
        style="margin-top: 24px"
        :row-key="rowKey"
        :checked-row-keys="selectedRowKeys"
        @update:checked-row-keys="updateSelectedRowKeys"
        @update:page="handlePageChange"
      />
    </n-card>

    <!-- 课程详情模态框 -->
    <course-detail-modal
      :show="showDetailModal"
      :loading="updatingCourse"
      :publishing="publishingCourse"
      :archiving="archivingCourse"
      :course="selectedCourse"
      :can-edit="true"
      :can-publish="true"
      :can-archive="true"
      @update:show="updateDetailModalShow"
      @submit="handleUpdateCourse"
      @publish="handlePublishCourse"
      @archive="handleArchiveCourse"
    />

    <!-- 课程创建模态框 -->
    <course-create-modal :show="showCreateModal" :loading="creatingCourse" @update:show="updateCreateModalShow" @submit="handleCreateCourseSubmit" />
  </div>
</template>

<script lang="ts" setup>
import { getCourseStatusText as getStatusText, getCourseStatusType as getStatusType } from '@/utils/status'
import { NButton, NTag, useMessage, useModal } from 'naive-ui'
import { computed, h, onMounted, reactive, ref } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getCategories } from '@/api/categories'
import { adminDeleteCourse, archiveCourse, batchDeleteCourses, createCourse, deleteCourse, getCourseDetail, getMyCoursePage, publishCourse, updateCourse } from '@/api/course'
import CourseCreateModal from '@/components/course/course-create-modal.vue'
import CourseDetailModal from '@/components/course/course-detail-modal.vue'
import { useAuthStore } from '@/store/auth'
import { formatDate } from '@/utils/datetime'
import { COURSE_STATUS_FILTER_OPTIONS as statusOptions } from '@/constants/options'

import type { DataTableColumns, DataTableRowKey } from 'naive-ui'
import type { CategoryFlatVO, CourseDetailVO, CourseVO, CreateCourseDTO, UpdateCourseDTO } from '@/api/types'

const message = useMessage()
const modal = useModal()

const courses = ref<CourseVO[]>([])
const categories = ref<CategoryFlatVO[]>([])
const searchKeyword = ref('')
const subjectFilter = ref<string | null>(null)
const gradeFilter = ref<string | null>(null)
const statusFilter = ref<string | null>(null)
const selectedRowKeys = ref<DataTableRowKey[]>([])
const selectedCourse = ref<CourseDetailVO | null>(null)
const showDetailModal = ref(false)
const showCreateModal = ref(false)

const auth = useAuthStore()

const rowKey = (row: CourseVO) => row.id

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
    fetchCourses()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchCourses()
  }
})

// 从分类中获取学科和年级选项
const subjectOptions = computed(() => {
  const subjects = categories.value.filter(cat => cat.parent && typeof cat.parent === 'object' && cat.parent.name === '学科').map(cat => ({ label: cat.name, value: cat.name }))
  return [{ label: '全部', value: null }, ...subjects]
})

const gradeOptions = computed(() => {
  const grades = categories.value.filter(cat => cat.parent && typeof cat.parent === 'object' && cat.parent.name === '年级').map(cat => ({ label: cat.name, value: cat.name }))
  return [{ label: '全部', value: null }, ...grades]
})



// 获取分类数据
const { run: fetchCategories } = useRequest(getCategories, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      categories.value = res.data || []
    }
  }
})

// 加载数据
const { loading: loadingCourses, run: fetchCourses } = useRequest(
  () =>
    getMyCoursePage({
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchKeyword.value,
      subject: subjectFilter.value || undefined,
      grade: gradeFilter.value || undefined,
      status: statusFilter.value || undefined
    }),
  {
    onSuccess: (res: any) => {
      if (res.code === 200) {
        courses.value = res.data.content
        pagination.itemCount = res.data.total
      } else {
        message.error(res.message || '获取我的课程列表失败')
      }
    }
  }
)

const { loading: creatingCourse, runAsync: runCreateCourse } = useRequest(createCourse, { manual: true })
const { loading: updatingCourse, runAsync: runUpdateCourse } = useRequest(updateCourse, { manual: true })
const { loading: publishingCourse, runAsync: runPublishCourse } = useRequest(publishCourse, { manual: true })
const { loading: archivingCourse, runAsync: runArchiveCourse } = useRequest(archiveCourse, { manual: true })

onMounted(() => {
  fetchCategories()
  fetchCourses()
})

// 表格列定义
const columns: DataTableColumns<CourseVO> = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 60 },
  { title: '课程名称', key: 'name', minWidth: 200 },
  { title: '学科', key: 'subject', width: 100 },
  { title: '年级', key: 'grade', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: CourseVO) =>
      // 状态标签，点击可筛选
      h(
        NTag,
        {
          type: getStatusType(row.status),
          style: { cursor: 'pointer' },
          onClick: () => handleStatusTagClick(row.status)
        },
        { default: () => getStatusText(row.status) }
      )
  },
  { title: '总学时', key: 'totalHours', width: 80, render: (row: CourseVO) => `${row.totalHours || 0}h` },
  { title: '已完成', key: 'completedHours', width: 80, render: (row: CourseVO) => `${row.completedHours || 0}h` },
  { title: '创建时间', key: 'createTime', width: 120, render: (row: CourseVO) => formatDate(row.createTime) },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row: CourseVO) => [
      // 查看按钮
      h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          ghost: true,
          style: 'margin-right: 8px',
          onClick: () => handleViewCourse(row)
        },
        { default: () => '查看' }
      ),
      // 删除按钮
      h(
        NButton,
        {
          size: 'small',
          type: 'error',
          ghost: true,
          onClick: () => handleDeleteCourse(row)
        },
        { default: () => '删除' }
      )
    ]
  }
]

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchCourses()
}

// 分页切换
function handlePageChange(page: number) {
  pagination.page = page
}

// 选中行变化
function updateSelectedRowKeys(keys: DataTableRowKey[]) {
  selectedRowKeys.value = keys
}

// 更新详情模态框显示
function updateDetailModalShow(show: boolean) {
  showDetailModal.value = show
}

// 更新创建模态框显示
function updateCreateModalShow(show: boolean) {
  showCreateModal.value = show
}

// 新建课程
function handleCreateCourse() {
  showCreateModal.value = true
}

// 新建课程提交
async function handleCreateCourseSubmit(data: CreateCourseDTO) {
  try {
    const res = await runCreateCourse(data)
    if (res.code === 200) {
      message.success('创建课程成功')
      showCreateModal.value = false
      fetchCourses()
    } else {
      message.error(res.message || '创建课程失败')
    }
  } catch (e: any) {
    message.error(e.message || '创建课程异常')
  }
}

// 查看课程详情
async function handleViewCourse(course: CourseVO) {
  try {
    const res = await getCourseDetail(course.id)
    if (res.code === 200) {
      selectedCourse.value = res.data
      showDetailModal.value = true
    } else {
      message.error(res.message || '获取课程详情失败')
    }
  } catch (e: any) {
    message.error(e.message || '获取课程详情异常')
  }
}

async function handleUpdateCourse({ id, data }: { id: number; data: UpdateCourseDTO }) {
  try {
    const res = await runUpdateCourse(id, data)
    if (res.code === 200) {
      message.success('更新课程成功')
      showDetailModal.value = false
      fetchCourses()
    } else {
      message.error(res.message || '更新课程失败')
    }
  } catch (e: any) {
    message.error(e.message || '更新课程异常')
  }
}

async function handlePublishCourse(courseId: number) {
  try {
    const res = await runPublishCourse(courseId)
    if (res.code === 200) {
      message.success('发布课程成功')
      showDetailModal.value = false
      fetchCourses()
    } else {
      message.error(res.message || '发布课程失败')
    }
  } catch (e: any) {
    message.error(e.message || '发布课程异常')
  }
}

async function handleArchiveCourse(courseId: number) {
  try {
    const res = await runArchiveCourse(courseId)
    if (res.code === 200) {
      message.success('归档课程成功')
      showDetailModal.value = false
      fetchCourses()
    } else {
      message.error(res.message || '归档课程失败')
    }
  } catch (e: any) {
    message.error(e.message || '归档课程异常')
  }
}

// 删除课程
function handleDeleteCourse(course: CourseVO) {
  modal.create({
    title: '确认删除',
    content: `确定要删除课程"${course.name}"吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        let res
        if (auth.hasPermission('course:delete:all')) {
          res = await adminDeleteCourse(course.id)
        } else {
          res = await deleteCourse(course.id)
        }
        if (res.code === 200) {
          message.success('删除成功')
          fetchCourses()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '删除异常')
      }
    }
  })
}

// 批量删除课程
function handleBatchDelete() {
  if (!selectedRowKeys.value.length) return
  modal.create({
    title: '批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 门课程吗？`,
    positiveText: '删除',
    negativeText: '取消',
    preset: 'dialog',
    onPositiveClick: async () => {
      try {
        const res = await batchDeleteCourses(selectedRowKeys.value as number[])
        if (res.code === 200) {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          fetchCourses()
        } else {
          message.error(res.message || '批量删除失败')
        }
      } catch (e: any) {
        message.error(e.message || '批量删除异常')
      }
    }
  })
}

// 状态标签点击筛选
function handleStatusTagClick(status: string) {
  // 如果当前筛选的状态和点击的状态相同，则清除筛选
  if (statusFilter.value === status) {
    statusFilter.value = null
    message.info('已清除状态筛选')
  } else {
    // 否则设置新的状态筛选
    statusFilter.value = status
    message.info(`已筛选状态：${getStatusText(status)}`)
  }
  handleSearch()
}
</script>
