<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import { useUserStore } from '@/stores/user'
import { deleteNews as deleteNewsApi, auditNews, getMyNews, getPendingNews, getAllNews } from '@/api/news'
import NavBar from '@/components/news/NavBar.vue'

const router = useRouter()
const newsStore = useNewsStore()
const userStore = useUserStore()

const currentTab = ref<'PENDING' | 'PUBLISHED' | 'REJECTED'>('PENDING')
const searchKeyword = ref('')
const allNewsList = ref<any[]>([])
const myNewsList = ref<any[]>([])

const fullList = computed(() => {
  return userStore.isAdmin ? allNewsList.value : myNewsList.value
})

const newsList = computed(() => {
  let list = fullList.value
  if (!userStore.isAdmin) {
    list = myNewsList.value
  }
  return list.filter(news => {
    const matchesStatus = currentTab.value === 'PENDING' ? news.status === 'PENDING' :
                          currentTab.value === 'PUBLISHED' ? news.status === 'PUBLISHED' :
                          news.status === 'REJECTED'
    const matchesKeyword = !searchKeyword.value ||
                          news.title.includes(searchKeyword.value) ||
                          (news.summary && news.summary.includes(searchKeyword.value))
    return matchesStatus && matchesKeyword
  })
})

const loadData = async () => {
  try {
    await newsStore.loadCategories()
    if (userStore.isAdmin) {
      const response = await getAllNews()
      allNewsList.value = response.data || []
    }
    if (userStore.user?.id) {
      const response = await getMyNews(userStore.user.id)
      myNewsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载稿件失败:', error)
  }
}

onMounted(() => { loadData() })

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

const handleEdit = (newsId: number) => { router.push(`/publish?id=${newsId}`) }

const handleDelete = async (newsId: number) => {
  if (confirm('确定要删除这篇新闻吗？')) {
    try {
      await deleteNewsApi(newsId)
      allNewsList.value = allNewsList.value.filter(n => n.id !== newsId)
      myNewsList.value = myNewsList.value.filter(n => n.id !== newsId)
    } catch (error) { console.error('删除失败:', error) }
  }
}

const handleApprove = async (newsId: number) => {
  try {
    await auditNews(newsId, 'approve')
    await loadData()
  } catch (error) { console.error('审核通过失败:', error) }
}

const handleReject = async (newsId: number) => {
  try {
    await auditNews(newsId, 'reject')
    await loadData()
  } catch (error) { console.error('驳回失败:', error) }
}

const getCategoryName = (categoryId: number): string => {
  const cat = newsStore.categories.find(c => c.id === categoryId)
  return cat?.name || '未分类'
}

const isOwnNews = (news: any): boolean => {
  return news.authorId === userStore.user?.id || news.author?.id === userStore.user?.id
}
</script>

<template>
  <div class="editor-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">稿件管理</h1>
          <button class="btn btn-primary" @click="router.push('/publish')">+ 发布新闻</button>
        </div>

        <div class="filter-bar">
          <div class="search-box">
            <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
              <circle cx="9" cy="9" r="6.5" stroke="#8e99a9" stroke-width="1.5"/>
              <path d="M14 14l4 4" stroke="#8e99a9" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input v-model="searchKeyword" type="text" placeholder="搜索标题或摘要..." />
          </div>

          <div class="status-tabs">
            <button :class="['tab', { active: currentTab === 'PENDING' }]" @click="currentTab = 'PENDING'">
              <span class="tab-label">待审核</span>
              <span class="tab-count">{{ fullList.filter(n => n.status === 'PENDING').length }}</span>
            </button>
            <button :class="['tab', { active: currentTab === 'PUBLISHED' }]" @click="currentTab = 'PUBLISHED'">
              <span class="tab-label">已发布</span>
              <span class="tab-count">{{ fullList.filter(n => n.status === 'PUBLISHED').length }}</span>
            </button>
            <button :class="['tab', { active: currentTab === 'REJECTED' }]" @click="currentTab = 'REJECTED'">
              <span class="tab-label">已驳回</span>
              <span class="tab-count">{{ fullList.filter(n => n.status === 'REJECTED').length }}</span>
            </button>
          </div>
        </div>

        <div class="news-table">
          <table>
            <thead>
              <tr>
                <th>标题</th>
                <th>类别</th>
                <th>作者</th>
                <th>状态</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="news in newsList" :key="news.id">
                <td class="title-cell">
                  <span class="news-title">{{ news.title }}</span>
                  <span class="news-summary">{{ news.summary }}</span>
                </td>
                <td><span class="tag tag-blue">{{ getCategoryName(news.categoryId) }}</span></td>
                <td>{{ news.author?.username || news.source || '未知' }}</td>
                <td>
                  <span :class="['status-badge', news.status.toLowerCase()]">
                    {{ news.status === 'PENDING' ? '待审核' : news.status === 'PUBLISHED' ? '已发布' : '已驳回' }}
                  </span>
                </td>
                <td>{{ formatDate(news.publishTime || news.createdAt) }}</td>
                <td>
                  <div class="action-buttons">
                    <!-- 自己的新闻可以编辑 -->
                    <button v-if="isOwnNews(news)" class="action-btn edit" @click="handleEdit(news.id)">编辑</button>
                    <!-- 管理员审核按钮 -->
                    <button v-if="userStore.isAdmin && news.status === 'PENDING'" class="action-btn approve" @click="handleApprove(news.id)">通过</button>
                    <button v-if="userStore.isAdmin && news.status === 'PENDING'" class="action-btn reject" @click="handleReject(news.id)">驳回</button>
                    <!-- 管理员可删除任何新闻，普通用户只能删除自己的 -->
                    <button v-if="userStore.isAdmin || isOwnNews(news)" class="action-btn delete" @click="handleDelete(news.id)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="newsList.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/>
            </svg>
            <p>暂无{{ currentTab === 'PENDING' ? '待审核' : currentTab === 'PUBLISHED' ? '已发布' : '已驳回' }}的稿件</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.editor-page { min-height: 100vh; background: #f5f8fc; }
.main-content { padding-top: 72px; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; padding: 32px 0; }
.page-title { font-size: 1.8rem; font-weight: 800; color: #1a2332; }
.filter-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px; flex-wrap: wrap; gap: 16px; }
.search-box { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: white; border: 2px solid #e1e8ef; border-radius: 50px; width: 300px; }
.search-box input { flex: 1; border: none; outline: none; font-size: 0.85rem; color: #1a2332; font-family: inherit; }
.search-box input::placeholder { color: #8e99a9; }
.status-tabs { display: flex; gap: 4px; background: white; padding: 4px; border-radius: 10px; }
.tab { display: flex; align-items: center; gap: 8px; padding: 8px 20px; border: none; background: transparent; border-radius: 8px; font-size: 0.85rem; font-weight: 600; color: #5f6b7a; cursor: pointer; transition: all 0.3s ease; }
.tab.active { background: #1a73e8; color: white; }
.tab-count { padding: 2px 8px; background: rgba(0,0,0,0.1); border-radius: 10px; font-size: 0.7rem; }
.tab.active .tab-count { background: rgba(255,255,255,0.2); }
.news-table { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 12px rgba(26,115,232,0.06); overflow-x: auto; }
.news-table table { width: 100%; border-collapse: collapse; }
.news-table th { text-align: left; padding: 12px 16px; font-size: 0.85rem; font-weight: 600; color: #5f6b7a; border-bottom: 2px solid #eef2f7; }
.news-table td { padding: 16px; font-size: 0.85rem; color: #333; border-bottom: 1px solid #f0f4f8; vertical-align: top; word-break: break-word; overflow-wrap: break-word; white-space: normal; }
.news-table tr:hover td { background: #fafcff; }
.title-cell { display: flex; flex-direction: column; gap: 4px; max-width: 400px; word-break: break-word; }
.news-title { font-weight: 600; color: #1a2332; word-break: break-word; }
.news-summary { font-size: 0.78rem; color: #8e99a9; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.status-badge { padding: 4px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
.status-badge.pending { background: #fff7e6; color: #ff9500; }
.status-badge.published { background: #e8f5e9; color: #4caf50; }
.status-badge.rejected { background: #ffebee; color: #ff3b30; }
.action-buttons { display: flex; gap: 8px; }
.action-btn { padding: 6px 12px; border: none; border-radius: 6px; font-size: 0.78rem; font-weight: 600; cursor: pointer; transition: all 0.3s ease; }
.action-btn.edit { background: #e8f0fe; color: #1a73e8; }
.action-btn.edit:hover { background: #1a73e8; color: white; }
.action-btn.approve { background: #e8f5e9; color: #4caf50; }
.action-btn.approve:hover { background: #4caf50; color: white; }
.action-btn.reject { background: #ffebee; color: #ff3b30; }
.action-btn.reject:hover { background: #ff3b30; color: white; }
.action-btn.delete { background: #f5f8fc; color: #8e99a9; }
.action-btn.delete:hover { background: #ff3b30; color: white; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 0; color: #8e99a9; }
.empty-state p { margin-top: 16px; font-size: 0.95rem; }

@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .search-box { width: 100%; }
  .news-table { padding: 16px; }
  .news-table th, .news-table td { padding: 12px 8px; font-size: 0.78rem; }
  .action-buttons { flex-direction: column; }
}
</style>
