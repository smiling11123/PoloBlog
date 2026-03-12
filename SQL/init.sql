-- 1. 用户表 (核心)
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `uuid` varchar(100) DEFAULT NULL COMMENT '第三方id',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(100) DEFAULT NULL COMMENT '密码(加密)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `intro` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `status` char(1) DEFAULT '0' COMMENT '状态(0:正常 1:停用)',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `source` varchar(50) DEFAULT NULL COMMENT '登录方式',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int(1) DEFAULT 0 COMMENT '逻辑删除(0:未删 1:已删)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表 (管理员/普通用户)
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(30) NOT NULL COMMENT '角色权限字符串(如 admin)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0:正常 1:停用)',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 菜单权限表 (控制后台侧边栏和按钮)
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识(如 blog:article:list)',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `menu_type` char(1) DEFAULT '' COMMENT '类型(M:目录 C:菜单 F:按钮)',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态(0:正常 1:停用)',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 4. 用户-角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- 5. 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` bigint(20) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- 6. 分类表
CREATE TABLE IF NOT EXISTS `blog_category` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(128) NOT NULL COMMENT '分类名',
  `pid` bigint(20) DEFAULT -1 COMMENT '父分类ID',
  `description` varchar(512) DEFAULT NULL COMMENT '描述',
  `sort` int(4) DEFAULT 0 COMMENT '排序',
  `view_count` bigint(20) DEFAULT 0 COMMENT '浏览量',
  `thumbnail` varchar(256) DEFAULT NULL COMMENT '缩略图/封面',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 7. 标签表
CREATE TABLE IF NOT EXISTS `blog_tag` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(128) NOT NULL COMMENT '标签名',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- 8. 文章表
CREATE TABLE IF NOT EXISTS `blog_article` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '所属分类ID',
  `title` varchar(256) NOT NULL COMMENT '文章标题',
  `summary` varchar(1024) DEFAULT NULL COMMENT '文章摘要(列表页显示)',
  `content` longtext COMMENT '文章内容(Markdown源码)',
  `thumbnail` varchar(256) DEFAULT NULL COMMENT '缩略图/封面',
  `is_top` char(1) DEFAULT '0' COMMENT '是否置顶(0否 1是)',
  `status` char(1) DEFAULT '1' COMMENT '状态(0:草稿 1:发布)',
  `is_comment` char(1) DEFAULT '1' COMMENT '是否允许评论(0否 1是)',
  `view_count` bigint(20) DEFAULT 0 COMMENT '浏览量',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `is_deleted` int(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 9. 文章-标签关联表
CREATE TABLE IF NOT EXISTS `blog_article_tag` (
  `article_id` bigint(20) NOT NULL,
  `tag_id` bigint(20) NOT NULL,
  PRIMARY KEY (`article_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章和标签关联表';

-- 10. 评论表
CREATE TABLE IF NOT EXISTS `blog_comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '关联文章ID',
  `user_id` bigint(20) NOT NULL COMMENT '评论人ID',
  `user_name` varchar(128) DEFAULT '匿名用户' COMMENT '评论人昵称',
  `content` varchar(2048) NOT NULL COMMENT '评论内容',
  `root_id` bigint(20) DEFAULT -1 COMMENT '根评论ID',
--  `to_comment_id` bigint(20) DEFAULT -1 COMMENT '回复目标评论ID',
  `to_user_id` bigint(20) DEFAULT -1 COMMENT '回复目标用户ID',
  `to_user_name` char(128) DEFAULT '匿名用户' COMMENT '回复目标用户昵称',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `status` varchar(1) DEFAULT '0' COMMENT '状态',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`) USING BTREE,
  KEY `idx_root_id` (`root_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 11. 评论点赞记录表
CREATE TABLE IF NOT EXISTS `blog_comment_like` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';

-- 12. 全站每日数据统计表
CREATE TABLE IF NOT EXISTS `sys_daily_statistics` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `date` date NOT NULL COMMENT '统计日期',
  `login_user_count` int(11) DEFAULT 0,
  `new_article_count` int(11) DEFAULT 0,
  `new_comment_count` int(11) DEFAULT 0,
  `total_view_count` bigint(20) DEFAULT 0,
  `total_visit_count` bigint(20) DEFAULT 0,
  `total_visitor_count` bigint(20) DEFAULT 0,
  `message_slip_count` bigint(20) DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date` (`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站每日数据统计表';

-- 13. 单篇文章每日统计表
CREATE TABLE IF NOT EXISTS `blog_article_daily_stats` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `view_count` int(11) DEFAULT 0,
  `like_count` int(11) DEFAULT 0,
  `comment_count` int(11) DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_date` (`article_id`,`date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章每日数据统计表';

-- 14. 用户搜索记录表
CREATE TABLE IF NOT EXISTS `sys_search_history` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL,
  `keyword` varchar(100) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `search_times` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史记录表';

-- 15. 壁纸库
CREATE TABLE IF NOT EXISTS `sys_wallpaper` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `address` varchar(100)  COMMENT '地址',
  `name` varchar(100) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='壁纸库';
-- 16. 留言
CREATE TABLE IF NOT EXISTS `sys_message_slip` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `content` varchar(256)  COMMENT '留言内容',
  `user_id` bigint(20) COMMENT '留言人id',
  `is_deleted` int(1) DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言';
-- 17. 作者简介
CREATE TABLE IF NOT EXISTS `sys_auth_info` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_name` varchar(128) COMMENT '作者昵称',
  `avatar` varchar(128) COMMENT '头像地址',
  `profile` longtext COMMENT '作者简介',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作者简介';
-- 18. 作品集
CREATE TABLE IF NOT EXISTS `sys_works` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `title` varchar(128) COMMENT '标题',
  `thumbnail` varchar(128) COMMENT '封面地址',
  `address` varchar(128) COMMENT '作品地址',
  `content` longtext COMMENT '内容',
  `is_deleted` int(1) DEFAULT 0,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) USING BTREE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品集';
-- 19. 全站数据
CREATE TABLE IF NOT EXISTS `sys_all_site_data` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `total_user_count` int(11) DEFAULT 0,
  `total_article_count` int(11) DEFAULT 0,
  `total_comment_count` int(11) DEFAULT 0,
  `total_view_count` bigint(20) DEFAULT 0,
  `total_visit_count` bigint(20) DEFAULT 0,
  `total_visitor_count` bigint(20) DEFAULT 0,
  `total_message_slip_count` bigint(20) DEFAULT 0,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站数据';
-- 20. 标语表
CREATE TABLE IF NOT EXISTS `sys_slogan` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `content` varchar(128) NOT NULL COMMENT '内容',
  `create_time` datetime DEFAULT NULL,
  `is_deleted` int(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标语表';
-- ==========================================
-- 1. 初始化角色表 (sys_role)
-- ==========================================
DELETE FROM `sys_role` WHERE id IN (1, 2);

INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `status`, `create_time`, `is_deleted`) VALUES
(1, '管理员', 'admin', '0', NOW(), 0),
(2, '用户',   'user',  '0', NOW(), 0);

-- ==========================================
-- 2. 初始化用户表 (sys_user)
-- 密码统一为: 123456
-- ==========================================
DELETE FROM `sys_user` WHERE id IN (1, 2);

INSERT INTO `sys_user`
(`id`, `username`, `password`, `nickname`, `email`, `avatar`, `intro`, `status`, `create_time`, `is_deleted`)
VALUES
(1, 'admin',  '$2a$12$m9.weCe83PUUsCuANsb1Z.6sa45s0Fl1KBHIzUVQHLkXKBB1GLLI2', '管理员大BOSS', 'admin@polo.com',  'http://localhost:9000/travel/管理员头像.jpg', '管理员', '0', NOW(), 0),
(2, 'test',   '$2a$12$m9.weCe83PUUsCuANsb1Z.6sa45s0Fl1KBHIzUVQHLkXKBB1GLLI2', '路人甲',       'user@polo.com',   'http://localhost:9000/travel/管理员头像.jpg', '一名普通游客',   '0', NOW(), 0);

-- ==========================================
-- 3. 初始化用户-角色关联表 (sys_user_role)
-- ==========================================
DELETE FROM `sys_user_role` WHERE user_id IN (1, 2);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- 用户 admin (id=1)  <-> 角色 admin (id=1)
(2, 2); -- 用户 test (id=2)   <-> 角色 user (id=2)

-- ==========================================
-- 4. 初始化作者信息 (sys_auth_info)
-- ==========================================
DELETE FROM `sys_auth_info` WHERE id = 1;

INSERT INTO `sys_auth_info`
(`id`, `user_name`, `avatar`, `profile`, `update_time`)
VALUES
(1, '博主', '', '这个人很懒，还没有填写简介。', NOW());

-- ==========================================
-- 5. 初始化全站数据 (sys_all_site_data)
-- ==========================================
DELETE FROM `sys_all_site_data` WHERE id = 1;

INSERT INTO `sys_all_site_data`
(`id`, `total_user_count`, `total_article_count`, `total_comment_count`, `total_view_count`, `total_visit_count`, `total_visitor_count`, `total_message_slip_count`, `update_time`)
VALUES
(1, 2, 0, 0, 0, 0, 0, 0, NOW());
