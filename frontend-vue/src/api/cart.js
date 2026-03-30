import request from './index'

// 添加购物车
export function addToCart(data) {
  return request({
    url: '/shoppingCar/addCart',
    method: 'post',
    data
  })
}

// 获取购物车列表
export function getCartList(userId) {
  return request({
    url: '/shoppingCar/showCart',
    method: 'post',
    data: { userId }
  })
}

// 删除购物车商品
export function deleteFromCart(itemId, userId) {
  return request({
    url: '/shoppingCar/deleteById',
    method: 'post',
    data: { itemId, userId }
  })
}

// 更新购物车中商品数量
export function updateCartNum(itemId, userId, num) {
  return request({
    url: '/shoppingCar/updateCartNum',
    method: 'post',
    data: { itemId, userId, num }
  })
}

// 结算
export function settleCart(data) {
  return request({
    url: '/shoppingCar/goSettlement',
    method: 'post',
    data
  })
}

// 支付
export function payOrder(data) {
  return request({
    url: '/shoppingCar/goPay',
    method: 'post',
    data
  })
}
