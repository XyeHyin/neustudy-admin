<template>
  <n-modal :show="show" title="导出知识点" preset="dialog" style="width: 560px" @update:show="emit('update:show', $event)">
    <n-form label-width="88px">
      <n-form-item label="选择课程">
        <n-select v-model:value="selectedCourseId" :options="[{ label: '全部课程', value: null }, ...courseOptions]" placeholder="请选择课程" show-search filterable />
      </n-form-item>
      <n-form-item label="知识点状态">
        <n-select v-model:value="selectedEnabled" :options="enabledOptions" placeholder="请选择状态" />
      </n-form-item>
      <n-form-item label="难度等级">
        <n-select v-model:value="selectedDifficulty" :options="difficultyOptions" placeholder="请选择难度" />
      </n-form-item>
      <n-form-item label="导出格式">
        <n-select v-model:value="exportFormat" :options="exportFormatOptions" />
      </n-form-item>
      <n-space justify="end" style="width: 100%; margin-top: 16px">
        <n-button @click="emitClose">取消</n-button>
        <n-button v-if="isMindMapFormat" secondary @click="showMindMap">预览思维导图</n-button>
        <n-button type="primary" :loading="loading" @click="doExport">导出</n-button>
      </n-space>
    </n-form>

    <n-modal v-model:show="mindMapVisible" title="思维导图预览" preset="card" style="width: min(1080px, calc(100vw - 32px))">
      <div class="mindmap-preview">
        <svg ref="mindMapSvgRef" class="mindmap-preview__svg" />
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="mindMapVisible = false">关闭</n-button>
          <n-button secondary @click="downloadMindMap('svg')">导出 SVG</n-button>
          <n-button type="primary" @click="downloadMindMap('png')">导出 PNG</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-modal>
</template>

<script lang="ts" setup>
import { Transformer } from 'markmap-lib'
import { Markmap } from 'markmap-view'
import { useMessage } from 'naive-ui'
import { computed, nextTick, ref, watch } from 'vue'

import { exportKnowledgePoints, exportMyKnowledgePoints, getKnowledgePoints, getMyKnowledgePoints } from '@/api/knowledge-point'
import { DIFFICULTY_FILTER_OPTIONS as difficultyOptions, ENABLED_FILTER_OPTIONS as enabledOptions } from '@/constants/options'
import { useAuthStore } from '@/store/auth'
import { todayKey } from '@/utils/datetime'

import type { CourseVO, KnowledgePointVO } from '@/api/types'

type MindMapExportFormat = 'markdown' | 'svg' | 'png'

const props = defineProps<{ show: boolean; data: CourseVO[] }>()
const emit = defineEmits<{ (e: 'update:show', v: boolean): void; (e: 'success'): void }>()

const message = useMessage()
const auth = useAuthStore()
const transformer = new Transformer()

const loading = ref(false)
const selectedCourseId = ref<number | null>(null)
const selectedEnabled = ref<boolean | null>(null)
const selectedDifficulty = ref<string | null>(null)
const exportFormat = ref<'xlsx' | MindMapExportFormat>('xlsx')
const mindMapVisible = ref(false)
const mindMapSvgRef = ref<SVGSVGElement | null>(null)
const latestMindMapMarkdown = ref('')
let markmap: Markmap | null = null

const exportFormatOptions = [
  { label: 'Excel (.xlsx)', value: 'xlsx' },
  { label: '思维导图 Markdown (.md)', value: 'markdown' },
  { label: '思维导图 SVG 图片 (.svg)', value: 'svg' },
  { label: '思维导图 PNG 图片 (.png)', value: 'png' }
]

const courseOptions = computed(() => props.data.map(course => ({ label: course.name, value: course.id })))
const isMindMapFormat = computed(() => ['markdown', 'svg', 'png'].includes(exportFormat.value))
const canListAllKnowledgePoints = computed(() => auth.hasPermission('knowledge_point:list:all'))

watch(
  () => props.show,
  v => {
    if (v) {
      selectedCourseId.value = null
      selectedEnabled.value = null
      selectedDifficulty.value = null
      exportFormat.value = 'xlsx'
      latestMindMapMarkdown.value = ''
    }
  }
)

function emitClose() {
  emit('update:show', false)
}

function sanitizeFileName(value: string) {
  return value.replace(/[\\/:*?"<>|]/g, '_')
}

function buildFileName(extension: string) {
  let fileName = '知识点导出'
  if (selectedCourseId.value) {
    const course = props.data.find(c => c.id === selectedCourseId.value)
    if (course) fileName += `_${course.name}`
  } else {
    fileName += '_全部课程'
  }
  return `${sanitizeFileName(fileName)}_${todayKey()}.${extension}`
}

async function fetchFilteredKnowledgePoints() {
  const res = canListAllKnowledgePoints.value ? await getKnowledgePoints() : await getMyKnowledgePoints()
  if (!res || res.code !== 200) {
    throw new Error(res?.message || '获取知识点失败')
  }

  let list: KnowledgePointVO[] = res.data || []
  if (selectedCourseId.value) list = list.filter(kp => kp.courseId === selectedCourseId.value)
  if (selectedEnabled.value !== null) list = list.filter(kp => kp.enabled === selectedEnabled.value)
  if (selectedDifficulty.value) list = list.filter(kp => kp.difficulty === selectedDifficulty.value)

  return list.sort((a, b) => {
    if (a.courseId !== b.courseId) return a.courseId - b.courseId
    return (a.orderNum || 0) - (b.orderNum || 0)
  })
}

function buildCourseMap() {
  const courseMap: Record<number, string> = {}
  props.data.forEach(course => {
    courseMap[course.id] = course.name
  })
  return courseMap
}

function buildMindMapMarkdown(list: KnowledgePointVO[]) {
  const courseMap = buildCourseMap()
  const rootName = selectedCourseId.value ? courseMap[selectedCourseId.value] || '知识点' : '全部课程知识点'
  const grouped = new Map<number, KnowledgePointVO[]>()

  list.forEach(item => {
    if (!grouped.has(item.courseId)) grouped.set(item.courseId, [])
    grouped.get(item.courseId)?.push(item)
  })

  const lines = [`# ${rootName}`]
  grouped.forEach((items, courseId) => {
    lines.push(`## ${courseMap[courseId] || `课程${courseId}`}`)
    items.forEach(item => {
      lines.push(`### ${item.name}`)
      lines.push(`- 难度：${item.difficulty || '-'}`)
      lines.push(`- 状态：${item.enabled ? '启用' : '禁用'}`)
      if (item.description) lines.push(`- 描述：${item.description}`)
      if (item.keywords) lines.push(`- 关键词：${item.keywords}`)
    })
  })

  return lines.join('\n')
}

async function renderMindMap(markdown: string) {
  mindMapVisible.value = true
  await nextTick()

  const svg = mindMapSvgRef.value
  if (!svg) {
    throw new Error('思维导图容器未挂载，请重试')
  }

  svg.innerHTML = ''
  svg.setAttribute('viewBox', '0 0 1200 760')
  svg.setAttribute('width', '1200')
  svg.setAttribute('height', '760')

  const { root } = transformer.transform(markdown)
  markmap = Markmap.create(svg, {
    autoFit: true,
    duration: 220,
    paddingX: 18
  }, root)
  await nextTick()
  markmap.fit()
}

async function prepareMindMap() {
  const list = await fetchFilteredKnowledgePoints()
  if (!list.length) {
    throw new Error('没有符合条件的知识点可导出')
  }
  latestMindMapMarkdown.value = buildMindMapMarkdown(list)
  return latestMindMapMarkdown.value
}

async function showMindMap() {
  loading.value = true
  try {
    const markdown = await prepareMindMap()
    await renderMindMap(markdown)
  } catch (error: any) {
    message.error(error.message || '预览思维导图失败')
  } finally {
    loading.value = false
  }
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  URL.revokeObjectURL(url)
}

function serializeSvg() {
  const svg = mindMapSvgRef.value
  if (!svg) {
    throw new Error('请先预览思维导图')
  }
  const cloned = svg.cloneNode(true) as SVGSVGElement
  cloned.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
  cloned.setAttribute('width', '1600')
  cloned.setAttribute('height', '1000')
  return new XMLSerializer().serializeToString(cloned)
}

async function downloadSvg() {
  const svgText = serializeSvg()
  downloadBlob(new Blob([svgText], { type: 'image/svg+xml;charset=utf-8' }), buildFileName('svg'))
}

async function downloadPng() {
  const svgText = serializeSvg()
  const image = new Image()
  const svgUrl = URL.createObjectURL(new Blob([svgText], { type: 'image/svg+xml;charset=utf-8' }))
  image.src = svgUrl
  await new Promise<void>((resolve, reject) => {
    image.onload = () => resolve()
    image.onerror = () => reject(new Error('思维导图图片生成失败'))
  })

  const canvas = document.createElement('canvas')
  canvas.width = 1600
  canvas.height = 1000
  const context = canvas.getContext('2d')
  if (!context) throw new Error('浏览器不支持图片导出')
  context.fillStyle = '#ffffff'
  context.fillRect(0, 0, canvas.width, canvas.height)
  context.drawImage(image, 0, 0, canvas.width, canvas.height)
  URL.revokeObjectURL(svgUrl)

  const blob = await new Promise<Blob>((resolve, reject) => {
    canvas.toBlob(result => (result ? resolve(result) : reject(new Error('思维导图图片生成失败'))), 'image/png')
  })
  downloadBlob(blob, buildFileName('png'))
}

async function downloadMindMap(format: MindMapExportFormat) {
  try {
    if (format === 'markdown') {
      const markdown = latestMindMapMarkdown.value || (await prepareMindMap())
      downloadBlob(new Blob([markdown], { type: 'text/markdown;charset=utf-8' }), buildFileName('md'))
    } else if (format === 'svg') {
      await downloadSvg()
    } else {
      await downloadPng()
    }
    message.success('思维导图导出成功')
  } catch (error: any) {
    message.error(error.message || '思维导图导出失败')
  }
}

async function doExport() {
  loading.value = true
  try {
    if (exportFormat.value === 'xlsx') {
      const exportApi = canListAllKnowledgePoints.value ? exportKnowledgePoints : exportMyKnowledgePoints
      const blob = await exportApi({
        courseId: selectedCourseId.value || undefined,
        enabled: selectedEnabled.value !== null ? selectedEnabled.value : undefined,
        difficulty: selectedDifficulty.value || undefined
      })
      downloadBlob(blob, buildFileName('xlsx'))
      message.success('导出成功')
      emit('success')
      emitClose()
      return
    }

    const markdown = await prepareMindMap()
    if (exportFormat.value === 'markdown') {
      downloadBlob(new Blob([markdown], { type: 'text/markdown;charset=utf-8' }), buildFileName('md'))
    } else {
      await renderMindMap(markdown)
      await downloadMindMap(exportFormat.value)
    }
    emit('success')
    if (exportFormat.value === 'markdown') emitClose()
  } catch (error: any) {
    message.error(error.message || '导出失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.mindmap-preview {
  width: 100%;
  overflow: auto;
  border: 1px solid var(--border-color);
  border-radius: var(--surface-radius-sm);
  background: #fff;
}

.mindmap-preview__svg {
  display: block;
  width: 100%;
  min-width: 900px;
  height: 620px;
}
</style>
