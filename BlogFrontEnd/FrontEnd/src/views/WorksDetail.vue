<template>
  <div class="works-detail-container animate-in">
    <!-- 背景装饰 -->
    <div class="grid-background"></div>

    <div class="header-section">
      <ArticleHeader
        :title="workDetail?.title"
        :updateTime="workDetail?.createTime"
        :thumbnail="workDetail?.thumbnail"
      />
    </div>

    <div class="content">
      <ArticleContent
        :content="workDetail?.content"
        class="work-content plain-links"
      />
    </div>

    <!-- 底部操作悬浮栏 -->
    <div class="floating-actions-bar" :class="{ 'bar--visible': workDetail }">
      <div class="actions-wrapper">
        <button v-if="workDetail?.address && workDetail.address.startsWith('http')"
          class="glass-btn primary-btn" @click="handleVisit">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path fill="currentColor" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
          </svg>
          <span>查看演示</span>
        </button>

        <button class="glass-btn secondary-btn" @click="handleCopy">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path fill="currentColor" d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
          </svg>
          <span :style="{color: themStore.them.textColor}">获取链接</span>
        </button>

        <div class="divider"></div>

        <button class="glass-btn back-btn" @click="router.back()">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path fill="currentColor" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWorksDetail } from '@/api/works'
import ArticleHeader from '@/components/Article/ArticleHeader.vue'
import ArticleContent from '@/components/Article/ArticleContent.vue'
import { useThemStore } from '@/stores/them'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const themStore = useThemStore()
const workDetail = ref<any>(null)
const workId = computed(() => route.params.id)

onMounted(async () => {
  try {
    const res = await getWorksDetail(workId.value)
    workDetail.value = res.data
    // 自动跳转到内容区域
    setTimeout(() => {
      const contentElement = document.querySelector('.works-detail-container')
      if (contentElement) {
        contentElement.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
  } catch (error) {
    console.error('获取作品详情失败', error)
    ElMessage.error('获取作品详情失败')
  }
})

const handleVisit = () => {
  if (workDetail.value?.address) {
    window.open(workDetail.value.address, '_blank')
  }
}

const handleCopy = async () => {
  const link = workDetail.value?.address || window.location.href
  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success({
      message: '作品链接已复制',
      type: 'success',
      plain: true,
    })
  } catch (err) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.works-detail-container {
  max-width: 920px;
  margin: 0 auto;
  padding: 20px 20px 120px;
  position: relative;
  min-height: 100vh;
}

.grid-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(v-bind('themStore.them.cardBorder') 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.2;
  pointer-events: none;
  z-index: -1;
}

.header-section {
  margin-top: 20px;
  z-index: 1;
}

.content {
  margin-top: 20px;
  border-radius: 16px;
  z-index: 1;
  position: relative;
}

.work-content {
  border-radius: 16px;
}

/* 底部浮动栏 */
.floating-actions-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translate(-50%, 100%);
  padding: 24px;
  z-index: 100;
  transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.bar--visible {
    transform: translate(-50%, 0);
  }
}

.actions-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: v-bind('themStore.them.cardBg');
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid v-bind('themStore.them.cardBorder');
  border-radius: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2), v-bind('themStore.them.cardShadow');
}

.glass-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 30px;
  border: none;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: v-bind('themStore.them.textColor');
  background: transparent;

  &.primary-btn {
    background: v-bind('themStore.them.accentColor');
    color: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    }
  }

  &.secondary-btn {
    background: v-bind('themStore.them.wavesColor2');

    &:hover {
      transform: translateY(-4px);
      background: v-bind('themStore.them.cardBgHover');
      color: v-bind('themStore.them.accentColor');
    }
  }

  &.back-btn {
    width: 48px;
    height: 48px;
    padding: 0;
    justify-content: center;
    border-radius: 50%;

    &:hover {
      background: v-bind('themStore.them.cardBgHover');
      transform: rotate(-15deg);
    }
  }
}

.divider {
  width: 1px;
  height: 24px;
  background: v-bind('themStore.them.cardBorder');
  margin: 0 4px;
}

/* 动效 */
.animate-in {
  animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .works-detail-container {
    padding: 20px 15px 140px;
  }

  .actions-wrapper {
    padding: 8px;
    gap: 8px;
  }

  .glass-btn span {
    display: none;
  }

  .glass-btn {
    padding: 12px;
  }
}
</style>
