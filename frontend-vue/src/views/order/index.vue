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
          <el-table :data="orders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button 
                  v-if="scope.row.status === 'pending'" 
                  type="danger" 
                  size="small" 
                  @click="handleCancelOrder(scope.row)"
                >
                  取消订单
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="待付款" name="pending">
          <el-empty v-if="filteredOrders.length === 0" description="暂无待付款订单" />
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="warning">待付款</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button type="danger" size="small" @click="handleCancelOrder(scope.row)">
                  取消订单
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="待发货" name="processing">
          <el-empty v-if="filteredOrders.length === 0" description="暂无待发货订单" />
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="info">待发货</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="待收货" name="shipped">
          <el-empty v-if="filteredOrders.length === 0" description="暂无待收货订单" />
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="success">待收货</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button type="success" size="small" @click="handleConfirmReceipt(scope.row)">
                  确认收货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="已完成" name="completed">
          <el-empty v-if="filteredOrders.length === 0" description="暂无已完成订单" />
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="success">已完成</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="已取消" name="cancelled">
          <el-empty v-if="filteredOrders.length === 0" description="暂无已取消订单" />
          <el-table :data="filteredOrders" style="width: 100%" v-loading="loading">
            <el-table-column prop="orderNumber" label="订单号" width="200" />
            <el-table-column prop="orderTime" label="下单时间" width="180" />
            <el-table-column prop="totalAmount" label="总金额" width="100">
              <template #default="scope">
                ¥{{ scope.row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="danger">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const activeTab = ref('all')

const orders = ref([
  {
    id: 1,
    orderNumber: 'ORD202401150001',
    orderTime: '2024-01-15 10:30:00',
    totalAmount: 3998,
    status: 'pending',
    items: [
      { name: '智能手机', price: 2999, quantity: 1 },
      { name: '无线耳机', price: 999, quantity: 1 }
    ]
  },
  {
    id: 2,
    orderNumber: 'ORD202401140002',
    orderTime: '2024-01-14 15:20:00',
    totalAmount: 5999,
    status: 'processing',
    items: [
      { name: '笔记本电脑', price: 5999, quantity: 1 }
    ]
  },
  {
    id: 3,
    orderNumber: 'ORD202401130003',
    orderTime: '2024-01-13 09:15:00',
    totalAmount: 1999,
    status: 'shipped',
    items: [
      { name: '智能手表', price: 1999, quantity: 1 }
    ]
  },
  {
    id: 4,
    orderNumber: 'ORD202401120004',
    orderTime: '2024-01-12 14:45:00',
    totalAmount: 8998,
    status: 'completed',
    items: [
      { name: '智能手机', price: 2999, quantity: 2 },
      { name: '无线耳机', price: 999, quantity: 2 }
    ]
  },
  {
    id: 5,
    orderNumber: 'ORD202401110005',
    orderTime: '2024-01-11 11:30:00',
    totalAmount: 0,
    status: 'cancelled',
    items: []
  }
])

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === activeTab.value)
})

const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'processing': return 'info'
    case 'shipped': return 'success'
    case 'completed': return 'success'
    case 'cancelled': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待付款'
    case 'processing': return '待发货'
    case 'shipped': return '待收货'
    case 'completed': return '已完成'
    case 'cancelled': return '已取消'
    default: return '未知状态'
  }
}

const handleViewDetail = (order) => {
  router.push(`/order/detail/${order.id}`)
}

const handleCancelOrder = (order) => {
  const index = orders.value.findIndex(o => o.id === order.id)
  if (index !== -1) {
    orders.value[index].status = 'cancelled'
    ElMessage.success('订单已取消')
  }
}

const handleConfirmReceipt = (order) => {
  const index = orders.value.findIndex(o => o.id === order.id)
  if (index !== -1) {
    orders.value[index].status = 'completed'
    ElMessage.success('确认收货成功')
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