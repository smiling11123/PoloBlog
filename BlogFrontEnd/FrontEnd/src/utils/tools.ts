import dayjs from 'dayjs'

// 工具函数
export function formatTime(timer: undefined | string) {
  return dayjs(timer).format('YYYY-MM-DD HH:mm:ss')
}