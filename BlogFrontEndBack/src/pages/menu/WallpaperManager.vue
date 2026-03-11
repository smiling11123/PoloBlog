<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getWallpaperList, deleteWallpaper, uploadWallpaper } from '~/api/wallpaper'

const data = ref<any[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id' },
  { title: '壁纸名称', dataIndex: 'name', key: 'name' },
  { title: '图片预览', dataIndex: 'address', key: 'address' },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' },
]

const fetchWallpapers = async (page = 1, size = 10) => {
  loading.value = true
  try {
    const res: any = await getWallpaperList(page, size)
    data.value = res?.data?.records || []
    pagination.value.current = res?.data?.current || 1
    pagination.value.total = res?.data?.total || 0
  } catch (error) {
    message.error('获取壁纸列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  fetchWallpapers(pag.current, pag.pageSize)
}

const handleDelete = async (id: number) => {
  try {
    await deleteWallpaper(id)
    message.success('删除成功')
    fetchWallpapers(pagination.value.current, pagination.value.pageSize)
  } catch (error) {
    message.error('删除壁纸失败')
  }
}

const openUploadModal = ref(false)
const uploadName = ref('')
const uploadFile = ref<File | null>(null)

const beforeUpload = (file: File) => {
  uploadFile.value = file
  return false // Prevent auto upload
}

const handleUploadSubmit = async () => {
  if (!uploadName.value || !uploadFile.value) {
    message.warning('请提供壁纸名称并选择图片文件')
    return
  }
  try {
    await uploadWallpaper(uploadName.value, uploadFile.value)
    message.success('上传壁纸成功')
    openUploadModal.value = false
    uploadName.value = ''
    uploadFile.value = null
    fetchWallpapers()
  } catch (error) {
    message.error('上传壁纸失败')
  }
}

onMounted(() => {
  fetchWallpapers()
})
</script>

<template>
  <div class="wallpaper-manager-container" style="padding: 24px">
    <a-card title="壁纸管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="openUploadModal = true">上传壁纸</a-button>
      </template>

      <a-table
        :columns="columns"
        :row-key="(record) => record.id"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'address'">
            <a-image v-if="record.address" :width="100" :src="record.address" />
            <span v-else>无预览</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除此壁纸吗？"
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
      v-model:open="openUploadModal"
      title="上传壁纸"
      @ok="handleUploadSubmit"
      ok-text="确认上传"
      cancel-text="取消"
    >
      <a-form layout="vertical">
        <a-form-item label="壁纸名称" required>
          <a-input v-model:value="uploadName" placeholder="请输入壁纸名称" />
        </a-form-item>
        <a-form-item label="选择文件" required>
          <a-upload
            :before-upload="beforeUpload"
            :max-count="1"
            accept="image/*"
          >
            <a-button>点击选择图片</a-button>
          </a-upload>
          <div v-if="uploadFile" style="margin-top: 8px;">
            已选择文件： {{ uploadFile.name }}
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
