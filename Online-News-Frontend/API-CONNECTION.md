# Axios 后端连接配置说明

## 📋 配置概览

### 环境变量配置
```env
# .env 文件
VITE_API_BASE_URL=http://localhost:8080/api
```

### Axios 实例配置
- **Base URL**: `http://localhost:8080/api`
- **超时时间**: 15 秒
- **Content-Type**: `application/json`
- **自动添加 Token**: 从 localStorage 读取

## 🔧 Axios 拦截器

### 请求拦截器
```typescript
// 自动添加 Authorization header
config.headers.Authorization = `Bearer ${token}`
```

### 响应拦截器
```typescript
// 统一处理响应
- code 200/0: 成功，返回数据
- code 401: Token 过期，清除并跳转登录
- 其他 code: 视为业务错误
```

## 📡 可用的 API 接口

### 认证模块 (auth.ts)
```typescript
// 用户登录
POST /api/auth/login
Body: { username, password }

// 用户注册
POST /api/auth/register
Body: { username, email, password }

// 退出登录
POST /api/auth/logout

// 获取当前用户信息
GET /api/auth/me

// 刷新 Token
POST /api/auth/refresh
```

### 新闻模块 (news.ts)
```typescript
// 获取新闻列表
GET /api/news
Params: { page, pageSize, category, keyword, sort, status }

// 获取新闻详情
GET /api/news/:id

// 获取热门新闻
GET /api/news/hot

// 获取推荐新闻
GET /api/news/recommended

// 搜索新闻
GET /api/news/search
Params: { keyword, page }

// 获取热门搜索关键词
GET /api/news/hot-keywords

// 增加新闻阅读量
POST /api/news/:id/views

// 点赞/取消点赞新闻
POST /api/news/:id/like

// 获取轮播图列表
GET /api/banners

// 获取实时快讯
GET /api/news/breaking
```

### 媒体模块 (media.ts)
```typescript
// 获取视频列表
GET /api/videos

// 获取视频详情
GET /api/videos/:id

// 获取图库列表
GET /api/galleries

// 获取播客列表
GET /api/podcasts
```

### 社区模块 (community.ts)
```typescript
// 获取帖子列表
GET /api/posts

// 获取帖子详情
GET /api/posts/:id

// 创建帖子
POST /api/posts

// 获取评论列表
GET /api/comments

// 创建评论
POST /api/comments
```

## 🧪 测试连接

### 方法 1: 使用浏览器控制台
1. 打开应用: http://localhost:5174/
2. 按 F12 打开开发者工具
3. 在控制台输入:
```javascript
testBackendConnection()
```

### 方法 2: 手动测试
```typescript
import { getNewsList } from '@/api/news'

// 测试获取新闻列表
const response = await getNewsList({ page: 1, pageSize: 10 })
console.log(response)
```

## 🔍 调试技巧

### 查看请求详情
```typescript
// 在 Network 标签页查看所有请求
// 检查:
// - Request URL
// - Request Headers (特别是 Authorization)
// - Response Data
// - Status Code
```

### 常见错误处理
```typescript
try {
  const response = await getNewsList()
} catch (error) {
  if (error.response) {
    // 服务器返回了响应，但状态码不在 2xx 范围
    console.error('Status:', error.response.status)
    console.error('Data:', error.response.data)
  } else if (error.request) {
    // 请求已发送但没有收到响应
    console.error('No response received')
  } else {
    // 设置请求时出错
    console.error('Error:', error.message)
  }
}
```

## 📝 响应数据格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 实际数据
  }
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10
  }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## 🔐 认证流程

### 1. 登录获取 Token
```typescript
import { login } from '@/api/auth'

const response = await login({ username, password })
const { token, user } = response.data

// 保存到 localStorage
localStorage.setItem('token', token)
localStorage.setItem('user', JSON.stringify(user))
```

### 2. 后续请求自动携带 Token
```typescript
// axios 拦截器会自动添加
headers: {
  'Authorization': 'Bearer {token}'
}
```

### 3. Token 过期处理
```typescript
// 响应拦截器会自动:
// 1. 清除 localStorage 中的 token 和 user
// 2. 跳转到首页
```

## 🌐 Swagger 文档

访问 Swagger UI 查看完整的 API 文档:
```
http://localhost:8080/swagger-ui/index.html
```

在 Swagger 中你可以:
- 查看所有可用的 API 端点
- 查看请求/响应格式
- 直接测试 API 接口
- 查看参数说明

## 📚 使用示例

### 获取新闻列表
```typescript
import { getNewsList } from '@/api/news'

const newsList = await getNewsList({
  page: 1,
  pageSize: 10,
  category: '科技',
  sort: 'hot'
})

console.log(newsList.data.items)
```

### 用户登录
```typescript
import { login } from '@/api/auth'

const result = await login({
  username: 'admin',
  password: '123456'
})

if (result.code === 200) {
  console.log('登录成功', result.data.user)
}
```

### 搜索新闻
```typescript
import { searchNews } from '@/api/news'

const result = await searchNews('AI', 1)
console.log('搜索结果:', result.data.items)
```

## ⚠️ 注意事项

1. **CORS 配置**: 确保后端已配置允许跨域请求
2. **Token 存储**: Token 存储在 localStorage 中，注意安全性
3. **错误处理**: 所有 API 调用都应该使用 try-catch 处理错误
4. **类型安全**: 使用 TypeScript 类型定义确保类型安全
5. **环境变量**: 开发环境使用 `.env`，生产环境使用 `.env.production`

## 🚀 下一步

1. 确保 Swagger 文档可访问: http://localhost:8080/swagger-ui/index.html
2. 使用 Swagger 测试各个 API 接口
3. 在浏览器控制台运行 `testBackendConnection()` 测试连接
4. 根据实际后端 API 调整接口定义