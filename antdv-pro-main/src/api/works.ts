import { request } from "./request";

export interface WorksDTO {
    id?: string;
    title: string;
    content: string;
    thumbnail: string;
    address: string;
}

export const getWorksList = async (page: number = 1, size: number = 5) => {
    return request({
        url: '/works/list',
        method: 'get',
        params: {
            page,
            size
        }
    })
}

export const getWorksDetail = async (id: string) => {
    return request({
        url: '/works/detail',
        method: 'get',
        params: { id }
    })
}

export const publishWorks = async (data: WorksDTO) => {
    return request({
        url: '/works/publish',
        method: 'post',
        data
    })
}

export const deleteWorks = async (id: string) => {
    return request({
        url: '/works/delete',
        method: 'post',
        params: { id }
    })
}

export const updateWorks = async (data: WorksDTO) => {
    return request({
        url: '/works/update',
        method: 'post',
        data
    })
}
