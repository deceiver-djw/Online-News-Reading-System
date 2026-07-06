import { get, post, put, del } from './request'
import type {
  ApiResponse,
  NewsItem,
  Category,
  Tag,
} from './types'

// ===================== 新闻 =====================

/** 获取新闻列表（分页，支持分类筛选） */
export function getNewsList(categoryId?: number, pageNum = 1, pageSize = 10): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/news/list', { categoryId, pageNum, pageSize })
}

/** 获取新闻详情（同时会增加浏览量） */
export function getNewsDetail(id: number): Promise<ApiResponse<NewsItem>> {
  return get<NewsItem>(`/news/${id}`)
}

/** 获取头条新闻（首页轮播） */
export function getHeadlineNews(): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/news/headline')
}

/** 获取实时热点（按阅读量排序） */
export function getHotNews(limit = 10): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/news/hot', { limit })
}

/** 搜索新闻 */
export function searchNews(keyword: string, pageNum = 1, pageSize = 10): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/news/search', { keyword, pageNum, pageSize })
}

/** 高级筛选 */
export function filterNews(params: {
  categoryId?: number
  startTime?: string
  endTime?: string
  source?: string
  minViews?: number
  maxViews?: number
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/news/filter', params)
}

// ===================== 推荐 =====================

/** 猜你喜欢 */
export function guessYouLike(userId: number, limit = 10): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/recommend/guess-you-like', { userId, limit })
}

/** 相关推荐 */
export function getRelatedNews(newsId: number, limit = 5): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/recommend/related', { newsId, limit })
}

// ===================== 点赞 =====================

/** 点赞新闻 */
export function likeNews(userId: number, newsId: number): Promise<ApiResponse<null>> {
  return post<null>('/like/news', null, { params: { userId, newsId } })
}

/** 取消点赞新闻 */
export function unlikeNews(userId: number, newsId: number): Promise<ApiResponse<null>> {
  return del<null>('/like/news', { params: { userId, newsId } })
}

/** 检查点赞状态 */
export function checkLike(userId: number, targetType: string, targetId: number): Promise<ApiResponse<boolean>> {
  return get<boolean>('/like/check', { userId, targetType, targetId })
}

// ===================== 收藏 =====================

/** 收藏新闻 */
export function addCollection(userId: number, newsId: number): Promise<ApiResponse<null>> {
  return post<null>('/collection', null, { params: { userId, newsId } })
}

/** 取消收藏 */
export function removeCollection(userId: number, newsId: number): Promise<ApiResponse<null>> {
  return del<null>('/collection', { params: { userId, newsId } })
}

/** 我的收藏列表 */
export function getMyCollections(userId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/collection/my', { userId })
}

/** 检查收藏状态 */
export function checkCollection(userId: number, newsId: number): Promise<ApiResponse<boolean>> {
  return get<boolean>('/collection/check', { userId, newsId })
}

// ===================== 评论 =====================

/** 获取新闻评论列表 */
export function getCommentsByNewsId(newsId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>(`/comment/news/${newsId}`)
}

/** 发表评论 */
export function addComment(data: { content: string; newsId: number; userId: number }): Promise<ApiResponse<any>> {
  return post<any>('/comment', data)
}

/** 回复评论 */
export function replyComment(data: { content: string; newsId: number; userId: number; parentCommentId: number }): Promise<ApiResponse<any>> {
  return post<any>('/comment/reply', data)
}

/** 获取评论回复列表 */
export function getReplies(commentId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>(`/comment/replies/${commentId}`)
}

/** 我的评论列表 */
export function getMyComments(userId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/comment/my', { userId })
}

/** 删除评论 */
export function deleteComment(id: number): Promise<ApiResponse<null>> {
  return del<null>(`/comment/${id}`)
}

/** 评论点赞 */
export function likeComment(id: number): Promise<ApiResponse<any>> {
  return put<any>(`/comment/${id}/like`)
}

/** 置顶/取消置顶评论 */
export function pinComment(id: number, isPinned: boolean): Promise<ApiResponse<null>> {
  return put<null>(`/comment/${id}/pin`, null, { params: { isPinned } })
}

// ===================== 分类 =====================

/** 获取分类列表 */
export function getCategoryList(): Promise<ApiResponse<Category[]>> {
  return get<Category[]>('/category/list')
}

/** 获取分类详情 */
export function getCategoryById(id: number): Promise<ApiResponse<Category>> {
  return get<Category>(`/category/${id}`)
}

/** 新增分类 */
export function addCategory(data: { name: string; sortOrder?: number; icon?: string }): Promise<ApiResponse<Category>> {
  return post<Category>('/category', data)
}

/** 更新分类 */
export function updateCategory(id: number, data: Partial<Category>): Promise<ApiResponse<Category>> {
  return put<Category>(`/category/${id}`, data)
}

/** 删除分类 */
export function deleteCategory(id: number): Promise<ApiResponse<null>> {
  return del<null>(`/category/${id}`)
}

// ===================== 阅读历史 =====================

/** 记录阅读历史 */
export function addReadHistory(userId: number, newsId: number): Promise<ApiResponse<null>> {
  return post<null>('/read-history', null, { params: { userId, newsId } })
}

/** 我的阅读历史 */
export function getMyReadHistory(userId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/read-history/my', { userId })
}

/** 删除单条阅读记录 */
export function deleteReadHistory(id: number): Promise<ApiResponse<null>> {
  return del<null>(`/read-history/${id}`)
}

/** 清除全部阅读历史 */
export function clearMyReadHistory(userId: number): Promise<ApiResponse<null>> {
  return del<null>('/read-history/my', { params: { userId } })
}

// ===================== 订阅 =====================

/** 订阅分类 */
export function subscribe(userId: number, categoryId: number): Promise<ApiResponse<null>> {
  return post<null>('/subscription', null, { params: { userId, categoryId } })
}

/** 取消订阅 */
export function unsubscribe(userId: number, categoryId: number): Promise<ApiResponse<null>> {
  return del<null>('/subscription', { params: { userId, categoryId } })
}

/** 我的订阅列表 */
export function getMySubscriptions(userId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/subscription/my', { userId })
}

/** 检查订阅状态 */
export function checkSubscription(userId: number, categoryId: number): Promise<ApiResponse<boolean>> {
  return get<boolean>('/subscription/check', { userId, categoryId })
}

/** 更新推送设置 */
export function updatePushSetting(id: number, enabled: boolean): Promise<ApiResponse<null>> {
  return put<null>(`/subscription/${id}/push`, null, { params: { enabled } })
}

// ===================== 通知 =====================

/** 我的通知列表 */
export function getMyNotifications(userId: number): Promise<ApiResponse<any[]>> {
  return get<any[]>('/notification/my', { userId })
}

/** 未读通知数量 */
export function getUnreadCount(userId: number): Promise<ApiResponse<number>> {
  return get<number>('/notification/unread-count', { userId })
}

/** 标记已读 */
export function markAsRead(id: number): Promise<ApiResponse<null>> {
  return put<null>(`/notification/${id}/read`)
}

/** 全部标记已读 */
export function markAllAsRead(userId: number): Promise<ApiResponse<null>> {
  return put<null>('/notification/read-all', null, { params: { userId } })
}

/** 删除通知 */
export function deleteNotification(id: number): Promise<ApiResponse<null>> {
  return del<null>(`/notification/${id}`)
}


/** 获取统计快照 */
export function getSnapshots(days = 30): Promise<ApiResponse<any[]>> {
  return get<any[]>('/statistics/snapshot', { days })
}

// ===================== 编辑相关 =====================

/** 发布新闻 */
export function publishNews(data: any): Promise<ApiResponse<NewsItem>> {
  return post<NewsItem>('/editor/news', data)
}

/** 编辑新闻 */
export function updateNews(id: number, data: any): Promise<ApiResponse<NewsItem>> {
  return put<NewsItem>(`/editor/news/${id}`, data)
}

/** 删除新闻（编辑） */
export function deleteNews(id: number): Promise<ApiResponse<null>> {
  return del<null>(`/editor/news/${id}`)
}

/** 获取新闻详情（用于编辑） */
export function getEditorNewsDetail(id: number): Promise<ApiResponse<NewsItem>> {
  return get<NewsItem>(`/editor/news/${id}`)
}

/** 我的新闻列表 */
export function getMyNews(authorId: number): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/editor/news/my', { authorId })
}

/** 待审核新闻列表 */
export function getPendingNews(): Promise<ApiResponse<NewsItem[]>> {
  return get<NewsItem[]>('/editor/news/pending')
}

/** 审核新闻 */
export function auditNews(id: number, action: string): Promise<ApiResponse<null>> {
  return put<null>(`/editor/news/${id}/audit`, null, { params: { action } })
}

/** 上传新闻图片 */
export function uploadNewsImage(file: File): Promise<ApiResponse<string>> {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/editor/news/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 获取全部新闻（管理员） */
export function getAllNews(): Promise<ApiResponse<any[]>> {
  return get<any[]>('/editor/news/all')
}

// ===================== 点赞管理（独立） =====================

/** 点赞评论（独立接口） */
export function likeCommentV2(userId: number, commentId: number): Promise<ApiResponse<null>> {
  return post<null>('/like/comment', null, { params: { userId, commentId } })
}

/** 取消点赞评论 */
export function unlikeComment(userId: number, commentId: number): Promise<ApiResponse<null>> {
  return del<null>('/like/comment', { params: { userId, commentId } })
}