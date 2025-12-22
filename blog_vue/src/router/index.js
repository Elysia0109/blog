import { createRouter, createWebHistory } from 'vue-router'
import {ElMessage} from "element-plus";

const routes = [
    {
        path: '/',
        component: () => import('../views/Layout.vue'),
        children: [
            {
                path: '',
                component: () => import('../views/Home.vue'),  // 首页显示所有文章
                meta: { title: '首页' }
            },
            {
                path: 'category/:category',
                component: () => import('../views/Home.vue'),  // 使用Home组件显示分类文章
                meta: { title: '分类文章' }
            },
            {
                path: 'search/:keyword',
                component: () => import('../views/Home.vue'),  // 使用Home组件显示搜索结果
                meta: { title: '搜索结果' }
            },
            {
                path: 'my-articles',
                component: () => import('../views/MyArticles.vue'),
                meta: {
                    requiresAuth: true,
                    title: '我的文章'
                }
            },
            {
                path: 'favorites',
                component: () => import('../views/Home.vue'),  // 使用Home组件显示收藏夹
                meta: {
                    requiresAuth: true,
                    title: '我的收藏'
                }
            },
            {
                path: 'record',
                component: () => import('../views/Home.vue'),  // 使用Home组件显示浏览记录
                meta: {
                    requiresAuth: true,
                    title: '浏览记录'
                }
            },
            {
                path: 'setting',
                component: () => import('../views/Setting.vue'),
                meta: {
                    requiresAuth: true,
                    title: '设置'
                }
            },
            {
                path: 'publish',
                component: () => import('../views/PublishArticles.vue'),
                meta: {
                    requiresAuth: true,
                    title: '发布文章'
                }
            },
            {
                path: 'edit/:id',
                component: () => import('../views/PublishArticles.vue'),
                meta: {
                    requiresAuth: true,
                    title: '编辑文章'
                }
            }
        ]
    },
    {
        path: '/login',
        component: () => import('../views/Login.vue'),
        meta: {
            guestOnly: true,  // 只有未登录用户可以访问
            title: '登录'
        }
    },
    {
        path: '/register',
        component: () => import('../views/Register.vue'),
        meta: {
            guestOnly: true,  // 只有未登录用户可以访问
            title: '注册'
        }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const guestOnly = to.matched.some(record => record.meta.guestOnly);

    // 设置页面标题
    if (to.meta.title) {
        document.title = `${to.meta.title} - 博客系统`;
    }

    // 需要登录但未登录
    if (requiresAuth && !token) {
        ElMessage.warning('请先登录！');
        next('/login');
        return;
    }

    // 已登录但访问 guestOnly 页面（如登录/注册页）
    if (guestOnly && token) {
        ElMessage.info('您已登录，将跳转到首页');
        next('/');
        return;
    }

    // 其他情况正常导航
    next();
});


export default router