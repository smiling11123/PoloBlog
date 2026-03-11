<template>
  <div class="user-manage-container">
    <a-card :bordered="false" title="用户管理">
      
      <template #extra>
        <a-space>
          <a-input-search
            v-model:value="queryParams.keyword"
            placeholder="输入昵称或邮箱搜索"
            enter-button
            allow-clear
            @search="handleSearch"
          />
          <a-button type="primary" danger @click="router.push({ name: 'deletedusers' })">
            已删除用户
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
          <a-list-item>
            <template #actions>
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

            <a-list-item-meta :description="item.intro || '该用户暂无简介'">
              <template #title>
                <a class="username-link" @click="handleDetail(item)">{{ item.nickname }}</a>
                <span class="sub-text">(@{{ item.username }})</span>
                <a-tag :color="item.statusText === '正常' ? 'green' : 'red'" class="ml-2">
                  {{ item.statusText === "正常" ? '在线' : '离线' }}
                </a-tag>
              </template>
              
              <template #avatar>
                <a-avatar :src="item.avatar" :size="50" shape="square">
                  {{ item.nickname?.charAt(0) }}
                </a-avatar>
              </template>
            </a-list-item-meta>

            <div class="list-content">
              <div class="list-content-item">
                <span>邮箱</span>
                <p>{{ item.email || '未绑定' }}</p>
              </div>
              <div class="list-content-item">
                <span>注册时间</span>
                <p>{{ formatTimer(item.createTime) }}</p>
              </div>
            </div>
          </a-list-item>
        </template>
      </a-list>

    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getUserList, deleteUser, searchUser } from '@/api/common/user'; // 确保路径正确
import { formatTimer } from '~@/utils/tools';
// 定义接口 (VO)
interface UserVO {
  id: string;
  username: string;
  nickname: string;
  avatar: string;
  email: string;
  intro: string;
  status: string;
  createTime: string;
}

const router = useRouter();

// --- 状态定义 ---
const loading = ref(false);
const userList = ref<UserVO[]>([]);
const queryParams = reactive({
  keyword: '',
  page: 1,
  size: 10
});

// 分页配置
const paginationProps = reactive({
  onChange: (page: number) => {
    queryParams.page = page;
    fetchData();
  },
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条数据`
});

// --- 生命周期 ---
onMounted(() => {
  fetchData();
});

// --- 核心方法 ---

// 1. 获取数据 (整合了搜索)
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getUserList(queryParams) as any;

    if (res.code === 200) {
      userList.value = res.data.records;
      paginationProps.total = res.data.total;
      paginationProps.pageSize = res.data.size;
    } else {
      message.error(res.msg || '获取列表失败');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 2. 搜索
const handleSearch = async () => {
  queryParams.page = 1; // 重置到第一页
  //fetchData();
  loading.value = true;
  try {
    const res = await searchUser(queryParams) as any;

    if (res.code === 200) {
      userList.value = res.data.records;
      paginationProps.total = res.data.total;
      paginationProps.pageSize = res.data.size;
    } else {
      message.error(res.msg || '获取列表失败');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 3. 删除
const handleDelete = async (id: string) => {
  try {
    const res = await deleteUser(id) as any;
    if (res.code === 200) {
      message.success('删除成功');
      // 只有一条数据且不是第一页时，删完回到上一页
      if (userList.value.length === 1 && queryParams.page > 1) {
        queryParams.page--;
      }
      fetchData();
    } else {
      message.error(res.msg || '删除失败');
    }
  } catch (error) {
    message.error('请求失败');
  }
};

// 4. 跳转详情页
const handleDetail = (item: UserVO) => {
  router.push({ name: 'UserDetail', params: { id: item.id } });
};

</script>

<style scoped lang="scss">
.user-manage-container {
  padding: 24px;
}

.username-link {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: color 0.3s;
  
  &:hover {
    color: #1890ff;
  }
}

.sub-text {
  color: #999;
  font-size: 13px;
  margin-left: 8px;
}

.ml-2 {
  margin-left: 8px;
}

.text-danger {
  color: #ff4d4f;
  &:hover {
    color: #ff7875;
  }
}

.list-content {
  display: flex;
  
  .list-content-item {
    color: rgba(0, 0, 0, 0.45);
    display: inline-block;
    vertical-align: middle;
    font-size: 14px;
    margin-left: 40px;
    text-align: center;
    min-width: 100px;
    
    span {
      color: rgba(255, 255, 255, 0.45);
      font-size: 12px;
    }
    
    p {
      margin-top: 4px;
      margin-bottom: 0;
      color: rgba(255, 255, 255, 0.85);
    }
  }
}

/* 移动端响应式：隐藏右侧的邮箱和时间，避免挤压 */
@media screen and (max-width: 768px) {
  .list-content {
    display: none;
  }
}
</style>
