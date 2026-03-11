import { request } from "../request";

export const getArticleDetail = async (id: any) => {
    return request({
        url: '/article/detail',
        params: {id: id}
    })
}