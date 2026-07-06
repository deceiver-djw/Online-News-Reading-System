import request, { get, post, put } from './request'
import type { ApiResponse, User } from './types'

/** 用户登录（后端 data 直接返回 User 对象，无 token 包装） */
export function login(username: string, password: string): Promise<ApiResponse<User>> {
  return request({
    method: 'POST',
    url: '/auth/login',
    params: { username, password },
    headers: {},  // 覆盖默认 Content-Type
  }).then((res) => res.data as ApiResponse<User>)
}

/** 用户注册（后端使用 JSON body） */
export function register(data: { username: string; email: string; password: string }): Promise<ApiResponse<User>> {
  return post<User>('/auth/register', data)
}

/** 退出登录 */
export function logout(): Promise<ApiResponse<null>> {
  return post<null>('/auth/logout')
}

/** 获取用户信息 */
export function getUserInfo(userId: number): Promise<ApiResponse<User>> {
  return get<User>(`/auth/user/${userId}`)
}

/** 更新用户信息 */
export function updateUserInfo(id: number, data: Partial<User>): Promise<ApiResponse<User>> {
  return put<User>(`/auth/user/${id}`, data)
}