<script setup lang="ts">
import { useRouter } from 'vue-router'
import NavBar from '@/components/news/NavBar.vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="not-found-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="error-content">
          <div class="error-icon">
            <svg width="120" height="120" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#e8f0fe"/>
              <path d="M2 17l10 5 10-5" stroke="#1a73e8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12l10 5 10-5" stroke="#1a73e8" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="error-title">404</h1>
          <p class="error-subtitle">页面未找到</p>
          <p class="error-message">抱歉，您访问的页面不存在或已被删除</p>
          <button class="btn btn-primary" @click="goHome">返回首页</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.not-found-page {
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

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.error-icon {
  margin-bottom: 24px;
}

.error-title {
  font-size: 4rem;
  font-weight: 800;
  color: #1a73e8;
  margin-bottom: 16px;
}

.error-subtitle {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a2332;
  margin-bottom: 12px;
}

.error-message {
  font-size: 0.95rem;
  color: #8e99a9;
  margin-bottom: 32px;
}
</style>
