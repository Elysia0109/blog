<template>
  <div class="article-detail">
    <!-- 文章头部 -->
    <div class="article-header">
      <div class="title-section">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <el-tag :type="getCategoryType(article.category)" size="small">
            {{ getCategoryName(article.category) }}
          </el-tag>
          <div class="meta-info">
            <span class="author">
              <el-avatar :size="24" :src="article.authorAvatar" />
              <span>{{ article.authorName }}</span>
            </span>
            <span class="time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(article.publishTime) }}
            </span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ article.viewCount || 0 }} 阅读
            </span>
            <span v-if="isLoggedIn && isAuthor" class="edit-btn">
              <el-button type="primary" size="small" @click="editArticle">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 文章内容 -->
    <el-card class="article-content-card">
      <div class="article-content" v-html="formatContent(article.content)"></div>

      <!-- 文章操作栏 -->
      <div class="article-actions-bar">
        <div class="action-buttons">
          <el-button
              type="primary"
              plain
              @click="handleLike"
              :loading="likeLoading"
          >
            <el-icon><Star /></el-icon>
            {{ article.isLiked ? '取消点赞' : '点赞' }}
            <span v-if="article.likeCount > 0">({{ article.likeCount }})</span>
          </el-button>

          <el-button
              type="success"
              plain
              @click="handleCollect"
              :loading="collectLoading"
          >
            <el-icon><Collection /></el-icon>
            {{ article.isCollected ? '已收藏' : '收藏' }}
            <span v-if="article.collectCount > 0">({{ article.collectCount }})</span>
          </el-button>

          <el-button
              type="info"
              plain
              @click="scrollToComments"
          >
            <el-icon><ChatDotSquare /></el-icon>
            评论 ({{ commentCount }})
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card class="comments-section">
      <div class="comments-header">
        <h3>评论 ({{ commentCount }})</h3>
        <el-button
            v-if="isLoggedIn"
            type="primary"
            size="small"
            @click="showCommentForm = !showCommentForm"
        >
          <el-icon><EditPen /></el-icon>
          发表评论
        </el-button>
      </div>

      <!-- 评论输入框 -->
      <div v-if="showCommentForm" class="comment-form">
        <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            maxlength="500"
            show-word-limit
            resize="none"
        />
        <div class="form-actions">
          <el-button @click="cancelComment">取消</el-button>
          <el-button
              type="primary"
              @click="submitComment"
              :loading="submittingComment"
              :disabled="!commentContent.trim()"
          >
            发表评论
          </el-button>
        </div>
      </div>

      <!-- 评论列表 -->
      <div v-if="loadingComments" class="loading-comments">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="comments.length === 0" class="no-comments">
        <el-empty description="暂无评论，快来抢沙发~" />
      </div>

      <div v-else class="comments-list">
        <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item"
            :class="{ 'reply-mode': comment.parentId }"
        >
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.userAvatar" />
          </div>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-user">{{ comment.username }}</span>
              <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
            </div>
            <div class="comment-text">
              {{ comment.content }}
            </div>
            <div class="comment-actions">
              <el-button
                  v-if="isLoggedIn"
                  type="text"
                  size="small"
                  @click="replyToComment(comment)"
              >
                回复
              </el-button>
              <el-button
                  v-if="canDeleteComment(comment)"
                  type="text"
                  size="small"
                  class="delete-btn"
                  @click="deleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>

            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
              <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
              >
                <div class="reply-avatar">
                  <el-avatar :size="32" :src="reply.userAvatar" />
                </div>
                <div class="reply-content">
                  <div class="reply-header">
                    <span class="reply-user">{{ reply.username }}</span>
                    <span>回复</span>
                    <span class="reply-target">{{ reply.targetUsername }}</span>
                  </div>
                  <div class="reply-text">
                    {{ reply.content }}
                  </div>
                  <div class="reply-time">
                    {{ formatCommentTime(reply.createTime) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
          v-if="commentCount > pageSize"
          class="pagination"
          v-model:current-page="commentPage"
          v-model:page-size="pageSize"
          :background="true"
          layout="prev, pager, next, jumper"
          :total="commentCount"
          @current-change="loadComments"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  View,
  Edit,
  Star,
  Collection,
  ChatDotSquare,
  EditPen
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 文章数据
const article = ref({
  id: '',
  title: '',
  content: '',
  category: '',
  authorId: '',
  authorName: '',
  authorAvatar: '',
  publishTime: '',
  viewCount: 0,
  likeCount: 0,
  collectCount: 0,
  isLiked: false,
  isCollected: false
})

// 评论数据
const comments = ref([])
const commentContent = ref('')
const commentCount = ref(0)
const commentPage = ref(1)
const pageSize = ref(10)
const replyingTo = ref(null)

// 状态
const loading = ref(true)
const loadingComments = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)
const submittingComment = ref(false)
const showCommentForm = ref(false)
const isLoggedIn = ref(false)
const currentUser = ref(null)

// 计算属性
const isAuthor = computed(() => {
  return isLoggedIn.value && currentUser.value && currentUser.value.id === article.value.authorId
})

// 辅助函数
const getCategoryType = (category) => {
  const typeMap = {
    'tech': 'primary',
    'life': 'success',
    'study': 'warning'
  }
  return typeMap[category] || 'info'
}

const getCategoryName = (category) => {
  const categoryMap = {
    'tech': '技术文章',
    'life': '生活随笔',
    'study': '学习笔记'
  }
  return categoryMap[category] || category
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 如果是今天内
  if (diff < 24 * 60 * 60 * 1000) {
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000))
      return `${minutes}分钟前`
    }
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }

  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatCommentTime = (time) => {
  return formatTime(time)
}

const formatContent = (content) => {
  if (!content) return ''
  // 简单处理换行和空格
  return content.replace(/\n/g, '<br>').replace(/ /g, '&nbsp;')
}

const canDeleteComment = (comment) => {
  if (!isLoggedIn.value) return false
  // 评论作者或文章作者可以删除评论
  return currentUser.value.id === comment.userId || currentUser.value.id === article.value.authorId
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token

  if (isLoggedIn.value) {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        currentUser.value = JSON.parse(userInfoStr)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }
}

// 加载文章详情
const loadArticle = async () => {
  try {
    loading.value = true
    const articleId = route.params.id

    const res = await request.get(`/articles/${articleId}`)

    if (res.code === 200) {
      article.value = res.data || res

      // 增加阅读量
      await request.post(`/articles/${articleId}/view`)
    } else {
      ElMessage.error(res.message || '加载文章失败')
      router.push('/')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 加载评论
const loadComments = async () => {
  try {
    loadingComments.value = true
    const articleId = route.params.id

    const res = await request.get('/comments/article/' + articleId, {
      params: {
        page: commentPage.value,
        size: pageSize.value
      }
    })

    if (res.code === 200) {
      comments.value = res.data || []

      // 获取评论总数
      const countRes = await request.get(`/comments/article/${articleId}/count`)
      if (countRes.code === 200) {
        commentCount.value = countRes.data || 0
      }
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

// 点赞文章
const handleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再点赞！')
    router.push('/login')
    return
  }

  try {
    likeLoading.value = true
    const articleId = route.params.id

    const res = await request({
      url: `/articles/${articleId}/like`,
      method: article.value.isLiked ? 'DELETE' : 'POST'
    })

    if (res.code === 200) {
      article.value.isLiked = !article.value.isLiked
      article.value.likeCount = article.value.isLiked
          ? article.value.likeCount + 1
          : Math.max(0, article.value.likeCount - 1)

      ElMessage.success(article.value.isLiked ? '点赞成功' : '取消点赞')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
  } finally {
    likeLoading.value = false
  }
}

// 收藏文章
const handleCollect = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再收藏！')
    router.push('/login')
    return
  }

  try {
    collectLoading.value = true
    const articleId = route.params.id

    const res = await request({
      url: `/articles/${articleId}/collect`,
      method: article.value.isCollected ? 'DELETE' : 'POST'
    })

    if (res.code === 200) {
      article.value.isCollected = !article.value.isCollected
      article.value.collectCount = article.value.isCollected
          ? article.value.collectCount + 1
          : Math.max(0, article.value.collectCount - 1)

      ElMessage.success(article.value.isCollected ? '收藏成功' : '取消收藏')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  } finally {
    collectLoading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再评论！')
    router.push('/login')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    submittingComment.value = true
    const articleId = route.params.id

    const commentData = {
      articleId: articleId,
      content: commentContent.value.trim()
    }

    // 如果是回复
    if (replyingTo.value) {
      commentData.parentId = replyingTo.value.id
      commentData.targetUserId = replyingTo.value.userId
    }

    const res = await request.post('/comments', commentData)

    if (res.code === 200) {
      ElMessage.success('评论发表成功')
      commentContent.value = ''
      showCommentForm.value = false
      replyingTo.value = null

      // 重新加载评论
      commentPage.value = 1
      loadComments()
      commentCount.value += 1
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    submittingComment.value = false
  }
}

// 回复评论
const replyToComment = (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再回复！')
    router.push('/login')
    return
  }

  replyingTo.value = comment
  showCommentForm.value = true
  commentContent.value = `@${comment.username} `

  // 滚动到评论输入框
  setTimeout(() => {
    document.querySelector('.comment-form textarea').focus()
  }, 100)
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除这条评论吗？',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const res = await request.delete(`/comments/${commentId}`)

    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载评论
      loadComments()
      commentCount.value = Math.max(0, commentCount.value - 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 取消评论
const cancelComment = () => {
  commentContent.value = ''
  showCommentForm.value = false
  replyingTo.value = null
}

// 滚动到评论区
const scrollToComments = () => {
  const commentsSection = document.querySelector('.comments-section')
  if (commentsSection) {
    commentsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

// 编辑文章
const editArticle = () => {
  router.push(`/edit/${article.value.id}`)
}

// 初始化
onMounted(() => {
  checkLoginStatus()
  loadArticle()
  loadComments()
})

// 监听路由变化
watch(
    () => route.params.id,
    (newId) => {
      if (newId) {
        loadArticle()
        loadComments()
      }
    }
)
</script>

<style scoped>
.article-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 20px;
}

.title-section {
  text-align: center;
}

.article-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 25px;
  flex-wrap: wrap;
  justify-content: center;
}

.meta-info span {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author .el-avatar {
  background-color: #409EFF;
}

.edit-btn {
  margin-left: 10px;
}

.article-content-card {
  margin-bottom: 30px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  min-height: 300px;
  padding: 20px 0;
}

.article-content :deep(p) {
  margin-bottom: 1.5em;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

.article-actions-bar {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.comments-section {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.comments-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.comment-form {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}

.loading-comments {
  padding: 40px 0;
}

.no-comments {
  padding: 60px 0;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  background: #fafafa;
}

.comment-item.reply-mode {
  margin-left: 40px;
  background: #f5f5f5;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-avatar .el-avatar {
  background-color: #67c23a;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user {
  font-weight: bold;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.comment-actions .delete-btn {
  color: #f56c6c;
}

.replies-list {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #dcdfe6;
}

.reply-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px;
  background: white;
  border-radius: 6px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-avatar .el-avatar {
  background-color: #e6a23c;
}

.reply-content {
  flex: 1;
}

.reply-header {
  margin-bottom: 5px;
}

.reply-header span {
  font-size: 13px;
  color: #606266;
}

.reply-user {
  font-weight: bold;
  margin-right: 5px;
}

.reply-target {
  color: #409EFF;
  font-weight: bold;
  margin: 0 5px;
}

.reply-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-title {
    font-size: 22px;
  }

  .meta-info {
    gap: 15px;
    font-size: 13px;
  }

  .action-buttons {
    flex-wrap: wrap;
  }

  .comment-item.reply-mode {
    margin-left: 20px;
  }
}

@media (max-width: 480px) {
  .article-detail {
    padding: 15px;
  }

  .article-title {
    font-size: 20px;
  }

  .meta-info {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .comment-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-item.reply-mode {
    margin-left: 0;
  }
}
</style>