<template>
  <div class="user-edit-container">
    <a-skeleton active v-if="loading" />

    <a-card v-else :bordered="false" title="编辑用户资料">
      <a-alert
        type="info"
        show-icon
        class="info-banner"
        message="这里用于维护用户基础资料，密码修改仍建议在账号安全页单独处理。"
      />

      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :lg="16">
          <a-form
            ref="formRef"
            :model="formState"
            :rules="rules"
            layout="vertical"
            class="edit-form"
          >
            <a-form-item label="用户名" name="username">
              <a-input v-model:value="formState.username" placeholder="请输入用户名" />
            </a-form-item>

            <a-form-item label="昵称" name="nickname">
              <a-input v-model:value="formState.nickname" placeholder="请输入昵称" />
            </a-form-item>

            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formState.email" placeholder="请输入邮箱地址" />
            </a-form-item>

            <a-form-item label="个人简介" name="intro">
              <a-textarea
                v-model:value="formState.intro"
                :rows="6"
                show-count
                :maxlength="500"
                placeholder="写一点关于这个用户的简介信息"
              />
            </a-form-item>

            <a-form-item>
              <a-space wrap>
                <a-button @click="goBack">返回详情</a-button>
                <a-button type="primary" :loading="saving" @click="handleSubmit">保存修改</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-col>

        <a-col :xs="24" :lg="8">
          <div class="side-panel">
            <div class="avatar-panel">
              <a-avatar :size="112" shape="square" class="avatar-preview">
                <template #icon>
                  <img v-if="formState.avatar" :src="formState.avatar" alt="avatar" />
                  <UserOutlined v-else />
                </template>
              </a-avatar>

              <a-upload
                :show-upload-list="false"
                :custom-request="customUploadRequest"
                accept="image/*"
              >
                <a-button class="upload-btn">
                  <UploadOutlined />
                  更换头像
                </a-button>
              </a-upload>
            </div>

            <a-descriptions :column="1" size="small" bordered class="meta-panel">
              <a-descriptions-item label="用户 ID">{{ formState.id }}</a-descriptions-item>
              <a-descriptions-item label="角色">{{ formState.roleName || '未分配' }}</a-descriptions-item>
              <a-descriptions-item label="来源">{{ sourceLabel }}</a-descriptions-item>
              <a-descriptions-item label="当前状态">{{ formState.statusText || '未知状态' }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, notification } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { UploadOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getUserDetail, updateUserInfo, type UpdateUserPayload, type UserInfo } from '@/api/common/user'
import { uploadFile } from '@/api/upload'

type UserEditForm = UpdateUserPayload & {
  roleName: string
  sourceText: string
  statusText: string
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const formState = reactive<UserEditForm>({
  id: '',
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  intro: '',
  source: '',
  status: '',
  roleName: '',
  sourceText: '',
  statusText: '',
})

const rules: Record<string, Rule[]> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
}

const sourceLabel = computed(() => {
  const source = formState.source?.toLowerCase()
  if (!source) return '未记录'
  if (source === 'gitee') return 'Gitee'
  if (source === 'github') return 'GitHub'
  if (source === 'local') return '本地账号'
  return formState.source
})

onMounted(() => {
  const id = route.params.id as string
  if (id) {
    loadDetail(id)
  }
})

const loadDetail = async (id: string) => {
  loading.value = true
  try {
    const res: any = await getUserDetail(id)
    if (res.code === 200 && res.data) {
      applyDetail(res.data as UserInfo)
    } else {
      message.error(res.msg || '获取用户详情失败')
    }
  } catch (error) {
    message.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

const applyDetail = (user: UserInfo) => {
  formState.id = user.id
  formState.username = user.username || ''
  formState.nickname = user.nickname || ''
  formState.email = user.email || ''
  formState.avatar = user.avatar || ''
  formState.intro = user.intro || ''
  formState.source = user.source || ''
  formState.status = user.status || ''
  formState.roleName = user.roleName || ''
  formState.sourceText = user.source || ''
  formState.statusText = user.statusText || ''
}

const customUploadRequest = async (options: any) => {
  const { file, onSuccess, onError } = options
  try {
    const res: any = await uploadFile(file)
    if (res.code === 200) {
      formState.avatar = res.data
      onSuccess?.(res, file)
      notification.success({
        message: '头像上传成功',
        description: '头像已更新，保存资料后即可生效。',
        placement: 'topRight',
      })
    } else {
      message.error(res.msg || '头像上传失败')
      onError?.(new Error('upload failed'))
    }
  } catch (error) {
    message.error('头像上传失败')
    onError?.(error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    const res: any = await updateUserInfo({
      id: formState.id,
      username: formState.username.trim(),
      nickname: formState.nickname.trim(),
      email: formState.email.trim(),
      avatar: formState.avatar.trim(),
      intro: formState.intro.trim(),
    })
    if (res.code === 200) {
      message.success('用户资料已更新')
      router.push({ name: 'UserDetail', params: { id: formState.id } })
    } else {
      message.error(res.msg || '更新失败')
    }
  } catch (error) {
    message.error('更新失败')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push({ name: 'UserDetail', params: { id: formState.id || route.params.id } })
}
</script>

<style scoped lang="scss">
.user-edit-container {
  padding: 24px;
}

.info-banner {
  margin-bottom: 24px;
}

.edit-form {
  max-width: 100%;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.02);
}

.avatar-preview {
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.16);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.upload-btn {
  width: 100%;
}

.meta-panel {
  background: rgba(255, 255, 255, 0.02);
}

@media screen and (max-width: 768px) {
  .user-edit-container {
    padding: 16px;
  }
}
</style>
