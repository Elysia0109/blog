<template>
  <div class="register-container">
    <div class="register-box">
      <div style="padding: 40px 30px; background-color: white; margin-left: 100px; border-radius: 5px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.2)">
        <el-form ref="formRef" :model="data.form" style="width: 350px">
          <div style="margin-bottom: 30px; font-size: 24px; color: #409EFF; font-weight: bold; text-align: center">用户注册</div>

          <!-- 用户名 -->
          <el-form-item>
            <el-input
                size="large"
                v-model="data.form.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                clearable
            ></el-input>
          </el-form-item>

          <!-- 账户 -->
          <el-form-item>
            <el-input
                size="large"
                v-model="data.form.account"
                placeholder="请输入账户（用于登录）"
                prefix-icon="UserFilled"
                clearable
            ></el-input>
          </el-form-item>

          <!-- 密码 -->
          <el-form-item>
            <el-input
                size="large"
                v-model="data.form.password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                type="password"
                show-password
            ></el-input>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item>
            <el-input
                size="large"
                v-model="data.form.confirmPassword"
                placeholder="请确认密码"
                prefix-icon="Lock"
                type="password"
                show-password
            ></el-input>
          </el-form-item>

          <div style="margin-bottom: 20px">
            <el-button
                type="primary"
                style="width: 100%"
                @click="register"
                :loading="data.loading"
            >
              {{ data.loading ? '注册中...' : '注册' }}
            </el-button>
          </div>

          <div style="text-align: center;">
            <a style="color: #409EFF; text-decoration: none;" href="/login">已有账号？去登录</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue"
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const data = reactive({
  form: {
    username: "",
    account: "",
    password: "",
    confirmPassword: ""
  },
  loading: false
})

// 表单验证
function validateForm() {
  if (!data.form.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }

  if (!data.form.account.trim()) {
    ElMessage.error('请输入账户')
    return false
  }

  if (!data.form.password) {
    ElMessage.error('请输入密码')
    return false
  }

  if (data.form.password.length < 6) {
    ElMessage.error('密码至少6位')
    return false
  }

  if (data.form.password !== data.form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return false
  }

  return true
}

async function register() {
  // 验证表单
  if (!validateForm()) {
    return
  }

  try {
    data.loading = true

    const res = await request({
      url: "/users/register",
      method: "POST",
      data: {
        username: data.form.username,
        account: data.form.account,
        password: data.form.password
      },
    })

    if (res.code === 200) {
      ElMessage.success('注册成功！')

      // 注册成功后跳转到登录页面
      setTimeout(() => {
        router.push('/login')
      }, 1000)

    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error("注册失败:", error)

    // 处理账户重复的错误
    if (error.response?.data?.message?.includes('账户已存在') ||
        error.response?.data?.message?.includes('重复')) {
      ElMessage.error('该账户已存在，请更换其他账户')
    } else {
      ElMessage.error("注册失败：" + (error.response?.data?.message || error.message))
    }
  } finally {
    data.loading = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  overflow: hidden;
  background-image: url("https://img-s.msn.cn/tenant/amp/entityid/BB1msDBQ?w=0&h=0&q=60&m=6&f=jpg&u=t");
  background-size: cover;
  background-position: 0-50px;
}
.register-box{
  width: 50%;
  height: 100%;
  right: 0;
  position: absolute;
  display: flex;
  align-items: center;
}
</style>