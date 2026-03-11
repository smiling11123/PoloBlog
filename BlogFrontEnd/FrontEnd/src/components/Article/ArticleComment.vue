<template>
  <section class="comments-section">
    <h3 class="comments-title">
      <span class="comments-count">{{ totalComments }}</span> 条评论
    </h3>

    <div class="comment-input-wrapper">
      <div class="comment-avatar">我</div>
      <div class="comment-input-area">

        <transition name="fade">
          <div v-if="replyTarget" class="reply-target-bar">
            <span class="reply-hint">回复给：</span>
            <div class="reply-tag">
              <span class="tag-name">@{{ replyTarget.comment.userName }}</span>
              <button class="tag-close" title="取消回复" @click="cancelReply">×</button>
            </div>
          </div>
        </transition>

        <textarea ref="textareaRef" v-model="newComment" class="comment-textarea"
          :placeholder="replyTarget ? '请文明发言，给予友善的回复...' : '写下你的想法，参与讨论吧...'" rows="3"
          @keydown.enter.ctrl.prevent="submitComment"></textarea>

        <div class="comment-actions">
          <span class="comment-hint">Ctrl + Enter 快速发送</span>
          <button type="button" class="comment-submit-btn" :disabled="!newComment.trim() || isSubmitting"
            @click="submitComment">
            {{ isSubmitting ? '发送中...' : '发表评论' }}
          </button>
        </div>
      </div>
    </div>

    <div class="comments-list">
      <div v-for="comment in rootComments" :key="comment.id" class="comment-group">

        <div class="comment-item root-comment">
          <div class="comment-main">
            <div class="comment-avatar" :style="{ background: themStore.them.accentColor }">
              {{ comment.userName?.[0] || '匿' }}
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ comment.userName || '匿名用户' }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>

              <div class="comment-footer">

                <button type="button" class="action-btn reply-btn" @click="handleReply(comment)">
                  回复
                </button>

                <button type="button" class="view-replies-btn" @click="toggleReplies(comment)">
                  <span class="arrow-icon" :class="{ expanded: comment.showReplies === 2 }">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path fill="currentColor" d="M7 10l5 5 5-5z" />
                    </svg>
                  </span>
                  <span>{{ comment.showReplies === 2 ? '收起回复' : '查看回复' }}</span>

                </button>
              </div>
            </div>
          </div>
        </div>

        <transition name="slide-fade">
          <div v-if="comment.showReplies === 2" class="replies-wrapper">
            <div v-if="comment.children && comment.children.length > 0" class="replies-list">
              <div v-for="reply in comment.children" :key="reply.id" class="comment-item reply-item">
                <div class="comment-avatar small" :style="{ background: themStore.them.accentColor }">
                  {{ reply.userName?.[0] || '匿' }}
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ reply.userName || '匿名用户' }}</span>
                    <span v-if="reply.toUserName" class="reply-info">
                      回复 <span class="at-target">@{{ reply.toUserName }}</span>
                    </span>
                    <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                  <p class="comment-text">{{ reply.content }}</p>
                  <div class="comment-footer">

                    <button type="button" class="action-btn reply-btn" @click="handleReply(reply, comment)">
                      回复
                    </button>
                    <!-- 子评论的"查看回复"按钮，不缩进 -->
                    <button type="button" class="view-replies-btn" @click="toggleChildReplies(reply)">
                      <span class="arrow-icon" :class="{ expanded: reply.showReplies === 2 }">
                        <svg viewBox="0 0 24 24" width="14" height="14">
                          <path fill="currentColor" d="M7 10l5 5 5-5z" />
                        </svg>
                      </span>
                      <span>{{ reply.showReplies === 2 ? '收起回复' : '查看回复' }}</span>

                    </button>
                  </div>
                  <!-- 子评论的子回复列表（无额外缩进） -->
                  <transition name="slide-fade">
                    <div v-if="reply.showReplies === 2" class="sub-replies-list">
                      <div v-if="reply.children && reply.children.length > 0" class="replies-list">
                        <div v-for="subReply in (reply.children || [])" :key="subReply.id"
                          class="comment-item reply-item">
                          <div class="comment-avatar small" :style="{ background: themStore.them.accentColor }">
                            {{ subReply.userName?.[0] || '匿' }}
                          </div>
                          <div class="comment-content">
                            <div class="comment-header">
                              <span class="comment-author">{{ subReply.userName || '匿名用户' }}</span>
                              <span v-if="subReply.toUserName" class="reply-info">
                                回复 <span class="at-target">@{{ subReply.toUserName }}</span>
                              </span>
                              <span class="comment-time">{{ formatTime(subReply.createTime) }}</span>
                            </div>
                            <p class="comment-text">{{ subReply.content }}</p>
                            <div class="comment-footer">

                              <button type="button" class="action-btn reply-btn"
                                @click="handleReply(subReply, comment)">
                                回复
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div v-else class="replies-empty">暂无更多回复</div>
                    </div>
                  </transition>
                </div>
              </div>
            </div>
            <div v-else class="replies-empty">暂无更多回复</div>
          </div>
        </transition>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useThemStore } from '@/stores/them'
import { useUserStore } from '@/stores/user'
import { getRootComment, getChildComment, publishComment } from '@/api/comment'
import type { CommentVO } from '@/type/Interface'
import { formatTime } from '@/utils/tools'

const props = defineProps({
  articleId: { type: String, required: true }
})

const themStore = useThemStore()
const userStore = useUserStore()

// --- 基础状态 ---
const newComment = ref('')
const userAvatar = ref('我')
const rootComments = ref<CommentVO[]>([])
const isSubmitting = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

// --- 回复状态标记 ---
const replyTarget = ref<{ comment: CommentVO, rootComment?: CommentVO } | null>(null)

// --- 计算属性：总评论数 (根评论 + 所有子评论) ---
const totalComments = computed(() => {
  return rootComments.value.reduce((acc, cur) => acc + 1 + (cur.childCount || 0), 0)
})

// --- 业务逻辑 ---

// 1. 初始化获取根评论
const fetchRootComments = async () => {
  try {
    const res = await getRootComment(props.articleId)
    rootComments.value = (res.data.records || []).map((item: any) => ({
      ...item,
      children: [],
      showReplies: 1 // 1:收起, 2:展开
    }))
  } catch (error) {
    console.error('获取评论列表失败:', error)
  }
}

// 2. 展开/收起回复列表
const toggleReplies = async (comment: CommentVO) => {
  if (comment.showReplies === 2) {
    comment.showReplies = 1
    return
  }

  // 展开时判断是否需要加载数据
  if (!comment.children || comment.children.length === 0) {
    try {
      const res = await getChildComment(comment.id)
      comment.children = (res.data.records || []).map((item: any) => ({
        ...item,
        children: [],
        showReplies: 1
      }))
    } catch (error) {
      console.error('获取子评论失败:', error)
      return
    }
  }
  comment.showReplies = 2
}

// 2b. 展开/收起子评论的二级回复列表
const toggleChildReplies = async (reply: CommentVO) => {
  if (reply.showReplies === 2) {
    reply.showReplies = 1
    return
  }
  if (!reply.children || reply.children.length === 0) {
    try {
      const res = await getChildComment(reply.id)
      reply.children = (res.data.records || []).map((item: any) => ({
        ...item,
        children: [],
        showReplies: 1
      }))
    } catch (error) {
      console.error('获取子评论的子回复失败:', error)
      return
    }
  }
  reply.showReplies = 2
}

// 3. 点击“回复”按钮
const handleReply = (comment: CommentVO, root?: CommentVO) => {
  replyTarget.value = { comment, rootComment: root }
  textareaRef.value?.focus()
}

// 4. 取消回复状态 (点击×)
const cancelReply = () => {
  replyTarget.value = null
}

// 5. 提交评论/回复
const submitComment = async () => {
  const content = newComment.value.trim()
  if (!content || isSubmitting.value) return

  isSubmitting.value = true
  try {
    const isReply = !!replyTarget.value
    // 构造发送给后端的 DTO
    const payload = {
      articleId: props.articleId,
      content: content,
      userId: userStore.userInfo?.id || '',
      userName: userStore.userInfo?.nickname || '匿名用户',
      // 如果是回复，计算 rootId；否则为 -1
      rootId: isReply ? (replyTarget.value?.rootComment?.id || replyTarget.value?.comment.id) : "-1",
      toUserId: isReply ? replyTarget.value?.comment.userId : null,
      toUserName: isReply ? replyTarget.value?.comment.userName : null
    }

    const res = await publishComment(payload as any)

    // 构造本地预览对象 (CommentVO)
    const newCommentVO: CommentVO = {
      ...payload,
      id: res.data?.id || Date.now().toString(),
      createTime: new Date().toISOString(),
      likeCount: 0,
      isLiked: false,
      childCount: 0,
      children: [],
      showReplies: 1
    } as any

    // 即时更新 UI
    if (isReply) {
      const rootItem = rootComments.value.find(c => c.id === payload.rootId)
      if (rootItem) {
        rootItem.children = rootItem.children || []
        rootItem.children.unshift(newCommentVO)
        rootItem.childCount++
        rootItem.showReplies = 2 // 发表回复后自动展开
      }
    } else {
      rootComments.value.unshift(newCommentVO)
    }

    // 重置状态
    newComment.value = ''
    cancelReply()
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

// 6. 点赞切换
const toggleLike = (item: CommentVO) => {
  item.isLiked = !item.isLiked
  item.likeCount += item.isLiked ? 1 : -1
}

onMounted(fetchRootComments)
</script>

<style scoped lang="scss">
/* --- 布局容器 --- */
.comments-section {
  max-width: 900px;
  margin: 20px auto;
  padding: 30px;
  background: v-bind('themStore.them.cardBg');
  border: 1px solid v-bind('themStore.them.cardBorder');
  border-radius: 16px;
  transition: all 0.3s;
}

.comments-title {
  font-size: 1.25rem;
  font-weight: bold;
  color: v-bind('themStore.them.textColor');
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 10px;

  .comments-count {
    background: v-bind('themStore.them.accentColor');
    color: #fff;
    padding: 2px 10px;
    border-radius: 20px;
    font-size: 0.9rem;
  }
}

/* --- 输入框区域 --- */
.comment-input-wrapper {
  display: flex;
  gap: 15px;
  margin-bottom: 40px;
}

.comment-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: v-bind('themStore.them.accentColor');
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;

  &.small {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }
}

.comment-input-area {
  flex: 1;
}

/* 回复标签样式 */
.reply-target-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-hint {
  font-size: 0.85rem;
  color: v-bind('themStore.them.textTertiary');
}

.reply-tag {
  display: inline-flex;
  align-items: center;
  background: v-bind('themStore.them.accentColor');
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.85rem;
  gap: 5px;

  .tag-close {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;

    &:hover {
      background: rgba(255, 255, 255, 0.4);
    }
  }
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid v-bind('themStore.them.cardBorder');
  border-radius: 10px;
  background: v-bind('themStore.them.cardBgHover');
  color: v-bind('themStore.them.textColor');
  font-family: inherit;
  resize: vertical;

  &:focus {
    outline: none;
    border-color: v-bind('themStore.them.accentColor');
  }
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;

  .comment-hint {
    font-size: 0.75rem;
    color: v-bind('themStore.them.textTertiary');
  }

  .comment-submit-btn {
    background: v-bind('themStore.them.accentColor');
    color: #fff;
    border: none;
    padding: 8px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

/* --- 评论列表样式 --- */
.comment-group {
  margin-bottom: 25px;
  border-bottom: 1px solid v-bind('themStore.them.cardBorder');
  padding-bottom: 20px;

  &:last-child {
    border-bottom: none;
  }
}

.comment-main {
  display: flex;
  gap: 15px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-author {
  font-weight: 600;
  color: v-bind('themStore.them.textColor');
}

.reply-info {
  font-size: 0.85rem;
  color: v-bind('themStore.them.textColor');

  .at-target {
    color: v-bind('themStore.them.textColor');
    font-weight: 500;
  }
}

.comment-time {
  font-size: 0.75rem;
  color: v-bind('themStore.them.textTertiary');
  margin-left: auto;
}

.comment-text {
  font-size: 0.95rem;
  line-height: 1.6;
  color: v-bind('themStore.them.textSecondary');
  margin: 8px 0;
}

/* --- 底部操作栏 --- */
.comment-footer {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  color: v-bind('themStore.them.textTertiary');
  display: flex;
  align-items: center;
  gap: 4px;
  transition: 0.2s;

  &:hover {
    color: v-bind('themStore.them.textColor');
  }

  &.liked {
    color: #e74c3c;

    .icon {
      transform: scale(1.1);
    }
  }
}

.view-replies-btn {
  margin-left: auto;
  background: none;
  border: none;
  color: v-bind('themStore.them.textColor');
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;

  &:hover {
    background: v-bind('themStore.them.cardBgHover');
  }

  .arrow-icon {
    display: flex;
    transition: transform 0.3s;

    &.expanded {
      transform: rotate(180deg);
    }
  }

  .child-num {
    font-weight: 600;
  }
}

/* --- 子评论容器 --- */
.replies-wrapper {
  margin-left: 57px;
  padding-left: 15px;
  border-left: 2px solid v-bind('themStore.them.cardBorder');
  margin-top: 15px;
}

.reply-item {
  margin-top: 15px;
  display: flex;
  gap: 12px;
}

/* 子评论的子回复列表（无额外缩进） */
.sub-replies-list {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed v-bind('themStore.them.cardBorder');
}

.replies-empty {
  padding: 10px 0;
  font-size: 0.85rem;
  color: v-bind('themStore.them.textTertiary');
  font-style: italic;
}

/* --- 动画 --- */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
  max-height: 2000px;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
