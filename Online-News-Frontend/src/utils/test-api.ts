import { getNewsList } from '@/api/news'

// 测试与后端的连接
async function testBackendConnection() {
  console.log('🔍 开始测试后端连接...')
  console.log('📍 API 地址:', import.meta.env.VITE_API_BASE_URL)

  try {
    // 测试获取新闻列表
    console.log('📡 发送请求: GET /api/news')
    const response = await getNewsList(undefined, 1, 5)
    
    console.log('✅ 请求成功!')
    console.log('📊 响应数据:', {
      code: response.code,
      message: response.message,
      data: response.data
    })
    
    if (response.data && Array.isArray(response.data)) {
      console.log(`📰 获取到 ${response.data.length} 条新闻`)
      console.log('📝 第一条新闻:', response.data[0])
    }
    
    return true
  } catch (error: any) {
    console.error('❌ 请求失败:', error.message)
    
    if (error.response) {
      console.error('🔴 响应状态:', error.response.status)
      console.error('🔴 响应数据:', error.response.data)
    } else if (error.request) {
      console.error('🔴 请求已发送但无响应')
    } else {
      console.error('🔴 错误详情:', error)
    }
    
    return false
  }
}

// 在浏览器控制台运行测试
if (typeof window !== 'undefined') {
  (window as any).testBackendConnection = testBackendConnection
  console.log('💡 在控制台输入 testBackendConnection() 即可测试后端连接')
}

export { testBackendConnection }