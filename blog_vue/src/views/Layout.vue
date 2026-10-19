<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo">
          <el-image src="https://ts1.tc.mm.bing.net/th/id/OIP-C.XEkMn3xE4iFIzIB5DNE9fgHaHa?w=193&h=193&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.1&pid=3.1&rm=2&ucfimg=1" style="width: 100px" fit="contain" />
        </div>

        <!-- 导航菜单 -->
        <div class="nav-center">
          <div class="nav-menu">
            <div
                class="nav-item"
                :class="{ active: activeNav === 'home' }"
                @click="goToHome"
            >
              首页
            </div>
            <el-popover
                placement="bottom"
                trigger="hover"
                :width="120"
            >
              <template #reference>
                <div
                    class="nav-item category-item"
                    :class="{ active: activeNav === 'category' }"
                >
                  分类
                </div>
              </template>
              <div class="category-dropdown">
                <div @click="goToCategory('tech')">技术文章</div>
                <div @click="goToCategory('life')">生活随笔</div>
                <div @click="goToCategory('study')">学习笔记</div>
              </div>
            </el-popover>
          </div>
        </div>

        <!-- 右侧功能区 -->
        <div class="nav-right">
          <!-- 搜索框 -->
          <el-input
              v-model="searchText"
              placeholder="请输入搜索内容"
              clearable
              @keyup.enter="handleSearch"
              @clear="clearSearch"
              class="search-input"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>

          <!-- 动态显示登录/用户信息 -->
          <div v-if="isLoggedIn" class="user-info">
            <el-dropdown @command="handleUserCommand">
              <span class="user-dropdown">
                <el-avatar :size="30" :src="avatarUrl" />
                <span class="username">{{ username }}</span>
                <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-button
              v-else
              class="login-btn"
              type="primary"
              @click="goToLogin"
          >
            登录
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 左侧内容区 -->
      <el-main class="content-main" :style="{ marginRight: sidebarCollapsed ? '0' : '280px' }">
        <router-view />
      </el-main>
      <!-- 侧边栏分隔条 -->
      <div
          class="sidebar-handle"
          @click="toggleSidebar"
          :style="{ right: sidebarCollapsed ? '0' : '280px' }"
      >
        <div class="arrow-icon" :class="{ 'collapsed': sidebarCollapsed }">
          ◀
        </div>
      </div>

      <!-- 右侧侧边栏 -->
      <el-aside
          :width="sidebarCollapsed ? '0px' : '280px'"
          class="right-sidebar"
      >
        <!-- 个人信息卡片 -->
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar
                :size="80"
                :src="isLoggedIn ? avatarUrl : ''"
                class="profile-avatar"
            />
            <div class="profile-info">
              <div class="profile-name">{{ isLoggedIn ? username : '游客' }}</div>
              <el-tag v-if="!isLoggedIn" type="info" size="small">未登录</el-tag>
            </div>
          </div>
        </el-card>

        <!-- 功能菜单 -->
        <el-card class="menu-card">
          <el-menu
              :default-active="activeMenu"
              class="sidebar-menu"
              @select="handleMenuSelect"
          >
            <el-menu-item
                index="my-articles"
                :disabled="!isLoggedIn"
            >
              <el-icon><Document /></el-icon>
              <span>我的文章</span>
              <el-tag v-if="!isLoggedIn" type="info" size="small" class="login-tag">需登录</el-tag>
            </el-menu-item>

            <el-menu-item
                index="favorites"
                :disabled="!isLoggedIn"
            >
              <el-icon><Star /></el-icon>
              <span>收藏夹</span>
              <el-tag v-if="!isLoggedIn" type="info" size="small" class="login-tag">需登录</el-tag>
            </el-menu-item>

            <el-menu-item
                index="record"
                :disabled="!isLoggedIn"
            >
              <el-icon><Clock /></el-icon>
              <span>浏览记录</span>
              <el-tag v-if="!isLoggedIn" type="info" size="small" class="login-tag">需登录</el-tag>
            </el-menu-item>

            <el-menu-item
                index="setting"
                :disabled="!isLoggedIn"
            >
              <el-icon><Setting /></el-icon>
              <span>设置</span>
              <el-tag v-if="!isLoggedIn" type="info" size="small" class="login-tag">需登录</el-tag>
            </el-menu-item>
          </el-menu>

          <!-- 发表文章按钮 -->
          <div class="publish-section">
            <el-button
                type="primary"
                :disabled="!isLoggedIn"
                @click="goToPublish"
                class="publish-btn"
            >
              <el-icon><EditPen /></el-icon>
              发表文章
            </el-button>
            <div v-if="!isLoggedIn" class="login-tip">请先登录</div>
          </div>
        </el-card>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {ArrowDown, Clock, Document, EditPen, Search, Setting, Star} from "@element-plus/icons-vue"
import emitter from '@/utils/eventBus'  // 导入事件总线

const router = useRouter()
const route = useRoute()

// 响应式数据
const isLoggedIn = ref(false)
const username = ref('')
const userId = ref('')
const activeNav = ref('home')
const activeMenu = ref('')
const searchText = ref('')
const sidebarCollapsed = ref(false)
const userAvatar = ref('')  // 专门存储头像URL

// 计算属性：完整的头像URL
const avatarUrl = computed(() => {
  return getValidAvatar(userAvatar.value)
})

// 处理头像URL
const getValidAvatar = (avatar) => {
  try {
    const defaultAvatar = 'https://ts1.tc.mm.bing.net/th/id/OIP-C.XEkMn3xE4iFIzIB5DNE9fgHaHa?w=193&h=193&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.1&pid=3.1&rm=2&ucfimg=1'
    if (!avatar || typeof avatar !== 'string') return defaultAvatar
    const trimmedAvatar = avatar.trim()
    if (!trimmedAvatar) return defaultAvatar
    if (trimmedAvatar.startsWith('http://') || trimmedAvatar.startsWith('https://')) {
      return trimmedAvatar
    }
    console.warn('头像URL格式不正确:', trimmedAvatar)
    return defaultAvatar
  } catch (error) {
    console.error('处理头像URL时出错:', error)
    return 'https://ts1.tc.mm.bing.net/th/id/OIP-C.XEkMn3xE4iFIzIB5DNE9fgHaHa?w=193&h=193&c=8&rs=1&qlt=90&o=6&cb=ucfimg1&dpr=1.1&pid=3.1&rm=2&ucfimg=1'
  }
}

// 检查登录状态
const checkLoginStatus = () => {
  const token = localStorage.getItem('token')
  isLoggedIn.value = !!token

  // 从 localStorage 获取用户信息
  const userInfoStr = localStorage.getItem('userInfo')
  console.log('localStorage中的userInfo:', userInfoStr)
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      console.log('解析后的userInfo:', userInfo)
      username.value = userInfo.nickname || userInfo.username
      userId.value = userInfo.id
      userAvatar.value = userInfo.avatar || ''  // 设置头像
      console.log('头像URL:', userAvatar.value)
      console.log('头像URL类型:', typeof userAvatar.value)
      console.log('头像URL startsWith http:', userAvatar.value.startsWith('http'))
      console.log('用户信息加载成功:', { username: username.value, avatar: userAvatar.value })
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  } else {
    console.log('未找到用户信息')
  }
}

// 获取用户信息的工具函数
const getUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    return JSON.parse(userInfoStr)
  }
  return null
}

// 显示登录提示
const showLoginAlert = () => {
  ElMessage.warning('请先登录后再使用此功能！')
}

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 导航菜单选择
const handleNavSelect = (index) => {
  if (index === 'home') {
    goToHome()
  }
}

// 侧边栏菜单选择
const handleMenuSelect = (index) => {
  if (!isLoggedIn.value) {
    showLoginAlert()
    return
  }

  activeMenu.value = index
  switch (index) {
    case 'my-articles':
      goToMyArticles()
      break
    case 'favorites':
      goToFavorites()
      break
    case 'record':
      goToRecord()
      break
    case 'setting':
      goToSetting()
      break
  }
}

// 用户命令处理
const handleUserCommand = (command) => {
  if (command === 'logout') {
    logout()
  }
}

// 退出登录
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('nickname')
  isLoggedIn.value = false
  username.value = ''
  userId.value = ''
  userAvatar.value = ''  // 清空头像

  if (['/my-articles', '/favorites', '/record', '/setting', '/publish'].some(path => route.path.startsWith(path))) {
    router.push('/')
  }

  ElMessage.success('已退出登录')
}

// 路由跳转方法
const goToLogin = () => {
  router.push('/login')
}

const goToHome = () => {
  activeNav.value = 'home'
  activeMenu.value = ''
  router.push('/')
}

const goToCategory = (category) => {
  activeNav.value = 'category'
  activeMenu.value = ''
  router.push(`/category/${category}`)
}

const handleSearch = () => {
  if (searchText.value.trim()) {
    activeNav.value = ''
    activeMenu.value = ''
    router.push(`/search/${encodeURIComponent(searchText.value.trim())}`)
  }
}

const clearSearch = () => {
  if (route.path.startsWith('/search')) {
    router.push('/')
  }
}

const goToMyArticles = () => {
  if (!isLoggedIn.value) return
  router.push('/my-articles')
}

const goToFavorites = () => {
  if (!isLoggedIn.value) return
  router.push('/favorites')
}

const goToRecord = () => {
  if (!isLoggedIn.value) return
  router.push('/record')
}

const goToSetting = () => {
  if (!isLoggedIn.value) return
  router.push('/setting')
}

const goToPublish = () => {
  if (!isLoggedIn.value) {
    showLoginAlert()
    return
  }
  router.push('/publish')
}

// 更新激活状态
const updateActiveState = () => {
  const path = route.path

  // 检查是否需要登录
  const protectedPaths = ['/my-articles', '/favorites', '/record', '/setting', '/publish']
  if (protectedPaths.some(protectedPath => path.startsWith(protectedPath)) && !isLoggedIn.value) {
    ElMessage.warning('请先登录后再访问此页面！')
    router.push('/login')
    return
  }

  // 更新导航状态
  if (path === '/') {
    activeNav.value = 'home'
    activeMenu.value = ''
  } else if (path.startsWith('/category')) {
    activeNav.value = 'category'
    activeMenu.value = ''
  } else if (path.startsWith('/search')) {
    activeNav.value = ''
    activeMenu.value = ''
    const keyword = decodeURIComponent(path.split('/search/')[1] || '')
    searchText.value = keyword
  } else if (path.startsWith('/my-articles')) {
    activeNav.value = ''
    activeMenu.value = 'my-articles'
  } else if (path.startsWith('/favorites')) {
    activeNav.value = ''
    activeMenu.value = 'favorites'
  } else if (path.startsWith('/record')) {
    activeNav.value = ''
    activeMenu.value = 'record'
  } else if (path.startsWith('/setting')) {
    activeNav.value = ''
    activeMenu.value = 'setting'
  } else if (path.startsWith('/publish')) {
    activeNav.value = ''
    activeMenu.value = ''
  }
}

// 处理头像更新事件
const handleAvatarUpdated = (newAvatar) => {
  console.log('收到头像更新事件，新头像URL:', newAvatar)
  userAvatar.value = newAvatar

  // 同时更新本地存储
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      userInfo.avatar = newAvatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    } catch (e) {
      console.error('更新本地存储失败:', e)
    }
  }
}

// 处理storage变化事件（备用方案）
const handleStorageChange = (event) => {
  if (event.key === 'userInfo' && event.newValue) {
    try {
      const userInfo = JSON.parse(event.newValue)
      if (userInfo.avatar && userAvatar.value !== userInfo.avatar) {
        console.log('通过storage事件更新头像:', userInfo.avatar)
        userAvatar.value = userInfo.avatar
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 初始化
onMounted(() => {
  checkLoginStatus()
  updateActiveState()

  // 监听头像更新事件
  emitter.on('avatar-updated', handleAvatarUpdated)

  // 监听storage变化（作为备用方案）
  window.addEventListener('storage', handleStorageChange)

  console.log('Layout组件已挂载，事件监听已设置')
})

// 组件卸载时清理
onUnmounted(() => {
  // 移除事件监听
  emitter.off('avatar-updated', handleAvatarUpdated)
  window.removeEventListener('storage', handleStorageChange)

  console.log('Layout组件已卸载，事件监听已清理')
})

// 监听路由变化
watch(() => route.path, () => {
  checkLoginStatus()
  updateActiveState()
})
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 头部样式 */
.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  height: 60px;
  padding: 0;

  .header-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    padding: 0 20px;
  }
}

.logo {
  width: 100px;
  display: flex;
  align-items: center;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;

  .nav-menu {
    display: flex;
    gap: 40px;
    align-items: center;
  }

  .nav-item {
    color: white;
    cursor: pointer;
    padding: 8px 16px;
    font-size: 16px;
    transition: all 0.3s;
    border-radius: 4px;
    white-space: nowrap;

    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }

    &.active {
      color: #ffd04b;
      font-weight: 500;
    }
  }

  .category-item {
    position: relative;
  }
}

.category-dropdown {
  padding: 8px 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  & > div {
    padding: 10px 16px;
    color: #333;
    cursor: pointer;
    white-space: nowrap;

    &:hover {
      background-color: #f5f7fa;
      color: #409EFF;
    }
  }
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 250px;

  :deep(.el-input-group__append) {
    background-color: #409EFF;
    border-color: #409EFF;

    .el-button {
      color: white;

      &:hover {
        background-color: #337ecc;
      }
    }
  }
}

.login-btn {
  min-width: 80px;
}

/* 主容器样式 */
.main-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.content-main {
  height: calc(100vh - 60px); /* 改为固定计算 */
  min-height: calc(100vh - 60px); /* 添加最小高度 */
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  transition: margin-right 0.3s ease;
}

/* 侧边栏分隔条 */
.sidebar-handle {
  position: absolute;
  top: 50%;
  width: 20px;
  height: 60px;
  background: white;
  border: 1px solid #dcdfe6;
  border-right: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: right 0.3s ease;
  transform: translateY(-50%);

  &:hover {
    background: #f5f7fa;
  }

  .arrow-icon {
    font-size: 12px;
    color: #606266;
    transition: transform 0.3s ease;
  }

  .arrow-icon.collapsed {
    transform: rotate(180deg);
  }
}

/* 右侧侧边栏 */
.right-sidebar {
  background: white;
  border-left: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  overflow: hidden;
  height: calc(100vh - 60px);

  .profile-card {
    margin: 10px;
    border: none;

    .profile-header {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .profile-avatar {
      flex-shrink: 0;
    }

    .profile-info {
      flex: 1;

      .profile-name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 5px;
      }
    }
  }

  .menu-card {
    margin: 10px;
    border: none;
    height: calc(100% - 160px);
    display: flex;
    flex-direction: column;

    .sidebar-menu {
      border: none;
      flex: 1;

      .el-menu-item {
        height: 50px;
        line-height: 50px;
        margin-bottom: 5px;
        border-radius: 4px;

        &:hover {
          background-color: #f5f7fa;
        }

        &.is-active {
          background-color: #ecf5ff;
          color: #409EFF;
        }

        &.is-disabled {
          opacity: 0.6;
          cursor: not-allowed;

          &:hover {
            background-color: transparent;
          }
        }

        .login-tag {
          margin-left: auto;
          font-size: 10px;
          padding: 0 5px;
          height: 18px;
          line-height: 18px;
        }
      }
    }

    .publish-section {
      margin-top: auto;
      padding-top: 20px;
      border-top: 1px solid #e4e7ed;
      text-align: center;

      .publish-btn {
        width: 100%;
        height: 45px;

        &.is-disabled {
          cursor: not-allowed;
          opacity: 0.6;
        }
      }

      .login-tip {
        margin-top: 8px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 992px) {
  .search-input {
    width: 200px;
  }

  .nav-menu {
    gap: 20px !important;
  }

  .content-main {
    margin-right: 250px !important;
  }

  .sidebar-handle {
    right: 250px !important;
  }

  .right-sidebar {
    width: 250px !important;
  }
}

@media (max-width: 768px) {
  .nav-center {
    display: none;
  }

  .search-input {
    width: 150px;
  }

  .login-btn {
    min-width: 60px;
    padding: 8px 12px;
  }

  .content-main {
    margin-right: 200px !important;
  }

  .sidebar-handle {
    width: 15px;
    height: 50px;
    right: 200px !important;
  }

  .right-sidebar {
    position: fixed;
    top: 60px;
    right: 0;
    z-index: 1000;
    width: 200px !important;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  }

  .sidebar-handle {
    position: fixed;
    right: 200px !important;
  }
}

@media (max-width: 480px) {
  .search-input {
    width: 120px;
  }
}

.user-info {
  .user-dropdown {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    color: white;

    .username {
      font-size: 14px;
    }

    &:hover {
      color: #ffd04b;
    }
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .user-info .username {
    display: none;
  }
}
</style>