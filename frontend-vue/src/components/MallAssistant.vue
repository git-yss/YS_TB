<template>
  <div class="assistant-wrapper">
    <el-button class="assistant-float-btn" type="primary" circle @click="visible = true">
      AI
    </el-button>

    <el-drawer v-model="visible" title="商城助手" :with-header="true" size="380px">
      <div class="assistant-chat">
        <div class="session-bar">
          <el-select v-model="sessionId" placeholder="选择会话" style="width: 210px" @change="onSessionChange">
            <el-option v-for="s in sessions" :key="s.sessionId" :label="s.title || s.sessionId" :value="s.sessionId" />
          </el-select>
          <el-button size="small" @click="newSession">新建</el-button>
          <el-button size="small" type="danger" plain @click="removeSession">删除</el-button>
        </div>
        <div class="message-list" ref="listRef">
          <div v-for="(item, idx) in messages" :key="idx" :class="['msg', item.role]">
            <div class="bubble" :class="{ streaming: item.streaming }">
              <div class="text">{{ item.text }}</div>
              <template v-if="item.payload && item.payload.type === 'order_list'">
                <div class="cards">
                  <div class="card" v-for="row in item.payload.items || []" :key="row.orderId">
                    <div>订单号：{{ row.orderId }}</div>
                    <div>状态：{{ row.status }}</div>
                    <div>金额：¥{{ row.totalAmount }}</div>
                  </div>
                </div>
              </template>
              <template v-if="item.payload && item.payload.type === 'goods_search'">
                <div class="cards">
                  <div class="card" v-for="g in ((item.payload.result || {}).list || [])" :key="g.id">
                    <div>{{ g.name }}</div>
                    <div>¥{{ g.price }}</div>
                  </div>
                </div>
              </template>
              <template v-if="item.payload && (item.payload.toolTraces || []).length">
                <div class="trace-wrap">
                  <div class="trace-title">调用轨迹</div>
                  <div class="trace-item" v-for="(t, i) in item.payload.toolTraces" :key="i">
                    <span>{{ t.tool }}</span>
                    <span class="trace-status">[{{ t.status || 'ok' }}]</span>
                    <span class="trace-duration">{{ t.durationMs != null ? t.durationMs + 'ms' : '' }}</span>
                    <code>{{ JSON.stringify(t.args || {}) }}</code>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div class="input-bar">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="问我：最近订单、支付情况、商品介绍"
            @keyup.enter="send"
          />
          <el-button type="primary" :loading="sending" @click="send">发送</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  assistantChatStream,
  listAssistantSessions,
  createAssistantSession,
  deleteAssistantSession,
  getAssistantSessionHistory
} from '@/api/assistant'
import { useUserStore } from '@/store/user'

const visible = ref(false)
const sending = ref(false)
const input = ref('')
const listRef = ref(null)
const userStore = useUserStore()
const sessionId = ref(localStorage.getItem('assistantSessionId') || '')
const sessions = ref([])
const streamAbort = ref(null)

const messages = ref([
  { role: 'bot', text: '你好，我是商城助手。可以帮你查询最近订单、支付情况、介绍商品。' }
])

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  sending.value = true
  const botIdx = messages.value.length
  messages.value.push({ role: 'bot', text: '', payload: {}, streaming: true })
  scrollToBottom()
  if (streamAbort.value) {
    streamAbort.value.abort()
  }
  const ac = new AbortController()
  streamAbort.value = ac
  try {
    await assistantChatStream(
      {
        userId: userStore.userInfo?.id,
        message: text,
        sessionId: sessionId.value || undefined
      },
      {
        onDelta: (t) => {
          const row = messages.value[botIdx]
          if (row) row.text += t
          scrollToBottom()
        },
        onDone: (data) => {
          const row = messages.value[botIdx]
          if (!row) return
          row.streaming = false
          if (data.sessionId) {
            sessionId.value = String(data.sessionId)
            localStorage.setItem('assistantSessionId', sessionId.value)
          }
          row.text = data.reply != null && String(data.reply) !== '' ? String(data.reply) : row.text
          row.payload = data
        },
        signal: ac.signal
      }
    )
  } catch (e) {
    if (e.name === 'AbortError') {
      const row = messages.value[botIdx]
      if (row) {
        row.streaming = false
        if (!row.text) row.text = '（已中断）'
      }
    } else {
      ElMessage.error(e.message || '助手响应失败')
      const row = messages.value[botIdx]
      if (row) {
        row.streaming = false
        row.text = row.text || '抱歉，我刚刚开小差了，请稍后重试。'
      }
    }
  } finally {
    streamAbort.value = null
    sending.value = false
    scrollToBottom()
  }
}

async function loadSessions() {
  const userId = userStore.userInfo?.id
  const res = await listAssistantSessions(userId)
  sessions.value = Array.isArray(res.data) ? res.data : []
  if (!sessionId.value && sessions.value.length) {
    sessionId.value = sessions.value[0].sessionId
  }
  if (!sessionId.value) {
    await newSession()
  }
}

async function newSession() {
  const userId = userStore.userInfo?.id
  const res = await createAssistantSession({ userId, title: '新会话' })
  const sid = res.data?.sessionId
  if (sid) {
    sessionId.value = String(sid)
    localStorage.setItem('assistantSessionId', sessionId.value)
    messages.value = [{ role: 'bot', text: '新会话已创建，你可以开始提问了。' }]
    await loadSessions()
    scrollToBottom()
  }
}

async function removeSession() {
  if (!sessionId.value) return
  try {
    await ElMessageBox.confirm('确定删除当前会话吗？', '提示', { type: 'warning' })
    const userId = userStore.userInfo?.id
    await deleteAssistantSession({ userId, sessionId: sessionId.value })
    sessionId.value = ''
    localStorage.removeItem('assistantSessionId')
    await loadSessions()
    await onSessionChange()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '删除会话失败')
    }
  }
}

async function onSessionChange() {
  if (!sessionId.value) return
  localStorage.setItem('assistantSessionId', sessionId.value)
  try {
    const userId = userStore.userInfo?.id
    const res = await getAssistantSessionHistory({ userId, sessionId: sessionId.value })
    const rows = Array.isArray(res.data) ? res.data : []
    messages.value = rows.length ? rows : [{ role: 'bot', text: '该会话暂无历史消息。' }]
    scrollToBottom()
  } catch (e) {
    ElMessage.error(e.message || '加载会话历史失败')
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onMounted(async () => {
  try {
    await loadSessions()
    await onSessionChange()
  } catch (e) {
    ElMessage.error(e.message || '初始化助手失败')
  }
})
</script>

<style scoped>
.assistant-float-btn {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  width: 56px;
  height: 56px;
  font-weight: bold;
}

.assistant-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.session-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
}

.msg {
  display: flex;
  margin-bottom: 10px;
}

.msg.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 86%;
  background: #f5f7fa;
  border-radius: 10px;
  padding: 8px 10px;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
}

.bubble.streaming .text {
  min-height: 1.2em;
}

.bubble.streaming .text:empty::after {
  content: '…';
  opacity: 0.5;
}

.cards {
  margin-top: 8px;
}

.card {
  background: #fff;
  color: #333;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px;
  margin-bottom: 6px;
}

.input-bar {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
  display: grid;
  gap: 8px;
}

.trace-wrap {
  margin-top: 8px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 6px;
}

.trace-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.trace-item {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  word-break: break-all;
}

.trace-status {
  margin-left: 4px;
  color: #909399;
}

.trace-duration {
  margin-left: 4px;
  color: #67c23a;
}

.trace-item code {
  margin-left: 6px;
  background: #f2f6fc;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
