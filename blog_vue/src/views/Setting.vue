<template>
  <div class="setting">
    <div class="page-title">设置</div>

    <div class="settings-content">
      <!-- 头像设置 -->
      <div class="setting-card">
        <div class="card-title">头像设置</div>
        <div class="avatar-box">
          <img :src="userInfo.avatar" class="avatar" alt="头像">
          <div class="avatar-btns">
            <el-button type="primary" @click="uploadAvatar" :loading="uploadingAvatar">
              <el-icon><Upload /></el-icon>选择头像
            </el-button>
            <input
                type="file"
                ref="fileInput"
                @change="handleAvatarChange"
                accept="image/*"
                hidden
            >
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="setting-card">
        <div class="card-title">基本信息</div>
        <div class="form-item">
          <label>用户名：</label>
          <el-input v-model="userInfo.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>昵称：</label>
          <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
        </div>
        <el-button type="primary" @click="saveBasicInfo" :loading="savingBasic">保存</el-button>
      </div>

      <!-- 密码修改 -->
      <div class="setting-card">
        <div class="card-title">修改密码</div>
        <div class="form-item">
          <label>旧密码：</label>
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
          />
        </div>
        <div class="form-item">
          <label>新密码：</label>
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
          />
        </div>
        <div class="form-item">
          <label>确认密码：</label>
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入"
          />
        </div>
        <el-button type="primary" @click="savePassword" :loading="changingPassword">修改密码</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request from '@/utils/request' // 引入统一的request实例

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  nickname: '',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 加载状态
const uploadingAvatar = ref(false)
const savingBasic = ref(false)
const changingPassword = ref(false)

const fileInput = ref(null)

// 加载用户信息
const loadUserInfo = () => {
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      userInfo.id = user.id || ''
      userInfo.username = user.username || ''
      userInfo.nickname = user.nickname || user.username || ''
      userInfo.avatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 选择头像
const uploadAvatar = () => {
  fileInput.value.click()
}

// 处理头像上传 - 使用 request.js
const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  uploadingAvatar.value = true

  try {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('avatar', file)

    // 使用request.js上传头像
    const res = await request({
      url: '/user/avatar',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data' // 覆盖默认的Content-Type
      }
    })

    if (res.code === 200) {
      // 更新本地头像
      userInfo.avatar = res.data.avatar || res.avatar || userInfo.avatar

      // 更新本地存储的用户信息
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        const user = JSON.parse(storedUser)
        user.avatar = userInfo.avatar
        localStorage.setItem('userInfo', JSON.stringify(user))
      }

      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
  } finally {
    uploadingAvatar.value = false
    // 清空input，方便重复选择
    e.target.value = ''
  }
}

// 保存基本信息 - 使用 request.js
const saveBasicInfo = async () => {
  if (!userInfo.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }

  savingBasic.value = true

  try {
    const updateData = {
      id: userInfo.id,
      username: userInfo.username,
      nickname: userInfo.nickname
    }

    // 使用request.js更新用户信息
    const res = await request({
      url: '/user/profile',
      method: 'PUT',
      data: updateData
    })

    if (res.code === 200) {
      // 更新本地存储的用户信息
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        const user = JSON.parse(storedUser)
        user.username = userInfo.username
        user.nickname = userInfo.nickname
        localStorage.setItem('userInfo', JSON.stringify(user))
        localStorage.setItem('username', userInfo.username)
        localStorage.setItem('nickname', userInfo.nickname)
      }

      ElMessage.success('基本信息已保存')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存基本信息失败:', error)
    ElMessage.error('保存失败，请检查网络连接')
  } finally {
    savingBasic.value = false
  }
}

// 修改密码 - 使用 request.js
const savePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.error('请输入旧密码')
    return
  }

  if (!passwordForm.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('密码至少6位')
    return
  }

  changingPassword.value = true

  try {
    const passwordData = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }

    // 使用request.js修改密码
    const res = await request({
      url: '/user/password',
      method: 'PUT',
      data: passwordData
    })

    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败，请重试')
  } finally {
    changingPassword.value = false
  }
}

// 初始化
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.setting {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
  color: #333;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.avatar-box {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eee;
}

.avatar-btns {
  display: flex;
  gap: 10px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: inline-block;
  width: 80px;
  font-size: 14px;
  color: #666;
}

.form-item .el-input {
  width: 300px;
}

.el-button {
  width: 120px;
}
</style>