# PoloBlog

一个前后端分离的个人博客项目，包含博客前台、后台管理和 Spring Boot 后端。项目覆盖文章、评论、留言墙、作品展示、数据统计和第三方 OAuth 登录等常见博客能力。

## 项目概览

- 博客前台：文章展示、搜索、分类标签、留言墙、OAuth 登录
- 后台管理：文章、分类、标签、评论、留言、壁纸、作品、标语、数据看板
- 后端服务：认证鉴权、业务接口、文件上传、缓存与定时统计

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.3、MyBatis-Plus、Redis、JWT、JustAuth、MinIO |
| 博客前台 | Vue 3、Vite、Pinia、Element Plus |
| 后台管理 | Vue 3、Vite、Pinia、Ant Design Vue、G2Plot |
| 数据库 | MySQL 8 |
| 部署 | Docker Compose、Nginx |

## 项目结构

```text
.
├─ src/                          # Spring Boot 后端源码
├─ SQL/                          # 数据库初始化脚本
├─ BlogFrontEnd/FrontEnd/        # 博客前台
├─ BlogFrontEndBack/             # 后台管理
├─ deploy/                       # Docker / Nginx / 生产配置模板
├─ docs/                         # 项目文档
├─ pom.xml                       # 后端 Maven 配置
└─ .env.example                  # 环境变量模板
```

## 核心功能

- 文章：发布、编辑、搜索、分类筛选、标签筛选、热门文章
- 评论：根评论、子评论、回复链路、后台管理
- 留言墙：前台展示、后台删除、弹幕展示
- 站点内容：博主信息、壁纸、作品集、标语
- 数据统计：全站数据、每日统计、文章趋势
- 登录鉴权：管理员账号登录、Gitee/GitHub OAuth 登录
- 文件管理：MinIO 上传与访问

## 开发环境

- JDK `21`
- Maven `3.9+` 或仓库自带 `mvnw`
- Node.js `20+`
- pnpm `10+`
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

推荐复制本地模板：

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

按实际环境填写：

- 数据库连接
- Redis 地址
- MinIO 账号
- OAuth Client ID / Secret
- `jwt.secret`

也可以直接参考 [.env.example](./.env.example) 使用环境变量方式。

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

推荐方案：

- 系统：`Debian 12`
- 容器编排：`Docker CE + Docker Compose`
- 反向代理：`Nginx`
- 站点结构：单域名 + 路径分流

推荐访问路径：

- 前台：`https://example.com/`
- 后台：`https://example.com/admin/`
- 接口：`https://example.com/api/`
- 文件：`https://example.com/files/`

如果只是临时测试，也可以把 `example.com` 换成服务器 IP，直接走 `http`。

### 1. 部署文件

- [Dockerfile](./Dockerfile)
- [.dockerignore](./.dockerignore)
- [deploy/docker-compose.yml](./deploy/docker-compose.yml)
- [deploy/.env.example](./deploy/.env.example)
- [deploy/backend-config/application-prod.example.yml](./deploy/backend-config/application-prod.example.yml)
- [deploy/nginx/myblog.conf](./deploy/nginx/myblog.conf)

### 2. 服务器准备

以 `Debian 12` 为例：

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg nginx
```

安装 Docker CE 和 Compose Plugin 后确认版本：

```bash
docker --version
docker compose version
```

### 3. 准备部署配置

复制模板：

```bash
cd /opt/blog
cp deploy/.env.example deploy/.env
cp deploy/backend-config/application-prod.example.yml deploy/backend-config/application-prod.yml
```

重点修改：

- `deploy/.env`
  - `MYSQL_ROOT_PASSWORD`
  - `MYSQL_PASSWORD`
  - `MINIO_ROOT_USER`
  - `MINIO_ROOT_PASSWORD`
  - `MINIO_BUCKET_NAME`
- `deploy/backend-config/application-prod.yml`
  - `spring.datasource.password`
  - `minio.endpoint`，Docker 部署时建议填 `http://minio:9000`
  - `minio.publicEndpoint`，单域名部署时建议填 `https://example.com/files`
  - `minio.accessKey`
  - `minio.secretKey`
  - `minio.bucketName`
  - `gitee.clientId`
  - `gitee.clientSecret`
  - `github.clientId`
  - `github.clientSecret`
  - `callback.url`，单域名部署时建议填 `https://example.com/api/oauth/callback/`
  - `frontend.url`，单域名部署时建议填 `https://example.com/`
  - `jwt.secret`
- [BlogFrontEndBack/.env.production](./BlogFrontEndBack/.env.production)
  - `VITE_APP_BASE=/admin/`
  - `VITE_APP_BASE_API=/api`
  - `VITE_APP_BASE_URL=https://example.com`

说明：

- `minio.bucketName` 必须和 `MINIO_BUCKET_NAME` 保持一致
- `minio.endpoint` 是后端直连 MinIO 的内部地址，不能带路径
- `minio.publicEndpoint` 是返回给前端的公网文件地址，可以带 `/files`
- 如果你还没启用 HTTPS，先写 `http://example.com`，后续再统一切到 `https`

### 4. 启动后端依赖

执行：

```bash
cd deploy
docker compose --env-file .env up -d --build
```

这一步会启动 `mysql`、`redis`、`minio`、`minio-init` 和 `backend`。  
其中 `minio-init` 会自动创建 bucket，并设置匿名只读的自定义策略。

### 5. 构建并发布前台

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

将 [deploy/nginx/myblog.conf](./deploy/nginx/myblog.conf) 复制到服务器：

```bash
sudo cp deploy/nginx/myblog.conf /etc/nginx/conf.d/myblog.conf
```

然后把里面的域名 `example.com` 改成你的真实域名。

这份 Nginx 配置默认做了四件事：

- `/`：托管博客前台静态文件
- `/admin/`：托管后台静态文件
- `/api/`：转发到后端 `127.0.0.1:8080`
- `/files/`：反向代理 MinIO `127.0.0.1:9000`

配置完成后重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 8. 配置 HTTPS

建议使用 `certbot`：

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

启用 HTTPS 后，需要把生产配置中的这些地址改成 `https://`：

- `minio.publicEndpoint`
- `callback.url`
- `frontend.url`

然后重启后端：

```bash
cd /opt/blog/deploy
docker compose restart backend
```

说明：

- 博客前台通常不用重打包，因为它使用相对路径 `/api`
- 后台如果修改了 `BlogFrontEndBack/.env.production`，需要重新执行 `pnpm build`

### 9. 上线检查

- `docker compose ps` 所有核心容器都处于 `Up`
- `http://127.0.0.1:8080/article/list` 能返回数据
- `https://your-domain.com` 能打开前台
- `https://your-domain.com/admin/` 能打开后台
- `https://your-domain.com/files/` 能访问 MinIO 文件
- Gitee/GitHub OAuth 回调地址已配置到：
  - `https://your-domain.com/api/oauth/callback/gitee`
  - `https://your-domain.com/api/oauth/callback/github`

### 10. 常用命令

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

## 文档入口

- 后端接口说明：[docs/backend-api.md](./docs/backend-api.md)

## 开发约定

- 前后台开发环境统一通过 `/api` 代理到后端 `8080`
- 需要登录的接口通过 `Authorization` 请求头传递 JWT，当前实现不要求 `Bearer ` 前缀
- 上传接口使用 `multipart/form-data`

## 注意事项

- 如果要部署到服务器，推荐 `Debian 12 + Docker CE + Nginx`
- 修改或二次分发请注明出处
