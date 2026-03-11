<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getWorksList, deleteWorks, WorksDTO } from '~/api/works'

const router = useRouter()
const data = ref<WorksDTO[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id' },
  { title: '作品名称', dataIndex: 'title', key: 'title' },
  { title: '封面预览', dataIndex: 'thumbnail', key: 'thumbnail' },
  { title: '作品地址', dataIndex: 'address', key: 'address' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' },
]

const fetchWorks = async (page = 1, size = 10) => {
  loading.value = true
  try {
    const res: any = await getWorksList(page, size)
    // 确保列表中的 ID 都是字符串
    data.value = (res?.data?.records || []).map((item: any) => ({
      ...item,
      id: String(item.id)
    }))
    pagination.value.current = res?.data?.current || 1
    pagination.value.total = res?.data?.total || 0
  } catch (error) {
    message.error('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  fetchWorks(pag.current, pag.pageSize)
}

const handleDelete = async (id: string | number) => {
  try {
    const stringId = String(id)
    await deleteWorks(stringId)
    message.success('删除成功')
    fetchWorks(pagination.value.current, pagination.value.pageSize)
  } catch (error) {
    message.error('删除作品失败')
  }
}

const handleEdit = (id: string | number) => {
  const stringId = String(id)
  router.push({ name: 'worksedit', params: { id: stringId } })
}

const goPublish = () => {
  router.push({ name: 'workspublish' })
}

onMounted(() => {
  fetchWorks()
})
</script>

<template>
  <div class="works-manager-container" style="padding: 24px">
    <a-card title="作品管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="goPublish">发布作品</a-button>
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
          <template v-if="column.key === 'thumbnail'">
            <a-image v-if="record.thumbnail" :width="80" :src="record.thumbnail" />
            <span v-else>无封面</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="handleEdit(record.id)">编辑</a-button>
            <a-popconfirm
              title="确定要删除此作品吗？"
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
  </div>
</template>
