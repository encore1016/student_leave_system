import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      // 不在这里显示提示，让调用方自己处理
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    let errorMessage = '网络错误，请稍后重试'
    
    if (error.response?.status === 401) {
      showToast('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    
    // 尝试从多个来源提取错误消息
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message && error.message !== 'Network Error') {
      errorMessage = error.message
    } else if (error.response?.status) {
      errorMessage = `请求失败 (${error.response.status})`
    }
    
    // 将错误消息附加到error对象上
    error.message = errorMessage
    
    // 其他错误也交给调用方处理，只返回错误对象
    return Promise.reject(error)
  }
)

export default request
