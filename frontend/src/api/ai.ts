import request from './request'

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
