import { request } from "../request";

export const searchByKeyWord = async (params: any) => {
    return request({
        url: '/article/search',
        params: { keyword: params.keyword, categoryId: params.categoryIds } 
    })
}