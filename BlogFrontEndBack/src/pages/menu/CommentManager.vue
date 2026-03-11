<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getRootComment } from '@/api/Comment'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import CommentItem from './components/CommentItem.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const articleId = ref(String(route.params.articleId))
const rootComments = ref<any[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  onChange: (page: number) => {
    pagination.current = page
    fetchRootComments()
  }
})

const fetchRootComments = async () => {
  if (!articleId.value) return
  loading.value = true
  try {
    const res: any = await getRootComment({
      articleId: articleId.value,
      page: pagination.current,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      rootComments.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    message.error('获取根评论失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchRootComments()
})
</script>

<template>
  <page-container>
    <a-card title="评论管理">
      <template #extra>
        <a-button @click="goBack">
          <ArrowLeftOutlined />
          返回
        </a-button>
      </template>
      
      <a-list
        :loading="loading"
        item-layout="vertical"
        :data-source="rootComments"
        :pagination="pagination"
      >
        <template #renderItem="{ item }">
          <a-list-item :key="item.id">
            <CommentItem :comment="item" :depth="0" />
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </page-container>
</template>

<style scoped lang="less">
/* Page specific styles */
</style>
