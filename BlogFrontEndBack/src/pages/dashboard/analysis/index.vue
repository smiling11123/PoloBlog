<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ArticleVO } from 'types/Entity'
import { getHotArticleList } from '~@/api/Article/HotArticleList'
import { useRouter } from 'vue-router'
//import Map from './components/Map.vue'

const router = useRouter()
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
    const res = await getHotArticleList(params) as any
    
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

onMounted(() => {
  loadData()
})

const gotoDetail = (id: any) => {
  router.push({ name: 'ArticleDetail', params: { id: id } })
}
</script>

<template>
  <page-container>
    
    <div class="header-title">热门推荐</div>

    <a-list
      :loading="loading"
      :data-source="articleList"
      :pagination="pagination"
      :grid="{ gutter: 24, xs: 1, sm: 2, md: 3, lg: 4, xl: 4, xxl: 6 }"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <div class="article-card" @click="gotoDetail(item.id)">
            <div class="cover-wrapper">
              <img :src="item.thumbnail" loading="lazy" class="cover" />
              <div class="overlay"></div>
            </div>
            <div class="info">
              <div class="title" :title="item.title">{{ item.title }}</div>
              <div class="view-count">浏览量：{{ item.viewCount || 0 }}</div>
            </div>
          </div>
        </a-list-item>
      </template>
    </a-list>
  </page-container>
</template>

<style scoped lang="scss">
.header-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  
}

// 修改 a-list 的分页器样式，让它居中并带点距离
:deep(.ant-pagination) {
  text-align: center;
  margin-top: 24px;
}

.article-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  //background: #3a3a3a;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  height: 100%; // 确保卡片高度一致

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    
    .cover {
      transform: scale(1.05);
    }
  }
}

.cover-wrapper {
  width: 100%;
  //aspect-ratio: 3 / 4;
  position: relative;
  overflow: hidden;
  //background: #f0f0f0;
  border-radius: 12px;
}

.cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.info {
  padding: 12px;
}

.title {
  font-size: 15px;
  font-weight: 600;
  
  line-height: 1.4;
  height: 42px; // 固定高度，防止标题行数不同导致卡片高度不一
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.view-count {
  font-size: 12px;
  
  font-weight: 500;
}
</style>
