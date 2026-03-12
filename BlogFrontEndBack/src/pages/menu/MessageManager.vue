<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getMessageSlipList, deleteMessageSlip } from '~/api/messageSlip'

const router = useRouter()
const data = ref<any[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id' },
  { title: '用户 ID', dataIndex: 'userId', key: 'userId' },
  { title: '留言内容', dataIndex: 'content', key: 'content' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action' },
]

const fetchMessages = async (page = 1, size = 20) => {
  loading.value = true
  try {
    const res: any = await getMessageSlipList(page, size)
    data.value = res?.data?.records || []
    pagination.value.current = res?.data?.current || 1
    pagination.value.total = res?.data?.total || 0
  } catch (error) {
    message.error('获取留言列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  fetchMessages(pag.current, pag.pageSize)
}

const handleDelete = async (id: number) => {
  try {
    const res: any = await deleteMessageSlip(id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchMessages(pagination.value.current, pagination.value.pageSize)
      return
    }
    message.error(res.msg || '删除留言失败')
  } catch (error) {
    message.error('删除留言失败')
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<template>
  <div class="message-manager-container" style="padding: 24px">
    <a-card title="留言管理" :bordered="false">
      <template #extra>
        <a-button @click="router.push({ name: 'recyclebin', query: { tab: 'messages' } })">回收站</a-button>
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
          <template v-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除这条留言吗？"
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
