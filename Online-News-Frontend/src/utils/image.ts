/**
 * 将后端返回的封面图片路径转换为前端可访问的URL
 *
 * 后端 coverImage 存储的是本地绝对路径，如:
 *   - C:\images\news_1703123456789_a1b2c3d4_title.png
 *   - C:/images/news_1703123456789_a1b2c3d4_title.png
 *
 * 转换后得到可访问的URL:
 *   - /images/news_1703123456789_a1b2c3d4_title.png
 *
 * 前端通过 Vite 代理将 /images 请求转发到后端 8080 端口，
 * 后端 WebMvcConfig 将 /images/** 映射到本地 C:\images\ 目录
 */
export function getCoverImageUrl(coverImage: string | null | undefined): string | null {
  if (!coverImage) return null

  // 提取文件名（兼容 Windows 反斜杠和 Unix 正斜杠）
  const normalized = coverImage.replace(/\\/g, '/')
  const fileName = normalized.substring(normalized.lastIndexOf('/') + 1)

  if (!fileName) return null

  return `/images/${fileName}`
}
