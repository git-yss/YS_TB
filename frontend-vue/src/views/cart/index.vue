<template>
  <div class="cart-container">
    <el-card class="cart-card">
      <template #header>
        <div class="card-header">
          <h2>购物车</h2>
          <el-button type="primary" @click="handleCheckout">结算</el-button>
        </div>
      </template>
      
      <el-table :data="cartItems" style="width: 100%" v-loading="loading">
        <el-table-column prop="image" label="商品图片" width="100">
          <template #default="scope">
            <el-image :src="scope.row.image" fit="contain" style="width: 80px; height: 80px" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" width="300" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="120">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.quantity" 
              :min="1" 
              :max="scope.row.stock"
              @change="handleQuantityChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="100">
          <template #default="scope">
            ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleRemove(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-summary" v-if="cartItems.length > 0">
        <div class="summary-info">
          <span>总计: </span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-actions">
          <el-button @click="handleClearCart">清空购物车</el-button>
          <el-button type="primary" @click="handleCheckout">去结算</el-button>
        </div>
      </div>
      
      <el-empty v-else description="购物车为空" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const cartItems = ref([
  {
    id: 1,
    name: '智能手机',
    price: 2999,
    quantity: 1,
    stock: 100,
    image: 'https://picsum.photos/seed/product1/80/80.jpg'
  },
  {
    id: 2,
    name: '无线耳机',
    price: 999,
    quantity: 2,
    stock: 50,
    image: 'https://picsum.photos/seed/product3/80/80.jpg'
  }
])

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

const handleQuantityChange = (item) => {
  // 这里应该调用更新购物车数量的API
  ElMessage.success('数量已更新')
}

const handleRemove = (item) => {
  const index = cartItems.value.findIndex(cartItem => cartItem.id === item.id)
  if (index !== -1) {
    cartItems.value.splice(index, 1)
    ElMessage.success('商品已移除')
  }
}

const handleClearCart = () => {
  cartItems.value = []
  ElMessage.success('购物车已清空')
}

const handleCheckout = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  router.push('/order')
}
</script>

<style scoped>
.cart-container {
  padding: 20px;
}

.cart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.summary-info {
  font-size: 18px;
  font-weight: bold;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
}
</style>