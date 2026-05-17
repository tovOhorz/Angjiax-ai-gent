import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { title: '登录', public: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/mall/HomeMallView.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'product/:id',
          name: 'product-detail',
          component: () => import('@/views/mall/ProductDetailView.vue'),
          meta: { title: '商品详情' },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfileView.vue'),
          meta: { title: '个人信息' },
        },
        {
          path: 'agents',
          name: 'agents',
          component: () => import('@/views/agents/AgentsHubView.vue'),
          meta: { title: '智能助手' },
        },
        {
          path: 'rag-chat',
          name: 'rag-chat',
          component: () => import('@/views/chat/RagChatView.vue'),
          meta: { title: 'RAG 智能问答助手' },
        },
        {
          path: 'manus-chat',
          name: 'manus-chat',
          component: () => import('@/views/chat/ManusChatView.vue'),
          meta: { title: 'AngjiaxManus 超级智能体' },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/user/UserManageView.vue'),
          meta: { title: '用户管理' },
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('@/views/product/ProductManageView.vue'),
          meta: { title: '商品管理' },
        },
        {
          path: 'coupons',
          name: 'coupons',
          component: () => import('@/views/coupon/CouponManageView.vue'),
          meta: { title: '优惠券管理' },
        },
        {
          path: 'my-cart',
          name: 'my-cart',
          component: () => import('@/views/cart/MyCartView.vue'),
          meta: { title: '我的购物车' },
        },
        {
          path: 'cart-admin',
          name: 'cart-admin',
          component: () => import('@/views/cart/CartAdminView.vue'),
          meta: { title: '购物车管理' },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('@/views/order/MyOrdersView.vue'),
          meta: { title: '我的订单' },
        },
        {
          path: 'order-admin',
          name: 'order-admin',
          component: () => import('@/views/order/OrderManageView.vue'),
          meta: { title: '订单管理（管理员）' },
        },
        {
          path: 'my-coupons',
          name: 'my-coupons',
          component: () => import('@/views/coupon/MyCouponView.vue'),
          meta: { title: '我的优惠券' },
        },
        {
          path: 'contact',
          name: 'contact',
          component: () => import('@/views/contact/ContactView.vue'),
          meta: { title: '联系作者' },
        },
        {
          path: 'welfare',
          name: 'welfare',
          component: () => import('@/views/welfare/WelfareView.vue'),
          meta: { title: '福利中心' },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (!store.bootstrapped) {
    await store.bootstrap()
  }
  if (to.meta.public) {
    if (store.isLoggedIn && to.name === 'login') {
      return { path: '/' }
    }
    return true
  }
  if (!store.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  if (title) document.title = `${title} · Angjiax`
})

export default router
