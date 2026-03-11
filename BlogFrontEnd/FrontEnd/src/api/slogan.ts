import { request } from "./request";

export const getSloganList = () => {
    return request({
        url: '/slogan/list',
    })
}
