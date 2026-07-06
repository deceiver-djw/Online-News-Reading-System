<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false,
})

const errors = ref<Record<string, string>>({})

const isLoading = computed(() => userStore.isLoading)

const validateForm = () => {
  errors.value = {}
  
  if (!form.value.username.trim()) {
    errors.value.username = '请输入用户名'
  } else if (form.value.username.length < 3) {
    errors.value.username = '用户名至少3个字符'
  }
  
  if (!form.value.email.trim()) {
    errors.value.email = '请输入邮箱'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = '请输入有效的邮箱地址'
  }
  
  if (!form.value.password.trim()) {
    errors.value.password = '请输入密码'
  } else if (form.value.password.length < 6) {
    errors.value.password = '密码长度至少6位'
  }
  
  if (!form.value.confirmPassword.trim()) {
    errors.value.confirmPassword = '请确认密码'
  } else if (form.value.confirmPassword !== form.value.password) {
    errors.value.confirmPassword = '两次输入的密码不一致'
  }
  
  if (!form.value.agreeTerms) {
    errors.value.agreeTerms = '请同意服务条款'
  }
  
  return Object.keys(errors.value).length === 0
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  try {
    await userStore.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password,
    })
    
    router.push('/login')
  } catch (error: any) {
    errors.value.global = error.message || '注册失败，请稍后重试'
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="logo">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M3 3h18v18H3V3zm2 2v14h14V5H5z" fill="#1a73e8"/>
              <path d="M8 16h8v-2H8v2zm0-4h8v-2H8v2zm0-4h5V7H8v2z" fill="white"/>
            </svg>
          </div>
          <h1 class="register-title">创建账号</h1>
          <p class="register-subtitle">注册后即可享受完整的新闻阅读体验</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="register-form">
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
            <label class="form-label">邮箱</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
              :class="['form-input', { error: errors.email }]"
            />
            <span v-if="errors.email" class="form-error">{{ errors.email }}</span>
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
          
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :class="['form-input', { error: errors.confirmPassword }]"
            />
            <span v-if="errors.confirmPassword" class="form-error">{{ errors.confirmPassword }}</span>
          </div>
          
          <div class="form-group form-checkbox">
            <label class="checkbox-label">
              <input v-model="form.agreeTerms" type="checkbox" />
              <span class="checkbox-custom"></span>
              我已阅读并同意
              <a href="#" class="terms-link">服务条款</a>
              和
              <a href="#" class="terms-link">隐私政策</a>
            </label>
            <span v-if="errors.agreeTerms" class="form-error">{{ errors.agreeTerms }}</span>
          </div>
          
          <button
            type="submit"
            class="btn btn-primary btn-block"
            :disabled="isLoading"
          >
            <span v-if="isLoading" class="loading-spinner"></span>
            {{ isLoading ? '注册中...' : '注 册' }}
          </button>
        </form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <button class="link-btn" @click="goToLogin">立即登录</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f0fe 0%, #f5f8fc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.register-container {
  width: 100%;
  max-width: 460px;
}

.register-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 12px 48px rgba(26, 115, 232, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin-bottom: 16px;
}

.register-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: #1a2332;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 0.95rem;
  color: #8e99a9;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  gap: 6px;
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
  align-items: flex-start;
  justify-content: flex-start;
  gap: 8px;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.82rem;
  color: #5f6b7a;
  cursor: pointer;
  line-height: 1.5;
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
  flex-shrink: 0;
  margin-top: 2px;
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

.terms-link {
  color: #1a73e8;
  text-decoration: none;
}

.terms-link:hover {
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 14px;
  font-size: 1rem;
  margin-top: 8px;
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

.register-footer {
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
