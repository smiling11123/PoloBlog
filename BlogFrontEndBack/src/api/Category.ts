import { request } from "./request";

export const getHotCategoryList = async () => {
  return request({
    url: '/category/hotList',
  })
}

export const getCategoryList = async () => {
  return request({
    url: '/category/list',
  })
}

export const getDeletedCategoryList = async (page: number = 1, size: number = 20) => {
  return request({
    url: '/category/deletedList',
    params: { page, size },
  })
}

export const createCategory = async (data: any) => {
  return request({
    url: '/category/create',
    method: 'post',
    data,
  })
}

export const deleteCategory = async (id: string | number) => {
  return request({
    url: '/category/delete',
    method: 'post',
    params: { id },
  })
}

export const restoreCategory = async (id: string | number) => {
  return request({
    url: '/category/restore',
    method: 'post',
    params: { id },
  })
}
