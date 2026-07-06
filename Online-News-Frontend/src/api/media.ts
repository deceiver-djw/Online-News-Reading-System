import { get } from './request'
import type { ApiResponse } from './types'

// 媒体相关接口（后端暂未提供独立媒体模块，作为备用）
export function getVideos(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/media/videos')
}

export function getVideoDetail(id: number): Promise<ApiResponse<any>> {
  return get<any>(`/media/videos/${id}`)
}

export function getGalleries(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/media/galleries')
}

export function getGalleryDetail(id: number): Promise<ApiResponse<any>> {
  return get<any>(`/media/galleries/${id}`)
}

export function getPodcasts(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/media/podcasts')
}

export function getPodcastDetail(id: number): Promise<ApiResponse<any>> {
  return get<any>(`/media/podcasts/${id}`)
}