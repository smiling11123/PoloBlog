import { request } from "./request";

export const getTagList = async () => {
    return request({
        url: '/tag/list',
    })

}