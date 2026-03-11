<template>
  <div 
    class="danmaku-item" 
    :style="itemStyle"
    @animationend="$emit('finished')"
  >
    <div class="danmaku-content">
      <span class="bullet"></span>
      {{ content }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  content: string;
  top: number;      // 轨道高度 %
  duration: number; // 滚动时长
  color: string;
}>();

defineEmits(['finished']);

const itemStyle = computed(() => ({
  top: `${props.top}%`,
  animationDuration: `${props.duration}s`,
  color: props.color,
  // 使用稍微透明的背景增强在多变背景图下的可读性
  backgroundColor: 'rgba(0, 0, 0, 0.25)',
  backdropFilter: 'blur(4px)',
}));
</script>

<style scoped lang="scss">
.danmaku-item {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  padding: 6px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  pointer-events: none;
  z-index: 1;
  will-change: transform;
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: scroll linear forwards;

  /* 强制开启 GPU 硬件加速 */
  transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
}

@keyframes scroll {
  from { transform: translateX(0); }
  to { transform: translateX(-250vw); } /* 确保彻底滚出视野 */
}
</style>
