<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { http } from '@/api/http'
import type { OrderVO, PageResult } from '@/types'

const loading = ref(false)
const tableData = ref<OrderVO[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 10,
  orderId: '',
  orderStatus: undefined as number | undefined,
})

const detailVisible = ref(false)
const detail = ref<OrderVO | null>(null)
const detailLoading = ref(false)

function orderStatusLabel(s: number) {
  const map: Record<number, string> = {
    1: '待付款',
    2: '待发货',
    3: '待收货',
    4: '已完成',
    5: '已取消',
  }
  return map[s] ?? String(s)
}

async function fetchList() {
  loading.value = true
  try {
    const body: Record<string, unknown> = {
      current: query.current,
      pageSize: query.pageSize,
      sortOrder: 'descend',
    }
    if (query.orderId) body.orderId = query.orderId
    if (query.orderStatus != null) body.orderStatus = query.orderStatus

    const page = await http.post<PageResult<OrderVO>>('/order/list/page', body)
    tableData.value = page.records || []
    total.value = Number(page.total) || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.current = 1
  fetchList()
}

async function openDetail(orderId: string) {
  detailVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = await http.get<OrderVO>(`/order/detail?orderId=${encodeURIComponent(orderId)}`)
  } finally {
    detailLoading.value = false
  }
}

async function payOrder(orderId: string) {
  await ElMessageBox.confirm('模拟支付该订单？', '提示', { type: 'info' })
  await http.post<boolean>(`/order/pay?orderId=${encodeURIComponent(orderId)}`)
  fetchList()
}

async function confirmRecv(orderId: string) {
  await ElMessageBox.confirm('确认收货？', '提示', { type: 'warning' })
  await http.post<boolean>(`/order/confirm?orderId=${encodeURIComponent(orderId)}`)
  fetchList()
}

async function cancelOrder(orderId: string) {
  await ElMessageBox.confirm('取消该订单？', '提示', { type: 'warning' })
  await http.post<boolean>(`/order/cancel?orderId=${encodeURIComponent(orderId)}`)
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <h2 class="title">我的订单</h2>
    <el-card shadow="never">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="订单号">
          <el-input v-model="query.orderId" clearable placeholder="订单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.orderStatus" clearable placeholder="全部" style="width: 140px">
            <el-option label="待付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="待收货" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="orderId" label="订单号" min-width="168" />
        <el-table-column prop="totalAmount" label="总金额" width="100" />
        <el-table-column prop="discountAmount" label="优惠" width="88" />
        <el-table-column prop="actualPaid" label="实付" width="88" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            {{ row.orderStatusText || orderStatusLabel(row.orderStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.orderId)">详情</el-button>
            <el-button v-if="row.orderStatus === 1" link type="success" @click="payOrder(row.orderId)">
              支付
            </el-button>
            <el-button v-if="row.orderStatus === 3" link type="warning" @click="confirmRecv(row.orderId)">
              确认收货
            </el-button>
            <el-button
              v-if="row.orderStatus === 1 || row.orderStatus === 2"
              link
              type="danger"
              @click="cancelOrder(row.orderId)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="fetchList"
          @size-change="
            () => {
              query.current = 1
              fetchList()
            }
          "
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="订单详情" size="520px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ detail.orderId }}</el-descriptions-item>
            <el-descriptions-item label="总金额">{{ detail.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="优惠金额">{{ detail.discountAmount }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">{{ detail.actualPaid }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              {{ detail.orderStatusText || orderStatusLabel(detail.orderStatus) }}
            </el-descriptions-item>
            <el-descriptions-item label="地址">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="sub-title">商品明细</h4>
          <el-table :data="detail.orderItems || []" border size="small" style="width: 100%">
            <el-table-column prop="productName" label="商品" min-width="140" />
            <el-table-column prop="quantity" label="数量" width="72" />
            <el-table-column prop="unitPrice" label="单价" width="88" />
            <el-table-column prop="totalPrice" label="小计" width="88" />
          </el-table>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.page {
  padding: 20px 24px 48px;
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.sub-title {
  margin: 16px 0 8px;
  font-size: 15px;
}
</style>
