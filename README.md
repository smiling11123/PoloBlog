# MyBlog

一个前后端分离的个人博客项目，包含博客前台、后台管理和 Spring Boot 后端。项目支持文章发布、分类标签、评论、留言墙、标语管理、壁纸管理、作品展示、全站统计和第三方 OAuth 登录。

## 项目结构

```text
.
├─ src/                          # Spring Boot 后端源码
├─ SQL/                          # 数据库初始化脚本
├─ BlogFrontEnd/FrontEnd/        # 博客前台（Vue 3 + Vite）
├─ BlogFrontEndBack/             # 后台管理（Vue 3 + Ant Design Vue）
├─ docs/                         # 项目文档
├─ pom.xml                       # Maven 项目配置
├─ .env.example                  # 环境变量模板
└─ src/main/resources/           # 后端配置模板
```

## 技术栈

- 后端：Spring Boot 3.3、MyBatis-Plus、Redis、JWT、JustAuth、MinIO
- 前台：Vue 3、Vite、Pinia、Element Plus
- 后台：Vue 3、Vite、Ant Design Vue、Pinia、G2Plot
- 数据库：MySQL 8

## 核心功能

- 博客文章：列表、详情、搜索、分类/标签筛选、热门文章
- 评论系统：根评论、子评论、回复链路
- 留言墙：随机展示、分页管理、发布与删除
- 站点内容：博主信息、标语、壁纸、作品展示
- 监控统计：全站数据、每日统计、文章近 30 天趋势
- 登录鉴权：后台账号密码登录、Gitee/GitHub OAuth 登录
- 文件管理：MinIO 文件上传

## 运行环境

- JDK `21`
- Maven `3.9+`（也可用仓库自带 `mvnw`）
- Node.js `20+`
- pnpm `10+`（后台管理推荐）
- MySQL `8.x`
- Redis `6+`
- MinIO `RELEASE.2024+`

## 快速开始

### 1. 初始化数据库

执行 [SQL/init.sql](./SQL/init.sql) 导入表结构和基础数据。

初始化脚本会写入两个测试账号：

- 管理员：`admin / 123456`
- 普通用户：`test / 123456`

建议导入后立即修改默认密码。

### 2. 配置后端

仓库中的 [application.yaml](./src/main/resources/application.yaml) 只保留了环境变量入口。

本地开发推荐复制模板：

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

然后按实际环境填写：

- 数据库连接
- Redis 地址
- MinIO 账号
- OAuth Client ID / Secret
- `jwt.secret`

也可以直接参考 [.env.example](./.env.example) 走环境变量方式。

### 3. 启动后端

在仓库根目录执行：

```bash
./mvnw spring-boot:run
```

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

后端默认地址：`http://localhost:8080`

### 4. 启动博客前台

```bash
cd BlogFrontEnd/FrontEnd
npm install
npm run dev
```

前台默认地址：`http://localhost:6678`

### 5. 启动后台管理

```bash
cd BlogFrontEndBack
corepack enable
pnpm install
pnpm dev
```

后台默认地址：`http://localhost:6600`

## 构建命令

后端：

```bash
./mvnw -DskipTests package
```

前台：

```bash
cd BlogFrontEnd/FrontEnd
npm run build
```

后台：

```bash
cd BlogFrontEndBack
pnpm build
```

## 配置说明

后端当前会自动尝试加载本地覆盖文件：

- [application.yaml](./src/main/resources/application.yaml)
- `application-local.yml`（本地私有，已被 `.gitignore` 忽略）

可提交模板：

- [application-local.example.yml](./src/main/resources/application-local.example.yml)
- [application-prod.example.yml](./src/main/resources/application-prod.example.yml)
- [.env.example](./.env.example)

## 接口文档

后端接口说明见：

- [docs/backend-api.md](./docs/backend-api.md)

## 开发约定

- 前后台开发环境统一通过 `/api` 代理到后端 `8080`
- 需要登录的接口通过 `Authorization` 请求头传递 JWT，当前实现不要求 `Bearer ` 前缀
- 上传接口使用 `multipart/form-data`
- 建议将本地和生产敏感配置放在被忽略的文件中，不要直接提交到仓库

## 注意事项

- 当前仓库已将密钥配置改为环境变量/模板模式，历史上使用过的测试密钥不建议继续用于生产
- OAuth 回调地址、前台域名和 MinIO 公网地址在生产环境必须改成正式域名
- 如果要部署到服务器，建议使用 `Debian 12 + Docker CE + Nginx`
