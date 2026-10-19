<template>
  <div class="setting">
    <div class="page-title">设置</div>

    <div class="settings-content">
      <!-- 头像设置 -->
      <div class="setting-card">
        <div class="card-title">头像设置</div>
        <div class="avatar-box">
          <el-image
              :src="avatarUrl"
              :preview-src-list="[avatarUrl]"
              fit="cover"
              class="avatar"
              @error="handleAvatarLoadError"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="avatar-btns">
            <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeAvatarUpload"
                accept="image/*"
                name="file"
            >
              <el-button type="primary" :loading="uploadingAvatar">
                <el-icon><Upload /></el-icon>上传头像
              </el-button>
            </el-upload>
            <div class="avatar-tips">
              支持 JPG、PNG 格式，大小不超过 5MB
            </div>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="setting-card">
        <div class="card-title">基本信息</div>
        <div class="form-item">
          <label>昵称：</label>
          <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
        </div>
        <div class="form-item">
          <label>邮箱：</label>
          <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
        </div>
        <el-button
            type="primary"
            @click="saveBasicInfo"
            :loading="savingBasic"
        >
          保存基本信息
        </el-button>
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
              show-password
          />
        </div>
        <div class="form-item">
          <label>新密码：</label>
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
          />
        </div>
        <div class="form-item">
          <label>确认密码：</label>
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入"
              show-password
          />
        </div>
        <el-button
            type="primary"
            @click="savePassword"
            :loading="changingPassword"
        >
          修改密码
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import emitter from '@/utils/eventBus'  // 导入事件总线

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = reactive({
  id: '',
  nickname: '',
  email: '',
  avatar: ''
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

// 计算属性：头像URL
const avatarUrl = computed(() => {
  try {
    // 确保头像URL是字符串类型
    const avatar = userInfo.avatar
    if (!avatar || typeof avatar !== 'string') {
      return defaultAvatar
    }
    
    // 去除首尾空白字符
    const trimmedAvatar = avatar.trim()
    if (!trimmedAvatar) {
      return defaultAvatar
    }
    
    // 如果已经是完整URL，直接返回
    if (trimmedAvatar.startsWith('http://') || trimmedAvatar.startsWith('https://')) {
      return trimmedAvatar
    }
    
    // 如果是相对路径或其他格式，使用默认头像
    console.warn('头像URL格式不正确:', trimmedAvatar)
    return defaultAvatar
  } catch (error) {
    console.error('处理头像URL时出错:', error)
    return defaultAvatar
  }
})

// 上传配置
const uploadAction = computed(() => {
  const baseUrl = 'http://localhost:8080'  // 根据实际环境修改
  return `${baseUrl}/api/settings/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': `Bearer ${token}`
  }
})

// 头像加载失败处理
const handleAvatarLoadError = () => {
  console.log('头像加载失败，使用默认头像')
}

// 上传前的验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return false
  }

  uploadingAvatar.value = true
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  uploadingAvatar.value = false
  console.log('上传响应:', response)

  if (response.code === 200 && response.data) {
    userInfo.avatar = response.data.avatar
    ElMessage.success('头像上传成功！')

    // 更新本地存储的用户信息
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        const localUserInfo = JSON.parse(userInfoStr)
        localUserInfo.avatar = userInfo.avatar
        const updatedUserInfoStr = JSON.stringify(localUserInfo)
        localStorage.setItem('userInfo', updatedUserInfoStr)

        // 发送头像更新事件给其他组件
        emitter.emit('avatar-updated', userInfo.avatar)

        // 同时触发storage事件，作为备用方案
        window.dispatchEvent(new StorageEvent('storage', {
          key: 'userInfo',
          newValue: updatedUserInfoStr,
          oldValue: userInfoStr
        }))

      } catch (e) {
        console.error('更新本地存储失败:', e)
      }
    }
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 头像上传失败
const handleUploadError = (error) => {
  uploadingAvatar.value = false
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败')
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    console.log('开始加载用户信息...')
    const res = await request.get('/settings/profile')
    console.log('用户信息响应:', res)

    if (res.code === 200 && res.data) {
      Object.assign(userInfo, res.data)
      console.log('用户信息加载成功:', userInfo)
      console.log('头像URL:', userInfo.avatar)
      console.log('头像URL类型:', typeof userInfo.avatar)
      console.log('头像URL长度:', userInfo.avatar.length)
      console.log('头像URL startsWith http:', userInfo.avatar.startsWith('http'))
    } else {
      console.error('加载用户信息失败:', res.message)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 保存基本信息
const saveBasicInfo = async () => {

  savingBasic.value = true
  try {
    const res = await request.put('/settings/profile', {
      nickname: userInfo.nickname,
      email: userInfo.email
    })

    if (res.code === 200) {
      ElMessage.success('基本信息已保存')

      // 更新本地存储
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const localUserInfo = JSON.parse(userInfoStr)
          localUserInfo.nickname = userInfo.nickname
          localUserInfo.email = userInfo.email
          localStorage.setItem('userInfo', JSON.stringify(localUserInfo))
        } catch (e) {
          console.error('更新本地存储失败:', e)
        }
      }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存基本信息失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingBasic.value = false
  }
}

// 修改密码
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
    const res = await request.put('/settings/password', null, {
      params: {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }
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
  console.log('Setting组件已挂载')
  loadUserInfo()
})
</script>

<style scoped>
.setting {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
  text-align: center;
  color: #333;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.setting-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 25px;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
}

.avatar-box {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid #eee;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  overflow: hidden;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
}

.avatar-btns {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.form-item {
  margin-bottom: 25px;
}

.form-item label {
  display: inline-block;
  width: 80px;
  font-size: 14px;
  color: #666;
  margin-right: 10px;
  text-align: right;
}

.form-item .el-input {
  width: 300px;
}

.el-button {
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .avatar-box {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .form-item label {
    display: block;
    width: 100%;
    text-align: left;
    margin-bottom: 8px;
  }

  .form-item .el-input {
    width: 100%;
  }
}
</style>