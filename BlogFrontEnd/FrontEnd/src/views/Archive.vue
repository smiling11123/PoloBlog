<template>
    <div class="archive-page">
        <!-- 头部区域 -->
        <header class="page-header" :style="headerStyle">
            <div class="header-content">
                <h1 class="page-title" :style="{ color: them.headerTextColor, textShadow: them.headerTextShadow }">
                    文章归档
                </h1>
                <p class="page-subtitle" :style="{ color: them.textColor }">
                    共 <strong>{{ totalArticles }}</strong> 篇，记录成长的每一步
                </p>
            </div>
        </header>

        <!-- 时间轴内容区 -->
        <main class="timeline-container" ref="containerRef" :style="{ background: them.contentBgSolid }">
            <TransitionGroup name="month-fade" tag="div" class="timeline-content">
                <article v-for="month in sortedMonths" :key="month" class="month-group">
                    <!-- 月份标题 -->
                    <header class="month-header">
                        <div class="month-badge">
                            <time class="year" :style="{ color: them.textColor }">{{ getYear(month) }}</time>
                            <span class="month" :style="{ color: them.textColor }">{{ getMonth(month) }}月</span>
                        </div>
                        <span class="count" :style="{ color: them.textColor }">
                            {{ archiveData.get(month)?.length ?? 0 }} 篇
                        </span>
                    </header>

                    <!-- 文章列表 -->
                    <div class="timeline-items" role="list">
                        <div class="timeline-line"
                            :style="{ background: `linear-gradient(to bottom, ${them.accentColor}, ${them.textTertiary})` }"
                            aria-hidden="true"></div>

                        <div v-for="item in (archiveData.get(month) ?? [])" :key="item.id" class="item"
                            :class="{ 'is-top': item.isTop, 'is-hovered': hoveredItem === item.id }"
                            @mouseenter="hoveredItem = item.id" @mouseleave="hoveredItem = null" role="listitem"
                            @click="gotoDetail(item.id, item.isComment)">
                            <!-- 时间轴节点 -->
                            <div class="dot-wrapper" aria-hidden="true">
                                <div class="dot" :class="{ 'pinned': item.isTop }" :style="getDotStyle(item.isTop)">
                                    <span v-if="item.isTop" class="pin-icon" aria-label="置顶">📌</span>
                                </div>
                            </div>

                            <!-- 卡片内容 -->
                            <div class="content-wrapper" :style="getCardBackground(item.thumbnail)">
                                <div class="content-overlay"></div>

                                <div class="content-inner">
                                    <!-- 元信息 -->
                                    <div class="meta-row">
                                        <time class="date" :datetime="item.createTime">
                                            {{ formatDay(item.createTime) }}
                                        </time>
                                        <span v-if="item.categoryName" class="category-tag">
                                            {{ item.categoryName }}
                                        </span>
                                    </div>

                                    <!-- 标题链接 -->
                                    <h2 class="title-wrapper">
                                        <router-link :to="{ path: `/article/${item.id}`, query: { isComment: item.isComment ?? 1 } }" class="title-link"
                                            @mouseenter="prefetchArticle(item.id)" @focus="prefetchArticle(item.id)">
                                            {{ item.title }}
                                            <span v-if="item.isTop" class="top-badge">置顶</span>
                                        </router-link>
                                    </h2>

                                    <!-- 摘要 -->
                                    <p v-if="item.summary" class="summary">{{ item.summary }}</p>

                                    <!-- 统计信息 -->
                                    <div class="stats">
                                        <span class="stat" :title="`浏览量 ${item.viewCount}`">
                                            <span class="icon" aria-hidden="true">👁</span>
                                            <span class="stat-text">{{ formatNumber(item.viewCount) }}</span>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </article>
            </TransitionGroup>

            <!-- 加载触发器 -->
            <div ref="sentinel" class="sentinel" :class="{ 'loading': loading }" role="status" :aria-busy="loading">
                <div v-if="loading" class="loader">
                    <div class="spinner" :style="{
                        borderColor: `${them.accentColor}33`,
                        borderTopColor: them.accentColor
                    }"></div>
                    <span :style="{ color: them.textTertiary }">加载更多精彩...</span>
                </div>
                <p v-else-if="!hasMore && totalArticles > 0" class="no-more">
                    已经到底啦～
                </p>
            </div>
        </main>

        <!-- 返回顶部 -->
        <Transition name="fade">
            <button v-show="showBackToTop" class="back-to-top" @click="scrollToTop"
                :style="{ background: them.accentColor }" aria-label="返回顶部">
                <svg viewBox="0 0 24 24" width="20" height="20">
                    <path fill="currentColor" d="M12 4l-8 8h5v8h6v-8h5z" />
                </svg>
            </button>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import {
    ref,
    onMounted,
    onUnmounted,
    computed,
    nextTick,
    watch,
    onActivated,
    onDeactivated
} from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useThemStore } from '@/stores/them';
import type { articleVO } from '@/type/Interface';
import { getArticleList } from '@/api/article';

// Store
const themStore = useThemStore();
const { them } = storeToRefs(themStore);
const router = useRouter();

// 状态
const archiveData = ref<Map<string, articleVO[]>>(new Map());
const page = ref(1);
const loading = ref(false);
const hasMore = ref(true);
const hoveredItem = ref<string | number | null>(null);
const showBackToTop = ref(false);
const sentinel = ref<HTMLElement | null>(null);
const containerRef = ref<HTMLElement | null>(null);

// 观察者引用（用于清理）
let observer: IntersectionObserver | null = null;
let scrollHandler: (() => void) | null = null;

// 计算属性
const headerStyle = computed(() => ({
    background: them.value.contentBgTrans,
    backdropFilter: 'blur(10px)',
    WebkitBackdropFilter: 'blur(10px)', // Safari 兼容
    borderBottom: `1px solid ${them.value.cardBorder}`
}));

const totalArticles = computed(() =>
    Array.from(archiveData.value.values())
        .reduce((sum, arr) => sum + arr.length, 0)
);

// 按时间倒序排列的月份
const sortedMonths = computed(() =>
    Array.from(archiveData.value.keys())
        .sort((a, b) => {
            const parseMonth = (str: string) => {
                const match = str.match(/(\d{4})年(\d{1,2})月/);
                return match ? parseInt(match[1] as any) * 12 + parseInt(match[2] as any) : 0;
            };
            return parseMonth(b) - parseMonth(a);
        })
);

// 工具函数
const getYear = (monthStr: string) => monthStr.match(/(\d{4})年/)?.[1] ?? '';
const getMonth = (monthStr: string) => monthStr.match(/(\d{1,2})月/)?.[1] ?? '';

const formatNumber = (n: number): string => {
    if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';
    if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
    return n.toString();
};

const formatDay = (time: string): string => {
    const date = new Date(time);
    return `${date.getMonth() + 1}月${date.getDate()}日`;
};

const getDotStyle = (isTop?: number) => ({
    backgroundColor: isTop ? '#ffc107' : them.value.accentColor,
    boxShadow: isTop ? '0 0 0 4px rgba(255, 193, 7, 0.3)' : 'none'
});

// 优化背景图加载：使用 CSS 变量避免重排
const getCardBackground = (thumbnail?: string) => {
    if (!thumbnail) return {};
    return {
        '--bg-image': `url('${thumbnail}')`
    } as Record<string, string>;
};

// 数据加载
const loadData = async () => {
    if (loading.value || !hasMore.value) return;

    loading.value = true;
    try {
        const { data } = await getArticleList({
            page: page.value,
            size: 20,
            sort: 'createTime,desc'
        });

        const records: articleVO[] = data?.records || [];

        if (records.length === 0) {
            hasMore.value = false;
            return;
        }

        // 使用 Map 保证响应式且避免重复 key
        const newData = new Map(archiveData.value);

        records.forEach(item => {
            const date = new Date(item.createTime);
            const key = `${date.getFullYear()}年${date.getMonth() + 1}月`;

            if (!newData.has(key)) {
                newData.set(key, []);
            }
            const list = newData.get(key)!;

            // 去重检查
            if (!list.find(existing => existing.id === item.id)) {
                list.push(item);
            }
        });

        archiveData.value = newData;
        page.value++;
    } catch (error) {
        console.error('加载归档数据失败:', error);
    } finally {
        loading.value = false;
    }
};

// 预加载文章（防抖）
let prefetchTimer: ReturnType<typeof setTimeout> | null = null;
const prefetchArticle = (id: string) => {
    if (prefetchTimer) clearTimeout(prefetchTimer);
    prefetchTimer = setTimeout(() => {
        // 实现预加载逻辑
        router.resolve(`/article/${id}`);
    }, 100);
};

// Intersection Observer 设置
const setupObserver = () => {
    if (!sentinel.value || observer) return;

    observer = new IntersectionObserver(
        (entries) => {
            const [entry] = entries;
            if (entry?.isIntersecting && !loading.value && hasMore.value) {
                loadData();
            }
        },
        {
            rootMargin: '100px', // 提前触发加载
            threshold: 0.1
        }
    );

    observer.observe(sentinel.value);
};

// 清理观察者
const cleanupObserver = () => {
    if (observer) {
        observer.disconnect();
        observer = null;
    }
};

// 返回顶部功能
const scrollToTop = () => {
    window.scrollTo({ top: 800, behavior: 'smooth' });
};

const handleScroll = () => {
    showBackToTop.value = window.scrollY > 500;
};

//文章跳转
const gotoDetail = (id: string | number, isComment?: number) => {
    router.push({ name: 'article', params: { id: id }, query: { isComment: isComment ?? 1 } })
}

// 生命周期
onMounted(() => {
    loadData().then(() => nextTick(setupObserver));

    scrollHandler = handleScroll;
    window.addEventListener('scroll', scrollHandler, { passive: true });
   setTimeout(() => {
      const contentElement = document.querySelector('.archive-page')
      if (contentElement) {
        contentElement.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
});

onUnmounted(() => {
    cleanupObserver();
    if (scrollHandler) window.removeEventListener('scroll', scrollHandler);
    if (prefetchTimer) clearTimeout(prefetchTimer);
});

// KeepAlive 支持
onActivated(() => {
    if (!observer) setupObserver();
});

onDeactivated(() => {
    cleanupObserver();
});

// 监听主题变化（可选：重新渲染关键元素）
watch(() => them.value.accentColor, () => {
    // 触发必要的样式更新
});
</script>

<style scoped>
/* ==========================================================================
   归档页面 - 优化版样式表
   ========================================================================== */

.archive-page {
    min-height: 100vh;
    transition: all 0.3s ease;
}

/* 头部区域 */
.page-header {
    text-align: center;
    padding: clamp(60px, 10vh, 100px) 20px clamp(40px, 6vh, 60px);
    position: relative;
    overflow: hidden;
    background-color: v-bind('themStore.them.cardBg') !important;
    border-radius: 0 0 24px 24px;
    margin-bottom: 20px;
}

.page-header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 50% 50%, v-bind('them.accentColor + "10"'), transparent 70%);
    opacity: 0.5;
    z-index: 0;
}

.header-content {
    position: relative;
    z-index: 1;
}

.page-title {
    font-size: clamp(2rem, 5vw, 3rem);
    font-weight: 800;
    margin: 0 0 16px;
    letter-spacing: -0.02em;
    line-height: 1.2;
}

.page-subtitle {
    font-size: 1.1rem;
    margin: 0;
    opacity: 0.8;
}

.page-subtitle strong {
    color: v-bind('them.textColor');
    font-weight: 700;
}

/* 时间轴容器 */
.timeline-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 40px 20px;
    min-height: 50vh;
    border-radius: 20px;
    position: relative;
    z-index: 2;
    background-color: v-bind('themStore.them.cardBg') !important;
    contain: layout style;
    /* 性能优化 */
}

/* 月份组 */
.month-group {
    margin-bottom: 48px;
    animation: slideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.month-group:nth-child(1) {
    animation-delay: 0.1s;
}

.month-group:nth-child(2) {
    animation-delay: 0.2s;
}

.month-group:nth-child(3) {
    animation-delay: 0.3s;
}

.month-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    padding: 0 10px;
    position: sticky;
    top: 20px;
    z-index: 10;
    background: v-bind('themStore.them.cardBg');
    backdrop-filter: blur(10px);
    border-radius: 12px;
    padding: 12px;
    margin: -12px -12px 24px;
}

.month-badge {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.month-badge .year {
    font-size: 1.6rem;
    font-weight: 800;
    font-variant-numeric: tabular-nums;
}

.month-badge .month {
    font-size: 1.2rem;
    font-weight: 600;
    opacity: 0.9;
}

.count {
    font-size: 0.9rem;
    font-weight: 600;
    padding: 4px 12px;
    background: v-bind('them.accentColor + "15"');
    border-radius: 20px;
    color: v-bind('them.textColor') !important;
}

/* 时间轴线与内容 */
.timeline-items {
    position: relative;
    padding-left: 32px;
}

.timeline-line {
    position: absolute;
    left: 6px;
    top: 8px;
    bottom: 8px;
    width: 2px;
    opacity: 0.3;
    border-radius: 1px;
}

/* 文章卡片 - 性能优化版 */
.item {
    position: relative;
    margin-bottom: 20px;
    border-radius: 16px;
    overflow: hidden;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s ease;
    cursor: pointer;
    contain: layout style paint;
}

.item:hover,
.item.is-hovered {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.item.is-top {
    border: 2px solid #ffc107;
}

/* 背景图容器 - 使用伪元素优化性能 */
.content-wrapper {
    position: relative;
    min-height: 200px;
    background-color: v-bind('them.cardBg');
    isolation: isolate;
}

.content-wrapper::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: var(--bg-image, none);
    background-size: cover;
    background-position: center;
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 0;
}

.item:hover .content-wrapper::before {
    transform: scale(1.05);
}

/* 遮罩层 */
.content-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom,
            rgba(0, 0, 0, 0.2) 0%,
            rgba(0, 0, 0, 0.6) 50%,
            rgba(0, 0, 0, 0.8) 100%);
    z-index: 1;
    transition: opacity 0.3s ease;
}

.item:hover .content-overlay {
    opacity: 0.9;
}

/* 内容区 */
.content-inner {
    position: relative;
    z-index: 2;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    color: #fff;
}

/* 元信息行 */
.meta-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 0.85rem;
    opacity: 0.9;
}

.date {
    font-variant-numeric: tabular-nums;
    font-weight: 500;
}

.category-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(4px);
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: 600;
    border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 标题优化 */
.title-wrapper {
    margin: 0;
    line-height: 1.4;
}

.title-link {
    font-size: 1.25rem;
    font-weight: 700;
    color: inherit;
    text-decoration: none;
    display: inline;
    background-image: linear-gradient(currentColor, currentColor);
    background-position: 0% 100%;
    background-repeat: no-repeat;
    background-size: 0% 2px;
    transition: background-size 0.3s ease;
    line-height: 1.5;
}

.title-link:hover,
.title-link:focus {
    background-size: 100% 2px;
    outline: none;
}

.top-badge {
    display: inline-flex;
    align-items: center;
    margin-left: 8px;
    padding: 2px 8px;
    background: #ffc107;
    color: #000;
    font-size: 0.7rem;
    font-weight: 700;
    border-radius: 4px;
    vertical-align: middle;
}

/* 摘要 */
.summary {
    margin: 0;
    font-size: 0.95rem;
    line-height: 1.6;
    opacity: 0.85;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    max-width: 90%;
}

/* 统计信息 */
.stats {
    display: flex;
    gap: 16px;
    margin-top: auto;
    padding-top: 8px;
}

.stat {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 0.85rem;
    opacity: 0.8;
    transition: opacity 0.2s;
}

.item:hover .stat {
    opacity: 1;
}

.icon {
    font-size: 1rem;
    filter: grayscale(0.3);
}

/* 时间轴节点 */
.dot-wrapper {
    position: absolute;
    left: -38px;
    top: 24px;
    z-index: 3;
}

.dot {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 3px solid v-bind('themStore.them.cardBg');
    background-color: v-bind('them.accentColor');
    transition: transform 0.2s ease, box-shadow 0.2s;
    position: relative;
}

.item:hover .dot {
    transform: scale(1.2);
}

.dot.pinned {
    background-color: #ffc107;
    animation: pulse 2s infinite;
}

.pin-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 10px;
    filter: grayscale(1) contrast(2);
}

@keyframes pulse {

    0%,
    100% {
        box-shadow: 0 0 0 0 rgba(255, 193, 7, 0.4);
    }

    50% {
        box-shadow: 0 0 0 8px rgba(255, 193, 7, 0);
    }
}

/* 加载状态 */
.sentinel {
    text-align: center;
    padding: 40px 20px;
    min-height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.loader {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.spinner {
    width: 32px;
    height: 32px;
    border: 3px solid;
    border-top-color: transparent;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    will-change: transform;
}

.no-more {
    color: v-bind('them.textColor');
    font-size: 0.9rem;
    opacity: 0.6;
}

/* 返回顶部按钮 */
.back-to-top {
    position: fixed;
    bottom: 32px;
    right: 32px;
    width: 48px;
    height: 48px;
    border: none;
    border-radius: 50%;
    color: #fff;
    font-size: 1.5rem;
    cursor: pointer;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-to-top:hover {
    transform: translateY(-4px) scale(1.1);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

.back-to-top:active {
    transform: translateY(-2px) scale(0.95);
}

/* 动画定义 */
@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(30px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

/* TransitionGroup 动画 */
.month-fade-enter-active,
.month-fade-leave-active {
    transition: all 0.5s ease;
}

.month-fade-enter-from,
.month-fade-leave-to {
    opacity: 0;
    transform: translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: scale(0.8);
}

/* 响应式适配 */
@media (max-width: 768px) {
    .page-header {
        border-radius: 0 0 20px 20px;
        padding: 40px 16px 30px;
    }

    .timeline-container {
        padding: 24px 16px;
        border-radius: 0;
        margin-top: 0;
    }

    .timeline-items {
        padding-left: 28px;
    }

    .dot-wrapper {
        left: -34px;
        top: 20px;
    }

    .dot {
        width: 14px;
        height: 14px;
        border-width: 2px;
    }

    .content-inner {
        padding: 20px;
    }

    .title-link {
        font-size: 1.1rem;
    }

    .summary {
        font-size: 0.9rem;
        -webkit-line-clamp: 3;
    }

    .back-to-top {
        bottom: 20px;
        right: 20px;
        width: 44px;
        height: 44px;
        font-size: 1.25rem;
    }

    .month-header {
        position: relative;
        top: 0;
    }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {

    *,
    *::before,
    *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        transition-duration: 0.01ms !important;
    }
}

/* 暗色模式优化（如果系统支持） */
@media (prefers-color-scheme: dark) {
    .content-overlay {
        background: linear-gradient(to bottom,
                rgba(0, 0, 0, 0.4) 0%,
                rgba(0, 0, 0, 0.7) 50%,
                rgba(0, 0, 0, 0.9) 100%);
    }
}

/* 打印样式 */
@media print {

    .page-header,
    .back-to-top,
    .sentinel,
    .dot-wrapper {
        display: none;
    }

    .timeline-container {
        max-width: 100%;
    }

    .item {
        break-inside: avoid;
        page-break-inside: avoid;
    }
}
</style>