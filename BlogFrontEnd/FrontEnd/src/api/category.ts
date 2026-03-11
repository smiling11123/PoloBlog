import { request } from "./request";

export const getCategoryList = async () => {
    return request({
        url: '/category/list',
    })
}

export const getCategoryHotList = async () => {
    return request({
        url: '/category/hotList',
    })
}