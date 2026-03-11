<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { deleteSlogan, getSloganList, publishSlogan } from '~/api/slogan'

const slogans = ref<any[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const formState = ref({
  content: '',
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id', width: 220 },
  { title: '标语内容', dataIndex: 'content', key: 'content' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 220 },
  { title: '操作', key: 'action', width: 120 },
]

const fetchSlogans = async () => {
  loading.value = true
  try {
    const res: any = await getSloganList()
    if (res.code === 200) {
      slogans.value = res.data || []
    } else {
      message.error(res.msg || '获取标语列表失败')
    }
  } catch (error) {
    message.error('获取标语列表失败')
  } finally {
    loading.value = false
  }
}

const showModal = () => {
  formState.value = { content: '' }
  modalVisible.value = true
}

const handleCreate = async () => {
  const content = formState.value.content.trim()
  if (!content) {
    return message.warning('标语内容不能为空')
  }

  confirmLoading.value = true
  try {
    const res: any = await publishSlogan({ content })
    if (res.code === 200) {
      message.success('添加成功')
      modalVisible.value = false
      formState.value = { content: '' }
      fetchSlogans()
    } else {
      message.error(res.msg || '添加失败')
    }
  } catch (error) {
    message.error('添加失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleDelete = async (id: string | number) => {
  try {
    const res: any = await deleteSlogan(id)
    if (res.code === 200) {
      message.success('删除成功')
      fetchSlogans()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchSlogans()
})
</script>

<template>
  <div class="slogan-manager-container" style="padding: 24px">
    <a-card title="标语管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showModal">添加标语</a-button>
      </template>

      <a-table
        :columns="columns"
        :row-key="(record) => record.id"
        :data-source="slogans"
        :loading="loading"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'content'">
            <div class="slogan-content">{{ record.content }}</div>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除这条标语吗？"
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
      title="添加标语"
      :confirm-loading="confirmLoading"
      @ok="handleCreate"
    >
      <a-form layout="vertical">
        <a-form-item label="标语内容" required>
          <a-textarea
            v-model:value="formState.content"
            :rows="4"
            :maxlength="128"
            placeholder="请输入前台展示的标语内容"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.slogan-manager-container {
  min-height: 100%;
}

.slogan-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}
</style>
