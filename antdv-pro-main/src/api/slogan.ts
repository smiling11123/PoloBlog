import { request } from "./request";

export interface SloganDTO {
  content: string
}

export const getSloganList = async () => {
  return request({
    url: '/slogan/list',
    method: 'get',
  })
}

export const publishSlogan = async (data: SloganDTO) => {
  return request({
    url: '/slogan/publish',
    method: 'post',
    data,
  })
}

export const deleteSlogan = async (id: string | number) => {
  return request({
    url: '/slogan/delete',
    method: 'post',
    params: { id },
  })
}
