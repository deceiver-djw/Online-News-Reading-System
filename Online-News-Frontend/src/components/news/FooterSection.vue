<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = new Date().getFullYear()

const handleNav = (path: string) => {
  router.push(path).catch(() => {})
}
</script>

<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-grid">
        <div class="footer-col">
          <div class="footer-brand" @click="handleNav('/')" style="cursor:pointer">
            <svg viewBox="0 0 32 32" width="24" height="24" fill="none">
              <rect width="32" height="32" rx="8" fill="url(#brand-gradient)"/>
              <path d="M8 10h12v2H8zm0 5h16v2H8zm0 5h10v2H8z" fill="white"/>
              <defs>
                <linearGradient id="brand-gradient" x1="0" y1="0" x2="32" y2="32">
                  <stop stop-color="#1a73e8"/>
                  <stop offset="1" stop-color="#4a90d9"/>
                </linearGradient>
              </defs>
            </svg>
            <span class="footer-brand-text">NewsHub</span>
          </div>
          <p class="footer-desc">
            NewsHub 致力于为用户提供最快速、最准确、最深度的新闻报道。我们汇聚全球资讯，为您呈现精彩世界。
          </p>
          <div class="footer-social">
            <span class="social-link" title="微信">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M8.5 2C4.91 2 2 4.46 2 7.5c0 1.69.87 3.21 2.23 4.19L3 14l2.77-1.38c.76.25 1.58.38 2.23.38M8.5 12c3.59 0 6.5-2.46 6.5-5.5S12.09 1 8.5 1 2 3.46 2 6.5s.87 3.21 2.23 4.19L3 13l2.77-1.38c.76.25 1.58.38 2.23.38z"/>
                <path d="M17.5 10c-2.21 0-4 1.57-4 3.5s1.79 3.5 4 3.5c.56 0 1.1-.1 1.6-.27L21 18l-.95-1.85C21.15 15.42 22 14.05 22 12.5c0-1.93-1.79-3.5-4-3.5z"/>
              </svg>
            </span>
            <span class="social-link" title="微博">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                <path d="M8 12c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2zM14 12c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2z"/>
              </svg>
            </span>
            <span class="social-link" title="Twitter">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/>
              </svg>
            </span>
            <span class="social-link" title="RSS">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <circle cx="6.18" cy="17.82" r="2.18"/>
                <path d="M4 4.44v2.83c7.03 0 12.73 5.7 12.73 12.73h2.83c0-8.59-6.97-15.56-15.56-15.56zm0 5.66v2.83c3.9 0 7.07 3.17 7.07 7.07h2.83c0-5.47-4.43-9.9-9.9-9.9z"/>
              </svg>
            </span>
          </div>
        </div>

        <div class="footer-col">
          <h4 class="footer-col-title">快速链接</h4>
          <ul class="footer-links">
            <li><router-link to="/">最新新闻</router-link></li>
            <li><router-link to="/news/category/全部">热门话题</router-link></li>
            <li><router-link to="/search">新闻专题</router-link></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4 class="footer-col-title">新闻分类</h4>
          <ul class="footer-links">
            <li><router-link to="/news/category/科技">科技</router-link></li>
            <li><router-link to="/news/category/财经">财经</router-link></li>
            <li><router-link to="/news/category/体育">体育</router-link></li>
            <li><router-link to="/news/category/娱乐">娱乐</router-link></li>
            <li><router-link to="/news/category/教育">教育</router-link></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4 class="footer-col-title">联系我们</h4>
          <ul class="footer-contact">
            <li><span class="contact-icon">📧</span><span>551147279@qq.com</span></li>
            <li><span class="contact-icon">📞</span><span>155-4800-2235</span></li>
            <li><span class="contact-icon">📍</span><span>赤峰大学</span></li>
            <li><span class="contact-icon">🕐</span><span>周一至周日 7x24小时</span></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="footer-bottom-links">
          <span @click="handleNav('/')">关于我们</span>
          <span @click="handleNav('/')">隐私政策</span>
          <span @click="handleNav('/')">用户协议</span>
          <span @click="handleNav('/')">广告合作</span>
          <span @click="handleNav('/')">帮助中心</span>
        </div>
        <p class="footer-copyright">
          &copy; {{ currentYear }} NewsHub. All rights reserved.
        </p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer { background: #1a2332; color: rgba(255,255,255,0.8); padding: 60px 0 30px; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 24px; }
.footer-grid { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1.2fr; gap: 40px; padding-bottom: 40px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.footer-brand { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.footer-brand-text { font-size: 1.3rem; font-weight: 800; color: white; letter-spacing: -0.5px; }
.footer-desc { font-size: 0.88rem; line-height: 1.7; color: rgba(255,255,255,0.6); margin-bottom: 20px; }
.footer-social { display: flex; gap: 12px; }
.social-link { width: 40px; height: 40px; border-radius: 10px; background: rgba(255,255,255,0.08); display: flex; align-items: center; justify-content: center; color: rgba(255,255,255,0.6); cursor: pointer; transition: all 0.3s ease; }
.social-link:hover { background: #1a73e8; color: white; transform: translateY(-2px); }
.footer-col-title { font-size: 1rem; font-weight: 700; color: white; margin-bottom: 20px; }
.footer-links { display: flex; flex-direction: column; gap: 12px; }
.footer-links a { font-size: 0.88rem; color: rgba(255,255,255,0.6); cursor: pointer; transition: all 0.3s ease; text-decoration: none; }
.footer-links a:hover { color: white; padding-left: 4px; }
.footer-contact { display: flex; flex-direction: column; gap: 14px; }
.footer-contact li { display: flex; align-items: center; gap: 10px; font-size: 0.88rem; color: rgba(255,255,255,0.6); }
.contact-icon { font-size: 1rem; }
.footer-bottom { padding-top: 24px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.footer-bottom-links { display: flex; gap: 20px; }
.footer-bottom-links span { font-size: 0.82rem; color: rgba(255,255,255,0.45); cursor: pointer; transition: all 0.3s ease; }
.footer-bottom-links span:hover { color: white; }
.footer-copyright { font-size: 0.82rem; color: rgba(255,255,255,0.45); }

@media (max-width: 900px) { .footer-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 600px) {
  .footer-grid { grid-template-columns: 1fr; }
  .footer-bottom { flex-direction: column; text-align: center; }
  .footer-bottom-links { flex-wrap: wrap; justify-content: center; }
}
</style>
