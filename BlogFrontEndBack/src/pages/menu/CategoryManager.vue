<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, theme } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getCategoryList, createCategory, deleteCategory } from '~/api/Category'
import { uploadFile } from '@/api/upload'

const { useToken } = theme
const { token } = useToken()

const router = useRouter()
const categories = ref<any[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)

const formState = ref({
  name: '',
  description: '',
  sort: 0,
  thumbnail: ''
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id' },
  { title: '封面', dataIndex: 'thumbnail', key: 'thumbnail' },
  { title: '分类名称', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '排序', dataIndex: 'sort', key: 'sort' },
  { title: '操作', key: 'action' },
]

const fetchCategories = async () => {
  loading.value = true
  try {
    const res: any = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    message.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const showModal = () => {
  formState.value = { name: '', description: '', sort: 0, thumbnail: '' }
  modalVisible.value = true
}

const customUpload = async ({ file, onSuccess }: any) => {
  try {
    const res = await uploadFile(file) as any
    if (res.code === 200) {
      formState.value.thumbnail = res.data
      onSuccess()
    } else {
      message.error(res.msg || '上传失败')
    }
  } catch (error) {
    message.error('上传出错')
  }
}

const handleCreate = async () => {
  if (!formState.value.name) {
    return message.warning('分类名称不能为空')
  }
  confirmLoading.value = true
  try {
    const res: any = await createCategory(formState.value)
    if (res.code === 200) {
      message.success('创建成功')
      modalVisible.value = false
      fetchCategories()
    } else {
      message.error(res.msg || '创建失败')
    }
  } catch (error) {
    message.error('创建失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleDelete = async (id: string | number) => {
  try {
    const res: any = await deleteCategory(id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchCategories()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

const viewArticles = (categoryName: string) => {
  router.push({ name: 'blogmanager', query: { categoryName } })
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="category-manager-container" style="padding: 24px">
    <a-card title="分类管理" :bordered="false">
      <template #extra>
        <a-space>
          <a-button @click="router.push({ name: 'recyclebin', query: { tab: 'categories' } })">回收站</a-button>
          <a-button type="primary" @click="showModal">添加分类</a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :row-key="(record) => record.id"
        :data-source="categories"
        :loading="loading"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'thumbnail'">
            <a-image
              v-if="record.thumbnail"
              :width="50"
              :src="record.thumbnail"
              fallback="https://aliyun-oss-polo.oss-cn-beijing.aliyuncs.com/2024/09/25/479b1248074d4400a944ae55979bb334.jpg"
            />
            <span v-else>-</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="viewArticles(record.name)">查看文章</a-button>
            <a-popconfirm
              title="确定要删除此分类吗？"
              @confirm="handleDelete(record.id)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      title="添加新分类"
      :confirm-loading="confirmLoading"
      @ok="handleCreate"
    >
      <a-form layout="vertical">
        <a-form-item label="封面图">
          <a-upload
            list-type="picture-card"
            class="category-uploader"
            :show-upload-list="false"
            :custom-request="customUpload"
          >
            <img v-if="formState.thumbnail" :src="formState.thumbnail" alt="thumbnail" style="width: 100%; height: 100%; object-fit: cover;" />
            <div v-else>
              <div style="margin-top: 8px">点击上传</div>
            </div>
          </a-upload>
        </a-form-item>
        <a-form-item label="分类名称" required>
          <a-input v-model:value="formState.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="分类描述">
          <a-textarea v-model:value="formState.description" placeholder="请输入分类描述" :rows="4" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="formState.sort" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.category-manager-container {
  min-height: 100%;
}

.category-uploader :deep(.ant-upload) {
  width: 100px;
  height: 100px;
  background-color: v-bind('token.colorFillQuaternary');
  border: 1px dashed v-bind('token.colorBorder');
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;

  &:hover {
    border-color: v-bind('token.colorPrimary');
  }
}
</style>
