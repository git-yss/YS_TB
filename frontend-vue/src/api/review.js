import request from './index'

export function listReviewsByGoods(goodsId, pageNum = 1, pageSize = 20) {
  return request({
    url: '/productReview/listByGoods',
    method: 'post',
    data: { goodsId, pageNum, pageSize }
  })
}

export function addReview(data) {
  return request({
    url: '/productReview/add',
    method: 'post',
    data: data || {}
  })
}
