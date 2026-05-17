<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { http } from '@/api/http'
import type { UserCouponVO } from '@/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const loading = ref(false)
const rawList = ref<UserCouponVO[]>([])

const filter = reactive({
  couponKeyword: '',
  status: undefined as number | undefined,
})

const pager = reactive({
  current: 1,
  pageSize: 10,
})

const receiveVisible = ref(false)
const receiveCouponId = ref<string | undefined>(undefined)

const grantVisible = ref(false)
const grantForm = reactive({
  userId: undefined as string | undefined,
  couponId: undefined as string | undefined,
})

function ucStatusLabel(s: number) {
  const map: Record<number, string> = { 1: '未使用', 2: '已使用', 3: '已过期' }
  return map[s] ?? String(s)
}

const filtered = computed(() => {
  let rows = rawList.value.slice()
  const kw = filter.couponKeyword.trim()
  if (kw) {
    rows = rows.filter(
      (r) =>
        r.coupon?.couponName?.includes(kw) ||
        String(r.userId).includes(kw),
    )
  }
  if (filter.status != null) {
    rows = rows.filter((r) => r.status === filter.status)
  }
  return rows
})

const total = computed(() => filtered.value.length)

const pageRows = computed(() => {
  const start = (pager.current - 1) * pager.pageSize
  return filtered.value.slice(start, start + pager.pageSize)
})

async function fetchList() {
  loading.value = true
  try {
    rawList.value = await http.get<UserCouponVO[]>('/user/coupon/list')
  } finally {
    loading.value = false
  }
}

function onFilter() {
  pager.current = 1
}

async function receiveCoupon() {
  if (receiveCouponId.value == null) {
    ElMessage.warning('请输入优惠券 ID')
    return
  }
  await http.post<boolean>('/user/coupon/receive', { couponId: receiveCouponId.value })
  receiveVisible.value = false
  receiveCouponId.value = undefined
  fetchList()
}

/**
 * 管理员指定用户发放优惠券：默认调用 POST /user/coupon/grant
 * 若后端路径不同，请修改此处或在后端实现同名接口（body: userId, couponId）。
 */
async function grantCoupon() {
  if (grantForm.userId == null || grantForm.couponId == null) {
    ElMessage.warning('请填写用户 ID 与优惠券 ID')
    return
  }
  await http.post<boolean>('/user/coupon/grant', {
    userId: grantForm.userId,
    couponId: grantForm.couponId,
  })
  grantVisible.value = false
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <div class="page-head mb">
      <h2 class="title">我的优惠券</h2>
      <div>
        <el-button type="primary" @click="receiveVisible = true">领取优惠券</el-button>
        <el-button @click="grantVisible = true">发放给用户</el-button>
      </div>
    </div>

    <el-alert
      class="mb"
      type="info"
      show-icon
      :closable="false"
      title="列表数据来自 GET /user/coupon/list（当前登录用户）。搜索与分页在前端完成。发放接口默认 POST /user/coupon/grant，可按后端实际路径调整。"
    />

    <el-card shadow="never">
      <div class="hint">当前用户：{{ user?.userAccount || '未登录' }}（展示字段中的「用户」为领取记录中的 userId）</div>
      <el-form :inline="true" class="toolbar">
        <el-form-item label="优惠券/用户">
          <el-input v-model="filter.couponKeyword" clearable placeholder="优惠券名称或用户 ID" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="未使用" :value="1" />
            <el-option label="已使用" :value="2" />
            <el-option label="已过期" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onFilter">筛选</el-button>
          <el-button @click="fetchList">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="pageRows" border stripe style="width: 100%">
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column label="优惠券名称" min-width="160">
          <template #default="{ row }">{{ row.coupon?.couponName }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ ucStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="receivedAt" label="领取时间" min-width="160" />
        <el-table-column prop="usedAt" label="使用时间" min-width="160" />
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

    <el-dialog v-model="receiveVisible" title="领取优惠券" width="400px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="优惠券 ID">
          <el-input v-model="receiveCouponId" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveVisible = false">取消</el-button>
        <el-button type="primary" @click="receiveCoupon">领取</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" title="发放优惠券给用户" width="440px" destroy-on-close>
      <el-form label-width="104px">
        <el-form-item label="用户 ID">
          <el-input v-model="grantForm.userId" style="width: 100%" />
        </el-form-item>
        <el-form-item label="优惠券 ID">
          <el-input v-model="grantForm.couponId" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantVisible = false">取消</el-button>
        <el-button type="primary" @click="grantCoupon">发放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.title {
  font-weight: 700;
  font-size: 20px;
  color: #0f172a;
  margin: 0;
}

.mb {
  margin-bottom: 16px;
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
