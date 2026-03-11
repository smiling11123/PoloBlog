import { request } from "../request";

export const publishArticle = async (data: any) => {
    return request({
        url: '/article/publish',
        method: 'POST',
        data: data,
    })
}

export const updateArticle = async (data: any) => {
    return request({ 
        url: '/article/update',
        method: 'POST',
        data: data,
    })
}