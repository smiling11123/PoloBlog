import { request } from "../request"

export interface LoginParams {
  username: string
  password: string
  type?: 'account'
}

export interface LoginMobileParams {
  mobile: string
  code: string
  type: 'mobile'
}

export interface LoginResultModel {
  token: string
}

export function loginApi(params: LoginParams | LoginMobileParams) {
  return request({
    url: '/login/admin',
    method: 'post',
    data: params,
  })
}

export function logoutApi() {
  return request({
    url: '/login/admin/logout',
    method: 'post',
  })
}
