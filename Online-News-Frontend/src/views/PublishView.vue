<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useNewsStore } from '@/stores/news'
import { useUserStore } from '@/stores/user'
import { publishNews, updateNews, getEditorNewsDetail, uploadNewsImage } from '@/api/news'
import NavBar from '@/components/news/NavBar.vue'
import { getCoverImageUrl } from '@/utils/image'

const router = useRouter()
const route = useRoute()
const newsStore = useNewsStore()
const userStore = useUserStore()

const isEditMode = ref(false)
const editId = ref<number | null>(null)
const uploading = ref(false)
const coverImagePath = ref('')

const form = ref({
  title: '',
  category: '国内',
  summary: '',
  content: '',
  coverImage: '',
  source: '',
  tags: '',
})

const errors = ref<Record<string, string>>({})
const submitting = ref(false)
const loading = ref(false)

const categories = ['国内', '国际', '财经', '科技', '体育', '娱乐', '健康', '教育']

// Check if editing existing news
onMounted(async () => {
  const id = route.query.id
  if (id) {
    isEditMode.value = true
    editId.value = Number(id)
    loading.value = true
    try {
      await newsStore.loadCategories()
      const response = await getEditorNewsDetail(editId.value)
      const news = response.data
      if (news) {
        form.value.title = news.title || ''
        form.value.summary = news.summary || ''
        form.value.content = news.content || ''
        form.value.source = news.source || ''
        form.value.coverImage = news.coverImage || ''
        coverImagePath.value = news.coverImage || ''
        form.value.tags = news.tags ? news.tags.map((t: any) => t.tagName).join(', ') : ''
        const cat = newsStore.categories.find((c: any) => c.id === news.categoryId)
        form.value.category = cat?.name || '国内'
      }
    } catch (e) {
      console.error('加载新闻失败', e)
    } finally {
      loading.value = false
    }
  }
})

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const response = await uploadNewsImage(file)
    coverImagePath.value = response.data
  } catch (e: any) {
    errors.value.coverImage = '图片上传失败: ' + (e.message || '未知错误')
  } finally {
    uploading.value = false
  }
}

const validateForm = () => {
  errors.value = {}
  if (!form.value.title.trim()) {
    errors.value.title = '请输入新闻标题'
  } else if (form.value.title.length < 5) {
    errors.value.title = '标题至少5个字符'
  }
  if (!form.value.summary.trim()) {
    errors.value.summary = '请输入新闻摘要'
  } else if (form.value.summary.length < 20) {
    errors.value.summary = '摘要至少20个字符'
  }
  if (!form.value.content.trim()) {
    errors.value.content = '请输入新闻正文'
  } else if (form.value.content.length < 50) {
    errors.value.content = '正文至少50个字符'
  }
  if (!form.value.source.trim()) {
    errors.value.source = '请输入来源'
  }
  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  if (!validateForm()) return
  submitting.value = true
  const tags = form.value.tags.split(/[,，\s]+/).filter(t => t.trim())

  const data = {
    title: form.value.title,
    categoryId: categories.indexOf(form.value.category) + 1,
    summary: form.value.summary,
    content: form.value.content,
    coverImage: coverImagePath.value || undefined,
    source: form.value.source,
    authorId: userStore.user?.id,
    tags,
  }

  try {
    if (isEditMode.value && editId.value) {
      await updateNews(editId.value, data)
    } else {
      await publishNews(data)
    }
    router.push('/editor')
  } catch (error: any) {
    errors.value.global = error.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/editor')
}
</script>

<template>
  <div class="publish-page">
    <NavBar />
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1 class="page-title">{{ isEditMode ? '编辑新闻' : '发布新闻' }}</h1>
          <p class="page-subtitle">{{ isEditMode ? '修改新闻内容，重新提交审核' : '填写新闻信息，提交后等待审核' }}</p>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <form v-else @submit.prevent="handleSubmit" class="publish-form">
          <div class="form-section">
            <div class="form-group">
              <label class="form-label">新闻标题 <span class="required">*</span></label>
              <input v-model="form.title" type="text" placeholder="请输入新闻标题" :class="['form-input', { error: errors.title }]" />
              <span v-if="errors.title" class="form-error">{{ errors.title }}</span>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">新闻类别 <span class="required">*</span></label>
                <select v-model="form.category" class="form-input">
                  <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">来源 <span class="required">*</span></label>
                <input v-model="form.source" type="text" placeholder="请输入来源" :class="['form-input', { error: errors.source }]" />
                <span v-if="errors.source" class="form-error">{{ errors.source }}</span>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">关键词标签</label>
              <input v-model="form.tags" type="text" placeholder="多个标签用逗号或空格分隔" class="form-input" />
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label class="form-label">新闻摘要 <span class="required">*</span></label>
              <textarea v-model="form.summary" placeholder="请输入新闻摘要，简洁概括新闻内容" rows="3" :class="['form-input', { error: errors.summary }]"></textarea>
              <span v-if="errors.summary" class="form-error">{{ errors.summary }}</span>
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label class="form-label">新闻正文 <span class="required">*</span></label>
              <textarea v-model="form.content" placeholder="请输入新闻正文内容" rows="12" :class="['form-input', { error: errors.content }]"></textarea>
              <span v-if="errors.content" class="form-error">{{ errors.content }}</span>
            </div>
          </div>

          <div class="form-section">
            <div class="form-group">
              <label class="form-label">封面图片</label>
              <div class="upload-area">
                <div v-if="coverImagePath" class="cover-preview">
                  <img :src="getCoverImageUrl(coverImagePath) || coverImagePath" alt="封面预览" class="preview-img" />
                  <button type="button" class="remove-btn" @click="coverImagePath = ''">✕</button>
                </div>
                <label class="upload-btn" :class="{ disabled: uploading }">
                  <input type="file" accept="image/*" @change="handleFileChange" :disabled="uploading" style="display:none" />
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                  <span>{{ uploading ? '上传中...' : (coverImagePath ? '更换图片' : '上传图片') }}</span>
                </label>
              </div>
              <span v-if="errors.coverImage" class="form-error">{{ errors.coverImage }}</span>
            </div>
          </div>

          <div v-if="errors.global" class="global-error">
            {{ errors.global }}
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-outline" @click="handleCancel">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交审核' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<style scoped>
.publish-page { min-height: 100vh; background: #f5f8fc; }
.main-content { padding-top: 72px; }
.container { max-width: 900px; margin: 0 auto; padding: 0 24px; }
.page-header { padding: 32px 0; }
.page-title { font-size: 1.8rem; font-weight: 800; color: #1a2332; margin-bottom: 8px; }
.page-subtitle { font-size: 0.95rem; color: #8e99a9; }
.loading-state { display: flex; flex-direction: column; align-items: center; padding: 80px 0; color: #8e99a9; }
.spinner { width: 40px; height: 40px; border: 4px solid #e1e8ef; border-top-color: #1a73e8; border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.publish-form { background: white; border-radius: 16px; padding: 32px; box-shadow: 0 2px 12px rgba(26, 115, 232, 0.06); }
.form-section { margin-bottom: 24px; }
.form-section:not(:last-child) { padding-bottom: 24px; border-bottom: 1px solid #eef2f7; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.form-label { font-size: 0.85rem; font-weight: 600; color: #334155; }
.required { color: #ff3b30; }
.form-input { padding: 12px 16px; border: 2px solid #e1e8ef; border-radius: 10px; font-size: 0.9rem; color: #1a2332; outline: none; transition: all 0.3s ease; font-family: inherit; }
.form-input:focus { border-color: #1a73e8; box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1); }
.form-input.error { border-color: #ff3b30; }
.form-input::placeholder { color: #8e99a9; }
textarea.form-input { resize: vertical; }
.form-error { font-size: 0.78rem; color: #ff3b30; }
.global-error { background: #fff2f0; border: 1px solid #ffccc7; border-radius: 8px; padding: 12px 16px; color: #ff3b30; font-size: 0.85rem; margin-bottom: 16px; }

.upload-area { display: flex; flex-direction: column; gap: 12px; }
.cover-preview { position: relative; width: 200px; height: 150px; border-radius: 10px; overflow: hidden; border: 2px solid #e1e8ef; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn { position: absolute; top: 6px; right: 6px; width: 24px; height: 24px; border-radius: 50%; background: rgba(0,0,0,0.5); color: white; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 0.75rem; }
.upload-btn { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; background: #e8f0fe; color: #1a73e8; border-radius: 8px; cursor: pointer; font-size: 0.85rem; font-weight: 600; transition: all 0.3s ease; width: fit-content; }
.upload-btn:hover { background: #d0e3fc; }
.upload-btn.disabled { opacity: 0.6; cursor: not-allowed; }

.form-actions { display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; }
.form-actions .btn:disabled { opacity: 0.6; cursor: not-allowed; }

@media (max-width: 768px) {
  .form-row { grid-template-columns: 1fr; }
  .publish-form { padding: 24px; }
}
</style>
