<template>
    <div class="home-view">
        <!-- 搜索控制栏（固定在顶部） -->
        <div class="search-controls" :style="{
            backgroundColor: themStore.them.cardBg,
            borderBottomColor: themStore.them.cardBorder
        }">
            <div class="controls-inner">
                <!-- 搜索框 -->
                <div class="search-box-container">
                    <div class="search-box" :style="{
                        backgroundColor: themStore.them.cardBg,
                        borderColor: themStore.them.cardBorder,
                        boxShadow: themStore.them.cardShadow
                    }">
                        <input v-model="searchKeyword" type="text" placeholder="搜索文章标题、内容..." class="search-input"
                            :style="{ color: themStore.them.textColor }" @keyup.enter="handleSearch(1)" />
                        <!-- 清空按钮 -->
                        <button v-if="searchKeyword" class="clear-input-btn" @click="clearSearchInput"
                            :style="{ color: themStore.them.textTertiary }">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                        <button class="search-btn" @click="handleSearch(1)" :style="{
                            backgroundColor: themStore.them.accentColor,
                            color: '#fff'
                        }">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <circle cx="11" cy="11" r="8"></circle>
                                <path d="m21 21-4.35-4.35"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                <!-- 标签筛选 - 始终显示，不受搜索状态影响 -->
                <div class="tag-filter" v-if="tagList.length > 0">
                    <span class="filter-label" :style="{ color: themStore.them.textSecondary }">热门标签：</span>
                    <div class="tag-list">
                        <button v-for="tag in tagList" :key="tag.id" class="tag-btn"
                            :class="{ active: selectedTags.includes(tag.id) }" :style="getTagStyle(tag.id)"
                            @click="toggleTag(tag.id)">
                            {{ tag.name }}
                        </button>
                    </div>
                </div>

                <div class="mobile-categories" v-if="categoryList.length > 0 && isMobile">
                    <div class="custom-select-wrapper" :class="{ 'is-open': isDropdownOpen }">
                        <div class="mobile-category-select" @click="isDropdownOpen = !isDropdownOpen" :style="{
                            backgroundColor: themStore.them.cardBg,
                            borderColor: isDropdownOpen ? themStore.them.accentColor : themStore.them.cardBorder,
                            boxShadow: isDropdownOpen ? `0 0 0 2px ${themStore.them.accentColor}40` : themStore.them.cardShadow,
                            color: themStore.them.textColor
                        }">
                            <svg class="category-icon" width="18" height="18" viewBox="0 0 24 24" fill="none"
                                :stroke="themStore.them.textSecondary" stroke-width="2">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                            </svg>

                            <span class="selected-text">{{ currentCategoryName }}</span>

                            <svg class="dropdown-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none"
                                :stroke="themStore.them.textTertiary" stroke-width="2">
                                <polyline points="6 9 12 15 18 9"></polyline>
                            </svg>
                        </div>

                        <transition name="dropdown">
                            <div class="custom-options-panel" v-show="isDropdownOpen" :style="{
                                backgroundColor: themStore.them.articleBg, /* 使用较深的背景避免文字看不清 */
                                borderColor: themStore.them.cardBorder,
                                boxShadow: themStore.them.cardShadowHover
                            }">
                                <div class="custom-option" @click="handleSelect('')"
                                    :style="{ color: !mobileSelectedCategory ? themStore.them.textColor : themStore.them.textSecondary }">
                                    全部分类
                                </div>
                                <div class="custom-option" v-for="cat in categoryList" :key="cat.id"
                                    @click="handleSelect(cat.id)"
                                    :style="{ color: mobileSelectedCategory === cat.id ? themStore.them.textColor : themStore.them.textSecondary }">
                                    {{ cat.name }} ({{ cat.viewCount || 0 }})
                                </div>
                            </div>
                        </transition>
                    </div>
                </div>
            </div>
        </div>

        <!-- 主内容区 -->
        <div class="content-body">
            <!-- 标题：根据是否有活跃筛选条件显示不同标题 -->
            <h3 class="animate-in title" v-if="!hasActiveFilters">精选文章</h3>
            <h3 class="animate-in title" v-else>
                {{ searchKeyword ? '搜索结果' : '筛选结果' }}
                <span :style="{ color: themStore.them.textSecondary, fontSize: '0.6em', fontWeight: 'normal' }">
                    找到 {{ total }} 篇文章
                </span>
                <button class="reset-filter-btn" @click="clearAllFilters"
                    :style="{ color: themStore.them.accentColor, fontSize: '0.5em', marginLeft: '12px' }">
                    重置筛选
                </button>
            </h3>

            <div class="layout-container">
                <!-- 左侧：文章列表 -->
                <main class="post-list" v-loading="loading" element-loading-background="rgba(0,0,0,0)">

                    <!-- 轮播图 -->
                    <div class="featured-carousel animate-in"  style="--delay: 0s">
                        <div class="carousel-container" ref="carouselRef">
                            <div v-for="(article, index) in hotArticles" :key="article.id" class="carousel-slide"
                                :class="{ active: currentSlide === index }" @click="gotoDetail(article.id, article.isComment)">
                                <div class="slide-bg"
                                    :style="{ backgroundImage: `url(${article.thumbnail || 'https://img.api.aa1.cn/2025/01/17/2ed9424c13a05.jpeg'})` }">
                                </div>
                                <div class="slide-overlay"></div>
                                <div class="slide-content">
                                    <span class="hot-badge">热门推荐</span>
                                    <h3>{{ article.title }}</h3>
                                    <p>{{ article.summary }}</p>
                                </div>
                            </div>
                        </div>

                        <!-- 轮播控制 -->
                        <button class="carousel-btn prev-btn" @click.stop="prevSlide">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M15 18l-6-6 6-6" />
                            </svg>
                        </button>
                        <button class="carousel-btn next-btn" @click.stop="nextSlide">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M9 18l6-6-6-6" />
                            </svg>
                        </button>

                        <div class="carousel-dots">
                            <span v-for="(article, index) in hotArticles" :key="index"
                                :class="{ active: currentSlide === index }" @click.stop="goToSlide(index)"></span>
                        </div>
                    </div>

                    <!-- 活跃筛选时的排序选项 -->
                    <div v-if="hasActiveFilters" class="search-results-header animate-in">
                        <div class="sort-options">
                            <button v-for="opt in sortOptions" :key="opt.value"
                                :class="{ active: sortBy === opt.value }" :style="getSortStyle(opt.value)"
                                @click="changeSort(opt.value)">
                                {{ opt.label }}
                            </button>
                        </div>
                    </div>

                    <!-- 文章网格 - 改为长方形卡片 -->
                    <div class="articles-grid" :class="{ 'search-mode': hasActiveFilters }">
                        <article v-for="(article, index) in displayArticles" :key="article.id || index"
                            class="post-card animate-in" :style="{ '--delay': `${index * 0.1}s` }"
                            @click="gotoDetail(article.id, article.isComment)">
                            <!-- 封面占100%，信息叠加在封面上 -->
                            <div class="post-cover">
                                <img v-if="article.thumbnail" class="cover-image" :src="article.thumbnail" alt="cover"
                                    loading="lazy">
                                <div v-else class="cover-image empty-cover">
                                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                        stroke-width="1.5">
                                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                        <circle cx="8.5" cy="8.5" r="1.5"></circle>
                                        <polyline points="21 15 16 10 5 21"></polyline>
                                    </svg>
                                </div>

                                <!-- 渐变遮罩 -->
                                <div class="cover-overlay"></div>

                                <!-- 置顶标识 -->
                                <div v-if="article.isTop" class="top-badge"
                                    :style="{ backgroundColor: themStore.them.accentColor }">
                                    置顶
                                </div>

                                <!-- 封面上的信息 -->
                                <div class="cover-content">
                                    <div class="content-header">
                                        <div class="meta-row">
                                            <span class="category-tag">{{ article.categoryName || '未分类' }}</span>
                                            <span class="dot">·</span>
                                            <span class="date-text">{{ formatTime(article.createTime) }}</span>
                                        </div>
                                        <h3 class="article-title" :title="article.title">{{ article.title }}</h3>
                                        <p class="article-summary">{{ article.summary }}</p>
                                    </div>

                                    <div class="content-footer">
                                        <span class="views">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                                stroke="currentColor" stroke-width="2">
                                                <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                                                <circle cx="12" cy="12" r="3"></circle>
                                            </svg>
                                            {{ article.viewCount || 0 }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </article>
                    </div>

                    <!-- 空状态 -->
                    <div v-if="!loading && displayArticles.length === 0" class="empty-state animate-in">
                        <svg width="80" height="80" viewBox="0 0 24 24" fill="none"
                            :stroke="themStore.them.textTertiary" stroke-width="1">
                            <circle cx="11" cy="11" r="8"></circle>
                            <path d="m21 21-4.35-4.35"></path>
                            <path d="M8 8l6 6"></path>
                            <path d="M14 8l-6 6"></path>
                        </svg>
                        <p :style="{ color: themStore.them.textSecondary }">没有找到相关文章</p>
                        <button @click="clearAllFilters" :style="{
                            backgroundColor: themStore.them.accentColor,
                            color: '#fff'
                        }" class="back-btn">
                            返回全部文章
                        </button>
                    </div>

                    <!-- 分页 -->
                    <div class="pagination-wrapper animate-in" v-if="total > pageSize">
                        <el-pagination layout="prev, pager, next" v-model:current-page="currentPage"
                            v-model:page-size="pageSize" :total="total" :background="true" class="custom-pagination"
                            @current-change="handlePageChange" />
                    </div>
                </main>

                <!-- 右侧：侧边栏 -->
                <aside class="sidebar">
                    <!-- 分类筛选（桌面端 - 改为多选勾选模式） -->
                    <div class="widget category-widget animate-in">
                        <div class="widget-header">
                            <h4>分类筛选</h4>
                            <span v-if="selectedCategories.length > 0" class="clear-filter"
                                @click="clearCategoryFilter">
                                清除
                            </span>
                        </div>
                        <div class="category-list checkbox-mode">
                            <label v-for="cat in categoryList" :key="cat.id" class="category-item checkbox-item"
                                :class="{ active: selectedCategories.includes(cat.id) }" :style="{
                                    color: selectedCategories.includes(cat.id) ? themStore.them.accentColor : themStore.them.textSecondary,
                                    backgroundColor: selectedCategories.includes(cat.id) ? `${themStore.them.accentColor}15` : 'transparent'
                                }">
                                <div class="checkbox-wrapper">
                                    <input type="checkbox" :value="cat.id" v-model="selectedCategories"
                                        @change="handleCategoryFilterChange" />
                                    <span class="checkmark"></span>
                                </div>
                                <span class="cat-name">{{ cat.name }}</span>
                                <span class="cat-count" :style="{ color: themStore.them.textTertiary }">{{ cat.viewCount
                                    || 0 }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 个人信息 -->
                    <div class="widget profile-widget animate-in">
                        <div class="avatar">
                            <img :src="userStore.authInfo?.avatar || '/default-avatar.jpg'" alt="Admin">
                        </div>
                        <h4>{{ userStore.authInfo?.userName || 'Admin' }}</h4>
                        <div class="slogan-list">
                            <span v-for="slogan in displaySlogans" :key="slogan" class="slogan-pill">
                                {{ slogan }}
                            </span>
                        </div>
                        <div class="tech-stack">
                            <span v-for="tech in ['Vue3', 'Java', 'Docker']" :key="tech" class="badge">
                                {{ tech }}
                            </span>
                        </div>
                    </div>

                    <!-- 站点统计 -->
                    <div class="widget stats-widget animate-in" ref="statsWidgetRef">
                        <h4>站点统计</h4>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                    <polyline points="14 2 14 8 20 8"></polyline>
                                </svg>
                                文章总数
                            </span>
                            <strong class="count-up">{{ animatedStats.totalArticleCount.toLocaleString() }}</strong>
                        </div>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                                    <circle cx="12" cy="12" r="3"></circle>
                                </svg>
                                总浏览量
                            </span>
                            <strong class="count-up">{{ animatedStats.totalViewCount.toLocaleString() }}</strong>
                        </div>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path d="M12 2v20M2 12h20"></path>
                                </svg>
                                网站访问
                            </span>
                            <strong class="count-up">{{ animatedStats.totalVisitCount.toLocaleString() }}</strong>
                        </div>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="9" cy="7" r="4"></circle>
                                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                </svg>
                                独立访客
                            </span>
                            <strong class="count-up">{{ siteStats.totalVisitorCount.toLocaleString() }}</strong>
                        </div>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path
                                        d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z">
                                    </path>
                                </svg>
                                评论总数
                            </span>
                            <strong class="count-up">{{ siteStats.totalCommentCount.toLocaleString() }}</strong>
                        </div>
                        <div class="stat-item">
                            <span>
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                    :stroke="themStore.them.textColor" stroke-width="2">
                                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                    <circle cx="9" cy="7" r="4"></circle>
                                </svg>
                                注册用户
                            </span>
                            <strong class="count-up">{{ siteStats.totalUserCount.toLocaleString() }}</strong>
                        </div>
                    </div>

                    <!-- 热门标签云
                    <div class="widget tags-widget animate-in">
                        <div class="tags-cloud">
                            <span v-for="tag in tagList.slice(0, 15)" :key="tag.id" :style="getTagCloudStyle(tag.id)"
                                @click="toggleTag(tag.id)">
                                {{ tag.name }}
                            </span>
                        </div>
                    </div> -->
                </aside>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useThemStore } from '@/stores/them'
import { useUserStore } from '@/stores/user'
import { getArticleList, getHotArticle, getArticleByKeyword, getArticleByTag } from '@/api/article'
import { getTagList } from '@/api/tag'
import { getCategoryList } from '@/api/category'
import { getAllSiteData } from '@/api/allSiteData'
import { formatTime } from '@/utils/tools'
import type { articleVO } from '@/type/Interface'
import { visit } from '@/api/visit'

const router = useRouter()
const route = useRoute()
const themStore = useThemStore()
const userStore = useUserStore()

// ==================== 状态管理 ====================
const loading = ref(false)
const articleList = ref<articleVO[]>([])
const hotArticles = ref<articleVO[]>([])
const tagList = ref<any[]>([])
const categoryList = ref<any[]>([])

// 分页
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const totalArticles = ref(0)
const totalViews = ref(0)

// 搜索状态
const searchKeyword = ref('')
const selectedTags = ref<string[]>([])
// 桌面端多选
const selectedCategories = ref<number[]>([])
// 移动端单选
const mobileSelectedCategory = ref<number | ''>('')

const sortBy = ref('newest')

// 响应式检测
const isMobile = ref(false)
const checkMobile = () => {
    isMobile.value = window.innerWidth <= 1200
}

const isDropdownOpen = ref(false);

// 计算当前选中的分类名称用于回显
const currentCategoryName = computed(() => {
    if (!mobileSelectedCategory.value) return '全部分类';
    const cat = categoryList.value.find(c => c.id === mobileSelectedCategory.value);
    return cat ? `${cat.name} (${cat.viewCount || 0})` : '全部分类';
});

// 处理选中逻辑
const handleSelect = (id: string | number) => {
    mobileSelectedCategory.value = id as any;
    isDropdownOpen.value = false; // 选完自动关闭
    handleMobileCategoryChange(); // 触发你原有的搜索/筛选方法
};
// 关键修复：计算是否有活跃筛选条件（仅包括会触发搜索的条件）
const hasActiveFilters = computed(() => {
    return searchKeyword.value !== '' ||
        selectedTags.value.length > 0 ||
        selectedCategories.value.length > 0 ||
        mobileSelectedCategory.value !== ''
})

// 轮播图
const currentSlide = ref(0)
const carouselRef = ref<HTMLElement | null>(null)
let autoPlayTimer: ReturnType<typeof setInterval> | null = null

// 观察器
let observer: IntersectionObserver | null = null

// 排序选项
const sortOptions = [
    { label: '最新发布', value: 'newest' },
    { label: '最多浏览', value: 'views' },
    { label: '最多点赞', value: 'likes' }
]

// 计算属性：显示的文章列表
const displayArticles = computed(() => {
    return articleList.value
})
const displaySlogans = computed(() => {
    return userStore.displaySlogans.length > 0
        ? userStore.displaySlogans
        : ['热爱折腾的全栈开发者']
})

// 运行天数
const runningDays = computed(() => {
    const startDate = new Date('2024-01-01')
    const now = new Date()
    return Math.floor((now.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24))
})

// ==================== 新增：站点数据统计 ====================
const siteStats = ref({
    totalArticleCount: 0,
    totalViewCount: 0,
    totalVisitCount: 0,
    totalVisitorCount: 0,
    totalCommentCount: 0,
    totalUserCount: 0
})

// 动画显示的数字（用于展示）
const animatedStats = ref({
    totalArticleCount: 0,
    totalViewCount: 0,
    totalVisitCount: 0
})

// 站点统计组件的引用
const statsWidgetRef = ref<HTMLElement | null>(null)
// 是否已经动画过
const hasAnimated = ref(false)
// 统计观察器
let statsObserver: IntersectionObserver | null = null

// 加载站点统计数据
const loadSiteStats = async () => {
    try {
        const res = await getAllSiteData() as any
        if (res.code === 200 && res.data) {
            siteStats.value = {
                totalArticleCount: res.data.totalArticleCount || 0,
                totalViewCount: res.data.totalViewCount || 0,
                totalVisitCount: res.data.totalVisitCount || 0,
                totalVisitorCount: res.data.totalVisitorCount || 0,
                totalCommentCount: res.data.totalCommentCount || 0,
                totalUserCount: res.data.totalUserCount || 0
            }
            // 不立即触发动画，等待进入视口
            setupStatsObserver()
        }
    } catch (error) {
        console.error('加载站点统计失败:', error)
    }
}

// 设置统计组件的观察器
const setupStatsObserver = () => {
    if (!statsWidgetRef.value) return

    statsObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting && !hasAnimated.value) {
                hasAnimated.value = true
                animateNumbers()
                // 动画完成后停止观察
                if (statsObserver) {
                    statsObserver.unobserve(entry.target)
                }
            }
        })
    }, {
        threshold: 0.3, // 30% 可见时触发
        rootMargin: '0px 0px -50px 0px'
    })

    statsObserver.observe(statsWidgetRef.value)
}

// 数字滚动动画
const animateNumbers = () => {
    const duration = 2000 // 动画持续时间 2秒
    const startTime = performance.now()

    const animate = (currentTime: number) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)

        // 使用 easeOutQuart 缓动函数，让动画更自然
        const easeOutQuart = 1 - Math.pow(1 - progress, 4)

        animatedStats.value.totalArticleCount = Math.floor(
            siteStats.value.totalArticleCount * easeOutQuart
        )
        animatedStats.value.totalViewCount = Math.floor(
            siteStats.value.totalViewCount * easeOutQuart
        )
        animatedStats.value.totalVisitCount = Math.floor(
            siteStats.value.totalVisitCount * easeOutQuart
        )

        if (progress < 1) {
            requestAnimationFrame(animate)
        } else {
            // 确保最终显示精确值
            animatedStats.value.totalArticleCount = siteStats.value.totalArticleCount
            animatedStats.value.totalViewCount = siteStats.value.totalViewCount
            animatedStats.value.totalVisitCount = siteStats.value.totalVisitCount
        }
    }

    requestAnimationFrame(animate)
}

// ==================== 样式计算 ====================
const getTagStyle = (tagId: string) => {
    const isSelected = selectedTags.value.includes(tagId)
    return {
        backgroundColor: isSelected ? themStore.them.accentColor : themStore.them.cardBg,
        color: isSelected ? '#fff' : themStore.them.textSecondary,
        borderColor: isSelected ? themStore.them.accentColor : themStore.them.cardBorder
    }
}

const getTagCloudStyle = (tagId: string) => {
    const isSelected = selectedTags.value.includes(tagId)
    return {
        backgroundColor: isSelected ? themStore.them.accentColor : `${themStore.them.accentColor}15`,
        color: isSelected ? '#fff' : themStore.them.accentColor,
        transform: isSelected ? 'scale(1.1)' : 'scale(1)'
    }
}

const getSortStyle = (value: string) => {
    const isActive = sortBy.value === value
    return {
        color: isActive ? themStore.them.accentColor : themStore.them.textSecondary,
        borderBottom: isActive ? `2px solid ${themStore.them.accentColor}` : '2px solid transparent'
    }
}

// ==================== 方法 ====================
// 获取当前有效的分类筛选值
const getActiveCategoryIds = (): number[] => {
    const desktopIds = selectedCategories.value
    const mobileId = mobileSelectedCategory.value
    if (mobileId !== '' && mobileId !== null && mobileId !== undefined) {
        // 移动端单选优先，避免重复
        if (!desktopIds.includes(mobileId as number)) {
            return [...desktopIds, mobileId as number]
        }
    }
    return desktopIds
}

// 加载普通文章列表（初始状态）
const loadArticles = async (page: number = 1) => {
    loading.value = true
    try {
        const params: any = {
            page,
            size: pageSize.value,
            sort: sortBy.value
        }

        const res = await getArticleList(params) as any
        if (res.code === 200) {
            articleList.value = res.data.records || []
            total.value = res.data.total
            if (page === 1) {
                totalArticles.value = res.data.total
            }
        }
    } catch (error) {
        console.error('加载文章失败:', error)
    } finally {
        loading.value = false
        nextTick(() => resetObserver())
    }
}

// 加载热门文章（轮播图）
const loadHotArticles = async () => {
    try {
        const res = await getHotArticle({ page: 1, size: 5 }) as any
        if (res.code === 200) {
            hotArticles.value = res.data.records || []
        }
    } catch (error) {
        console.error('加载热门文章失败:', error)
    }
}

// 搜索文章（整合所有筛选条件）- 修复：支持传入页码参数
const handleSearch = async (page: number = 1) => {
    // 如果没有活跃筛选条件，回到初始状态
    if (!hasActiveFilters.value) {
        currentPage.value = page
        await loadArticles(page)
        return
    }

    loading.value = true
    currentPage.value = page  // 更新当前页码

    try {
        const categoryIds = getActiveCategoryIds()

        // 构建请求参数 - 使用传入的 page 参数
        const params: any = {
            page: page,  // 使用传入的 page，而不是固定 1
            size: pageSize.value,
            sort: sortBy.value,
            keyword: searchKeyword.value || undefined,
            categoryIds: categoryIds.length > 0 ? categoryIds : undefined,
            tagIds: selectedTags.value.length > 0 ? selectedTags.value : undefined
        }

        // 如果有标签筛选，优先使用标签搜索接口
        if (selectedTags.value.length > 0) {
            const res = await getArticleByTag(params) as any
            if (res.code === 200) {
                articleList.value = res.data.records || []
                total.value = res.data.total
            }
        } else {
            // 普通搜索（支持关键词+分类）
            const res = await getArticleByKeyword(params) as any
            if (res.code === 200) {
                articleList.value = res.data.records || []
                total.value = res.data.total
            }
        }
    } catch (error) {
        console.error('搜索失败:', error)
    } finally {
        loading.value = false
        nextTick(() => resetObserver())
    }
}

// 仅清空搜索输入框 - 修复：重置到第一页
const clearSearchInput = () => {
    searchKeyword.value = ''
    handleSearch(1)
}

// 桌面端：分类筛选变更处理 - 修复：筛选条件变化时重置到第一页
const handleCategoryFilterChange = () => {
    handleSearch(1)
}

// 移动端：分类下拉变更处理 - 修复：筛选条件变化时重置到第一页
const handleMobileCategoryChange = () => {
    // 同步到桌面端数组（移动端单选）
    if (mobileSelectedCategory.value !== '') {
        selectedCategories.value = [mobileSelectedCategory.value as number]
    } else {
        selectedCategories.value = []
    }
    handleSearch(1)
}

// 清除分类筛选 - 修复：重置到第一页
const clearCategoryFilter = () => {
    selectedCategories.value = []
    mobileSelectedCategory.value = ''
    handleSearch(1)
}

// 切换标签 - 修复：筛选条件变化时重置到第一页
const toggleTag = (tagId: string) => {
    const index = selectedTags.value.indexOf(tagId)
    if (index > -1) {
        selectedTags.value.splice(index, 1)
    } else {
        selectedTags.value.push(tagId)
    }
    handleSearch(1)
}

// 排序切换 - 修复：排序变化时重置到第一页
const changeSort = (sort: string) => {
    sortBy.value = sort
    handleSearch(1)
}

// 清除所有筛选（完全重置）
const clearAllFilters = () => {
    searchKeyword.value = ''
    selectedTags.value = []
    selectedCategories.value = []
    mobileSelectedCategory.value = ''
    sortBy.value = 'newest'
    currentPage.value = 1
    loadArticles(1)
}

// 分页切换 - 修复：正确传递页码参数
const handlePageChange = (page: number) => {
    currentPage.value = page
    if (hasActiveFilters.value) {
        handleSearch(page)  // 传入当前页码
    } else {
        loadArticles(page)
    }
    // 滚动到顶部
    const listEl = document.querySelector('.post-list')
    if (listEl) {
        const top = listEl.getBoundingClientRect().top + window.pageYOffset - 100
        window.scrollTo({ top, behavior: 'smooth' })
    }
}

// 轮播图控制
const nextSlide = () => {
    currentSlide.value = (currentSlide.value + 1) % hotArticles.value.length
    resetAutoPlay()
}

const prevSlide = () => {
    currentSlide.value = currentSlide.value === 0
        ? hotArticles.value.length - 1
        : currentSlide.value - 1
    resetAutoPlay()
}

const goToSlide = (index: number) => {
    currentSlide.value = index
    resetAutoPlay()
}

const startAutoPlay = () => {
    autoPlayTimer = setInterval(nextSlide, 5000)
}

const stopAutoPlay = () => {
    if (autoPlayTimer) {
        clearInterval(autoPlayTimer)
        autoPlayTimer = null
    }
}

const resetAutoPlay = () => {
    stopAutoPlay()
    startAutoPlay()
}

// 文章跳转
const gotoDetail = (id: string | number, isComment: number) => {
    router.push({ name: 'article', params: { id: id.toString() }, query: { isComment: isComment ?? 1 } })
}

// 加载标签和分类
const loadFilters = async () => {
    try {
        const [tagRes, catRes] = await Promise.all([
            getTagList() as any,
            getCategoryList() as any
        ])
        if (tagRes.code === 200) tagList.value = tagRes.data || []
        if (catRes.code === 200) {
            categoryList.value = catRes.data || []
            totalViews.value = categoryList.value.reduce((sum, cat) => sum + (cat.viewCount || 0), 0)
        }
    } catch (error) {
        console.error('加载筛选条件失败:', error)
    }
}

// 动画观察器
const resetObserver = () => {
    if (observer) observer.disconnect()
    const elements = document.querySelectorAll('.animate-in:not(.show)')
    if (elements.length === 0) return

    observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                const target = entry.target as HTMLElement
                target.classList.add('show')
                observer?.unobserve(target)
            }
        })
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' })

    elements.forEach(el => observer?.observe(el))
}

// 监听路由参数变化
watch(() => route.query, (query) => {
    if (query.keyword) searchKeyword.value = query.keyword as string
    if (query.tagId) selectedTags.value = [query.tagId as string]
    if (query.categoryId) {
        const catId = Number(query.categoryId)
        selectedCategories.value = [catId]
        mobileSelectedCategory.value = catId
    }
    if (query.keyword || query.tagId || query.categoryId) {
        handleSearch(1)
    }
}, { immediate: true })

// ==================== 生命周期 ====================
onMounted(async () => {
    checkMobile()
    window.addEventListener('resize', checkMobile)

    await Promise.all([
        loadArticles(),
        loadHotArticles(),
        loadFilters(),
        loadSiteStats(), // 新增：加载站点统计
        visit()
    ])
    startAutoPlay()
    resetObserver()
})

onUnmounted(() => {
    if (observer) observer.disconnect()
    if (statsObserver) statsObserver.disconnect()
    stopAutoPlay()
    window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.home-view {
    min-height: 100vh;

    /* CSS 变量定义 */
    --card-bg: v-bind('themStore.them.cardBg');
    --card-bg-hover: v-bind('themStore.them.cardBgHover');
    --card-border: v-bind('themStore.them.cardBorder');
    --card-border-hover: v-bind('themStore.them.cardBorderHover');
    --card-shadow: v-bind('themStore.them.cardShadow');
    --card-shadow-hover: v-bind('themStore.them.cardShadowHover');
    --widget-bg: v-bind('themStore.them.widgetBg');
    --text-primary: v-bind('themStore.them.textColor');
    --text-secondary: v-bind('themStore.them.textSecondary');
    --text-tertiary: v-bind('themStore.them.textTertiary');
    --accent-color: v-bind('themStore.them.accentColor');
    --accent-color-rgb: v-bind('themStore.them.accentColor ? themStore.them.accentColor.replace(/[^\d,]/g, "").split(",").slice(0, 3).join(",") : "59, 130, 246"');
    --separator: v-bind('themStore.them.cardBorder');
}

/* ==================== 搜索控制栏 ==================== */
.search-controls {
    position: sticky;
    top: 0;
    z-index: 100;
    padding: 24px 0;
    margin-bottom: 30px;
    border-radius: 16px;
    border-bottom: 1px solid;
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    transition: all 0.3s ease;

    &.scrolled {
        padding: 16px 0;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }
}

.controls-inner {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
}

.search-box-container {
    width: 100%;
    max-width: 600px;
}

.search-box {
    display: flex;
    align-items: center;
    border-radius: 50px;
    border: 1px solid;
    padding: 4px;
    transition: all 0.3s ease;
    position: relative;

    &:focus-within {
        transform: translateY(-2px);
        box-shadow: var(--card-shadow-hover);
        border-color: var(--accent-color);
    }
}

.search-input {
    flex: 1;
    background: transparent;
    border: none;
    padding: 12px 24px;
    font-size: 1rem;
    outline: none;

    &::placeholder {
        color: var(--text-tertiary);
    }
}

.clear-input-btn {
    background: none;
    border: none;
    cursor: pointer;
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: all 0.2s;
    margin-right: 4px;

    &:hover {
        background: rgba(128, 128, 128, 0.1);
    }
}

.search-btn {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
        transform: scale(1.05);
        filter: brightness(1.1);
    }
}

.tag-filter {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    justify-content: center;
    max-width: 800px;
}

.filter-label {
    font-size: 0.9rem;
    white-space: nowrap;
}

.tag-list {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: center;
}

.tag-btn {
    padding: 6px 14px;
    border-radius: 20px;
    border: 1px solid;
    cursor: pointer;
    font-size: 0.85rem;
    transition: all 0.3s ease;
    background: transparent;

    &:hover {
        transform: translateY(-2px);
    }

    &.active {
        font-weight: 500;
    }
}

/* 移动端分类选择器 - 优化样式 */
.mobile-categories {
    width: 100%;
    max-width: 320px;
}

.custom-select-wrapper {
    position: relative;
    width: 100%;
}

.mobile-category-select {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 16px;
    border: 1px solid;
    transition: all 0.3s ease;
    cursor: pointer;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);

    .category-icon {
        flex-shrink: 0;
        opacity: 0.8;
    }

    .selected-text {
        flex: 1;
        font-size: 0.95rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        user-select: none;
    }

    .dropdown-arrow {
        flex-shrink: 0;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
}

/* 箭头旋转动画 */
.custom-select-wrapper.is-open .dropdown-arrow {
    transform: rotate(180deg);
}

/* 悬浮下拉面板样式 */
.custom-options-panel {
    position: absolute;
    top: calc(100% + 8px);
    /* 距离输入框 8px 间距 */
    left: 0;
    right: 0;
    border-radius: 16px;
    border: 1px solid;
    overflow: hidden;
    z-index: 100;
    max-height: 240px;
    /* 限制最高高度 */
    overflow-y: auto;
    backdrop-filter: blur(15px);
    /* 增强下拉框的毛玻璃效果 */
    -webkit-backdrop-filter: blur(15px);
    padding: 8px 0;

    /* 美化滚动条 */
    &::-webkit-scrollbar {
        width: 4px;
    }

    &::-webkit-scrollbar-thumb {
        background: rgba(150, 150, 150, 0.3);
        border-radius: 4px;
    }
}

.custom-option {
    padding: 12px 20px;
    font-size: 0.95rem;
    cursor: pointer;
    transition: background-color 0.2s ease, color 0.2s ease;
    user-select: none;

    &:hover,
    &:active {
        border-radius: 16px;
        background-color: rgba(255, 255, 255, 0.1);
        /* 轻微高亮 */
    }
}

/* 下拉菜单弹出动画 */
.dropdown-enter-active,
.dropdown-leave-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from,
.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
}

/* ==================== 内容区域 ==================== */
.content-body {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px 60px;
}

.title {
    font-size: 32px;
    margin-bottom: 24px;
    color: var(--text-primary);
    font-weight: 700;
    letter-spacing: -0.5px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
}

.reset-filter-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 0.5em;
    padding: 4px 12px;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
        background: rgba(var(--accent-color), 0.1);
    }
}

.layout-container {
    display: flex;
    gap: 32px;
    align-items: flex-start;
}

/* ==================== 文章列表区域 ==================== */
.post-list {
    flex: 1;
    min-height: 500px;
}

/* 轮播图样式 */
.featured-carousel {
    width: 100%;
    height: 360px;
    position: relative;
    border-radius: 20px;
    overflow: hidden;
    margin-bottom: 32px;
    background: var(--card-bg);
    border: 1px solid var(--card-border);
    box-shadow: var(--card-shadow);
    backdrop-filter: blur(12px);

    .carousel-container {
        width: 100%;
        height: 100%;
        position: relative;
    }

    .carousel-slide {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        visibility: hidden;
        transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;

        &.active {
            opacity: 1;
            visibility: visible;
        }

        &:hover .slide-bg {
            transform: scale(1.05);
        }
    }

    .slide-bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-size: cover;
        background-position: center;
        transition: transform 0.6s ease;
    }

    .slide-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, rgba(0, 0, 0, 0.3) 50%, rgba(0, 0, 0, 0.1) 100%);
    }

    .slide-content {
        position: absolute;
        bottom: 40px;
        left: 40px;
        right: 40px;
        z-index: 2;
        color: #fff;

        .hot-badge {
            display: inline-block;
            padding: 4px 12px;
            background: var(--accent-color);
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            margin-bottom: 12px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        h3 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 12px;
            line-height: 1.3;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        }

        p {
            font-size: 1rem;
            opacity: 0.9;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            max-width: 600px;
        }
    }

    .carousel-btn {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        transition: all 0.3s ease;
        z-index: 10;

        &:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-50%) scale(1.1);
        }

        svg {
            width: 24px;
            height: 24px;
        }

        &.prev-btn {
            left: 20px;
        }

        &.next-btn {
            right: 20px;
        }
    }

    .carousel-dots {
        position: absolute;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        gap: 8px;
        z-index: 10;

        span {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.4);
            cursor: pointer;
            transition: all 0.3s ease;

            &.active {
                background: var(--accent-color);
                width: 24px;
                border-radius: 4px;
            }

            &:hover {
                background: rgba(255, 255, 255, 0.8);
            }
        }
    }
}

/* 搜索结果头部 */
.search-results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 0 4px;

    .sort-options {
        display: flex;
        gap: 24px;
    }

    .sort-options button {
        background: none;
        border: none;
        padding: 8px 0;
        cursor: pointer;
        font-size: 0.95rem;
        transition: all 0.2s;
    }
}

/* 文章网格 - 改为长方形卡片，封面占100% */
.articles-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;

    &.search-mode {
        grid-template-columns: repeat(2, 1fr);
    }
}

/* 长方形文章卡片 - 封面占100%，信息叠加在封面上 */
.post-card {
    position: relative;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    box-shadow: var(--card-shadow);
    border: 1px solid var(--card-border);
    aspect-ratio: 16 / 10;
    /* 长方形比例 */

    &:hover {
        transform: translateY(-8px);
        box-shadow: var(--card-shadow-hover);
        border-color: var(--card-border-hover);

        .cover-image {
            transform: scale(1.08);
        }

        .cover-overlay {
            background: linear-gradient(to top,
                    rgba(0, 0, 0, 0.95) 0%,
                    rgba(0, 0, 0, 0.6) 40%,
                    rgba(0, 0, 0, 0.2) 100%);
        }
    }

    .post-cover {
        position: relative;
        width: 100%;
        height: 100%;
    }

    .cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.6s cubic-bezier(0.25, 0.8, 0.25, 1);

        &.empty-cover {
            background: linear-gradient(135deg, rgba(128, 128, 128, 0.2), rgba(128, 128, 128, 0.1));
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--text-tertiary);
        }
    }

    /* 渐变遮罩 - 从下到上，确保文字可读 */
    .cover-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(to top,
                rgba(0, 0, 0, 0.85) 0%,
                rgba(0, 0, 0, 0.5) 50%,
                rgba(0, 0, 0, 0.1) 100%);
        transition: all 0.4s ease;
    }

    /* 置顶标识 */
    .top-badge {
        position: absolute;
        top: 16px;
        right: 16px;
        padding: 6px 14px;
        border-radius: 20px;
        font-size: 0.75rem;
        color: #fff;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        z-index: 2;
        backdrop-filter: blur(10px);
    }

    /* 封面上的内容 */
    .cover-content {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        padding: 24px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        z-index: 1;
        color: #fff;

        .content-header {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
        }

        .meta-row {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 0.85rem;
            margin-bottom: 12px;
            opacity: 0.9;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);

            .category-tag {
                background: rgba(255, 255, 255, 0.2);
                backdrop-filter: blur(10px);
                padding: 4px 10px;
                border-radius: 12px;
                font-weight: 500;
            }

            .dot {
                opacity: 0.6;
            }

            .date-text {
                opacity: 0.8;
            }
        }

        .article-title {
            font-size: 1.35rem;
            font-weight: 700;
            line-height: 1.4;
            margin-bottom: 8px;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .article-summary {
            font-size: 0.9rem;
            line-height: 1.5;
            opacity: 0.85;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.4);
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .content-footer {
            display: flex;
            align-items: center;
            justify-content: flex-end;
            padding-top: 12px;
            margin-top: auto;

            .views {
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 0.9rem;
                opacity: 0.9;
                background: rgba(255, 255, 255, 0.15);
                backdrop-filter: blur(10px);
                padding: 6px 12px;
                border-radius: 20px;
                text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);

                svg {
                    opacity: 0.8;
                }
            }
        }
    }
}

/* 空状态 */
.empty-state {
    grid-column: span 2;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    gap: 20px;
    text-align: center;

    p {
        font-size: 1.1rem;
    }

    .back-btn {
        padding: 12px 28px;
        border-radius: 25px;
        border: none;
        cursor: pointer;
        font-size: 0.9rem;
        transition: transform 0.2s;

        &:hover {
            transform: scale(1.05);
        }
    }
}

/* 分页 - 优化后的样式 */
.pagination-wrapper {
    margin-top: 48px;
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

:deep(.custom-pagination) {
    /* 基础变量设置 */
    --pagination-bg: var(--card-bg);
    --pagination-border: var(--card-border);
    --pagination-text: var(--text-secondary);
    --pagination-hover-bg: rgba(var(--accent-color-rgb), 0.1);
    --pagination-active-bg: var(--accent-color);
    --pagination-active-text: #ffffff;
    --pagination-disabled: var(--text-tertiary);

    /*  glass morphism 效果 */
    .el-pager {
        display: flex;
        gap: 8px;

        li {
            min-width: 40px;
            height: 40px;
            padding: 0 12px;
            margin: 0;
            background: var(--pagination-bg);
            border: 1px solid var(--pagination-border);
            border-radius: 12px;
            color: var(--pagination-text);
            font-weight: 500;
            font-size: 0.95rem;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
            backdrop-filter: blur(8px);

            /* 悬停状态 */
            &:hover:not(.is-active):not(.disabled) {
                background: var(--pagination-hover-bg);
                border-color: var(--accent-color);
                color: var(--accent-color);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(var(--accent-color-rgb), 0.15);
            }

            /* 激活状态 */
            &.is-active {
                background: var(--pagination-active-bg);
                color: var(--pagination-active-text);
                border-color: var(--pagination-active-bg);
                font-weight: 600;
                transform: scale(1.05);
                box-shadow: 0 4px 16px rgba(var(--accent-color-rgb), 0.35),
                    0 0 0 3px rgba(var(--accent-color-rgb), 0.1);

                &:hover {
                    background: var(--pagination-active-bg);
                    filter: brightness(1.1);
                }
            }

            /* 禁用状态 */
            &.disabled {
                opacity: 0.5;
                cursor: not-allowed;
                background: transparent;
            }
        }
    }

    /* 上一页/下一页按钮 */
    .btn-prev,
    .btn-next {
        min-width: 40px;
        height: 40px;
        padding: 0 12px;
        margin: 0 4px;
        background: var(--pagination-bg);
        border: 1px solid var(--pagination-border);
        border-radius: 12px;
        color: var(--pagination-text);
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        backdrop-filter: blur(8px);

        .el-icon {
            font-size: 16px;
            font-weight: bold;
        }

        &:hover:not(:disabled) {
            background: var(--pagination-hover-bg);
            border-color: var(--accent-color);
            color: var(--accent-color);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(var(--accent-color-rgb), 0.15);
        }

        &:disabled {
            color: var(--pagination-disabled);
            opacity: 0.4;
            cursor: not-allowed;
            background: transparent;
        }
    }

    /* 快速跳转输入框（如果有） */
    .el-pagination__jump {
        color: var(--text-secondary);
        font-weight: 500;
        margin-left: 16px;

        .el-input__inner {
            background: var(--card-bg);
            border: 1px solid var(--card-border);
            border-radius: 8px;
            color: var(--text-primary);
            font-weight: 500;

            &:focus {
                border-color: var(--accent-color);
                box-shadow: 0 0 0 3px rgba(var(--accent-color-rgb), 0.1);
            }
        }
    }

    /* 总条目数 */
    .el-pagination__total {
        color: var(--text-secondary);
        font-weight: 500;
        margin-right: 16px;
    }
}

/* ==================== 侧边栏 ==================== */
.sidebar {
    width: 320px;
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.widget {
    background: var(--widget-bg);
    backdrop-filter: blur(12px);
    border: 1px solid var(--card-border);
    border-radius: 16px;
    padding: 24px;
    transition: all 0.3s;

    &:hover {
        border-color: var(--card-border-hover);
        box-shadow: var(--card-shadow);
    }

    h4 {
        font-size: 1.1rem;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid var(--separator);
    }
}

/* 分类小部件 - 复选框模式 */
.category-widget {
    .widget-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 16px;
        border-bottom: 1px solid var(--separator);

        h4 {
            margin: 0;
            padding: 0;
            border: none;
        }

        .clear-filter {
            font-size: 0.8rem;
            color: var(--accent-color);
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 6px;
            transition: all 0.2s;

            &:hover {
                background: rgba(var(--accent-color), 0.1);
            }
        }
    }

    .category-list {
        display: flex;
        flex-direction: column;
        gap: 8px;

        &.checkbox-mode {
            gap: 4px;
        }
    }

    .category-item {
        display: flex;
        align-items: center;
        padding: 10px 12px;
        border-radius: 10px;
        cursor: pointer;
        transition: all 0.2s;
        font-size: 0.95rem;

        &:hover {
            background: rgba(var(--accent-color), 0.05);
        }

        &.active {
            font-weight: 500;
        }

        // 复选框样式
        &.checkbox-item {
            position: relative;
            padding-left: 40px;

            .checkbox-wrapper {
                position: absolute;
                left: 12px;
                top: 50%;
                transform: translateY(-50%);

                input[type="checkbox"] {
                    position: absolute;
                    opacity: 0;
                    cursor: pointer;
                    width: 0;
                    height: 0;
                }

                .checkmark {
                    position: relative;
                    display: inline-block;
                    width: 18px;
                    height: 18px;
                    border: 2px solid var(--text-tertiary);
                    border-radius: 4px;
                    transition: all 0.2s;

                    &::after {
                        content: '';
                        position: absolute;
                        display: none;
                        left: 5px;
                        top: 1px;
                        width: 4px;
                        height: 9px;
                        border: solid white;
                        border-width: 0 2px 2px 0;
                        transform: rotate(45deg);
                    }
                }

                input:checked~.checkmark {
                    background-color: var(--accent-color);
                    border-color: var(--accent-color);

                    &::after {
                        display: block;
                    }
                }
            }

            .cat-name {
                flex: 1;
            }

            .cat-count {
                font-size: 0.85rem;
                background: rgba(128, 128, 128, 0.1);
                padding: 2px 8px;
                border-radius: 12px;
                margin-left: 8px;
            }
        }
    }
}

/* 个人资料小部件 */
.profile-widget {
    text-align: center;

    .avatar {
        margin-bottom: 16px;

        img {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            border: 3px solid var(--card-border);
            object-fit: cover;
            transition: all 0.3s;

            &:hover {
                transform: scale(1.05);
                border-color: var(--accent-color);
            }
        }
    }

    h4 {
        border-bottom: none;
        padding-bottom: 0;
        margin-bottom: 8px;
    }

    .info {
        font-size: 0.9rem;
        color: var(--text-secondary);
        line-height: 1.5;
        margin-bottom: 16px;
    }

    .slogan-list {
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 8px;
        margin-bottom: 16px;
    }

    .slogan-pill {
        padding: 6px 12px;
        border-radius: 999px;
        background: rgba(var(--accent-color-rgb), 0.12);
        border: 1px solid rgba(var(--accent-color-rgb), 0.18);
        color: var(--text-secondary);
        font-size: 0.82rem;
        line-height: 1;
    }

    .tech-stack {
        display: flex;
        justify-content: center;
        gap: 8px;
        flex-wrap: wrap;

        .badge {
            background: rgba(128, 128, 128, 0.1);
            color: var(--text-secondary);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            transition: all 0.3s;

            &:hover {
                background: var(--accent-color);
                color: #fff;
            }
        }
    }
}

/* 统计小部件 - 带数字动画效果 */
.stats-widget {
    .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 14px 0;
        border-bottom: 1px solid rgba(128, 128, 128, 0.1);
        font-size: 0.95rem;
        color: var(--text-secondary);
        transition: all 0.3s ease;

        &:last-child {
            border-bottom: none;
        }

        &:hover {
            padding-left: 8px;
            color: var(--text-primary);

            .count-up {
                transform: scale(1.1);
                color: var(--text-color);
            }
        }

        span {
            display: flex;
            align-items: center;
            gap: 10px;

            svg {
                opacity: 0.7;
                transition: all 0.3s ease;
            }
        }

        strong {
            color: var(--text-primary);
            font-weight: 700;
            font-size: 1.1rem;
            font-variant-numeric: tabular-nums; // 等宽数字，防止抖动
            transition: all 0.3s ease;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
            letter-spacing: -0.5px;
        }
    }
}

/* 数字变化时的闪烁效果 */
@keyframes numberPulse {
    0% {
        text-shadow: 0 0 0 rgba(var(--accent-color-rgb), 0);
    }

    50% {
        text-shadow: 0 0 10px rgba(var(--accent-color-rgb), 0.3);
    }

    100% {
        text-shadow: 0 0 0 rgba(var(--accent-color-rgb), 0);
    }
}

.count-up {
    animation: numberPulse 0.3s ease;
}

/* 标签云小部件 */
.tags-widget {
    .tags-cloud {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
    }

    span {
        padding: 6px 14px;
        border-radius: 20px;
        font-size: 0.85rem;
        cursor: pointer;
        transition: all 0.3s;
        background: rgba(var(--accent-color), 0.1);
        color: var(--accent-color);

        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
    }
}

/* ==================== 动画 ==================== */
.animate-in {
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);

    &.show {
        opacity: 1;
        transform: translateY(0);
    }
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
    .layout-container {
        flex-direction: column;
    }

    .sidebar {
        width: 100%;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    }

    .category-widget {
        display: none; // 移动端使用下拉选择
    }

    .mobile-categories {
        display: block;
    }
}

@media (max-width: 768px) {
    .search-controls {
        padding: 16px 0;
    }

    .title {
        font-size: 24px;
    }

    .featured-carousel {
        height: 240px;
        margin-bottom: 24px;

        .slide-content {
            left: 20px;
            right: 20px;
            bottom: 24px;

            h3 {
                font-size: 1.25rem;
            }

            p {
                font-size: 0.85rem;
                -webkit-line-clamp: 1;
            }
        }

        .carousel-btn {
            width: 36px;
            height: 36px;

            &.prev-btn {
                left: 10px;
            }

            &.next-btn {
                right: 10px;
            }
        }
    }

    /* 移动端文章卡片调整 */
    .articles-grid,
    .articles-grid.search-mode {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .post-card {
        aspect-ratio: 16 / 9;
        /* 移动端稍微扁一点 */

        .cover-content {
            padding: 16px;

            .meta-row {
                font-size: 0.8rem;
                margin-bottom: 8px;

                .category-tag {
                    padding: 3px 8px;
                }
            }

            .article-title {
                font-size: 1.1rem;
                -webkit-line-clamp: 2;
            }

            .article-summary {
                font-size: 0.85rem;
                -webkit-line-clamp: 1;
            }

            .content-footer {
                .views {
                    font-size: 0.85rem;
                    padding: 4px 10px;
                }
            }
        }

        .top-badge {
            top: 12px;
            right: 12px;
            padding: 4px 10px;
            font-size: 0.7rem;
        }
    }

    .tag-filter {
        .filter-label {
            display: none;
        }

        .tag-list {
            max-width: 100%;
            overflow-x: auto;
            flex-wrap: nowrap;
            padding-bottom: 8px;
            -webkit-overflow-scrolling: touch;

            &::-webkit-scrollbar {
                display: none;
            }
        }
    }

    /* 移动端分类选择器优化 */
    .mobile-categories {
        max-width: 100%;
        padding: 0 4px;
    }

    .mobile-category-select {
        padding: 10px 14px;
        border-radius: 12px;

        .category-icon {
            width: 16px;
            height: 16px;
        }

        select {
            font-size: 0.9rem;
        }

        .dropdown-arrow {
            right: 14px;
            width: 14px;
            height: 14px;
        }
    }

    /* 移动端分页优化 */
    :deep(.custom-pagination) {
        .el-pager li {
            min-width: 36px;
            height: 36px;
            font-size: 0.9rem;
        }

        .btn-prev,
        .btn-next {
            min-width: 36px;
            height: 36px;
        }
    }

    /* 移动端侧边栏调整 */
    .sidebar {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .widget {
        padding: 20px;
    }
}

/* 小屏幕手机优化 */
@media (max-width: 480px) {
    .featured-carousel {
        height: 208px;

        .slide-content {
            left: 16px;
            right: 16px;
            bottom: 18px;

            h3 {
                font-size: 1.08rem;
            }
        }
    }

    .controls-inner {
        padding: 0 16px;
        gap: 12px;
    }

    .search-box {
        .search-input {
            padding: 10px 16px;
            font-size: 0.95rem;
        }

        .search-btn {
            width: 40px;
            height: 40px;
        }
    }

    .post-card {
        aspect-ratio: 16 / 10;

        .cover-content {
            padding: 14px;

            .article-title {
                font-size: 1rem;
            }

            .article-summary {
                display: none;
                /* 超小屏幕隐藏摘要 */
            }
        }
    }

    .content-body {
        padding: 0 16px 40px;
    }
}
</style>
