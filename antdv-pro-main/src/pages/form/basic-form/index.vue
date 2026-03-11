<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue' // 引入 computed
import { useRoute, useRouter } from 'vue-router'
import { message, theme } from 'ant-design-vue' // 引入 theme
import 'leaflet/dist/leaflet.css'

// 引入 Markdown 编辑器
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

import { publishArticle, updateArticle } from '@/api/Article/ArticleEdit'
import { uploadFile } from '@/api/upload'
import { getArticleDetail } from '~@/api/Article/ArticleDetail'
import { getCategoryList } from '~@/api/Category'
import { useAppStore } from "@/stores/app" // 引入 Store

const route = useRoute()
const router = useRouter()
const appStore = useAppStore() // 获取 store 实例

const { useToken } = theme
const { token } = useToken()

// 计算编辑器的主题 (light 或 dark)
const editorTheme = computed(() => appStore.layoutSetting.theme === 'dark' ? 'dark' : 'light')



const formState = reactive({
  id: undefined as number | undefined,
  title: '',
  summary: '',
  content: '',
  thumbnail: '',
  categoryId: undefined as number | undefined,
  categoryName: '',
  isTop: false,
  isComment: true,
  isManuscript: false,
  status: 1,
})
const loading = ref(false)

const categories = ref<any[]>([])
const fetchCategories = async () => {
  try {
    const res: any = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

onMounted(async () => {
  fetchCategories()
  const id = route.params.id
  if (id) {
    await loadData(id)
  }
})

const loadData = async (id: any) => {
  loading.value = true
  try {
    const res = await getArticleDetail(id) as any
    if (res.code === 200) {
      const data = res.data
      formState.id = data.id
      formState.title = data.title
      formState.summary = data.summary
      formState.content = data.content
      formState.thumbnail = data.thumbnail
      formState.isTop = data.isTop == 1
      formState.isComment = data.isComment == 1
      formState.categoryName = data.categoryName
      formState.status = data.status
      formState.isManuscript = data.status == 0
    }
  } finally {
    loading.value = false
  }
}

const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  const res = await Promise.all(files.map((file) => uploadFile(file)))
  const urls = res.map((r: any) => r.data)
  callback(urls)
}

const customUpload = async ({ file, onSuccess }: any) => {
  const res = await uploadFile(file) as any
  if (res.code === 200) {
    formState.thumbnail = res.data
    onSuccess()
  }
}

const handleSubmit = async (isManuscript: Boolean = false) => {
  if (!formState.title || !formState.content) {
    return message.warning('标题和内容不能为空')
  }
  loading.value = true
  try {
    const params = {
      ...formState,
      isTop: formState.isTop ? 1 : 0,
      isComment: formState.isComment ? 1 : 0,
      // 这里的逻辑合并优化一下，更清晰
      status: isManuscript ? 0 : 1
    }
    
    // 同步更新本地状态，防止UI不跳变
    formState.status = params.status
    formState.isManuscript = isManuscript ? true : false

    if (formState.id) {
      await updateArticle(params)
      message.success(isManuscript ? '已存为草稿' : '修改成功')
    } else {
      await publishArticle(params)
      message.success(isManuscript ? '已存为草稿' : '发布成功')
    }
    router.push({ name: 'ArticleList' })
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <div class="publish-container">
    <a-card :title="formState.id ? '编辑文章' : '发布新文章'" :bordered="false">
      <a-form layout="vertical" :model="formState">

        <a-row :gutter="24">
          <a-col :span="16">
            <a-form-item label="文章标题" required>
              <a-input v-model:value="formState.title" placeholder="请输入吸引人的标题" size="large" />
            </a-form-item>

            <a-form-item label="文章内容" required>
              <div class="editor-wrapper">
                <MdEditor 
                  v-model="formState.content" 
                  :toolbarsExclude="['github']" 
                  :theme="editorTheme" 
                  placeholder="开始编写你的Blog..."
                  @onUploadImg="onUploadImg" 
                  style="height: 800px;" 
                />
              </div>
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-card title="基础信息" class="mb-4" size="small">
              <a-form-item label="分类">
                <a-select
                  v-model:value="formState.categoryName"
                  placeholder="请选择分类"
                  show-search
                  :options="categories.map(c => ({ value: c.name, label: c.name }))"
                  :filter-option="(inputValue: string, option: any) => option.value.toUpperCase().indexOf(inputValue.toUpperCase()) !== -1"
                >
                </a-select>
              </a-form-item>

              <a-form-item label="文章摘要">
                <a-textarea v-model:value="formState.summary" :rows="3" placeholder="简短介绍..." show-count :maxlength="200" />
              </a-form-item>

              <a-form-item label="封面图">
                <a-upload list-type="picture-card" class="cover-uploader" :show-upload-list="false" :custom-request="customUpload">
                  <img v-if="formState.thumbnail" :src="formState.thumbnail" alt="avatar" />
                  <div v-else>
                    <span class="upload-text" :style="{ color: token.colorTextSecondary }">点击上传</span>
                  </div>
                </a-upload>
              </a-form-item>
            </a-card>

            <a-card title="发布设置" size="small">
              <div class="setting-item">
                <span>允许评论</span>
                <a-switch v-model:checked="formState.isComment" />
              </div>
              <a-divider style="margin: 12px 0" />
              <div class="setting-item">
                <span>是否置顶</span>
                <a-switch v-model:checked="formState.isTop" />
              </div>

              <div class="mt-6">
                <a-button type="primary" block size="large" :loading="loading" @click="handleSubmit(false)">
                  {{ formState.id ? (formState.status == 1 ? '确认修改' : '确认修改并发布') : '立即发布' }}
                </a-button>
              </div>
              <div class="mt-6">
                <a-button block size="large" :loading="loading" @click="handleSubmit(true)">
                  {{ formState.isManuscript ? "保存草稿" : "存为草稿" }}
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
  /* 编辑器外层边框颜色 */
  
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
    
    /* 默认背景色跟随容器背景 */
    background-color: v-bind('token.colorFillQuaternary'); 
    /* 默认边框 */
    border: 1px dashed v-bind('token.colorBorder'); 

    &:hover {
      /* 悬停时边框变为主色调 */
      border-color: v-bind('token.colorPrimary'); 
      /* 悬停时背景稍微变深 */
      background-color: v-bind('token.colorFillSecondary');
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* 文字颜色跟随主题 */
  color: v-bind('token.colorText');
}

.mb-4 {
  margin-bottom: 16px;
}

.mt-6 {
  margin-top: 24px;
}
</style>
