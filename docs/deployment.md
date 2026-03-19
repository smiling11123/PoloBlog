# 部署文档

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

## 1. 部署文件

- [Dockerfile](../Dockerfile)
- [.dockerignore](../.dockerignore)
- [deploy/docker-compose.yml](../deploy/docker-compose.yml)
- [deploy/.env.example](../deploy/.env.example)
- [deploy/backend-config/application-prod.example.yml](../deploy/backend-config/application-prod.example.yml)
- [deploy/nginx/myblog.conf](../deploy/nginx/myblog.conf)

## 2. 服务器准备

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg nginx
```

安装 Docker CE 和 Compose Plugin 后确认版本：

```bash
docker --version
docker compose version
```

## 3. 准备部署配置

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
- [BlogFrontEndBack/.env.production](../BlogFrontEndBack/.env.production)
  - `VITE_APP_BASE=/admin/`
  - `VITE_APP_BASE_API=/api`
  - `VITE_APP_BASE_URL=https://example.com`

说明：

- `minio.bucketName` 必须和 `MINIO_BUCKET_NAME` 保持一致
- `minio.endpoint` 是后端直连 MinIO 的内部地址，不能带路径
- `minio.publicEndpoint` 是返回给前端的公网文件地址，可以带 `/files`
- 如果你还没启用 HTTPS，先写 `http://example.com`，后续再统一切到 `https`

## 4. 启动后端依赖

执行：

```bash
cd deploy
docker compose --env-file .env up -d --build
```

这一步会启动 `mysql`、`redis`、`minio`、`minio-init` 和 `backend`。  
其中 `minio-init` 会自动创建 bucket，并设置匿名只读的自定义策略。

## 5. 构建并发布前台

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

## 6. 构建并发布后台管理

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

## 7. 配置 Nginx

将 [deploy/nginx/myblog.conf](../deploy/nginx/myblog.conf) 复制到服务器：

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

## 8. 配置 HTTPS

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

## 9. 上线检查

- `docker compose ps` 所有核心容器都处于 `Up`
- `http://127.0.0.1:8080/article/list` 能返回数据
- `https://your-domain.com` 能打开前台
- `https://your-domain.com/admin/` 能打开后台
- `https://your-domain.com/files/` 能访问 MinIO 文件
- Gitee/GitHub OAuth 回调地址已配置到：
  - `https://your-domain.com/api/oauth/callback/gitee`
  - `https://your-domain.com/api/oauth/callback/github`

## 10. 常用命令

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
