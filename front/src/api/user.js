import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取个人信息
export const getUserProfile = () => {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 修改个人信息
export const updateUserProfile = (data) => {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 修改密码
export const updatePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 获取审批人列表（领导）
export const getApprovers = () => {
  return request({
    url: '/user/approvers',
    method: 'get'
  })
}
