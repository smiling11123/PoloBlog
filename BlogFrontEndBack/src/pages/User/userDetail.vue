<template>
  <div class="user-detail-container">
    <a-skeleton active v-if="loading" />

    <div v-else-if="userInfo">
      <a-card :bordered="false" class="header-card">
        <div class="user-header">
          <div class="left-info">
            <a-avatar :src="userInfo.avatar" :size="80" shape="square">
              {{ userInfo.nickname?.charAt(0) }}
            </a-avatar>
            <div class="base-info">
              <h2 class="nickname">
                {{ userInfo.nickname || userInfo.username }}
                <a-tag :color="userInfo.statusText === '正常' ? 'green' : 'default'">
                  {{ userInfo.statusText || '未知状态' }}
                </a-tag>
              </h2>
              <div class="username">账号: {{ userInfo.username }}</div>
              <div class="role-tag">
                <a-tag v-if="userInfo.roleName" color="blue">{{ userInfo.roleName }}</a-tag>
                <a-tag color="geekblue">{{ sourceLabel }}</a-tag>
              </div>
            </div>
          </div>
          
          <div class="right-actions">
            <a-button @click="router.back()">返回列表</a-button>
            <a-button type="primary" class="ml-2" @click="handleEdit">编辑资料</a-button>
          </div>
        </div>
      </a-card>

      <a-row :gutter="[16, 16]" class="quick-panels">
        <a-col :xs="24" :md="8">
          <a-card :bordered="false" class="quick-panel">
            <span class="panel-label">绑定邮箱</span>
            <strong>{{ userInfo.email || '未绑定邮箱' }}</strong>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card :bordered="false" class="quick-panel">
            <span class="panel-label">最近登录</span>
            <strong>{{ formatTime(userInfo.loginDate) }}</strong>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="8">
          <a-card :bordered="false" class="quick-panel">
            <span class="panel-label">注册时间</span>
            <strong>{{ formatTime(userInfo.createTime) }}</strong>
          </a-card>
        </a-col>
      </a-row>

      <a-card title="详细资料" :bordered="false" class="mt-4">
        <a-descriptions bordered :column="{ xxl: 3, xl: 3, lg: 2, md: 2, sm: 1, xs: 1 }">
          <a-descriptions-item label="用户 ID">{{ userInfo.id }}</a-descriptions-item>
          <a-descriptions-item label="绑定邮箱">{{ userInfo.email || '未绑定' }}</a-descriptions-item>
          <a-descriptions-item label="登录方式">{{ sourceLabel }}</a-descriptions-item>
          <a-descriptions-item label="账号状态">{{ userInfo.statusText || '未知状态' }}</a-descriptions-item>
          <a-descriptions-item label="最近登录">{{ formatTime(userInfo.loginDate) }}</a-descriptions-item>
          <a-descriptions-item label="最近更新">{{ formatTime(userInfo.updateTime) }}</a-descriptions-item>
          
          <a-descriptions-item label="个人简介" :span="3">
            {{ userInfo.intro || '暂无介绍' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>

    <a-empty v-else description="未找到该用户信息" style="margin-top: 100px;">
      <a-button type="primary" @click="router.back()">返回列表</a-button>
    </a-empty>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
// 引入 API
import { getUserDetail, type UserInfo } from '@/api/common/user';
import { formatTimer } from '~@/utils/tools';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const userInfo = ref<UserInfo | null>(null);

const sourceLabel = computed(() => {
  const source = userInfo.value?.source?.toLowerCase();
  if (!source) return '未记录';
  if (source === 'gitee') return 'Gitee';
  if (source === 'github') return 'GitHub';
  if (source === 'local') return '本地账号';
  return userInfo.value?.source || '未记录';
});

onMounted(async () => {
  const id = route.params.id as string;
  if (id) {
    await loadData(id);
  }
});

const formatTime = (value?: string) => value ? formatTimer(value) : '暂无记录';

const loadData = async (id: string) => {
  loading.value = true;
  try {
    const res = await getUserDetail(id) as any;
    if (res.code === 200) {
      userInfo.value = res.data;
    } else {
      message.error(res.msg || '获取详情失败');
    }
  } catch (error) {
    console.error(error);
    message.error('网络请求错误');
  } finally {
    loading.value = false;
  }
};

const handleEdit = () => {
  router.push({ name: 'UserEdit', params: { id: route.params.id } });
};
</script>

<style scoped lang="scss">
.user-detail-container {
  padding: 24px;
}

.header-card {
  .user-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left-info {
      display: flex;
      align-items: center;
      gap: 24px;

      .base-info {
        .nickname {
          margin: 0;
          font-size: 24px;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 12px;
        }
        .username {
          color: #999;
          margin-top: 4px;
        }
        .role-tag {
          margin-top: 8px;
        }
      }
    }
  }
}

.quick-panels {
  margin-top: 24px;
}

.quick-panel {
  min-height: 112px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .panel-label {
    color: rgba(255, 255, 255, 0.45);
    font-size: 12px;
    margin-bottom: 8px;
    display: inline-block;
  }

  strong {
    color: rgba(255, 255, 255, 0.88);
    font-size: 16px;
    line-height: 1.5;
  }
}

.mt-4 {
  margin-top: 24px;
}
.ml-2 {
  margin-left: 12px;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .user-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;
    
    .right-actions {
      width: 100%;
      display: flex;
      gap: 10px;
      button { flex: 1; }
    }
  }
}
</style>
