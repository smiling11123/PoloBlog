<template>
    <div class="category-page">
        <!-- 页面标题区 -->
        <div class="page-header" :style="{ borderBottomColor: themStore.them.cardBorder }">
            <div class="header-inner">
                <h1 :style="{ color: themStore.them.textColor }">
                    <span class="title-icon">📂</span>
                    文章分类
                </h1>
                <p :style="{ color: themStore.them.textSecondary }">
                    探索不同领域的知识宝库，共 {{ allCategories.length }} 个分类
                </p>
            </div>
        </div>

        <!-- 内容区域 -->
        <div class="category-content">
            <!-- 热门分类 -->
            <section class="hot-categories" v-if="hotCategories.length > 0">
                <h2 :style="{ color: themStore.them.textColor }">
                    
                    热门分类
                </h2>
                <div class="category-grid">
                    <div v-for="cat in hotCategories" :key="cat.id" class="category-card hot" :style="{
                        backgroundColor: themStore.them.cardBg,
                        borderColor: themStore.them.cardBorder,
                        boxShadow: themStore.them.cardShadow
                    }" @mouseenter="hoveredCategory = cat.id" @mouseleave="hoveredCategory = null"
                        :class="{ hover: hoveredCategory === cat.id }" @click="goToCategory(cat.id)">
                        <div class="card-bg-image" v-if="cat.thumbnail">
                            <img
                                :src="getOptimizedImageUrl(cat.thumbnail, 'md')"
                                :srcset="buildImageSrcSet(cat.thumbnail)"
                                sizes="(max-width: 768px) 100vw, 320px"
                                loading="lazy"
                                decoding="async"
                                @error="fallbackToOriginalImage($event, cat.thumbnail)"
                            />
                            <div class="overlay"></div>
                        </div>

                        <div class="card-icon" :style="{ backgroundColor: themStore.them.accentColor }">
                            {{ cat.name.charAt(0) }}
                        </div>

                        <h3 :style="{ color: themStore.them.textColor }">{{ cat.name }}</h3>
                        <p class="description" :style="{ color: themStore.them.textSecondary }">
                            {{ cat.description || '暂无描述' }}
                        </p>

                        <div class="stats" :style="{ color: themStore.them.textTertiary }">
                            <span class="stat-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2">
                                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                                    <circle cx="12" cy="12" r="3"></circle>
                                </svg>
                                {{ cat.viewCount || 0 }} 浏览
                            </span>
                        </div>

                        <div class="arrow-icon" :style="{ color: themStore.them.accentColor }">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path d="M5 12h14"></path>
                                <path d="m12 5 7 7-7 7"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 全部分类 - 扁平网格布局 -->
            <section class="all-categories" v-if="allCategories.length > 0">
                <h2 :style="{ color: themStore.them.textColor }">
                    
                    全部分类
                </h2>

                <!-- 扁平网格展示所有分类 -->
                <div class="category-grid flat">
                    <div v-for="cat in allCategories" :key="cat.id" class="category-card" :style="{
                        backgroundColor: themStore.them.cardBg,
                        borderColor: themStore.them.cardBorder,
                        boxShadow: themStore.them.cardShadow
                    }" @mouseenter="hoveredAllCategory = cat.id" @mouseleave="hoveredAllCategory = null"
                        :class="{ hover: hoveredAllCategory === cat.id }" @click="goToCategory(cat.id)">
                        <div class="card-bg-image" v-if="cat.thumbnail">
                            <img
                                :src="getOptimizedImageUrl(cat.thumbnail, 'md')"
                                :srcset="buildImageSrcSet(cat.thumbnail)"
                                sizes="(max-width: 768px) 100vw, 320px"
                                loading="lazy"
                                decoding="async"
                                @error="fallbackToOriginalImage($event, cat.thumbnail)"
                            />
                            <div class="overlay"></div>
                        </div>

                        <div class="card-icon" :style="{ backgroundColor: themStore.them.accentColor }">
                            {{ cat.name.charAt(0) }}
                        </div>

                        <h3 :style="{ color: themStore.them.textColor }">{{ cat.name }}</h3>
                        <p class="description" :style="{ color: themStore.them.textSecondary }">
                            {{ cat.description || '暂无描述' }}
                        </p>

                        <div class="stats" :style="{ color: themStore.them.textTertiary }">
                            <span class="stat-item">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2">
                                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"></path>
                                    <circle cx="12" cy="12" r="3"></circle>
                                </svg>
                                {{ cat.viewCount || 0 }} 浏览
                            </span>
                        </div>

                        <div class="arrow-icon" :style="{ color: themStore.them.accentColor }">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                stroke-width="2">
                                <path d="M5 12h14"></path>
                                <path d="m12 5 7 7-7 7"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            </section>

            <!-- 空状态 -->
            <section v-else class="empty-state">
                <div class="empty-content" :style="{ color: themStore.them.textSecondary }">
                    <span class="empty-icon">📭</span>
                    <p>暂无分类数据</p>
                </div>
            </section>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemStore } from '@/stores/them'
import { getCategoryHotList, getCategoryList } from '@/api/category'
import { buildImageSrcSet, fallbackToOriginalImage, getOptimizedImageUrl } from '@/utils/image'

const themStore = useThemStore()
const router = useRouter()

const allCategories = ref<any[]>([])
const hotCategories = ref<any[]>([])
const hoveredCategory = ref<number | null>(null)
const hoveredAllCategory = ref<number | null>(null)

const goToCategory = (categoryId: number) => {
    router.push({
        name: 'categoryArticles',
        params: { id: categoryId.toString() }
    })
}

onMounted(async () => {
    // 加载热门分类
    try {
        const hotRes = await getCategoryHotList() as any
        if (hotRes.code === 200) {
            hotCategories.value = hotRes.data
        }
    } catch (error) {
        console.error('加载热门分类失败:', error)
    }

    // 加载全部分类
    try {
        const allRes = await getCategoryList() as any
        console.log('分类API响应:', allRes) // 调试日志

        // 兼容多种响应格式
        let list = []
        if (allRes.code === 200 || allRes.code === 0) {
            list = Array.isArray(allRes.data) ? allRes.data : (allRes.data?.list || allRes.data?.records || [])
        }

        allCategories.value = list
        console.log('加载分类数量:', list.length) // 调试日志
        setTimeout(() => {
            const contentElement = document.querySelector('.category-page')
            if (contentElement) {
                contentElement.scrollIntoView({ behavior: 'smooth' })
            }
        }, 100)
    } catch (error) {
        console.error('加载分类失败:', error)
    }
})
</script>

<style scoped>
.category-page {
    min-height: 100vh;
}

/* 页面标题区 */
.page-header {
    padding: 40px 20px;
    border-bottom: 1px solid;
    margin-bottom: 40px;
}

.header-inner {
    max-width: 1400px;
    margin: 0 auto;
    text-align: center;
}

.page-header h1 {
    font-size: 2.5rem;
    font-weight: 700;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 15px;
}

.title-icon {
    font-size: 2rem;
}

.page-header p {
    font-size: 1.1rem;
    opacity: 0.8;
}

/* 内容区域 */
.category-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px 60px;
    display: flex;
    flex-direction: column;
    gap: 60px;
}

section h2 {
    font-size: 1.8rem;
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.section-icon {
    font-size: 1.5rem;
}

/* 分类网格 */
.category-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
}

.category-card {
    position: relative;
    border-radius: 16px;
    border: 1px solid;
    padding: 20px;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 160px;
}

.category-card.hover {
    background-color: v-bind('themStore.them.cardBgHover') !important;
    border-color: v-bind('themStore.them.cardBorderHover') !important;
    box-shadow: v-bind('themStore.them.cardShadowHover') !important;
    transform: translateY(-8px) scale(1.02);
}

.card-bg-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
}

.card-bg-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    opacity: 0.3;
}

.card-bg-image .overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(to bottom, transparent 0%, v-bind('themStore.them.cardBg') 100%);
}

.category-card>*:not(.card-bg-image) {
    position: relative;
    z-index: 1;
}

.card-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.25rem;
    font-weight: 700;
    color: white;
}

.category-card h3 {
    font-size: 1.2rem;
    font-weight: 600;
}

.description {
    font-size: 0.85rem;
    line-height: 1.5;
    flex: 1;
}

.stats {
    display: flex;
    gap: 15px;
    font-size: 0.85rem;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 5px;
}

.arrow-icon {
    position: absolute;
    right: 20px;
    bottom: 20px;
    opacity: 0;
    transform: translateX(-10px);
    transition: all 0.3s;
}

.category-card.hover .arrow-icon {
    opacity: 1;
    transform: translateX(0);
}

/* 空状态 */
.empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
}

.empty-content {
    text-align: center;
    font-size: 1.2rem;
}

.empty-icon {
    font-size: 4rem;
    display: block;
    margin-bottom: 20px;
}

@media (max-width: 768px) {
    .page-header h1 {
        font-size: 1.8rem;
    }

    .category-grid {
        grid-template-columns: 1fr;
    }

    .category-content {
        padding: 0 15px 40px;
    }
}
</style>
