<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { http } from '@/api/http'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const editVisible = ref(false)
const form = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
  phone: '',
})

function openEdit() {
  const u = user.value
  if (!u) return
  form.userName = u.userName || ''
  form.userAvatar = u.userAvatar || ''
  form.userProfile = u.userProfile || ''
  form.phone = u.phone || ''
  editVisible.value = true
}

async function saveProfile() {
  const u = user.value
  if (!u?.id) return
  await http.post<boolean>('/user/update', {
    id: u.id,
    userName: form.userName,
    userAvatar: form.userAvatar,
    userProfile: form.userProfile,
    phone: form.phone,
    userRole: u.userRole,
    vip_level: u.vip_level,
    points: u.points,
  })
  ElMessage.success('已保存')
  editVisible.value = false
  await userStore.refreshLoginUser()
}

async function logout() {
  await userStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="page">
    <h2 class="title">个人信息</h2>
    <el-card v-if="user" shadow="never" class="card">
      <div class="head">
        <el-avatar :size="72" :src="user.userAvatar">{{ user.userName?.[0] || user.userAccount?.[0] }}</el-avatar>
        <div class="sum">
          <div class="name">{{ user.userName || user.userAccount }}</div>
          <div class="acct">{{ user.userAccount }}</div>
          <el-tag size="small" type="info">{{ user.userRole }}</el-tag>
        </div>
      </div>
      <el-descriptions :column="1" border class="desc">
        <el-descriptions-item label="简介">{{ user.userProfile || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ user.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">{{ user.vip_level }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ user.points }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="openEdit">修改资料</el-button>
        <el-button @click="logout">注销登录</el-button>
      </div>
    </el-card>
    <el-empty v-else description="未登录" />

    <el-dialog v-model="editVisible" title="修改资料" width="480px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="昵称">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.userAvatar" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.userProfile" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page {
  padding: 20px 24px 48px;
  max-width: 640px;
  margin: 0 auto;
}

.title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.card {
  border-radius: 14px;
}

.head {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
}

.sum {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name {
  font-size: 18px;
  font-weight: 600;
}

.acct {
  font-size: 13px;
  color: #64748b;
}

.desc {
  margin-bottom: 20px;
}

.actions {
  display: flex;
  gap: 12px;
}
</style>
