import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { NewsItem, Category } from '@/api/types'
import {
  getNewsList as fetchNewsListApi,
  getNewsDetail as getNewsDetailApi,
  getHotNews as getHotNewsApi,
  getHeadlineNews as getHeadlineNewsApi,
  getCategoryList as getCategoryListApi,
  likeNews,
  unlikeNews,
  checkLike,
  searchNews as searchNewsApi,
  guessYouLike,
} from '@/api/news'
import { addReadHistory } from '@/api/news'

export const useNewsStore = defineStore('news', () => {
  const newsList = ref<NewsItem[]>([])
  const currentNews = ref<NewsItem | null>(null)
  const headlineNews = ref<NewsItem[]>([])
  const hotNewsList = ref<NewsItem[]>([])
  const recommendedNewsList = ref<NewsItem[]>([])
  const categories = ref<Category[]>([])
  const loading = ref(false)

  const loadNewsList = async (categoryId?: number, pageNum = 1, pageSize = 10) => {
    loading.value = true
    try {
      const response = await fetchNewsListApi(categoryId, pageNum, pageSize)
      newsList.value = response.data
      return response.data
    } catch (error) {
      console.error('加载新闻列表失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }

  const getNewsList = async (categoryId?: number, pageNum = 1, pageSize = 10): Promise<NewsItem[]> => {
    return loadNewsList(categoryId, pageNum, pageSize)
  }

  const getNewsById = async (id: number): Promise<NewsItem | null> => {
    try {
      const response = await getNewsDetailApi(id)
      currentNews.value = response.data
      return response.data
    } catch (error) {
      console.error('获取新闻详情失败:', error)
      return null
    }
  }

  const getNewsByIdSync = (id: number): NewsItem | undefined => {
    return newsList.value.find(n => n.id === id)
  }

  const setCurrentNews = (news: NewsItem) => {
    currentNews.value = news
  }

  const loadHeadlineNews = async () => {
    try {
      const response = await getHeadlineNewsApi()
      headlineNews.value = response.data
    } catch (error) {
      console.error('加载头条新闻失败:', error)
    }
  }

  const loadHotNews = async (limit = 10) => {
    try {
      const response = await getHotNewsApi(limit)
      hotNewsList.value = response.data
    } catch (error) {
      console.error('加载热点新闻失败:', error)
    }
  }

  const loadRecommendedNews = async (userId?: number) => {
    try {
      if (userId) {
        const response = await guessYouLike(userId, 6)
        recommendedNewsList.value = response.data
      }
    } catch (error) {
      console.error('加载推荐新闻失败:', error)
    }
  }

  const loadCategories = async () => {
    try {
      const response = await getCategoryListApi()
      categories.value = response.data
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  const incrementViews = async (id: number) => {
    // Backend auto-increments view count on detail fetch
    try {
      await getNewsDetailApi(id)
    } catch (error) {
      console.error('更新阅读量失败:', error)
    }
  }

  const toggleLike = async (newsId: number, userId: number): Promise<boolean> => {
    try {
      // Check current like status
      const checkResponse = await checkLike(userId, 'news', newsId)
      if (checkResponse.data) {
        await unlikeNews(userId, newsId)
        return false
      } else {
        await likeNews(userId, newsId)
        return true
      }
    } catch (error) {
      console.error('点赞操作失败:', error)
      return false
    }
  }

  const hotNews = computed(() => {
    return hotNewsList.value.slice(0, 10)
  })

  const latestNews = computed(() => {
    return [...newsList.value].slice(0, 10)
  })

  const recommendedNews = computed(() => {
    return recommendedNewsList.value.slice(0, 6)
  })

  const categoriesList = computed(() => {
    return categories.value
  })

  // 记录阅读历史
  const recordReadHistory = async (userId: number, newsId: number) => {
    try {
      await addReadHistory(userId, newsId)
    } catch (error) {
      console.error('记录阅读历史失败:', error)
    }
  }

  return {
    newsList,
    currentNews,
    headlineNews,
    hotNewsList,
    recommendedNewsList,
    categories,
    loading,
    getNewsList,
    getNewsById,
    getNewsByIdSync,
    setCurrentNews,
    loadHeadlineNews,
    loadHotNews,
    loadRecommendedNews,
    loadCategories,
    incrementViews,
    toggleLike,
    hotNews,
    latestNews,
    recommendedNews,
    categoriesList,
    loadNewsList,
    recordReadHistory,
  }
})