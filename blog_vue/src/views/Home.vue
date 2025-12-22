<template>
  <div class="home">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">{{ pageTitle }}</div>
      <div class="page-description">{{ pageDescription }}</div>
    </div>

    <!-- 文章列表 -->
    <el-card class="articles-container">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="articles.length === 0" class="no-data">
        <el-empty :description="noDataText" />
      </div>
      <div v-else>
        <div v-for="article in articles" :key="article.id" class="article-card">
          <!-- 文章卡片头部 -->
          <div class="article-header">
            <div class="article-title">{{ article.title }}</div>
            <el-tag size="small" :type="getCategoryType(article.category)">
              {{ article.category }}
            </el-tag>
          </div>
          <!-- 文章信息 -->
          <div class="article-info">
            <span class="author">
              <el-icon><User /></el-icon>
              {{ article.authorName }}
            </span>
            <span class="time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(article.publishTime) }}
            </span>
          </div>

          <!-- 文章内容 -->
          <div class="article-content">{{ article.summary }}</div>

          <!-- 文章操作 -->
          <div class="article-actions">
            <el-button type="primary" size="small" plain @click="readArticle(article.id)">
              阅读全文
            </el-button>
            <el-button type="info" size="small" plain @click="collectArticle(article.id)" v-if="isLoggedIn">
              {{ isCollected(article.id) ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <!-- 分页组件 -->
        <el-pagination
            class="pagination-wrapper"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { User, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElEmpty, ElCard, ElTag, ElButton, ElIcon, ElPagination, ElSkeleton, ElMessage } from 'element-plus'
const route = useRoute()

// 响应式数据
const articles = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const isLoggedIn = ref(false)
const collectedArticles = ref([])

// 辅助函数（保持不变）
const getCategoryType = (category) => {
  const typeMap = {
    '技术文章': 'primary',
    '生活随笔': 'success',
    '学习笔记': 'warning'
  }
  return typeMap[category] || 'info'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
}

const isCollected = (articleId) => {
  return collectedArticles.value && collectedArticles.value.some(id => id === articleId)
}

// 计算属性（保持不变）
const pageTitle = computed(() => {
  const path = route.path

  if (path.startsWith('/category/')) {
    const category = route.params.category
    const categoryNames = {
      'tech': '技术文章',
      'life': '生活随笔',
      'study': '学习笔记'
    }
    return `${categoryNames[category] || category} - 文章列表`
  }

  if (path.startsWith('/search/')) {
    const keyword = decodeURIComponent(route.params.keyword || '')
    return `搜索: ${keyword}`
  }

  if (path === '/favorites') {
    return '我的收藏'
  }

  if (path === '/record') {
    return '浏览记录'
  }

  return '首页 - 全部文章'
})

const pageDescription = computed(() => {
  // 保持不变
  const path = route.path

  if (path.startsWith('/category/')) {
    return '按分类浏览文章'
  }

  if (path.startsWith('/search/')) {
    return '搜索结果'
  }

  if (path === '/favorites') {
    return '查看收藏的文章'
  }

  if (path === '/record') {
    return '查看最近浏览的文章'
  }

  return '浏览所有精彩文章'
})

const noDataText = computed(() => {
  const path = route.path

  if (path.startsWith('/search/')) {
    return '未找到相关文章'
  }

  if (path === '/favorites') {
    return '暂无收藏文章'
  }

  if (path === '/record') {
    return '暂无浏览记录'
  }

  return '暂无文章'
})

// 获取请求参数（保持不变）
const getRequestParams = () => {
  const path = route.path
  const params = {
    page: currentPage.value,
    size: pageSize.value
  }

  if (path.startsWith('/category/')) {
    const category = route.params.category
    const categoryMap = {
      'tech': '技术文章',
      'life': '生活随笔',
      'study': '学习笔记'
    }
    params.category = categoryMap[category] || category
  }

  if (path.startsWith('/search/')) {
    const keyword = decodeURIComponent(route.params.keyword || '')
    if (keyword) {
      params.keyword = keyword
    }
  }

  if (path === '/favorites') {
    params.favorites = true
  }

  if (path === '/record') {
    params.record = true
  }

  return params
}


// 加载文章 - 使用 request.js
const loadArticles = async () => {
  try {
    loading.value = true

    const params = getRequestParams()

    // 使用统一的request实例发送请求
    const res = await request.get('/articles', {
      params: params
    })
    if (res.code === 200) {
      articles.value = res.data?.list || res.list || res.articles || []
      total.value = res.data?.total || res.total || 0

      // 如果是收藏页面，获取收藏列表
      if (route.path === '/favorites' && res.data?.collectedIds) {
        collectedArticles.value = res.data.collectedIds
      }
    }else {
      articles.value = []
      total.value = 0
      console.error('获取文章列表失败:', res.message)
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 分页事件处理（保持不变）
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadArticles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadArticles()
}

// 阅读文章（保持不变）
const readArticle = (articleId) => {
  console.log('阅读文章:', articleId)
}

// 收藏文章 - 使用 request.js
const collectArticle = async (articleId) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再收藏文章！')  // 添加了提示
    return
  }

  try {
    const isCurrentlyCollected = isCollected(articleId)

    // 使用统一的request实例发送请求
    const res = await request({
      url: '/articles/collect',
      method: isCurrentlyCollected ? 'DELETE' : 'POST',
      data: { articleId: articleId }
    })

    if (res.code === 200) {
      if (isCurrentlyCollected) {
        collectedArticles.value = collectedArticles.value.filter(id => id !== articleId)
      } else {
        collectedArticles.value.push(articleId)
      }
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

// 初始化（保持不变）
onMounted(() => {
  checkLoginStatus()
  loadArticles()
})

// 监听路由变化（保持不变）
watch(
    () => route.path,
    () => {
      currentPage.value = 1  // 路由变化时重置页码
      checkLoginStatus()
      loadArticles()
    }
)
</script>

<style scoped>
.home {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.page-description {
  font-size: 14px;
  color: #909399;
}

.articles-container {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading {
  padding: 20px 0;
}

.no-data {
  padding: 40px 0;
}

.article-card {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.article-card:last-child {
  border-bottom: none;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  flex: 1;
  margin-right: 15px;
}

.article-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.article-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-actions {
  display: flex;
  gap: 10px;
}

.pagination-wrapper {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
}
</style>