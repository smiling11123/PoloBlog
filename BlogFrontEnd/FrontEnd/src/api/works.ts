import { request } from "./request";

export const getWorksList = async (params: any) => {
    return request({
        url: '/works/list',
        params: { page: params.page, size: params.size }
    })
}

export const getWorksDetail = async (params: any) =>{
    return request({
        url: 'works/detail',
        params: { id: params }
    })
}