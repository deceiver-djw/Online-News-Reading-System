<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import { useFavoriteStore } from '@/stores/favorite'
import { useUserStore } from '@/stores/user'
import type { NewsItem } from '@/api/types'
import { getRelatedNews, addComment as addCommentApi, getCommentsByNewsId as getCommentsApi, likeComment as likeCommentApi, deleteComment as deleteCommentApi } from '@/api/news'
import NavBar from '@/components/news/NavBar.vue'
import FooterSection from '@/components/news/FooterSection.vue'
import { getCoverImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const newsStore = useNewsStore()
const favoriteStore = useFavoriteStore()
const userStore = useUserStore()

const newsId = ref(Number(route.params.id))
const news = ref<NewsItem | null>(null)
const loading = ref(true)

const commentText = ref('')
const comments = ref<any[]>([])
const isFavorited = ref(false)

const loadNews = async () => {
  loading.value = true
  news.value = await newsStore.getNewsById(newsId.value)
  loading.value = false
}

const loadComments = async () => {
  try {
    const response = await getCommentsApi(newsId.value)
    comments.value = response.data || []
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = []
  }
}

const checkFavoriteStatus = async () => {
  if (userStore.user?.id) {
    isFavorited.value = await favoriteStore.isFavorited(userStore.user.id, newsId.value)
  }
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    newsId.value = Number(newId)
    loadNews()
    loadComments()
    checkFavoriteStatus()
  }
})

onMounted(() => {
  loadNews()
  loadComments()
  checkFavoriteStatus()
})

watch(news, (newNews) => {
  if (newNews) {
    newsStore.incrementViews(newsId.value)
    if (userStore.user?.id) {
      newsStore.recordReadHistory(userStore.user.id, newsId.value)
    }
  }
}, { immediate: false })

const handleLike = async () => {
  if (!userStore.user?.id) {
    router.push('/login')
    return
  }
  const result = await newsStore.toggleLike(newsId.value, userStore.user.id)
  if (news.value) {
    news.value.likeCount = result ? news.value.likeCount + 1 : Math.max(0, news.value.likeCount - 1)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!news.value) return
  isFavorited.value = await favoriteStore.toggleFavorite(userStore.user!.id, news.value.id)
}

const submitComment = async () => {
  if (!commentText.value.trim() || !userStore.isLoggedIn || !userStore.user?.id) {
    if (!userStore.isLoggedIn) router.push('/login')
    return
  }
  try {
    await addCommentApi({
      content: commentText.value,
      newsId: newsId.value,
      userId: userStore.user.id,
    })
    commentText.value = ''
    await loadComments()
    if (news.value) {
      news.value.commentCount++
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  }
}

const handleCommentLike = async (commentId: number) => {
  try {
    await likeCommentApi(commentId)
    const comment = findComment(comments.value, commentId)
    if (comment) {
      comment.likeCount++
    }
  } catch (error) {
    console.error('点赞评论失败:', error)
  }
}

const deleteComment = async (commentId: number) => {
  try {
    await deleteCommentApi(commentId)
    comments.value = comments.value.filter(c => c.id !== commentId)
  } catch (error) {
    console.error('删除评论失败:', error)
  }
}

const findComment = (list: any[], id: number): any | undefined => {
  for (const c of list) {
    if (c.id === id) return c
    if (c.replyList?.length > 0) {
      const found = findComment(c.replyList, id)
      if (found) return found
    }
  }
  return undefined
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const relatedNews = ref<NewsItem[]>([])

const loadRelatedNews = async () => {
  try {
    const response = await getRelatedNews(newsId.value, 4)
    relatedNews.value = response.data || []
  } catch (error) {
    console.error('加载相关推荐失败:', error)
  }
}

watch(newsId, () => {
  loadRelatedNews()
}, { immediate: false })

onMounted(() => {
  loadRelatedNews()
})
</script>

<template>
  <div class="news-detail-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="news-detail-content">
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>

          <article v-else-if="news" class="news-article">
            <div class="article-header">
              <span class="tag tag-blue">{{ news.category?.name || '未分类' }}</span>
              <h1 class="article-title">{{ news.title }}</h1>
              <div class="article-meta">
                <span class="meta-item">👤 {{ news.author?.username || news.source || '未知' }}</span>
                <span class="meta-item">📅 {{ formatDate(news.publishTime || news.createdAt) }}</span>
                <span class="meta-item">👁️ {{ news.viewCount }}</span>
              </div>
            </div>

            <div v-if="getCoverImageUrl(news.coverImage)" class="article-cover">
              <img :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
            </div>

            <div v-if="news.tags && news.tags.length > 0" class="article-tags">
              <span
                v-for="tag in news.tags"
                :key="tag.id"
                class="article-tag"
                @click="router.push(`/search?keyword=${tag.tagName}`)"
              >
                #{{ tag.tagName }}
              </span>
            </div>

            <div class="article-body">{{ news.content }}</div>

            <div class="article-actions">
              <button class="action-btn" @click="handleLike">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="#ff6b35">
                  <path d="M9 2C7.34 2 6 3.34 6 5c0 1.5 1.25 3.5 3 5.5 1.75-2 3-4 3-5.5 0-1.66-1.34-3-3-3z"/>
                  <path d="M18 8c-1.5 0-3 .5-4.5 2-1.5-1.5-3-2-4.5-2C6.5 8 5 8.5 3.5 10c-1.5-1.5-3-2-4.5-2v10c1.5 0 3-.5 4.5-2 1.5 1.5 3 2 4.5 2s3-.5 4.5-2c1.5 1.5 3 2 4.5 2V8z"/>
                </svg>
                <span>{{ news.likeCount }}</span>
              </button>
              <button
                :class="['action-btn', { favorited: isFavorited }]"
                @click="handleFavorite"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" :fill="isFavorited ? '#ff3b30' : 'none'" :stroke="isFavorited ? '#ff3b30' : 'currentColor'" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
              </button>
            </div>
          </article>

          <div v-else class="empty-state">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
            <p>新闻不存在或已被删除</p>
          </div>

          <div class="comments-section">
            <div class="section-header">
              <h2 class="section-title">评论区</h2>
              <span class="comments-count">{{ comments.length }} 条评论</span>
            </div>

            <div class="comment-input-wrapper">
              <textarea
                v-model="commentText"
                placeholder="写下你的评论..."
                class="comment-input"
                rows="3"
              ></textarea>
              <button class="btn btn-primary comment-submit" @click="submitComment">发表评论</button>
            </div>

            <div class="comments-list">
              <div
                v-for="comment in comments"
                :key="comment.id"
                :class="['comment-item', { pinned: comment.isPinned }]"
              >
                <div v-if="comment.isPinned" class="pinned-badge">🔥 热门评论</div>
                <div class="comment-content">
                  <img
                    :src="getCoverImageUrl(comment.user?.avatar) || 'https://api.dicebear.com/7.x/avataaars/svg?seed=user'"
                    alt="用户头像"
                    class="comment-avatar"
                  />
                  <div class="comment-body">
                    <div class="comment-header">
                      <span class="comment-author">{{ comment.user?.username || '用户' }}</span>
                      <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                    </div>
                    <p class="comment-text">{{ comment.content }}</p>
                    <div class="comment-actions">
                      <button class="comment-action-btn" @click="handleCommentLike(comment.id)">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                          <path d="M8 2.5c-1.38 0-2.5 1.12-2.5 2.5s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5S9.38 2.5 8 2.5z"/>
                          <path d="M16 8c0-3.31-2.69-6-6-6H6c-3.31 0-6 2.69-6 6v6c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8z"/>
                        </svg>
                        <span>{{ comment.likeCount }}</span>
                      </button>
                      <button v-if="userStore.isAdmin" class="comment-action-btn delete" @click="deleteComment(comment.id)">
                        删除
                      </button>
                    </div>

                    <div v-if="comment.replyList && comment.replyList.length > 0" class="replies-list">
                      <div
                        v-for="reply in comment.replyList"
                        :key="reply.id"
                        class="reply-item"
                      >
                        <img
                          :src="getCoverImageUrl(reply.user?.avatar) || 'https://api.dicebear.com/7.x/avataaars/svg?seed=reply'"
                          alt="用户头像"
                          class="reply-avatar"
                        />
                        <div class="reply-body">
                          <span class="reply-author">{{ reply.user?.username || '用户' }}</span>
                          <span class="reply-text">{{ reply.content }}</span>
                          <div class="reply-meta">
                            <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="comments.length === 0" class="empty-comments">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                  <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
                </svg>
                <p>暂无评论，快来发表第一条评论吧！</p>
              </div>
            </div>
          </div>

          <div v-if="relatedNews.length > 0" class="related-news">
            <div class="section-header">
              <h2 class="section-title">相关推荐</h2>
            </div>
            <div class="related-news-list">
              <div
                v-for="item in relatedNews"
                :key="item.id"
                class="related-news-item"
                @click="router.push(`/news/${item.id}`)"
              >
                <div class="related-news-image">
                  <img v-if="getCoverImageUrl(item.coverImage)" :src="getCoverImageUrl(item.coverImage)!" :alt="item.title" class="cover-img" />
                  <div v-else class="placeholder-img" style="background: linear-gradient(135deg, #e8f0fe, #f5f8fc);">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#1a73e8" stroke-width="1.5">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <path d="M21 15l-5-5L5 21"/>
                    </svg>
                  </div>
                </div>
                <div class="related-news-content">
                  <h4 class="related-news-title">{{ item.title }}</h4>
                  <span class="related-news-meta">{{ formatDate(item.publishTime) }} · {{ item.viewCount }}阅读</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    <FooterSection />
  </div>
</template>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.main-content {
  padding-top: 72px;
}

.container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 24px;
}

.news-detail-content {
  padding: 40px 0;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #8e99a9;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e1e8ef;
  border-top-color: #1a73e8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.news-article {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
  margin-bottom: 32px;
}

.article-header {
  margin-bottom: 24px;
}

.article-title {
  font-size: 2rem;
  font-weight: 800;
  color: #1a2332;
  line-height: 1.3;
  margin: 16px 0;
}

.article-meta {
  display: flex;
  gap: 20px;
  font-size: 0.85rem;
  color: #8e99a9;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.article-tag {
  padding: 4px 12px;
  background: #e8f0fe;
  border-radius: 20px;
  font-size: 0.75rem;
  color: #1a73e8;
  cursor: pointer;
  transition: all 0.3s ease;
}

.article-tag:hover {
  background: #1a73e8;
  color: white;
}

.article-cover {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  max-height: 400px;
}

.article-cover .cover-img {
  width: 100%;
  height: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

.article-body {
  font-size: 1rem;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.article-actions {
  display: flex;
  gap: 24px;
  padding: 24px 0;
  border-top: 1px solid #eef2f7;
  margin-top: 32px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #f5f8fc;
  border: none;
  border-radius: 50px;
  font-size: 0.9rem;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #e8f0fe;
  color: #1a73e8;
}

.action-btn.favorited {
  color: #ff3b30;
}

.comments-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a2332;
}

.comments-count {
  font-size: 0.85rem;
  color: #8e99a9;
}

.comment-input-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.comment-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e1e8ef;
  border-radius: 12px;
  font-size: 0.9rem;
  resize: vertical;
  font-family: inherit;
  outline: none;
  transition: all 0.3s ease;
}

.comment-input:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.comment-submit {
  padding: 12px 24px;
  flex-shrink: 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 20px;
  background: #fafcff;
  border-radius: 12px;
  position: relative;
}

.comment-item.pinned {
  background: #fff3ed;
  border: 1px solid #ff6b35;
}

.pinned-badge {
  position: absolute;
  top: -10px;
  left: 20px;
  padding: 2px 12px;
  background: #ff6b35;
  color: white;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.comment-content {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #1a2332;
}

.comment-time {
  font-size: 0.75rem;
  color: #8e99a9;
}

.comment-text {
  font-size: 0.9rem;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: none;
  border: none;
  font-size: 0.8rem;
  color: #8e99a9;
  cursor: pointer;
  transition: all 0.3s ease;
}

.comment-action-btn:hover {
  color: #1a73e8;
}

.comment-action-btn.delete:hover {
  color: #ff3b30;
}

.replies-list {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid #eef2f7;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f4f8;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
}

.reply-author {
  font-weight: 600;
  font-size: 0.8rem;
  color: #1a2332;
}

.reply-text {
  font-size: 0.85rem;
  color: #333;
  margin-left: 8px;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.reply-time {
  font-size: 0.7rem;
  color: #8e99a9;
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #8e99a9;
}

.empty-comments p {
  margin-top: 12px;
  font-size: 0.95rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #8e99a9;
}

.empty-state p {
  margin-top: 16px;
  font-size: 1rem;
}

.related-news {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
}

.related-news-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.related-news-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fafcff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-news-item:hover {
  background: #e8f0fe;
  transform: translateX(4px);
}

.related-news-image {
  width: 80px;
  height: 60px;
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

.related-news-image .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-news-content {
  flex: 1;
  overflow: hidden;
}

.related-news-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a2332;
  line-height: 1.4;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-news-meta {
  font-size: 0.75rem;
  color: #8e99a9;
}

@media (max-width: 768px) {
  .news-article {
    padding: 24px;
  }

  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    flex-wrap: wrap;
  }

  .article-actions {
    gap: 16px;
  }

  .action-btn {
    padding: 8px 16px;
    font-size: 0.8rem;
  }

  .comment-input-wrapper {
    flex-direction: column;
  }

  .related-news-list {
    grid-template-columns: 1fr;
  }
}
</style>