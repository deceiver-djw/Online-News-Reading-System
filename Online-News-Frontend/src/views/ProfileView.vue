<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useFavoriteStore } from '@/stores/favorite'
import { useHistoryStore } from '@/stores/history'
import { useCommentStore } from '@/stores/comment'
import NavBar from '@/components/news/NavBar.vue'
import { getCoverImageUrl } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()
const historyStore = useHistoryStore()
const commentStore = useCommentStore()

const currentTab = ref<'history' | 'favorites' | 'comments'>('history')

const historyList = computed(() => historyStore.historyList)
const favoritesList = computed(() => favoriteStore.favorites)
const commentsList = computed(() => commentStore.myComments)

const userId = computed(() => userStore.user?.id)

const goToNewsDetail = (id: number) => {
  router.push(`/news/${id}`)
}

const removeFavorite = (newsId: number) => {
  if (userId.value) {
    favoriteStore.removeFavorite(userId.value, newsId)
  }
}

const removeComment = (commentId: number) => {
  commentStore.deleteComment(commentId)
}

const clearHistory = () => {
  if (confirm('确定要清除所有阅读历史吗？')) {
    if (userId.value) {
      historyStore.clearAll(userId.value)
    }
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatViews = (views: number) => {
  if (views >= 10000) {
    return (views / 10000).toFixed(1) + '万'
  }
  return views.toString()
}

const loadData = () => {
  if (userId.value) {
    historyStore.loadHistory(userId.value)
    favoriteStore.loadFavorites(userId.value)
    commentStore.loadMyComments(userId.value)
  }
}

onMounted(loadData)
</script>

<template>
  <div class="profile-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="profile-card">
          <div class="profile-header">
            <div class="avatar">
              <img
                v-if="getCoverImageUrl(userStore.user?.avatar)"
                :src="getCoverImageUrl(userStore.user!.avatar)!"
                :alt="userStore.user?.username"
                class="avatar-img"
              />
              <svg v-else width="80" height="80" viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" fill="#1a73e8"/>
                <circle cx="12" cy="7" r="4" fill="white"/>
              </svg>
            </div>
            <div class="profile-info">
              <h2 class="profile-name">{{ userStore.user?.username || '用户' }}</h2>
              <p class="profile-role">{{ userStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</p>
            </div>
            <div class="profile-stats">
              <div class="stat-item">
                <span class="stat-value">{{ favoritesList.length }}</span>
                <span class="stat-label">收藏</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ commentsList.length }}</span>
                <span class="stat-label">评论</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ historyList.length }}</span>
                <span class="stat-label">浏览</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="tabs-container">
          <div class="tabs">
            <button
              :class="['tab', { active: currentTab === 'history' }]"
              @click="currentTab = 'history'"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M12 8v4l3 3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              阅读历史
            </button>
            <button
              :class="['tab', { active: currentTab === 'favorites' }]"
              @click="currentTab = 'favorites'"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              我的收藏
            </button>
            <button
              :class="['tab', { active: currentTab === 'comments' }]"
              @click="currentTab = 'comments'"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              我的评论
            </button>
          </div>
          
          <div class="tab-content">
            <div v-if="currentTab === 'history'" class="content-section">
              <div v-if="historyList.length > 0" class="action-bar">
                <span class="action-text">共 {{ historyList.length }} 条记录</span>
                <button class="btn btn-outline btn-sm" @click="clearHistory">清除全部</button>
              </div>
              
              <div class="list-container">
                <div
                  v-for="item in historyList"
                  :key="item.id"
                  class="list-item"
                  @click="goToNewsDetail(item.newsId)"
                >
                  <div class="item-content">
                    <span class="tag tag-blue">{{ item.news?.category?.name || '未分类' }}</span>
                    <template v-if="item.news?.tags">
                      <span class="tag tag-line" v-for="tag in item.news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                    </template>
                    <h4 class="item-title">{{ item.news?.title || '未知' }}</h4>
                    <p class="item-summary">{{ item.news?.summary || '' }}</p>
                    <div class="item-meta">
                      <span>{{ item.news?.author?.username || item.news?.source || '未知' }}</span>
                      <span>{{ formatDate(item.readTime) }}</span>
                      <span>👁️ {{ formatViews(item.news?.viewCount ?? 0) }}</span>
                    </div>
                  </div>
                  <div class="item-image">
                    <img v-if="getCoverImageUrl(item.news?.coverImage)" :src="getCoverImageUrl(item.news!.coverImage)!" :alt="item.news?.title" class="cover-img" />
                    <div v-else class="placeholder-img" style="background: linear-gradient(135deg, #e8f0fe, #1a73e8);">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <path d="M21 15l-5-5L5 21"/>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="historyList.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                  <path d="M12 8v4l3 3" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M19 12H5" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <p>暂无阅读历史</p>
              </div>
            </div>
            
            <div v-if="currentTab === 'favorites'" class="content-section">
              <div v-if="favoritesList.length > 0" class="action-bar">
                <span class="action-text">共 {{ favoritesList.length }} 条收藏</span>
              </div>
              
              <div class="list-container">
                <div
                  v-for="item in favoritesList"
                  :key="item.newsId"
                  class="list-item"
                  @click="goToNewsDetail(item.newsId)"
                >
                  <div class="item-content">
                    <span class="tag tag-blue">{{ item.news?.category?.name || '未分类' }}</span>
                    <template v-if="item.news?.tags">
                      <span class="tag tag-line" v-for="tag in item.news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                    </template>
                    <h4 class="item-title">{{ item.news?.title || '未知' }}</h4>
                    <p class="item-summary">{{ item.news?.summary || '' }}</p>
                    <div class="item-meta">
                      <span>{{ item.news?.author?.username || item.news?.source || '未知' }}</span>
                      <span>{{ formatDate(item.createTime) }}</span>
                      <span>👁️ {{ formatViews(item.news?.viewCount ?? 0) }}</span>
                    </div>
                  </div>
                  <div class="item-actions">
                    <button class="action-btn remove" @click.stop="removeFavorite(item.newsId)">取消收藏</button>
                  </div>
                </div>
              </div>
              
              <div v-if="favoritesList.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p>暂无收藏内容</p>
              </div>
            </div>
            
            <div v-if="currentTab === 'comments'" class="content-section">
              <div v-if="commentsList.length > 0" class="action-bar">
                <span class="action-text">共 {{ commentsList.length }} 条评论</span>
              </div>
              
              <div class="comments-container">
                <div
                  v-for="comment in commentsList"
                  :key="comment.id"
                  class="comment-card"
                  @click="goToNewsDetail(comment.newsId)"
                >
                  <div class="comment-content">
                    <span class="comment-news-title">{{ comment.news?.title || '未知新闻' }}</span>
                    <p class="comment-text">{{ comment.content }}</p>
                    <div class="comment-meta">
                      <span>{{ formatDate(comment.createTime) }}</span>
                      <span>👍 {{ comment.likeCount }}</span>
                      <span v-if="comment.replyList?.length > 0">💬 {{ comment.replyList.length }}条回复</span>
                    </div>
                  </div>
                  <div class="comment-actions">
                    <button class="action-btn remove" @click.stop="removeComment(comment.id)">删除</button>
                  </div>
                </div>
              </div>
              
              <div v-if="commentsList.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p>暂无评论记录</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.main-content {
  padding-top: 72px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1a2332;
  margin-bottom: 4px;
}

.profile-role {
  font-size: 0.9rem;
  color: #8e99a9;
}

.profile-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 800;
  color: #1a73e8;
  display: block;
}

.stat-label {
  font-size: 0.78rem;
  color: #8e99a9;
}

.tabs-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #eef2f7;
}

.tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px;
  background: transparent;
  border: none;
  font-size: 0.9rem;
  font-weight: 600;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab.active {
  color: #1a73e8;
  background: #e8f0fe;
}

.tab:hover:not(.active) {
  color: #1a73e8;
}

.tab-content {
  padding: 24px;
}

.content-section {
  min-height: 300px;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.action-text {
  font-size: 0.85rem;
  color: #8e99a9;
}

.btn-sm {
  padding: 6px 16px;
  font-size: 0.8rem;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fafcff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.list-item:hover {
  background: #e8f0fe;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1a2332;
  margin: 8px 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-summary {
  font-size: 0.82rem;
  color: #8e99a9;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 12px;
  font-size: 0.75rem;
  color: #8e99a9;
}

.item-image {
  width: 100px;
  height: 70px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-actions {
  flex-shrink: 0;
}

.action-btn.remove {
  padding: 6px 12px;
  background: #ffebee;
  color: #ff3b30;
  border: none;
  border-radius: 6px;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn.remove:hover {
  background: #ff3b30;
  color: white;
}

.comments-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  background: #fafcff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.comment-card:hover {
  background: #e8f0fe;
}

.comment-content {
  flex: 1;
}

.comment-news-title {
  font-size: 0.78rem;
  color: #1a73e8;
  font-weight: 600;
  margin-bottom: 6px;
  display: block;
}

.comment-text {
  font-size: 0.9rem;
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-meta {
  display: flex;
  gap: 12px;
  font-size: 0.75rem;
  color: #8e99a9;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 0;
  color: #8e99a9;
}

.empty-state p {
  margin-top: 16px;
  font-size: 0.95rem;
}

@media (max-width: 600px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .profile-stats {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #eef2f7;
  }
  
  .list-item {
    flex-direction: column;
  }
  
  .item-image {
    width: 100%;
    height: 150px;
  }
  
  .comment-card {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
