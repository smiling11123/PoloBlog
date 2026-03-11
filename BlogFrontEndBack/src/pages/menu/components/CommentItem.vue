<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getChildComment } from '@/api/Comment'
import { formatTimer } from '@/utils/tools'
import { MessageOutlined, UserOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  comment: any
  depth: number
}>()

const replies = ref<any[]>([])
const loading = ref(false)
const visible = ref(false)
const pagination = reactive({
  current: 1,
  total: 0
})

const toggleReplies = async () => {
  if (!visible.value) {
    if (replies.value.length === 0) {
      await fetchReplies()
    }
    visible.value = true
  } else {
    visible.value = false
  }
}

const fetchReplies = async () => {
  loading.value = true
  try {
    const res: any = await getChildComment({
      rootId: String(props.comment.id),
      page: pagination.current,
      size: 10
    })
    if (res.code === 200) {
      replies.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    message.error('获取回复失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
  fetchReplies()
}
</script>

<template>
  <div class="comment-item-container">
    <a-comment>
      <template #author>
        <span :class="depth === 0 ? 'root-user' : 'reply-user'">{{ comment.userName }}</span>
        <span v-if="comment.toUserName" class="reply-to">回复</span>
        <span v-if="comment.toUserName" class="reply-target">@{{ comment.toUserName }}</span>
      </template>
      <template #avatar>
        <a-avatar :size="depth === 0 ? 'default' : 'small'" :style="{ backgroundColor: depth === 0 ? '#1890ff' : '#87d068' }">
          <template #icon><UserOutlined /></template>
        </a-avatar>
      </template>
      <template #content>
        <div :class="depth === 0 ? 'root-comment-text' : 'reply-text-modern'">
          {{ comment.content }}
        </div>
      </template>
      <template #datetime>
        <span class="comment-time">{{ formatTimer(comment.createTime) }}</span>
      </template>
      <template #actions>
        <span class="action-trigger" @click="toggleReplies">
          <MessageOutlined />
          <span style="margin-left: 4px">{{ visible ? '收起回复' : '查看回复' }}</span>
        </span>
      </template>

      <!-- Nested Replies -->
      <transition name="expand">
        <div v-if="visible" class="replies-wrapper" :style="{ marginLeft: depth === 0 ? '24px' : '0' }">
          <div v-if="depth === 0" class="replies-connector"></div>
          <div :class="['replies-content-box', depth > 0 ? 'nested-box' : '']">
            <a-spin :spinning="loading">
              <div v-if="replies.length > 0">
                <CommentItem 
                  v-for="reply in replies" 
                  :key="reply.id" 
                  :comment="reply" 
                  :depth="depth + 1"
                />
              </div>
              
              <div v-if="pagination.total > 10" class="reply-pagination-box">
                <a-pagination
                  size="small"
                  :current="pagination.current"
                  :total="pagination.total"
                  :page-size="10"
                  simple
                  @change="handlePageChange"
                />
              </div>
              
              <div v-if="replies.length === 0 && !loading" class="empty-replies">
                暂无回复
              </div>
            </a-spin>
          </div>
        </div>
      </transition>
    </a-comment>
  </div>
</template>

<style lang="less">
/* 
 * Note: NOT using `scoped` here so we can properly target ant-design-vue 
 * a-comment internal elements. We namespace all rules under .comment-item-container.
 */
.comment-item-container {
  /* Override Ant Design comment author link hover color */
  .ant-comment-content-author-name {
    > * {
      color: inherit !important;
    }
    a, span {
      &:hover {
        color: inherit !important;
      }
    }
  }
  
  /* Override Ant Design comment action item hover */
  .ant-comment-actions > li > span {
    color: #595959;
    &:hover {
      color: #595959;
    }
  }
  
  .root-user {
    font-weight: bold;
    color: #1890ff;
    font-size: 15px;
    &:hover {
      color: #40a9ff !important;
      text-decoration: none;
    }
  }

  .reply-user {
    font-weight: 600;
    color: #333;
    font-size: 14px;
    &:hover {
      color: #1890ff !important;
      text-decoration: none;
    }
  }

  .reply-to {
    margin: 0 8px;
    color: #8c8c8c;
    font-size: 12px;
  }

  .reply-target {
    color: #1890ff;
    font-weight: 500;
  }

  .root-comment-text {
    font-size: 14px;
    line-height: 1.6;
    color: #333;
    padding: 10px 14px;
    background-color: #f9f9f9;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    margin-top: 4px;
  }

  .reply-text-modern {
    color: #595959;
    margin-top: 4px;
    font-size: 13.5px;
  }

  .comment-time {
    font-size: 12px;
    color: #bfbfbf;
  }

  /* Pill-style button that is always visible */
  .action-trigger {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    color: #1890ff;
    border: 1px solid #1890ff;
    background-color: transparent;
    cursor: pointer;
    transition: all 0.25s ease;
    user-select: none;

    &:hover {
      color: #fff !important;
      background-color: #1890ff;
    }
  }

  .replies-wrapper {
    position: relative;
    margin-top: 12px;
    display: flex;
  }

  .replies-connector {
    width: 2px;
    background: linear-gradient(to bottom, #1890ff 0%, #f0f0f0 100%);
    margin-right: 16px;
    border-radius: 2px;
    opacity: 0.4;
    flex-shrink: 0;
  }

  .replies-content-box {
    flex: 1;
    padding: 8px 16px;
    background-color: #fcfcfc;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    box-shadow: inset 0 2px 4px rgba(0,0,0,0.01);
  }

  .nested-box {
    border: none;
    background-color: transparent;
    box-shadow: none;
    padding: 0;
    border-top: 1px dashed #eee;
    border-radius: 0;
    margin-top: 8px;
  }

  .reply-pagination-box {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .empty-replies {
    text-align: center;
    color: #bfbfbf;
    padding: 10px 0;
    font-size: 13px;
  }

  .expand-enter-active, .expand-leave-active {
    transition: all 0.3s ease;
    overflow: hidden;
  }
  .expand-enter-from, .expand-leave-to {
    opacity: 0;
    transform: translateY(-5px);
  }
}
</style>
