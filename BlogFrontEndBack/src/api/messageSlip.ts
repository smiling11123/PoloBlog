import { request } from "./request";

export const getMessageSlipList = async (page: number = 1, size: number = 20) => {
    return request({
        url: '/messageSlip/list',
        method: 'get',
        params: {
            page,
            size
        }
    })
}

export const getMessageSlipToShow = async (num: number = 20) => {
    return request({
        url: '/messageSlip/showList',
        method: 'get',
        params: { num }
    })
}

export const publishMessageSlip = async (content: string, userId?: number) => {
    return request({
        url: '/messageSlip/publish',
        method: 'post',
        params: {
            content,
            userId
        }
    })
}

export const deleteMessageSlip = async (id: number) => {
    return request({
        url: '/messageSlip/delete',
        method: 'post',
        params: { id }
    })
}
