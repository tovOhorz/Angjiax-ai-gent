<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { http } from '@/api/http'
import type { CartVO } from '@/types'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const loading = ref(false)
const rawList = ref<CartVO[]>([])

const filter = reactive({
  keyword: '',
})

const pager = reactive({
  current: 1,
  pageSize: 10,
})

const addVisible = ref(false)
const addForm = reactive({
  productId: undefined as string | undefined,
  quantity: 1,
})

const checkoutVisible = ref(false)
const checkoutForm = reactive({
  address: '',
  couponId: undefined as string | undefined,
})

const filtered = computed(() => {
  const kw = filter.keyword.trim()
  if (!kw) return rawList.value.slice()
  return rawList.value.filter((r) => r.productName?.includes(kw))
})

const total = computed(() => filtered.value.length)

const pageRows = computed(() => {
  const start = (pager.current - 1) * pager.pageSize
  return filtered.value.slice(start, start + pager.pageSize)
})

async function fetchList() {
  loading.value = true
  try {
    rawList.value = await http.get<CartVO[]>('/cart/list')
  } finally {
    loading.value = false
  }
}

function onFilter() {
  pager.current = 1
}

async function addToCart() {
  if (addForm.productId == null) return
  await http.post<boolean>('/cart/add', {
    productId: addForm.productId,
    quantity: addForm.quantity,
  })
  addVisible.value = false
  fetchList()
}

async function changeQty(row: CartVO, quantity: number) {
  await http.post<boolean>('/cart/update', {
    id: row.id,
    quantity,
    selected: row.selected,
  })
  fetchList()
}

async function toggleSelect(row: CartVO, selected: number) {
  await http.post<boolean>('/cart/toggle', {
    id: row.id,
    quantity: row.quantity,
    selected,
  })
  fetchList()
}

async function removeRow(row: CartVO) {
  await ElMessageBox.confirm('从购物车删除该商品？', '提示', { type: 'warning' })
  await http.post<boolean>(`/cart/delete?cartId=${row.id}`)
  fetchList()
}

const selectedRows = computed(() => rawList.value.filter((r) => r.selected === 1))

function openCheckout() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }
  checkoutForm.address = ''
  checkoutForm.couponId = undefined
  checkoutVisible.value = true
}

async function submitCheckout() {
  if (!checkoutForm.address.trim()) {
    ElMessage.warning('请填写收货地址')
    return
  }
  const cartItemIds = selectedRows.value.map((r) => r.id)
  const order = await http.post<{ orderId: string }>('/order/createFromCart', {
    cartItemIds,
    address: checkoutForm.address.trim(),
    ...(checkoutForm.couponId != null ? { couponId: checkoutForm.couponId } : {}),
  })
  checkoutVisible.value = false
  ElMessage.success(`下单成功，订单号 ${order.orderId}`)
  router.push('/orders')
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h2 class="title">我的购物车</h2>
      <div class="head-actions">
        <el-button type="success" @click="openCheckout">结算选中商品</el-button>
        <el-button type="primary" @click="addVisible = true">添加商品</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <div class="hint">当前账号：<strong>{{ user?.userAccount || '—' }}</strong></div>
      <el-form :inline="true" class="toolbar">
        <el-form-item label="商品">
          <el-input v-model="filter.keyword" clearable placeholder="商品名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onFilter">搜索</el-button>
          <el-button @click="fetchList">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="pageRows" border stripe style="width: 100%">
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              size="small"
              @change="(v: number | undefined) => v != null && changeQty(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="选中" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.selected === 1"
              @change="(v: boolean) => toggleSelect(row, v ? 1 : 0)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="单价" width="88" />
        <el-table-column prop="subtotal" label="小计" width="88" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pager.current"
          v-model:page-size="pager.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </el-card>

    <el-dialog v-model="checkoutVisible" title="结算" width="440px" destroy-on-close>
      <p class="checkout-hint">将对 {{ selectedRows.length }} 件选中商品下单，完成后可在「订单」中查看与支付。</p>
      <el-form label-width="88px">
        <el-form-item label="收货地址" required>
          <el-input v-model="checkoutForm.address" type="textarea" rows="2" placeholder="省市区 / 街道 / 门牌" />
        </el-form-item>
        <el-form-item label="优惠券 ID">
          <el-input
            v-model="checkoutForm.couponId"
            placeholder="选填"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckout">提交订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="添加到购物车" width="420px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="商品 ID">
          <el-input v-model="addForm.productId" placeholder="请输入商品ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="addForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="addToCart">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  max-width: 1100px;
  margin: 0 auto;
}

.title {
  font-weight: 700;
  font-size: 20px;
  color: #0f172a;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.head-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.checkout-hint {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px;
}

.toolbar {
  margin-bottom: 12px;
}

.hint {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
