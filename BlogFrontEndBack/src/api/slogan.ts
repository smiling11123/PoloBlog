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

export const getDeletedSloganList = async (page: number = 1, size: number = 20) => {
  return request({
    url: '/slogan/deletedList',
    method: 'get',
    params: { page, size },
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

export const restoreSlogan = async (id: string | number) => {
  return request({
    url: '/slogan/restore',
    method: 'post',
    params: { id },
  })
}
