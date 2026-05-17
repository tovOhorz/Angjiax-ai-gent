<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '@/api/http'
import type { PageResult, UserVO } from '@/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<UserVO[]>([])
const total = ref(0)

const query = reactive({
  current: 1,
  pageSize: 10,
  userAccount: '',
  userName: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const form = reactive({
  id: undefined as string | undefined,
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
  vip_level: 0,
  points: 0,
  phone: '',
})

const loginVisible = ref(false)
const loginForm = reactive({ userAccount: '', userPassword: '' })

const registerVisible = ref(false)
const registerForm = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

function vipLabel(v: number) {
  const map: Record<number, string> = { 0: '普通', 1: '黄金', 2: '铂金', 3: '钻石' }
  return map[v] ?? String(v)
}

async function fetchList() {
  loading.value = true
  try {
    const page = await http.post<PageResult<UserVO>>('/user/list/page/vo', {
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
  dialogTitle.value = '添加用户'
  Object.assign(form, {
    id: undefined,
    userName: '',
    userAccount: '',
    userAvatar: '',
    userProfile: '',
    userRole: 'user',
    vip_level: 0,
    points: 0,
    phone: '',
  })
  dialogVisible.value = true
}

function openEdit(row: UserVO) {
  dialogTitle.value = '编辑用户'
  Object.assign(form, {
    id: row.id,
    userName: row.userName,
    userAccount: row.userAccount,
    userAvatar: row.userAvatar,
    userProfile: row.userProfile,
    userRole: row.userRole,
    vip_level: row.vip_level,
    points: row.points,
    phone: row.phone,
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (form.id == null) {
    await http.post<number>('/user/add', {
      userName: form.userName,
      userAccount: form.userAccount,
      userAvatar: form.userAvatar,
      userProfile: form.userProfile,
      userRole: form.userRole,
      vip_level: form.vip_level,
      points: form.points,
      phone: form.phone,
    })
  } else {
    await http.post<boolean>('/user/update', {
      id: form.id,
      userName: form.userName,
      userAvatar: form.userAvatar,
      userProfile: form.userProfile,
      userRole: form.userRole,
      vip_level: form.vip_level,
      points: form.points,
      phone: form.phone,
    })
  }
  dialogVisible.value = false
  fetchList()
}

async function removeRow(row: UserVO) {
  await ElMessageBox.confirm(`确定删除用户「${row.userAccount}」？`, '提示', { type: 'warning' })
  await http.post<boolean>('/user/delete', { id: row.id })
  fetchList()
}

async function doLogin() {
  await userStore.login({ ...loginForm })
  loginVisible.value = false
  loginForm.userPassword = ''
}

async function doRegister() {
  if (registerForm.userPassword !== registerForm.checkPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  await http.post<number>('/user/register', { ...registerForm })
  registerVisible.value = false
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <h2 class="title page-title">用户管理</h2>

    <el-card shadow="never">
      <el-form :inline="true" class="toolbar">
        <el-form-item label="账号">
          <el-input v-model="query.userAccount" clearable placeholder="用户账号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="query.userName" clearable placeholder="昵称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="openAdd">添加用户</el-button>
          <el-button @click="loginVisible = true">登录</el-button>
          <el-button @click="registerVisible = true">注册</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="88" />
        <el-table-column prop="userAccount" label="账号" min-width="120" />
        <el-table-column prop="userName" label="昵称" min-width="100" />
        <el-table-column label="头像" width="72">
          <template #default="{ row }">
            <el-avatar v-if="row.userAvatar" :src="row.userAvatar" :size="36" />
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="userProfile" label="简介" min-width="140" show-overflow-tooltip />
        <el-table-column prop="userRole" label="角色" width="88" />
        <el-table-column label="会员" width="88">
          <template #default="{ row }">{{ vipLabel(row.vip_level) }}</template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" />
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item v-if="form.id == null" label="账号" required>
          <el-input v-model="form.userAccount" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.userAvatar" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.userProfile" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.userRole" style="width: 100%">
            <el-option label="user" value="user" />
            <el-option label="admin" value="admin" />
            <el-option label="ban" value="ban" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="form.vip_level" style="width: 100%">
            <el-option label="普通" :value="0" />
            <el-option label="黄金" :value="1" />
            <el-option label="铂金" :value="2" />
            <el-option label="钻石" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="form.points" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="loginVisible" title="登录" width="400px" destroy-on-close>
      <el-form label-width="72px">
        <el-form-item label="账号">
          <el-input v-model="loginForm.userAccount" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.userPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="loginVisible = false">取消</el-button>
        <el-button type="primary" @click="doLogin">登录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="registerVisible" title="注册" width="420px" destroy-on-close>
      <el-form label-width="96px">
        <el-form-item label="账号">
          <el-input v-model="registerForm.userAccount" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.userPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="registerForm.checkPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" @click="doRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 16px;
  max-width: 1400px;
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
