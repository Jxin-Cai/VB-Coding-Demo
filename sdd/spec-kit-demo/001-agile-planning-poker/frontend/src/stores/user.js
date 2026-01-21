import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)
  const sessionToken = ref(null)

  const isLoggedIn = computed(() => !!sessionToken.value)

  function login(user, token) {
    currentUser.value = user
    sessionToken.value = token
    // 保存到 localStorage
    localStorage.setItem('user', JSON.stringify(user))
    localStorage.setItem('sessionToken', token)
  }

  function logout() {
    currentUser.value = null
    sessionToken.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('sessionToken')
  }

  function loadFromStorage() {
    const savedUser = localStorage.getItem('user')
    const savedToken = localStorage.getItem('sessionToken')
    if (savedUser && savedToken) {
      currentUser.value = JSON.parse(savedUser)
      sessionToken.value = savedToken
    }
  }

  // 初始化时从 localStorage 加载
  loadFromStorage()

  return {
    currentUser,
    sessionToken,
    isLoggedIn,
    login,
    logout,
    loadFromStorage
  }
})
