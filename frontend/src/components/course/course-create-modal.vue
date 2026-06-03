<template>
  <n-modal :show="show" preset="dialog" title="创建课程" @close="handleCancel" style="width: 800px">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80" style="padding: 16px 24px">
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="课程名称" path="name">
            <n-input v-model:value="form.name" placeholder="请输入课程名称" />
          </n-form-item>
          <n-form-item label="学科" path="subject">
            <n-select v-model:value="form.subject" :options="subjectOptions" placeholder="请选择学科" :loading="loadingCategories" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="年级" path="grade">
            <n-select v-model:value="form.grade" :options="gradeOptions" placeholder="请选择年级" :loading="loadingCategories" />
          </n-form-item>
          <n-form-item label="学期" path="semester">
            <n-select v-model:value="form.semester" :options="semesterOptions" placeholder="请选择学期" />
          </n-form-item>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <n-form-item label="总学时" path="totalHours">
            <n-input-number v-model:value="form.totalHours" :min="0" placeholder="请输入总学时" />
          </n-form-item>
          <n-form-item v-if="isAdmin" label="教师" path="teacherId">
            <n-select v-model:value="form.teacherId" :options="userOptions" placeholder="请选择教师" />
          </n-form-item>
        </div>
        <div class="form-col">
          <n-form-item label="封面图片" path="coverImage">
            <n-upload
              v-model:file-list="coverImageList"
              :max="1"
              list-type="image-card"
              :custom-request="handleCustomRequest"
              :on-remove="handleRemove"
              accept="image/*"
              :show-file-list="true"
            >
              点击上传封面
            </n-upload>
          </n-form-item>
        </div>
      </div>
      <n-form-item label="课程描述" path="description">
        <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
      </n-form-item>
      <n-form-item>
        <n-space justify="end" style="width: 100%">
          <n-button @click="handleCancel">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleSubmit">创建</n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { getCategories } from '@/api/categories'
import { uploadFile } from '@/api/file'
import { SEMESTER_OPTIONS as semesterOptions } from '@/constants/options'

import type { CategoryFlatVO, CreateCourseDTO, UserVO } from '@/api/types'

// 文件上传项类型定义
interface FileItem {
  id: string
  name: string
  status: 'pending' | 'uploading' | 'finished' | 'removed' | 'error'
  url?: string
}

const props = defineProps<{
  show: boolean
  loading?: boolean
  users?: UserVO[]
  isAdmin?: boolean
}>()

const emit = defineEmits(['update:show', 'submit'])

const formRef = ref()
// 课程表单数据
const form = ref<CreateCourseDTO & { teacherId?: number }>({
  name: '',
  description: '',
  subject: '',
  grade: '',
  semester: '',
  enabled: true,
  coverImage: '',
  totalHours: 0,
  status: 'DRAFT',
  teacherId: undefined
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

// 教师选项
const userOptions = computed(() => props.users?.map(u => ({ label: u.nickname, value: u.id })) || [])

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
    const actualFile = file.file || file
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
    onProgress({ percent: 20 })
    const res = await uploadFile(actualFile)
    if (res.code === 200 && res.data) {
      form.value.coverImage = res.data
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

// 处理文件移除
const handleRemove = () => {
  // 移除图片时清空表单字段
  form.value.coverImage = ''
  coverImageList.value = []
  return true
}

// 重置表单
function resetForm() {
  // 重置所有表单字段和图片
  form.value = {
    name: '',
    description: '',
    subject: '',
    grade: '',
    semester: '',
    enabled: true,
    coverImage: '',
    totalHours: 0,
    status: 'DRAFT',
    teacherId: undefined
  }
  coverImageList.value = []
}

// 关闭弹窗并重置表单
function handleCancel() {
  emit('update:show', false)
  resetForm()
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      emit('submit', form.value)
    }
  })
}

// 监听显示状态，弹窗打开时重置表单并加载分类
watch(
  () => props.show,
  show => {
    if (show) {
      resetForm()
      fetchCategories()
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
</style>
