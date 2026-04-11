import request from './index'

export function assistantChat(data) {
  return request({
    url: '/assistant/chat',
    method: 'post',
    data: data || {}
  })
}

/**
 * 商城助手流式问答（SSE）。事件：delta { t }，done 为与同步 chat 相同结构的 JSON。
 */
export async function assistantChatStream(payload, { onDelta, onDone, signal }) {
  const token = localStorage.getItem('token')
  const res = await fetch('/api/assistant/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload || {}),
    signal
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `请求失败 ${res.status}`)
  }
  if (!res.body) {
    throw new Error('响应不支持流式读取')
  }
  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let sawDone = false
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    let sep
    while ((sep = buffer.indexOf('\n\n')) >= 0) {
      const block = buffer.slice(0, sep)
      buffer = buffer.slice(sep + 2)
      let eventName = 'message'
      const dataLines = []
      for (const line of block.split('\n')) {
        if (line.startsWith('event:')) {
          eventName = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        }
      }
      const dataStr = dataLines.join('\n')
      if (!dataStr) continue
      let data
      try {
        data = JSON.parse(dataStr)
      } catch {
        continue
      }
      if (eventName === 'delta' && data && typeof data.t === 'string') {
        onDelta(data.t)
      } else if (eventName === 'done') {
        sawDone = true
        onDone(data || {})
        return
      }
    }
  }
  if (!sawDone) {
    throw new Error('流式响应未正常结束')
  }
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
