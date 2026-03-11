import { request } from "./request";

export const getRootComment = async (params: { articleId: string, page: number, size: number }) => {
    return request({
        url: '/comment/rootComment',
        method: 'get',
        params: params
    })
}

export const getChildComment = async (params: { rootId: string, page: number, size: number }) => {
    return request({
        url: '/comment/childComment',
        method: 'get',
        params: params
    })
}

export const publishComment = async (data: any) => {
    return request({
        url: '/comment/publish',
        method: 'post',
        params: data
    })
}
