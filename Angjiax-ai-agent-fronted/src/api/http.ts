import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_BASE || '/api'

const raw: AxiosInstance = axios.create({
  baseURL,
  timeout: 60000,
  withCredentials: true,
})

function unwrap<T>(response: AxiosResponse<ApiResponse<T>>): T {
  const body = response.data
  if (body.code !== 0) {
    const msg = body.message || '请求失败'
    ElMessage.error(msg)
    throw new Error(msg)
  }
  return body.data as T
}

raw.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export interface HttpInstance {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
}

export const http: HttpInstance = {
  async get<T>(url: string, config?: AxiosRequestConfig) {
    const res = await raw.get<ApiResponse<T>>(url, config)
    return unwrap<T>(res)
  },
  async delete<T>(url: string, config?: AxiosRequestConfig) {
    const res = await raw.delete<ApiResponse<T>>(url, config)
    return unwrap<T>(res)
  },
  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const res = await raw.post<ApiResponse<T>>(url, data, config)
    return unwrap<T>(res)
  },
  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    const res = await raw.put<ApiResponse<T>>(url, data, config)
    return unwrap<T>(res)
  },
}
