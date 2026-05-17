<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '@/api/http'
import type { ProductVO } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref<ProductVO | null>(null)
const quantity = ref(1)

const id = computed(() => String(route.params.id))

async function load() {
  if (!id.value || id.value === 'undefined' || id.value === 'null') {
    ElMessage.error('无效的商品')
    router.replace('/')
    return
  }
  loading.value = true
  try {
    product.value = await http.get<ProductVO>(`/product/get/vo?id=${encodeURIComponent(id.value)}`)
    quantity.value = 1
  } catch {
    router.replace('/')
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  if (!product.value) return
  await http.post<boolean>('/cart/add', {
    productId: product.value.id,
    quantity: quantity.value,
  })
  ElMessage.success('已加入购物车')
}

watch(id, load)
onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="product">
      <el-button link type="primary" class="back" @click="router.push('/')">← 返回首页</el-button>
      <div class="layout">
        <div class="visual">
          <img
            class="hero-img"
            :src="product.imageUrl || 'https://via.placeholder.com/560x420?text=No+Image'"
            :alt="product.productName"
          />
        </div>
        <div class="info">
          <h1>{{ product.productName }}</h1>
          <p class="sub">{{ product.categoryName }} · {{ product.brand }} · {{ product.platform }}</p>
          <div class="price-row">
            <span class="price">¥{{ product.currentPrice }}</span>
            <span v-if="product.originalPrice > product.currentPrice" class="orig">¥{{ product.originalPrice }}</span>
          </div>
          <el-descriptions :column="1" border size="small" class="desc">
            <el-descriptions-item label="库存">{{ product.stock }}</el-descriptions-item>
            <el-descriptions-item label="销量">{{ product.salesVolume }}</el-descriptions-item>
            <el-descriptions-item label="评分">{{ product.rating }}</el-descriptions-item>
          </el-descriptions>
          <p v-if="product.productDesc" class="long-desc">{{ product.productDesc }}</p>
          <div class="buy-row">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(1, product.stock)" />
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  padding: 20px 24px 48px;
  max-width: 1100px;
  margin: 0 auto;
  min-height: 320px;
}

.back {
  margin-bottom: 12px;
  padding-left: 0;
}

.layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  align-items: start;
}

@media (max-width: 768px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.visual {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.hero-img {
  width: 100%;
  display: block;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.info h1 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #0f172a;
}

.sub {
  margin: 0 0 16px;
  color: #64748b;
  font-size: 14px;
}

.price-row {
  margin-bottom: 16px;
}

.price {
  font-size: 28px;
  font-weight: 800;
  color: #dc2626;
}

.orig {
  margin-left: 10px;
  font-size: 14px;
  color: #94a3b8;
  text-decoration: line-through;
}

.desc {
  margin-bottom: 16px;
}

.long-desc {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0 0 20px;
}

.buy-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.label {
  font-size: 14px;
  color: #334155;
}
</style>
