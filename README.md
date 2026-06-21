<<<<<<< HEAD
# 个人AI博客 (Personal AI Blog)

Vue 3 + Spring Boot 构建的个人技术博客，集成了 AI 智能搜索与问答。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API), Vue Router 4, Pinia, Element Plus, Vite 5 |
| 后端 | Spring Boot 3.2, MyBatis-Plus 3.5.5, JWT |
| 数据库 | MySQL 8.0 |
| AI | DeepSeek API + 本地 TF-IDF 向量检索, SSE 流式响应 |
| 渲染 | Marked.js + highlight.js 代码高亮 |

## 功能

- 文章管理 (CRUD, 分类, 标签, Markdown 编辑器, 图片上传)
- 访客前台 (文章列表, 详情, 目录导航, 代码高亮, 点赞, 评论)
- AI 智能搜索 (TF-IDF 本地向量化 + DeepSeek 对话, SSE 流式输出)
- 3 角色权限 (Admin / Guest / AI Service)
- 友链管理, 关于页编辑
- 暗色模式, SEO Meta 标签, RSS, Sitemap

## 快速开始

### 1. 数据库

创建 MySQL 数据库 `blog`，执行 `backend/src/main/resources/schema.sql` 初始化表结构。

### 2. 后端

```bash
cd backend

# 复制配置模板，填入真实信息
cp src/main/resources/application-template.yml src/main/resources/application.yml

# 修改 application.yml 中的数据库密码和 DeepSeek API Key

# 启动
mvnw spring-boot:run
# 或
mvn spring-boot:run
```

### 3. 前端

```bash
cd frontend

# 复制环境变量配置
cp .env.example .env   # 或直接创建 .env 文件

npm install
npm run dev
```

### 4. 访问

- 前台: `http://localhost:5173/visitor`
- 后台: `http://localhost:5173/admin`
- 默认管理员: `admin` / `admin123`

## 项目结构

```
blog/
├── backend/
│   ├── src/main/java/com/personalblog/
│   │   ├── config/          # Spring 配置
│   │   ├── controller/      # 控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── interceptor/     # JWT 拦截器
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── service/         # 业务逻辑
│   │   └── util/            # 工具类 (JWT, Captcha)
│   └── src/main/resources/
│       ├── application-template.yml  # 配置模板
│       └── schema.sql                # 建表 SQL
└── frontend/
    └── src/
        ├── api/              # API 请求封装
        ├── components/       # 公共组件
        ├── router/           # 路由配置
        ├── stores/           # Pinia 状态管理
        ├── styles/           # 全局样式
        └── views/            # 页面组件
            ├── admin/        # 后台管理页
            └── public/       # 前台访客页
```

## 配置说明

后端配置文件 `application.yml` 不会提交到仓库，请从模板复制：

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.password` | MySQL 密码 |
| `jwt.secret` | JWT 签名密钥 |
| `ai.api-key` | DeepSeek API Key |

**注意：** 真实 `application.yml` 已在 `.gitignore` 中排除，不会提交到 Git。
=======
# AI-blog
基于 Vue3 + Spring Boot 的个人技术博客，集成 AI 智能搜索（RAG），支持文章管理、分类标签、Markdown 撰写。访客可用自然语言搜索博客内容，流式输出答案并显示引用来源。
>>>>>>> f1aeae5222da5dcb06de1650504affaa5ba50a7c
