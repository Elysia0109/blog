<template>
  <div class="login-container">
    <div class="login-box">
      <div style="padding: 50px 30px;background-color: white;margin-left: 100px;border-radius: 5px;box-shadow: 0 0 10px rgba(0, 0, 0, 0.2)">
        <el-form ref="formRef" :model="data.form" style="width: 350px">
          <div style="margin-bottom: 30px; font-size: 24px;color: #409EFF;font-weight: bold;text-align: center">欢迎登录</div>
          <el-form-item>
            <el-input size="large" v-model="data.form.username" placeholder="请输入账号" prefix-icon="User"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input size="large" v-model="data.form.password" placeholder="请输入密码" prefix-icon="Lock" type="password" show-password></el-input>
          </el-form-item>
          <div style="margin-bottom: 20px">
            <el-button type="primary" style="width: 100%" @click="login" :loading="data.loading">
              {{ data.loading ? '登录中...' : '登录' }}
            </el-button>
          </div>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <a style="color: #409EFF; text-decoration: none;" href="/">去首页</a>
            <a style="color: #409EFF; text-decoration: none;" href="/register">还没有账号？请注册</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue"
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const data = reactive({
  form: {
    username: "",
    password: ""
  },
  loading: false
})

// Login.vue - 修改login方法
async function login() {
  try {
    data.loading = true;

    console.log('发送登录请求:', data.form);

    const res = await request({
      url: "/auth/login",
      method: "POST",
      data: {
        account: data.form.username,
        password: data.form.password
      }
    });

    console.log('登录响应:', res);

    if (res.code === 200 && res.data) {
      // 保存token - 从res.data中获取
      const loginData = res.data;
      const token = loginData.token;

      if (!token) {
        console.error('响应中没有找到token字段', res);
        ElMessage.error('登录响应异常：未获取到token');
        return;
      }

      localStorage.setItem("token", token);
      console.log('Token已保存:', token.substring(0, 30) + '...');

      // 保存用户信息 - 从res.data.userInfo中获取
      const userInfo = loginData.userInfo || {};
      if (userInfo && userInfo.id) {
        localStorage.setItem("userInfo", JSON.stringify(userInfo));
        localStorage.setItem("userId", userInfo.id);
        localStorage.setItem("username", userInfo.username || data.form.username);
        localStorage.setItem("nickname", userInfo.nickname || userInfo.username || data.form.username);
      } else {
        console.warn('响应中没有用户信息', res);
        // 如果没有用户信息，至少保存用户名
        localStorage.setItem("userId", data.form.username); // 临时用用户名作为ID
        localStorage.setItem("username", data.form.username);
      }

      ElMessage.success("登录成功！");

      // 跳转到首页
      setTimeout(() => {
        router.push('/');
      }, 500);

    } else {
      console.error('登录失败:', res);
      ElMessage.error(res.message || '登录失败');
    }
  } catch(error) {
    console.error("登录失败:", error);
    // 更详细的错误信息
    if (error.response) {
      console.error("响应数据:", error.response.data);
      console.error("状态码:", error.response.status);
      ElMessage.error(error.response.data?.message || "登录失败");
    } else if (error.request) {
      console.error("请求已发送但无响应:", error.request);
      ElMessage.error("网络连接失败，请检查网络");
    } else {
      console.error("请求配置错误:", error.message);
      ElMessage.error("登录请求失败: " + error.message);
    }
  } finally {
    data.loading = false;
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  background-image: url("https://www.bizhigq.com/pc-img/2023-05/g3039.jpg");
  background-size: cover;
  background-position: 0-50px;
}
.login-box{
  width: 50%;
  height: 100%;
  right: 0;
  position: absolute;
  display: flex;
  align-items: center;
}
</style>