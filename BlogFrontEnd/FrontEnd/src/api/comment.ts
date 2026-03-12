import { request } from "./request";

import type { CommentDTO } from '@/type/Interface'

//发表评论(发布者id,  文章id, 父级评论id, 回复人id, 评论内容, 根评论id, 评论内容)
export const publishComment = async (data: CommentDTO) => {
    return request({
        url: 'comment/publish',
        method: "POST",
        data,
    })
}
//获取根评论(文章Id)
export const getRootComment = async (params: any) => {
    return request({
        url: 'comment/rootComment',
        params: { articleId: params }
    })
}
//获取子评论
export const getChildComment = async (params: any) => {
    return request({
        url: 'comment/childComment',
        params: { rootId: params }
    })
}
