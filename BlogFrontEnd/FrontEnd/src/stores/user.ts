import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { AuthInfo, userInfo } from '@/type/Interface'
import { getUserDetail } from '@/api/user'
import { getAuthInfo } from '@/api/authInfo'
import { getSloganList } from '@/api/slogan'
export const useUserStore = defineStore('user', () => {
  const userInfo = ref<userInfo>()
  const token = ref<string>()
  const isLogin = ref(false)
  const authInfo = ref<AuthInfo>()
  const displaySlogans = ref<string[]>([])

  const getUserInfo = async () => {
    const res = await getUserDetail()
    userInfo.value = res.data
  }

  const loadAuthInfo = async () => {
    const res = await getAuthInfo() as any
    if (res.code === 200 && res.data) {
      authInfo.value = res.data
      if (typeof res.data.slogan === 'string') {
        authInfo.value!.slogan = res.data.slogan
          .split(/[,，]/)
          .map((item: string) => item.trim())
          .filter((item: string) => item.length > 0)
      }
    }
  }

  const loadDisplaySlogans = async () => {
    const res = await getSloganList() as any
    if (res.code === 200) {
      displaySlogans.value = (res.data || [])
        .map((item: { content?: string }) => typeof item.content === 'string' ? item.content.trim() : '')
        .filter((item: string) => item.length > 0)
    }
  }

  return {
    userInfo,
    authInfo,
    displaySlogans,
    token,
    isLogin,
    getUserInfo,
    loadAuthInfo,
    loadDisplaySlogans,
  }
},
  {
    persist: true
  })
