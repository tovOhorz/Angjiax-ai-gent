<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  House,
  Cpu,
  User,
  Goods,
  Ticket,
  ShoppingCart,
  List,
  Message,
  Search,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const searchInput = ref('')

watch(
  () => route.path,
  () => {
    if (route.path === '/') {
      const q = typeof route.query.q === 'string' ? route.query.q : ''
      searchInput.value = q
    }
  },
  { immediate: true },
)

/** 左侧：不含个人购物车、个人优惠券（仅从顶栏进入） */
const navItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/agents', label: '智能助手', icon: Cpu },
  { path: '/users', label: '用户管理', icon: User },
  { path: '/products', label: '商品管理', icon: Goods },
  { path: '/coupons', label: '优惠券管理', icon: Ticket },
  { path: '/cart-admin', label: '购物车管理', icon: ShoppingCart },
  { path: '/orders', label: '订单', icon: List },
  { path: '/contact', label: '联系作者', icon: Message },
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(`${path}/`)
}

function goHome() {
  router.push('/')
}

function submitSearch() {
  router.push({ path: '/', query: { ...route.query, q: searchInput.value.trim() || undefined } })
}

const avatarSrc = computed(() => user.value?.userAvatar || undefined)

async function logout() {
  await userStore.logout()
  router.replace('/login')
}

function goProfile() {
  router.push('/profile')
}
</script>

<template>
  <div class="shell">
    <header class="top-bar">
      <div class="top-inner">
        <button type="button" class="brand" @click="goHome">
          <span class="brand-icon" aria-hidden="true">
            <svg viewBox="0 0 32 32" width="26" height="26" fill="none">
              <rect width="32" height="32" rx="8" fill="#4f46e5" />
              <path
                d="M9 22V10l7 8 7-8v12"
                stroke="#fff"
                stroke-width="2.2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
          <span class="brand-text">Angjiax</span>
        </button>

        <nav class="top-nav">
          <el-button text class="nav-btn" :class="{ active: route.path === '/' }" @click="goHome">首页</el-button>
        </nav>

        <div class="search-wrap">
          <el-input
            v-model="searchInput"
            placeholder="搜索商品（模糊匹配）"
            clearable
            class="search-input"
            @keyup.enter="submitSearch"
          >
            <template #prefix>
              <el-icon class="search-ico"><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" class="search-btn" @click="submitSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="top-actions">
          <el-tooltip content="我的购物车" placement="bottom">
            <el-button circle class="icon-btn" @click="router.push('/my-cart')">
              <el-icon :size="20"><ShoppingCart /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="我的优惠券" placement="bottom">
            <el-button circle class="icon-btn" @click="router.push('/my-coupons')">
              <el-icon :size="20"><Ticket /></el-icon>
            </el-button>
          </el-tooltip>

          <el-dropdown trigger="click" placement="bottom-end">
            <button type="button" class="avatar-trigger">
              <el-avatar :size="34" :src="avatarSrc">{{ user?.userName?.[0] || user?.userAccount?.[0] || '?' }}</el-avatar>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人信息</el-dropdown-item>
                <el-dropdown-item divided @click="logout">注销登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div class="body-row">
      <aside class="side">
        <div class="side-scroll">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="side-link"
            :class="{ active: isActive(item.path) }"
          >
            <el-icon class="side-ico"><component :is="item.icon" /></el-icon>
            <span class="side-label">{{ item.label }}</span>
          </router-link>
        </div>
        <router-link to="/welfare" class="welfare-btn" :class="{ active: route.path === '/welfare' }">
          <span class="welfare-icon" aria-hidden="true">🎁</span>
          <span>福利中心</span>
        </router-link>
      </aside>

      <main class="main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Noto Sans SC', 'Segoe UI', system-ui, sans-serif;
  background: #f4f4f5;
}

.top-bar {
  flex: 0 0 calc(100vh / 9);
  min-height: 48px;
  max-height: 96px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.9) inset;
  z-index: 20;
}

.top-inner {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  padding: 0 clamp(12px, 2vw, 28px);
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: inherit;
  padding: 0;
  flex-shrink: 0;
}

.brand-text {
  font-family: Outfit, 'Noto Sans SC', sans-serif;
  font-weight: 700;
  font-size: 1.2rem;
  letter-spacing: 0.03em;
  color: #111827;
}

.top-nav {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.nav-btn {
  color: #64748b !important;
  font-size: 14px;
}

.nav-btn.active {
  color: #4f46e5 !important;
  font-weight: 600;
}

.search-wrap {
  flex: 1;
  min-width: 0;
}

.search-ico {
  color: #94a3b8;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
}

.search-input :deep(.el-input-group__append) {
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.search-input :deep(.el-input-group__append .el-button.search-btn) {
  border-radius: 0 10px 10px 0;
  margin: 0;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.icon-btn {
  background: #fff !important;
  border: 1px solid #e5e7eb !important;
  color: #374151 !important;
}

.icon-btn:hover {
  border-color: #c7d2fe !important;
  color: #4f46e5 !important;
}

.avatar-trigger {
  border: 2px solid #e5e7eb;
  border-radius: 50%;
  padding: 0;
  cursor: pointer;
  background: #fff;
  margin-left: 4px;
  transition: border-color 0.15s;
}

.avatar-trigger:hover {
  border-color: #c7d2fe;
}

.body-row {
  flex: 1;
  min-height: 0;
  display: flex;
}

.side {
  width: 132px;
  flex-shrink: 0;
  background: #fafafa;
  color: #52525b;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e4e7;
}

.side-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 10px 8px;
}

.side-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 4px;
  padding: 8px 6px;
  margin-bottom: 6px;
  border-radius: 10px;
  color: inherit;
  text-decoration: none;
  font-size: 11px;
  line-height: 1.25;
  transition: background 0.15s, color 0.15s;
}

.side-link:hover {
  background: rgba(79, 70, 229, 0.06);
  color: #4338ca;
}

.side-link.active {
  background: rgba(79, 70, 229, 0.12);
  color: #4338ca;
  font-weight: 600;
}

.side-ico {
  font-size: 18px;
}

.side-label {
  word-break: keep-all;
}

.welfare-btn {
  flex-shrink: 0;
  margin: 8px;
  padding: 10px 8px;
  border-radius: 12px;
  text-align: center;
  text-decoration: none;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #5b21b6;
  background: linear-gradient(180deg, #f5f3ff 0%, #ede9fe 100%);
  border: 1px solid #ddd6fe;
  box-shadow: 0 1px 2px rgba(91, 33, 182, 0.06);
  transition: transform 0.12s, box-shadow 0.12s;
}

.welfare-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(91, 33, 182, 0.12);
}

.welfare-btn.active {
  outline: 2px solid #a78bfa;
  outline-offset: 0;
}

.welfare-icon {
  font-size: 20px;
  line-height: 1;
}

.main {
  flex: 1;
  min-width: 0;
  overflow: auto;
  background: #f4f4f5;
}
</style>
