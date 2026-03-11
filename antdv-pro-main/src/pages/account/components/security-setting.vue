<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { message, notification } from 'ant-design-vue' // 引入 notification
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { updatePassWord } from '~@/api/common/user'
import { useRouter } from 'vue-router'
// 如果你有 userStore 用于退出登录，请引入它
// import { useUserStore } from '@/store/modules/user'

interface DataItem {
  title: string
  desc: string
}

const { t } = useI18n()
const router = useRouter()
// const userStore = useUserStore()

// 列表数据
const data = computed<DataItem[]>(() => {
  return [
    {
      title: t('account.settings.security.account-password'),
      desc: t('account.settings.security.account-password-desc'),
    },
  ]
})

// --- 修改密码弹窗逻辑 ---

const modalVisible = ref(false)
const confirmLoading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据模型
const formState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 校验“确认密码”是否与“新密码”一致
const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (value === '') {
    return Promise.reject('请再次输入密码')
  } else if (value !== formState.newPassword) {
    return Promise.reject('两次输入的密码不一致!')
  } else {
    return Promise.resolve()
  }
}

// 表单校验规则
const rules: Record<string, Rule[]> = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 打开弹窗
const handleModify = () => {
  modalVisible.value = true
}

// 提交表单 (核心修改逻辑)
const handleOk = async () => {
  try {
    await formRef.value?.validate()
    confirmLoading.value = true
    
    const res = await updatePassWord({
      passWord: formState.newPassword, 
      oldPassWord: formState.oldPassword
    }) as any

    if (res.code === 200) {
      // 1. 修改成功，停止加载，关闭弹窗
      confirmLoading.value = false
      modalVisible.value = false
      
      // 2. 定义倒计时逻辑
      let seconds = 3
      const key = 'passwordUpdNotification' //以此 key 来更新同一个通知框
      
      // 3. 立即弹出初始提示
      notification.success({
        message: '密码修改成功',
        description: `密码已更新，将在 ${seconds} 秒后自动退出登录...`,
        key,
        duration: null, // 设置为 null 不会自动关闭，由代码控制
        placement: 'topRight'
      })

      // 4. 开始倒计时
      const timer = setInterval(() => {
        seconds--
        
        // 更新通知框文案
        notification.success({
          message: '密码修改成功',
          description: `密码已更新，将在 ${seconds} 秒后自动退出登录...`,
          key,
          duration: null,
          placement: 'topRight'
        })

        // 倒计时结束
        if (seconds <= 0) {
          clearInterval(timer)
          notification.close(key) // 关闭通知框
          
          // 5. 执行退出逻辑
          // 如果你有 store，建议用 store.logout()
          // userStore.logout() 
          
          // 或者手动清除 token
          localStorage.removeItem('Authorization') // 假设你的 token 存在这里
          
          // 6. 跳转登录页
          router.replace('/login')
        }
      }, 1000)

    } else {
      // 业务错误（如原密码错误）
      message.error(res.msg || '修改失败')
      confirmLoading.value = false
    }
  } catch (error) {
    console.log('验证失败或请求错误', error)
    confirmLoading.value = false
  }
}

// 取消/关闭弹窗
const handleCancel = () => {
  formRef.value?.resetFields()
  modalVisible.value = false
}
</script>

<template>
  <a-card :title="t('account.settings.security-setting')" :bordered="false">
    <a-list item-layout="horizontal" :data-source="data">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta :description="item.desc">
            <template #title>
              <span>{{ item.title }}</span>
            </template>
          </a-list-item-meta>
          <template #actions>
            <a-button type="link" @click="handleModify">
              {{ t('account.settings.modify') }}
            </a-button>
          </template>
        </a-list-item>
      </template>
    </a-list>

    <a-modal
      v-model:open="modalVisible"
      title="修改密码"
      :confirm-loading="confirmLoading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        name="password_modal"
        class="mt-4"
      >
        <a-form-item label="原密码" name="oldPassword">
          <a-input-password 
            v-model:value="formState.oldPassword" 
            placeholder="请输入当前使用的密码" 
            allow-clear 
          />
        </a-form-item>

        <a-form-item label="新密码" name="newPassword">
          <a-input-password 
            v-model:value="formState.newPassword" 
            placeholder="请输入新密码" 
            allow-clear 
          />
        </a-form-item>

        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password 
            v-model:value="formState.confirmPassword" 
            placeholder="请再次输入新密码" 
            allow-clear 
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<style scoped lang="less">
:deep(.ant-card-body) {
  padding-left: 0 !important;
}

// 给弹窗内的表单增加一点间距
.mt-4 {
  margin-top: 16px;
}
</style>
