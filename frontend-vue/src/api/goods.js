import request from './index'

export function searchGoods(data) {
  return request({
    url: '/product/search',
    method: 'post',
    data: data || {}
  })
}

export function getGoodsDetail(id) {
  return request({
    url: '/product/detail',
    method: 'post',
    data: { goodsId: id }
  })
}

export function getCategoryList() {
  return request({
    url: '/product/categoryList',
    method: 'get'
  })
}

export function getHotGoods(limit = 10) {
  return request({
    url: '/product/hotGoods',
    method: 'post',
    data: { limit }
  })
}

export function getGoodsByCategory(params) {
  return request({
    url: '/product/listByCategory',
    method: 'post',
    data: params
  })
}
