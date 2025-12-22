import axios from "axios";
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
    baseURL: 'http://localhost:8080/api/',
    timeout: 10000,
});

// 请求拦截器 - 增强版本
request.interceptors.request.use(config => {
    console.log('[Request] URL:', config.url)
    console.log('[Request] Method:', config.method)

    // 获取并清理token
    let token = localStorage.getItem('token')

    // 调试信息
    console.log('[Request] Raw token from localStorage:', token)
    console.log('[Request] Token type:', typeof token)

    // 清理无效的token
    if (token === 'undefined' || token === 'null' || token === '' || token === undefined) {
        console.warn('[Request] Invalid token detected, clearing...')
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('userInfo')
        token = null
    }

    // 只有在有有效token时才设置Authorization头
    if (token && token.length > 10) { // JWT token通常较长
        config.headers.Authorization = `Bearer ${token}`
        console.log('[Request] Authorization header set, length:', token.length)
        console.log('[Request] Authorization preview:', token.substring(0, 30) + '...')
    } else {
        console.warn('[Request] No valid token available')
        // 清除可能存在的无效header
        delete config.headers.Authorization
    }

    // 打印最终headers
    console.log('[Request] Final headers:', {
        ...config.headers,
        Authorization: config.headers.Authorization ? '[HIDDEN]' : 'Not set'
    })

    return config
}, error => {
    console.error('[Request Interceptor Error]:', error)
    return Promise.reject(error)
});

// 响应拦截器 - 增强版本
request.interceptors.response.use(response => {
    console.log('[Response] Status:', response.status)
    console.log('[Response] URL:', response.config.url)
    console.log('[Response] Data:', response.data)

    const res = response.data

    // 如果返回的是文件流等特殊类型，直接返回
    if (response.config.responseType === 'blob' ||
        response.config.responseType === 'arraybuffer') {
        return res
    }

    // 处理业务逻辑状态码
    if (res.code !== undefined && res.code !== 200) {
        console.warn('[Response] Business error:', res)

        // 401未授权
        if (res.code === 401) {
            ElMessage.error(res.message || '登录已过期，请重新登录')

            // 清除本地存储
            localStorage.removeItem('token')
            localStorage.removeItem('userId')
            localStorage.removeItem('userInfo')

            // 跳转到登录页
            setTimeout(() => {
                if (router.currentRoute.value.path !== '/login') {
                    router.push('/login')
                }
            }, 1500)

            return Promise.reject(new Error(res.message || 'Unauthorized'))
        }

        // 其他业务错误
        ElMessage.error(res.message || '操作失败')
        return Promise.reject(new Error(res.message || 'Operation failed'))
    }

    return res
}, error => {
    console.error('[Response Interceptor Error]:', error)

    if (error.response) {
        // 服务器返回了错误状态码
        console.error('[Response] Error details:', {
            status: error.response.status,
            statusText: error.response.statusText,
            data: error.response.data,
            url: error.response.config?.url,
            method: error.response.config?.method
        })

        // 处理HTTP状态码
        switch (error.response.status) {
            case 401:
                ElMessage.error('未授权，请重新登录')
                localStorage.removeItem('token')
                localStorage.removeItem('userId')
                localStorage.removeItem('userInfo')

                // 避免重复跳转
                if (router.currentRoute.value.path !== '/login') {
                    setTimeout(() => {
                        router.push('/login')
                    }, 1000)
                }
                break

            case 403:
                ElMessage.error('权限不足，拒绝访问')
                break

            case 404:
                ElMessage.error('请求的资源不存在')
                break

            case 500:
                ElMessage.error('服务器内部错误，请稍后重试')
                break

            default:
                ElMessage.error(`请求失败 (${error.response.status})`)
        }

        // 返回错误信息，方便组件处理
        return Promise.reject({
            code: error.response.status,
            message: error.response.data?.message || error.response.statusText,
            data: error.response.data
        })

    } else if (error.request) {
        // 请求已发送但没有收到响应
        console.error('[Response] No response received:', error.request)
        ElMessage.error('网络连接异常，请检查网络设置')

    } else {
        // 请求配置错误
        console.error('[Response] Request config error:', error.message)
        ElMessage.error('请求配置错误: ' + error.message)
    }

    return Promise.reject(error)
});

// 添加工具方法
request.getToken = function() {
    const token = localStorage.getItem('token')
    return token && token !== 'undefined' && token !== 'null' ? token : null
}

request.clearAuth = function() {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('userInfo')
    console.log('[Auth] Cleared authentication data')
}

// 测试token是否有效
request.isAuthenticated = function() {
    const token = this.getToken()
    return !!token && token.length > 10
}

export default request