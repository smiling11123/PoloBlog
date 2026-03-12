<template>
  <div class="datetime-card" :style="{
      backgroundColor: themStore.them.wavesColor2,
      borderColor: 'rgba(255, 255, 255, 0.1)',
      color: themStore.them.textColor
    }">
    <div class="time">{{ currentTime }}</div>
    <div class="date-wrapper" :style="{ color: themStore.them.textSecondary }">
      <span class="date">{{ currentDate }}</span>
      <span class="day">{{ currentDay }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useThemStore } from '@/stores/them'

const themStore = useThemStore()

const currentTime = ref('')
const currentDate = ref('')
const currentDay = ref('')

const updateTime = () => {
  const now = new Date()

  // 时间格式: HH:mm:ss
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })

  // 日期格式: YYYY-MM-DD
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  currentDate.value = `${year}-${month}-${date}`

  // 星期格式
  const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  currentDay.value = days[now.getDay()] as any
}

let timer: number | null = null

onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped lang="scss">
.datetime-card {
  padding: 10px 18px;
  border-radius: 12px;
  border: 1px solid;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  user-select: none;
  transition: all 0.3s ease;
  min-width: 130px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  }

  .time {
    font-size: 1.4rem;
    font-weight: 700;
    font-family: 'Courier New', Courier, monospace;
    letter-spacing: 1px;
    line-height: 1.2;
  }

  .date-wrapper {
    display: flex;
    gap: 8px;
    font-size: 0.75rem;
    font-weight: 500;
    margin-top: 2px;
  }
}

@media (max-width: 768px) {
  .datetime-card {
    padding: 6px 10px;
    min-width: 88px;
    border-radius: 10px;

    .time {
      font-size: 1rem;
      letter-spacing: 0.5px;
    }

    .date-wrapper {
      gap: 4px;
      font-size: 0.6rem;
    }
  }
}

@media (max-width: 576px) {
  .datetime-card {
    min-width: 74px;
    padding: 6px 8px;

    .time {
      font-size: 0.95rem;
    }

    .date-wrapper {
      display: none;
    }
  }
}
</style>
