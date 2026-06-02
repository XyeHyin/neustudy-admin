import request from './request';
import type { ApiResponse } from './types';

// 文件上传
export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request<ApiResponse<string>>({
    url: '/files/upload',
    method: 'POST',
    data: formData
  })
}