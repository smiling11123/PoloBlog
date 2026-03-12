<script setup lang="ts">
import { ref, reactive, toRaw, onMounted } from 'vue'
import { UploadOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message, notification } from 'ant-design-vue'
import { uploadFile } from '@/api/upload'
import { getAuthInfo, updateAuthInfo, AuthInfoDTO } from '@/api/authInfo'
import { getSloganList } from '~/api/slogan'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const formRef = ref()
const loading = ref(false)
const sloganList = ref<string[]>([])

const formState: AuthInfoDTO = reactive({
  userName: '',
  avatar: '',
  profile: '',
  slogan: ''
})

const rules: any = {
  userName: [{ required: true, message: '请输入名称', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const [authRes, sloganRes]: any = await Promise.all([getAuthInfo(), getSloganList()])
    if (authRes?.data) {
      formState.userName = authRes.data.userName || ''
      formState.avatar = authRes.data.avatar || ''
      formState.profile = authRes.data.profile || ''
      formState.slogan = authRes.data.slogan || ''
    }
    if (sloganRes?.code === 200) {
      sloganList.value = (sloganRes.data || [])
        .map((item: { content?: string }) => typeof item.content === 'string' ? item.content.trim() : '')
        .filter((item: string) => item.length > 0)
    }
  } catch (error) {
    message.error('获取作者信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

const customUploadRequest = async (options: any) => {
  const { file, onSuccess, onError } = options
  
  try {
    const res = await uploadFile(file) as any
    if (res.code === 200) {
      const imgUrl = res.data 
      formState.avatar = imgUrl
      onSuccess(res, file)
      notification.info({
        message: '头像已上传',
        description: '图片上传成功，请点击下方的“更新基本信息”按钮以保存修改。',
        duration: 4,
        placement: 'topRight',
      })
    } else {
      message.error(res.msg || '上传失败')
      onError(new Error('Upload failed'))
    }
  } catch (error) {
    console.error(error)
    message.error('网络错误，上传失败')
    onError(error)
  }
}

async function onSubmit() {
  try {
    await formRef.value.validate()
    const params = toRaw(formState)
    await updateAuthInfo(params)
    message.success('修改作者信息成功')
  } catch (error) {
    console.log('error', error)
  }
}
</script>

<template>
  <a-card title="作者基本设置" :bordered="false" class="author-setting-card">
    <a-spin :spinning="loading">
      <a-row>
        <a-col :span="14">
          <a-form
            ref="formRef"
            :model="formState"
            :rules="rules"
            layout="vertical"
            class="setting-form"
          >
            <h3 class="section-title">基本信息</h3>
            <!-- 对应 Backend: AuthInfo 的字段 (userName, avatar, profile, slogan) -->
            <a-form-item label="名称" name="userName">
              <a-input size="large" v-model:value="formState.userName" placeholder="请输入名称" />
            </a-form-item>
            
            <a-form-item label="标语">
              <div class="slogan-display">
                <template v-if="sloganList.length">
                  <a-tag
                    v-for="item in sloganList"
                    :key="item"
                    color="blue"
                    class="slogan-tag"
                  >
                    {{ item }}
                  </a-tag>
                </template>
                <span v-else class="slogan-empty">暂无标语，请前往“标语管理”维护。</span>
              </div>
            </a-form-item>
            
            <a-form-item name="profile" label="个人简介">
              <MdEditor 
                v-model="formState.profile" 
                placeholder="请简单介绍一下自己..."
                :toolbars="['bold', 'underline', 'italic', '-', 'strikeThrough', 'sub', 'sup', 'quote', 'unorderedList', 'orderedList', 'codeRow', 'code', 'link', 'image', 'table', 'revoke', 'next', '=', 'pageFullscreen', 'fullscreen', 'preview', 'htmlPreview', 'catalog']"
                :style="{ height: '400px' }" 
              />
            </a-form-item>
            
            <a-form-item style="margin-top: 32px;">
              <a-button type="primary" size="large" @click="onSubmit" class="submit-btn" :loading="loading">
                保存基本信息
              </a-button>
            </a-form-item>
          </a-form>
        </a-col>
        
        <a-col :span="8" :offset="2">
          <div class="avatar-section">
            <h3 class="section-title">头像设置</h3>
            <div class="avatar-wrapper">
              <a-avatar :size="120" class="avatar-preview">
                <template #icon>
                  <img v-if="formState.avatar" :src="formState.avatar" alt="avatar" />
                  <UserOutlined v-else />
                </template>
              </a-avatar>
              
              <a-upload
                name="file"
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
            <p class="avatar-hint">支持 jpg、png、jpeg 格式大小 5M 以内的图片</p>
          </div>
        </a-col>
      </a-row>
    </a-spin>
  </a-card>
</template>

<style scoped lang="less">
.author-setting-card {
  min-height: 600px;
}

.setting-form {
  padding-right: 0px;
}

.section-title {
  margin-bottom: 24px;
  font-size: 16px;
  font-weight: 500;
  color: #f9f9f9;
}

.avatar-section {
  display: flex;
  flex-direction: column;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 16px;
  
  .avatar-preview {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    margin-bottom: 24px;
    border: 2px solid #fff;
    
    img {
      object-fit: cover;
      width: 100%;
      height: 100%;
    }
  }
}

.upload-btn {
  border-radius: 6px;
  transition: all 0.3s;
  
  &:hover {
    color: #1890ff;
    border-color: #1890ff;
  }
}

.avatar-hint {
  text-align: center;
  margin-top: 16px;
  color: #8c8c8c;
  font-size: 12px;
}

.submit-btn {
  width: 140px;
  border-radius: 6px;
}

.slogan-display {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: center;
}

.slogan-tag {
  font-size: 14px;
  padding: 4px 10px;
}

.slogan-empty {
  color: #8c8c8c;
  font-size: 13px;
}
</style>
