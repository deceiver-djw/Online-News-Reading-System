import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Favorite, NewsItem } from '@/api/types'
import { addCollection, removeCollection, getMyCollections, checkCollection } from '@/api/news'

export const useFavoriteStore = defineStore('favorite', () => {
  const favorites = ref<Favorite[]>([])

  const loadFavorites = async (userId: number) => {
    try {
      const response = await getMyCollections(userId)
      favorites.value = response.data || []
    } catch (error) {
      console.error('加载收藏失败:', error)
    }
  }

  const isFavorited = async (userId: number, newsId: number): Promise<boolean> => {
    try {
      const response = await checkCollection(userId, newsId)
      return response.data
    } catch (error) {
      console.error('检查收藏状态失败:', error)
      return false
    }
  }

  const toggleFavorite = async (userId: number, newsId: number) => {
    try {
      const isFav = (await checkCollection(userId, newsId)).data
      if (isFav) {
        await removeCollection(userId, newsId)
        const index = favorites.value.findIndex(f => f.newsId === newsId)
        if (index !== -1) favorites.value.splice(index, 1)
        return false
      } else {
        await addCollection(userId, newsId)
        await loadFavorites(userId)
        return true
      }
    } catch (error) {
      console.error('收藏操作失败:', error)
      return false
    }
  }

  const favoriteList = computed(() => favorites.value)

  const removeFavorite = async (userId: number, newsId: number) => {
    try {
      await removeCollection(userId, newsId)
      const index = favorites.value.findIndex(f => f.newsId === newsId)
      if (index !== -1) {
        favorites.value.splice(index, 1)
        return true
      }
      return false
    } catch (error) {
      console.error('取消收藏失败:', error)
      return false
    }
  }

  const clearAll = () => {
    favorites.value = []
  }

  return {
    favorites,
    loadFavorites,
    isFavorited,
    toggleFavorite,
    favoriteList,
    removeFavorite,
    clearAll,
  }
})