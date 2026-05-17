<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { http } from '@/api/http'
import type { CouponEntity, PageResult } from '@/types'

const loading = ref(false)
const tableData = ref<CouponEntity[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 10,
  couponName: '',
  couponType: undefined as number | undefined,
  platform: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('添加优惠券')
const form = reactive({
  id: undefined as string | undefined,
  couponName: '',
  couponType: 1,
  conditionAmount: 0,
  discountAmount: 0,
  discountRate: 1,
  startTime: '',
  endTime: '',
  applicableProducts: '',
  platform: '',
  totalQuantity: 0,
  usedQuantity: 0,
  status: 1,
})

function couponTypeLabel(t: number) {
  const map: Record<number, string> = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
  return map[t] ?? String(t)
}

function couponStatusLabel(s: number) {
  return s === 1 ? '有效' : '失效'
}

async function fetchList() {
  loading.value = true
  try {
    const body: Record<string, unknown> = {
      current: query.current,
      pageSize: query.pageSize,
      sortOrder: 'descend',
    }
    if (query.couponName) body.couponName = query.couponName
    if (query.couponType != null) body.couponType = query.couponType
    if (query.platform) body.platform = query.platform

    const page = await http.post<PageResult<CouponEntity>>('/coupon/list/page', body)
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

function openAdd() {
  dialogTitle.value = '添加优惠券'
  Object.assign(form, {
    id: undefined,
    couponName: '',
    couponType: 1,
    conditionAmount: 0,
    discountAmount: 0,
    discountRate: 1,
    startTime: '',
    endTime: '',
    applicableProducts: '',
    platform: '',
    totalQuantity: 100,
    usedQuantity: 0,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: CouponEntity) {
  dialogTitle.value = '编辑优惠券'
  Object.assign(form, {
    id: row.id,
    couponName: row.couponName,
    couponType: row.couponType,
    conditionAmount: row.conditionAmount,
    discountAmount: row.discountAmount,
    discountRate: row.discountRate,
    startTime: row.startTime,
    endTime: row.endTime,
    applicableProducts: row.applicableProducts || '',
    platform: row.platform,
    totalQuantity: row.totalQuantity,
    usedQuantity: row.usedQuantity,
    status: row.status,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (form.id == null) {
    await http.post<number>('/coupon/add', {
      couponName: form.couponName,
      couponType: form.couponType,
      conditionAmount: form.conditionAmount,
      discountAmount: form.discountAmount,
      discountRate: form.discountRate,
      startTime: form.startTime,
      endTime: form.endTime,
      applicableProducts: form.applicableProducts,
      platform: form.platform,
      totalQuantity: form.totalQuantity,
    })
  } else {
    await http.post<boolean>('/coupon/update', {
      id: form.id,
      couponName: form.couponName,
      couponType: form.couponType,
      conditionAmount: form.conditionAmount,
      discountAmount: form.discountAmount,
      discountRate: form.discountRate,
      startTime: form.startTime,
      endTime: form.endTime,
      totalQuantity: form.totalQuantity,
      status: form.status,
    })
  }
  dialogVisible.value = false
  fetchList()
}

async function removeRow(row: CouponEntity) {
  await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」？`, '提示', { type: 'warning' })
  await http.post<boolean>('/coupon/delete', { id: row.id })
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <h2 class="title page-title">优惠券管理</h2>

    <el-card shadow="never">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="名称">
          <el-input v-model="query.couponName" clearable placeholder="优惠券名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.couponType" clearable placeholder="全部" style="width: 140px">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="query.platform" clearable placeholder="平台" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="openAdd">添加优惠券</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="88" />
        <el-table-column prop="couponName" label="名称" min-width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ couponTypeLabel(row.couponType) }}</template>
        </el-table-column>
        <el-table-column prop="conditionAmount" label="满减条件" width="100" />
        <el-table-column prop="discountAmount" label="优惠金额" width="100" />
        <el-table-column prop="discountRate" label="折扣率" width="88" />
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="endTime" label="结束时间" min-width="160" />
        <el-table-column prop="platform" label="平台" width="100" />
        <el-table-column prop="totalQuantity" label="发行量" width="88" />
        <el-table-column prop="usedQuantity" label="已用" width="72" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">{{ couponStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRow(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form label-width="112px">
        <el-form-item label="名称" required>
          <el-input v-model="form.couponName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.couponType" style="width: 100%">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="满减条件">
          <el-input-number v-model="form.conditionAmount" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="优惠金额">
          <el-input-number v-model="form.discountAmount" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="折扣率">
          <el-input-number v-model="form.discountRate" :min="0" :max="1" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="form.startTime" placeholder="如 2026-05-01T08:00:00" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input v-model="form.endTime" placeholder="如 2026-05-31T23:59:59" />
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="form.platform" />
        </el-form-item>
        <el-form-item v-if="form.id == null" label="发行量">
          <el-input-number v-model="form.totalQuantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.id != null" label="发行量">
          <el-input-number v-model="form.totalQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.id != null" label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">有效</el-radio>
            <el-radio :label="0">失效</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.id == null" label="适用商品">
          <el-input v-model="form.applicableProducts" type="textarea" rows="2" placeholder="可选 JSON" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  max-width: 1700px;
  margin: 0 auto;
}

.title {
  font-weight: 700;
  font-size: 20px;
  color: #0f172a;
}

.page-title {
  margin: 0 0 16px;
}

.mb {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
