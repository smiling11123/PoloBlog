<template>
    <div class="article-list-page">
        <!-- 分类信息头部（简洁版） -->
        <div class="list-header" :style="{ borderBottomColor: themStore.them.cardBorder }">
            <div class="header-inner">
                <div class="breadcrumb" :style="{ color: themStore.them.textSecondary }">
                    <span class="link" @click="$router.push('/categories')">分类</span>
                    <span class="separator">/</span>
                    <span :style="{ color: themStore.them.textColor }">{{ categoryInfo?.name || '文章列表' }}</span>
                </div>

                <h1 :style="{ color: themStore.them.textColor }">
                    {{ categoryInfo?.name || '文章列表' }}
                </h1>

                <p class="category-desc" :style="{ color: themStore.them.textSecondary }">
                    {{ categoryInfo?.description || '探索该分类下的所有文章' }}
                </p>

                <div class="category-stats" :style="{ color: themStore.them.textTertiary }">
                    <span>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                            <polyline points="14 2 14 8 20 8"></polyline>
                        </svg>
                        {{ total }} 篇文章
                    </span>
                    <span>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                            stroke-width="2">
                            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        {{ categoryInfo?.viewCount || 0 }} 次浏览
                    </span>
                </div>
            </div>
        </div>

        <!-- 内容区域 -->
        <div class="list-content">
            <div class="list-container">
                <!-- 排序和筛选 -->
                <div class="list-toolbar">
                    <div class="filter-tabs">
                        <button v-for="tab in filterTabs" :key="tab.value"
                            :class="{ active: currentFilter === tab.value }" :style="getTabStyle(tab.value)"
                            @click="currentFilter = tab.value; loadArticles()">
                            {{ tab.label }}
                        </button>
                    </div>
                </div>

                <!-- 文章列表 -->
                <div class="articles-list" v-if="!loading">
                    <article v-for="article in articles" :key="article.id" class="list-item" :style="{
                        backgroundColor: themStore.them.cardBg,
                        borderColor: themStore.them.cardBorder
                    }" @mouseenter="hoveredArticle = article.id" @mouseleave="hoveredArticle = null"
                        :class="{ hover: hoveredArticle === article.id }" @click="goToArticle(article.id, article.isComment)">
                        <div class="item-image" v-if="article.thumbnail">
                            <img
                                :src="getOptimizedImageUrl(article.thumbnail, 'md')"
                                :srcset="buildImageSrcSet(article.thumbnail)"
                                sizes="(max-width: 968px) 100vw, 200px"
                                loading="lazy"
                                decoding="async"
                                @error="fallbackToOriginalImage($event, article.thumbnail)"
                            />
                            <div v-if="article.isTop" class="top-badge"
                                :style="{ backgroundColor: themStore.them.accentColor }">
                                置顶
                            </div>
                        </div>

                        <div class="item-content">
                            <div class="item-meta">
            
                                <span class="date" :style="{ color: themStore.them.textTertiary }">
                                    {{ formatDate(article.createTime) }}
                                </span>
                            </div>

                            <h2 :style="{ color: themStore.them.textColor }">{{ article.title }}</h2>

                            <p class="item-summary" :style="{ color: themStore.them.textSecondary }">
                                {{ article.summary }}
                            </p>

                            <div class="item-footer">
                                <div class="stats" :style="{ color: themStore.them.textTertiary }">
                                    <span>
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                                            <circle cx="12" cy="12" r="3"></circle>
                                        </svg>
                                        {{ article.viewCount }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </article>
                </div>

                <!-- 加载中状态 -->
                <div v-else class="loading-state">
                    <div class="loader" :style="{ borderTopColor: themStore.them.accentColor }"></div>
                    <p :style="{ color: themStore.them.textSecondary }">加载文章中...</p>
                </div>

                <!-- 分页 -->
                <div class="pagination" v-if="total > 0 && !loading">
                    <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)" :style="{
                        backgroundColor: themStore.them.cardBg,
                        color: themStore.them.textColor
                    }" :class="{ disabled: currentPage === 1 }">
                        上一页
                    </button>

                    <div class="page-numbers">
                        <button v-for="page in displayedPages" :key="page" :class="{ active: currentPage === page }"
                            :style="getPageStyle(page)" @click="changePage(page)">
                            {{ page }}
                        </button>
                    </div>

                    <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)" :style="{
                        backgroundColor: themStore.them.cardBg,
                        color: themStore.them.textColor
                    }" :class="{ disabled: currentPage >= totalPages }">
                        下一页
                    </button>
                </div>

                <!-- 空状态 -->
                <div v-if="articles.length === 0 && !loading" class="empty-state">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" :stroke="themStore.them.textTertiary"
                        stroke-width="1">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                        <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                    <p :style="{ color: themStore.them.textSecondary }">该分类下暂无文章</p>
                    <button @click="$router.push('/categories')" :style="{
                        backgroundColor: themStore.them.accentColor,
                        color: themStore.them.textColor
                    }" class="back-btn">
                        返回分类页
                    </button>
                </div>
            </div>

            <!-- 侧边栏 -->
            <aside class="list-sidebar">
                <div class="sidebar-widget" :style="{ backgroundColor: themStore.them.widgetBg }">
                    <h3 :style="{ color: themStore.them.textColor }">相关分类</h3>
                    <div class="related-categories">
                        <div v-for="cat in relatedCategories" :key="cat.id" class="related-item"
                            :style="{ borderBottomColor: themStore.them.cardBorder }" @click="switchCategory(cat.id)">
                            <span :style="{ color: themStore.them.textColor }">{{ cat.name }}</span>
                           
                        </div>
                    </div>
                </div>
            </aside>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemStore } from '@/stores/them'
import { getArticleByCategory } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { buildImageSrcSet, fallbackToOriginalImage, getOptimizedImageUrl } from '@/utils/image'

const themStore = useThemStore()
const route = useRoute()
const router = useRouter()

const categoryId = computed(() => String(route.params.id))
const categoryInfo = ref<any>(null)
const articles = ref<any[]>([])
const relatedCategories = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const hoveredArticle = ref<string | null>(null)
const currentFilter = ref('newest')

const filterTabs = [
    { label: '最新发布', value: 'newest' },
    { label: '最多浏览', value: 'hotest' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const displayedPages = computed(() => {
    const pages = []
    const maxDisplay = 5
    let start = Math.max(1, currentPage.value - Math.floor(maxDisplay / 2))
    let end = Math.min(totalPages.value, start + maxDisplay - 1)

    if (end - start < maxDisplay - 1) {
        start = Math.max(1, end - maxDisplay + 1)
    }

    for (let i = start; i <= end; i++) {
        pages.push(i)
    }
    return pages
})

const getTabStyle = (value: string) => {
    const isActive = currentFilter.value === value
    return {
        color: isActive ? themStore.them.textColor : themStore.them.textSecondary,
        borderBottom: isActive ? `2px solid ${themStore.them.textColor}` : '2px solid transparent'
    }
}

const getPageStyle = (page: number) => {
    const isActive = currentPage.value === page
    return {
        backgroundColor: isActive ? themStore.them.accentColor : themStore.them.cardBg,
        color: isActive ? themStore.them.textColor : themStore.them.textSecondary
    }
}

const loadArticles = async () => {
    if (!categoryId.value) return

    loading.value = true
    try {
        const res = await getArticleByCategory({
            categoryId: categoryId.value,
            page: currentPage.value,
            size: pageSize.value,
            orderBy: currentFilter.value === 'hotest' ? 'view_count' : 'create_time'
        }) as any
        
        if (res.code === 200) {
            articles.value = res.data.records
            total.value = res.data.total
        }
    } catch (error) {
        console.error('加载文章失败:', error)
    } finally {
        loading.value = false
    }
}

const loadCategoryInfo = async () => {
    try {
        const res = await getCategoryList() as any
        if (res.code === 200) {
            const all = res.data
            categoryInfo.value = all.find((c: any) => c.id === categoryId.value)
            if (categoryInfo.value?.pid) {
                relatedCategories.value = all.filter((c: any) => c.pid === categoryInfo.value.pid && c.id !== categoryId.value)
            } else {
                relatedCategories.value = all.filter((c: any) => !c.pid && c.id !== categoryId.value)
            }
        }
    } catch (error) {
        console.error('加载分类信息失败:', error)
    }
}

const changePage = (page: number) => {
    currentPage.value = page
    loadArticles()
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

const switchCategory = (id: number) => {
    router.push({ name: 'categoryArticles', params: { id: id.toString() } })
}

const goToArticle = (id: string, isComment?: number) => {
    router.push({ path: `/article/${id}`, query: { isComment: isComment ?? 1 } })
}

const formatDate = (date: string) => {
    return new Date(date).toLocaleDateString('zh-CN')
}

watch(categoryId, () => {
    currentPage.value = 1
    loadCategoryInfo()
    loadArticles()
}, { immediate: true })

onMounted(() => {
    loadCategoryInfo()
})
</script>

<style scoped>
.article-list-page {
    min-height: 100vh;
}

/* 头部信息区 - 简洁版 */
.list-header {
    padding: 40px 20px;
    border-bottom: 1px solid;
    margin-bottom: 40px;
}

.header-inner {
    max-width: 1400px;
    margin: 0 auto;
    text-align: center;
}

.breadcrumb {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    font-size: 0.9rem;
    margin-bottom: 15px;
}

.breadcrumb .link {
    cursor: pointer;
    transition: color 0.2s;
}

.breadcrumb .link:hover {
    color: v-bind('themStore.them.accentColor');
}

.list-header h1 {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 10px;
}

.category-desc {
    font-size: 1.1rem;
    max-width: 600px;
    margin: 0 auto 15px;
    line-height: 1.6;
}

.category-stats {
    display: flex;
    justify-content: center;
    gap: 30px;
    font-size: 0.9rem;
}

.category-stats span {
    display: flex;
    align-items: center;
    gap: 5px;
}

/* 内容区域 */
.list-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px 60px;
    display: grid;
    grid-template-columns: 1fr 300px;
    gap: 40px;
}

.list-container {
    display: flex;
    flex-direction: column;
    gap: 30px;
}

.list-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 10px;
}

.filter-tabs {
    display: flex;
    gap: 25px;
}

.filter-tabs button {
    background: none;
    border: none;
    padding: 10px 0;
    cursor: pointer;
    font-size: 0.95rem;
    transition: all 0.2s;
}

.articles-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.list-item {
    display: grid;
    grid-template-columns: 200px 1fr;
    gap: 25px;
    padding: 25px;
    border-radius: 16px;
    border: 1px solid;
    cursor: pointer;
    transition: all 0.3s ease;
    overflow: hidden;
}

.list-item.hover {
    background-color: v-bind('themStore.them.cardBgHover') !important;
    border-color: v-bind('themStore.them.cardBorderHover') !important;
    transform: translateX(10px);
    box-shadow: v-bind('themStore.them.cardShadow');
}

.item-image {
    position: relative;
    height: 150px;
    border-radius: 12px;
    overflow: hidden;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.list-item:hover .item-image img {
    transform: scale(1.05);
}

.top-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 0.75rem;
    color: white;
    font-weight: 600;
}

.item-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.item-meta {
    display: flex;
    gap: 20px;
    font-size: 0.85rem;
}

.item-meta span {
    display: flex;
    align-items: center;
    gap: 5px;
}

.item-content h2 {
    font-size: 1.4rem;
    font-weight: 600;
    line-height: 1.4;
}

.item-summary {
    font-size: 0.95rem;
    line-height: 1.7;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
    padding-top: 15px;
}

.stats {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 0.85rem;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    padding: 40px 0;
}

.pagination button {
    padding: 10px 20px;
    border-radius: 8px;
    border: 1px solid v-bind('themStore.them.cardBorder');
    cursor: pointer;
    transition: all 0.2s;
}

.pagination button.disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-numbers {
    display: flex;
    gap: 8px;
}

.page-numbers button {
    width: 40px;
    height: 40px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    border: 1px solid v-bind('themStore.them.cardBorder');
}

.list-sidebar {
    position: sticky;
    top: 100px;
    height: fit-content;
}

.sidebar-widget {
    border-radius: 16px;
    padding: 25px;
    backdrop-filter: blur(10px);
}

.sidebar-widget h3 {
    font-size: 1.1rem;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid v-bind('themStore.them.cardBorder');
}

.related-categories {
    display: flex;
    flex-direction: column;
}

.related-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid;
    cursor: pointer;
    transition: all 0.2s;
}

.related-item:last-child {
    border-bottom: none;
}

.related-item:hover {
    padding-left: 10px;
    color: v-bind('themStore.them.accentColor');
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    gap: 20px;
}

.back-btn {
    padding: 12px 30px;
    border-radius: 25px;
    border: none;
    cursor: pointer;
    font-size: 0.9rem;
    margin-top: 10px;
    transition: transform 0.2s;
}

.back-btn:hover {
    transform: scale(1.05);
}

/* 加载中状态 */
.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 100px 0;
    gap: 20px;
}

.loader {
    width: 40px;
    height: 40px;
    border: 3px solid transparent;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

@media (max-width: 968px) {
    .list-content {
        grid-template-columns: 1fr;
    }

    .list-sidebar {
        position: static;
        order: -1;
    }

    .list-item {
        grid-template-columns: 1fr;
    }

    .item-image {
        height: 200px;
    }

    .list-header h1 {
        font-size: 1.8rem;
    }
}
</style>
