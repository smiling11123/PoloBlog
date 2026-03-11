import { request } from "../request";

export const getHotArticleList = async (params: any) => {
    return request({
        url: '/article/hotList',
        params: params,
    })
}