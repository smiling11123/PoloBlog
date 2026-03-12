import axios from "axios";
//import router from '@/router'
//import { useUserStore } from "@/stores/user";
//const userStore = useUserStore()
//import { useUserStore } from "~@/stores/user";
//const userStore = useUserStore()
import { useAppStore } from "@/stores/app";
import { clearFrontAuthorization, frontAuthorization, syncFrontAuthorization } from "@/utils/auth";

export const request = axios.create({
  baseURL: '/api',
  timeout: 0
})



//请求拦截器：发送请求前执行
request.interceptors.request.use(
  (config) => {
    const token = frontAuthorization.value || syncFrontAuthorization()

    if (token) {
      config.headers['Authorization'] = token
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)



//响应拦截器：收到响应后执行
request.interceptors.response.use(
  (response) => {
    // 200 正常放行

    return response.data
  },
  (error) => {
    // 检查是否有响应
    if (error.response) {
      const { status } = error.response

      //核心逻辑：遇到 401 代表 Token 失效
      if (status === 401) {
        const appStore = useAppStore()
        appStore.isShowLoginPage = true
        clearFrontAuthorization()
      }
    }
    return Promise.reject(error)
  }
)
