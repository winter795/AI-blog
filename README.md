# 个人 AI 博客

基于 Vue 3 + Spring Boot 的个人技术博客，集成 RAG 智能搜索。访客可用自然语言提问，AI 从博客文章中检索并流式生成答案。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API), Vue Router 4, Pinia, Element Plus, Vite 5 |
| 后端 | Spring Boot 3.2, MyBatis-Plus 3.5, JWT (jjwt 0.12) |
| 数据库 | MySQL 8.0 |
| AI | DeepSeek Chat API + Jieba 分词 + TF-IDF 向量检索, SSE 流式响应 |
| 安全 | BCrypt 密码加密, JWT 认证, CORS 白名单, 参数校验 |
| 渲染 | Marked.js + highlight.js 代码高亮 |

## 功能

**访客前台**
- 文章列表（分页、分类/标签筛选、关键词搜索）
- 文章详情（Markdown 渲染、代码高亮、目录导航）
- AI 智能问答（自然语言提问，流式输出，标注引用来源）
- 评论（提交后审核展示）、点赞
- 暗色模式、RSS 订阅、Sitemap、SEO Meta

**管理后台**
- 文章 CRUD（Markdown 编辑器 + 实时预览 + 图片上传）
- AI 辅助写作（一键生成摘要、推荐标签）
- 分类/标签管理、评论审核、友链管理、站点设置

## 环境要求

- **JDK 17+**
- **Maven 3.6+**（或使用项目自带的 `mvnw`）
- **MySQL 8.0+**
- **Node.js 18+**

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行建表脚本：

```bash
mysql -u root -p blog < backend/src/main/resources/schema.sql
```

脚本会创建所有表并插入默认管理员账号。

### 2. 配置后端

```bash
cd backend

# 从模板复制配置文件
cp src/main/resources/application-template.yml src/main/resources/application.yml
```

编辑 `application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    username: root
    password: "你的数据库密码"   # 或设置环境变量 DB_PASSWORD

jwt:
  secret: "你的JWT密钥"         # 或设置环境变量 JWT_SECRET

ai:
  api-key: "你的DeepSeek Key"   # 或设置环境变量 AI_API_KEY
```

> 所有敏感配置都支持 `${ENV_VAR:default}` 语法，生产环境建议用环境变量覆盖，避免密钥写死在文件里。

### 3. 启动后端

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 4. 配置并启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理将 `/api` 请求转发到后端 `8080` 端口。

### 5. 访问

| 地址 | 说明 |
|------|------|
| `http://localhost:5173/visitor` | 博客前台 |
| `http://localhost:5173/admin` | 管理后台 |
| `http://localhost:5173/visitor/ai` | AI 搜索页 |

**默认管理员**：`admin` / `admin123`

## 环境变量

所有配置项均可通过环境变量覆盖，无需修改 `application.yml`：

| 环境变量 | 说明 | 默认值 |
|----------|------|--------|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | — |
| `JWT_SECRET` | JWT 签名密钥（生产环境务必修改） | — |
| `AI_API_KEY` | DeepSeek API Key | — |
| `CORS_ORIGINS` | 允许的前端跨域来源 | `http://localhost:5173` |
| `SITE_DOMAIN` | 站点域名（Sitemap / RSS 使用） | `http://localhost:5173` |
| `SERVER_PORT` | 后端端口 | `8080` |

示例（macOS / Linux）：

```bash
export DB_PASSWORD=mypassword
export JWT_SECRET=$(openssl rand -base64 32)
export AI_API_KEY=sk-xxxxxxxx
./mvnw spring-boot:run
```

## 项目结构

```
blog/
├── backend/
│   ├── src/main/java/com/personalblog/
│   │   ├── config/              # Spring 配置 (CORS, BCrypt, MyBatis, AI)
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 请求/响应 DTO
│   │   ├── entity/              # 数据库实体
│   │   ├── interceptor/         # JWT 登录拦截器
│   │   ├── mapper/              # MyBatis Mapper
│   │   ├── vo/                  # 前端视图对象 (隐藏内部字段)
│   │   ├── service/
│   │   │   ├── AiChatService        # SSE 流式对话 + RAG 上下文构建
│   │   │   ├── AiSearchService      # Jieba 分词 + TF-IDF 检索引擎
│   │   │   └── AiAssistantService   # AI 摘要 / 标签 / 搜索建议
│   │   └── util/                # JWT 工具、Jieba 分词工具
│   └── src/main/resources/
│       ├── application-template.yml  # 配置模板（可提交）
│       └── schema.sql                # 建表 + 默认数据
│
└── frontend/
    └── src/
        ├── api/                  # Axios 请求封装
        ├── components/           # 公共组件 (Sidebar, Comment, EmptyState)
        ├── router/               # 路由 + 导航守卫
        ├── stores/               # Pinia (用户状态, SEO)
        ├── styles/               # 全局 CSS 变量 + 暗色模式
        └── views/
            ├── admin/            # 后台页面 (文章/分类/标签/评论/友链/设置)
            └── public/           # 前台页面 (首页/详情/AI搜索/关于/友链)
```

## API 概览

### 公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/public/article/page` | 文章分页列表 |
| GET | `/api/public/article/{id}` | 文章详情 |
| GET | `/api/public/article/latest` | 最新文章 |
| GET | `/api/public/article/hot` | 热门文章 |
| GET | `/api/public/category` | 全部分类 |
| GET | `/api/public/tag` | 全部标签 |
| GET | `/api/public/comment/{articleId}` | 文章评论 |
| POST | `/api/public/comment` | 提交评论 |
| GET | `/api/public/friend-links` | 友链列表 |
| GET | `/api/public/config` | 站点配置 |
| GET | `/api/public/ai/search?q=` | AI 问答（SSE 流式） |
| GET | `/api/public/ai/suggest?q=` | 搜索建议 |
| GET | `/api/public/ai/related/{id}` | 相关文章 |
| GET | `/api/site/sitemap.xml` | Sitemap |
| GET | `/api/site/rss` | RSS 订阅 |

### 管理接口（需要 admin 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/login` | 登录 |
| GET/POST/PUT/DELETE | `/api/article/**` | 文章 CRUD |
| GET/POST/PUT/DELETE | `/api/category` | 分类管理 |
| GET/POST/PUT/DELETE | `/api/tag` | 标签管理 |
| GET/PUT/DELETE | `/api/admin/comment/**` | 评论审核 |
| POST | `/api/admin/ai/summary` | AI 生成摘要 |
| POST | `/api/admin/ai/tags` | AI 推荐标签 |
| POST | `/api/admin/upload` | 图片上传 |
| GET/POST | `/api/admin/config` | 站点设置 |
| GET/POST/PUT/DELETE | `/api/admin/friend-links` | 友链管理 |

## AI 搜索原理

1. **索引构建**：文章发布/更新时，Jieba 对全文（标题 + 摘要 + 正文）分词，构建 TF-IDF 向量索引
2. **混合检索**：用户提问 → Jieba 分词 → 余弦相似度匹配 + 关键词加权 → Top 5 相关文章
3. **RAG 生成**：将检索到的文章片段作为上下文，通过 DeepSeek Chat API 流式生成答案
4. **来源标注**：答案中标注引用编号 `[1][2]`，前端展示参考文章列表

## 常见问题

**Q: 前端访问后端报 CORS 错误？**
确保 `application.yml` 中 `cors.allowed-origins` 包含前端地址（默认 `http://localhost:5173`）。如果修改了前端端口，需同步更新。

**Q: AI 搜索返回"AI功能未启用"？**
检查 `ai.enabled` 是否为 `true`，`ai.api-key` 是否正确。

**Q: 如何修改默认管理员密码？**
登录后台后，目前需要通过数据库直接修改 `user` 表的 `password` 字段。新密码需要用 BCrypt 编码，可以用在线工具或 Spring 的 `BCryptPasswordEncoder` 生成。

**Q: 图片上传后无法显示？**
确认 `blog.upload-path` 指向的目录存在且有写入权限。上传的图片通过 `/uploads/**` 路径访问。

**Q: 文章内容支持哪些 Markdown 语法？**
支持全部标准 Markdown（标题、列表、代码块、表格、链接、图片等），代码块自动高亮。
