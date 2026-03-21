import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || ''
  }),
  actions: {
    setUserInfo(info) {
      this.userInfo = info
    },
    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    logout() {
      this.userInfo = null
      this.token = ''
      localStorage.removeItem('token')
    }
  }
})