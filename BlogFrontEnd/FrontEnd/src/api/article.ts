import { request } from "./request";
//获取文章列表
export const getArticleList = async (params: any) => {
    return request({
        url: 'article/list',
        params: { page: params.page, size: params.size },
    })
}

//获取文章详情
export const getArticleDetail = async (id: any) => {
    return request({
        url: 'article/detail',
        params: {id: id}
    })
}
//根据关键词、分类获取文章列表 搜索
export const getArticleByKeyword = async (params: any) => {
    return request({
        url: 'article/search',
        params: {keyword: params.keyword, categoryId: params.categoryIds, page: params.page, size: params.size }
    })
}
//根据标签id获取文章列表
export const getArticleByTag = async (params: any) => {
    return request({
        url: 'article/listByTag',
        params: {tagId: params.tagId, page: params.page, size: params.size }
    })
}

//根据分类id获取文章列表
export const getArticleByCategory = async (params: any) => {
    return request({
        url: 'article/listByCategory',
        params: { 
            categoryId: params.categoryId, 
            page: params.page, 
            size: params.size,
            orderBy: params.orderBy
        }
    })
}
//获取热门文章列表
export const getHotArticle = async (params: any) => {
    return request({
        url: 'article/hotList',
        params: { page: params.page, size: params.size}
    })
}