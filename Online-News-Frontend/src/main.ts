import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

// 监听 auth:logout 事件（由 axios 响应拦截器触发），跳转到首页
window.addEventListener('auth:logout', () => {
  router.push('/')
})
