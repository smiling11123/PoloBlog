<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ReloadOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { ArticleVO } from 'types/Entity'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getDeletedArticleList } from '~@/api/Article/ArticleList'
import { updateArticle } from '~@/api/Article/ArticleEdit'
import { formatTimer } from '~@/utils/tools'

const router = useRouter()

// --- 1. 状态定义 ---
const loading = ref(false)
const articleList = ref<ArticleVO[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
  showSizeChanger: false,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page
    pagination.pageSize = pageSize
    loadData()
  }
})

// --- 2. 加载数据逻辑 ---
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize
    }
    const res = await getDeletedArticleList(params) as any
    
    if (res.code === 200) {
      articleList.value = res.data.records || []
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    message.error('获取回收站数据失败')
  } finally {
    loading.value = false
  }
}

// --- 3. 恢复加载 ---
onMounted(() => {
  loadData()
})

// --- 4. 业务逻辑 ---
async function handleRestore(item: ArticleVO) {
  try {
    // 构造更新数据，将 isDeleted 设为 0
    const updateData = {
      ...item,
      isDeleted: 0
    }
    const res = await updateArticle(updateData) as any
    if (res.code === 200) {
      message.success('文章恢复成功')
      await loadData()
    } else {
      message.error(res.msg || '恢复失败')
    }
  } catch (e) {
    console.error(e)
    message.error('恢复操作出错')
  }
}

function goBack() {
  router.push({ name: 'blogmanager' })
}
</script>

<template>
  <page-container>
    <a-card mb-4>
      <div class="flex justify-between items-center">
        <a-button @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回博客管理
        </a-button>
        <h2 class="text-xl font-bold m-0">回收站</h2>
        <div style="width: 100px"></div> <!-- 占位保持居中效果 -->
      </div>
    </a-card>

    <a-card :bordered="false" class="mt-4 search-result-card" :body-style="{ padding: '24px' }">
      <a-empty v-if="!loading && articleList.length === 0" description="回收站空空如也" />
      <a-list v-else :loading="loading" :data-source="articleList" item-layout="vertical" size="large" :pagination="pagination">
        <template #renderItem="{ item }">
          <a-list-item :key="item.id" class="article-list-item">
            <template #actions>
              <a-popconfirm title="确定恢复该文章吗？" ok-text="确定" cancel-text="取消" @confirm="handleRestore(item)">
                <span key="restore" class="action-btn hover:text-green-500">
                  <ReloadOutlined />
                  <span class="ml-1">恢复</span>
                </span>
              </a-popconfirm>
            </template>
            <template #extra>
              <div v-if="item.thumbnail" class="article-cover">
                <img :src="item.thumbnail" alt="cover" />
              </div>
            </template>

            <div class="article-content">
              <h3 class="article-title text-lg font-medium mb-2 transition-colors">
                {{ item.title }}
              </h3>

              <div class="mb-3">
                <a-tag color="blue" class="mr-1">{{ item.categoryName }}</a-tag>
                <a-tag color="red">已删除</a-tag>
              </div>

              <div class="text-gray-500 mb-4 line-clamp-3 leading-relaxed">
                {{ item.summary || '暂无摘要' }}
              </div>

              <div class="flex items-center justify-between text-sm text-gray-400">
                <span>创建于 {{ formatTimer(item.createTime) }}</span>
                <span>删除于 {{ formatTimer(item.updateTime) }}</span>
              </div>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </page-container>
</template>

<style lang="less" scoped>
.article-list-item {
  padding: 20px 0;
  transition: all 0.3s;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.action-btn {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.3s;
  margin-right: 16px;
}

.article-cover {
  width: 200px;
  height: 134px;
  overflow: hidden;
  border-radius: 6px;
  flex-shrink: 0;
  margin-left: 24px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }

  .article-list-item:hover & img {
    transform: scale(1.05);
  }
}

@media (max-width: 768px) {
  :deep(.ant-list-item-main),
  :deep(.ant-list-item-extra) {
    display: block;
    margin: 0;
    width: 100%;
  }

  .article-cover {
    width: 100%;
    height: 180px;
    margin-left: 0;
    margin-bottom: 16px;
  }
}
</style>
