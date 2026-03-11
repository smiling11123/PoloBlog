<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getRootComment, getChildComment } from '@/api/Comment'
import { CommentVO } from 'types/Entity'
import { formatTimer } from '@/utils/tools'
import { MessageOutlined, UserOutlined } from '@ant-design/icons-vue'

const visible = ref(false)
const loading = ref(false)
const articleId = ref<string | null>(null)
const articleTitle = ref('')
const rootComments = ref<CommentVO[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  onChange: (page: number) => {
    pagination.current = page
    fetchRootComments()
  }
})

const open = (article: any) => {
  articleId.value = article.id
  articleTitle.value = article.title
  pagination.current = 1
  rootComments.value = []
  visible.value = true
  fetchRootComments()
}

const fetchRootComments = async () => {
  if (!articleId.value) return
  loading.value = true
  try {
    const res: any = await getRootComment({
      articleId: articleId.value,
      page: pagination.current,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      rootComments.value = res.data.records.map((c: any) => ({ ...c, _replies: [], _repliesLoading: false, _repliesVisible: false, _repliesPagination: { current: 1, total: 0 } }))
      pagination.total = res.data.total
    }
  } catch (error) {
    message.error('获取根评论失败')
  } finally {
    loading.value = false
  }
}

const toggleReplies = async (comment: any) => {
  if (!comment._repliesVisible) {
    if (comment._replies.length === 0) {
      await fetchChildComments(comment)
    }
    comment._repliesVisible = true
  } else {
    comment._repliesVisible = false
  }
}

const fetchChildComments = async (comment: any) => {
  comment._repliesLoading = true
  try {
    const res: any = await getChildComment({
      rootId: comment.id,
      page: comment._repliesPagination.current,
      size: 10
    })
    if (res.code === 200) {
      comment._replies = res.data.records
      comment._repliesPagination.total = res.data.total
    }
  } catch (error) {
    message.error('获取回复失败')
  } finally {
    comment._repliesLoading = false
  }
}

const handleReplyPageChange = (comment: any, page: number) => {
  comment._repliesPagination.current = page
  fetchChildComments(comment)
}

defineExpose({ open })
</script>

<template>
  <a-modal
    v-model:open="visible"
    :title="`评论管理 - ${articleTitle}`"
    width="800px"
    :footer="null"
  >
    <a-list
      :loading="loading"
      item-layout="vertical"
      :data-source="rootComments"
      :pagination="pagination"
    >
      <template #renderItem="{ item }">
        <a-list-item :key="item.id">
          <a-comment>
            <template #author>
              <a>{{ item.userName }}</a>
            </template>
            <template #avatar>
              <a-avatar>
                <template #icon><UserOutlined /></template>
              </a-avatar>
            </template>
            <template #content>
              <p>{{ item.content }}</p>
            </template>
            <template #datetime>
              <span>{{ formatTimer(item.createTime) }}</span>
            </template>
            <template #actions>
              <span v-if="item.childCount > 0" @click="toggleReplies(item)">
                <MessageOutlined />
                <span style="margin-left: 4px">{{ item._repliesVisible ? '收起回复' : `查看回复 (${item.childCount})` }}</span>
              </span>
            </template>

            <!-- Replies section -->
            <div v-if="item._repliesVisible" class="replies-container">
              <a-spin :spinning="item._repliesLoading">
                <a-list
                  item-layout="horizontal"
                  :data-source="item._replies"
                  size="small"
                >
                  <template #renderItem="{ item: reply }">
                    <a-list-item>
                      <a-comment>
                        <template #author>
                          <a>{{ reply.userName }}</a>
                          <span v-if="reply.toUserName" style="margin: 0 4px">回复</span>
                          <a v-if="reply.toUserName">{{ reply.toUserName }}</a>
                        </template>
                        <template #avatar>
                          <a-avatar size="small">
                            <template #icon><UserOutlined /></template>
                          </a-avatar>
                        </template>
                        <template #content>
                          <p>{{ reply.content }}</p>
                        </template>
                        <template #datetime>
                          <span>{{ formatTimer(reply.createTime) }}</span>
                        </template>
                      </a-comment>
                    </a-list-item>
                  </template>
                </a-list>
                <div v-if="item._repliesPagination.total > 10" class="reply-pagination">
                  <a-pagination
                    size="small"
                    :current="item._repliesPagination.current"
                    :total="item._repliesPagination.total"
                    :page-size="10"
                    @change="(page: number) => handleReplyPageChange(item, page)"
                  />
                </div>
              </a-spin>
            </div>
          </a-comment>
        </a-list-item>
      </template>
    </a-list>
  </a-modal>
</template>

<style scoped lang="less">
.replies-container {
  margin-left: 44px;
  padding: 12px;
  background-color: #fafafa;
  border-radius: 4px;
  margin-top: 8px;
}
.reply-pagination {
  margin-top: 8px;
  text-align: right;
}
</style>
