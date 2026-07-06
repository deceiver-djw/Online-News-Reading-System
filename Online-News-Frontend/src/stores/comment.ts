import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCommentsByNewsId, addComment, replyComment, deleteComment, likeComment, getMyComments } from '@/api/news'

export const useCommentStore = defineStore('comment', () => {
  const comments = ref<any[]>([])

  const loadComments = async (newsId: number) => {
    try {
      const response = await getCommentsByNewsId(newsId)
      comments.value = response.data || []
    } catch (error) {
      console.error('加载评论失败:', error)
    }
  }

  const getCommentsByNewsIdFn = async (newsId: number) => {
    await loadComments(newsId)
    return comments.value
  }

  const addCommentFn = async (data: { content: string; newsId: number; userId: number }) => {
    try {
      await addComment(data)
      await loadComments(data.newsId)
    } catch (error) {
      console.error('发表评论失败:', error)
    }
  }

  const addReply = async (data: { content: string; newsId: number; userId: number; parentCommentId: number }) => {
    try {
      await replyComment(data)
      await loadComments(data.newsId)
    } catch (error) {
      console.error('回复评论失败:', error)
    }
  }

  const toggleLike = async (commentId: number) => {
    try {
      await likeComment(commentId)
    } catch (error) {
      console.error('点赞评论失败:', error)
    }
  }

  const deleteCommentFn = async (commentId: number) => {
    try {
      await deleteComment(commentId)
      comments.value = comments.value.filter((c: any) => c.id !== commentId)
    } catch (error) {
      console.error('删除评论失败:', error)
    }
  }

  const myComments = ref<any[]>([])

  const loadMyComments = async (userId: number) => {
    try {
      const response = await getMyComments(userId)
      myComments.value = response.data || []
    } catch (error) {
      console.error('加载我的评论失败:', error)
    }
  }

  return {
    comments,
    myComments,
    loadComments,
    getCommentsByNewsId: getCommentsByNewsIdFn,
    addComment: addCommentFn,
    addReply,
    toggleLike,
    deleteComment: deleteCommentFn,
    loadMyComments,
  }
})