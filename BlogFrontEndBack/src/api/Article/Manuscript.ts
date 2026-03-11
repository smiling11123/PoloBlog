import { request } from "../request";

export const getManuscriptList = async (params: any) => {
    return request({
      url: '/article/manuscript',
      params: params,
    })
}