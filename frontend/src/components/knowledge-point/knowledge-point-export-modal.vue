<template>
  <!-- 导出知识点弹窗 -->
  <n-modal :show="show" title="导出知识点" preset="dialog" style="width: 500px" @update:show="emit('update:show', $event)">
    <n-form label-width="80px">
      <!-- 课程选择 -->
      <n-form-item label="选择课程">
        <n-select v-model:value="selectedCourseId" :options="[{ label: '全部课程', value: null }, ...courseOptions]" placeholder="请选择课程" show-search filterable />
      </n-form-item>
      <!-- 知识点状态选择 -->
      <n-form-item label="知识点状态">
        <n-select v-model:value="selectedEnabled" :options="enabledOptions" placeholder="请选择状态" />
      </n-form-item>
      <!-- 难度等级选择 -->
      <n-form-item label="难度等级">
        <n-select v-model:value="selectedDifficulty" :options="difficultyOptions" placeholder="请选择难度" />
      </n-form-item>
      <!-- 导出格式选择 -->
      <n-form-item label="导出格式">
        <n-select v-model:value="exportFormat" :options="exportFormatOptions" />
      </n-form-item>
      <n-space justify="end" style="width: 100%; margin-top: 16px">
        <n-button @click="emitClose">取消</n-button>
        <n-button type="primary" :loading="loading" @click="doExport">导出</n-button>
        <!-- 仅当选择思维导图格式时显示预览按钮 -->
        <n-button v-if="exportFormat === 'mm'" @click="showMindMap">预览思维导图</n-button>
      </n-space>
    </n-form>
    <!-- 思维导图预览弹窗 -->
    <n-modal v-model:show="mindMapVisible" title="思维导图预览" style="width: 900px">
      <template #default>
        <div style="display: flex; flex-direction: column; align-items: stretch">
          <div id="jsmind_container" style="width: 800px; height: 600px; border: 1px solid #ccc; background-color: #2c2c32"></div>
          <n-space justify="end" style="margin-top: 16px">
            <!-- 关闭按钮 -->
            <n-button @click="mindMapVisible = false">关闭</n-button>
          </n-space>
        </div>
      </template>
    </n-modal>
  </n-modal>
</template>

<script lang="ts" setup>
// 引入 jsMind 及其截图插件和样式
import jsMind from 'jsmind'

import 'jsmind/js/jsmind.screenshot.js'
import 'jsmind/style/jsmind.css'

import { useMessage } from 'naive-ui'
import { computed, nextTick, ref, watch } from 'vue'

import { getKnowledgePoints } from '@/api/knowledge-point'
import { DIFFICULTY_FILTER_OPTIONS as difficultyOptions, ENABLED_FILTER_OPTIONS as enabledOptions } from '@/constants/options'
import { todayKey } from '@/utils/datetime'

import type { CourseVO, KnowledgePointVO } from '@/api/types'

// 组件属性和事件定义
const props = defineProps<{ show: boolean; data: CourseVO[] }>()
const emit = defineEmits<{ (e: 'update:show', v: boolean): void; (e: 'success'): void }>()

// 消息提示实例
const message = useMessage()
// 导出按钮 loading 状态
const loading = ref(false)
// 选中的课程ID
const selectedCourseId = ref<number | null>(null)
// 选中的知识点启用状态
const selectedEnabled = ref<boolean | null>(null)
// 选中的难度等级
const selectedDifficulty = ref<string | null>(null)
// 导出格式（xlsx 或 mm）
const exportFormat = ref('xlsx')
// 导出格式选项
const exportFormatOptions = [
  { label: 'Excel (.xlsx)', value: 'xlsx' },
  { label: '思维导图 (.mm)', value: 'mm' }
]
// 思维导图弹窗显示状态
const mindMapVisible = ref(false)
// jsMind 实例
let jm: any = null

// 课程下拉选项（由 props.data 计算得出）
const courseOptions = computed(() => props.data.map(course => ({ label: course.name, value: course.id })))

// 监听弹窗显示，重置筛选条件
watch(
  () => props.show,
  v => {
    if (v) {
      selectedCourseId.value = null
      selectedEnabled.value = null
      selectedDifficulty.value = null
      exportFormat.value = 'xlsx'
    }
  }
)

// 关闭弹窗
function emitClose() {
  emit('update:show', false)
}

// 构建知识点树结构（按课程分组）
function buildTree(flatList: KnowledgePointVO[], courseMap: Record<number, string>) {
  // 按课程分组
  const courseGroups: Record<number, KnowledgePointVO[]> = {}
  flatList.forEach((kp: KnowledgePointVO) => {
    // 显式声明类型
    if (!courseGroups[kp.courseId]) courseGroups[kp.courseId] = []
    courseGroups[kp.courseId].push(kp)
  })
  // 构造树
  return Object.entries(courseGroups).map(([courseId, kps]) => ({
    name: courseMap[Number(courseId)] || `课程${courseId}`,
    children: kps.map((kp: KnowledgePointVO) => ({
      // 显式声明类型
      name: kp.name
    }))
  }))
}

// XML 字符串转义
function escapeXML(str: string) {
  return str.replace(
    /[<>&'"]/g,
    c =>
      ({
        '<': '&lt;',
        '>': '&gt;',
        '&': '&amp;',
        "'": '&apos;',
        '"': '&quot;'
      })[c] || c
  )
}

// 构建 jsMind 数据结构
function buildJsMindData(tree: any[], rootText: string) {
  // 转为 jsmind node_array 格式
  const data: any[] = [{ id: 'root', isroot: true, topic: rootText }]
  let id = 1
  tree.forEach(course => {
    const courseId = `c${id++}`
    data.push({ id: courseId, parentid: 'root', topic: course.name })
    course.children.forEach((kp: KnowledgePointVO) => {
      data.push({ id: `k${id++}`, parentid: courseId, topic: kp.name })
    })
  })
  return {
    meta: { name: rootText, author: 'XyeHyin', version: '1.0' },
    format: 'node_array',
    data
  }
}

// 预览思维导图
async function showMindMap() {
  // 获取知识点并筛选
  const res = await getKnowledgePoints()
  if (!res || res.code !== 200) {
    message.error(res?.message || '获取知识点失败')
    return
  }
  let list: KnowledgePointVO[] = res.data || []
  if (selectedCourseId.value) list = list.filter(kp => kp.courseId === selectedCourseId.value)
  if (selectedEnabled.value !== null) list = list.filter(kp => kp.enabled === selectedEnabled.value)
  if (selectedDifficulty.value) list = list.filter(kp => kp.difficulty === selectedDifficulty.value)
  const courseMap: Record<number, string> = {}
  props.data.forEach(c => {
    courseMap[c.id] = c.name
  })
  const tree = buildTree(list, courseMap)
  const rootName = selectedCourseId.value ? courseMap[selectedCourseId.value] || '知识点' : '全部课程知识点'
  // 渲染 jsmind
  mindMapVisible.value = true
  await nextTick()
  setTimeout(() => {
    const container = document.getElementById('jsmind_container')
    if (!container) {
      message.error('思维导图容器未挂载，请重试')
      return
    }
    // 如果旧实例有 destroy 方法则调用，否则直接清空容器
    if (jm && typeof jm.destroy === 'function') {
      jm.destroy()
    }
    container.innerHTML = '' // 清空上次渲染的画布
    jm = new jsMind({
      container: 'jsmind_container',
      editable: false,
      theme: 'primary'
    })
    jm.show(buildJsMindData(tree, rootName))
    // 兼容性处理：确保 get_snapshot 方法存在
    {
      // 先尝试取 import jsMind 的 prototype，再 fallback 到全局 window.jsMind
      const proto = (jsMind as any).prototype || (window as any).jsMind?.prototype
      if (typeof jm.get_snapshot !== 'function' && typeof proto?.get_snapshot === 'function') {
        jm.get_snapshot = proto.get_snapshot.bind(jm)
      }
    }
  }, 500) // 延长等待时间
}

// 生成 FreeMind 格式的 XML
function generateFreeMindXML(tree: any[], rootText: string) {
  function nodeToXML(node: any): string {
    if (!node.children || node.children.length === 0) {
      return `<node TEXT="${escapeXML(node.name)}"/>`
    }
    return `<node TEXT="${escapeXML(node.name)}">${node.children.map(nodeToXML).join('')}</node>`
  }
  return `<?xml version="1.0" encoding="UTF-8"?><map version="1.0.1"><node TEXT="${escapeXML(rootText)}">${tree.map(nodeToXML).join('')}</node></map>`
}

// 导出主逻辑（支持 Excel 和思维导图）
async function doExport() {
  loading.value = true
  try {
    // 如果选择思维导图图片，先预览
    if (exportFormat.value === 'jsmind-img') {
      await showMindMap()
      loading.value = false
      return
    }
    // 导出思维导图 .mm 文件
    if (exportFormat.value === 'mm') {
  
      const res = await getKnowledgePoints()
      if (!res || res.code !== 200) throw new Error(res?.message || '获取知识点失败')
      let list: KnowledgePointVO[] = res.data || []
    
      if (selectedCourseId.value) list = list.filter(kp => kp.courseId === selectedCourseId.value)
      if (selectedEnabled.value !== null) list = list.filter(kp => kp.enabled === selectedEnabled.value)
      if (selectedDifficulty.value) list = list.filter(kp => kp.difficulty === selectedDifficulty.value)
    
      const courseMap: Record<number, string> = {}
      props.data.forEach(c => {
        courseMap[c.id] = c.name
      })
     
      const tree = buildTree(list, courseMap)
   
      const rootName = selectedCourseId.value ? courseMap[selectedCourseId.value] || '知识点' : '全部课程知识点'
      const xml = generateFreeMindXML(tree, rootName)
    
      const blob = new Blob([xml], { type: 'text/xml' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      let fileName = '知识点导出'
      if (selectedCourseId.value) {
        const course = props.data.find(c => c.id === selectedCourseId.value)
        if (course) fileName += `_${course.name}`
      } else {
        fileName += '_全部课程'
      }
      fileName += `_${todayKey()}.mm`
      a.download = fileName
      a.click()
      URL.revokeObjectURL(url)
      message.success('思维导图导出成功')
      emit('success')
      emitClose()
      return
    }

    // Excel 导出
    const { exportKnowledgePoints } = await import('@/api/knowledge-point')
    const blob = await exportKnowledgePoints({
      courseId: selectedCourseId.value || undefined,
      enabled: selectedEnabled.value !== null ? selectedEnabled.value : undefined,
      difficulty: selectedDifficulty.value || undefined,
      format: 'xlsx'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    let fileName = '知识点导出'
    if (selectedCourseId.value) {
      const course = props.data.find(c => c.id === selectedCourseId.value)
      if (course) fileName += `_${course.name}`
    } else {
      fileName += '_全部课程'
    }
    fileName += `_${todayKey()}.xlsx`
    a.download = fileName
    a.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
    emit('success')
    emitClose()
  } catch (e: any) {
    message.error(e.message || '导出失败')
  } finally {
    loading.value = false
  }
}
</script>
