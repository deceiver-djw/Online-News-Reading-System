<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useSubscriptionStore } from '@/stores/subscription'
import { useUserStore } from '@/stores/user'
import { useNewsStore } from '@/stores/news'
import NavBar from '@/components/news/NavBar.vue'

const subscriptionStore = useSubscriptionStore()
const userStore = useUserStore()
const newsStore = useNewsStore()

const enablePush = ref(false)

const categories = computed(() => newsStore.categories.map(c => ({
  id: c.id,
  name: c.name,
  icon: c.icon || '📰',
  color: '#1a73e8',
})))

const toggleSubscription = async (categoryId: number) => {
  if (!userStore.user?.id) return
  await subscriptionStore.toggleSubscription(userStore.user.id, categoryId)
}

const subscribeAll = async () => {
  if (!userStore.user?.id) return
  for (const cat of categories.value) {
    const subbed = await subscriptionStore.isSubscribed(userStore.user.id, cat.id)
    if (!subbed) {
      await subscriptionStore.toggleSubscription(userStore.user.id, cat.id)
    }
  }
}

const unsubscribeAll = async () => {
  if (!userStore.user?.id) return
  for (const cat of categories.value) {
    const subbed = await subscriptionStore.isSubscribed(userStore.user.id, cat.id)
    if (subbed) {
      await subscriptionStore.toggleSubscription(userStore.user.id, cat.id)
    }
  }
}

const checkNotificationPermission = () => {
  if ('Notification' in window) {
    enablePush.value = Notification.permission === 'granted'
  }
}

const requestNotificationPermission = () => {
  if ('Notification' in window) {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        enablePush.value = true
        new Notification('订阅通知', {
          body: '您已成功开启推送通知',
        })
      }
    })
  }
}

onMounted(async () => {
  await newsStore.loadCategories()
  if (userStore.user?.id) {
    await subscriptionStore.loadSubscriptions(userStore.user.id)
  }
  checkNotificationPermission()
})
</script>

<template>
  <div class="subscriptions-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">订阅管理</h1>
          <p class="page-subtitle">管理您感兴趣的新闻类别</p>
        </div>

        <div class="push-settings">
          <div class="push-card">
            <div class="push-info">
              <div class="push-icon">🔔</div>
              <div class="push-text">
                <h3>推送通知</h3>
                <p>订阅类别有新新闻时，接收浏览器通知</p>
              </div>
            </div>
            <button
              :class="['btn', enablePush ? 'btn-primary' : 'btn-outline']"
              @click="requestNotificationPermission"
              :disabled="enablePush"
            >
              {{ enablePush ? '已开启' : '开启推送' }}
            </button>
          </div>
        </div>

        <div class="category-grid">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-card"
            :class="{ subscribed: subscriptionStore.subscribedCategories.includes(cat.id) }"
            @click="toggleSubscription(cat.id)"
          >
            <div class="category-icon" :style="{ background: cat.color + '15', color: cat.color }">
              <span>{{ cat.icon }}</span>
            </div>
            <div class="category-info">
              <h3 class="category-name">{{ cat.name }}</h3>
              <p class="category-desc">{{ cat.name }}新闻资讯</p>
            </div>
            <div class="category-toggle">
              <div :class="['toggle', { active: subscriptionStore.subscribedCategories.includes(cat.id) }]">
                <div class="toggle-thumb"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn btn-outline" @click="unsubscribeAll">取消全部订阅</button>
          <button class="btn btn-primary" @click="subscribeAll">订阅全部类别</button>
        </div>

        <div class="tips-card">
          <div class="tips-icon">💡</div>
          <div class="tips-content">
            <h3>订阅提示</h3>
            <ul>
              <li>订阅后，该类别有新新闻发布时会在导航栏显示红点提醒</li>
              <li>您可以随时取消订阅，不会影响已阅读的内容</li>
              <li>浏览器推送通知需要您的授权才能生效</li>
            </ul>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.subscriptions-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.main-content {
  padding-top: 72px;
}

.container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  padding: 32px 0;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1a2332;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e99a9;
}

.push-settings {
  margin-bottom: 24px;
}

.push-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
}

.push-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.push-icon {
  font-size: 2rem;
}

.push-text h3 {
  font-size: 1rem;
  font-weight: 700;
  color: #1a2332;
  margin-bottom: 4px;
}

.push-text p {
  font-size: 0.85rem;
  color: #8e99a9;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.category-card:hover {
  transform: translateY(-2px);
}

.category-card.subscribed {
  border-color: #1a73e8;
  background: #fafcff;
}

.category-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 1.8rem;
  margin-bottom: 12px;
}

.category-card.subscribed .category-icon {
  background: #1a73e8;
  color: white;
}

.category-info {
  text-align: center;
  margin-bottom: 16px;
}

.category-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1a2332;
  margin-bottom: 4px;
}

.category-desc {
  font-size: 0.78rem;
  color: #8e99a9;
}

.category-toggle {
  display: flex;
  justify-content: center;
}

.toggle {
  width: 48px;
  height: 28px;
  background: #e1e8ef;
  border-radius: 14px;
  position: relative;
  transition: all 0.3s ease;
}

.toggle.active {
  background: #1a73e8;
}

.toggle-thumb {
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle.active .toggle-thumb {
  left: 22px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-buttons .btn {
  flex: 1;
}

.tips-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff7e6;
  border-radius: 16px;
  border: 1px solid #ffe0b2;
}

.tips-icon {
  font-size: 1.8rem;
  flex-shrink: 0;
}

.tips-content h3 {
  font-size: 0.95rem;
  font-weight: 700;
  color: #e65100;
  margin-bottom: 8px;
}

.tips-content ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-content li {
  font-size: 0.85rem;
  color: #ef6c00;
  margin-bottom: 4px;
  padding-left: 16px;
  position: relative;
}

.tips-content li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #ff9500;
}

@media (max-width: 600px) {
  .category-grid {
    grid-template-columns: 1fr;
  }

  .push-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .push-info {
    flex-direction: column;
  }
}
</style>