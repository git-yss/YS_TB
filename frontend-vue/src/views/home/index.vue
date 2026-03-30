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
          <el-empty v-if="categories.length === 0" description="暂无分类数据" />
          <el-menu v-else class="category-menu">
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
          <el-skeleton v-if="loading" :rows="6" animated />
          <el-empty v-else-if="products.length === 0" description="暂无商品，请检查数据库或后端服务" />
          <el-row v-else :gutter="20">
            <el-col :span="6" v-for="product in products" :key="product.id">
              <el-card class="product-item" shadow="hover">
                <el-image :src="product.image || placeholderImg" fit="cover" style="height: 200px" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHotGoods, getCategoryList } from '@/api/goods'

const router = useRouter()
const loading = ref(true)
const placeholderImg = 'https://picsum.photos/seed/placeholder/300/200.jpg'

const banners = ref([
  { id: 1, image: 'https://picsum.photos/seed/banner1/800/300.jpg' },
  { id: 2, image: 'https://picsum.photos/seed/banner2/800/300.jpg' },
  { id: 3, image: 'https://picsum.photos/seed/banner3/800/300.jpg' }
])

const categories = ref([])
const products = ref([])

function mapGoodsRow(g) {
  return {
    id: g.id,
    name: g.name,
    price: g.price != null ? Number(g.price) : 0,
    description: (g.introduce || '').slice(0, 80) + ((g.introduce || '').length > 80 ? '…' : ''),
    image: g.image
  }
}

async function loadData() {
  loading.value = true
  try {
    const [hotRes, catRes] = await Promise.all([getHotGoods(12), getCategoryList()])
    const list = hotRes.data
    products.value = Array.isArray(list) ? list.map(mapGoodsRow) : []
    const cats = catRes.data
    categories.value = Array.isArray(cats) ? cats : []
  } catch (e) {
    ElMessage.error(e.message || '加载首页数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleCategoryClick = (category) => {
  router.push({ path: '/search', query: { categoryId: category.id, name: category.name } })
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
  max-height: 400px;
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
