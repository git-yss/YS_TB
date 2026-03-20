<template>
  <div class="product-detail-container">
    <el-card class="product-card">
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)

const product = ref({
  id: 1,
  name: '智能手机',
  price: 2999,
  stock: 100,
  sales: 520,
  rating: 4.8,
  image: 'https://picsum.photos/seed/product1/400/400.jpg',
  description: '最新款智能手机，搭载高性能处理器，配备高清显示屏，拍照效果出色，续航能力强。',
  brand: '科技品牌',
  category: '电子产品',
  origin: '中国',
  shelfLife: '长期'
})

const reviews = ref([
  {
    id: 1,
    username: '张三',
    avatar: 'https://picsum.photos/seed/user1/40/40.jpg',
    rating: 5,
    content: '产品质量很好，运行流畅，拍照效果非常出色！',
    time: '2024-01-15 14:30'
  },
  {
    id: 2,
    username: '李四',
    avatar: 'https://picsum.photos/seed/user2/40/40.jpg',
    rating: 4,
    content: '手机整体不错，就是电池续航稍微有点短。',
    time: '2024-01-14 09:15'
  },
  {
    id: 3,
    username: '王五',
    avatar: 'https://picsum.photos/seed/user3/40/40.jpg',
    rating: 5,
    content: '物流很快，客服态度也很好，手机用着很顺手。',
    time: '2024-01-13 16:45'
  }
])

const handleAddToCart = async () => {
  loading.value = true
  try {
    // 这里应该调用添加购物车的API
    ElMessage.success('商品已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  } finally {
    loading.value = false
  }
}

const handleBuyNow = () => {
  router.push('/order')
}

const handleAddReview = () => {
  ElMessage.info('评价功能开发中...')
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