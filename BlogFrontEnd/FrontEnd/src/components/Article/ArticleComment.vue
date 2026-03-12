<template>
  <section class="comments-section">

    <div class="comment-input-wrapper">
      <div class="comment-avatar" :class="{ guest: !isLoggedIn }">
        <img
          v-if="shouldShowAvatar(currentUserAvatar, currentUserAvatarKey)"
          :src="getOptimizedImageUrl(currentUserAvatar, 'sm')"
          :srcset="buildImageSrcSet(currentUserAvatar)"
          sizes="42px"
          alt="当前用户头像"
          @error="handleAvatarError($event, currentUserAvatar, currentUserAvatarKey)"
        />
        <span v-else>{{ currentUserInitial }}</span>
      </div>
      <div class="comment-input-area">
        <div v-if="!isLoggedIn" class="comment-login-tip">
          登录后可发表评论和回复，点击输入框会自动唤起登录弹窗。
        </div>

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
          :placeholder="textareaPlaceholder" rows="3" :maxlength="MAX_COMMENT_LENGTH" :readonly="!isLoggedIn"
          @focus="handleTextareaFocus" @keydown.enter.ctrl.prevent="submitComment"></textarea>

        <div class="comment-actions">
          <span class="comment-hint">
            {{ isLoggedIn ? `${ currentCommentLength }/${ MAX_COMMENT_LENGTH } · Ctrl + Enter 快速发送` : '登录后即可参与讨论' }}
          </span>
          <button type="button" class="comment-submit-btn"
            :disabled="isSubmitting || (isLoggedIn && !newComment.trim())"
            @click="submitComment">
            {{ isLoggedIn ? (isSubmitting ? '发送中...' : '发表评论') : '登录后评论' }}
          </button>
        </div>
      </div>
    </div>

    <div class="comments-list">
      <div v-for="comment in rootComments" :key="comment.id" class="comment-group">

        <div class="comment-item root-comment">
          <div class="comment-main">
            <div class="comment-avatar">
              <img
                v-if="shouldShowAvatar(comment.userAvatar, getAvatarKey('comment', comment.id))"
                :src="getOptimizedImageUrl(comment.userAvatar, 'sm')"
                :srcset="buildImageSrcSet(comment.userAvatar)"
                sizes="42px"
                :alt="`${resolveDisplayName(comment)}头像`"
                @error="handleAvatarError($event, comment.userAvatar, getAvatarKey('comment', comment.id))"
              />
              <span v-else>{{ getCommentInitial(comment) }}</span>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-author">{{ resolveDisplayName(comment) }}</span>
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
                  <span>{{ getRepliesButtonText(comment) }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <transition name="slide-fade">
          <div v-if="comment.showReplies === 2" class="replies-wrapper">
            <div v-if="comment.children && comment.children.length > 0" class="replies-list">
              <div v-for="reply in comment.children" :key="reply.id" class="comment-item reply-item">
                <div class="comment-avatar small">
                  <img
                    v-if="shouldShowAvatar(reply.userAvatar, getAvatarKey('reply', reply.id))"
                    :src="getOptimizedImageUrl(reply.userAvatar, 'sm')"
                    :srcset="buildImageSrcSet(reply.userAvatar)"
                    sizes="32px"
                    :alt="`${resolveDisplayName(reply)}头像`"
                    @error="handleAvatarError($event, reply.userAvatar, getAvatarKey('reply', reply.id))"
                  />
                  <span v-else>{{ getCommentInitial(reply) }}</span>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ resolveDisplayName(reply) }}</span>
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
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="replies-empty">暂无更多回复</div>
          </div>
        </transition>
      </div>

      <div v-if="rootComments.length === 0" class="comments-empty">
        还没有评论，来留下第一条想法吧。
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useThemStore } from '@/stores/them'
import { useUserStore } from '@/stores/user'
import { getRootComment, getChildComment, publishComment } from '@/api/comment'
import type { CommentDTO, CommentVO } from '@/type/Interface'
import { frontAuthorization } from '@/utils/auth'
import { buildImageSrcSet, fallbackToOriginalImage, getOptimizedImageUrl } from '@/utils/image'
import { formatTime } from '@/utils/tools'

const props = defineProps({
  articleId: { type: String, required: true }
})

const themStore = useThemStore()
const appStore = useAppStore()
const userStore = useUserStore()
const MAX_COMMENT_LENGTH = 500

// --- 基础状态 ---
const newComment = ref('')
const rootComments = ref<CommentVO[]>([])
const isSubmitting = ref(false)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const avatarFallbackMap = ref<Record<string, boolean>>({})
const currentUserAvatarKey = 'current-user'

// --- 回复状态标记 ---
const replyTarget = ref<{ comment: CommentVO, rootComment?: CommentVO } | null>(null)
const isLoggedIn = computed(() => Boolean(frontAuthorization.value))
const currentCommentLength = computed(() => newComment.value.trim().length)
const currentUserAvatar = computed(() => userStore.userInfo?.avatar || '')
const currentUserInitial = computed(() => {
  if (!isLoggedIn.value) {
    return '登'
  }
  const displayName = userStore.userInfo?.nickname || userStore.userInfo?.username || '我'
  return displayName.charAt(0)
})
const textareaPlaceholder = computed(() => {
  if (!isLoggedIn.value) {
    return '登录后写下你的想法，参与讨论吧...'
  }
  return replyTarget.value ? '请文明发言，给予友善的回复...' : '写下你的想法，参与讨论吧...'
})

// --- 计算属性：总评论数 (根评论 + 所有子评论) ---
const totalComments = computed(() => {
  return rootComments.value.reduce((acc, cur) => acc + 1 + (cur.childCount || 0), 0)
})

const openLoginForComment = (messageText: string) => {
  appStore.isShowLoginPage = true
}

const getAvatarKey = (prefix: string, id?: string | number) => `${prefix}-${id ?? 'unknown'}`

const markAvatarError = (key: string) => {
  avatarFallbackMap.value = { ...avatarFallbackMap.value, [key]: true }
}

const handleAvatarError = (event: Event, originalUrl: string | undefined, key: string) => {
  const target = event.target as HTMLImageElement | null
  if (avatarFallbackMap.value[key]) {
    return
  }
  if (target?.dataset.originalFallbackApplied === 'true') {
    markAvatarError(key)
    return
  }
  fallbackToOriginalImage(event, originalUrl)
}

const shouldShowAvatar = (avatar?: string, key?: string) => {
  if (!avatar || !avatar.trim()) {
    return false
  }
  return key ? !avatarFallbackMap.value[key] : true
}

const resolveDisplayName = (comment?: Pick<CommentVO, 'userName'> | null) => {
  return comment?.userName?.trim() || '匿名用户'
}

const getCommentInitial = (comment?: Pick<CommentVO, 'userName'> | null) => {
  return resolveDisplayName(comment).charAt(0)
}

const getRepliesButtonText = (comment: Pick<CommentVO, 'childCount' | 'showReplies'>) => {
  if (comment.showReplies === 2) {
    return '收起回复'
  }
  return comment.childCount ? `查看回复` : '查看回复'
}

// --- 业务逻辑 ---

// 1. 初始化获取根评论
const fetchRootComments = async () => {
  try {
    const res = await getRootComment(props.articleId)
    rootComments.value = (res.data.records || []).map((item: any) => ({
      ...item,
      childCount: Number(item.childCount || 0),
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
        childCount: Number(item.childCount || 0),
        showReplies: 1
      }))
    } catch (error) {
      console.error('获取子评论失败:', error)
      ElMessage.error('获取回复失败，请稍后重试')
      return
    }
  }
  comment.showReplies = 2
}

const handleTextareaFocus = () => {
  if (isLoggedIn.value) {
    return
  }
  textareaRef.value?.blur()
  openLoginForComment('登录后才可以发表评论')
}

// 3. 点击“回复”按钮
const handleReply = (comment: CommentVO, root?: CommentVO) => {
  if (!isLoggedIn.value) {
    openLoginForComment('登录后才可以回复评论')
    return
  }
  replyTarget.value = { comment, rootComment: root }
  textareaRef.value?.focus()
}

// 4. 取消回复状态 (点击×)
const cancelReply = () => {
  replyTarget.value = null
}

// 5. 提交评论/回复
const submitComment = async () => {
  if (!isLoggedIn.value) {
    openLoginForComment('登录后才可以发表评论')
    return
  }

  const content = newComment.value.trim()
  if (!content || isSubmitting.value) return
  if (content.length > MAX_COMMENT_LENGTH) {
    ElMessage.warning(`评论内容不能超过 ${MAX_COMMENT_LENGTH} 个字符`)
    return
  }

  isSubmitting.value = true
  try {
    const isReply = !!replyTarget.value
    const payload: CommentDTO = {
      articleId: props.articleId,
      content: content,
      rootId: isReply ? String(replyTarget.value?.rootComment?.id || replyTarget.value?.comment.id) : "-1",
      toUserId: isReply ? replyTarget.value?.comment.userId : undefined,
    }

    const res = await publishComment(payload) as any
    if (res.code !== 200) {
      return
    }

    const newCommentVO: CommentVO = {
      id: String(Date.now()),
      articleId: props.articleId,
      userId: userStore.userInfo?.id || '',
      userName: userStore.userInfo?.nickname || userStore.userInfo?.username || '我',
      userAvatar: userStore.userInfo?.avatar || '',
      content: payload.content,
      rootId: payload.rootId,
      toUserId: payload.toUserId,
      toUserName: isReply ? replyTarget.value?.comment.userName : undefined,
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
        rootItem.childCount = (rootItem.childCount || 0) + 1
        rootItem.showReplies = 2 // 发表回复后自动展开
      }
    } else {
      rootComments.value.unshift(newCommentVO)
    }

    ElMessage.success(isReply ? '回复成功' : '评论成功')
    newComment.value = ''
    cancelReply()
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  await fetchRootComments()
  if (isLoggedIn.value && !userStore.userInfo?.id) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('获取当前用户信息失败:', error)
    }
  }
})
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
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  span {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
  }

  &.guest {
    background: v-bind('themStore.them.cardBgHover');
    color: v-bind('themStore.them.textColor');
    border: 1px solid v-bind('themStore.them.cardBorder');
  }

  &.small {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }
}

.comment-input-area {
  flex: 1;
}

.comment-login-tip {
  margin-bottom: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: v-bind('themStore.them.cardBgHover');
  border: 1px dashed v-bind('themStore.them.cardBorder');
  color: v-bind('themStore.them.textSecondary');
  font-size: 0.85rem;
  line-height: 1.6;
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
  min-height: 108px;

  &:focus {
    outline: none;
    border-color: v-bind('themStore.them.accentColor');
  }

  &[readonly] {
    cursor: pointer;
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
  flex-wrap: wrap;
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

.replies-empty {
  padding: 10px 0;
  font-size: 0.85rem;
  color: v-bind('themStore.them.textTertiary');
  font-style: italic;
}

.comments-empty {
  padding: 18px 0 4px;
  color: v-bind('themStore.them.textTertiary');
  text-align: center;
  font-size: 0.9rem;
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

@media (max-width: 768px) {
  .comments-section {
    padding: 20px 16px;
  }

  .comment-input-wrapper,
  .comment-main,
  .reply-item {
    gap: 12px;
  }

  .comment-actions {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .comment-submit-btn {
    width: 100%;
  }

  .replies-wrapper {
    margin-left: 20px;
    padding-left: 12px;
  }

  .comment-header {
    gap: 4px 10px;
  }

  .comment-time {
    width: 100%;
    margin-left: 0;
  }
}
</style>
