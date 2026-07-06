import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMySubscriptions, subscribe as subscribeApi, unsubscribe as unsubscribeApi, checkSubscription, updatePushSetting } from '@/api/news'

export const useSubscriptionStore = defineStore('subscription', () => {
  const subscriptions = ref<any[]>([])
  const notificationsEnabled = ref(true)

  const loadSubscriptions = async (userId: number) => {
    try {
      const response = await getMySubscriptions(userId)
      subscriptions.value = response.data || []
    } catch (error) {
      console.error('加载订阅失败:', error)
    }
  }

  const isSubscribed = async (userId: number, categoryId: number): Promise<boolean> => {
    try {
      const response = await checkSubscription(userId, categoryId)
      return response.data
    } catch (error) {
      console.error('检查订阅状态失败:', error)
      return false
    }
  }

  const toggleSubscription = async (userId: number, categoryId: number) => {
    try {
      const subbed = (await checkSubscription(userId, categoryId)).data
      if (subbed) {
        await unsubscribeApi(userId, categoryId)
        await loadSubscriptions(userId)
        return false
      } else {
        await subscribeApi(userId, categoryId)
        await loadSubscriptions(userId)
        return true
      }
    } catch (error) {
      console.error('订阅操作失败:', error)
      return false
    }
  }

  const subscribedCategories = computed(() => {
    return subscriptions.value.map((s: any) => s.categoryId || s.category)
  })

  const setNotificationsEnabled = async (id: number, enabled: boolean) => {
    try {
      await updatePushSetting(id, enabled)
      notificationsEnabled.value = enabled
    } catch (error) {
      console.error('更新推送设置失败:', error)
    }
  }

  const clearAll = () => {
    subscriptions.value = []
  }

  return {
    subscriptions,
    notificationsEnabled,
    loadSubscriptions,
    isSubscribed,
    toggleSubscription,
    subscribedCategories,
    setNotificationsEnabled,
    clearAll,
  }
})