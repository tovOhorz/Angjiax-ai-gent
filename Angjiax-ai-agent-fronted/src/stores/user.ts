import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginUserVO } from '@/types'
import { http } from '@/api/http'

export const useUserStore = defineStore('user', () => {
  const user = ref<LoginUserVO | null>(null)
  const bootstrapped = ref(false)

  const isLoggedIn = computed(() => !!user.value?.id)

  async function refreshLoginUser() {
    try {
      const data = await http.get<LoginUserVO>('/user/get/login')
      user.value = data
      return data
    } catch {
      user.value = null
      return null
    }
  }

  async function bootstrap() {
    if (bootstrapped.value) return
    await refreshLoginUser()
    bootstrapped.value = true
  }

  async function login(payload: { userAccount: string; userPassword: string }) {
    const data = await http.post<LoginUserVO>('/user/login', payload)
    user.value = data
    return data
  }

  async function register(payload: { userAccount: string; userPassword: string; checkPassword: string }) {
    await http.post<number>('/user/register', payload)
  }

  async function logout() {
    try {
      await http.post<boolean>('/user/logout')
    } finally {
      user.value = null
    }
  }

  return { user, bootstrapped, isLoggedIn, bootstrap, refreshLoginUser, login, register, logout }
})
