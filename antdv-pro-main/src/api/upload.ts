import { request } from "./request";

export const uploadFile = async (file: any) => {
    const formData = new FormData()
  
    formData.append('file', file) 

    return request({
        url: '/upload/file',
        method: 'POST',
        data: formData,
    })
}