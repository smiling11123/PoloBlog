<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getDeletedArticleList, restoreDeletedArticle } from '~@/api/Article/ArticleList'
import { getDeletedUserList, restoreUserById } from '@/api/common/user'
import { getDeletedCategoryList, restoreCategory } from '~/api/Category'
import { getDeletedWorksList, restoreWorks } from '~/api/works'
import { getDeletedWallpaperList, restoreWallpaper } from '~/api/wallpaper'
import { getDeletedMessageSlipList, restoreMessageSlip } from '~/api/messageSlip'
import { getDeletedSloganList, restoreSlogan } from '~/api/slogan'
import { formatTimer } from '~@/utils/tools'

type TabKey = 'articles' | 'users' | 'categories' | 'works' | 'wallpapers' | 'messages' | 'slogans'

interface PaginationState {
  current: number
  pageSize: number
  total: number
}

const route = useRoute()
const router = useRouter()
const validTabs: TabKey[] = ['articles', 'users', 'categories', 'works', 'wallpapers', 'messages', 'slogans']

const resolveTab = (value: unknown): TabKey => {
  return validTabs.includes(value as TabKey) ? value as TabKey : 'articles'
}

const currentTab = ref<TabKey>(resolveTab(route.query.tab))
const loadingMap = reactive<Record<TabKey, boolean>>({
  articles: false,
  users: false,
  categories: false,
  works: false,
  wallpapers: false,
  messages: false,
  slogans: false,
})
const loadedMap = reactive<Record<TabKey, boolean>>({
  articles: false,
  users: false,
  categories: false,
  works: false,
  wallpapers: false,
  messages: false,
  slogans: false,
})
const dataMap = reactive<Record<TabKey, any[]>>({
  articles: [],
  users: [],
  categories: [],
  works: [],
  wallpapers: [],
  messages: [],
  slogans: [],
})
const paginationMap = reactive<Record<TabKey, PaginationState>>({
  articles: { current: 1, pageSize: 8, total: 0 },
  users: { current: 1, pageSize: 8, total: 0 },
  categories: { current: 1, pageSize: 10, total: 0 },
  works: { current: 1, pageSize: 8, total: 0 },
  wallpapers: { current: 1, pageSize: 8, total: 0 },
  messages: { current: 1, pageSize: 10, total: 0 },
  slogans: { current: 1, pageSize: 10, total: 0 },
})

const tabOptions = [
  { key: 'articles', label: '文章回收站' },
  { key: 'users', label: '用户回收站' },
  { key: 'categories', label: '分类回收站' },
  { key: 'works', label: '作品回收站' },
  { key: 'wallpapers', label: '壁纸回收站' },
  { key: 'messages', label: '留言回收站' },
  { key: 'slogans', label: '标语回收站' },
] as const

const tabRouteMap: Record<TabKey, string> = {
  articles: 'blogmanager',
  users: 'usermanager',
  categories: 'categorymanager',
  works: 'worksmanager',
  wallpapers: 'wallpapermanager',
  messages: 'messagemanager',
  slogans: 'sloganmanager',
}

const columnsMap: Record<TabKey, any[]> = {
  articles: [
    { title: '文章标题', dataIndex: 'title', key: 'title', ellipsis: true },
    { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 140 },
    { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  users: [
    { title: '用户', dataIndex: 'nickname', key: 'user', ellipsis: true },
    { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
    { title: '角色', dataIndex: 'roleName', key: 'roleName', width: 120 },
    { title: '注册时间', dataIndex: 'createTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  categories: [
    { title: '分类名称', dataIndex: 'name', key: 'name', width: 180 },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 100 },
    { title: '更新时间', dataIndex: 'updateTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  works: [
    { title: '作品名称', dataIndex: 'title', key: 'title', width: 180 },
    { title: '封面', dataIndex: 'thumbnail', key: 'thumbnail', width: 120 },
    { title: '作品地址', dataIndex: 'address', key: 'address', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  wallpapers: [
    { title: '壁纸名称', dataIndex: 'name', key: 'name', width: 180 },
    { title: '预览', dataIndex: 'address', key: 'preview', width: 140 },
    { title: '创建时间', dataIndex: 'createTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  messages: [
    { title: '编号', dataIndex: 'id', key: 'id', width: 180 },
    { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 180 },
    { title: '留言内容', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
  slogans: [
    { title: '标语内容', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', key: 'time', width: 180 },
    { title: '操作', key: 'action', width: 120 },
  ],
}

const fetcherMap: Record<TabKey, (page: number, size: number) => Promise<any>> = {
  articles: (page, size) => getDeletedArticleList({ page, size }),
  users: (page, size) => getDeletedUserList({ page, size }),
  categories: (page, size) => getDeletedCategoryList(page, size),
  works: (page, size) => getDeletedWorksList(page, size),
  wallpapers: (page, size) => getDeletedWallpaperList(page, size),
  messages: (page, size) => getDeletedMessageSlipList(page, size),
  slogans: (page, size) => getDeletedSloganList(page, size),
}

const restoreMap: Record<TabKey, (record: any) => Promise<any>> = {
  articles: record => restoreDeletedArticle(record.id),
  users: record => restoreUserById(record.id),
  categories: record => restoreCategory(record.id),
  works: record => restoreWorks(record.id),
  wallpapers: record => restoreWallpaper(record.id),
  messages: record => restoreMessageSlip(record.id),
  slogans: record => restoreSlogan(record.id),
}

const currentRows = computed(() => dataMap[currentTab.value])
const currentColumns = computed(() => columnsMap[currentTab.value])
const currentLoading = computed(() => loadingMap[currentTab.value])
const currentPagination = computed(() => {
  const pageState = paginationMap[currentTab.value]
  return {
    current: pageState.current,
    pageSize: pageState.pageSize,
    total: pageState.total,
    showSizeChanger: false,
    onChange: (page: number, pageSize: number) => {
      pageState.current = page
      pageState.pageSize = pageSize
      void loadTabData(currentTab.value)
    },
    showTotal: (total: number) => `共 ${total} 条数据`,
  }
})

const formatTime = (value?: string) => value ? formatTimer(value) : '暂无记录'

const getCellValue = (record: Record<string, any>, dataIndex: unknown) => {
  if (typeof dataIndex === 'string' || typeof dataIndex === 'number') {
    return record[dataIndex]
  }
  if (Array.isArray(dataIndex)) {
    return dataIndex.reduce<any>((value, key) => (value == null ? value : value[key]), record)
  }
  return undefined
}

const syncTabToRoute = (tab: TabKey) => {
  router.replace({
    name: 'recyclebin',
    query: { ...route.query, tab },
  })
}

const loadTabData = async (tab: TabKey) => {
  loadingMap[tab] = true
  try {
    const { current, pageSize } = paginationMap[tab]
    const res: any = await fetcherMap[tab](current, pageSize)
    if (res.code === 200) {
      dataMap[tab] = res.data?.records || []
      paginationMap[tab].total = res.data?.total || 0
      paginationMap[tab].current = res.data?.current || current
      loadedMap[tab] = true
    } else {
      message.error(res.msg || '获取回收站数据失败')
    }
  } catch (error) {
    message.error('获取回收站数据失败')
  } finally {
    loadingMap[tab] = false
  }
}

const handleRestore = async (record: any) => {
  const tab = currentTab.value
  try {
    const res: any = await restoreMap[tab](record)
    if (res.code === 200) {
      message.success('恢复成功')
      const pageState = paginationMap[tab]
      if (dataMap[tab].length === 1 && pageState.current > 1) {
        pageState.current -= 1
      }
      await loadTabData(tab)
    } else {
      message.error(res.msg || '恢复失败')
    }
  } catch (error) {
    message.error('恢复失败')
  }
}

const handleTabChange = (key: string | number) => {
  currentTab.value = resolveTab(String(key))
}

const goBack = () => {
  router.push({ name: tabRouteMap[currentTab.value] })
}

watch(() => route.query.tab, (value) => {
  const nextTab = resolveTab(value)
  if (currentTab.value !== nextTab) {
    currentTab.value = nextTab
  }
})

watch(currentTab, (tab) => {
  syncTabToRoute(tab)
  if (!loadedMap[tab]) {
    void loadTabData(tab)
  }
})

onMounted(() => {
  syncTabToRoute(currentTab.value)
  void loadTabData(currentTab.value)
})
</script>

<template>
  <page-container>
    <a-card class="recycle-overview" :bordered="false">
      <div class="overview-bar">
        <a-button @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回管理页
        </a-button>
        <div class="overview-copy">
          <h2>统一回收站</h2>
          <p>集中查看后台被逻辑删除的文章、用户、分类、作品、壁纸、留言和标语。</p>
        </div>
      </div>
    </a-card>

    <a-card :bordered="false" class="mt-4">
      <a-tabs :active-key="currentTab" @change="handleTabChange">
        <a-tab-pane v-for="item in tabOptions" :key="item.key" :tab="item.label" />
      </a-tabs>

      <a-table
        :columns="currentColumns"
        :row-key="(record: any) => record.id"
        :data-source="currentRows"
        :loading="currentLoading"
        :pagination="currentPagination"
        :scroll="{ x: 920 }"
        class="recycle-table"
      >
        <template #emptyText>
          <a-empty description="当前分类暂无已删除内容" />
        </template>

        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-popconfirm
              title="确定恢复这条记录吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleRestore(record)"
            >
              <a-button type="link" class="restore-btn">
                <ReloadOutlined />
                恢复
              </a-button>
            </a-popconfirm>
          </template>

          <template v-else-if="column.key === 'user'">
            <div class="user-cell">
              <a-avatar :src="record.avatar" :size="38" shape="square">
                {{ (record.nickname || record.username)?.charAt(0) }}
              </a-avatar>
              <div class="user-copy">
                <strong>{{ record.nickname || record.username }}</strong>
                <span>@{{ record.username }}</span>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'preview' || column.key === 'thumbnail'">
            <template v-if="getCellValue(record, column.dataIndex)">
              <a-image
                :width="column.key === 'preview' ? 100 : 80"
                :src="getCellValue(record, column.dataIndex)"
              />
            </template>
            <span v-else>无预览</span>
          </template>

          <template v-else-if="column.key === 'time'">
            {{ formatTime(getCellValue(record, column.dataIndex)) }}
          </template>

          <template v-else-if="column.key === 'roleName'">
            <a-tag v-if="record.roleName" color="blue">{{ record.roleName }}</a-tag>
            <span v-else>未分配</span>
          </template>

          <template v-else-if="column.key === 'email'">
            {{ record.email || '未绑定' }}
          </template>

          <template v-else-if="column.key === 'address'">
            <a :href="record.address" target="_blank" class="address-link">{{ record.address }}</a>
          </template>

          <template v-else>
            <span>{{ getCellValue(record, column.dataIndex) || '-' }}</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </page-container>
</template>

<style scoped lang="scss">
.recycle-overview {
  margin-bottom: 16px;
}

.overview-bar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-copy {
  h2 {
    margin: 0 0 6px;
    font-size: 24px;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.45);
  }
}

.restore-btn {
  color: #52c41a;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;

  strong {
    color: rgba(255, 255, 255, 0.88);
  }

  span {
    color: rgba(255, 255, 255, 0.45);
    font-size: 12px;
  }
}

.address-link {
  color: inherit;
  text-decoration: none;
  word-break: break-all;

  &:hover {
    color: #1677ff;
  }
}

@media screen and (max-width: 768px) {
  .overview-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
