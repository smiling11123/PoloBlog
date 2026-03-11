import { request } from "./request";

export const getMessageSlipList = async (params: any) => {
    return request({
        url: '/messageSlip/list',
        params: {page: params.page, size: params.size}
    })
}

export const getMessageSlipToShow = async (params: any) => {
    return request({
        url: '/messageSlip/showList',
        params: {num: params}
    })
}

export const publishMessageSlip = async (params: any) => {
    return request({
        url: '/messageSlip/publish',
        params: { content: params.content, userId: params.userId },
        method: "POST",
    })
}