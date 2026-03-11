<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message, theme } from 'ant-design-vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { publishWorks } from '~/api/works'
import { uploadFile } from '~/api/upload' // Ensure this exists, reuse article upload logic
import { useAppStore } from "@/stores/app"

const router = useRouter()
const appStore = useAppStore()
const { useToken } = theme
const { token } = useToken()

const editorTheme = computed(() => appStore.layoutSetting.theme === 'dark' ? 'dark' : 'light')

const formState = reactive({
  title: '',
  content: '',
  thumbnail: '',
  address: ''
})

const loading = ref(false)

const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  try {
    const res = await Promise.all(files.map((file) => uploadFile(file)))
    const urls = res.map((r: any) => r.data)
    callback(urls)
  } catch(error) {
    message.error('图片上传失败')
  }
}

const customUpload = async ({ file, onSuccess }: any) => {
  try {
    const res = await uploadFile(file) as any
    if (res.code === 200 || res.data) {
      // Sometimes res.data is the direct URL depending on your backend
      formState.thumbnail = res.data
      onSuccess()
    } else {
      message.error('封面上传失败')
    }
  } catch(error) {
    message.error('封面上传失败')
  }
}

const handleSubmit = async () => {
  if (!formState.title || !formState.content || !formState.address) {
    return message.warning('标题、内容和地址不能为空')
  }
  loading.value = true
  try {
    await publishWorks(formState)
    message.success('发布成功')
    router.push({ name: 'worksmanager' })
  } catch (error) {
    message.error('发布失败')
  } finally {
    loading.value = false
  }
}

// Return back
const handleBack = () => {
  router.push({ name: 'worksmanager' })
}
</script>

<template>
  <div class="publish-container">
    <div class="header-actions">
        <a-button @click="handleBack" style="margin-bottom: 16px;">返回管理页</a-button>
    </div>
    <a-card title="发布新作品" :bordered="false">
      <a-form layout="vertical" :model="formState">
        <a-row :gutter="24">
          <a-col :span="16">
            <a-form-item label="作品标题" required>
              <a-input v-model:value="formState.title" placeholder="请输入作品标题" size="large" />
            </a-form-item>

            <a-form-item label="作品地址" required>
              <a-input v-model:value="formState.address" placeholder="请输入作品地址 (例如: http://...)" size="large" />
            </a-form-item>

            <a-form-item label="作品内容" required>
              <div class="editor-wrapper">
                <MdEditor 
                  v-model="formState.content" 
                  :toolbarsExclude="['github']" 
                  :theme="editorTheme" 
                  placeholder="开始编写作品详情..."
                  @onUploadImg="onUploadImg" 
                  style="height: 600px;" 
                />
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-card title="作品封面" size="small" class="mb-4">
              <a-form-item>
                <a-upload 
                  list-type="picture-card" 
                  class="cover-uploader" 
                  :show-upload-list="false" 
                  :custom-request="customUpload">
                  <img v-if="formState.thumbnail" :src="formState.thumbnail" alt="thumbnail" />
                  <div v-else>
                    <span class="upload-text" :style="{ color: token.colorTextSecondary }">点击上传</span>
                  </div>
                </a-upload>
              </a-form-item>
            </a-card>

            <a-card title="发布设置" size="small">
              <div class="mt-6">
                <a-button type="primary" block size="large" :loading="loading" @click="handleSubmit">
                  立即发布
                </a-button>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped lang="scss">
.publish-container {
  padding: 24px;
  min-height: 100vh;
}

.editor-wrapper {
  border-radius: 4px;
  overflow: hidden;
}

.cover-uploader {
  :deep(.ant-upload) {
    width: 100%;
    height: 150px;
    border-radius: 8px; 
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    
    background-color: v-bind('token.colorFillQuaternary'); 
    border: 1px dashed v-bind('token.colorBorder'); 

    &:hover {
      border-color: v-bind('token.colorPrimary'); 
      background-color: v-bind('token.colorFillSecondary');
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-6 {
  margin-top: 24px;
}
</style>
