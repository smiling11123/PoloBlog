export interface UserDTO {
    username: string;
    password: string;
}

export interface ArticleVO {
    id;                         // 文章id
    categoryId;                 // 所属分类ID
    categoryName;
    title;                    // 文章标题
    summary;                  // 文章摘要
    thumbnail;                // 封面地址
    isTop;                   // 是否置顶(0否 1是)
    status;                  // 状态(0:草稿 1:发布)
    viewCount;                  // 浏览量
    createTime;        // 创建时间
    updateTime;        // 更新时间
    createBy;                   // 创建人ID
    latitude;                 // 纬度
    longitude;                // 经度
    name;                     // 景点名称
    auth;
}

export interface CommentVO {
    id: number;
    articleId: number;
    userId: number;
    userName: string;
    content: string;
    rootId: number;
    toUserId: number;
    toUserName: string;
    likeCount: number;
    childCount: number;
    showReplies: number;
    createTime: string;
}