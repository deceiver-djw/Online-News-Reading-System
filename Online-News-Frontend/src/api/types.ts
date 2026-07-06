// ===================== 基础类型 =====================

export interface User {
  id: number
  username: string
  email: string
  avatar: string
  role: 'ADMIN' | 'READER'
  password?: string
  registerTime?: string
  lastLoginTime?: string
  createdAt?: string
  updatedAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

// ===================== 新闻类型 =====================

export interface NewsItem {
  id: number
  title: string
  summary: string
  content: string
  categoryId: number
  category?: Category
  authorId: number
  author?: User
  coverImage: string | null
  videoUrl: string | null
  source: string
  tags: Tag[] | null
  status: 'DRAFT' | 'PENDING' | 'PUBLISHED' | 'ARCHIVED' | 'REJECTED'
  viewCount: number
  likeCount: number
  collectCount: number
  commentCount: number
  publishTime: string
  createdAt: string | null
  updatedAt: string | null
  commentList?: NewsComment[] | null
  collections?: any[] | null
}

export interface NewsListParams {
  categoryId?: number
  pageNum?: number
  pageSize?: number
}

export interface NewsSearchParams {
  keyword: string
  pageNum?: number
  pageSize?: number
}

export interface PaginatedResponse<T> {
  items?: T[]
  total?: number
  page?: number
  pageSize?: number
  totalPages?: number
}

// ===================== 分类 =====================

export interface Category {
  id: number
  name: string
  sortOrder: number
  icon: string
  createdAt: string | null
  updatedAt: string | null
  newsList?: NewsItem[] | null
}

// ===================== 标签 =====================

export interface Tag {
  id: number
  tagName: string
  createdAt: string | null
  newsList?: NewsItem[] | null
}

// ===================== 评论 =====================

export interface NewsComment {
  id: number
  newsId: number
  userId: number
  content: string
  likeCount: number
  isPinned: boolean
  status: 'NORMAL' | 'DELETED'
  createTime: string
  parentCommentId: number | null
  user?: User
  news?: NewsItem
  parentComment?: NewsComment
  replyList?: NewsComment[]
}

// ===================== 收藏 =====================

export interface Favorite {
  id: number
  userId: number
  newsId: number
  createTime: string
  user?: User
  news?: NewsItem
}

// ===================== 阅读历史 =====================

export interface ReadingHistory {
  id: number
  userId: number
  newsId: number
  news?: NewsItem
  readAt?: string
}

// ===================== 订阅 =====================

export interface CategorySubscription {
  id: number
  userId: number
  category: string
  createdAt: string
}

// ===================== 通知 =====================

export interface Notification {
  id: number
  userId: number
  type: 'news' | 'comment' | 'system'
  title: string
  content: string
  isRead: boolean
  link: string
  createdAt: string
}

// ===================== 统计 =====================

export interface Statistics {
  totalNews: number
  totalUsers: number
  todayViews: number
  totalComments: number
}

export interface CategoryStats {
  category: string
  count: number
}

export interface DailyStats {
  date: string
  count: number
}

export interface TopNews {
  id: number
  title: string
  views: number
  rank: number
}

export interface AuthorStats {
  name: string
  count: number
}

// ===================== 统一响应 =====================

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface ActiveUser {
  id: number
  username: string
  avatar: string
  points: number
  level: string
  rank: number
}

export interface HotTopic {
  id: number
  name: string
  postCount: number
  isHot: boolean
}