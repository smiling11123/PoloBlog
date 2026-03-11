<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { EditOutlined, DeleteOutlined, StopOutlined, CheckCircleOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import CrudTableModal from './components/crud-table-modal.vue'
import { ArticleVO } from 'types/Entity'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getManuscriptList } from '~@/api/Article/Manuscript'
import { updateArticle } from '~@/api/Article/ArticleEdit'
import { formatTimer } from '~@/utils/tools'

const router = useRouter()

// --- 1. 手动定义状态 ---
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
      size: pagination.pageSize
    }
    const res = await getManuscriptList(params) as any

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


// 初始化加载
onMounted(() => {
  loadData()
})

// --- 4. 业务操作逻辑 ---

// 删除
async function handleDelete(id: number | string) {
  try {
    console.log('删除文章ID:', id)
    // await deleteMyArticle(id) // 你的删除API
    message.success('删除成功')

    // 删除成功后刷新
    await loadData()
  } catch (e) {
    console.log(e)
  }
}

// 打开编辑
function handleEdit(record: any) {
  router.push({ name: 'edit', params: { id: record.id } })
}


// 禁评/允评
async function toggleCommentStatus(item: any) {
  const newStatus = !item.enableComment
  if (item.isComment == 0) {
    item.isComment = 1
  }
  else {
    item.isComment = 0
  }

  await updateArticle(item)
  item.enableComment = newStatus
  message.success(newStatus ? '已允许评论' : '已禁止评论')
}
// 发布
const handlePublish = async (item: any) => {
  if(item.status == 0){
      item.status = 1
  }
  await updateArticle(item)
  await loadData()
}

// 跳转详情
const gotoDetail = async (id: any) => {
  router.push({ name: 'ArticleDetail', params: { id: id } })
}
</script>

<template>
  <page-container>
    <a-card mb-4>
    </a-card>

    <a-card title="草稿管理">
    </a-card>

    <a-card :bordered="false" class="mt-4 search-result-card" :body-style="{ padding: '24px' }">
      <a-list :loading="loading" :data-source="articleList" item-layout="vertical" size="large" :pagination="pagination">
        <template #renderItem="{ item }">
          <a-list-item :key="item.id" class="article-list-item">

            <template #actions>
              <span key="edit" class="action-btn hover:text-blue-500" @click="handleEdit(item)">
                <EditOutlined />
                <span class="ml-1">编辑</span>
              </span>

              <span key="comment" class="action-btn hover:text-orange-500" @click="toggleCommentStatus(item)">
                <StopOutlined v-if="item.enableComment" />
                <CheckCircleOutlined v-else />
                <span class="ml-1">{{ item.isComment == 1 ? '禁止评论' : '允许评论' }}</span>
              </span>

              <a-popconfirm title="确定删除该文章吗？" ok-text="确定" cancel-text="取消" @confirm="handleDelete(item.id)">
                <span key="delete" class="action-btn hover:text-red-500">
                  <DeleteOutlined />
                  <span class="ml-1">删除</span>
                </span>
              </a-popconfirm>

              <a-popconfirm title="确定发布该文章吗？" ok-text="确定" cancel-text="取消" @confirm="handlePublish(item)">
                <span key="publish" class="action-btn hover:text-red-500">
                  <ShareAltOutlined />
                  <span class="ml-1">发布</span>
                </span>
              </a-popconfirm>
            </template>
            <template #extra>
              <div v-if="item.thumbnail" class="article-cover">
                <img :src="item.thumbnail" alt="cover" />
              </div>
            </template>

            <div class="article-content">
              <h3 @click="gotoDetail(item.id)"
                class="article-title text-lg font-medium mb-2 cursor-pointer hover:text-blue-600 transition-colors">
                {{ item.title }}
              </h3>

              <div class="mb-3">
                <a-tag color="blue" class="mr-1">{{ item.categoryName }}</a-tag>
                <a-tag v-if="item.isComment == 0" color="orange">禁评</a-tag>
                <a-tag v-else color="blue">允许评论</a-tag>
              </div>

              <div class="text-gray-500 mb-4 line-clamp-3 leading-relaxed">
                {{ item.summary || '暂无摘要' }}
              </div>

              <div class="flex items-center justify-between text-sm text-gray-400">
                <span>创建于 {{ formatTimer(item.createTime) }}</span>
                <span>更新于 {{ formatTimer(item.updateTime) }}</span>
              </div>
            </div>

          </a-list-item>
        </template>
      </a-list>
    </a-card>
    <CrudTableModal ref="crudTableModal" />
  </page-container>
</template>


<style lang="less" scoped>
/* 样式保持不变，直接复用你之前的 */
.system-crud-wrapper {
  .ant-form-item {
    margin: 0;
  }
}

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
  margin-right: 16px; // 增加间距
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
