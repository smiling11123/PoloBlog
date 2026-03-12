<template>
  <div class="user-manage-container">
    <a-card :bordered="false" title="用户管理">
      <template #extra>
        <a-space wrap>
          <a-input-search
            v-model:value="queryParams.keyword"
            placeholder="输入用户名、昵称或邮箱搜索"
            enter-button
            allow-clear
            @search="handleSearch"
          />
          <a-button @click="router.push({ name: 'recyclebin', query: { tab: 'users' } })">
            回收站
          </a-button>
        </a-space>
      </template>

      <a-list
        class="user-list"
        :loading="loading"
        item-layout="horizontal"
        :data-source="userList"
        :pagination="paginationProps"
      >
        <template #renderItem="{ item }">
          <a-list-item class="user-row">
            <template #actions>
              <a key="edit" @click="handleEdit(item)">编辑资料</a>
              <a key="view" @click="handleDetail(item)">查看详情</a>
              <a-popconfirm
                title="确定要删除此用户吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(item.id)"
              >
                <a key="delete" class="text-danger">删除</a>
              </a-popconfirm>
            </template>

            <div class="user-row-content">
              <a-list-item-meta :description="item.intro || '该用户暂无简介'">
                <template #title>
                  <div class="title-row">
                    <a class="username-link" @click="handleDetail(item)">{{ item.nickname || item.username }}</a>
                    <span class="sub-text">@{{ item.username }}</span>
                    <a-tag v-if="item.roleName" color="blue">{{ item.roleName }}</a-tag>
                    <a-tag :color="item.statusText === '正常' ? 'green' : 'default'">
                      {{ item.statusText || '未知状态' }}
                    </a-tag>
                    <a-tag color="geekblue">{{ getSourceLabel(item.source) }}</a-tag>
                  </div>
                </template>

                <template #avatar>
                  <a-avatar :src="item.avatar" :size="56" shape="square">
                    {{ (item.nickname || item.username)?.charAt(0) }}
                  </a-avatar>
                </template>
              </a-list-item-meta>

              <div class="list-content">
                <div class="list-content-item">
                  <span>邮箱</span>
                  <p>{{ item.email || '未绑定' }}</p>
                </div>
                <div class="list-content-item">
                  <span>最近登录</span>
                  <p>{{ formatTime(item.loginDate) }}</p>
                </div>
                <div class="list-content-item">
                  <span>注册时间</span>
                  <p>{{ formatTime(item.createTime) }}</p>
                </div>
              </div>
            </div>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteUser, getUserList, searchUser, type UserInfo } from '@/api/common/user'
import { formatTimer } from '~@/utils/tools'

type UserVO = UserInfo

const router = useRouter()
const loading = ref(false)
const userList = ref<UserVO[]>([])
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10,
})

const paginationProps = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  onChange: (page: number) => {
    queryParams.page = page
    fetchData()
  },
  showTotal: (total: number) => `共 ${total} 条数据`,
})

onMounted(() => {
  fetchData()
})

const formatTime = (value?: string) => value ? formatTimer(value) : '暂无记录'

const getSourceLabel = (source?: string) => {
  if (!source) {
    return '未记录'
  }
  const normalized = source.toLowerCase()
  if (normalized === 'gitee') {
    return 'Gitee'
  }
  if (normalized === 'github') {
    return 'GitHub'
  }
  if (normalized === 'local') {
    return '本地账号'
  }
  return source
}

const fetchData = async () => {
  loading.value = true
  try {
    const api = queryParams.keyword.trim() ? searchUser : getUserList
    const res: any = await api(queryParams)

    if (res.code === 200) {
      userList.value = res.data.records || []
      paginationProps.total = res.data.total
      paginationProps.pageSize = res.data.size
      paginationProps.current = res.data.current
    } else {
      message.error(res.msg || '获取列表失败')
    }
  } catch (error) {
    console.error(error)
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  queryParams.page = 1
  await fetchData()
}

const handleDelete = async (id: string | number) => {
  try {
    const res: any = await deleteUser(String(id))
    if (res.code === 200) {
      message.success('删除成功')
      if (userList.value.length === 1 && queryParams.page > 1) {
        queryParams.page--
      }
      await fetchData()
    } else {
      message.error(res.msg || '删除失败')
    }
  } catch (error) {
    message.error('请求失败')
  }
}

const handleDetail = (item: UserVO) => {
  router.push({ name: 'UserDetail', params: { id: item.id } })
}

const handleEdit = (item: UserVO) => {
  router.push({ name: 'UserEdit', params: { id: item.id } })
}
</script>

<style scoped lang="scss">
.user-manage-container {
  padding: 24px;
}

.user-row {
  padding-inline: 0;
}

.user-row-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.username-link {
  color: rgba(255, 255, 255, 0.88);
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #1677ff;
  }
}

.sub-text {
  color: rgba(255, 255, 255, 0.45);
  font-size: 13px;
}

.text-danger {
  color: #ff4d4f;

  &:hover {
    color: #ff7875;
  }
}

.list-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.list-content-item {
  min-width: 120px;

  span {
    display: block;
    color: rgba(255, 255, 255, 0.45);
    font-size: 12px;
    margin-bottom: 4px;
  }

  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.88);
    font-size: 14px;
    line-height: 1.5;
  }
}

@media screen and (max-width: 992px) {
  .user-row-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .list-content {
    width: 100%;
    flex-wrap: wrap;
    gap: 16px;
    padding-left: 72px;
  }
}

@media screen and (max-width: 768px) {
  .user-manage-container {
    padding: 16px;
  }

  .list-content {
    padding-left: 0;
  }
}
</style>
