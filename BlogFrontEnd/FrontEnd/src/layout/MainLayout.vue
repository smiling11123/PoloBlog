<template>
  <div class="app-container">
    <Transition name="fade" v-if="appStore.isShowLoginPage">
      <Login />
    </Transition>

    <div class="top-bar" :class="{ 'hide-ui': isHidden }">
      <Topbar></Topbar>
    </div>

    <div class="menu" :class="{ 'hide-ui': isHidden }">
      <div class="datetime-wrapper">
        <DateTimeCard />
      </div>
      <button class="toggleMenuPage" title="菜单" @click="appStore.toggleMenuPage">
        <Transition name="icon" mode="out-in">
          <svg v-if="!appStore.isShowMenuPage" key="menu" class="icon" viewBox="0 0 1024 1024" version="1.1"
            xmlns="http://www.w3.org/2000/svg" width="20" height="20">
            <path
              d="M832.2 264.6H192.3c-35.2 0-64-28.8-64-64s28.8-64 64-64h639.8c35.2 0 64 28.8 64 64 0.1 35.2-28.7 64-63.9 64z m0 313.3H196.1c-35.2 0-64-28.8-64-64s28.8-64 64-64h636.1c35.2 0 64 28.8 64 64s-28.8 64-64 64z m0 319H192.3c-35.2 0-64-28.8-64-64s28.8-64 64-64h639.8c35.2 0 64 28.8 64 64 0.1 35.2-28.7 64-63.9 64z"
              fill="currentColor"></path>
          </svg>
          <svg v-else key="close" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            width="15" height="15">
            <path
              d="M632.117978 513.833356l361.805812 361.735298a85.462608 85.462608 0 1 1-121.001515 120.789974L511.116463 634.552816 146.913186 998.756094a86.026718 86.026718 0 0 1-121.706652-121.706652L389.480325 512.775651 27.674513 150.969839A85.392095 85.392095 0 0 1 148.393973 30.250379L510.199785 392.056191l366.671258-366.671258a86.026718 86.026718 0 0 1 121.706652 121.706652z"
              fill="currentColor"></path>
          </svg>
        </Transition>
      </button>
      <Transition name="menu-slide">
        <div class="menu-wrapper" v-if="appStore.isShowMenuPage">
          <Menu></Menu>
        </div>
      </Transition>
    </div>

    <Header :opacity="headerOpacity"></Header>

    <div class="scroll-content">
      <Waves />
      <div class="content-body">
        <RouterView/>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useAppStore } from '../stores/app';
import Login from '../views/Login.vue';
import Header from '../components/Header.vue';
import { useUserStore } from '../stores/user';
import { useThemStore } from '../stores/them';
import Menu from '../components/Menu.vue';
import Topbar from '../components/Topbar.vue';
import Waves from '../components/Waves.vue';
import DateTimeCard from '../components/DateTimeCard.vue';

const appStore = useAppStore()
const useStore = useUserStore()
const themStore = useThemStore()

// --- 滚动逻辑 ---
const headerOpacity = ref(1)
const isHidden = ref(false) // 控制菜单显隐
let lastScrollTop = 0
let ticking = false
const oauthTokenCookieName = 'oauth_token_bridge'

const getCookieValue = (name: string) => {
  const cookie = document.cookie
    .split('; ')
    .find(item => item.startsWith(`${name}=`))

  if (!cookie) {
    return ''
  }

  return decodeURIComponent(cookie.substring(name.length + 1))
}

const clearCookie = (name: string) => {
  document.cookie = `${name}=; Max-Age=0; path=/`
}

const replaceUrlWithoutToken = () => {
  const url = new URL(window.location.href)
  if (!url.searchParams.has('token')) {
    return
  }

  url.searchParams.delete('token')
  const nextUrl = `${url.pathname}${url.search}${url.hash}`
  window.history.replaceState({}, '', nextUrl)
}

const syncOAuthToken = async () => {
  const params = new URLSearchParams(window.location.search)
  const tokenFromQuery = params.get('token') || ''
  const tokenFromCookie = getCookieValue(oauthTokenCookieName)
  const token = tokenFromCookie || tokenFromQuery

  if (!token) {
    replaceUrlWithoutToken()
    return
  }

  localStorage.setItem('Authorization', token)
  useStore.token = token
  useStore.isLogin = true

  if (tokenFromCookie) {
    clearCookie(oauthTokenCookieName)
  }

  replaceUrlWithoutToken()
  await useStore.getUserInfo()
}

const onScroll = () => {
  if (!ticking) {
    window.requestAnimationFrame(() => {
      // 兼容写法获取滚动高度
      let currentScrollTop = window.scrollY || document.documentElement.scrollTop

      // 【关键】防止 Safari/移动端 顶部回弹出现负数
      if (currentScrollTop < 0) currentScrollTop = 0;

      // 1. Header 透明度 (滚得越远越透明)
      const fadeRange = 300
      let opacity = 1 - (currentScrollTop / fadeRange)
      headerOpacity.value = Math.max(0, Math.min(1, opacity))

      // 2. 菜单显隐逻辑
      // 向下滚动 (Current > Last) 且 滚动超过 50px (避免顶部微动闪烁) -> 隐藏
      if (currentScrollTop > lastScrollTop && currentScrollTop > 50) {
        isHidden.value = true
        appStore.isShowMenuPage = false //关闭菜单
        appStore.isShowWallpaperMenuPage = false //关闭壁纸选择菜单
      }
      if(currentScrollTop > lastScrollTop && currentScrollTop > 200) {
        appStore.isShowPublishMessageSlip = false;
      }
      // 向上滚动 (Current < Last) -> 显示
      else if (currentScrollTop < lastScrollTop) {
        isHidden.value = false
      }

      // 更新位置
      lastScrollTop = currentScrollTop
      ticking = false
    })
    ticking = true
  }
}

onMounted(async () => {
  await syncOAuthToken()

  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style lang="scss">
::view-transition-old(root),
::view-transition-new(root) {
  animation: none !important;
  mix-blend-mode: normal;
}

::view-transition-new(root) {
  z-index: 9999;
}

/* 全局变量 */
.app-container {
  min-height: 100vh;
  --content-bg-solid: v-bind('themStore.them.contentBgSolid');
  --content-bg-trans: v-bind('themStore.them.contentBgTrans');
  --wavesColor1: v-bind('themStore.them.wavesColor1');
  --wavesColor2: v-bind('themStore.them.wavesColor2');
  --wavesColor3: v-bind('themStore.them.wavesColor3');
}

/* 滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: var(--content-bg-solid);
}

::-webkit-scrollbar-thumb {
  background: #555;
  border-radius: 4px;

  &:hover {
    background: #777;
  }
}

/* 动画类 */
.icon-enter-active,
.icon-leave-active {
  transition: all 0.3s ease;
}

.icon-enter-from,
.icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}

.menu-slide-enter-active,
.menu-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

.menu-slide-enter-from,
.menu-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}


.menu,
.top-bar {
  position: fixed;
  z-index: 99;
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.hide-ui {
  transform: translateY(-100%);
}

/* Topbar 位置 */
.top-bar {
  top: 0;
  left: 0;
  width: 100%;
  min-height: 60px;
}

/* 菜单按钮位置 */
.menu {
  top: 15px;
  left: 15px;
  display: flex;
  flex-direction: column;

  &.hide-ui {
    transform: translateY(-100px);
  }

  .datetime-wrapper {
    margin-bottom: 15px;
  }

  .toggleMenuPage {
    width: 45px;
    height: 45px;
    background: #2c2c2c;
    color: #ffffff;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 0;
    cursor: pointer;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
      background: #444;
    }
  }

  .menu-wrapper {
    margin-top: 10px;
    position: relative;
  }
}

/* 滚动内容区域 */
.scroll-content {
  position: relative;
  z-index: 10;
  margin-top: 100vh;
  background: linear-gradient(to bottom,
      var(--content-bg-solid) 0%,
      var(--content-bg-solid) 50px,
      var(--content-bg-trans) 300px);
  min-height: 100vh;
  padding: 40px;
  color: white;
  box-shadow: 0 30px 30px rgba(0, 0, 0, 0.5);

  @media (max-width: 768px) {
    padding: 20px;
  }
}

.content-body {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
