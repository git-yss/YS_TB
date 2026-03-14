import request from './index'

// 用户登录
export function login(data) {
  return request({
    url: '/all/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/all/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'post',
    data
  })
}
