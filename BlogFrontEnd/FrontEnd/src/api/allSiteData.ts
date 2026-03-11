import { request } from "./request";

export const getAllSiteData = async () => {
    return request({
        url: '/allSiteData/get',
    })
}