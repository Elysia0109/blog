<template>
  <!-- 保持原有UI完全不变 -->
  <div class="publish-articles">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">发布新文章</div>
      <div class="page-description">分享您的知识和见解</div>
    </div>

    <!-- 表单容器 -->
    <el-card class="form-container" shadow="hover">
      <!-- 文章标题 -->
      <div class="form-item">
        <div class="form-label">文章标题 <span class="required">*</span></div>
        <el-input
            v-model="form.title"
            placeholder="请输入文章标题，不超过50字"
            clearable
            maxlength="50"
            show-word-limit
            :class="{ 'error-input': isTitleTooLong }"
        >
          <template #prefix>
            <el-icon><Document /></el-icon>
          </template>
        </el-input>
        <div v-if="isTitleTooLong" class="error-message">
          <el-icon><Warning /></el-icon>
          文章标题不能超过50字
        </div>
      </div>

      <!-- 文章分类 -->
      <div class="form-item">
        <div class="form-label">文章分类 <span class="required">*</span></div>
        <el-select
            v-model="form.category"
            placeholder="请选择文章分类"
            style="width: 100%"
            clearable
        >
          <template #prefix>
            <el-icon><Folder /></el-icon>
          </template>
          <el-option label="技术文章" value="tech" />
          <el-option label="生活随笔" value="life" />
          <el-option label="学习笔记" value="study" />
        </el-select>
        <div v-if="showCategoryError" class="error-message">
          <el-icon><Warning /></el-icon>
          请选择文章分类
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="form-item">
        <div class="form-label">文章内容 <span class="required">*</span></div>
        <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入文章内容，不超过5000字"
            :rows="10"
            maxlength="5000"
            show-word-limit
            resize="none"
            :class="{ 'error-input': isContentTooLong }"
        />
        <div v-if="isContentTooLong" class="error-message">
          <el-icon><Warning /></el-icon>
          文章内容不能超过5000字
        </div>
        <div v-if="showContentError" class="error-message">
          <el-icon><Warning /></el-icon>
          请输入文章内容
        </div>
      </div>

      <!-- 表单操作按钮 -->
      <div class="form-actions">
        <el-button
            type="primary"
            size="large"
            @click="submitForm"
            :loading="submitting"
            class="submit-btn"
        >
          <el-icon><Promotion /></el-icon>
          发布文章
        </el-button>
        <el-button
            type="info"
            size="large"
            @click="resetForm"
            plain
            class="reset-btn"
        >
          <el-icon><Refresh /></el-icon>
          重置表单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElCard, ElInput, ElSelect, ElOption, ElButton, ElIcon, ElMessage } from 'element-plus'
import { Document, Folder, Promotion, Refresh, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// 判断是否为编辑模式
const isEditMode = ref(false)
const articleId = ref('')

// 表单数据
const form = ref({
  title: '',
  category: '',
  content: ''
})

const submitting = ref(false)
const showCategoryError = ref(false)
const showContentError = ref(false)

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再发布文章！')
    router.push('/login')
    return false
  }
  return true
}

// 初始化时检查是否为编辑模式
const checkEditMode = () => {
  if (route.path.startsWith('/edit/')) {
    isEditMode.value = true
    articleId.value = route.params.id
    loadArticleDetail()
  }
}

// 加载文章详情（编辑模式）
const loadArticleDetail = async () => {
  if (!isEditMode.value || !articleId.value) {
    return
  }

  if (!checkLoginStatus()) {
    return
  }

  try {
    const res = await request.get(`/articles/${articleId.value}`)

    if (res.code === 200) {
      const article = res

      // 填充表单数据
      form.value = {
        id: article.id || articleId.value,
        title: article.title || '',
        category: getCategoryValue(article.category) || '',
        content: article.content || ''
      }

      // 更新页面标题（仅在编辑模式下）
      document.title = '编辑文章 - 博客系统'

      ElMessage.success('文章加载成功')
    } else {
      ElMessage.error(res.message || '加载文章失败')
      router.push('/my-articles')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败，请重试')
    router.push('/my-articles')
  }
}

// 将分类显示名称转换为值
const getCategoryValue = (categoryName) => {
  const categoryMap = {
    '技术文章': 'tech',
    '生活随笔': 'life',
    '学习笔记': 'study'
  }
  return categoryMap[categoryName] || categoryName;
}

// 获取分类显示名称
const getCategoryName = (value) => {
  const categoryMap = {
    'tech': '技术文章',
    'life': '生活随笔',
    'study': '学习笔记'
  }
  return categoryMap[value] || value
}

onMounted(() => {
  checkEditMode()
  if (!checkLoginStatus()) {

  }
})

// 标题是否超长
const isTitleTooLong = computed(() => {
  return form.value.title.length > 50
})

// 内容是否超长
const isContentTooLong = computed(() => {
  return form.value.content.length > 5000
})

// 表单验证
const validateForm = () => {
  let isValid = true
  showCategoryError.value = false
  showContentError.value = false

  if (!form.value.title || form.value.title.length === 0) {
    ElMessage.warning('请输入文章标题')
    isValid = false
  } else if (isTitleTooLong.value) {
    ElMessage.warning('文章标题不能超过50字')
    isValid = false
  }

  if (!form.value.category) {
    showCategoryError.value = true
    ElMessage.warning('请选择文章分类')
    isValid = false
  }

  if (!form.value.content || form.value.content.length === 0) {
    showContentError.value = true
    ElMessage.warning('请输入文章内容')
    isValid = false
  } else if (isContentTooLong.value) {
    ElMessage.warning('文章内容不能超过5000字')
    isValid = false
  }

  return isValid
}

// 提交表单 - 使用 request.js
// 提交表单
const submitForm = async () => {
  console.log('开始提交文章...')

  // 检查登录状态
  const token = localStorage.getItem('token')
  console.log(' 当前token:', token)
  console.log(' 表单数据:', form.value)

  if (!token || token === 'undefined' || token === 'null') {
    ElMessage.error('请先登录！')
    router.push('/login')
    return
  }

  // 表单验证
  if (!validateForm()) {
    return
  }

  submitting.value = true

  try {
    const requestData = {
      title: form.value.title,
      category: form.value.category,
      content: form.value.content
    }

    console.log('📤 发送请求数据:', requestData)

    let res

    if (isEditMode.value) {
      // 编辑文章
      console.log('编辑文章，ID:', articleId.value)
      res = await request({
        url: `/articles/${articleId.value}`,
        method: 'PUT',
        data: requestData
      })
    } else {
      // 创建文章
      console.log(' 创建新文章')
      res = await request({
        url: '/articles',
        method: 'POST',
        data: requestData
      })
    }

    console.log(' 收到响应:', res)

    if (res.code === 200) {
      const successMessage = isEditMode.value ? '文章修改成功！' : '文章发布成功！'
      ElMessage.success(successMessage)

      // 跳转到我的文章页面
      setTimeout(() => {
        router.push('/my-articles')
      }, 1500)

      if (!isEditMode.value) {
        resetForm()
      }
    } else {
      console.error(' 业务逻辑错误:', res)
      ElMessage.error(res.message || '操作失败')
    }

  } catch (error) {
    console.error(' 请求失败:', error)

    // 更详细的错误处理
    if (error.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.clear()
      router.push('/login')
    } else {
      ElMessage.error(error.message || '操作失败，请重试')
    }

  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.value = {
    title: '',
    category: '',
    content: ''
  }
  showCategoryError.value = false
  showContentError.value = false
}

// 监听路由变化
watch(
    () => route.params.id,
    (newId) => {
      if (newId && route.path.startsWith('/edit/')) {
        articleId.value = newId
        loadArticleDetail()
      }
    }
)
</script>

<style scoped>
/* 保持原有样式完全不变 */
.publish-articles {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
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

.form-container {
  max-width: 800px;
  margin: 0 auto;
  border: 1px solid #ebeef5;
}

.form-item {
  margin-bottom: 24px;
}

.form-label {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

.error-input :deep(.el-input__inner),
.error-input :deep(.el-textarea__inner) {
  border-color: #f56c6c !important;
}

.error-message {
  margin-top: 6px;
  font-size: 12px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-message .el-icon {
  font-size: 14px;
}

:deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  padding-right: 8px;
}

:deep(.el-select .el-input__prefix) {
  pointer-events: none;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
  line-height: 1.6;
  min-height: 200px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.submit-btn {
  min-width: 140px;
}

.reset-btn {
  min-width: 140px;
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
    gap: 15px;
  }

  .submit-btn,
  .reset-btn {
    width: 100%;
  }

  .form-container {
    padding: 20px;
  }

  :deep(.el-textarea__inner) {
    min-height: 150px;
  }
}
</style>