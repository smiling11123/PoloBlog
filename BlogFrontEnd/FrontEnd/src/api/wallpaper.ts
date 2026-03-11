import { request } from "./request";

export const getWallpapaerList = async (params: any) => {
    return request({
        url: '/wallpaper/list',
        params: {page: params.page, size: params.size}
    })
}