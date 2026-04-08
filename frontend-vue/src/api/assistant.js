import request from './index'

export function assistantChat(data) {
  return request({
    url: '/assistant/chat',
    method: 'post',
    data: data || {}
  })
}
