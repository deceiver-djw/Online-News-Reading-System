import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { NewsItem } from '@/api/types'
import { getMyReadHistory, addReadHistory, deleteReadHistory, clearMyReadHistory } from '@/api/news'

export const useHistoryStore = defineStore('history', () => {
  const history = ref<any[]>([])

  const loadHistory = async (userId: number) => {
    try {
      const response = await getMyReadHistory(userId)
      history.value = response.data || []
    } catch (error) {
      console.error('加载阅读历史失败:', error)
    }
  }

  const addHistory = async (userId: number, newsId: number) => {
    try {
      await addReadHistory(userId, newsId)
      await loadHistory(userId)
    } catch (error) {
      console.error('记录阅读历史失败:', error)
    }
  }

  const historyList = computed(() => history.value)

  const removeHistory = async (userId: number, id: number) => {
    try {
      await deleteReadHistory(id)
      const index = history.value.findIndex((h: any) => h.id === id)
      if (index !== -1) {
        history.value.splice(index, 1)
        return true
      }
      return false
    } catch (error) {
      console.error('删除阅读记录失败:', error)
      return false
    }
  }

  const clearAll = async (userId: number) => {
    try {
      await clearMyReadHistory(userId)
      history.value = []
    } catch (error) {
      console.error('清除阅读历史失败:', error)
    }
  }

  return {
    history,
    loadHistory,
    addHistory,
    historyList,
    removeHistory,
    clearAll,
  }
})