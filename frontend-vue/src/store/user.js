import { defineStore } from 'pinia'

function loadStoredUserInfo() {
  try {
    const raw = localStorage.getItem('userInfo')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: loadStoredUserInfo(),
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!(state.token && state.userInfo)
  },
  actions: {
    setUserInfo(info) {
      this.userInfo = info
      if (info) {
        localStorage.setItem('userInfo', JSON.stringify(info))
      } else {
        localStorage.removeItem('userInfo')
      }
    },
    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    login(payload) {
      if (!payload) return
      const { token, userInfo } = payload
      // 兼容两种后端返回：
      // 1) { token, userInfo }
      // 2) 直接返回用户对象 { id, username, ... }
      if (typeof token === 'string') this.setToken(token)
      if (userInfo) {
        this.setUserInfo(userInfo)
        return
      }
      if (payload.id != null || payload.username != null) {
        this.setUserInfo(payload)
      }
    },
    logout() {
      this.userInfo = null
      this.token = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
