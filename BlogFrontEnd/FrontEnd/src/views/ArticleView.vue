<template>
  <div class="articleContainet">
    <div class="header">
      <ArticleHeader 
        :title="articledetail?.data.title" 
        :author="articledetail?.data.auth || '匿名作者'"
        :updateTime="articledetail?.data.updateTime" 
        :viewCount="articledetail?.data.viewCount"
        :thumbnail="articledetail?.data.thumbnail" 
      />
    </div>
    
    <div class="content">
      <ArticleContent 
        :content="articledetail?.data.content" 
        class="article" 
      />
    </div>
    
    <!-- 评论区：isComment=1时显示，=0时隐藏 -->
    <div v-if="showComment" ref="commentSection" class="comment">
      <ArticleComment 
      :articleId="articleId as string"/>
    </div>

    <!-- 浮动按钮组 -->
    <div class="floating-actions">
      <button 
        v-if="showComment"
        class="floating-btn scroll-to-comments" 
        @click="scrollToComments"
        title="跳转到评论区"
      >
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path fill="currentColor" d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"/>
        </svg>
      </button>
      
      <button 
        class="floating-btn scroll-to-top" 
        @click="scrollToTop"
        title="返回顶部"
        :class="{ visible: showBackToTop }"
      >
        <svg viewBox="0 0 24 24" width="20" height="20">
          <path fill="currentColor" d="M12 4l-8 8h5v8h6v-8h5z"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { getArticleDetail } from '@/api/article';
import { useRoute } from 'vue-router';
import ArticleHeader from '@/components/Article/ArticleHeader.vue';
import ArticleContent from '@/components/Article/ArticleContent.vue';
import ArticleComment from '@/components/Article/ArticleComment.vue';
import { useThemStore } from '@/stores/them'
import { getRootComment } from '@/api/comment';

const route = useRoute()
const articledetail = ref()
const articleId = computed(() => route.params.id)
const commentSection = ref<HTMLElement | null>(null)
const showBackToTop = ref(false)
const themStore = useThemStore()

// isComment=0 时关闭评论区，其他值或未传时显示
const showComment = computed(() => {
  const val = route.query.isComment
  if (val === undefined || val === null || val === '') return true
  return Number(val) !== 0
})
// 获取文章详情
onMounted(async () => {
  
  console.log(articleId.value)
  articledetail.value = await getArticleDetail(articleId.value)
  console.log(articledetail.value)
  //获取文章评论
  //articleComment.value = await getRootComment(articleId.value)

  // 监听滚动事件
  window.addEventListener('scroll', handleScroll)
  scrollToTop()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 滚动处理：控制返回顶部按钮显示
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

// 平滑滚动到评论区
const scrollToComments = () => {
  if (commentSection.value) {
    commentSection.value.scrollIntoView({ 
      behavior: 'smooth', 
      block: 'start' 
    })
  }
}

// 平滑滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 800,
    behavior: 'smooth'
  })
}
</script>

<style scoped lang="scss">
.content {
  border-radius: 16px;
  margin-top: 20px;
}

.article {
  border-radius: 16px;
}

.comment {
  border-radius: 16px;
  margin-top: 15px;
}
/* 浮动按钮容器 */
.floating-actions {
  position: fixed;
  bottom: 40px;
  right: 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 1000;
  pointer-events: none; /* 让鼠标事件穿透容器空白处 */
}

/* 圆形按钮基础样式 */
.floating-btn {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: none;
  background: v-bind('themStore?.them?.cardBg || "#ffffff"');
  color: v-bind('themStore?.them?.textColor || "#333333"');
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 
              0 0 0 1px v-bind('themStore?.them?.cardBorder || "rgba(0,0,0,0.1)"');
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto; /* 恢复按钮的鼠标事件 */
  backdrop-filter: blur(10px);
  
  &:hover {
    transform: translateY(-4px) scale(1.1);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    background: v-bind('themStore?.them?.accentColor || "#667eea"');
    color: white;
  }
  
  &:active {
    transform: translateY(-2px) scale(0.95);
  }
  
  svg {
    transition: transform 0.3s ease;
  }
  
  &:hover svg {
    transform: scale(1.1);
  }
}

/* 跳转到评论区按钮 - 始终显示 */
.scroll-to-comments {
  opacity: 1;
  transform: translateY(0);
}

/* 返回顶部按钮 - 默认隐藏，滚动后显示 */
.scroll-to-top {
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
  
  &.visible {
    opacity: 1;
    transform: translateY(0);
    pointer-events: auto;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .floating-actions {
    bottom: 20px;
    right: 20px;
    gap: 12px;
  }
  
  .floating-btn {
    width: 44px;
    height: 44px;
    
    svg {
      width: 18px;
      height: 18px;
    }
  }
}

/* 暗色模式优化 */
@media (prefers-color-scheme: dark) {
  .floating-btn {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3),
                0 0 0 1px rgba(255, 255, 255, 0.1);
  }
}
</style>