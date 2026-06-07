import request from './request'
import { isJwtToken, useAuthStore } from '@/store/auth'

import type { AIGeneratedQuestionVO, AIGenerateQuestionDTO, ApiResponse } from './types'

// AI生成题目
export function generateQuestions(data: AIGenerateQuestionDTO) {
  return request<ApiResponse<AIGeneratedQuestionVO[]>>({
    url: '/questions/generate/ai',
    method: 'POST',
    data,
    timeout: 120000
  })
}

type SseMessage = {
  event: string
  data: string
}

export type GenerateQuestionsStreamQuestionEvent = {
  index: number
  total: number
  question: AIGeneratedQuestionVO
}

export type GenerateQuestionsStreamProgressEvent = {
  generated: number
  total: number
}

export type GenerateQuestionsStreamHandlers = {
  onStart?: (event: { total: number }) => void
  onQuestion?: (event: GenerateQuestionsStreamQuestionEvent) => void
  onProgress?: (event: GenerateQuestionsStreamProgressEvent) => void
  onDone?: (event: { total: number }) => void
  onError?: (event: { message: string }) => void
}

function getApiUrl(path: string) {
  const baseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  return `${baseUrl}${path}`
}

function parseSseBlock(block: string): SseMessage | null {
  const lines = block.split(/\r?\n/)
  let event = 'message'
  const data: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      data.push(line.slice(5).trimStart())
    }
  }

  if (!data.length) return null
  return { event, data: data.join('\n') }
}

export async function streamGenerateQuestions(
  data: AIGenerateQuestionDTO,
  handlers: GenerateQuestionsStreamHandlers,
  signal?: AbortSignal
) {
  const auth = useAuthStore()
  if (!isJwtToken(auth.token)) {
    throw new Error('登录已失效，请重新登录')
  }

  const response = await fetch(getApiUrl('/questions/generate/ai/stream'), {
    method: 'POST',
    headers: {
      Accept: 'text/event-stream',
      Authorization: `Bearer ${auth.token.trim()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data),
    signal
  })

  if (!response.ok || !response.body) {
    throw new Error(`AI生成失败：${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let remoteError: Error | null = null

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split(/\r?\n\r?\n/)
    buffer = parts.pop() ?? ''

    for (const part of parts) {
      const message = parseSseBlock(part)
      if (!message) continue

      const payload = JSON.parse(message.data)
      switch (message.event) {
        case 'start':
          handlers.onStart?.(payload)
          break
        case 'question':
          handlers.onQuestion?.(payload)
          break
        case 'progress':
          handlers.onProgress?.(payload)
          break
        case 'done':
          handlers.onDone?.(payload)
          await reader.cancel().catch(() => undefined)
          return
        case 'error':
          handlers.onError?.(payload)
          remoteError = new Error(payload.message || 'AI生成失败')
          await reader.cancel().catch(() => undefined)
          throw remoteError
      }
    }
  }

  if (remoteError) {
    throw remoteError
  }
}
