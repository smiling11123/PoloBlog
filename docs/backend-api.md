# 后端接口说明

本文档基于当前后端控制器源码整理，适用于仓库中的 Spring Boot 服务。

## 1. 基本信息

- 本地默认服务地址：`http://localhost:8080`
- 前台/后台开发环境通常通过 Vite 代理访问：`/api/**`
- 所有接口统一返回 `Result<T>` 结构

统一响应格式：

```json
{
  "code": 200,
  "msg": "成功响应",
  "data": {}
}
```

常见状态说明：

- `200`：成功
- `400`：参数错误或业务校验失败
- `401`：未登录、登录失效或权限校验失败
- `403`：已登录但无权限
- `404`：资源不存在
- `500`：服务异常

## 2. 认证说明

### 2.1 JWT

需要登录的接口会使用 `Authorization` 请求头读取 JWT：

```http
Authorization: <token>
```

当前实现不要求 `Bearer ` 前缀。

### 2.2 `@RequireAuth`

控制器或方法标注 `@RequireAuth` 时需要携带有效 token。  
未标注的接口为公开接口。

### 2.3 分页返回

分页接口底层使用 MyBatis-Plus `Page/IPage`，常见返回字段包括：

- `records`：列表数据
- `total`：总条数
- `size`：每页条数
- `current`：当前页
- `pages`：总页数

## 3. 登录与 OAuth

### 3.1 获取第三方登录地址

- 方法：`GET`
- 路径：`/login/url`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `type` | string | 是 | 登录源，当前支持 `gitee`、`github` |

返回：

- `data`：第三方授权 URL

### 3.2 后台管理员登录

- 方法：`POST`
- 路径：`/login/admin`
- 认证：公开
- Content-Type：`application/json`

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

返回：

- `data`：JWT token 字符串

### 3.3 后台退出登录

- 方法：`POST`
- 路径：`/login/admin/logout`
- 认证：登录

返回：

- `data`：`"下线成功"`

### 3.4 OAuth 回调

- 方法：`GET`
- 路径：`/oauth/callback/{type}`
- 认证：公开

路径参数：

| 参数 | 说明 |
| --- | --- |
| `type` | `gitee` 或 `github` |

说明：

- 该接口用于第三方登录回调
- 成功后会设置临时登录桥接 cookie，并重定向到前台地址
- 这是浏览器跳转接口，不是常规 JSON 接口

## 4. 用户接口

用户控制器带有类级别 `@RequireAuth`，即 `/user/**` 全部需要登录。

### 4.1 获取当前登录用户详情

- 方法：`GET`
- 路径：`/user/detail`
- 认证：登录

返回核心字段：

- `id`
- `username`
- `nickname`
- `email`
- `avatar`
- `source`
- `roleName`
- `roleKey`
- `statusText`

### 4.2 获取已删除用户列表

- 方法：`GET`
- 路径：`/user/deletedUserList`
- 认证：管理员

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `20` |

### 4.3 获取用户列表

- 方法：`GET`
- 路径：`/user/list`
- 认证：管理员

请求参数同上。

### 4.4 根据 ID 获取用户详情

- 方法：`GET`
- 路径：`/user/detailById`
- 认证：管理员

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 4.5 删除用户

- 方法：`POST`
- 路径：`/user/deleteById`
- 认证：管理员

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 4.6 搜索用户

- 方法：`GET`
- 路径：`/user/search`
- 认证：管理员

请求参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `page` | int | `1` | 页码 |
| `size` | int | `20` | 每页条数 |
| `keyword` | string | 无 | 用户名关键字 |

### 4.7 更新当前管理员信息

- 方法：`POST`
- 路径：`/user/update`
- 认证：管理员
- Content-Type：`application/json`

请求体字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 用户 ID |
| `username` | string | 用户名 |
| `nickname` | string | 昵称 |
| `email` | string | 邮箱 |
| `avatar` | string | 头像地址 |
| `source` | string | 登录来源 |
| `password` | string | 密码 |

## 5. 文章接口

### 5.1 获取文章详情

- 方法：`GET`
- 路径：`/article/detail`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

说明：

- 游客仅能看到已发布且未删除的文章
- 作者本人或管理员可查看自己的草稿/非公开文章

返回核心字段：

- `id`
- `categoryId`
- `categoryName`
- `title`
- `summary`
- `thumbnail`
- `content`
- `isTop`
- `status`
- `isComment`
- `viewCount`
- `createTime`
- `updateTime`
- `createBy`
- `auth`

### 5.2 获取文章列表

- 方法：`GET`
- 路径：`/article/list`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `20` |

### 5.3 根据标签获取文章

- 方法：`GET`
- 路径：`/article/listByTag`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `tagId` | string[] | 是 | 可重复传递 |
| `page` | int | 否 | 默认 `1` |
| `size` | int | 否 | 默认 `20` |

### 5.4 根据分类获取文章

- 方法：`GET`
- 路径：`/article/listByCategory`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `categoryId` | long | 是 |
| `page` | int | 否 |
| `size` | int | 否 |

### 5.5 搜索文章

- 方法：`GET`
- 路径：`/article/search`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `keyword` | string | `""` | 关键字 |
| `categoryId` | long[] | `[]` | 分类 ID 列表 |
| `page` | int | `1` | 页码 |
| `size` | int | `20` | 每页条数 |

### 5.6 获取热门文章

- 方法：`GET`
- 路径：`/article/hotList`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `5` |

### 5.7 获取已删除文章列表

- 方法：`GET`
- 路径：`/article/deletedList`
- 认证：管理员

### 5.8 获取草稿列表

- 方法：`GET`
- 路径：`/article/manuscript`
- 认证：管理员

### 5.9 发布文章

- 方法：`POST`
- 路径：`/article/publish`
- 认证：登录（当前实现要求 `roleKey != user`）
- Content-Type：`application/json`

请求体：

```json
{
  "id": "1",
  "categoryName": "后端",
  "title": "文章标题",
  "summary": "文章摘要",
  "content": "Markdown 内容",
  "thumbnail": "https://example.com/cover.png",
  "isTop": 0,
  "status": 1,
  "isComment": 1,
  "isDeleted": 0
}
```

### 5.10 更新文章

- 方法：`POST`
- 路径：`/article/update`
- 认证：作者或管理员
- Content-Type：`application/json`

请求体同 [5.9](#59-发布文章)。

### 5.11 删除文章

- 方法：`POST`
- 路径：`/article/delete`
- 认证：作者或管理员

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 5.12 草稿转发布

- 方法：`POST`
- 路径：`/article/manuscriptPublish`
- 认证：管理员
- Content-Type：`application/json`

请求体至少需要包含：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 5.13 切换评论开关

- 方法：`POST`
- 路径：`/article/toggleComment`
- 认证：作者或管理员

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

## 6. 分类与标签

### 6.1 获取热门分类

- 方法：`GET`
- 路径：`/category/hotList`
- 认证：公开

### 6.2 获取分类列表

- 方法：`GET`
- 路径：`/category/list`
- 认证：公开

返回核心字段：

- `id`
- `name`
- `pid`
- `description`
- `sort`
- `viewCount`
- `thumbnail`

### 6.3 创建分类

- 方法：`POST`
- 路径：`/category/create`
- 认证：登录
- Content-Type：`application/json`

请求体字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | string | 分类名 |
| `pid` | long | 父级 ID |
| `description` | string | 描述 |
| `sort` | int | 排序 |
| `viewCount` | long | 浏览量 |
| `thumbnail` | string | 缩略图 |

### 6.4 删除分类

- 方法：`POST`
- 路径：`/category/delete`
- 认证：登录

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 6.5 获取标签列表

- 方法：`GET`
- 路径：`/tag/list`
- 认证：公开

### 6.6 创建标签

- 方法：`POST`
- 路径：`/tag/create`
- 认证：登录

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `name` | string | 是 |

### 6.7 更新标签

- 方法：`POST`
- 路径：`/tag/update`
- 认证：登录

请求参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `name` | string | `""` | 新标签名 |
| `isDelete` | int | `0` | 是否删除 |
| `id` | long | 无 | 标签 ID |

## 7. 评论接口

### 7.1 发布评论

- 方法：`POST`
- 路径：`/comment/publish`
- 认证：登录

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | long | 是 | 当前实现由前端传入 |
| `userName` | string | 是 | 当前实现由前端传入 |
| `articleId` | long | 是 | 文章 ID |
| `rootId` | long | 否 | 默认 `-1`，根评论时为 `-1` |
| `content` | string | 是 | 评论内容 |
| `toUserName` | string | 否 | 回复目标用户名 |
| `toUserId` | long | 否 | 回复目标用户 ID |

返回：

- `data`：`"发布成功"`

### 7.2 获取根评论

- 方法：`GET`
- 路径：`/comment/rootComment`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `articleId` | long | 无 |
| `page` | int | `1` |
| `size` | int | `20` |

返回记录核心字段：

- `id`
- `articleId`
- `userId`
- `userName`
- `content`
- `rootId`
- `toUserId`
- `toUserName`
- `likeCount`
- `childCount`
- `showReplies`
- `createTime`

### 7.3 获取子评论

- 方法：`GET`
- 路径：`/comment/childComment`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `rootId` | long | 无 |
| `page` | int | `1` |
| `size` | int | `20` |

## 8. 留言墙接口

### 8.1 获取留言列表

- 方法：`GET`
- 路径：`/messageSlip/list`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `20` |

### 8.2 获取随机展示留言

- 方法：`GET`
- 路径：`/messageSlip/showList`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `num` | int | `20` |

### 8.3 发布留言

- 方法：`POST`
- 路径：`/messageSlip/publish`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `content` | string | 是 | 留言内容 |
| `userId` | long | 否 | 用户 ID，可为空 |

### 8.4 删除留言

- 方法：`POST`
- 路径：`/messageSlip/delete`
- 认证：登录

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

## 9. 博主信息与站点内容

### 9.1 获取博主信息

- 方法：`GET`
- 路径：`/authInfo/get`
- 认证：公开

返回字段：

- `id`
- `userName`
- `avatar`
- `profile`
- `updateTime`

### 9.2 更新博主信息

- 方法：`POST`
- 路径：`/authInfo/update`
- 认证：管理员
- Content-Type：`application/json`

请求体：

```json
{
  "userName": "Polo",
  "avatar": "https://example.com/avatar.png",
  "profile": "个人简介",
  "slogan": "这里是额外文案字段"
}
```

### 9.3 获取标语列表

- 方法：`GET`
- 路径：`/slogan/list`
- 认证：公开

### 9.4 新增标语

- 方法：`POST`
- 路径：`/slogan/publish`
- 认证：管理员
- Content-Type：`application/json`

请求体：

```json
{
  "content": "欢迎来到我的博客"
}
```

### 9.5 删除标语

- 方法：`POST`
- 路径：`/slogan/delete`
- 认证：管理员

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 9.6 获取壁纸列表

- 方法：`GET`
- 路径：`/wallpaper/list`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `6` |

### 9.7 删除壁纸

- 方法：`POST`
- 路径：`/wallpaper/delete`
- 认证：管理员

### 9.8 上传壁纸

- 方法：`POST`
- 路径：`/wallpaper/upload`
- 认证：管理员
- Content-Type：`multipart/form-data`

表单字段：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `name` | string | 是 |
| `file` | file | 是 |

### 9.9 获取作品列表

- 方法：`GET`
- 路径：`/works/list`
- 认证：公开

请求参数：

| 参数 | 类型 | 默认值 |
| --- | --- | --- |
| `page` | int | `1` |
| `size` | int | `5` |

返回记录核心字段：

- `id`
- `title`
- `thumbnail`
- `address`
- `createTime`

### 9.10 获取作品详情

- 方法：`GET`
- 路径：`/works/detail`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `id` | long | 是 |

### 9.11 发布作品

- 方法：`POST`
- 路径：`/works/publish`
- 认证：管理员
- Content-Type：`application/json`

请求体：

```json
{
  "title": "作品标题",
  "content": "作品内容",
  "thumbnail": "https://example.com/work.png"
}
```

### 9.12 更新作品

- 方法：`POST`
- 路径：`/works/update`
- 认证：管理员

请求体字段同上，更新时需包含 `id`。

### 9.13 删除作品

- 方法：`POST`
- 路径：`/works/delete`
- 认证：管理员

## 10. 上传接口

### 10.1 上传文件

- 方法：`POST`
- 路径：`/upload/file`
- 认证：管理员
- Content-Type：`multipart/form-data`

表单字段：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| `file` | file | 是 |

返回：

- `data`：文件访问 URL

## 11. 统计接口

### 11.1 获取全站统计

- 方法：`GET`
- 路径：`/allSiteData/get`
- 认证：公开

返回字段：

- `totalUserCount`
- `totalArticleCount`
- `totalCommentCount`
- `totalViewCount`
- `totalVisitCount`
- `totalVisitorCount`
- `totalMessageSlipCount`
- `updateTime`

### 11.2 获取最近每日统计

- 方法：`GET`
- 路径：`/dailyStats/recent`
- 认证：公开

返回字段：

- `date`
- `loginUserCount`
- `newArticleCount`
- `newCommentCount`
- `totalViewCount`
- `totalVisitCount`
- `totalVisitorCount`
- `messageSlipCount`

### 11.3 增加访问统计

- 方法：`POST`
- 路径：`/dailyStats/addVisit`
- 认证：公开

说明：

- 根据请求 IP 记录访问量和独立访客数

### 11.4 获取文章近 30 天统计

- 方法：`GET`
- 路径：`/articleDaily/recent`
- 认证：公开

请求参数：

| 参数 | 类型 | 必填 |
| --- | --- | --- |
| `articleId` | long | 是 |

返回字段：

- `date`
- `articleId`
- `viewCount`
- `likeCount`
- `commentCount`

## 12. 常用请求示例

### 12.1 后台登录

```bash
curl -X POST "http://localhost:8080/login/admin" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 12.2 获取当前用户信息

```bash
curl "http://localhost:8080/user/detail" \
  -H "Authorization: your-jwt-token"
```

### 12.3 发布文章

```bash
curl -X POST "http://localhost:8080/article/publish" \
  -H "Content-Type: application/json" \
  -H "Authorization: your-jwt-token" \
  -d '{
    "categoryName": "后端",
    "title": "Spring Boot 实战",
    "summary": "一篇示例文章",
    "content": "# Hello",
    "thumbnail": "https://example.com/cover.png",
    "isTop": 0,
    "status": 1,
    "isComment": 1
  }'
```

### 12.4 上传文件

```bash
curl -X POST "http://localhost:8080/upload/file" \
  -H "Authorization: your-jwt-token" \
  -F "file=@/path/to/example.png"
```

## 13. 备注

- 文档描述的是当前代码实现，不是理想权限模型
- 部分接口在控制器层只要求登录，实际是否允许操作还要看服务层实现
- 评论发布、留言发布等接口当前参数设计偏前端直传，后续如果有重构，建议同步更新本文档
