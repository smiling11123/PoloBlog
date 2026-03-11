import { get } from 'lodash-es'
import router from '@/router'
import dayjs from 'dayjs'
export function getQueryParam(param: string | string[], defaultVal = '') {
  const query = router.currentRoute.value?.query ?? {}
  const val = get(query, param) ?? defaultVal
  return decodeURIComponent(val)
}

// 工具函数
export function formatTimer(timer: undefined | string) {
  return dayjs(timer).format('YYYY-MM-DD HH:mm:ss')
}