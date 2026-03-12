<template>
  <div class="header-container">
    <img
      v-if="thumbnail"
      class="bg-layer"
      :src="getOptimizedImageUrl(thumbnail, 'md')"
      :srcset="buildImageSrcSet(thumbnail)"
      sizes="(max-width: 768px) 100vw, 960px"
      :alt="title"
      decoding="async"
      @error="fallbackToOriginalImage($event, thumbnail)"
    >
    <div class="bg-mask"></div>

    <div class="content-wrapper">
      <div class="text-content">
        <h1 class="title">{{ title }}</h1>
        
        <div class="meta-data">
          <div class="divider">/</div>
          <div class="meta-item">
            <span>发布于 {{ formatTime(updateTime) }}</span>
          </div>
          <div class="divider">/</div>
          <div class="meta-item">
            <span>{{ viewCount }} 阅读</span>
          </div>
        </div>
      </div>

      <div class="cover-content">
        <div class="cover-wrapper">
          <img
            :src="getOptimizedImageUrl(thumbnail, 'md')"
            :srcset="buildImageSrcSet(thumbnail)"
            sizes="(max-width: 768px) 100vw, 240px"
            alt="cover"
            loading="lazy"
            decoding="async"
            @error="fallbackToOriginalImage($event, thumbnail)"
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatTime } from '@/utils/tools';
import { useThemStore } from '@/stores/them';
import { buildImageSrcSet, fallbackToOriginalImage, getOptimizedImageUrl } from '@/utils/image';

const themStore = useThemStore()

interface Props {
  thumbnail?: string
  title?: string
  updateTime?: string
  viewCount?: number
}

withDefaults(defineProps<Props>(), {
  thumbnail: '',
  title: '无标题',
  viewCount: 0
})
</script>

<style scoped lang="scss">
.header-container {
  position: relative;
  width: 100%;
  max-width: 920px;
  min-height: 180px; 
  border-radius: 16px;
  margin: 0 auto;
  overflow: hidden;
  background-color: v-bind('themStore.them.articleBg');
  color: #fff;
  user-select: none;
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.5);
}

.bg-layer {
  position: absolute;
  top: -20px;
  left: -20px;
  width: calc(100% + 40px);
  height: calc(100% + 40px);
  object-fit: cover;
  filter: blur(40px) saturate(150%) brightness(0.55);
  z-index: 1;
  transform: scale(1.1); 
  transition: all 0.5s ease;
}

.bg-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.4) 100%);
  z-index: 2;
}

.content-wrapper {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px;
  gap: 40px;
  height: 100%;
}

.text-content {
  flex: 1; 
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;

  .title {
    margin: 0;
    font-size: 2rem;
    line-height: 1.3;
    font-weight: 700;
    text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.meta-data {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

  .divider {
    opacity: 0.4;
    font-size: 0.8em;
  }
  
  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.cover-content {
  flex-shrink: 0;
}

.cover-wrapper {
  width: 240px;
  height: 135px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.02);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

@media (max-width: 768px) {
  .header-container {
    border-radius: 18px;
    padding: 0;
  }

  .content-wrapper {
    flex-direction: column-reverse; 
    padding: 20px;
    gap: 20px;
    text-align: left;
  }

  .text-content {
    width: 100%;
    
    .title {
      font-size: 1.5rem;
    }
  }

  .cover-content {
    width: 100%;
  }

  .cover-wrapper {
    width: 100%;
    height: auto;
    aspect-ratio: 16/9; 
  }
  
  .meta-data {
    font-size: 0.8rem;
    gap: 8px;
  }
}
</style>
