import { request } from "./request";
//获取第三方授权地址
export const getauthUrl = async (params: string) => {
  return request({
    url: '/login/url',
    params: {type: params},
  })
}
//获取用户信息
export const getUserDetail = async () => {
 return request({
  url: '/user/detail',
 })
}