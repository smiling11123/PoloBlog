import { request } from "../request";

export const getArticleList = async (params: any) => {
    return request({
      url: '/article/list',
      params: params,
    })
}

export const getDeletedArticleList = async (params: any) => {
    return request({
        url: '/article/deletedList',
        params: params,
    })
}

export const restoreDeletedArticle = async (id: string | number) => {
    return request({
        url: '/article/restore',
        method: 'POST',
        params: { id },
    })
}

export const toggleArticleComment = async (id: any) => {
    return request({
        url: '/article/toggleComment',
        method: 'POST',
        params: { id }
    })
}

export const getArticleListByCategory = async (params: any) => {
    return request({
        url: '/article/listByCategory',
        params: params,
    })
}

