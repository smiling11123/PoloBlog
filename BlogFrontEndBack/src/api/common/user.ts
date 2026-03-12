import { request } from "../request"

export interface UserInfo {
  id: number | string
  username: string
  nickname: string
  avatar: string
  email: string
  intro: string
  loginType: string
  source: string
  status: string
  statusText: string
  roleName: string
  roleKey: string
  createTime?: string
  updateTime?: string
  loginDate?: string
}

export interface UpdateUserPayload {
  id: number | string
  username: string
  nickname: string
  email: string
  avatar: string
  intro: string
  source?: string
  status?: string
  isDeleted?: number
}

export function getUserInfoApi() {
  return request({
    url: '/user/detail',
  })
}

export const getUserList = async (params: any)=> {
  return request({
    url: '/user/list',
    params: {page: params.page, size: params.size}
  })
}

export const getDeletedUserList = async (params: any) => {
  return request({
    url: '/user/deletedUserList',
    params: {page: params.page, size: params.size}
  })
}

export const searchUser = async (params: any) => {
  return request({
    url: '/user/search',
    params: {page: params.page, size: params.size, keyword: params.keyword}
  })
}

export const getUserDetail = async (params: string) => {
  return request({
    url: '/user/detailById',
    params: {id: params}
  })
}

export const deleteUser = async (params: string) => {
  return request({
    url: '/user/deleteById',
    params: {id: params},
    method: 'POST',
  })
}

export const restoreUserById = async (params: string | number) => {
  return request({
    url: '/user/restoreById',
    params: { id: params },
    method: 'POST',
  })
}

export const updateUserInfo = async (params: UpdateUserPayload) => {
  return request({
    url: '/user/update',
    data: params,
    method: 'POST'
  })
}

export const updatePassWord = async (params: any) => {
  return request({
    url: '/user/updatePassword',
    data: {
      oldPassword: params.oldPassword,
      newPassword: params.newPassword,
    },
    method: 'POST',
  })
}
