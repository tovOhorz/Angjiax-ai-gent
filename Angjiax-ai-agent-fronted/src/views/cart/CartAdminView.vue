<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '@/api/http'
import type { CartVO, PageResult } from '@/types'

const loading = ref(false)
const tableData = ref<CartVO[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 10,
  userId: undefined as string | undefined,
  productId: undefined as string | undefined,
})

async function fetchList() {
  loading.value = true
  try {
    const body: Record<string, unknown> = {
      current: query.current,
      pageSize: query.pageSize,
      sortOrder: 'descend',
    }
    if (query.userId != null) body.userId = query.userId
    if (query.productId != null) body.productId = query.productId

    const page = await http.post<PageResult<CartVO>>('/cart/list/page/vo/admin', body)
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

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <h2 class="page-title">购物车管理</h2>
    <p class="intro">查看全平台用户的购物车条目，可按用户 ID、商品 ID 筛选（管理员权限由后端另行校验）。</p>

    <el-card shadow="never">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="用户 ID">
          <el-input v-model="query.userId" clearable />
        </el-form-item>
        <el-form-item label="商品 ID">
          <el-input v-model="query.productId" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="fetchList">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="id" label="购物车项 ID" width="120" />
        <el-table-column prop="productId" label="商品 ID" width="100" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="选中" width="80">
          <template #default="{ row }">{{ row.selected === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="单价" width="88" />
        <el-table-column prop="subtotal" label="小计" width="88" />
        <el-table-column prop="stock" label="库存" width="72" />
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
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.intro {
  margin: 0 0 16px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
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
