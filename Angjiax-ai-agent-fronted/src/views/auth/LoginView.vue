<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const mode = ref<'login' | 'register'>('login')
const loading = ref(false)

const loginForm = ref({ userAccount: '', userPassword: '' })
const registerForm = ref({ userAccount: '', userPassword: '', checkPassword: '' })

async function onLogin() {
  if (!loginForm.value.userAccount || !loginForm.value.userPassword) {
    ElMessage.warning('请填写账号与密码')
    return
  }
  loading.value = true
  try {
    await userStore.login({ ...loginForm.value })
    loginForm.value.userPassword = ''
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect || '/')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  const f = registerForm.value
  if (!f.userAccount || !f.userPassword) {
    ElMessage.warning('请填写账号与密码')
    return
  }
  if (f.userPassword !== f.checkPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await userStore.register({ ...f })
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.value.userAccount = f.userAccount
    registerForm.value = { userAccount: '', userPassword: '', checkPassword: '' }
  } finally {
    loading.value = false
  }
}

function switchRegister() {
  mode.value = 'register'
}

function switchLogin() {
  mode.value = 'login'
}
</script>

<template>
  <div class="login-page">
    <div class="backdrop" />
    <div class="card">
      <div class="card-head">
        <div class="logo-mark">
          <svg viewBox="0 0 40 40" width="40" height="40" fill="none">
            <defs>
              <linearGradient id="lg" x1="0" y1="0" x2="40" y2="40">
                <stop stop-color="#6366f1" />
                <stop offset="1" stop-color="#ec4899" />
              </linearGradient>
            </defs>
            <rect width="40" height="40" rx="10" fill="url(#lg)" />
            <path
              d="M11 27V13l9 10 9-10v14"
              stroke="#fff"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
        <h1 class="title">Angjiax</h1>
        <p class="subtitle">电商智能助手 · 登录后即可使用全部功能</p>
      </div>

      <template v-if="mode === 'login'">
        <el-form class="form" label-position="top" @submit.prevent="onLogin">
          <el-form-item label="账号">
            <el-input v-model="loginForm.userAccount" autocomplete="username" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="loginForm.userPassword"
              type="password"
              show-password
              autocomplete="current-password"
              size="large"
            />
          </el-form-item>
          <p class="hint">
            没有账户，
            <button type="button" class="linkish" @click="switchRegister">去注册</button>
          </p>
          <el-button type="primary" size="large" class="submit" native-type="submit" :loading="loading">
            登录
          </el-button>
        </el-form>
      </template>

      <template v-else>
        <el-form class="form" label-position="top" @submit.prevent="onRegister">
          <el-form-item label="账号">
            <el-input v-model="registerForm.userAccount" autocomplete="username" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="registerForm.userPassword"
              type="password"
              show-password
              autocomplete="new-password"
              size="large"
            />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="registerForm.checkPassword" type="password" show-password size="large" />
          </el-form-item>
          <p class="hint">
            已有账户，
            <button type="button" class="linkish" @click="switchLogin">去登录</button>
          </p>
          <el-button type="primary" size="large" class="submit" native-type="submit" :loading="loading">
            注册
          </el-button>
        </el-form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 24px;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}

.backdrop {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 20% 20%, rgba(99, 102, 241, 0.35), transparent),
    radial-gradient(ellipse 70% 50% at 80% 80%, rgba(236, 72, 153, 0.28), transparent),
    linear-gradient(165deg, #0f172a 0%, #1e1b4b 100%);
  z-index: 0;
}

.card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 36px 32px 40px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(14px);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-head {
  text-align: center;
  margin-bottom: 28px;
}

.logo-mark {
  display: inline-flex;
  margin-bottom: 12px;
}

.title {
  margin: 0 0 8px;
  font-family: Outfit, 'Noto Sans SC', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  background: linear-gradient(120deg, #4338ca, #db2777);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.form {
  margin-top: 8px;
}

.hint {
  margin: -4px 0 16px;
  font-size: 13px;
  color: #64748b;
}

.linkish {
  border: none;
  background: none;
  padding: 0;
  color: #4f46e5;
  cursor: pointer;
  font-size: inherit;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.linkish:hover {
  color: #7c3aed;
}

.submit {
  width: 100%;
}
</style>
