import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    redirect: '/admin/article',
    children: [
      { path: 'article', name: 'ArticleList', component: () => import('@/views/admin/ArticleList.vue') },
      { path: 'article/edit/:id?', name: 'ArticleEdit', component: () => import('@/views/admin/ArticleEdit.vue') },
      { path: 'category', name: 'Category', component: () => import('@/views/admin/Category.vue') },
      { path: 'tag', name: 'Tag', component: () => import('@/views/admin/Tag.vue') },
      { path: 'comment', name: 'CommentManage', component: () => import('@/views/admin/CommentManage.vue') },
      { path: 'friend-links', name: 'FriendLinkManage', component: () => import('@/views/admin/FriendLinkManage.vue') },
      { path: 'settings', name: 'Settings', component: () => import('@/views/admin/Settings.vue') },
    ],
  },
  {
    path: '/visitor',
    component: () => import('@/views/public/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/public/Home.vue') },
      { path: 'article/:id', name: 'ArticleDetail', component: () => import('@/views/public/ArticleDetail.vue') },
      { path: 'category/:id', name: 'PublicCategory', component: () => import('@/views/public/Home.vue') },
      { path: 'tag/:id', name: 'PublicTag', component: () => import('@/views/public/Home.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/public/Home.vue') },
      { path: 'about', name: 'About', component: () => import('@/views/public/About.vue') },
      { path: 'links', name: 'FriendLinks', component: () => import('@/views/public/FriendLinks.vue') },
      { path: 'ai', name: 'AiSearch', component: () => import('@/views/public/AiSearch.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/public/Profile.vue'), meta: { needAuth: true } },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  const role = userInfo?.role || 'guest'

  if ((to.path.startsWith('/admin') || to.meta.needAuth) && !token) return '/login'
  if (to.path.startsWith('/admin') && role !== 'admin') return '/visitor'
  if (to.path === '/login' && token) {
    return role === 'admin' ? '/admin' : '/visitor'
  }
})

export default router
