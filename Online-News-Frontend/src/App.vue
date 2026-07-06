<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* ===================== 全局变量 - 蓝白主题 ===================== */
:root {
  --primary: #1a73e8;
  --primary-dark: #1557b0;
  --primary-light: #e8f0fe;
  --primary-gradient: linear-gradient(135deg, #1a73e8, #4a90d9);
  --accent: #ff6b35;
  --accent-light: #fff3ed;
  --bg-white: #ffffff;
  --bg-light: #f5f8fc;
  --bg-gray: #eef2f7;
  --text-primary: #1a2332;
  --text-secondary: #5f6b7a;
  --text-muted: #8e99a9;
  --border-color: #e1e8ef;
  --shadow-sm: 0 2px 8px rgba(26, 115, 232, 0.08);
  --shadow-md: 0 4px 16px rgba(26, 115, 232, 0.12);
  --shadow-lg: 0 8px 32px rgba(26, 115, 232, 0.16);
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 20px;
  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --max-width: 1280px;
}

/* ===================== 全局重置 ===================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: var(--text-primary);
  background: var(--bg-light);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  text-decoration: none;
  color: inherit;
  transition: var(--transition);
}

a:hover {
  color: var(--primary);
}

ul {
  list-style: none;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

/* ===================== 滚动条 ===================== */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-light);
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}

/* ===================== 通用组件类 ===================== */
.container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 24px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

.section-more {
  font-size: 0.9rem;
  color: var(--primary);
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}

.section-more:hover {
  gap: 8px;
}

.card {
  background: var(--bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  overflow: hidden;
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.tag-blue {
  background: var(--primary-light);
  color: var(--primary);
}

.tag-orange {
  background: var(--accent-light);
  color: var(--accent);
}

.tag-green {
  background: #e6f7e6;
  color: #2e7d32;
}

.tag-line {
  background: #f0f2f5;
  color: #5f6b7a;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 24px;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: var(--transition);
}

.btn-primary {
  background: var(--primary-gradient);
  color: white;
  box-shadow: 0 4px 12px rgba(26, 115, 232, 0.3);
}

.btn-primary:hover {
  box-shadow: 0 6px 20px rgba(26, 115, 232, 0.4);
  transform: translateY(-1px);
  color: white;
}

.btn-outline {
  background: transparent;
  color: var(--primary);
  border: 2px solid var(--primary);
}

.btn-outline:hover {
  background: var(--primary);
  color: white;
}

.btn-sm {
  padding: 6px 16px;
  font-size: 0.8rem;
}

#app {
  max-width: 100%;
  margin: 0;
  padding: 0;
  font-weight: normal;
}

@media (min-width: 1024px) {
  body {
    display: block;
    place-items: unset;
  }

  #app {
    display: block;
    grid-template-columns: unset;
    padding: 0;
  }
}
</style>