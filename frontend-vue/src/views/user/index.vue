<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>

      <el-descriptions v-if="displayUser" :column="1" border>
        <el-descriptions-item label="用户 ID">{{ displayUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ displayUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ displayUser.email || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ displayUser.tel || '—' }}</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ displayUser.balance != null ? displayUser.balance : '0' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未登录或暂无用户信息" />

      <div v-if="displayUser" class="actions">
        <el-button type="primary" :loading="refreshing" @click="refresh">刷新资料</el-button>
        <el-button type="warning" @click="openRechargeDialog">余额充值</el-button>
      </div>

      <el-divider v-if="displayUser" />

      <el-form v-if="displayUser" :model="profileForm" label-width="90px" class="form" @submit.prevent="submitProfile">
        <el-form-item label="年龄">
          <el-input v-model="profileForm.age" placeholder="例如 25" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profileForm.sex" placeholder="请选择" style="width: 160px">
            <el-option label="未知" value="" />
            <el-option label="男" value="1" />
            <el-option label="女" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="updatingProfile" @click="submitProfile">保存资料</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="displayUser" />

      <el-form v-if="displayUser" :model="pwdForm" label-width="90px" class="form" @submit.prevent="submitPassword">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（>=6）" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="changingPwd" @click="submitPassword">修改密码</el-button>
        </el-form-item>
      </el-form>

      <el-dialog v-model="rechargeVisible" title="余额充值" width="420px">
        <el-form :model="rechargeForm" label-width="100px">
          <el-form-item label="充值金额">
            <el-input-number v-model="rechargeForm.amount" :min="1" :max="50000" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="支付渠道">
            <el-radio-group v-model="rechargeForm.channel">
              <el-radio label="ALIPAY">支付宝</el-radio>
              <el-radio label="WECHAT">微信支付</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="currentRecharge.payContentType === 'QRCODE_URL' && currentRecharge.payContent" label="支付链接">
            <el-link :href="currentRecharge.payContent" target="_blank" type="primary">打开微信支付链接</el-link>
          </el-form-item>
          <el-form-item v-if="currentRecharge.payContentType === 'HTML_FORM'" label="支付宝">
            <el-button type="primary" plain @click="openAlipayForm">打开支付宝收银台</el-button>
          </el-form-item>
          <el-form-item v-if="currentRecharge.rechargeNo" label="充值单号">
            <el-text>{{ currentRecharge.rechargeNo }}</el-text>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="closeRechargeDialog">关闭</el-button>
          <el-button type="primary" :loading="creatingRecharge" @click="createRecharge">发起充值</el-button>
          <el-button
            type="success"
            :disabled="!currentRecharge.rechargeNo"
            :loading="confirmingRecharge"
            @click="confirmRechargeSuccess"
          >
            我已完成支付
          </el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateUserInfo, changePassword, createRechargeOrder, confirmRecharge, getRechargeStatus } from '@/api/user'

const userStore = useUserStore()
const refreshing = ref(false)
const updatingProfile = ref(false)
const changingPwd = ref(false)
const rechargeVisible = ref(false)
const creatingRecharge = ref(false)
const confirmingRecharge = ref(false)
const pollingRecharge = ref(false)
let rechargePollTimer = null
let rechargePollCount = 0

const displayUser = computed(() => {
  const info = userStore.userInfo
  // 兼容后端两种返回：直接用户对象 / 聚合对象 { user, addresses }
  return info?.user || info || null
})

const profileForm = ref({
  age: '',
  sex: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: ''
})

const rechargeForm = ref({
  amount: 100,
  channel: 'ALIPAY'
})

const currentRecharge = ref({
  rechargeNo: '',
  payContent: '',
  payContentType: '',
  channel: 'ALIPAY'
})

watch(
  displayUser,
  (u) => {
    if (!u) return
    profileForm.value.age = u.age != null ? String(u.age) : ''
    // 后端 sex 可能是 '0'/'1'
    profileForm.value.sex = u.sex != null ? String(u.sex) : ''
  },
  { immediate: true }
)

async function refresh() {
  const id = displayUser.value?.id
  if (!id) return
  refreshing.value = true
  try {
    const res = await getUserInfo(id)
    userStore.setUserInfo(res.data?.user || res.data)
    ElMessage.success('已更新')
  } catch (e) {
    ElMessage.error(e.message || '刷新失败')
  } finally {
    refreshing.value = false
  }
}

async function submitProfile() {
  const uid = displayUser.value?.id
  if (!uid) return
  updatingProfile.value = true
  try {
    await updateUserInfo({
      userId: uid,
      age: profileForm.value.age || undefined,
      sex: profileForm.value.sex || undefined
    })
    ElMessage.success('资料已保存')
    await refresh()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    updatingProfile.value = false
  }
}

async function submitPassword() {
  const uid = displayUser.value?.id
  if (!uid) return
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写旧密码和新密码')
    return
  }
  changingPwd.value = true
  try {
    await changePassword({
      userId: uid,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.value.oldPassword = ''
    pwdForm.value.newPassword = ''
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}

function openRechargeDialog() {
  rechargeVisible.value = true
}

function closeRechargeDialog() {
  stopRechargePolling()
  rechargeVisible.value = false
}

async function createRecharge() {
  const uid = displayUser.value?.id
  if (!uid) return
  if (!rechargeForm.value.amount || Number(rechargeForm.value.amount) <= 0) {
    ElMessage.warning('请输入有效充值金额')
    return
  }
  creatingRecharge.value = true
  try {
    const res = await createRechargeOrder({
      userId: uid,
      amount: rechargeForm.value.amount,
      channel: rechargeForm.value.channel
    })
    const data = res.data || {}
    currentRecharge.value = {
      rechargeNo: data.rechargeNo || '',
      payContent: data.payContent || '',
      payContentType: data.payContentType || '',
      channel: data.channel || rechargeForm.value.channel
    }
    ElMessage.success('充值订单已创建，请完成支付')
    startRechargePolling()
  } catch (e) {
    ElMessage.error(e.message || '创建充值订单失败')
  } finally {
    creatingRecharge.value = false
  }
}

async function checkRechargeStatusOnce() {
  const rechargeNo = currentRecharge.value.rechargeNo
  if (!rechargeNo) return
  const res = await getRechargeStatus(rechargeNo)
  const status = res.data?.status
  if (status === 'SUCCESS') {
    stopRechargePolling()
    ElMessage.success('检测到充值成功，余额已更新')
    currentRecharge.value = { rechargeNo: '', payContent: '', payContentType: '', channel: rechargeForm.value.channel }
    await refresh()
  }
}

function startRechargePolling() {
  stopRechargePolling()
  if (!currentRecharge.value.rechargeNo) return
  pollingRecharge.value = true
  rechargePollCount = 0
  rechargePollTimer = setInterval(async () => {
    rechargePollCount += 1
    try {
      await checkRechargeStatusOnce()
      if (rechargePollCount >= 40) {
        stopRechargePolling()
      }
    } catch {
      // 轮询错误不打断用户支付流程
    }
  }, 3000)
}

function stopRechargePolling() {
  pollingRecharge.value = false
  if (rechargePollTimer) {
    clearInterval(rechargePollTimer)
    rechargePollTimer = null
  }
}

async function confirmRechargeSuccess() {
  const uid = displayUser.value?.id
  if (!uid || !currentRecharge.value.rechargeNo) return
  confirmingRecharge.value = true
  try {
    await confirmRecharge({
      rechargeNo: currentRecharge.value.rechargeNo,
      channel: currentRecharge.value.channel,
      tradeNo: `SIM-${Date.now()}`
    })
    ElMessage.success('充值成功，余额已更新')
    currentRecharge.value = { rechargeNo: '', payContent: '', payContentType: '', channel: rechargeForm.value.channel }
    stopRechargePolling()
    await refresh()
  } catch (e) {
    ElMessage.error(e.message || '充值确认失败')
  } finally {
    confirmingRecharge.value = false
  }
}

function openAlipayForm() {
  if (!currentRecharge.value.payContent) {
    ElMessage.warning('请先发起充值')
    return
  }
  const newWin = window.open('', '_blank')
  if (!newWin) {
    ElMessage.warning('请允许浏览器弹窗后重试')
    return
  }
  newWin.document.write(currentRecharge.value.payContent)
  newWin.document.close()
}

onMounted(() => {
  if (displayUser.value?.id) {
    refresh()
  }
})

watch(rechargeVisible, (val) => {
  if (!val) stopRechargePolling()
})

onBeforeUnmount(() => {
  stopRechargePolling()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
  max-width: 640px;
}

.actions {
  margin-top: 12px;
}

.form {
  margin-top: 12px;
}
</style>
