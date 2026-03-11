<script lang="ts" setup>
import Category from './components/category.vue' // 确保路径正确
import { searchByKeyWord } from '~@/api/Article/SearchArticle'
import { useRouter } from 'vue-router'
import { ref, watch, onMounted } from 'vue'
import { ArticleVO } from 'types/Entity'
import { formatTimer } from '~@/utils/tools'

const router = useRouter()
const props = defineProps({
  keyword: {
    type: String,
    default: ''
  }
})

// 存储当前选中的分类ID
const currentCategoryIds = ref<string[]>([])
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

// 2. 加载数据逻辑
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.pageSize,
      keyword: props.keyword?.toString() || '',
      categoryIds: currentCategoryIds.value
    }
    const res = await searchByKeyWord(params) as any

    if (res.code === 200) {

      articleList.value = res.data.records || []

      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
// 处理分类组件的变更事件
const handleCategoryChange = (ids: string[]) => {
  console.log('收到子组件分类ID:', ids)
  currentCategoryIds.value = ids
  loadData() // 重新查询
}

// 监听 props.keyword 变化
watch(() => props.keyword, () => {
  loadData()
})

// 初始化
onMounted(() => {
  loadData()
})


const gotoDetail = async (id: any) => {
  router.push({ name: 'ArticleDetail', params: { id: id } })
}
</script>

<template>
  <div>
    <Category @change="handleCategoryChange" />

    <a-card :bordered="false" class="mt-4 search-result-card" :body-style="{ padding: '24px' }">
      <a-list :data-source="articleList" item-layout="vertical" size="large" :pagination="pagination">
        <template #renderItem="{ item }">
          <a-list-item :key="item.id" class="article-list-item">

            <template #extra v-if="item.thumbnail">
              <div class="article-cover">
                <img :src="item.thumbnail" alt="cover" />
              </div>
            </template>

            <div class="article-content">
              <h3 @click="gotoDetail(item.id)"
                class="article-title text-lg font-medium mb-2 cursor-pointer hover:text-blue-600 transition-colors">
                {{ item.title }}
              </h3>

              <div class="mb-3">
                <a-tag color="blue" class="mr-1">{{ item.auth }}</a-tag>
              </div>

              <p class="text-gray-500 mb-4 line-clamp-3 leading-relaxed">
                {{ item.summary || '暂无摘要' }}
              </p>

              <div class="flex items-center justify-between text-sm text-gray-400">
                <div class="flex items-center gap-2">
                  <a-avatar :src="item.avatar" :size="20" />
                  <span class="font-medium text-gray-700">{{ item.createdBy }}</span>
                </div>
                <span>发布于 {{ formatTimer(item.updateTime) }}</span>
              </div>
            </div>

          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<style scoped lang="less">
/* 单个文章项 */
.article-list-item {
  padding: 20px 0;
  transition: all 0.3s;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

/* 右侧封面图容器 */
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

/* 响应式适配 */
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
