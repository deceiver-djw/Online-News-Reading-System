<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  remember: false,
})

const errors = ref<Record<string, string>>({})

const isLoading = computed(() => userStore.isLoading)

const validateForm = () => {
  errors.value = {}
  
  if (!form.value.username.trim()) {
    errors.value.username = '请输入用户名'
  }
  
  if (!form.value.password.trim()) {
    errors.value.password = '请输入密码'
  } else if (form.value.password.length < 6) {
    errors.value.password = '密码长度至少6位'
  }
  
  return Object.keys(errors.value).length === 0
}

const handleLogin = async () => {
  if (!validateForm()) return
  
  try {
    await userStore.login({
      username: form.value.username,
      password: form.value.password,
    })
    
    router.push('/')
  } catch (error: any) {
    errors.value.global = error.message || '登录失败，请检查用户名和密码'
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M3 3h18v18H3V3zm2 2v14h14V5H5z" fill="#1a73e8"/>
              <path d="M8 16h8v-2H8v2zm0-4h8v-2H8v2zm0-4h5V7H8v2z" fill="white"/>
            </svg>
          </div>
          <h1 class="login-title">欢迎回来</h1>
          <p class="login-subtitle">登录您的账号，开始阅读之旅</p>
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div v-if="errors.global" class="error-message">{{ errors.global }}</div>
          
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              :class="['form-input', { error: errors.username }]"
            />
            <span v-if="errors.username" class="form-error">{{ errors.username }}</span>
          </div>
          
          <div class="form-group">
            <label class="form-label">密码</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :class="['form-input', { error: errors.password }]"
            />
            <span v-if="errors.password" class="form-error">{{ errors.password }}</span>
          </div>
          
          <div class="form-group form-checkbox">
            <label class="checkbox-label">
              <input v-model="form.remember" type="checkbox" />
              <span class="checkbox-custom"></span>
              记住我
            </label>
          </div>
          
          <button
            type="submit"
            class="btn btn-primary btn-block"
            :disabled="isLoading"
          >
            <span v-if="isLoading" class="loading-spinner"></span>
            {{ isLoading ? '登录中...' : '登 录' }}
          </button>
        </form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <button class="link-btn" @click="goToRegister">立即注册</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f0fe 0%, #f5f8fc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 12px 48px rgba(26, 115, 232, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin-bottom: 16px;
}

.login-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1a2332;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 0.95rem;
  color: #8e99a9;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.error-message {
  padding: 12px 16px;
  background: #ffebef;
  border-radius: 8px;
  font-size: 0.85rem;
  color: #ff3b30;
  text-align: center;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.form-input {
  padding: 14px 16px;
  border: 2px solid #e1e8ef;
  border-radius: 12px;
  font-size: 0.95rem;
  color: #1a2332;
  outline: none;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus {
  border-color: #1a73e8;
  box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
}

.form-input.error {
  border-color: #ff3b30;
}

.form-input::placeholder {
  color: #8e99a9;
}

.form-error {
  font-size: 0.78rem;
  color: #ff3b30;
}

.form-checkbox {
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #5f6b7a;
  cursor: pointer;
}

.checkbox-label input {
  display: none;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #e1e8ef;
  border-radius: 5px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-label input:checked + .checkbox-custom {
  background: #1a73e8;
  border-color: #1a73e8;
}

.checkbox-label input:checked + .checkbox-custom::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.btn-block {
  width: 100%;
  padding: 14px;
  font-size: 1rem;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 32px;
  font-size: 0.9rem;
  color: #5f6b7a;
}

.link-btn {
  background: none;
  border: none;
  color: #1a73e8;
  font-weight: 600;
  cursor: pointer;
  font-size: inherit;
  transition: all 0.3s ease;
}

.link-btn:hover {
  text-decoration: underline;
}
</style>
