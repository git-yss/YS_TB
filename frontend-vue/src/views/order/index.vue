<template>
  <div class="order-container">
    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部订单" name="all">
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)"> 查看详情 </el-button>
                <el-button
                  v-if="scope.row.status === 'pending'"
                  type="success"
                  size="small"
                  :loading="payingId === scope.row.id"
                  @click="handlePay(scope.row)"
                >
                  去支付
                </el-button>
                <el-button
                  v-if="scope.row.status === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleCancelOrder(scope.row)"
                >
                  取消订单
                </el-button>
                <el-button
                  v-if="scope.row.status === 'shipped'"
                  type="success"
                  size="small"
                  @click="handleConfirmReceipt(scope.row)"
                >
                  确认收货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" />
        </el-tab-pane>

        <el-tab-pane label="待付款" name="pending">
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无待付款订单" />
          <el-table v-else :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
                <el-button type="success" size="small" :loading="payingId === scope.row.id" @click="handlePay(scope.row)">
                  去支付
                </el-button>
                <el-button type="danger" size="small" @click="handleCancelOrder(scope.row)">取消订单</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="待发货" name="processing">
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无待发货订单" />
          <el-table v-else :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="待收货" name="shipped">
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无待收货订单" />
          <el-table v-else :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
                <el-button type="success" size="small" @click="handleConfirmReceipt(scope.row)">确认收货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已完成" name="completed">
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无已完成订单" />
          <el-table v-else :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已取消" name="cancelled">
          <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无已取消订单" />
          <el-table v-else :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope"> ¥{{ scope.row.totalAmount.toFixed(2) }} </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { listOrders, cancelOrder, confirmOrderReceipt } from '@/api/order'
import { payOrder } from '@/api/cart'
import { useUserStore } from '@/store/user'
import { groupOrderRows } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const payingId = ref(null)
const activeTab = ref('all')
const orders = ref([])

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter((o) => o.status === activeTab.value)
})

async function loadOrders() {
  const uid = userStore.userInfo?.id
  if (!uid) {
    orders.value = []
    return
  }
  loading.value = true
  try {
    const res = await listOrders(uid)
    orders.value = groupOrderRows(res.data || [])
  } catch (e) {
    ElMessage.error(e.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)

const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'processing':
      return 'info'
    case 'shipped':
      return 'success'
    case 'completed':
      return 'success'
    case 'cancelled':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待付款'
    case 'processing':
      return '待发货'
    case 'shipped':
      return '待收货'
    case 'completed':
      return '已完成'
    case 'cancelled':
      return '已取消'
    default:
      return '未知状态'
  }
}

const handleViewDetail = (order) => {
  router.push(`/order/detail/${order.id}`)
}

const handlePay = async (order) => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  payingId.value = order.id
  try {
    await payOrder({ userId: uid, orderIds: String(order.id) })
    ElMessage.success('支付成功')
    await loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    payingId.value = null
  }
}

const handleCancelOrder = async (order) => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  try {
    await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(order.id, uid)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (e) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '取消失败')
  }
}

const handleConfirmReceipt = async (order) => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  try {
    await confirmOrderReceipt(order.id, uid)
    ElMessage.success('确认收货成功')
    await loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
</script>

<style scoped>
.order-container {
  padding: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
