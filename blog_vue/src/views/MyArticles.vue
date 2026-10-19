<template>
  <div class="my-articles">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">我的文章</div>
      <div class="page-description">管理您发表的所有文章</div>
    </div>
    <!-- 内容区域 -->
    <el-card class="content-card">
      <!-- 未登录状态 -->
      <div v-if="!isLoggedIn" class="login-required">
        <el-empty description="请先登录查看我的文章">
          <template #image>
            <el-icon :size="60"><Lock /></el-icon>
          </template>
          <el-button type="primary" @click="goToLogin">去登录</el-button>
        </el-empty>
      </div>

      <!-- 加载中 -->
      <div v-else-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 已登录但无数据 -->
      <div v-else-if="articles.length === 0" class="no-data">
        <el-empty description="暂无文章">
          <el-button type="primary" @click="router.push('/publish')">去发布文章</el-button>
        </el-empty>
      </div>

      <!-- 有数据但筛选结果为空 -->
      <div v-else-if="searchText && filteredArticles.length === 0" class="no-data">
        <el-empty description="未找到相关文章">
          <el-button @click="clearFilter" type="primary">清空搜索</el-button>
        </el-empty>
      </div>
      <!-- 文章列表 -->
      <div v-else class="articles-list">
        <div v-for="article in pagedArticles" :key="article.id" class="article-item">
          <el-card shadow="hover" class="article-card">
            <!-- 文章头部 -->
            <div class="article-header">
              <div class="article-title-section">
                <div class="article-title">{{ article.title }}</div>
                <el-tag
                    :type="getCategoryType(article.category)"
                    size="small"
                    effect="light"
                >
                  {{ getCategoryName(article.category) }}
                </el-tag>
              </div>

              <div class="article-actions">
                <el-button type="primary" size="small" plain @click="editArticle(article.id)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-popconfirm
                    title="确定要删除这篇文章吗？"
                    @confirm="deleteArticle(article.id)"
                >
                  <template #reference>
                    <el-button type="danger" size="small" plain>
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>

            <!-- 文章信息 -->
            <div class="article-info">
              <div class="info-row">
                <span class="info-item">
                  <el-icon><User /></el-icon>
                  作者: {{ article.authorName }}
                </span>
                <span class="info-item">
                  <el-icon><Calendar /></el-icon>
                  发布时间: {{ formatTime(article.publishTime) }}
                </span>
              </div>
            </div>

            <!-- 文章内容预览 -->
            <div class="article-content">
              {{ article.summary }}
            </div>
          </el-card>
        </div>
      </div>

      <!-- 分页组件 -->
      <el-pagination
          v-if="pagedArticles.length > 0 && isLoggedIn"
          class="pagination-wrapper"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="displayTotal"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElInput, ElButton, ElTag, ElEmpty, ElIcon, ElPopconfirm, ElPagination, ElSkeleton, ElMessage } from 'element-plus'
import { Search, Lock, Edit, Delete, User, Calendar } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 响应式数据
const isLoggedIn = ref(false)
const loading = ref(false)
const articles = ref([])
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 根据分类获取标签类型 - 修改为英文
const getCategoryType = (category) => {
  const typeMap = {
    'tech': 'primary',
    'life': 'success',
    'study': 'warning'
  }
  return typeMap[category] || 'info'
}

// 新增：英文转中文的映射
const getCategoryName = (category) => {
  const categoryMap = {
    'tech': '技术文章',
    'life': '生活随笔',
    'study': '学习笔记'
  }
  return categoryMap[category] || category
}

// 筛选文章
const filteredArticles = computed(() => {
  if (!searchText.value) return []
  const search = searchText.value.toLowerCase()
  return articles.value.filter(article =>
      article.title.toLowerCase().includes(search) ||
      article.content.toLowerCase().includes(search)
  )
})

// 获取当前页的文章
const pagedArticles = computed(() => {
  const source = searchText.value ? filteredArticles.value : articles.value
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return source.slice(start, end)
})

// 显示的总数（用于分页组件）
const displayTotal = computed(() => {
  return searchText.value ? filteredArticles.value.length : total.value
})

// 加载我的文章
const loadMyArticles = async () => {
  try {
    loading.value = true

    const token = localStorage.getItem('token')
    if (!token) {
      isLoggedIn.value = false
      return
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    const res = await request.get('/articles/my-articles', {
      params: params
    })

    if (res.code === 200) {
      articles.value = res.data?.list || res.list || res.articles || []
      total.value = res.data?.total || res.total || 0
    } else {
      articles.value = []
      total.value = 0
      ElMessage.error(res.message || '获取文章失败')
    }
  } catch (error) {
    console.error('获取我的文章失败:', error)
    articles.value = []
    total.value = 0
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
}

// 筛选文章
const filterArticles = () => {
  currentPage.value = 1
}

// 清空筛选
const clearFilter = () => {
  searchText.value = ''
  currentPage.value = 1
}

// 去登录
const goToLogin = () => {
  router.push('/login')
}

// 编辑文章
const editArticle = (id) => {
  router.push(`/edit/${id}`)
}

// 删除文章
const deleteArticle = async (id) => {
  try {
    const res = await request.delete(`/articles/${id}`)

    if (res.code === 200) {
      // 从列表中移除
      const index = articles.value.findIndex(article => article.id === id)
      if (index > -1) {
        articles.value.splice(index, 1)
        total.value -= 1

        ElMessage.success('删除成功')

        // 如果删除后当前页没有数据且不是第一页，返回上一页
        if (pagedArticles.value.length === 0 && currentPage.value > 1) {
          currentPage.value -= 1
          if (!searchText.value) {
            loadMyArticles()
          }
        }
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    console.error('删除文章失败:', error)
    ElMessage.error('删除失败')
  }
}

// 分页事件处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadMyArticles()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  if (!searchText.value) {
    loadMyArticles()
  }
}

// 初始化
onMounted(() => {
  checkLoginStatus()
  if (isLoggedIn.value) {
    loadMyArticles()
  }
})

// 监听搜索文本变化
watch(searchText, () => {
  currentPage.value = 1
})
</script>
<style scoped>
.my-articles {
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

.filter-card {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.filter-bar {
  display: flex;
  gap: 15px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.content-card {
  border: 1px solid #ebeef5;
}

.login-required,
.no-data,
.loading {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.article-item {
  width: 100%;
}

.article-card {
  border: none;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.article-title-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.article-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
}

.article-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.article-info {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  gap: 20px;
  align-items: center;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.article-content {
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-header {
    flex-direction: column;
    gap: 15px;
  }

  .article-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .filter-bar {
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }
}
</style>