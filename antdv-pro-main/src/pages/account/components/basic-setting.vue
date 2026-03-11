<script setup lang="ts">
import { ref, reactive, toRaw, onMounted, nextTick } from 'vue'
import { UploadOutlined, UserOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message, notification } from 'ant-design-vue'
import { uploadFile } from '@/api/upload'
import { getAuthInfo, updateAuthInfo, AuthInfoDTO } from '@/api/authInfo'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const formRef = ref()
const loading = ref(false)

// === 动态标签状态 ===
const tags = ref<string[]>([])
const inputVisible = ref(false)
const inputValue = ref('')
const inputRef = ref()

const handleClose = (removedTag: string) => {
  tags.value = tags.value.filter(tag => tag !== removedTag)
  formState.slogan = tags.value.join(',') // 同步到 slogan
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value && !tags.value.includes(inputValue.value)) {
    tags.value = [...tags.value, inputValue.value]
    formState.slogan = tags.value.join(',') // 同步到 slogan
  }
  inputVisible.value = false
  inputValue.value = ''
}

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
    const res: any = await getAuthInfo()
    if (res?.data) {
      formState.userName = res.data.userName || ''
      formState.avatar = res.data.avatar || ''
      formState.profile = res.data.profile || ''
      formState.slogan = res.data.slogan || ''
      // 初始化 tags
      if (formState.slogan) {
        try {
          const parsed = JSON.parse(formState.slogan)
          if (Array.isArray(parsed)) {
            tags.value = parsed.map(String)
          } else {
            tags.value = String(formState.slogan).split(/[,，]/).filter(t => t.trim())
          }
        } catch(e) {
          // 如果原来的 slogan 不是 JSON 数组，则根据中英文逗号拆分为多个 tag
          tags.value = String(formState.slogan).split(/[,，]/).filter(t => t.trim())
        }
      } else {
        tags.value = []
      }
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
            
            <a-form-item label="标签" name="slogan">
              <div class="tags-container">
                <template v-for="tag in tags" :key="tag">
                  <a-tag closable @close="handleClose(tag)" color="blue" style="font-size: 14px; padding: 4px 10px; margin-bottom: 8px;">
                    {{ tag }}
                  </a-tag>
                </template>
                <a-input
                  v-if="inputVisible"
                  ref="inputRef"
                  v-model:value="inputValue"
                  type="text"
                  size="small"
                  :style="{ width: '100px', verticalAlign: 'top', marginBottom: '8px' }"
                  @blur="handleInputConfirm"
                  @keyup.enter="handleInputConfirm"
                />
                <a-tag v-else style="border-style: dashed; font-size: 14px; padding: 4px 10px; cursor: pointer; margin-bottom: 8px; background: transparent;" @click="showInput">
                  <plus-outlined />
                  添加标签
                </a-tag>
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
  color: #1f2937;
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

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
