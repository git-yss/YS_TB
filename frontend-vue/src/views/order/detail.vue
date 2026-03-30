<template>
  <div class="order-detail-page">
    <el-page-header @back="goBack" content="订单详情" />
    <el-skeleton v-if="loading" :rows="8" animated style="margin-top: 20px" />
    <el-alert v-else-if="errorMsg" :title="errorMsg" type="error" show-icon />
    <template v-else-if="payload">
      <el-card class="block" header="订单信息">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ payload.order?.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ payload.statusDesc || '—' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDateTime(payload.order?.addtime) }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ payload.order?.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ payload.order?.unitPrice }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ payload.order?.totalAmount }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card v-if="payload.goods" class="block" header="商品信息">
        <p><strong>名称：</strong>{{ payload.goods.name }}</p>
        <p><strong>介绍：</strong>{{ payload.goods.introduce || '—' }}</p>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { useUserStore } from '@/store/user'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const errorMsg = ref('')
const raw = ref(null)

const payload = computed(() => raw.value)

function goBack() {
  router.push('/order')
}

async function load() {
  const orderId = Number(route.params.id)
  const uid = userStore.userInfo?.id
  if (!uid || !orderId) {
    errorMsg.value = '参数错误'
    loading.value = false
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await getOrderDetail(orderId, uid)
    raw.value = res.data
  } catch (e) {
    errorMsg.value = e.message || '加载失败'
    ElMessage.error(errorMsg.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.order-detail-page {
  padding: 20px;
}
.block {
  margin-top: 20px;
}
</style>
