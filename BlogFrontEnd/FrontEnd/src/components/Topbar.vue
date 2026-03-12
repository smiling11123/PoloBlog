<template>
  <div class="bar-container">
    <div class="wallpaper-wrapper">
      <button class="action-btn" @click="toggleWallpaper" title="更换壁纸">
        <svg class="icon" viewBox="0 0 1024 1024" width="24" height="24">
          <path
            d="M853.333333 128H170.666667c-46.933333 0-85.333333 38.4-85.333334 85.333333v597.333334c0 46.933333 38.4 85.333333 85.333334 85.333333h682.666666c46.933333 0 85.333333-38.4 85.333334-85.333333V213.333333c0-46.933333-38.4-85.333333-85.333334-85.333333z m0 682.666667H170.666667V213.333333h682.666666v597.333334zM661.333333 362.666667c35.413333 0 64-28.586667 64-64s-28.586667-64-64-64-64 28.586667-64 64 28.586667 64 64 64zM213.333333 725.333333h597.333334l-170.666667-256-128 170.666667-106.666667-128-192 213.333333z"
            fill="currentColor"></path>
        </svg>
      </button>

      <Transition name="slide-fade">
        <div v-if="appStore.isShowWallpaperMenuPage" class="wallpaper-dropdown">
          <div class="dropdown-header">
            <span>选择壁纸</span>
            <span class="page-indicator">{{ currentPage }} / {{ totalPages || 1 }}</span>
          </div>

          <div class="wallpaper-grid" v-loading="loading">
            <div v-for="item in wallpaperList" :key="item.id" class="wallpaper-item"
              @click="handleSelectWallpaper($event, item.address)">
              <img :src="item.address" :alt="item.name" loading="lazy">
              <div class="item-mask">
                <span>应用</span>
              </div>
            </div>
            <div v-for="i in (6 - wallpaperList.length)" :key="'empty-' + i" class="wallpaper-item empty"
              v-if="wallpaperList.length < 6"></div>
          </div>

          <div class="pagination-ctrl">
            <button :disabled="currentPage <= 1" @click="changePage(-1)" class="page-btn">
              <svg viewBox="0 0 1024 1024" width="16" height="16">
                <path d="M671.2 160.5L315.5 512l355.7 351.5 62.8-63.1L441.2 512l292.8-288.3z" fill="currentColor">
                </path>
              </svg>
            </button>
            <button :disabled="currentPage >= totalPages" @click="changePage(1)" class="page-btn">
              <svg viewBox="0 0 1024 1024" width="16" height="16">
                <path d="M352.8 160.5l355.7 351.5-355.7 351.5-62.8-63.1L582.8 512 290 223.6z" fill="currentColor">
                </path>
              </svg>
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <div class="theme-wrapper">
      <button class="toggle-switch" :class="{ 'is-dark': themStore.themModel === 'dark' }"
        @click="themStore.themChange($event)" title="切换主题">
        <div class="toggle-thumb">
          <span class="crater" v-if="themStore.themModel === 'dark'"></span>
          <span class="crater" v-if="themStore.themModel === 'dark'"></span>
        </div>
        <div class="star star-1"></div>
        <div class="star star-2"></div>
      </button>
    </div>

    <div class="social-wrapper">
      <a href="https://github.com/smiling11123" target="_blank" class="social-btn github">
        <svg class="icon" viewBox="0 0 1024 1024" width="24" height="24">
          <path
            d="M512 42.666667A464.64 464.64 0 0 0 42.666667 502.186667 460.373333 460.373333 0 0 0 363.52 938.666667c23.466667 4.266667 32-9.813333 32-22.186667v-78.08c-130.56 27.733333-158.293333-61.44-158.293333-61.44a122.026667 122.026667 0 0 0-52.053334-67.413333c-42.666667-28.16 3.413333-27.733333 3.413334-27.733334a98.56 98.56 0 0 1 71.68 47.36 101.12 101.12 0 0 0 136.533333 37.973334 99.413333 99.413333 0 0 1 29.866667-61.44c-104.106667-11.52-213.333333-50.773333-213.333334-226.986667a177.066667 177.066667 0 0 1 47.36-124.16 161.28 161.28 0 0 1 4.693334-121.173333s39.68-12.373333 128 46.933333a455.68 455.68 0 0 1 234.666666 0c89.6-59.306667 128-46.933333 128-46.933333a161.28 161.28 0 0 1 4.693334 121.173333A177.066667 177.066667 0 0 1 810.666667 477.866667c0 176.64-110.08 215.466667-213.333334 226.986666a106.666667 106.666667 0 0 1 32 85.333334v125.866666c0 14.933333 8.533333 26.88 32 22.186667A460.8 460.8 0 0 0 981.333333 502.186667 464.64 464.64 0 0 0 512 42.666667"
            fill="currentColor"></path>
        </svg>
        <span>Github</span>
      </a>

      <a href="https://b23.tv/1e6TAcC" target="_blank" class="social-btn bilibili">
        <svg class="icon" viewBox="0 0 1129 1024" width="24" height="24">
          <path
            d="M234.909 9.656a80.468 80.468 0 0 1 68.398 0 167.374 167.374 0 0 1 41.843 30.578l160.937 140.82h115.07l160.936-140.82a168.983 168.983 0 0 1 41.843-30.578A80.468 80.468 0 0 1 930.96 76.445a80.468 80.468 0 0 1-17.703 53.914 449.818 449.818 0 0 1-35.406 32.187 232.553 232.553 0 0 1-22.531 18.508h100.585a170.593 170.593 0 0 1 118.289 53.109 171.397 171.397 0 0 1 53.914 118.288v462.693a325.897 325.897 0 0 1-4.024 70.007 178.64 178.64 0 0 1-80.468 112.656 173.007 173.007 0 0 1-92.539 25.75h-738.7a341.186 341.186 0 0 1-72.421-4.024A177.835 177.835 0 0 1 28.91 939.065a172.202 172.202 0 0 1-27.36-92.539V388.662a360.498 360.498 0 0 1 0-66.789A177.03 177.03 0 0 1 162.487 178.64h105.414c-16.899-12.07-31.383-26.555-46.672-39.43a80.468 80.468 0 0 1-25.75-65.984 80.468 80.468 0 0 1 39.43-63.57M216.4 321.873a80.468 80.468 0 0 0-63.57 57.937 108.632 108.632 0 0 0 0 30.578v380.615a80.468 80.468 0 0 0 55.523 80.469 106.218 106.218 0 0 0 34.601 5.632h654.208a80.468 80.468 0 0 0 76.444-47.476 112.656 112.656 0 0 0 8.047-53.109v-354.06a135.187 135.187 0 0 0 0-38.625 80.468 80.468 0 0 0-52.304-54.719 129.554 129.554 0 0 0-49.89-7.242H254.22a268.764 268.764 0 0 0-37.82 0z m0 0"
            fill="#20B0E3"></path>
          <path
            d="M348.369 447.404a80.468 80.468 0 0 1 55.523 18.507 80.468 80.468 0 0 1 28.164 59.547v80.468a80.468 80.468 0 0 1-16.094 51.5 80.468 80.468 0 0 1-131.968-9.656 104.609 104.609 0 0 1-10.46-54.719v-80.468a80.468 80.468 0 0 1 70.007-67.593z m416.02 0a80.468 80.468 0 0 1 86.102 75.64v80.468a94.148 94.148 0 0 1-12.07 53.11 80.468 80.468 0 0 1-132.773 0 95.757 95.757 0 0 1-12.875-57.133V519.02a80.468 80.468 0 0 1 70.007-70.812z m0 0"
            fill="#20B0E3"></path>
        </svg>
        <span>Bilibili</span>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useThemStore } from '../stores/them';
import { getWallpapaerList } from '@/api/wallpaper';
import { useAppStore } from '@/stores/app';

const themStore = useThemStore()
const wallpaperList = ref<any[]>([])
const loading = ref(false)
const appStore = useAppStore()

// 分页相关状态
const currentPage = ref(1)
const totalPages = ref(0)
const pageSize = 6 // 3列 * 2行

// 获取数据函数
const fetchWallpapers = async (page: number) => {
  loading.value = true
  try {
    const res = await getWallpapaerList({ page: page, size: pageSize })
    wallpaperList.value = res.data.records
    // 根据后端返回的总数计算总页数
    totalPages.value = Math.ceil(res.data.total / pageSize)
    currentPage.value = page
  } catch (error) {
    console.error("加载壁纸失败:", error)
  } finally {
    loading.value = false
  }
}

// 切换下拉列表显示
const toggleWallpaper = () => {
  appStore.toggleWallpaperMenupage()
  if (appStore.isShowWallpaperMenuPage && wallpaperList.value.length === 0) {
    fetchWallpapers(1)
  }
}

// 翻页处理
const changePage = (offset: number) => {
  const targetPage = currentPage.value + offset
  if (targetPage >= 1 && targetPage <= totalPages.value) {
    fetchWallpapers(targetPage)
  }
}

// 选择壁纸并触发扩散动画
const handleSelectWallpaper = (e: MouseEvent, url: string) => {
  themStore.changeHeaderImg(e, url)
  appStore.isShowWallpaperMenuPage = false
}
</script>

<style scoped lang="scss">
.bar-container {
  position: fixed;
  top: 15px;
  right: 30px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 20px;
  max-width: calc(100vw - 140px);
  justify-content: flex-end;
}

.wallpaper-wrapper {
  position: relative;
  flex-shrink: 0;

  .action-btn {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.2);
    background: v-bind('themStore.them.wavesColor2');
    color: v-bind('themStore.them.textColor');
    backdrop-filter: blur(8px);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.wallpaper-dropdown {
  position: absolute;
  top: 55px;
  right: 0;
  width: 380px; // 稍微加宽以适应3列
  background: v-bind('themStore.them.wavesColor2');
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  padding: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  z-index: 1001;

  .dropdown-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    font-weight: bold;
    color: v-bind('themStore.them.textColor');
    margin-bottom: 12px;
    padding: 0 5px;

    .page-indicator {
      font-size: 12px;
      opacity: 0.7;
      font-weight: normal;
    }
  }

  /* 3列2行的网格布局 */
  .wallpaper-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr); // 3列
    grid-template-rows: repeat(2, 100px); // 2行，每行固定100px高
    gap: 10px;
    min-height: 210px; // 保持容器高度稳定
  }

  .wallpaper-item {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s ease;
    background: rgba(255, 255, 255, 0.05);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .item-mask {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;
      color: #fff;
      font-size: 12px;
    }

    &:hover:not(.empty) {
      border-color: #20B0E3;

      .item-mask {
        opacity: 1;
      }
    }

    &.empty {
      cursor: default;
    }
  }

  /* 分页按钮栏 */
  .pagination-ctrl {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 15px;
    padding-top: 10px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    .page-btn {
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: v-bind('themStore.them.textColor');
      width: 30px;
      height: 30px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.2s;

      &:hover:not(:disabled) {
        background: rgba(255, 255, 255, 0.2);
        color: #20B0E3;
      }

      &:disabled {
        opacity: 0.3;
        cursor: not-allowed;
      }
    }
  }
}

/* 保持原有样式不变 */
.theme-wrapper {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.toggle-switch {
  position: relative;
  width: 60px;
  height: 30px;
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background-color: #8fbcd4;
  cursor: pointer;
  transition: all 0.5s ease;
  overflow: hidden;
  padding: 0;
  box-shadow: inset 2px 2px 5px rgba(0, 0, 0, 0.3);

  &.is-dark {
    background-color: #0f2027;

    .toggle-thumb {
      transform: translateX(30px);
      background-color: #f1c40f;
    }
  }
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 24px;
  height: 24px;
  background-color: #ffeb3b;
  border-radius: 50%;
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.27, 1.55);

  .crater {
    position: absolute;
    background: rgba(0, 0, 0, 0.15);
    border-radius: 50%;

    &:nth-child(1) {
      width: 5px;
      height: 5px;
      top: 5px;
      left: 8px;
    }

    &:nth-child(2) {
      width: 3px;
      height: 3px;
      bottom: 6px;
      right: 5px;
    }
  }
}

.social-wrapper {
  display: flex;
  gap: 15px;
  flex-shrink: 0;

  .social-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 15px;
    border-radius: 20px;
    background: v-bind('themStore.them.wavesColor2');
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    text-decoration: none;
    color: v-bind('themStore.them.textColor');
    font-size: 14px;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-2px);
    }
  }
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px) scale(0.95);
  opacity: 0;
}

@media (max-width: 768px) {
  .bar-container {
    top: 12px;
    right: 12px;
    gap: 10px;
    max-width: calc(100vw - 96px);
  }

  .wallpaper-wrapper {
    .action-btn {
      width: 36px;
      height: 36px;
    }
  }

  .wallpaper-dropdown {
    width: min(320px, calc(100vw - 24px));
    right: -6px;
    padding: 12px;

    .wallpaper-grid {
      grid-template-rows: repeat(2, 88px);
      gap: 8px;
      min-height: 184px;
    }
  }

  .toggle-switch {
    width: 52px;
    height: 28px;
    border-radius: 14px;

    &.is-dark {
      .toggle-thumb {
        transform: translateX(24px);
      }
    }
  }

  .toggle-thumb {
    width: 22px;
    height: 22px;
  }

  .social-wrapper {
    gap: 8px;

    .social-btn {
      width: 36px;
      height: 36px;
      padding: 0;
      border-radius: 50%;
      justify-content: center;

      span {
        display: none;
      }
    }
  }
}

@media (max-width: 480px) {
  .bar-container {
    max-width: calc(100vw - 78px);
    gap: 8px;
  }

  .wallpaper-dropdown {
    right: -4px;
  }
}
</style>
