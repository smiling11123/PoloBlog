import axios from "axios";
//import router from '@/router'
//import { useUserStore } from "@/stores/user";
//const userStore = useUserStore()
//import { useUserStore } from "~@/stores/user";
//const userStore = useUserStore()
import { useAppStore } from "@/stores/app";

export const request = axios.create({
  baseURL: '/api',
  timeout: 0
})



//请求拦截器：发送请求前执行
request.interceptors.request.use(
  (config) => {
    // 1. 从 localStorage 或 Store 取 Token
    const token = localStorage.getItem('Authorization')

    if (token) {
      // 2. 这里的 Key (Authorization) 必须和后端拦截器里取的一致
      // 有些后端要求格式为 'Bearer ' + token
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
        // 检测到 401 说明 Token 无效或过期
        //userStore.userInfo = undefined;
        const appStore = useAppStore()
        appStore.isShowLoginPage = true
        // 1. 清除本地脏数据
        localStorage.removeItem('Authorization')
        // if(userStore) {
        //   userStore.isLogin = false
        //   userStore.userInfo = undefined
        //   userStore.token = undefined
        // }
        // 2. 强制跳转登录页
        //location.reload() // 或者用 router.push
        //router.push("/login")
      }
    }
    return Promise.reject(error)
  }
)
