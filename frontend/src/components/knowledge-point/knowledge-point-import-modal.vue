<template>
  <n-modal :show="show" title="导入知识点" preset="dialog" style="width: 500px" @update:show="emit('update:show', $event)">
    <n-form label-width="80px">
      <n-form-item label="选择课程">
        <n-select v-model:value="selectedCourseId" :options="courseOptions" placeholder="请选择课程" show-search filterable />
      </n-form-item>
      <n-form-item label="导入模板">
        <n-space>
          <n-button type="info" :loading="downloadingTemplate" @click="downloadTemplate">下载导入模板</n-button>
          <n-text depth="3" style="font-size: 12px">请先下载模板，按格式填写后上传</n-text>
        </n-space>
      </n-form-item>
      <n-form-item label="选择文件">
        <n-upload
          v-model:file-list="fileList"
          :max="1"
          accept=".xlsx,.xls"
          :custom-request="handleFileUpload"
          :on-remove="handleFileRemove"
          :show-file-list="true"
          :default-upload="false"
        >
          <n-button>选择Excel文件</n-button>
        </n-upload>
      </n-form-item>
      <n-space justify="end" style="width: 100%; margin-top: 16px">
        <n-button @click="emitClose">取消</n-button>
        <n-button type="primary" :loading="loading" :disabled="!selectedCourseId || !selectedFile" @click="doImport">导入</n-button>
      </n-space>
    </n-form>
  </n-modal>
</template>

<script lang="ts" setup>
import { useMessage } from 'naive-ui'
import { computed, ref, watch } from 'vue'
import { useRequest } from 'vue-hooks-plus'

import { downloadImportTemplate, importKnowledgePoints } from '@/api/knowledge-point'

import type { UploadFileInfo } from 'naive-ui'
import type { CourseVO } from '@/api/types'

const props = defineProps<{
  show: boolean
  data: CourseVO[]
}>()
const emit = defineEmits<{
  (e: 'update:show', v: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()
const loading = ref(false)
const selectedCourseId = ref<number | null>(null)
const fileList = ref<UploadFileInfo[]>([])
const selectedFile = ref<File | null>(null)

// 课程下拉选项
const courseOptions = computed(() => props.data.map(course => ({ label: course.name, value: course.id })))

// 监听文件列表变化，校验文件类型和大小
watch(
  fileList,
  newFileList => {
    if (newFileList.length > 0 && newFileList[0].file) {
      const file = newFileList[0].file as File
      // 验证文件类型
      if (!file.name.match(/\.(xlsx|xls)$/i)) {
        message.error('请选择Excel文件（.xlsx或.xls格式）')
        fileList.value = []
        return
      }
      // 验证文件大小（10MB限制）
      if (file.size > 10 * 1024 * 1024) {
        message.error('文件大小不能超过10MB')
        fileList.value = []
        return
      }
      selectedFile.value = file
      message.success('文件选择成功')
    } else {
      selectedFile.value = null
    }
  },
  { deep: true }
)

// 下载导入模板
const { loading: downloadingTemplate, run: downloadTemplate } = useRequest(
  async () => {
    try {
      const blob = await downloadImportTemplate()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = '知识点导入模板.xlsx'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      message.success('模板下载成功')
    } catch (error: any) {
      message.error(error.message || '模板下载失败')
    }
  },
  { manual: true }
)

// 文件上传钩子（不自动上传）
const handleFileUpload = ({ file, onFinish }: any) => {
  onFinish()
}

// 处理文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  fileList.value = []
  return true
}

// 监听弹窗显示，重置状态
watch(
  () => props.show,
  v => {
    if (v) {
      selectedCourseId.value = null
      selectedFile.value = null
      fileList.value = []
    }
  }
)

// 关闭弹窗
function emitClose() {
  emit('update:show', false)
}

// 执行导入
async function doImport() {
  if (!selectedCourseId.value) {
    message.error('请选择课程')
    return
  }
  if (!selectedFile.value) {
    message.error('请选择文件')
    return
  }
  loading.value = true
  try {
    const res = await importKnowledgePoints(selectedFile.value, selectedCourseId.value)
    if (res.code === 200) {
      message.success(`导入成功：${res.data.successCount} 条，失败 ${res.data.failCount} 条`)
      if (res.data.errorMessages && res.data.errorMessages.length > 0) {
        console.warn('导入错误详情：', res.data.errorMessages)
      }
      emit('success')
      emitClose()
    } else {
      message.error(res.message || '导入失败')
    }
  } catch (e: any) {
    message.error(e.message || '导入异常')
  } finally {
    loading.value = false
  }
}
</script>
