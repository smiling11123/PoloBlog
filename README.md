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

## 部署教程

当前仓库已经提供了完整的部署文件，推荐方案为：

- 系统：`Debian 12`
- 容器编排：`Docker CE + Docker Compose`
- 反向代理：`Nginx`
- 静态资源：宿主机直接托管前台和后台构建产物

### 1. 部署文件说明

部署相关文件位于以下位置：

- [Dockerfile](./Dockerfile)：后端 Spring Boot 镜像构建文件
- [.dockerignore](./.dockerignore)：Docker 构建忽略规则
- [deploy/docker-compose.yml](./deploy/docker-compose.yml)：`MySQL + Redis + MinIO + Backend` 编排文件
- [deploy/.env.example](./deploy/.env.example)：Compose 环境变量模板
- [deploy/backend-config/application-prod.example.yml](./deploy/backend-config/application-prod.example.yml)：后端生产配置模板
- [deploy/nginx/myblog.conf](./deploy/nginx/myblog.conf)：Nginx 站点配置模板

### 2. 服务器准备

以 `Debian 12` 为例，先安装基础环境：

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg nginx
```

安装 Docker CE 和 Compose Plugin 后，确认版本：

```bash
docker --version
docker compose version
```

### 3. 准备部署配置

先复制部署模板：

```bash
cd /opt/blog
cp deploy/.env.example deploy/.env
cp deploy/backend-config/application-prod.example.yml deploy/backend-config/application-prod.yml
```

需要重点修改：

- `deploy/.env`
  - `MYSQL_ROOT_PASSWORD`
  - `MYSQL_PASSWORD`
  - `MINIO_ROOT_USER`
  - `MINIO_ROOT_PASSWORD`
  - `MINIO_BUCKET_NAME`
- `deploy/backend-config/application-prod.yml`
  - `spring.datasource.password`
  - `minio.endpoint`
  - `minio.accessKey`
  - `minio.secretKey`
  - `minio.bucketName`
  - `gitee.clientId`
  - `gitee.clientSecret`
  - `github.clientId`
  - `github.clientSecret`
  - `callback.url`
  - `frontend.url`
  - `jwt.secret`

注意：

- `minio.bucketName` 必须和 `MINIO_BUCKET_NAME` 保持一致

### 4. 启动后端依赖和 Spring Boot

执行：

```bash
cd deploy
docker compose --env-file .env up -d --build
```

这一步会启动：

- `mysql`
- `redis`
- `minio`
- `minio-init`
- `backend`

其中 `minio-init` 会自动完成两件事：

1. 创建 `MINIO_BUCKET_NAME` 对应的 bucket
2. 给这个 bucket 应用自定义匿名只读策略

因此 MinIO 控制台里该 bucket 的权限会显示为 `Custom`。

### 5. 构建并发布前台

在仓库根目录执行：

```bash
cd BlogFrontEnd/FrontEnd
npm install
npm run build
```

将构建产物复制到服务器静态目录：

```bash
sudo mkdir -p /var/www/blog
sudo rsync -av dist/ /var/www/blog/
```

### 6. 构建并发布后台管理

执行：

```bash
cd BlogFrontEndBack
corepack enable
pnpm install
pnpm build
```

复制构建产物：

```bash
sudo mkdir -p /var/www/admin
sudo rsync -av dist/ /var/www/admin/
```

### 7. 配置 Nginx

将 [deploy/nginx/myblog.conf](./deploy/nginx/myblog.conf) 复制到服务器，例如：

```bash
sudo cp deploy/nginx/myblog.conf /etc/nginx/conf.d/myblog.conf
```

然后把里面的域名改成你的真实域名：

- `blog.example.com`
- `admin.example.com`
- `files.example.com`

这份 Nginx 配置默认做了三件事：

- `blog.example.com`：托管博客前台静态文件，并把 `/api/` 转发到后端 `127.0.0.1:8080`
- `admin.example.com`：托管后台静态文件，并把 `/api/` 转发到后端 `127.0.0.1:8080`
- `files.example.com`：反向代理 MinIO `127.0.0.1:9000`

配置完成后重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 8. 配置 HTTPS

建议使用 `certbot`：

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d blog.your-domain.com -d admin.your-domain.com -d files.your-domain.com
```

证书签发完成后，记得把生产配置中的这些地址改成 HTTPS：

- `minio.endpoint`
- `callback.url`
- `frontend.url`

### 9. 首次部署完成后检查

建议至少检查下面这些项目：

- `docker compose ps` 所有核心容器都处于 `Up`
- `http://127.0.0.1:8080/article/list` 能返回数据
- `https://blog.your-domain.com` 能打开前台
- `https://admin.your-domain.com` 能打开后台
- `https://files.your-domain.com` 能访问 MinIO 文件
- Gitee/GitHub OAuth 回调地址已配置到：
  - `https://blog.your-domain.com/api/oauth/callback/gitee`
  - `https://blog.your-domain.com/api/oauth/callback/github`

### 10. 生产环境常用命令

查看容器状态：

```bash
cd deploy
docker compose ps
```

查看后端日志：

```bash
cd deploy
docker compose logs -f backend
```

重建后端：

```bash
cd deploy
docker compose up -d --build backend
```

## 配置说明

后端当前会自动尝试加载本地覆盖文件：

- [application.yaml](./src/main/resources/application.yaml)
- `application-local.yml`（本地私有，已被 `.gitignore` 忽略）

可提交模板：

- [application-local.example.yml](./src/main/resources/application-local.example.yml)
- [application-prod.example.yml](./src/main/resources/application-prod.example.yml)
- [.env.example](./.env.example)
- [deploy/.env.example](./deploy/.env.example)
- [deploy/backend-config/application-prod.example.yml](./deploy/backend-config/application-prod.example.yml)

## 接口文档

后端接口说明见：

- [docs/backend-api.md](./docs/backend-api.md)

## 开发约定

- 前后台开发环境统一通过 `/api` 代理到后端 `8080`
- 需要登录的接口通过 `Authorization` 请求头传递 JWT，当前实现不要求 `Bearer ` 前缀
- 上传接口使用 `multipart/form-data`

## 注意事项

- 如果要部署到服务器，建议使用 `Debian 12 + Docker CE + Nginx`
- 修改或二次分发请注明出处
