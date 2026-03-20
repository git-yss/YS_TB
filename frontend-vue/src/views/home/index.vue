<template>
  <div class="home-container">
    <el-card class="banner-card">
      <el-carousel :interval="4000" type="card" height="300px">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <el-image :src="item.image" fit="cover" style="height: 300px" />
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <el-col :span="6">
        <el-card class="category-card">
          <template #header>
            <div class="card-header">
              <h3>商品分类</h3>
            </div>
          </template>
          <el-menu class="category-menu">
            <el-menu-item v-for="category in categories" :key="category.id" @click="handleCategoryClick(category)">
              {{ category.name }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card class="product-card">
          <template #header>
            <div class="card-header">
              <h3>热门商品</h3>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="6" v-for="product in products" :key="product.id">
              <el-card class="product-item" shadow="hover">
                <el-image :src="product.image" fit="cover" style="height: 200px" />
                <div class="product-info">
                  <h4>{{ product.name }}</h4>
                  <p class="price">¥{{ product.price }}</p>
                  <p class="description">{{ product.description }}</p>
                  <el-button type="primary" @click="handleProductDetail(product)">查看详情</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const banners = ref([
  { id: 1, image: 'https://picsum.photos/seed/banner1/800/300.jpg' },
  { id: 2, image: 'https://picsum.photos/seed/banner2/800/300.jpg' },
  { id: 3, image: 'https://picsum.photos/seed/banner3/800/300.jpg' }
])

const categories = ref([
  { id: 1, name: '电子产品' },
  { id: 2, name: '服装鞋帽' },
  { id: 3, name: '家居用品' },
  { id: 4, name: '图书音像' },
  { id: 5, name: '运动户外' },
  { id: 6, name: '美妆护肤' }
])

const products = ref([
  { 
    id: 1, 
    name: '智能手机', 
    price: 2999, 
    description: '最新款智能手机，性能强劲', 
    image: 'https://picsum.photos/seed/product1/300/200.jpg' 
  },
  { 
    id: 2, 
    name: '笔记本电脑', 
    price: 5999, 
    description: '轻薄便携，性能卓越', 
    image: 'https://picsum.photos/seed/product2/300/200.jpg' 
  },
  { 
    id: 3, 
    name: '无线耳机', 
    price: 999, 
    description: '降噪效果出色，音质完美', 
    image: 'https://picsum.photos/seed/product3/300/200.jpg' 
  },
  { 
    id: 4, 
    name: '智能手表', 
    price: 1999, 
    description: '健康监测，智能提醒', 
    image: 'https://picsum.photos/seed/product4/300/200.jpg' 
  },
  { 
    id: 5, 
    name: '平板电脑', 
    price: 3999, 
    description: '大屏显示，便携办公', 
    image: 'https://picsum.photos/seed/product5/300/200.jpg' 
  },
  { 
    id: 6, 
    name: '蓝牙音箱', 
    price: 599, 
    description: '音质出色，便携时尚', 
    image: 'https://picsum.photos/seed/product6/300/200.jpg' 
  }
])

const handleCategoryClick = (category) => {
  ElMessage.info(`选择了分类: ${category.name}`)
}

const handleProductDetail = (product) => {
  router.push(`/goods/${product.id}`)
}
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.banner-card {
  margin-bottom: 20px;
}

.content-row {
  margin-bottom: 20px;
}

.category-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-menu {
  height: 400px;
  overflow-y: auto;
}

.product-card {
  height: 100%;
}

.product-item {
  margin-bottom: 20px;
}

.product-info {
  padding: 10px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.description {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
</style>