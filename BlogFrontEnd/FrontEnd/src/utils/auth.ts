import { ref } from 'vue'

export const FRONT_STORAGE_AUTHORIZE_KEY = 'FrontAuthorization'

const readStoredAuthorization = () => {
  if (typeof window === 'undefined') {
    return ''
  }
  return localStorage.getItem(FRONT_STORAGE_AUTHORIZE_KEY) || ''
}

export const frontAuthorization = ref<string>(readStoredAuthorization())

export const setFrontAuthorization = (token: string) => {
  frontAuthorization.value = token
  if (typeof window !== 'undefined') {
    localStorage.setItem(FRONT_STORAGE_AUTHORIZE_KEY, token)
  }
}

export const clearFrontAuthorization = () => {
  frontAuthorization.value = ''
  if (typeof window !== 'undefined') {
    localStorage.removeItem(FRONT_STORAGE_AUTHORIZE_KEY)
  }
}

export const syncFrontAuthorization = () => {
  frontAuthorization.value = readStoredAuthorization()
  return frontAuthorization.value
}
