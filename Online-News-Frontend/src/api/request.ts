import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from './types'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 自动添加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    // 后端返回的 code 不为 200 时，视为业务错误
    if (data.code !== 200 && data.code !== 0) {
      // Token 过期，清除并跳转登录
      if (data.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.dispatchEvent(new CustomEvent('auth:logout'))
      }
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          break
        case 403:
          console.error('没有权限访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器错误')
          break
      }
    } else if (error.code === 'ECONNABORTED') {
      console.error('请求超时')
    } else {
      console.error('网络错误')
    }
    return Promise.reject(error)
  },
)

// 封装 GET 请求
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.get(url, { params, ...config }).then((res) => res.data)
}

// 封装 POST 请求
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.post(url, data, config).then((res) => res.data)
}

// 封装 PUT 请求
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.put(url, data, config).then((res) => res.data)
}

// 封装 DELETE 请求
export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.delete(url, config).then((res) => res.data)
}

// 封装上传文件
export function upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    ...config,
  }).then((res) => res.data)
}

export default request