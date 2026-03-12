<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { useRouter } from 'vue-router'
import { deleteSlogan, getSloganList, publishSlogan } from '~/api/slogan'

interface SloganItem {
  id: string
  content: string
  createTime: string
}

const MAX_SLOGAN_LENGTH = 128
const router = useRouter()

const slogans = ref<SloganItem[]>([])
const loading = ref(false)
const modalVisible = ref(false)
const confirmLoading = ref(false)
const formRef = ref<FormInstance>()
const formState = reactive({
  content: '',
})

const columns = [
  { title: '编号 (ID)', dataIndex: 'id', key: 'id', width: 220 },
  { title: '标语内容', dataIndex: 'content', key: 'content' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 220 },
  { title: '操作', key: 'action', width: 120 },
]

const normalizedContent = computed(() => formState.content.trim())
const duplicateSlogan = computed(() =>
  slogans.value.find(item => item.content.trim() === normalizedContent.value),
)
const canSubmit = computed(() =>
  normalizedContent.value.length > 0
  && normalizedContent.value.length <= MAX_SLOGAN_LENGTH
  && !duplicateSlogan.value
  && !confirmLoading.value,
)

const rules: Record<string, Rule[]> = {
  content: [
    {
      required: true,
      validator: async (_rule, value: string) => {
        const content = value?.trim() || ''
        if (!content) {
          return Promise.reject('标语内容不能为空')
        }
        if (content.length > MAX_SLOGAN_LENGTH) {
          return Promise.reject(`标语内容不能超过 ${MAX_SLOGAN_LENGTH} 个字符`)
        }
        if (slogans.value.some(item => item.content.trim() === content)) {
          return Promise.reject('该标语已存在')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

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
  formRef.value?.resetFields()
  formState.content = ''
  modalVisible.value = true
}

const handleCancel = () => {
  formRef.value?.resetFields()
  formState.content = ''
  modalVisible.value = false
}

const handleCreate = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  const content = normalizedContent.value
  if (!content || duplicateSlogan.value) {
    if (duplicateSlogan.value) {
      message.warning('该标语已存在，无需重复添加')
    }
    return
  }

  confirmLoading.value = true
  try {
    const res: any = await publishSlogan({ content })
    if (res.code === 200) {
      message.success('添加成功')
      modalVisible.value = false
      formRef.value?.resetFields()
      formState.content = ''
      await fetchSlogans()
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
        <div class="action-group">
          <span class="slogan-count">共 {{ slogans.length }} 条</span>
          <a-button @click="router.push({ name: 'recyclebin', query: { tab: 'slogans' } })">回收站</a-button>
          <a-button @click="fetchSlogans" :loading="loading">刷新</a-button>
          <a-button type="primary" @click="showModal">添加标语</a-button>
        </div>
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
      v-model:open="modalVisible"
      title="添加标语"
      :confirm-loading="confirmLoading"
      :ok-button-props="{ disabled: !canSubmit }"
      @ok="handleCreate"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" layout="vertical" :model="formState" :rules="rules">
        <a-alert
          v-if="duplicateSlogan"
          type="warning"
          show-icon
          class="mb-4"
          :message="`已存在相同标语：${duplicateSlogan.content}`"
        />
        <a-form-item label="标语内容" required name="content">
          <a-textarea
            v-model:value="formState.content"
            :rows="4"
            :maxlength="MAX_SLOGAN_LENGTH"
            placeholder="请输入前台展示的标语内容"
            show-count
            allow-clear
          />
        </a-form-item>
        <div class="form-hint">
          建议输入简短、清晰的展示文案，提交后会同步到前台轮播标语。
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.slogan-manager-container {
  min-height: 100%;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.slogan-count {
  color: rgba(255, 255, 255, 0.45);
  font-size: 13px;
}

.slogan-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.form-hint {
  color: rgba(255, 255, 255, 0.45);
  font-size: 13px;
  line-height: 1.6;
}

.mb-4 {
  margin-bottom: 16px;
}
</style>
