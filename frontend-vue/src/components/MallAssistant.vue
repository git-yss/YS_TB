<template>
  <div class="assistant-wrapper">
    <el-button class="assistant-float-btn" type="primary" circle @click="visible = true">
      AI
    </el-button>

    <el-drawer v-model="visible" title="商城助手" :with-header="true" size="380px">
      <div class="assistant-chat">
        <div class="message-list" ref="listRef">
          <div v-for="(item, idx) in messages" :key="idx" :class="['msg', item.role]">
            <div class="bubble">
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
import { nextTick, ref } from 'vue'
import { assistantChat } from '@/api/assistant'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const visible = ref(false)
const sending = ref(false)
const input = ref('')
const listRef = ref(null)
const userStore = useUserStore()

const messages = ref([
  { role: 'bot', text: '你好，我是商城助手。可以帮你查询最近订单、支付情况、介绍商品。' }
])

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  messages.value.push({ role: 'user', text })
  input.value = ''
  sending.value = true
  scrollToBottom()
  try {
    const res = await assistantChat({
      userId: userStore.userInfo?.id,
      message: text
    })
    const data = res.data || {}
    messages.value.push({
      role: 'bot',
      text: data.reply || '收到，我再试试理解你的问题。',
      payload: data
    })
  } catch (e) {
    ElMessage.error(e.message || '助手响应失败')
    messages.value.push({ role: 'bot', text: '抱歉，我刚刚开小差了，请稍后重试。' })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
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
</style>
