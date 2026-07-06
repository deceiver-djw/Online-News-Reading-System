<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import { useUserStore } from '@/stores/user'
import { useSubscriptionStore } from '@/stores/subscription'
import type { NewsItem } from '@/api/types'
import NavBar from '@/components/news/NavBar.vue'
import FooterSection from '@/components/news/FooterSection.vue'
import { getCoverImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const newsStore = useNewsStore()
const userStore = useUserStore()
const subscriptionStore = useSubscriptionStore()

const currentCategory = ref(route.params.category as string || '全部')
const currentPage = ref(1)
const pageSize = 9
const sortType = ref<'hot' | 'latest' | 'recommend'>('hot')
const allNews = ref<NewsItem[]>([])
const loading = ref(false)

const categories = ['全部', '国内', '国际', '财经', '科技', '体育', '娱乐', '健康', '教育']

// Client-side sort
const sortedNews = computed(() => {
  const list = [...allNews.value]
  if (sortType.value === 'latest') {
    list.sort((a, b) => new Date(b.publishTime || 0).getTime() - new Date(a.publishTime || 0).getTime())
  } else {
    list.sort((a, b) => b.viewCount - a.viewCount)
  }
  return list
})

// Client-side pagination
const pagedNews = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedNews.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(sortedNews.value.length / pageSize) || 1)

const loadNews = async () => {
  loading.value = true
  try {
    const catId = currentCategory.value === '全部'
      ? undefined
      : newsStore.categories.find(c => c.name === currentCategory.value)?.id
    const result = await newsStore.getNewsList(catId, 1, 100)
    allNews.value = result || []
  } catch (error) {
    console.error('加载新闻失败:', error)
    allNews.value = []
  } finally {
    loading.value = false
    currentPage.value = 1
  }
}

watch(
  () => route.params.category,
  (newCategory) => {
    currentCategory.value = (newCategory as string) || '全部'
    loadNews()
  },
)

watch(sortType, () => {
  currentPage.value = 1
})

onMounted(async () => {
  await newsStore.loadCategories()
  if (userStore.user?.id) {
    subscriptionStore.loadSubscriptions(userStore.user.id)
  }
  loadNews()
})

const switchCategory = (category: string) => {
  router.push(`/news/category/${category}`).catch((err) => {
    if (err.name !== 'NavigationDuplicated') {
      throw err
    }
  })
}

const toggleCategorySubscription = (categoryName: string) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const cat = newsStore.categories.find(c => c.name === categoryName)
  if (cat) {
    subscriptionStore.toggleSubscription(userStore.user!.id, cat.id)
  }
}

const isCategorySubscribed = (categoryName: string): boolean => {
  const cat = newsStore.categories.find(c => c.name === categoryName)
  return cat ? subscriptionStore.subscribedCategories.includes(cat.id) : false
}

const goToNewsDetail = (id: number) => {
  router.push(`/news/${id}`)
}

const getCategoryName = (news: NewsItem) => {
  return news.category?.name || '未分类'
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

const formatViews = (views: number) => {
  if (views >= 10000) {
    return (views / 10000).toFixed(1) + '万'
  }
  return views.toString()
}
</script>

<template>
  <div class="category-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="category-header">
          <h1 class="category-title">{{ currentCategory === '全部' ? '热点新闻' : currentCategory }}</h1>
          <div class="sort-tabs">
            <button
              v-for="sort in [{ key: 'hot', label: '🔥 热门' }, { key: 'latest', label: '⏰ 最新' }, { key: 'recommend', label: '⭐ 推荐' }]"
              :key="sort.key"
              :class="['sort-tab', { active: sortType === sort.key }]"
              @click="sortType = sort.key as 'hot' | 'latest' | 'recommend'; currentPage = 1"
            >
              {{ sort.label }}
            </button>
          </div>
        </div>
        
        <div class="category-nav">
          <button
            v-for="category in categories"
            :key="category"
            :class="['category-btn', { active: currentCategory === category }]"
            @click="switchCategory(category)"
          >
            <span>{{ category }}</span>
            <span
              v-if="userStore.isLoggedIn && category !== '全部'"
              class="subscribe-badge"
              :class="{ subscribed: isCategorySubscribed(category) }"
              @click.stop="toggleCategorySubscription(category)"
              :title="isCategorySubscribed(category) ? '取消订阅' : '订阅此分类'"
            >{{ isCategorySubscribed(category) ? '✓' : '+' }}</span>
          </button>
        </div>
        
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>
        
        <div v-else class="news-grid">
          <div
            v-for="news in pagedNews"
            :key="news.id"
            class="news-card"
            @click="goToNewsDetail(news.id)"
          >
            <div class="news-image">
              <img v-if="getCoverImageUrl(news.coverImage)" :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
              <div v-else class="placeholder-img" style="background: linear-gradient(135deg, #e8f0fe, #1a73e8);">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <circle cx="8.5" cy="8.5" r="1.5"/>
                  <path d="M21 15l-5-5L5 21"/>
                </svg>
              </div>
            </div>
            <div class="news-content">
              <div class="news-header">
                <span class="tag tag-blue">{{ getCategoryName(news) }}</span>
                <template v-if="news.tags">
                  <span class="tag tag-line" v-for="tag in news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                </template>
              </div>
              <h3 class="news-title">{{ news.title }}</h3>
              <p class="news-summary">{{ news.summary }}</p>
              <div class="news-footer">
                <span class="news-author">{{ news.author?.username || news.source || '未知' }}</span>
                <span class="news-date">{{ formatDate(news.publishTime) }}</span>
                <span class="news-views">👁️ {{ formatViews(news.viewCount) }}</span>
                <span class="news-comments">💬 {{ news.commentCount }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading && totalPages > 1" class="pagination">
          <button
            class="page-btn"
            :disabled="currentPage <= 1"
            @click="currentPage--"
          >
            <svg width="16" height="16" viewBox="0 0 20 20" fill="none">
              <path d="M12 4l-6 6 6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button
            class="page-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage++"
          >
            <svg width="16" height="16" viewBox="0 0 20 20" fill="none">
              <path d="M8 4l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <div v-if="!loading && pagedNews.length === 0" class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
          </svg>
          <p>暂无相关新闻</p>
        </div>
      </div>
    </main>
    <FooterSection />
  </div>
</template>

<style scoped>
.category-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.main-content {
  padding-top: 72px;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32px 0 16px;
}

.category-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1a2332;
}

.sort-tabs {
  display: flex;
  gap: 4px;
  background: white;
  padding: 4px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.sort-tab {
  padding: 8px 16px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sort-tab.active {
  background: #1a73e8;
  color: white;
}

.sort-tab:hover:not(.active) {
  color: #1a73e8;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 0;
  border-bottom: 1px solid #eef2f7;
  margin-bottom: 32px;
}

.category-btn {
  padding: 8px 20px;
  background: white;
  border: 2px solid #e1e8ef;
  border-radius: 50px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-btn:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.category-btn.active {
  background: #1a73e8;
  border-color: #1a73e8;
  color: white;
}

.subscribe-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 700;
  margin-left: 4px;
  background: #e1e8ef;
  color: #5f6b7a;
  transition: all 0.3s ease;
  cursor: pointer;
}

.subscribe-badge.subscribed {
  background: #e6f7e6;
  color: #2e7d32;
}

.subscribe-badge:hover {
  background: #1a73e8;
  color: white;
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

.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.news-card {
  display: flex;
  gap: 16px;
  background: white;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.news-card:hover {
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.12);
  transform: translateY(-2px);
}

.news-image {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.news-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.news-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.news-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a2332;
  line-height: 1.4;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card:hover .news-title {
  color: #1a73e8;
}

.news-summary {
  font-size: 0.85rem;
  color: #5f6b7a;
  line-height: 1.5;
  margin-bottom: 12px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 0.78rem;
  color: #8e99a9;
}

.news-author {
  font-weight: 600;
  color: #5f6b7a;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px 0;
}

.page-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e1e8ef;
  border-radius: 8px;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #e8f0fe;
  color: #1a73e8;
  border-color: #1a73e8;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9rem;
  color: #5f6b7a;
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

@media (max-width: 900px) {
  .news-grid {
    grid-template-columns: 1fr;
  }
  
  .news-image {
    width: 100%;
    height: 180px;
  }
  
  .news-card {
    flex-direction: column;
  }
  
  .category-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .category-title {
    font-size: 1.5rem;
  }
}
</style>