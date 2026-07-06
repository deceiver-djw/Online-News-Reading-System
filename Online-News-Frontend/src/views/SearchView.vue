<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import type { NewsItem } from '@/api/types'
import { searchNews as searchNewsApi, filterNews as filterNewsApi } from '@/api/news'
import NavBar from '@/components/news/NavBar.vue'
import FooterSection from '@/components/news/FooterSection.vue'
import { getCoverImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const newsStore = useNewsStore()

const searchQuery = ref('')
const currentPage = ref(1)
const isFilterOpen = ref(false)
const loading = ref(false)

const filterCategory = ref('')
const filterTimeRange = ref('')
const filterSource = ref('')
const minViews = ref('')
const maxViews = ref('')

const timeRanges = [
  { value: '', label: '全部时间' },
  { value: 'today', label: '今天' },
  { value: 'week', label: '近一周' },
  { value: 'month', label: '近一个月' },
]

const newsList = ref<NewsItem[]>([])
const totalCount = ref(0)
const pageSize = 10
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 1)
const displayedNews = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return newsList.value.slice(start, start + pageSize)
})

const categories = ref(newsStore.categories)

const loadSearchResults = async () => {
  loading.value = true
  try {
    if (filterCategory.value || filterSource.value || minViews.value || maxViews.value || filterTimeRange.value) {
      // Use advanced filter
      const params: any = {
        keyword: searchQuery.value,
        pageSize: 100,
      }
      if (filterCategory.value) params.categoryId = parseInt(filterCategory.value)
      if (filterSource.value) params.source = filterSource.value
      if (minViews.value) params.minViews = parseInt(minViews.value)
      if (maxViews.value) params.maxViews = parseInt(maxViews.value)
      if (filterTimeRange.value) {
        const now = new Date()
        if (filterTimeRange.value === 'today') {
          params.startTime = new Date(now.getFullYear(), now.getMonth(), now.getDate()).toISOString()
        } else if (filterTimeRange.value === 'week') {
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          params.startTime = weekAgo.toISOString()
        } else if (filterTimeRange.value === 'month') {
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
          params.startTime = monthAgo.toISOString()
        }
      }
      const response = await filterNewsApi(params)
      newsList.value = response.data || []
    } else {
      const response = await searchNewsApi(searchQuery.value, 1, 100)
      newsList.value = response.data || []
    }
    totalCount.value = newsList.value.length
    currentPage.value = 1
  } catch (error) {
    console.error('搜索失败:', error)
    newsList.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

const hotSearches = ['AI大模型', '芯片突破', '绿色能源', '元宇宙', '太空探索']

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    currentPage.value = 1
    router.push({ path: '/search', query: { keyword: searchQuery.value } })
  }
}

const handleHotSearch = (keyword: string) => {
  searchQuery.value = keyword
  handleSearch()
}

const goToNewsDetail = (id: number) => {
  router.push(`/news/${id}`)
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

const resetFilters = () => {
  filterCategory.value = ''
  filterTimeRange.value = ''
  filterSource.value = ''
  minViews.value = ''
  maxViews.value = ''
  currentPage.value = 1
}

watch([filterCategory, filterTimeRange], () => {
  loadSearchResults()
})

watch(() => route.query.keyword, (keyword) => {
  if (keyword) {
    searchQuery.value = keyword as string
    currentPage.value = 1
  }
})

onMounted(async () => {
  await newsStore.loadCategories()
  categories.value = newsStore.categories
  if (route.query.keyword) {
    searchQuery.value = route.query.keyword as string
  }
  loadSearchResults()
})

const getCategoryName = (categoryId: number): string => {
  const cat = newsStore.categories.find(c => c.id === categoryId)
  return cat?.name || '未分类'
}
</script>

<template>
  <div class="search-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="search-header">
          <div class="search-box-wrapper">
            <div class="search-box">
              <svg class="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="9" cy="9" r="6.5" stroke="#8e99a9" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="#8e99a9" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索新闻、话题、人物..."
                @keyup.enter="handleSearch"
              />
              <button class="search-btn" @click="handleSearch">搜索</button>
            </div>
            <button
              :class="['filter-btn', { active: isFilterOpen }]"
              @click="isFilterOpen = !isFilterOpen"
            >
              <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                <path d="M2 10h16" stroke="#5f6b7a" stroke-width="2" stroke-linecap="round"/>
                <path d="M7 5h6" stroke="#5f6b7a" stroke-width="2" stroke-linecap="round"/>
                <path d="M10 15h3" stroke="#5f6b7a" stroke-width="2" stroke-linecap="round"/>
              </svg>
              高级筛选
            </button>
          </div>

          <div v-if="searchQuery && !loading" class="search-stats">
            <span>找到 <strong>{{ totalCount }}</strong> 条相关结果</span>
          </div>
        </div>

        <div class="search-content">
          <Transition name="slide-left">
            <div v-if="isFilterOpen" class="filter-sidebar">
              <div class="filter-section">
                <h3 class="filter-title">新闻类别</h3>
                <select v-model="filterCategory" class="filter-select">
                  <option value="">全部类别</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>

              <div class="filter-section">
                <h3 class="filter-title">发布时间</h3>
                <select v-model="filterTimeRange" class="filter-select">
                  <option v-for="range in timeRanges" :key="range.value" :value="range.value">
                    {{ range.label }}
                  </option>
                </select>
              </div>

              <div class="filter-section">
                <h3 class="filter-title">来源媒体</h3>
                <input
                  v-model="filterSource"
                  type="text"
                  placeholder="输入媒体名称"
                  class="filter-input"
                />
              </div>

              <div class="filter-section">
                <h3 class="filter-title">阅读量区间</h3>
                <div class="views-range">
                  <input
                    v-model="minViews"
                    type="number"
                    placeholder="最小值"
                    class="filter-input"
                  />
                  <span class="range-separator">-</span>
                  <input
                    v-model="maxViews"
                    type="number"
                    placeholder="最大值"
                    class="filter-input"
                  />
                </div>
              </div>

              <div class="filter-actions">
                <button class="btn btn-outline" @click="resetFilters">重置</button>
                <button class="btn btn-primary" @click="currentPage = 1; isFilterOpen = false">应用筛选</button>
              </div>
            </div>
          </Transition>

          <div class="search-results">
            <template v-if="searchQuery">
              <div v-if="!loading && displayedNews.length > 0" class="results-list">
                <div
                  v-for="news in displayedNews"
                  :key="news.id"
                  class="result-item"
                  @click="goToNewsDetail(news.id)"
                >
                  <div class="result-content">
                    <div class="result-header">
                      <span class="tag tag-blue">{{ getCategoryName(news.categoryId) }}</span>
                      <template v-if="news.tags">
                        <span class="tag tag-line" v-for="tag in news.tags.slice(0, 2)" :key="tag.id">{{ tag.tagName }}</span>
                      </template>
                    </div>
                    <h3 class="result-title">{{ news.title }}</h3>
                    <p class="result-summary">{{ news.summary }}</p>
                    <div class="result-footer">
                      <span class="result-author">{{ news.author?.username || news.source || '未知' }}</span>
                      <span class="result-date">{{ formatDate(news.publishTime) }}</span>
                      <span class="result-views">👁️ {{ formatViews(news.viewCount) }}</span>
                      <span class="result-comments">💬 {{ news.commentCount }}</span>
                    </div>
                  </div>
                  <div class="result-image">
                    <img v-if="getCoverImageUrl(news.coverImage)" :src="getCoverImageUrl(news.coverImage)!" :alt="news.title" class="cover-img" />
                    <div v-else class="placeholder-img" style="background: linear-gradient(135deg, #e8f0fe, #1a73e8);">
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <path d="M21 15l-5-5L5 21"/>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="totalPages > 1" class="pagination">
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

              <div v-if="!loading && newsList.length === 0" class="empty-state">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="M21 21l-4.35-4.35"/>
                </svg>
                <p>没有找到相关结果</p>
                <p class="empty-hint">试试其他关键词吧</p>
              </div>
            </template>

            <div v-else class="empty-search">
              <h2 class="empty-title">热门搜索</h2>
              <div class="hot-searches">
                <div
                  v-for="(keyword, index) in hotSearches"
                  :key="keyword"
                  class="hot-search-item"
                  @click="handleHotSearch(keyword)"
                >
                  <span class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</span>
                  <span class="hot-keyword">{{ keyword }}</span>
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
.search-page {
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

.search-header {
  padding: 32px 0;
}

.search-box-wrapper {
  display: flex;
  gap: 12px;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  background: white;
  border: 2px solid #e1e8ef;
  border-radius: 50px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.search-icon {
  flex-shrink: 0;
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
  flex-shrink: 0;
}

.search-btn:hover {
  background: #1557b0;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  background: white;
  border: 2px solid #e1e8ef;
  border-radius: 50px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.filter-btn:hover {
  border-color: #1a73e8;
  color: #1a73e8;
}

.filter-btn.active {
  background: #1a73e8;
  border-color: #1a73e8;
  color: white;
}

.filter-btn.active svg path {
  stroke: white;
}

.search-stats {
  margin-top: 16px;
  font-size: 0.9rem;
  color: #5f6b7a;
}

.search-content {
  display: flex;
  gap: 24px;
}

.filter-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
}

.filter-section {
  margin-bottom: 24px;
}

.filter-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a2332;
  margin-bottom: 12px;
}

.filter-select {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #e1e8ef;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #5f6b7a;
  outline: none;
  transition: all 0.3s ease;
  font-family: inherit;
}

.filter-select:focus {
  border-color: #1a73e8;
}

.filter-input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #e1e8ef;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #5f6b7a;
  outline: none;
  transition: all 0.3s ease;
  font-family: inherit;
}

.filter-input:focus {
  border-color: #1a73e8;
}

.views-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.views-range .filter-input {
  flex: 1;
}

.range-separator {
  color: #8e99a9;
}

.filter-actions {
  display: flex;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #eef2f7;
}

.filter-actions .btn {
  flex: 1;
  padding: 10px;
  font-size: 0.85rem;
}

.search-results {
  flex: 1;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  display: flex;
  gap: 16px;
  background: white;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.result-item:hover {
  box-shadow: 0 8px 24px rgba(26, 115, 232, 0.12);
  transform: translateX(4px);
}

.result-content {
  flex: 1;
}

.result-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.result-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a2332;
  line-height: 1.4;
  margin-bottom: 6px;
}

.result-item:hover .result-title {
  color: #1a73e8;
}

.result-summary {
  font-size: 0.85rem;
  color: #5f6b7a;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 0.78rem;
  color: #8e99a9;
}

.result-author {
  font-weight: 600;
  color: #5f6b7a;
}

.result-image {
  width: 150px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-image .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.empty-hint {
  font-size: 0.85rem !important;
}

.empty-search {
  padding: 40px 0;
}

.empty-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a2332;
  margin-bottom: 24px;
}

.hot-searches {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hot-search-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.hot-search-item:hover {
  background: #e8f0fe;
}

.hot-rank {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 700;
  background: #f5f8fc;
  color: #8e99a9;
}

.hot-rank.rank-1 {
  background: #ff3b30;
  color: white;
}

.hot-rank.rank-2 {
  background: #ff9500;
  color: white;
}

.hot-rank.rank-3 {
  background: #ffcc00;
  color: white;
}

.hot-keyword {
  font-size: 0.9rem;
  color: #1a2332;
  font-weight: 500;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from,
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 900px) {
  .search-box-wrapper {
    flex-direction: column;
  }

  .search-content {
    flex-direction: column;
  }

  .filter-sidebar {
    width: 100%;
  }

  .result-image {
    display: none;
  }

  .hot-searches {
    flex-direction: column;
  }
}
</style>