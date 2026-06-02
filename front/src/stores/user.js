import { defineStore } from 'pinia'
import { login, getUserProfile } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isStudent: (state) => state.userInfo.role === 'STUDENT',
    isCounselor: (state) => state.userInfo.role === 'COUNSELOR',
    isSecretary: (state) => state.userInfo.role === 'SECRETARY',
    isAdmin: (state) => state.userInfo.role === 'ADMIN'
  },

  actions: {
    // 登录
    async login(loginData) {
      const data = await login(loginData)
      this.token = data.token
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        name: data.name,
        role: data.role
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },

    // 获取用户信息
    async getUserInfo() {
      const data = await getUserProfile()
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
      return data
    },

    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
