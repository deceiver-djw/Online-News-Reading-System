/**
 * API 接口统一导出
 *
 * 用法示例：
 * import { login, getNewsList, getHeadlineNews } from '@/api'
 *
 * 或者按模块导入：
 * import { login } from '@/api/auth'
 * import { getNewsList } from '@/api/news'
 */

// 请求工具
export { get, post, put, del, upload } from './request'
export { default as request } from './request'

// 类型定义
export type * from './types'

// 业务模块
export * from './auth'
export * from './news'
export * from './media'
export * from './community'