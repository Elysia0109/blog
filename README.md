# 个人博客系统（Blog System）

一个前后端分离的个人博客系统，支持用户注册登录、文章发布与管理、分类浏览、搜索、点赞收藏、评论互动以及个人设置（头像上传等）。

## 技术栈

| 层级 | 技术 |
| ---- | ---- |
| 前端 | Vue 3、Vite 7、Vue Router 4、Element Plus、Axios、mitt |
| 后端 | Spring Boot 4.0、Java 17、MyBatis、Lombok |
| 数据库 | MySQL |
| 认证 | JWT（jjwt 0.11.5）、Spring Security Crypto（BCrypt 密码加密） |
| 存储 | 阿里云 OSS（头像等文件上传） |

## 项目结构

```
blog-final2
├── blog_vue/            # 前端（Vue 3 + Vite）
│   ├── src/
│   │   ├── router/      # 路由与导航守卫
│   │   ├── utils/       # Axios 封装、事件总线
│   │   └── views/       # 页面组件
│   ├── package.json
│   └── vite.config.js
└── springboot/          # 后端（Spring Boot 4）
    └── src/main/
        ├── java/com/niit/springboot/
        │   ├── config/      # 跨域、OSS、Web 配置
        │   ├── controller/  # 接口层（文章/评论/登录/设置/用户）
        │   ├── dto/         # 请求/响应对象
        │   ├── entity/      # 实体类
        │   ├── mapper/      # MyBatis Mapper 接口
        │   ├── service/     # 业务层接口与实现
        │   └── utils/       # JWT、OSS、密码工具类
        └── resources/
            ├── mapper/      # MyBatis XML
            └── application.yml
```

## 功能特性

### 用户与认证
- 用户注册 / 登录
- JWT Token 鉴权（`Bearer` 请求头），后端 `JwtInterceptor` 拦截解析
- 密码使用 BCrypt 加密存储

### 文章
- 发布 / 编辑 / 删除文章
- 我的文章列表（仅作者可见操作）
- 文章置顶 / 取消置顶
- 草稿 / 已发布 / 已删除状态管理

### 浏览与检索
- 首页文章列表（分页）
- 按分类浏览
- 关键词搜索
- 文章详情（自动记录浏览记录、增加阅读量）
- 热门文章

### 互动
- 点赞 / 取消点赞
- 收藏 / 取消收藏
- 评论 / 删除评论

### 个人设置
- 查看 / 修改个人信息（昵称、邮箱等）
- 头像上传（阿里云 OSS）
- 修改密码

## 环境要求

- Node.js `^20.19.0 || >=22.12.0`
- JDK 17
- Maven 3.x
- MySQL 8.x

## 快速开始

### 1. 配置数据库

在 MySQL 中创建数据库（默认库名 `niit_pro`），并创建所需的表结构（`user`、`article`、`category`、`comment`、`favorite`、`browse_record`、`like` 等表）。

### 2. 启动后端

编辑 `springboot/src/main/resources/application.yml`，按需修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/niit_pro?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

如需使用头像上传（阿里云 OSS），需配置环境变量（或修改配置）：

```bash
export ALIBABA_CLOUD_ACCESS_KEY_ID="你的AccessKeyId"
export ALIBABA_CLOUD_ACCESS_KEY_SECRET="你的AccessKeySecret"
```

启动后端：

```bash
cd springboot
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`，接口上下文路径为 `/api`。

### 3. 启动前端

```bash
cd blog_vue
npm install
npm run dev
```

前端默认通过 `http://localhost:8080/api/` 访问后端（可在 `blog_vue/src/utils/request.js` 中修改 `baseURL`）。

启动后访问 Vite 输出的本地地址（通常为 `http://localhost:5173`）即可。

## 接口概览

后端统一响应结构 `ApiResponse{code, message, data}`，成功时 `code=200`。

| 模块 | 方法 | 路径 | 说明 |
| ---- | ---- | ---- | ---- |
| 认证 | POST | `/api/auth/login` | 登录 |
| 用户 | POST | `/api/users/register` | 注册 |
| 用户 | GET | `/api/users/me` | 获取当前用户信息 |
| 用户 | PUT | `/api/users/me` | 更新用户信息 |
| 文章 | GET | `/api/articles` | 文章列表（支持分类/搜索/收藏/浏览记录） |
| 文章 | POST | `/api/articles` | 创建文章 |
| 文章 | GET | `/api/articles/{id}` | 文章详情 |
| 文章 | PUT | `/api/articles/{id}` | 更新文章 |
| 文章 | DELETE | `/api/articles/{id}` | 删除文章 |
| 文章 | POST | `/api/articles/{id}/like` | 点赞 |
| 文章 | POST | `/api/articles/{id}/collect` | 收藏 |
| 文章 | POST | `/api/articles/{id}/toggle-top` | 置顶/取消置顶 |
| 评论 | POST | `/api/comments` | 添加评论 |
| 评论 | GET | `/api/comments/article/{articleId}` | 获取文章评论 |
| 设置 | POST | `/api/settings/avatar` | 上传头像 |
| 设置 | PUT | `/api/settings/password` | 修改密码 |

> 以上为部分接口，完整接口见 `springboot/src/main/java/com/niit/springboot/controller/` 目录。

## 说明

- 前端 `request.js` 中的后端地址 `baseURL` 为硬编码，部署时请根据实际情况调整。
- 阿里云 OSS 的 AccessKey 已通过环境变量方式注入，请勿将明文密钥提交到代码仓库。
- 数据库建表 SQL 未包含在仓库中，需自行创建与实体类（`entity/` 目录）对应的表结构。
