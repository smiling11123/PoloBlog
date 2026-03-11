import { request } from "./request";

export const getAuthInfo = () => {
    return request({
        url: '/authInfo/get',
    })
}