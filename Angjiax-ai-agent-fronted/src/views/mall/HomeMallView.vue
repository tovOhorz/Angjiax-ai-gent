<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '@/api/http'
import type { PageResult, ProductVO } from '@/types'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const categoriesLoading = ref(false)
const categoryTags = ref<string[]>(['全部'])
const selectedCategory = ref('全部')

const query = reactive({
  current: 1,
  pageSize: 12,
  productName: '',
  categoryName: '' as string,
  status: 1,
})

const pageResult = ref<PageResult<ProductVO> | null>(null)

const qtyMap = ref<Record<number, number>>({})

function getQty(id: number) {
  return qtyMap.value[id] ?? 1
}

function setQty(id: number, v: number) {
  qtyMap.value = { ...qtyMap.value, [id]: v }
}

async function loadCategories() {
  categoriesLoading.value = true
  try {
    const page = await http.post<PageResult<ProductVO>>('/product/list/page/vo', {
      current: 1,
      pageSize: 400,
      sortOrder: 'descend',
      status: 1,
    })
    const set = new Set<string>()
    for (const p of page.records || []) {
      if (p.categoryName?.trim()) set.add(p.categoryName.trim())
    }
    categoryTags.value = ['全部', ...Array.from(set).sort()]
  } finally {
    categoriesLoading.value = false
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const body: Record<string, unknown> = {
      current: query.current,
      pageSize: query.pageSize,
      sortOrder: 'descend',
      status: query.status,
    }
    if (query.productName.trim()) body.productName = query.productName.trim()
    if (query.categoryName) body.categoryName = query.categoryName

    pageResult.value = await http.post<PageResult<ProductVO>>('/product/list/page/vo', body)
  } finally {
    loading.value = false
  }
}

function selectCategory(name: string) {
  selectedCategory.value = name
  query.categoryName = name === '全部' ? '' : name
  query.current = 1
  fetchProducts()
}

async function addToCart(row: ProductVO) {
  const q = getQty(row.id)
  await http.post<boolean>('/cart/add', { productId: row.id, quantity: q })
  ElMessage.success('已加入购物车')
}

function openDetail(row: ProductVO) {
  router.push(`/product/${row.id}`)
}

function syncSearchFromRoute() {
  const q = typeof route.query.q === 'string' ? route.query.q : ''
  query.productName = q
  query.current = 1
}

watch(
  () => route.query.q,
  () => {
    syncSearchFromRoute()
    fetchProducts()
  },
)

watch(
  () => [query.current, query.pageSize] as const,
  () => fetchProducts(),
)

onMounted(async () => {
  syncSearchFromRoute()
  await loadCategories()
  await fetchProducts()
})
</script>

<template>
  <div class="mall">
    <section class="hero-poster" aria-label="主题海报">
      <div class="hero-bg" />
      <div class="hero-overlay" />
      <div class="hero-content">
        <p class="hero-script">遇见心动，遇见更好的自己</p>
        <p class="hero-tag">甄选时尚好物 · 点亮日常每一刻</p>
      </div>
    </section>

    <section class="tag-section">
      <div class="section-title">商品分类</div>
      <div v-loading="categoriesLoading" class="tags">
        <el-tag
          v-for="tag in categoryTags"
          :key="tag"
          class="tag"
          :effect="selectedCategory === tag ? 'dark' : 'plain'"
          @click="selectCategory(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>
    </section>

    <section class="grid-section">
      <div class="toolbar">
        <span class="muted">共 {{ pageResult?.total ?? 0 }} 件商品</span>
        <div class="pager-top">
          <span class="muted">每页</span>
          <el-select v-model="query.pageSize" style="width: 88px" @change="query.current = 1">
            <el-option :label="`${12}`" :value="12" />
            <el-option :label="`${24}`" :value="24" />
            <el-option :label="`${48}`" :value="48" />
          </el-select>
        </div>
      </div>

      <div v-loading="loading" class="card-grid">
        <article
          v-for="row in pageResult?.records || []"
          :key="row.id"
          class="product-card"
          role="button"
          tabindex="0"
          @click="openDetail(row)"
          @keydown.enter="openDetail(row)"
        >
          <div class="thumb-wrap">
            <img
              class="thumb"
              :src="row.imageUrl || 'https://via.placeholder.com/320x240?text=No+Image'"
              :alt="row.productName"
              loading="lazy"
              @click.stop
            />
          </div>
          <h3 class="name" @click.stop="openDetail(row)">{{ row.productName }}</h3>
          <div class="meta">
            <span class="price">¥{{ row.currentPrice }}</span>
            <span v-if="row.originalPrice > row.currentPrice" class="orig">¥{{ row.originalPrice }}</span>
          </div>
          <div class="row-actions" @click.stop>
            <el-input-number
              :model-value="getQty(row.id)"
              :min="1"
              :max="Math.max(1, row.stock)"
              size="small"
              @update:model-value="(v: number | undefined) => v != null && setQty(row.id, v)"
            />
            <el-button type="primary" size="small" @click="addToCart(row)">加入购物车</el-button>
          </div>
        </article>
      </div>

      <div v-if="(pageResult?.total ?? 0) > 0" class="pager-bar">
        <el-pagination
          v-model:current-page="query.current"
          :page-size="query.pageSize"
          :total="Number(pageResult?.total) || 0"
          layout="prev, pager, next, jumper"
          background
        />
      </div>

      <el-empty v-if="!loading && (pageResult?.records?.length ?? 0) === 0" description="暂无商品" />
    </section>
  </div>
</template>

<style scoped>
.mall {
  padding: 20px 24px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.hero-poster {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  min-height: clamp(200px, 28vh, 320px);
  margin-bottom: 18px;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.12);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-image: url('https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=1800&q=85');
  background-size: cover;
  background-position: center 22%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    105deg,
    rgba(15, 23, 42, 0.58) 0%,
    rgba(15, 23, 42, 0.28) 42%,
    rgba(255, 255, 255, 0.06) 100%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  padding: clamp(28px, 5vw, 56px) clamp(22px, 4vw, 52px);
  max-width: 560px;
}

.hero-script {
  margin: 0 0 14px;
  font-family: 'Zhi Mang Xing', 'Noto Sans SC', cursive;
  font-size: clamp(2.25rem, 6vw, 3.75rem);
  line-height: 1.12;
  color: #fff;
  text-shadow: 0 6px 28px rgba(0, 0, 0, 0.4);
  letter-spacing: 0.08em;
}

.hero-tag {
  margin: 0;
  font-size: clamp(13px, 1.8vw, 16px);
  color: rgba(255, 255, 255, 0.94);
  font-weight: 500;
  letter-spacing: 0.2em;
}

.tag-section {
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px 18px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 36px;
}

.tag {
  cursor: pointer;
  user-select: none;
}

.grid-section {
  background: transparent;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.muted {
  font-size: 13px;
  color: #64748b;
}

.pager-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  padding-bottom: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06);
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}

.product-card:hover {
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

.thumb-wrap {
  aspect-ratio: 4 / 3;
  background: #f8fafc;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.name {
  margin: 10px 12px 6px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.meta {
  padding: 0 12px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 16px;
  font-weight: 700;
  color: #dc2626;
}

.orig {
  font-size: 12px;
  color: #94a3b8;
  text-decoration: line-through;
}

.row-actions {
  padding: 10px 12px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.pager-bar {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
