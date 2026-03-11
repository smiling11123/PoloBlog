import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
export const useAppStore = defineStore('app', () => {
  const isShowLoginPage = ref(false)
  const isShowMenuPage = ref(false)
  const isShowWallpaperMenuPage = ref(false)
  const isShowPublishMessageSlip = ref(false)
  //打开或关闭登录页面
  const toggleLoginPage = () => {
    isShowLoginPage.value = isShowLoginPage.value ? false : true

  }
  //打开或关闭菜单
  const toggleMenuPage = () => {
    isShowMenuPage.value = isShowMenuPage.value ? false : true
  }
  //打开或关闭壁纸选择菜单
  const toggleWallpaperMenupage = () => {
    isShowWallpaperMenuPage.value = isShowWallpaperMenuPage.value ? false : true
  }

  const toggleMessageSlipPublishPage = () => {
    isShowPublishMessageSlip.value = isShowPublishMessageSlip.value ? false : true
  }
  return {
    isShowLoginPage,
    isShowMenuPage,
    isShowWallpaperMenuPage,
    isShowPublishMessageSlip,
    toggleLoginPage,
    toggleMenuPage,
    toggleWallpaperMenupage,
    toggleMessageSlipPublishPage,
  }
})
