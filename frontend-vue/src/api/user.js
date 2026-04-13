import request from './index'

export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function getUserInfo(userId) {
  return request({
    url: '/user/info',
    method: 'get',
    params: { userId }
  })
}

export function changePassword(data) {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/update',
    method: 'post',
    data
  })
}

export function createRechargeOrder(data) {
  return request({
    url: '/user/recharge/create',
    method: 'post',
    data
  })
}

export function confirmRecharge(data) {
  return request({
    url: '/user/recharge/callback',
    method: 'post',
    data
  })
}

export function getRechargeStatus(rechargeNo) {
  return request({
    url: '/user/recharge/status',
    method: 'post',
    data: { rechargeNo }
  })
}
