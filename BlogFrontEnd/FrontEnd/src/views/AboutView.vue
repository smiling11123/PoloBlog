<template>
    <div class="about-container">
        <div class="main-layout">
            <!-- 左侧内容 -->
            <div class="left-content">
                <section class="profile-card" v-if="authInfo">
                    <div class="avatar-wrapper">
                        <img
                            :src="getOptimizedImageUrl(authInfo.avatar, 'sm')"
                            :srcset="buildImageSrcSet(authInfo.avatar)"
                            sizes="160px"
                            alt="avatar"
                            class="avatar"
                            loading="lazy"
                            decoding="async"
                            @error="fallbackToOriginalImage($event, authInfo.avatar)"
                        />
                    </div>
                    <h1 class="username"><i><strong>{{ authInfo.userName }}</strong></i></h1>

                    <!-- 社交链接区域 -->
                    <div class="social-links">
                        <a href="https://github.com/smiling11123" target="_blank" class="social-btn github">
                            <svg class="icon" viewBox="0 0 1024 1024" width="20" height="20">
                                <path
                                    d="M512 42.666667A464.64 464.64 0 0 0 42.666667 502.186667 460.373333 460.373333 0 0 0 363.52 938.666667c23.466667 4.266667 32-9.813333 32-22.186667v-78.08c-130.56 27.733333-158.293333-61.44-158.293333-61.44a122.026667 122.026667 0 0 0-52.053334-67.413333c-42.666667-28.16 3.413333-27.733333 3.413334-27.733334a98.56 98.56 0 0 1 71.68 47.36 101.12 101.12 0 0 0 136.533333 37.973334 99.413333 99.413333 0 0 1 29.866667-61.44c-104.106667-11.52-213.333333-50.773333-213.333334-226.986667a177.066667 177.066667 0 0 1 47.36-124.16 161.28 161.28 0 0 1 4.693334-121.173333s39.68-12.373333 128 46.933333a455.68 455.68 0 0 1 234.666666 0c89.6-59.306667 128-46.933333 128-46.933333a161.28 161.28 0 0 1 4.693334 121.173333A177.066667 177.066667 0 0 1 810.666667 477.866667c0 176.64-110.08 215.466667-213.333334 226.986666a106.666667 106.666667 0 0 1 32 85.333334v125.866666c0 14.933333 8.533333 26.88 32 22.186667A460.8 460.8 0 0 0 981.333333 502.186667 464.64 464.64 0 0 0 512 42.666667"
                                    fill="currentColor"></path>
                            </svg>
                            <span>GitHub</span>
                        </a>

                        <a href="https://b23.tv/1e6TAcC" target="_blank" class="social-btn bilibili">
                            <svg class="icon" viewBox="0 0 1129 1024" width="20" height="20">
                                <path
                                    d="M234.909 9.656a80.468 80.468 0 0 1 68.398 0 167.374 167.374 0 0 1 41.843 30.578l160.937 140.82h115.07l160.936-140.82a168.983 168.983 0 0 1 41.843-30.578A80.468 80.468 0 0 1 930.96 76.445a80.468 80.468 0 0 1-17.703 53.914 449.818 449.818 0 0 1-35.406 32.187 232.553 232.553 0 0 1-22.531 18.508h100.585a170.593 170.593 0 0 1 118.289 53.109 171.397 171.397 0 0 1 53.914 118.288v462.693a325.897 325.897 0 0 1-4.024 70.007 178.64 178.64 0 0 1-80.468 112.656 173.007 173.007 0 0 1-92.539 25.75h-738.7a341.186 341.186 0 0 1-72.421-4.024A177.835 177.835 0 0 1 28.91 939.065a172.202 172.202 0 0 1-27.36-92.539V388.662a360.498 360.498 0 0 1 0-66.789A177.03 177.03 0 0 1 162.487 178.64h105.414c-16.899-12.07-31.383-26.555-46.672-39.43a80.468 80.468 0 0 1-25.75-65.984 80.468 80.468 0 0 1 39.43-63.57M216.4 321.873a80.468 80.468 0 0 0-63.57 57.937 108.632 108.632 0 0 0 0 30.578v380.615a80.468 80.468 0 0 0 55.523 80.469 106.218 106.218 0 0 0 34.601 5.632h654.208a80.468 80.468 0 0 0 76.444-47.476 112.656 112.656 0 0 0 8.047-53.109v-354.06a135.187 135.187 0 0 0 0-38.625 80.468 80.468 0 0 0-52.304-54.719 129.554 129.554 0 0 0-49.89-7.242H254.22a268.764 268.764 0 0 0-37.82 0z m0 0"
                                    fill="#20B0E3"></path>
                                <path
                                    d="M348.369 447.404a80.468 80.468 0 0 1 55.523 18.507 80.468 80.468 0 0 1 28.164 59.547v80.468a80.468 80.468 0 0 1-16.094 51.5 80.468 80.468 0 0 1-131.968-9.656 104.609 104.609 0 0 1-10.46-54.719v-80.468a80.468 80.468 0 0 1 70.007-67.593z m416.02 0a80.468 80.468 0 0 1 86.102 75.64v80.468a94.148 94.148 0 0 1-12.07 53.11 80.468 80.468 0 0 1-132.773 0 95.757 95.757 0 0 1-12.875-57.133V519.02a80.468 80.468 0 0 1 70.007-70.812z m0 0"
                                    fill="#20B0E3"></path>
                            </svg>
                            <span>Bilibili</span>
                        </a>
                    </div>
                </section>

                <section class="details-section" v-if="authInfo">
                    <h2 class="section-title">关于我</h2>
                    <ArticleContent :content="authInfo.profile" class="profile-article" />
                </section>

                <section class="works-section">
                    <h2 class="section-title">个人作品</h2>

                    <!-- 使用 InfiniteMenu 展示作品 -->
                    <div class="infinite-menu-container">
                        <div class="menu-wrapper">
                            <InfiniteMenu :items="displayItems" :scale="scaleFactor"
                                @active-item-change="handleActiveItemChange" @movement-change="handleMovementChange" />
                        </div>

                        <!-- 按钮区域 -->
                        <div v-if="activeItem && !isMoving" class="visit-actions">
                            <!-- 访问按钮 -->
                            <div class="visit-circular-button" @click="handleVisitClick" title="查看详情">
                                <svg class="visit-icon" viewBox="0 0 24 24" width="20" height="20">
                                    <path d="M7 17L17 7M17 7H7M17 7V17" stroke="currentColor" stroke-width="2"
                                        stroke-linecap="round" stroke-linejoin="round" fill="none" />
                                </svg>
                            </div>
                            <!-- 复制链接按钮 -->
                            <div class="copy-button" @click="handleCopyClick" title="复制链接">
                                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                                </svg>
                            </div>
                        </div>

                        <!-- 底部提示 -->
                        <div class="menu-overlay">
                            <div class="hint-content">
                                <svg class="hint-icon" viewBox="0 0 24 24" width="16" height="16">
                                    <path
                                        d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                                        fill="currentColor" />
                                </svg>
                                <span>拖拽探索 · 松手查看</span>
                            </div>
                        </div>
                    </div>
                </section>
            </div>

            <!-- 右侧技能卡片 -->
            <aside class="right-sidebar">
                <div class="skills-card animate-in">
                    <div class="card-header">
                        <div class="header-icon">
                            <svg viewBox="0 0 24 24" width="20" height="20">
                                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" :stroke="themStore.them.textColor"
                                    stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" />
                            </svg>
                        </div>
                        <h3 class="card-title">技能</h3>
                    </div>

                    <div class="skills-cloud">
                        <IconCloud :images="imageUrls" />
                    </div>

                    <div class="skills-tags">
                        <span v-for="slug in slugs" :key="slug" class="skill-tag">
                            {{ formatSkillName(slug) }}
                        </span>
                    </div>
                </div>
            </aside>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useThemStore } from '@/stores/them'
import { getAuthInfo } from '@/api/authInfo'
import { getWorksList } from '@/api/works'
import MarkdownIt from 'markdown-it'
import InfiniteMenu from '@/components/Vue-bits/InfiniteMenu.vue'
import IconCloud from '@/components/Inspira/IconCloud.vue'
import ArticleContent from '@/components/Article/ArticleContent.vue'
import { buildImageSrcSet, fallbackToOriginalImage, getOptimizedImageUrl } from '@/utils/image'

const router = useRouter()
const themStore = useThemStore()
// const md = new MarkdownIt() // No longer needed
const scaleFactor = ref<number>(2.2)

// InfiniteMenu 状态
const activeItem = ref<any>(null)
const isMoving = ref(false)

// function removed

interface AuthInfo {
    userName: string;
    avatar: string;
    profile: string;
    slogan: string;
}

interface Work {
    id: string;
    title: string;
    content: string;
    thumbnail: string;
    address: string;
    createTime: string;
    link?: string;
}

const authInfo = ref<AuthInfo | null>(null)
const worksList = ref<Work[]>([])
const page = ref(1)
const hasMore = ref(true)

// 技能图标配置
const slugs = [
    "typescript", "javascript", "java", "react", "html5", "css3",
    "nodedotjs", "nginx", "vercel", "docker", "git", "github",
    "gitlab", "visualstudiocode", "androidstudio", "figma", "gitee"
]

const imageUrls = computed(() =>
    slugs.map((slug) => `https://cdn.simpleicons.org/${slug}/${slug}`)
)

// 格式化技能名称显示
const formatSkillName = (slug: string) => {
    const nameMap: Record<string, string> = {
        'typescript': 'TypeScript',
        'javascript': 'JavaScript',
        'nodedotjs': 'Node.js',
        'visualstudiocode': 'VS Code',
        'androidstudio': 'Android Studio',
        'html5': 'HTML5',
        'css3': 'CSS3'
    }
    return nameMap[slug] || slug.charAt(0).toUpperCase() + slug.slice(1)
}

// 将作品数据转换为 InfiniteMenu 格式
const displayItems = computed(() => {
    if (worksList.value.length > 0) {
        return worksList.value.map(work => ({
            image: work.thumbnail,
            title: work.title,
            description: work.content,
            link: work.address || `/works/${work.id}`
        }))
    }
    // 默认展示项
    return []
})

// 处理 InfiniteMenu 事件
const handleActiveItemChange = (item: any) => {
    activeItem.value = item
}

const handleMovementChange = (moving: boolean) => {
    isMoving.value = moving
}

// 处理访问按钮点击
const handleVisitClick = () => {
    if (!activeItem.value?.link || activeItem.value.link === '#') return

    if (activeItem.value.link.startsWith('http')) {
        window.open(activeItem.value.link, '_blank')
    } else {
        // 内部路由跳转
        router.push(activeItem.value.link)
    }
}

// 处理复制链接点击
const handleCopyClick = async () => {
    if (!activeItem.value?.link) return
    try {
        await navigator.clipboard.writeText(activeItem.value.link)
        ElMessage.success('链接已复制到剪贴板')
    } catch (err) {
        ElMessage.error('复制失败')
    }
}

const fetchData = async () => {
    try {
        const authRes = await getAuthInfo()
        authInfo.value = authRes.data
        const worksRes = await getWorksList({ page: page.value, size: 6 })
        worksList.value = worksRes.data.records
        if (worksRes.data.records.length < 6) hasMore.value = false
    } catch (error) {
        console.error("加载关于页面失败", error)
    }
}

const loadWorks = async () => {
    page.value++
    const res = await getWorksList({ page: page.value, size: 6 })
    if (res.data.records.length > 0) {
        worksList.value.push(...res.data.records)
    } else {
        hasMore.value = false
    }
}

const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
    fetchData()
    // 自动跳转到内容区域
    setTimeout(() => {
      const contentElement = document.querySelector('.main-layout')
      if (contentElement) {
        contentElement.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
})
</script>

<style scoped lang="scss">
.profile-content {

    :deep(h1),
    :deep(h2),
    :deep(h3) {
        color: v-bind('themStore.them.textColor');
        margin: 1.5rem 0 1rem;
    }

    :deep(p) {
        margin-bottom: 1rem;
    }

    :deep(a) {
        color: v-bind('themStore.them.accentColor');
        text-decoration: none;

        &:hover {
            text-decoration: underline;
        }
    }

    :deep(ul),
    :deep(ol) {
        padding-left: 1.5rem;
        margin-bottom: 1rem;
    }

    :deep(code) {
        background: rgba(0, 0, 0, 0.1);
        padding: 2px 4px;
        border-radius: 4px;
        font-family: monospace;
    }
}

.about-container {
    max-width: 1400px;
    margin: 0 auto;
    padding-bottom: 50px;

    // 注入 CSS 变量供 InfiniteMenu 使用
    --theme-text-color: v-bind('themStore.them.textColor');
    --theme-text-secondary: v-bind('themStore.them.textSecondary');
    --theme-accent-color: v-bind('themStore.them.accentColor');
    --theme-card-border: v-bind('themStore.them.cardBorder');
    --theme-card-shadow: v-bind('themStore.them.cardShadow');
}

/* 主布局 - 左右分栏 */
.main-layout {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 30px;
    align-items: start;

    @media (max-width: 1024px) {
        grid-template-columns: 1fr;
    }
}

.left-content {
    display: flex;
    flex-direction: column;
    gap: 40px;
}

/* 右侧边栏 */
.right-sidebar {
    position: sticky;
    top: 100px;
    height: fit-content;

    @media (max-width: 1024px) {
        position: relative;
        top: 0;
        order: -1;
    }
}

/* 技能卡片样式 */
.skills-card {
    background: v-bind('themStore.them.cardBg');
    border: 1px solid v-bind('themStore.them.cardBorder');
    border-radius: 20px;
    padding: 24px;
    box-shadow: v-bind('themStore.them.cardShadow');
    backdrop-filter: blur(10px);
    overflow: hidden;
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    &:hover {
        transform: translateY(-4px);
        box-shadow: v-bind('themStore.them.cardShadowHover');
    }

    .card-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid v-bind('themStore.them.cardBorder');

        .header-icon {
            width: 40px;
            height: 40px;
            border-radius: 12px;
            background: v-bind('themStore.them.wavesColor2');
            display: flex;
            align-items: center;
            justify-content: center;
            color: v-bind('themStore.them.accentColor');
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: v-bind('themStore.them.textColor');
            margin: 0;
        }
    }

    .skills-cloud {
        width: 100%;
        height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;

        :deep(canvas) {
            max-width: 100%;
            max-height: 100%;
        }
    }

    .skills-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        justify-content: center;

        .skill-tag {
            padding: 6px 12px;
            background: v-bind('themStore.them.wavesColor2');
            border: 1px solid v-bind('themStore.them.cardBorder');
            border-radius: 20px;
            font-size: 12px;
            color: v-bind('themStore.them.textSecondary');
            transition: all 0.3s ease;
            cursor: default;

            &:hover {
                background: v-bind('themStore.them.accentColor');
                color: white;
                border-color: v-bind('themStore.them.accentColor');
                transform: translateY(-2px);
            }
        }
    }
}

/* 入场动画 */
.animate-in {
    animation: slideInRight 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
    opacity: 0;
}

@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(30px);
    }

    to {
        opacity: 1;
        transform: translateX(0);
    }
}

/* 个人卡片样式 */
.profile-card {
    text-align: center;
    padding: 40px;
    background: v-bind('themStore.them.cardBg');
    border: 1px solid v-bind('themStore.them.cardBorder');
    border-radius: 20px;
    box-shadow: v-bind('themStore.them.cardShadow');
    backdrop-filter: blur(10px);

    .avatar-wrapper {
        width: 120px;
        height: 120px;
        margin: 0 auto 20px;
        border-radius: 50%;
        padding: 5px;
        border: 2px solid v-bind('themStore.them.accentColor');

        .avatar {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            object-fit: cover;
        }
    }

    .username {
        font-size: 2rem;
        color: v-bind('themStore.them.textColor');
        margin-bottom: 20px;
    }

    .social-links {
        display: flex;
        justify-content: center;
        gap: 15px;
        margin-top: 10px;

        .social-btn {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 8px 18px;
            border-radius: 20px;
            background: v-bind('themStore.them.wavesColor2');
            backdrop-filter: blur(8px);
            border: 1px solid v-bind('themStore.them.cardBorder');
            text-decoration: none;
            color: v-bind('themStore.them.textColor');
            font-size: 14px;
            transition: all 0.3s ease;
            cursor: pointer;

            &:hover {
                background: v-bind('themStore.them.cardBgHover');
                transform: translateY(-2px);
                box-shadow: v-bind('themStore.them.cardShadowHover');
            }

            &.github:hover {
                color: #fff;
                background: #24292e;
                border-color: #24292e;
            }

            &.bilibili:hover {
                color: #fff;
                background: #20B0E3;
                border-color: #20B0E3;

                svg path {
                    fill: #fff;
                }
            }

            .icon {
                flex-shrink: 0;
            }

            span {
                font-weight: 500;
            }
        }
    }
}

/* 通用标题与简介 */
.section-title {
    font-size: 1.5rem;
    color: v-bind('themStore.them.textColor');
    margin-bottom: 20px;
    padding-left: 10px;
    border-left: 4px solid v-bind('themStore.them.accentColor');
}

.details-section {
    background: transparent;
    border-radius: 15px;
    color: v-bind('themStore.them.textSecondary');
    line-height: 1.8;

    .profile-article {
        /* Restored background and padding by removing overrides */
        margin-top: 10px;
    }
}

/* InfiniteMenu 容器样式 - 半透明玻璃拟态 */
.infinite-menu-container {
    position: relative;
    width: 100%;
    height: 520px;
    background: v-bind('themStore.them.contentBgTrans');
    border: 1px solid v-bind('themStore.them.cardBorder');
    border-radius: 20px;
    overflow: hidden;
    box-shadow: v-bind('themStore.them.cardShadow');
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);

    .menu-wrapper {
        width: 100%;
        height: 100%;
        background: transparent;

        :deep(canvas) {
            width: 100% !important;
            height: 100% !important;
            border-radius: 20px;
            background: transparent !important;
        }
    }

    /* 访问与复制按钮统一风格 */
    .visit-circular-button,
    .copy-button {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        background: v-bind('themStore.them.wavesColor2');
        border: 1px solid v-bind('themStore.them.cardBorder');
        border-radius: 50%;
        color: v-bind('themStore.them.textColor');
        cursor: pointer;
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        box-shadow: v-bind('themStore.them.cardShadow');
        transition: all 0.3s ease;

        &:hover {
            transform: scale(1.1);
            background: v-bind('themStore.them.cardBgHover');
            border-color: v-bind('themStore.them.accentColor');
            color: v-bind('themStore.them.accentColor');
        }

        &:active {
            transform: scale(0.95);
        }
    }

    /* 底部提示 */
    .menu-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 24px;

        background: linear-gradient(to top,
                v-bind('themStore.them.contentBgTrans') 0%,
                rgba(0, 0, 0, 0) 100%);

        pointer-events: none;
        display: flex;
        justify-content: center;
        align-items: flex-end;

        .hint-content {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;

            background: v-bind('themStore.them.wavesColor2');
            border: 1px solid v-bind('themStore.them.cardBorder');
            border-radius: 24px;

            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            box-shadow: v-bind('themStore.them.cardShadow');

            .hint-icon {
                color: v-bind('themStore.them.accentColor');
                opacity: 0.9;
            }

            span {
                color: v-bind('themStore.them.textSecondary');
                font-size: 13px;
                font-weight: 500;
                letter-spacing: 1px;
            }
        }
    }
}
</style>
