<template>
  <div class="user-manage-container">
    <a-card :bordered="false" title="已删除用户">
      <template #extra>
        <a-button @click="goBack">
          返回用户管理
        </a-button>
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
              <a-popconfirm
                title="确定要恢复此用户吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleRestore(item)"
              >
                <a key="restore" class="text-success">恢复</a>
              </a-popconfirm>
            </template>

            <a-list-item-meta :description="item.intro || '该用户暂无简介'">
              <template #title>
                <span>{{ item.nickname }}</span>
                <span class="sub-text">(@{{ item.username }})</span>
                <a-tag color="red" class="ml-2">已删除</a-tag>
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
import { getDeletedUserList, updateUserInfo } from '@/api/common/user';
import { formatTimer } from '~@/utils/tools';

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

const loading = ref(false);
const userList = ref<UserVO[]>([]);
const queryParams = reactive({
  page: 1,
  size: 10
});

const paginationProps = reactive({
  onChange: (page: number) => {
    queryParams.page = page;
    fetchData();
  },
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条数据`
});

onMounted(() => {
  fetchData();
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getDeletedUserList(queryParams) as any;
    if (res.code === 200) {
      userList.value = res.data.records;
      paginationProps.total = res.data.total;
    } else {
      message.error(res.msg || '获取列表失败');
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleRestore = async (item: any) => {
  try {
    const res = await updateUserInfo({ ...item, isDeleted: 0 }) as any;
    if (res.code === 200) {
      message.success('恢复成功');
      fetchData();
    } else {
      message.error(res.msg || '恢复失败');
    }
  } catch (error) {
    message.error('请求失败');
  }
};

const goBack = () => {
  router.push({ name: 'usermanager' });
};

</script>

<style scoped lang="scss">
.user-manage-container {
  padding: 24px;
}

.sub-text {
  color: #999;
  font-size: 13px;
  margin-left: 8px;
}

.ml-2 {
  margin-left: 8px;
}

.text-success {
  color: #52c41a;
  &:hover {
    color: #73d13d;
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

@media screen and (max-width: 768px) {
  .list-content {
    display: none;
  }
}
</style>
