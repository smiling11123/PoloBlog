import { request } from "./request";

export const getWallpaperList = async (page: number = 1, size: number = 6) => {
    return request({
        url: '/wallpaper/list',
        method: 'get',
        params: {
            page,
            size
        }
    })
}

export const getDeletedWallpaperList = async (page: number = 1, size: number = 10) => {
    return request({
        url: '/wallpaper/deletedList',
        method: 'get',
        params: {
            page,
            size
        }
    })
}

export const deleteWallpaper = async (id: number) => {
    return request({
        url: '/wallpaper/delete',
        method: 'post',
        params: { id }
    })
}

export const restoreWallpaper = async (id: number | string) => {
    return request({
        url: '/wallpaper/restore',
        method: 'post',
        params: { id }
    })
}

export const uploadWallpaper = async (name: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request({
        url: '/wallpaper/upload',
        method: 'post',
        params: { name },
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
