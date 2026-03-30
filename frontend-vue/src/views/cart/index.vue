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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCartList, deleteFromCart, settleCart, updateCartNum } from '@/api/cart'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const cartItems = ref([])

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
})

function mapCartRow(item) {
  const itemId = item.itemId
  return {
    id: itemId,
    itemId,
    name: `商品 #${itemId}`,
    price: item.price != null ? Number(item.price) : 0,
    quantity: item.num != null ? item.num : 1,
    stock: 9999,
    image: `https://picsum.photos/seed/cart${itemId}/80/80.jpg`
  }
}

async function loadCart() {
  const uid = userStore.userInfo?.id
  if (!uid) {
    cartItems.value = []
    return
  }
  loading.value = true
  try {
    const res = await getCartList(uid)
    const list = res.data
    cartItems.value = Array.isArray(list) ? list.map(mapCartRow) : []
  } catch (e) {
    ElMessage.error(e.message || '加载购物车失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadCart)

const handleQuantityChange = async (row) => {
  const uid = userStore.userInfo?.id
  if (!uid) return

  try {
    await updateCartNum(row.itemId, uid, row.quantity)
    ElMessage.success('数量更新成功')
    await loadCart()
  } catch (e) {
    ElMessage.error(e.message || '数量更新失败')
    // 失败时重新拉取一次，避免前端与 Redis 不一致
    await loadCart()
  }
}

const handleRemove = async (row) => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  try {
    await deleteFromCart(row.itemId, uid)
    const index = cartItems.value.findIndex((c) => c.itemId === row.itemId)
    if (index !== -1) cartItems.value.splice(index, 1)
    ElMessage.success('商品已移除')
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

const handleClearCart = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) return
  loading.value = true
  try {
    for (const row of [...cartItems.value]) {
      await deleteFromCart(row.itemId, uid)
    }
    cartItems.value = []
    ElMessage.success('购物车已清空')
  } catch (e) {
    ElMessage.error(e.message || '清空失败')
  } finally {
    loading.value = false
  }
}

const handleCheckout = async () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  const uid = userStore.userInfo?.id
  if (!uid) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const itemIds = cartItems.value.map((r) => r.itemId).join(',')
  loading.value = true
  try {
    await settleCart({ userId: uid, items: itemIds })
    ElMessage.success('已生成订单，请前往「我的订单」支付')
    await loadCart()
    router.push('/order')
  } catch (e) {
    ElMessage.error(e.message || '结算失败')
  } finally {
    loading.value = false
  }
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
