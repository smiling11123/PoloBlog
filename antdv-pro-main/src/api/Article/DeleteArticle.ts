import { request } from "../request";

export const deleteMyArticle = async (params: any) => {
    return request({
        url: '/article/delete',
        params: {id: params},
        method: "POST"
    })
}