<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '@/api/http'
import type { CouponEntity, PageResult } from '@/types'

const router = useRouter()
const loading = ref(false)
const list = ref<CouponEntity[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 12,
})

async function fetchList() {
  loading.value = true
  try {
    const page = await http.post<PageResult<CouponEntity>>('/coupon/list/page', {
      current: query.current,
      pageSize: query.pageSize,
      sortOrder: 'descend',
      status: 1,
    })
    list.value = page.records || []
    total.value = Number(page.total) || 0
  } finally {
    loading.value = false
  }
}

async function claim(row: CouponEntity) {
  try {
    await http.post<boolean>('/user/coupon/receive', { couponId: row.id })
    ElMessage.success(`已领取「${row.couponName}」`)
  } catch {
    /* http 层已提示 */
  }
}

function couponTypeLabel(t: number) {
  const map: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
  return map[t] ?? String(t)
}

onMounted(fetchList)
</script>

<template>
  <div class="welfare">
    <div class="banner">
      <span class="emoji" aria-hidden="true">🎁</span>
      <div>
        <h1>福利中心</h1>
        <p>限时领取平台优惠券，下单自动抵扣（具体规则以券面为准）。</p>
      </div>
      <el-button type="primary" plain class="to-mine" @click="router.push('/my-coupons')">
        查看我的优惠券
      </el-button>
    </div>

    <div v-loading="loading" class="grid">
      <el-card v-for="row in list" :key="row.id" class="card" shadow="hover">
        <div class="card-head">
          <h3>{{ row.couponName }}</h3>
          <el-tag size="small">{{ couponTypeLabel(row.couponType) }}</el-tag>
        </div>
        <ul class="facts">
          <li>门槛：满 {{ row.conditionAmount }} 元</li>
          <li v-if="row.couponType === 2">折扣：{{ row.discountRate }} 折</li>
          <li v-else>减免：{{ row.discountAmount }} 元</li>
          <li>有效期：{{ row.startTime }} ~ {{ row.endTime }}</li>
          <li>
            剩余：{{ Math.max(0, (row.totalQuantity ?? 0) - (row.usedQuantity ?? 0)) }} /
            {{ row.totalQuantity ?? 0 }}
          </li>
        </ul>
        <el-button type="primary" class="claim" @click="claim(row)">立即领取</el-button>
      </el-card>
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无可领优惠券" />
  </div>
</template>

<style scoped>
.welfare {
  padding: 20px 24px 48px;
  max-width: 1100px;
  margin: 0 auto;
}

.banner {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding: 20px 22px;
  border-radius: 16px;
  background: linear-gradient(120deg, #fef3c7 0%, #fde68a 35%, #fcd34d 100%);
  margin-bottom: 20px;
  border: 1px solid #fbbf24;
}

.emoji {
  font-size: 40px;
  line-height: 1;
}

.banner h1 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #78350f;
}

.banner p {
  margin: 0;
  font-size: 14px;
  color: #92400e;
}

.to-mine {
  margin-left: auto;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.card {
  border-radius: 14px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.card-head h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}

.facts {
  margin: 0 0 14px;
  padding-left: 18px;
  font-size: 13px;
  color: #475569;
  line-height: 1.65;
}

.claim {
  width: 100%;
}

.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
