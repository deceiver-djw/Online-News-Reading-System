<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSubscriptionStore } from '@/stores/subscription'
import { getCoverImageUrl } from '@/utils/image'

const router = useRouter()
const userStore = useUserStore()
const subscriptionStore = useSubscriptionStore()

const isMenuOpen = ref(false)
const isScrolled = ref(false)
const isUserMenuOpen = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  userStore.loadUserFromStorage()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const navItems: { name: string; path: string; icon: string }[] = [
  { name: '首页', path: '/', icon: '🏠' },
  { name: '热点', path: '/news/category/全部', icon: '🔥' },
  { name: '科技', path: '/news/category/科技', icon: '💻' },
  { name: '财经', path: '/news/category/财经', icon: '📈' },
  { name: '体育', path: '/news/category/体育', icon: '⚽' },
  { name: '娱乐', path: '/news/category/娱乐', icon: '🎭' },
]

const userMenuItems = computed(() => {
  const items: { name: string; path?: string; action?: string; icon: string }[] = [
    { name: '个人中心', path: '/profile', icon: '👤' },
    { name: '订阅管理', path: '/subscriptions', icon: '🔔' },
  ]
  if (userStore.canPublish) {
    items.push({ name: '发布新闻', path: '/publish', icon: '✏️' })
    items.push({ name: '稿件管理', path: '/editor', icon: '📝' })
  }
  items.push({ name: '退出登录', action: 'logout', icon: '🚪' })
  return items
})

const handleLogout = () => {
  userStore.logout()
  router.push('/')
  isUserMenuOpen.value = false
}

const handleUserMenuClick = (item: { name: string; path?: string; action?: string; icon: string }) => {
  if (item.action === 'logout') {
    handleLogout()
  } else if (item.path) {
    router.push(item.path)
    isUserMenuOpen.value = false
  }
}

const hasUnreadSubscriptions = computed(() => {
  return subscriptionStore.subscriptions.length > 0
})
</script>

<template>
  <header :class="['navbar', { 'navbar-scrolled': isScrolled }]">
    <div class="navbar-inner container">
      <div class="navbar-brand" @click="router.push('/')">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
            <rect width="32" height="32" rx="8" fill="url(#brand-gradient)"/>
            <path d="M8 10h12v2H8zm0 5h16v2H8zm0 5h10v2H8z" fill="white"/>
            <defs>
              <linearGradient id="brand-gradient" x1="0" y1="0" x2="32" y2="32">
                <stop stop-color="#1a73e8"/>
                <stop offset="1" stop-color="#4a90d9"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span class="brand-text">News<span class="brand-highlight">Hub</span></span>
      </div>

      <nav class="navbar-nav">
        <a
          v-for="item in navItems"
          :key="item.path"
          :class="['nav-item', { active: router.currentRoute.value.path === item.path }]"
          @click="router.push(item.path)"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.name }}</span>
        </a>
      </nav>

      <div class="navbar-actions">
        <Transition name="fade">
          <button
            v-if="hasUnreadSubscriptions"
            class="notification-btn"
            @click="router.push('/subscriptions')"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" stroke="#5f6b7a" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="13" r="3" stroke="#5f6b7a" stroke-width="1.5"/>
            </svg>
            <span class="notification-dot"></span>
          </button>
        </Transition>
        
        <div v-if="userStore.isLoggedIn" class="user-menu-wrapper">
          <button class="user-menu-btn" @click="isUserMenuOpen = !isUserMenuOpen">
            <img
              v-if="getCoverImageUrl(userStore.user?.avatar)"
              :src="getCoverImageUrl(userStore.user!.avatar)!"
              :alt="userStore.user?.username"
              class="user-avatar"
              @error="($event.target as HTMLImageElement).style.display='none'"
            />
            <svg v-else width="32" height="32" viewBox="0 0 24 24" class="user-avatar-fallback">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" fill="#1a73e8"/>
              <circle cx="12" cy="7" r="4" fill="white"/>
            </svg>
            <span class="user-name">{{ userStore.user?.username }}</span>
          </button>
          <Transition name="slide-down">
            <div v-if="isUserMenuOpen" class="user-dropdown">
              <a
                v-for="item in userMenuItems"
                :key="item.name"
                class="dropdown-item"
                @click="handleUserMenuClick(item)"
              >
                <span class="dropdown-icon">{{ item.icon }}</span>
                <span class="dropdown-text">{{ item.name }}</span>
              </a>
            </div>
          </Transition>
        </div>
        
        <template v-else>
          <button class="btn btn-outline btn-sm" @click="router.push('/login')">登录</button>
          <button class="btn btn-primary btn-sm" @click="router.push('/register')">注册</button>
        </template>
        
        <button class="menu-toggle" @click="isMenuOpen = !isMenuOpen">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <Transition name="slide">
      <div v-if="isMenuOpen" class="mobile-menu">
        <a
          v-for="item in navItems"
          :key="item.path"
          :class="['mobile-nav-item', { active: router.currentRoute.value.path === item.path }]"
          @click="router.push(item.path); isMenuOpen = false"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          {{ item.name }}
        </a>
        <div class="mobile-auth" v-if="!userStore.isLoggedIn">
          <button class="btn btn-outline btn-sm" @click="router.push('/login'); isMenuOpen = false">登录</button>
          <button class="btn btn-primary btn-sm" @click="router.push('/register'); isMenuOpen = false">注册</button>
        </div>
        <div v-else class="mobile-user-menu">
          <div class="mobile-user-info">
            <img
              v-if="getCoverImageUrl(userStore.user?.avatar)"
              :src="getCoverImageUrl(userStore.user!.avatar)!"
              :alt="userStore.user?.username"
              class="mobile-user-avatar"
              @error="($event.target as HTMLImageElement).style.display='none'"
            />
            <svg v-else width="40" height="40" viewBox="0 0 24 24" class="mobile-user-avatar-fallback">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" fill="#1a73e8"/>
              <circle cx="12" cy="7" r="4" fill="white"/>
            </svg>
            <span class="mobile-user-name">{{ userStore.user?.username }}</span>
          </div>
          <a
            v-for="item in userMenuItems"
            :key="item.name"
            class="mobile-dropdown-item"
            @click="handleUserMenuClick(item); isMenuOpen = false"
          >
            <span class="dropdown-icon">{{ item.icon }}</span>
            <span class="dropdown-text">{{ item.name }}</span>
          </a>
        </div>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.navbar-scrolled {
  border-bottom-color: #e1e8ef;
  box-shadow: 0 2px 20px rgba(26, 115, 232, 0.08);
}

.navbar-inner {
  display: flex;
  align-items: center;
  height: 72px;
  gap: 32px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-text {
  font-size: 1.4rem;
  font-weight: 800;
  color: #1a2332;
  letter-spacing: -0.5px;
}

.brand-highlight {
  color: #1a73e8;
}

.navbar-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-item:hover {
  color: #1a73e8;
  background: #e8f0fe;
}

.nav-item.active {
  color: #1a73e8;
  background: #e8f0fe;
  font-weight: 600;
}

.nav-icon {
  font-size: 1rem;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.notification-btn {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #ff3b30;
  border-radius: 50%;
}

.user-menu-wrapper {
  position: relative;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f5f8fc;
  border: 1px solid #e1e8ef;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-menu-btn:hover {
  background: #e8f0fe;
  border-color: #1a73e8;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1a2332;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  min-width: 180px;
  overflow: hidden;
  z-index: 1001;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  font-size: 0.85rem;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dropdown-item:hover {
  background: #f5f8fc;
  color: #1a73e8;
}

.dropdown-icon {
  font-size: 0.9rem;
}

.menu-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
}

.menu-toggle span {
  display: block;
  width: 24px;
  height: 2px;
  background: #1a2332;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.mobile-menu {
  position: absolute;
  top: 72px;
  left: 0;
  right: 0;
  background: white;
  padding: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  color: #1a73e8;
  background: #e8f0fe;
}

.mobile-auth {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  margin-top: 8px;
  border-top: 1px solid #e1e8ef;
}

.mobile-user-menu {
  padding: 12px 16px;
  margin-top: 8px;
  border-top: 1px solid #e1e8ef;
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  margin-bottom: 8px;
  border-bottom: 1px solid #eef2f7;
}

.mobile-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.mobile-user-name {
  font-weight: 600;
  color: #1a2332;
}

.mobile-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  font-size: 0.9rem;
  color: #5f6b7a;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mobile-dropdown-item:hover {
  color: #1a73e8;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 900px) {
  .navbar-nav {
    display: none;
  }

  .menu-toggle {
    display: flex;
  }

  .navbar-actions .btn-outline,
  .navbar-actions .btn-primary {
    padding: 6px 12px;
    font-size: 0.8rem;
  }

  .user-menu-wrapper {
    display: none;
  }
}
</style>
