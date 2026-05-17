<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { http } from '@/api/http'
import type { PageResult, ProductVO } from '@/types'

const loading = ref(false)
const tableData = ref<ProductVO[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 10,
  productName: '',
  categoryName: '',
  brand: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('添加商品')
const form = reactive({
  id: undefined as string | undefined,
  productName: '',
  categoryName: '',
  brand: '',
  originalPrice: 0,
  currentPrice: 0,
  stock: 0,
  salesVolume: 0,
  rating: 5,
  platform: '',
  platformUrl: '',
  imageUrl: '',
  productDesc: '',
  status: 1,
})

function statusText(s: number) {
  return s === 1 ? '上架' : '下架'
}

async function fetchList() {
  loading.value = true
  try {
    const page = await http.post<PageResult<ProductVO>>('/product/list/page/vo', {
      ...query,
      sortOrder: 'descend',
    })
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
  dialogTitle.value = '添加商品'
  Object.assign(form, {
    id: undefined,
    productName: '',
    categoryName: '',
    brand: '',
    originalPrice: 0,
    currentPrice: 0,
    stock: 0,
    salesVolume: 0,
    rating: 5,
    platform: '',
    platformUrl: '',
    imageUrl: '',
    productDesc: '',
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(row: ProductVO) {
  dialogTitle.value = '编辑商品'
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    categoryName: row.categoryName,
    brand: row.brand,
    originalPrice: row.originalPrice,
    currentPrice: row.currentPrice,
    stock: row.stock,
    salesVolume: row.salesVolume,
    rating: row.rating,
    platform: row.platform,
    platformUrl: row.platformUrl || '',
    imageUrl: row.imageUrl || '',
    productDesc: row.productDesc || '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function submitForm() {
  const payload = {
    productName: form.productName,
    categoryName: form.categoryName,
    brand: form.brand,
    originalPrice: form.originalPrice,
    currentPrice: form.currentPrice,
    stock: form.stock,
    salesVolume: form.salesVolume,
    rating: form.rating,
    platform: form.platform,
    platformUrl: form.platformUrl,
    imageUrl: form.imageUrl,
    productDesc: form.productDesc,
    status: form.status,
  }
  if (form.id == null) {
    await http.post<number>('/product/add', payload)
  } else {
    await http.post<boolean>('/product/update', { id: form.id, ...payload })
  }
  dialogVisible.value = false
  fetchList()
}

async function removeRow(row: ProductVO) {
  await ElMessageBox.confirm(`确定删除商品「${row.productName}」？`, '提示', { type: 'warning' })
  await http.post<boolean>('/product/delete', { id: row.id })
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <h2 class="title page-title">商品管理</h2>

    <el-card shadow="never">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="名称">
          <el-input v-model="query.productName" clearable placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="query.categoryName" clearable placeholder="分类" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="query.brand" clearable placeholder="品牌" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="openAdd">添加商品</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="88" />
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="originalPrice" label="原价" width="88" />
        <el-table-column prop="currentPrice" label="现价" width="88" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesVolume" label="销量" width="80" />
        <el-table-column prop="rating" label="评分" width="72" />
        <el-table-column prop="platform" label="平台" width="88" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
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
      <el-form label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="现价">
          <el-input-number v-model="form.currentPrice" :min="0" :step="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="月销量">
          <el-input-number v-model="form.salesVolume" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.rating" :min="0" :max="5" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="平台">
          <el-input v-model="form.platform" placeholder="淘宝/京东/拼多多" />
        </el-form-item>
        <el-form-item label="商品链接">
          <el-input v-model="form.platformUrl" />
        </el-form-item>
        <el-form-item label="图片 URL">
          <el-input v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.productDesc" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
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
  max-width: 1600px;
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
