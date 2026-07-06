import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest } from '@/api/types'
import { login as loginApi, register as registerApi, logout as logoutApi, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const isLoading = ref(false)

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const canPublish = computed(() => isLoggedIn.value)

  const login = async (data: LoginRequest) => {
    isLoading.value = true
    try {
      const response = await loginApi(data.username, data.password)
      // 后端 data 直接就是 user 对象，没有外层的 { token, user } 包装
      const userData = response.data as unknown as User
      user.value = userData
      // 后端不返回 token，用 userId 作为标识
      const pseudoToken = 'authenticated-' + userData.id
      token.value = pseudoToken
      localStorage.setItem('token', pseudoToken)
      localStorage.setItem('user', JSON.stringify(userData))
      return { token: pseudoToken, user: userData }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const register = async (data: RegisterRequest) => {
    isLoading.value = true
    try {
      const response = await registerApi(data)
      user.value = response.data
      return { token: '', user: response.data }
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  const loadUserFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    if (storedToken) {
      token.value = storedToken
    }
    if (storedUser) {
      try {
        user.value = JSON.parse(storedUser)
      } catch {
        localStorage.removeItem('user')
        localStorage.removeItem('token')
      }
    }
  }

  return {
    token,
    user,
    isLoading,
    isLoggedIn,
    isAdmin,
    canPublish,
    login,
    register,
    logout,
    loadUserFromStorage,
  }
})