<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import { useUserStore } from '@/stores/user'
import { useHistoryStore } from '@/stores/history'
import { useFavoriteStore } from '@/stores/favorite'
import { useSubscriptionStore } from '@/stores/subscription'
import NavBar from '@/components/news/NavBar.vue'
import FooterSection from '@/components/news/FooterSection.vue'
import { getCoverImageUrl } from '@/utils/image'

const router = useRouter()
const newsStore = useNewsStore()
const userStore = useUserStore()
const historyStore = useHistoryStore()
const favoriteStore = useFavoriteStore()
const subscriptionStore = useSubscriptionStore()

const currentCategoryId = ref<number | undefined>(undefined)
const searchQuery = ref('')

const bannerNews = computed(() => {
  return newsStore.headlineNews.length > 0
    ? newsStore.headlineNews
    : newsStore.newsList.slice(0, 5)
})

const hotNewsList = computed(() => {
  return newsStore.hotNews
})

const currentNewsList = computed(() => {
  const list = newsStore.newsList.filter(n => n.status === 'PUBLISHED')
  if (currentCategoryId.value) {
    return list.filter(n => n.categoryId === currentCategoryId.value)
  }
  return list
})

const recommendNews = computed(() => {
  if (!userStore.isLoggedIn) return []
  return newsStore.recommendedNews
})

let refreshInterval: number

onMounted(async () => {
  await Promise.all([
    newsStore.loadCategories(),
    newsStore.loadNewsList(),
    newsStore.loadHeadlineNews(),
    newsStore.loadHotNews(),
  ])

  // Load recommended news if user logged in
  if (userStore.user?.id) {
    newsStore.loadRecommendedNews(userStore.user.id)
  }

  // Try to load favorites, history and subscriptions if user logged in
  if (userStore.user?.id) {
    await favoriteStore.loadFavorites(userStore.user.id)
    subscriptionStore.loadSubscriptions(userStore.user.id)
  }

  refreshInterval = setInterval(async () => {
    await Promise.all([
      newsStore.loadNewsList(),
      newsStore.loadHotNews(),
    ])
  }, 60000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})

const switchCategory = (catId?: number) => {
  currentCategoryId.value = catId
}

const toggleCategorySubscription = (categoryId: number) => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  subscriptionStore.toggleSubscription(userStore.user!.id, categoryId)
}

const isCategorySubscribed = (categoryId: number): boolean => {
  return subscriptionStore.subscribedCategories.includes(categoryId)
}

const goToNewsDetail = async (id: number) => {
  // Record reading history if logged in
  if (userStore.user?.id) {
    try {
      await newsStore.recordReadHistory(userStore.user.id, id)
    } catch (e) {
      // Silent fail for history recording
    }
  }
  router.push(`/news/${id}`)
}

const goToSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchQuery.value } })
  }
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))

  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`

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

const getCategoryName = (categoryId: number): string => {
  const cat = newsStore.categories.find(c => c.id === categoryId)
  return cat?.name || '未分类'
}

const getUserName = (news: any): string => {
  return news.author?.username || news.source || '未知'
}
</script>

<template>
  <div class="home-page">
    <NavBar />
    <main class="main-content">
      <div class="search-section">
        <div class="container">
          <div class="search-box-wrapper">
            <div class="search-box">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="9" cy="9" r="6.5" stroke="#8e99a9" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="#8e99a9" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索新闻、话题、人物..."
                @keyup.enter="goToSearch"
              />
              <button class="search-btn" @click="goToSearch">搜索</button>
            </div>
          </div>
        </div>
      </div>

      <div class="container">
        <!-- 头条轮播图 -->
        <div class="banner-section">
          <div class="banner-header">
            <h2 class="banner-title">🔥 头条新闻</h2>
          </div>
          <div class="banner-slider">
            <div
              v-for="(news, index) in bannerNews"
              :key="news.id"
              class="banner-item"
              @click="goToNewsDetail(news.id)"
            >
              <div class="banner-image">
                <img v-if="getCoverImageUrl(news.coverImage)" :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
                <div v-else class="placeholder-img" :style="{ background: `linear-gradient(135deg, #e8f0fe, #1a73e8)` }">
                  <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <path d="M21 15l-5-5L5 21"/>
                  </svg>
                </div>
              </div>
              <div class="banner-content">
                <span class="tag tag-blue">{{ getCategoryName(news.categoryId) }}</span>
                <template v-if="news.tags">
                  <span class="tag tag-line" v-for="tag in news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                </template>
                <h3 class="banner-text">{{ news.title }}</h3>
                <div class="banner-meta">
                  <span>{{ getUserName(news) }}</span>
                  <span>{{ formatViews(news.viewCount) }}阅读</span>
                  <span>{{ formatDate(news.publishTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="content-wrapper">
          <div class="main-content-area">
            <!-- 分类切换 -->
            <div class="category-tabs">
              <button
                :class="['category-tab', { active: !currentCategoryId }]"
                @click="switchCategory(undefined)"
              >
                全部
              </button>
              <button
                v-for="category in newsStore.categories"
                :key="category.id"
                :class="['category-tab', { active: currentCategoryId === category.id }]"
                @click="switchCategory(category.id)"
              >
                <span>{{ category.icon || '' }} {{ category.name }}</span>
                <span
                  v-if="userStore.isLoggedIn"
                  class="subscribe-badge"
                  :class="{ subscribed: isCategorySubscribed(category.id) }"
                  @click.stop="toggleCategorySubscription(category.id)"
                  :title="isCategorySubscribed(category.id) ? '取消订阅' : '订阅此分类'"
                >{{ isCategorySubscribed(category.id) ? '✓' : '+' }}</span>
              </button>
            </div>

            <!-- 新闻列表 -->
            <div class="news-list">
              <div
                v-for="news in currentNewsList.slice(0, 10)"
                :key="news.id"
                class="news-item"
                @click="goToNewsDetail(news.id)"
              >
                <div class="news-text">
                  <div class="news-header">
                    <span class="tag tag-blue">{{ getCategoryName(news.categoryId) }}</span>
                    <template v-if="news.tags">
                      <span class="tag tag-line" v-for="tag in news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                    </template>
                  </div>
                  <h3 class="news-title">{{ news.title }}</h3>
                  <p class="news-summary">{{ news.summary }}</p>
                  <div class="news-footer">
                    <span>{{ getUserName(news) }}</span>
                    <span>{{ formatDate(news.publishTime) }}</span>
                    <span>👁️ {{ formatViews(news.viewCount) }}</span>
                    <span>💬 {{ news.commentCount }}</span>
                  </div>
                </div>
                <div class="news-image">
                  <img v-if="getCoverImageUrl(news.coverImage)" :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
                  <div v-else class="placeholder-img" :style="{ background: `linear-gradient(135deg, #e8f0fe, #1a73e8)` }">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <path d="M21 15l-5-5L5 21"/>
                    </svg>
                  </div>
                </div>
              </div>
            </div>

            <!-- 猜你喜欢 -->
            <div v-if="userStore.isLoggedIn && recommendNews.length > 0" class="recommend-section">
              <div class="section-header">
                <h2 class="section-title">❤️ 猜你喜欢</h2>
              </div>
              <div class="recommend-grid">
                <div
                  v-for="news in recommendNews"
                  :key="news.id"
                  class="recommend-card"
                  @click="goToNewsDetail(news.id)"
                >
                  <div class="recommend-image">
                    <img v-if="getCoverImageUrl(news.coverImage)" :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
                    <div v-else class="placeholder-img" :style="{ background: `linear-gradient(135deg, #e8f0fe, #1a73e8)` }">
                      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <path d="M21 15l-5-5L5 21"/>
                      </svg>
                    </div>
                  </div>
                  <h4 class="recommend-title">{{ news.title }}</h4>
                  <p class="recommend-meta">{{ formatDate(news.publishTime) }} · {{ formatViews(news.viewCount) }}阅读</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 侧边栏：实时热点 -->
          <div class="sidebar">
            <div class="hot-section">
              <div class="section-header">
                <h2 class="section-title">🔥 实时热点</h2>
                <span class="hot-refresh">每分钟更新</span>
              </div>
              <div class="hot-list">
                <div
                  v-for="(news, index) in hotNewsList"
                  :key="news.id"
                  class="hot-item"
                  @click="goToNewsDetail(news.id)"
                >
                  <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                  <span class="hot-title">{{ news.title }}</span>
                  <span class="hot-views">{{ formatViews(news.viewCount) }}</span>
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
.home-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.main-content {
  padding-top: 72px;
}

.search-section {
  background: white;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.search-box-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  background: #f5f8fc;
  border: 2px solid #e1e8ef;
  border-radius: 50px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.search-box input {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: #1a2332;
  outline: none;
  font-family: inherit;
}

.search-box input::placeholder {
  color: #8e99a9;
}

.search-btn {
  padding: 8px 24px;
  background: #1a73e8;
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: #1557b0;
}

.banner-section {
  padding: 32px 0;
}

.banner-header {
  margin-bottom: 16px;
}

.banner-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a2332;
}

.banner-slider {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.banner-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
}

.banner-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.12);
}

.banner-item:first-child {
  grid-column: span 2;
}

.banner-image {
  height: 180px;
  overflow: hidden;
}

.banner-item:first-child .banner-image {
  height: 250px;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-content {
  padding: 16px;
}

.banner-text {
  font-size: 1rem;
  font-weight: 700;
  color: #1a2332;
  line-height: 1.4;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-item:first-child .banner-text {
  font-size: 1.2rem;
  -webkit-line-clamp: 3;
}

.banner-meta {
  display: flex;
  gap: 12px;
  font-size: 0.78rem;
  color: #8e99a9;
}

.content-wrapper {
  display: flex;
  gap: 24px;
}

.main-content-area {
  flex: 1;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.category-tab {
  padding: 8px 20px;
  background: white;
  border: 2px solid #e1e8ef;
  border-radius: 50px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-tab:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.category-tab.active {
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

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.news-item:hover {
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.12);
  transform: translateX(4px);
}

.news-text {
  flex: 1;
}

.news-header {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.news-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1a2332;
  line-height: 1.4;
  margin-bottom: 4px;
}

.news-item:hover .news-title {
  color: #1a73e8;
}

.news-summary {
  font-size: 0.82rem;
  color: #8e99a9;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  gap: 12px;
  font-size: 0.75rem;
  color: #8e99a9;
}

.news-image {
  width: 150px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.recommend-section {
  padding: 32px 0;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a2332;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.12);
}

.recommend-image {
  height: 120px;
  overflow: hidden;
}

.recommend-title {
  padding: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a2332;
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recommend-meta {
  padding: 0 12px 12px;
  font-size: 0.75rem;
  color: #8e99a9;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.hot-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
}

.hot-section .section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hot-refresh {
  font-size: 0.75rem;
  color: #ff9500;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.hot-item:hover {
  background: #fafcff;
}

.hot-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f8fc;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #8e99a9;
  flex-shrink: 0;
}

.hot-rank.top {
  background: #ff9500;
  color: white;
}

.hot-title {
  flex: 1;
  font-size: 0.82rem;
  color: #1a2332;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-item:hover .hot-title {
  color: #1a73e8;
}

.hot-views {
  font-size: 0.75rem;
  color: #8e99a9;
  flex-shrink: 0;
}

@media (max-width: 1024px) {
  .sidebar {
    display: none;
  }

  .banner-slider {
    grid-template-columns: 1fr;
  }

  .banner-item:first-child {
    grid-column: span 1;
  }

  .banner-image {
    height: 200px;
  }

  .banner-item:first-child .banner-image {
    height: 200px;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .news-item {
    flex-direction: column;
  }

  .news-image {
    width: 100%;
    height: 150px;
  }

  .category-tabs {
    gap: 6px;
  }

  .category-tab {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
}
</style>