<script setup lang="ts">
import type { LoginParams } from '~@/api/common/login'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue' // 引入图标
import { delayTimer } from '@v-c/utils'
import { AxiosError } from 'axios'
import { nextTick } from 'vue'
import pageBubble from '@/utils/page-bubble'
import { loginApi } from '~/api/common/login'
import { getQueryParam } from '~/utils/tools'

const notification = useNotification()
const appStore = useAppStore()
const { layoutSetting } = storeToRefs(appStore)
const userStore = useUserStore()
const router = useRouter()
const token = useAuthorization()

// 表单数据
const loginModel = reactive({
  username: undefined,
  password: undefined,
  mobile: undefined,
  code: undefined,
  type: 'account',
  remember: true,
})

const { t } = useI18nLocale()
const formRef = shallowRef()
const submitLoading = shallowRef(false)
const errorAlert = shallowRef(false)
const bubbleCanvas = ref<HTMLCanvasElement>()
const showBubbleCanvas = ref(false)
let bubbleStarted = false

// --- 新增状态 ---
const isRegisterMode = ref(false) // 控制是否处于注册模式

async function submit() {
  submitLoading.value = true
  try {
    await formRef.value?.validate()
    const params = {
      username: loginModel.username,
      password: loginModel.password,
    } as unknown as LoginParams
    
    const res = await loginApi(params) as any
    
    if (res.code === 200 && res.data) {
      userStore.name = params.username
      token.value = res.data // 保存Token
      notification.success({
        message: '登录成功',
        description: '欢迎回来！',
        duration: 3,
      })
      submitLoading.value = false
      // 获取当前是否存在重定向的链接，如果存在就走重定向的地址
      const redirect = getQueryParam('redirect', '/')
      router.replace(redirect)
    }
    else {
      notification.error({
        message: '登录失败',
        description: res.msg || '账号或密码错误！',
        duration: 3,
      })
      submitLoading.value = false
    }
  }
  catch (e) {
    if (e instanceof AxiosError)
      errorAlert.value = true
    submitLoading.value = false
  }
}

onMounted(async () => {
  await delayTimer(300)
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  showBubbleCanvas.value = window.innerWidth >= 1200 && !prefersReducedMotion

  if (!showBubbleCanvas.value)
    return

  await nextTick()
  window.requestAnimationFrame(() => {
    if (bubbleCanvas.value) {
      pageBubble.init(unref(bubbleCanvas)!)
      bubbleStarted = true
    }
  })
})

onBeforeUnmount(() => {
  if (bubbleStarted)
    pageBubble.removeListeners()
})
</script>

<template>
  <div class="login-container">
    <div v-if="showBubbleCanvas" h-screen w-screen absolute z-10>
      <canvas ref="bubbleCanvas" />
    </div>
    <div class="login-content flex-center">
      <div class="ant-pro-form-login-main rounded">
        <div class="flex-between h-15 px-4 mb-[2px]">
          <div class="flex-end">
            <span class="ant-pro-form-login-logo">
              <img w-full h-full object-cover src="/logo.svg">
            </span>
            <span class="ant-pro-form-login-title">
              blog后台管理
            </span>
          </div>
          <div class="login-lang flex-center relative z-11">
            <span class="flex-center cursor-pointer text-16px"
              @click="appStore.toggleTheme(layoutSetting.theme === 'dark' ? 'light' : 'dark')">
              <template v-if="layoutSetting.theme === 'light'">
                <carbon-moon />
              </template>
              <template v-else>
                <carbon-sun />
              </template>
            </span>
            <SelectLang />
          </div>
        </div>
        <a-divider m-0 />
        
        <div class="box-border flex min-h-[520px]">
          <div class="ant-pro-form-login-main-left min-h-[520px] flex-center bg-[var(--bg-color-container)]">
            <img src="@/assets/images/login-left.png" class="h-5/6 w-5/6">
          </div>
          <a-divider m-0 type="vertical" class="ant-pro-login-divider min-h-[520px]" />
          
          <div class="ant-pro-form-login-main-right px-5 w-[335px] flex-center flex-col relative z-11">
            <div class="text-center py-6 text-2xl font-bold text-[var(--text-color)]">
              {{t('pages.login.tips') }}
            </div>

            <a-form ref="formRef" :model="loginModel" class="w-full">
              
              <a-tabs v-if="!isRegisterMode" v-model:active-key="loginModel.type" centered>
                <a-tab-pane key="account" :tab="t('pages.login.accountLogin.tab')" />
              </a-tabs>

              <a-alert v-if="errorAlert" mb-24px
                :message="t('pages.login.accountLogin.errorMessage')" type="error" show-icon />

              <a-form-item name="username" :rules="[{ required: true, message: t('pages.login.username.required') }]">
                <a-input v-model:value="loginModel.username" allow-clear autocomplete="off"
                  :placeholder="t('pages.login.username.placeholder')" size="large" 
                  @press-enter="submit()">
                  <template #prefix>
                    <UserOutlined />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item name="password" :rules="[{ required: true, message: t('pages.login.password.required') }]">
                <a-input-password v-model:value="loginModel.password" allow-clear
                  :placeholder="t('pages.login.password.placeholder')" size="large" 
                  @press-enter="submit()">
                  <template #prefix>
                    <LockOutlined />
                  </template>
                </a-input-password>
              </a-form-item>
              <div v-if="!isRegisterMode" class="mb-24px flex-between">
                <a-checkbox v-model:checked="loginModel.remember">
                  {{ t('pages.login.rememberMe') }}
                </a-checkbox>
              </div>
              <div class="action-btn">
                <a-button type="primary" block :loading="submitLoading" size="large" @click="submit">
                  {{ t('pages.login.submit') }}
                </a-button>
              </div>

            </a-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
/* 保持原有样式不变，仅做了微调 */
.login-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: auto;
  background: var(--bg-color-container);
}

.login-lang {
  height: 40px;
  line-height: 44px;
}

.login-content {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.ant-pro-form-login-header a {
  text-decoration: none;
}

.ant-pro-form-login-title {
  color: var(--text-color);
  font-weight: 600;
  font-size: 33px;
  line-height: 1;
}

.ant-pro-form-login-logo {
  width: 44px;
  height: 44px;
  margin-right: 16px;
  vertical-align: top;
}

.ant-pro-form-login-main-right {
  .ant-tabs-nav-list {
    margin: 0 auto;
    font-size: 16px;
  }
}

.ant-pro-form-login-main {
  box-shadow: var(--c-shadow);
}

/* 媒体查询样式 */
.login-media(@width: 100%) {
  .ant-pro-form-login-main {
    width: @width;
  }

  .ant-pro-form-login-main-left {
    display: none;
  }

  .ant-pro-form-login-main-right {
    width: 100%;
  }
}

@media (min-width: 992px) {
  .ant-pro-form-login-main-left {
    width: 700px;
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .ant-pro-login-divider {
    display: none;
  }
  .login-media(400px);
}

@media screen and (max-width: 767px) {
  .login-media(350px);
  .ant-pro-login-divider {
    display: none;
  }
}
</style>
