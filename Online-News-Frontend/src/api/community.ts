import { get, post } from './request'
import type { ApiResponse } from './types'

// 社区相关接口（后端暂未提供独立社区模块，作为备用）
export function getPosts(page?: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/community/posts', { page })
}

export function getHotPosts(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/community/posts/hot')
}

export function getPinnedPosts(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/community/posts/pinned')
}

export function createPost(data: { title: string; content: string; tags?: string[] }): Promise<ApiResponse<any>> {
  return post<any>('/community/posts', data)
}

export function getPostDetail(id: number): Promise<ApiResponse<any>> {
  return get<any>(`/community/posts/${id}`)
}

export function togglePostLike(id: number): Promise<ApiResponse<{ liked: boolean; likes: number }>> {
  return post<{ liked: boolean; likes: number }>(`/community/posts/${id}/like`)
}

export function getComments(postId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>(`/community/posts/${postId}/comments`)
}

export function createComment(postId: number, content: string): Promise<ApiResponse<any>> {
  return post<any>(`/community/posts/${postId}/comments`, { content })
}

export function getActiveUsers(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/community/users/active')
}

export function getHotTopics(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/community/topics/hot')
}