import type { RouteRecordRaw } from 'vue-router'
import { AccessEnum } from '~@/utils/constant'
import { basicRouteMap } from './router-modules'

export default [
  {
    path: '/dashboard',
    redirect: '/dashboard/analysis',
    name: 'Dashboard',
    meta: {
      title: '仪表盘',
      icon: 'DashboardOutlined',
      access: [AccessEnum.ADMIN],
    },
    component: basicRouteMap.RouteView,
    children: [
      {
        path: '/dashboard/analysis',
        name: 'DashboardAnalysis',
        component: () => import('~/pages/dashboard/analysis/index.vue'),
        meta: {
          title: '首页',
        },
      },

      {
        path: '/dashboard/workplace',
        name: 'DashboardWorkplace',
        component: () => import('~/pages/dashboard/workplace/index.vue'),
        meta: {
          title: '监控页',
        },
      },
      {
        path: '/dashboard/allsitedata',
        name: 'AllSiteData',
        component: () => import('~/pages/dashboard/allsitedata/index.vue'),
        meta: {
          title: '全站数据',
        },
      },
    ],
  },
  {
    path: '/form',
    redirect: '/form/basic-form',
    name: 'Form',
    meta: {
      title: '博客发布',
      icon: 'FormOutlined',
      access: [AccessEnum.ADMIN],
    },
    component: basicRouteMap.RouteView,
    children: [
      {
        path: '/form/basic-form',
        name: 'FormBasic',
        component: () => import('~/pages/form/basic-form/index.vue'),
        meta: {
          title: '博客发布',
          //locale: 'menu.form.basic-form',
        },
      },
      {
        path: '/form/basic-form/:id',
        name: 'edit',
        component: () => import('~/pages/form/basic-form/index.vue'),
        meta: {
          title: '博客编辑',
          hideInMenu: true,
          
        },
      },
    ],
  },
  {
    path: '/menu',
    redirect: '/menu/menu1',
    name: 'Menu',
    meta: {
      title: '管理',
      icon: 'BarsOutlined',
      access: [AccessEnum.ADMIN],
    },
    component: basicRouteMap.RouteView,
    children: [
      {
        path: '/menu/usermanager',
        name: 'usermanager',
        component: () => import('~/pages/menu/UserManager.vue'),
        meta: {
          title: '用户管理',

        },
      },
      {
        path: '/menu/manuscriptmanager',
        name: 'manuscriptmanager',
        component: () => import('~/pages/menu/ManuscriptManager.vue'),
        meta: {
          title: '稿件管理',
        },
      },
      {
        path: '/menu/blogmanager',
        name: 'blogmanager',
        component: () => import('~/pages/menu/BlogManager.vue'),
        meta: {
          title: '博客管理',
        },
      },
      {
        path: '/menu/worksmanager',
        name: 'worksmanager',
        component: () => import('~/pages/menu/WorksManager.vue'),
        meta: {
          title: '作品管理',
        },
      },
      {
        path: '/menu/categorymanager',
        name: 'categorymanager',
        component: () => import('~/pages/menu/CategoryManager.vue'),
        meta: {
          title: '分类管理',
        },
      },
      {
        path: '/menu/workspublish',
        name: 'workspublish',
        component: () => import('~/pages/menu/WorksPublish.vue'),
        meta: {
          title: '发布作品',
          hideInMenu: true,
        },
      },
      {
        path: '/menu/worksedit/:id',
        name: 'worksedit',
        component: () => import('~/pages/menu/WorksEdit.vue'),
        meta: {
          title: '修改作品',
          hideInMenu: true,
        },
      },
      {
        path: '/menu/wallpapermanager',
        name: 'wallpapermanager',
        component: () => import('~/pages/menu/WallpaperManager.vue'),
        meta: {
          title: '壁纸管理',
        },
      },
      {
        path: '/menu/messagemanager',
        name: 'messagemanager',
        component: () => import('~/pages/menu/MessageManager.vue'),
        meta: {
          title: '留言管理',
        },
      },
      {
        path: '/menu/sloganmanager',
        name: 'sloganmanager',
        component: () => import('~/pages/menu/SloganManager.vue'),
        meta: {
          title: '标语管理',
        },
      },
      {
        path: '/menu/recyclebin',
        name: 'recyclebin',
        component: () => import('~/pages/Article/RecycleBin.vue'),
        meta: {
          title: '回收站',
        },
      },
      {
        path: '/menu/comment-manager/:articleId',
        name: 'commentmanager',
        component: () => import('~/pages/menu/CommentManager.vue'),
        meta: {
          title: '评论管理',
          hideInMenu: true,
        },
      },
    ],
  },

  {
    path: '/list',
    redirect: '/list/card-list',
    name: 'List',
    meta: {
      title: '博客列表',
      icon: 'TableOutlined',
      access: [AccessEnum.ADMIN],
    },
    component: basicRouteMap.RouteView,
    children: [
      {
        path: '/list/card-list',
        name: 'CardList',
        component: () => import('~/pages/list/card-list.vue'),
        meta: {
          title: '博客列表',
        },
      },
      
      {
        path: '/list/search-list',
        name: 'SearchList',
        component: () => import('~/pages/list/search-list/index.vue'),
        meta: {
          title: '搜索',
        },
        redirect: '/list/search-list/articles',
        children: [
          {
            path: '/list/search-list/articles',
            name: 'SearchListArticles',
            component: () => import('~/pages/list/search-list/articles.vue'),
            meta: {
              title: '搜索博客',
              //locale: 'menu.list.search-list.articles',
            },
          },
        ],
      },
    ],
  },
  {
    path: '/articledetail/:id',
    name: 'ArticleDetail',
    component: () => import('~/pages/Article/ArticleDetail.vue'),
    meta: {
      title: '博客详情',
      hideInMenu: true,
      access: [AccessEnum.ADMIN],
    }

  },
  {
    path: '/userdetail/:id',
    name: 'UserDetail',
    component: () => import('~/pages/User/userDetail.vue'),
    meta: {
      title: '用户信息',
      hideInMenu: true,
      access: [AccessEnum.ADMIN],
    }
  },
  {
    path: '/useredit/:id',
    name: 'UserEdit',
    component: () => import('~/pages/User/userEdit.vue'),
    meta: {
      title: '编辑用户资料',
      hideInMenu: true,
      access: [AccessEnum.ADMIN],
    }
  },
  {
    path: '/account',
    redirect: '/account/settings',
    name: 'Account',
    meta: {
      title: '个人页',
      icon: 'UserOutlined',
      locale: 'menu.account',
      access: [AccessEnum.ADMIN],
    },
    component: basicRouteMap.RouteView,
    children: [
      {
        path: '/account/settings',
        name: 'AccountSettings',
        component: () => import('~/pages/account/settings.vue'),
        meta: {
          title: '个人设置',
          locale: 'menu.account.settings',
        },
      },
    ],
  },
] as RouteRecordRaw[]
