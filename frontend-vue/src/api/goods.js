import request from './index'

// 搜索商品
export function searchGoods(params) {
  return request({
    url: '/goods/search',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getGoodsDetail(id) {
  return request({
    url: `/goods/detail/${id}`,
    method: 'get'
  })
}

// 获取商品分类
export function getCategoryList() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 获取热门商品
export function getHotGoods(limit = 10) {
  return request({
    url: '/goods/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取分类下的商品
export function getGoodsByCategory(params) {
  return request({
    url: '/goods/category',
    method: 'get',
    params
  })
}
