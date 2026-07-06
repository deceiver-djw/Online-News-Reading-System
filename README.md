# Online News — 在线新闻平台

一个全栈在线新闻平台，采用 **Vue 3 + Spring Boot** 架构，集成 **DeepSeek AI** 实现智能新闻生成与配图。

## 项目结构

```
├── Online-News-Frontend/   # 前端 — Vue 3 + TypeScript + Vite
├── Online-News-Backend/    # 后端 — Spring Boot 3 + MyBatis-Plus
├── online_news.sql         # 数据库初始化脚本 (MySQL)
└── images/                 # 项目截图 / 图片资源
```

## 技术栈

### 前端

| 技术 | 版本 |
|------|------|
| Vue | ^3.5 |
| TypeScript | ~6.0 |
| Vite | ^8.0 |
| Vue Router | ^5.1 |
| Pinia | ^3.0 |
| Axios | ^1.18 |

### 后端

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.5.15 |
| Java | 21 |
| MyBatis-Plus | 3.5.9 |
| MySQL | 9.5 |
| Spring AI (DeepSeek) | 1.1.8 |
| SpringDoc OpenAPI | 2.0.4 |
| Kotlin | 2.3.10 |

## 功能模块

### 用户端

- **用户认证** — 注册、登录、角色权限管理
- **新闻浏览** — 分类浏览、标签筛选、全文阅读
- **搜索推荐** — 关键词搜索、个性化推荐
- **评论互动** — 发表评论、点赞评论
- **收藏管理** — 收藏/取消收藏新闻
- **订阅通知** — 订阅分类、系统通知
- **阅读历史** — 浏览记录追踪

### 管理端

- **新闻编辑** — 发布、编辑、管理新闻内容
- **分类管理** — 新闻分类维护
- **标签管理** — 标签体系管理
- **数据统计** — 统计快照、数据报表

### AI 能力

- **AI 新闻生成** — 基于 DeepSeek 大模型自动生成新闻稿件
- **AI 图片生成** — 为新闻自动生成配图
- **智能标签** — 自动为新闻打标签

## 快速开始

### 环境要求

- **Node.js** >= 22.18.0 或 >= 24.12.0
- **Java** >= 21
- **Maven** >= 3.8
- **MySQL** >= 8.0

### 1. 数据库初始化

```bash
mysql -u root -p < online_news.sql
```

脚本会创建 `online_news` 数据库及所有表结构，并包含示例数据。

### 2. 启动后端

```bash
cd Online-News-Backend

# 修改 src/main/resources/application.yml 中的数据库连接信息
# 配置 DeepSeek API Key

./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`，Swagger 文档地址：`http://localhost:8080/swagger-ui/index.html`

### 3. 启动前端

```bash
cd Online-News-Frontend

npm install

npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`

### 生产构建

```bash
# 前端构建
cd Online-News-Frontend
npm run build

# 后端打包
cd Online-News-Backend
./mvnw package -DskipTests
```

## API 文档

启动后端后访问 Swagger UI：`http://localhost:8080/swagger-ui/index.html`

主要接口模块：

| 控制器 | 路径前缀 | 说明 |
|--------|----------|------|
| AuthController | `/api/auth` | 用户认证 |
| NewsController | `/api/news` | 新闻浏览 |
| NewsEditorController | `/api/news/editor` | 新闻编辑 |
| CategoryController | `/api/categories` | 分类管理 |
| CommentController | `/api/comments` | 评论管理 |
| LikeController | `/api/likes` | 点赞管理 |
| CollectionController | `/api/collections` | 收藏管理 |
| SubscriptionController | `/api/subscriptions` | 订阅管理 |
| NotificationController | `/api/notifications` | 通知管理 |
| ReadHistoryController | `/api/history` | 阅读历史 |
| StatisticsController | `/api/statistics` | 数据统计 |
| RecommendController | `/api/recommend` | 新闻推荐 |
| AINewsGenerationController | `/api/ai/news` | AI 新闻生成 |

## 数据库表

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| categories | 新闻分类表 |
| news | 新闻表 |
| tags | 标签表 |
| comments | 评论表 |
| like_records | 点赞记录表 |
| collections | 收藏表 |
| subscriptions | 订阅表 |
| notifications | 通知表 |
| read_history | 阅读历史表 |
| statistic_snapshots | 统计快照表 |

## License

本项目为实训教学项目。
