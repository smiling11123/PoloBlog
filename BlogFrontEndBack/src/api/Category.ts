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