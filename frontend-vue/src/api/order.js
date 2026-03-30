import request from './index'

/** 订单行列表（同一 orderId 可能多行商品） */
export function listOrders(userId) {
  return request({
    url: '/order/list',
    method: 'post',
    data: { userId }
  })
}

export function getOrderDetail(orderId, userId) {
  return request({
    url: '/order/detail',
    method: 'post',
    data: { orderId, userId }
  })
}

export function cancelOrder(orderId, userId, cancelReason = '用户取消') {
  return request({
    url: '/order/cancel',
    method: 'post',
    data: { orderId, userId, cancelReason }
  })
}

export function confirmOrderReceipt(orderId, userId) {
  return request({
    url: '/order/confirmReceipt',
    method: 'post',
    data: { orderId, userId }
  })
}
