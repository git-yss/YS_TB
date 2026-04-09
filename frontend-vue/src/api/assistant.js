import request from './index'

export function assistantChat(data) {
  return request({
    url: '/assistant/chat',
    method: 'post',
    data: data || {}
  })
}

export function listAssistantSessions(userId) {
  return request({
    url: '/assistant/session/list',
    method: 'get',
    params: { userId }
  })
}

export function createAssistantSession(data) {
  return request({
    url: '/assistant/session/create',
    method: 'post',
    data: data || {}
  })
}

export function deleteAssistantSession(data) {
  return request({
    url: '/assistant/session/delete',
    method: 'post',
    data: data || {}
  })
}

export function getAssistantSessionHistory(params) {
  return request({
    url: '/assistant/session/history',
    method: 'get',
    params
  })
}
