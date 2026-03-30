<template>
  <div class="product-detail-container">
    <el-alert v-if="loadError" :title="loadError" type="error" show-icon style="margin-bottom: 16px" />
    <el-card class="product-card" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-image :src="product.image" fit="contain" class="product-image" />
        </el-col>
        <el-col :span="16">
          <div class="product-info">
            <h1>{{ product.name }}</h1>
            <p class="price">¥{{ product.price }}</p>
            <div class="product-meta">
              <el-tag type="success">库存: {{ product.stock }}</el-tag>
              <el-tag type="info">销量: {{ product.sales }}</el-tag>
              <el-tag type="warning">评分: {{ product.rating }}分</el-tag>
            </div>
            <div class="product-description">
              <h3>商品描述</h3>
              <p>{{ product.description }}</p>
            </div>
            <div class="product-specs">
              <h3>规格参数</h3>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="品牌">{{ product.brand }}</el-descriptions-item>
                <el-descriptions-item label="分类">{{ product.category }}</el-descriptions-item>
                <el-descriptions-item label="产地">{{ product.origin }}</el-descriptions-item>
                <el-descriptions-item label="保质期">{{ product.shelfLife }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <div class="product-actions">
              <el-button type="primary" size="large" @click="handleAddToCart" :loading="loading">
                加入购物车
              </el-button>
              <el-button type="success" size="large" @click="handleBuyNow">
                立即购买
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="review-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <h2>用户评价</h2>
          <el-button type="primary" @click="handleAddReview">发表评价</el-button>
        </div>
      </template>
      <el-empty v-if="reviews.length === 0" description="暂无评价" />
      <el-timeline v-else>
        <el-timeline-item v-for="review in reviews" :key="review.id" :timestamp="review.time">
          <el-card class="review-item">
            <div class="review-header">
              <el-avatar :src="review.avatar" />
              <div class="review-user">
                <div class="user-name">{{ review.username }}</div>
                <div class="user-rating">
                  <el-rate v-model="review.rating" disabled show-text />
                </div>
              </div>
            </div>
            <div class="review-content">
              {{ review.content }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-dialog
      v-model="reviewDialogVisible"
      title="发表商品评价"
      width="600px"
      @closed="resetReviewForm"
    >
      <el-form :model="reviewForm" label-width="90px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" :max="5" show-text />
        </el-form-item>
        <el-form-item label="是否匿名">
          <el-switch v-model="reviewForm.isAnonymous" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-input
            v-model="reviewForm.images"
            placeholder="可填多个图片 URL，用逗号分隔"
          />
        </el-form-item>
        <el-form-item label="订单">
          <el-input v-model="selectedOrderIdDisplay" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingReview" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsDetail } from '@/api/goods'
import { addToCart } from '@/api/cart'
import { settleCart } from '@/api/cart'
import { listOrders } from '@/api/order'
import { addReview, listReviewsByGoods } from '@/api/review'
import { useUserStore } from '@/store/user'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadError = ref('')

const product = ref({
  id: null,
  name: '',
  price: 0,
  stock: 0,
  sales: 0,
  rating: 0,
  image: '',
  description: '',
  brand: '',
  category: '',
  origin: '',
  shelfLife: ''
})

const goodsId = computed(() => Number(route.params.id))

const reviews = ref([])

const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const selectedOrderId = ref(null)
const selectedOrderIdDisplay = computed(() => {
  return selectedOrderId.value != null ? String(selectedOrderId.value) : '未选择'
})

const reviewForm = ref({
  rating: 5,
  isAnonymous: 0,
  content: '',
  images: ''
})

async function fetchReviews(gid) {
  try {
    const res = await listReviewsByGoods(gid, 1, 30)
    const page = res.data
    const list = page?.list || []
    reviews.value = list.map((r) => ({
      id: r.id,
      username: r.username || '用户',
      avatar: `https://picsum.photos/seed/r${r.id}/40/40.jpg`,
      rating: r.rating,
      content: r.content || '',
      time: formatDateTime(r.createdAt)
    }))
  } catch {
    reviews.value = []
  }
}

async function fetchProduct() {
  if (!goodsId.value || Number.isNaN(goodsId.value)) {
    loadError.value = '无效的商品 ID'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const res = await getGoodsDetail(goodsId.value)
    const g = res.data
    if (!g) {
      loadError.value = '商品不存在'
      return
    }
    product.value = {
      id: g.id,
      name: g.name || '',
      price: g.price != null ? Number(g.price) : 0,
      stock: g.inventory != null ? g.inventory : 0,
      sales: 0,
      rating: 0,
      image: g.image || '',
      description: g.introduce || '',
      brand: g.brand || '',
      category: g.category || '',
      origin: '—',
      shelfLife: '—'
    }
    await fetchReviews(g.id)
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(fetchProduct)

const handleAddToCart = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loading.value = true
  try {
    await addToCart({
      itemId: product.value.id,
      price: product.value.price,
      num: 1,
      userId: uid,
      id: 0,
      statusEnum: 0
    })
    ElMessage.success('商品已加入购物车')
  } catch (error) {
    ElMessage.error(error.message || '加入购物车失败')
  } finally {
    loading.value = false
  }
}

const handleBuyNow = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.value?.id) {
    ElMessage.warning('商品信息异常')
    return
  }

  loading.value = true
  try {
    // 立即购买：先加入购物车，再直接结算生成订单（与后端现有逻辑对齐）
    await addToCart({
      itemId: product.value.id,
      price: product.value.price,
      num: 1,
      userId: uid,
      id: 0,
      statusEnum: 0
    })

    const res = await settleCart({ userId: uid, items: String(product.value.id) })
    const orderId = res?.data
    ElMessage.success('下单成功')

    // 优先跳转订单详情（若 orderId 有值），否则回订单列表
    if (orderId) {
      router.push(`/order/detail/${orderId}`)
    } else {
      router.push('/order')
    }
  } catch (e) {
    ElMessage.error(e?.message || '下单失败')
  } finally {
    loading.value = false
  }
}

const handleAddReview = () => {
  ;(async () => {
    const uid = userStore.userInfo?.id
    if (!uid) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    // 后端校验需要：orderId 对应商品且订单状态=已支付(3)
    const gid = goodsId.value
    const res = await listOrders(uid)
    const rows = Array.isArray(res.data) ? res.data : []
    const eligible = rows.find((o) => String(o.goodsId) === String(gid) && String(o.status) === '3')

    if (!eligible) {
      ElMessage.warning('未找到可评价订单（需要已支付订单）')
      return
    }

    selectedOrderId.value = eligible.id
    reviewDialogVisible.value = true
  })().catch((e) => {
    ElMessage.error(e?.message || '获取订单失败')
  })
}

function resetReviewForm() {
  submittingReview.value = false
  selectedOrderId.value = null
  reviewForm.value.rating = 5
  reviewForm.value.isAnonymous = 0
  reviewForm.value.content = ''
  reviewForm.value.images = ''
}

async function submitReview() {
  const uid = userStore.userInfo?.id
  if (!uid) return
  if (!selectedOrderId.value) {
    ElMessage.warning('缺少订单信息')
    return
  }

  const rating = Number(reviewForm.value.rating)
  if (!Number.isFinite(rating) || rating < 1 || rating > 5) {
    ElMessage.warning('评分必须在 1-5 之间')
    return
  }
  const payload = {
    orderId: selectedOrderId.value,
    goodsId: goodsId.value,
    userId: uid,
    rating,
    content: reviewForm.value.content || null,
    images: reviewForm.value.images || null,
    isAnonymous: reviewForm.value.isAnonymous
  }

  submittingReview.value = true
  try {
    await addReview(payload)
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    await fetchReviews(goodsId.value)
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submittingReview.value = false
  }
}
</script>

<style scoped>
.product-detail-container {
  padding: 20px;
}

.product-card {
  margin-bottom: 20px;
}

.product-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
}

.product-info {
  padding: 0 20px;
}

.price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
  margin: 10px 0;
}

.product-meta {
  margin: 15px 0;
}

.product-description {
  margin: 20px 0;
  line-height: 1.6;
}

.product-specs {
  margin: 20px 0;
}

.product-actions {
  margin-top: 20px;
}

.review-card {
  margin-top: 20px;
}

.review-item {
  margin-bottom: 15px;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.review-user {
  margin-left: 10px;
}

.user-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.user-rating {
  display: flex;
  align-items: center;
}
</style>