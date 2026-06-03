<template>
  <n-modal :show="show" preset="dialog" title="课程详情" @close="handleCancel" style="width: 800px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="封面图片" path="coverImage">
            <!-- 用同一个 upload 组件，editMode=false 时禁用并隐藏 trigger -->
            <n-upload
              v-model:file-list="coverImageList"
              :max="1"
              list-type="image-card"
              :custom-request="handleCustomRequest"
              :on-remove="handleRemove"
              accept="image/*"
              :show-file-list="true"
              :disabled="!editMode"
            >
              <!-- 只在编辑模式下显示上传按钮 -->
              <template #trigger>
                <n-button v-if="editMode" type="primary" size="small">点击上传封面</n-button>
              </template>
            </n-upload>
            <!-- 无文件时显示占位文字 -->
            <n-text v-if="!coverImageList.length" depth="3">暂无封面图片</n-text>
          </n-form-item>
          <n-form-item label="课程名称" path="name">
            <n-input v-model:value="form.name" :disabled="!editMode" placeholder="请输入课程名称" />
          </n-form-item>
          <n-form-item label="学科" path="subject">
            <n-select v-model:value="form.subject" :disabled="!editMode" :options="subjectOptions" placeholder="请选择学科" :loading="loadingCategories" />
          </n-form-item>
          <n-form-item label="年级" path="grade">
            <n-select v-model:value="form.grade" :disabled="!editMode" :options="gradeOptions" placeholder="请选择年级" :loading="loadingCategories" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="状态" path="status">
            <n-tag :type="getStatusType(form.status)">{{ getStatusText(form.status) }}</n-tag>
          </n-form-item>
          <n-form-item label="进度" path="progress">
            <div class="progress-container">
              <n-progress :percentage="Number(form.progress.toFixed(0))" />
            </div>
          </n-form-item>
          <n-form-item label="教师" path="teacherName">
            <n-input v-model:value="teacherNickname" disabled />
          </n-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="学期" path="semester">
            <n-select v-model:value="form.semester" :disabled="!editMode" :options="semesterOptions" placeholder="请选择学期" />
          </n-form-item>
          <n-form-item label="总学时" path="totalHours">
            <n-input-number v-model:value="form.totalHours" :disabled="!editMode" :min="0" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="已完成学时" path="completedHours">
            <n-input-number v-model:value="form.completedHours" :disabled="!editMode" :min="0" />
          </n-form-item>
          <n-form-item label="创建时间" path="createTime">
            <n-input :value="formattedCreateTime" disabled />
          </n-form-item>
        </div>
      </div>
      <n-form-item label="课程描述" path="description">
        <n-input v-model:value="form.description" :disabled="!editMode" type="textarea" :rows="3" placeholder="请输入课程描述" />
      </n-form-item>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button v-if="canEdit && !editMode" type="primary" @click="editMode = true">编辑</n-button>
          <template v-if="editMode">
            <n-button @click="editMode = false">取消编辑</n-button>
            <n-button type="primary" :loading="loading" @click="handleSubmit">保存</n-button>
          </template>
          <n-button v-if="canPublish && form.status === 'DRAFT'" type="success" :loading="publishing" @click="handlePublish">发布</n-button>
          <n-button v-if="canArchive && (form.status === 'PUBLISHED' || form.status === 'COMPLETED')" type="warning" :loading="archiving" @click="handleArchive">归档</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { getCourseStatusText as getStatusText, getCourseStatusType as getStatusType } from '@/utils/status'
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getCategories } from '@/api/categories'
import { uploadFile } from '@/api/file'

import type { CategoryFlatVO, CourseDetailVO } from '@/api/types'

// 定义文件列表类型
interface FileItem {
  id: string
  name: string
  status: 'pending' | 'uploading' | 'finished' | 'removed' | 'error'
  url?: string
}

const props = defineProps<{
  show: boolean
  loading?: boolean
  publishing?: boolean
  archiving?: boolean
  course: CourseDetailVO | null
  canEdit?: boolean
  canPublish?: boolean
  canArchive?: boolean
}>()

const emit = defineEmits(['update:show', 'submit', 'publish', 'archive'])

const formRef = ref()
const editMode = ref(false)

// 课程表单数据
const form = ref<CourseDetailVO>({
  id: 0,
  name: '',
  description: '',
  subject: '',
  grade: '',
  semester: '',
  enabled: true,
  coverImage: '',
  totalHours: 0,
  completedHours: 0,
  status: 'DRAFT',
  progress: 0,
  createTime: '',
  updateTime: ''
})

// 封面图片上传相关
const coverImageList = ref<FileItem[]>([])

// 分类数据
const categories = ref<CategoryFlatVO[]>([])
// 获取分类数据
const { loading: loadingCategories, run: fetchCategories } = useRequest(getCategories, {
  onSuccess: (res: any) => {
    if (res.code === 200) {
      categories.value = res.data || []
    }
  }
})

// 计算学科选项
const subjectOptions = computed(() => {
  // 从分类数据中筛选学科
  const subjects = categories.value.filter(cat => cat.parent && typeof cat.parent === 'object' && cat.parent.name === '学科').map(cat => ({ label: cat.name, value: cat.name }))
  return subjects
})

// 计算年级选项
const gradeOptions = computed(() => {
  // 从分类数据中筛选年级
  const grades = categories.value.filter(cat => cat.parent && typeof cat.parent === 'object' && cat.parent.name === '年级').map(cat => ({ label: cat.name, value: cat.name }))
  return grades
})

// 学期选项
const semesterOptions = [
  { label: '第一学期', value: '第一学期' },
  { label: '第二学期', value: '第二学期' },
  { label: '第三学期', value: '第三学期' }
]

// 教师昵称
const teacherNickname = computed(() => {
  // 获取教师昵称
  return form.value.teacher?.nickname || ''
})

// 格式化创建时间
const formattedCreateTime = computed(() => {
  // 格式化时间为“今天/昨天/日期”
  if (!form.value.createTime) return ''

  const date = new Date(form.value.createTime)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  // 如果是今天
  if (diffDays === 0) {
    return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  // 如果是昨天
  else if (diffDays === 1) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  // 如果是本年
  else if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  // 其他情况显示完整日期
  else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
})

// 表单校验规则
const rules = {
  name: { required: true, message: '请输入课程名称', trigger: 'blur' },
  description: { required: true, message: '请输入课程描述', trigger: 'blur' },
  subject: { required: true, message: '请选择学科', trigger: 'change' },
  grade: { required: true, message: '请选择年级', trigger: 'change' }
}

const message = useMessage()

// 自定义上传处理
const handleCustomRequest = async ({ file, onFinish, onError, onProgress }: any) => {
  // 处理图片上传，校验类型和大小
  try {
    // 获取实际的 File 对象
    const actualFile = file.file || file

    // 验证文件类型和大小
    if (!actualFile.type.startsWith('image/')) {
      message.error('只能上传图片文件')
      onError()
      return
    }
    if (actualFile.size > 5 * 1024 * 1024) {
      message.error('图片大小不能超过5MB')
      onError()
      return
    }

    // 模拟进度
    onProgress({ percent: 20 })
    const res = await uploadFile(actualFile)

    if (res.code === 200 && res.data) {
      form.value.coverImage = res.data

      // 更新文件列表显示
      coverImageList.value = [
        {
          id: 'cover',
          name: actualFile.name,
          status: 'finished',
          url: res.data
        }
      ]

      message.success('上传成功')
      onProgress({ percent: 100 })
      onFinish()
    } else {
      message.error(res.message || '上传失败')
      onError()
    }
  } catch (error) {
    message.error('上传失败')
    onError()
  }
}

// 处理移除图片
const handleRemove = () => {
  // 移除图片时清空表单字段
  form.value.coverImage = ''
  coverImageList.value = []
  return true
}

// 关闭弹窗
function handleCancel() {
  emit('update:show', false)
  editMode.value = false
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      // 进度 100% 时自动标记已完成
      if (form.value.progress >= 100) {
        form.value.status = 'COMPLETED'
      }
      emit('submit', { id: form.value.id, data: form.value })
      editMode.value = false
    }
  })
}

// 发布课程
function handlePublish() {
  emit('publish', form.value.id)
}

// 归档课程
function handleArchive() {
  emit('archive', form.value.id)
}

// 监听课程数据变化，更新表单和图片
watch(
  () => props.course,
  course => {
    if (course) {
      form.value = { ...course }
      // 设置封面图片预览
      if (course.coverImage) {
        coverImageList.value = [
          {
            id: 'cover',
            name: '封面图片',
            status: 'finished',
            url: course.coverImage
          }
        ]
      } else {
        coverImageList.value = []
      }
    }
  },
  { immediate: true }
)

// 监听弹窗显示状态
watch(
  () => props.show,
  show => {
    if (show) {
      fetchCategories()
      editMode.value = false
    }
  }
)

// 监听学时变化，自动计算进度和状态
watch(
  () => [form.value.completedHours, form.value.totalHours],
  ([completed, total]) => {
    // 自动计算进度百分比和状态
    if (typeof completed === 'number' && typeof total === 'number') {
      if (completed < 0) {
        form.value.completedHours = 0
      } else if (total > 0 && completed > total) {
        form.value.completedHours = total
      }
      const percent = total > 0 ? Math.min(100, Math.round(((form.value.completedHours ?? 0) / total) * 100)) : 0
      form.value.progress = percent
      // 进度达到100%，且当前为已发布，自动变为已完成
      if (percent >= 100 && form.value.status === 'PUBLISHED') {
        form.value.status = 'COMPLETED'
      }
      // 进度回退到小于100%，且当前为已完成，自动回退为已发布
      if (percent < 100 && form.value.status === 'COMPLETED') {
        form.value.status = 'PUBLISHED'
      }
    }
  }
)

onMounted(() => {
  if (props.show) {
    fetchCategories()
  }
})
</script>

<style scoped>
.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
}

.cover-preview {
  display: flex;
  align-items: center;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
</style>
