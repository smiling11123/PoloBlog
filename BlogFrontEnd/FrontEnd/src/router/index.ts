import { createRouter, createWebHistory } from 'vue-router'
// 引入布局
import MainLayout from '@/layout/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 处理滚动行为：切换页面时滚动到顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      // 因为你的布局是视差滚动，实际内容在下方 100vh 处
      // 如果你想让用户切页面后看到 header，就 top: 0
      // 如果想直接看内容，可能需要计算高度，一般建议 top: 0
      return { top: 0 }
    }
  },
  routes: [
    {
      path: '/',
      component: MainLayout, // 父路由使用布局组件
      children: [
        {
          path: '', // 默认子路由 (首页)
          name: 'home',
          component: () => import('@/views/HomeView.vue')
        },
        {
          path: '/article/:id', // 文章详情页
          name: 'article',
          component: () => import('@/views/ArticleView.vue')
        },
        {
          path: '/works/:id', // 作品详情页
          name: 'worksDetail',
          component: () => import('@/views/WorksDetail.vue')
        },
        {
          path: '/about', // 关于页
          name: 'about',
          component: () => import('@/views/AboutView.vue')
        },
        {
          path: '/archive', // 归档
          name: 'archive',
          component: () => import('@/views/Archive.vue')
        },
        {
          path: '/categories', // 分类页面
          name: 'categories',
          component: () => import('@/views/CategoryView.vue')
        },
        {
          path: '/category/:id', // 分类文章列表页面（使用路径参数更RESTful）
          name: 'categoryArticles',
          component: () => import('@/views/ArticleListView.vue')
        },
        {
          path: '/tag/:tagName', // 标签文章列表（可选扩展）
          name: 'tagArticles',
          component: () => import('@/views/ArticleListView.vue')
        }
      ]
    },
  ]
})

export default router
