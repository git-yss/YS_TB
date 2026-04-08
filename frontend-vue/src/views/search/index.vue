<template>
  <div class="search-container">
    <el-card>
      <el-form inline @submit.prevent="onSearch">
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="商品名称" clearable style="width: 240px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="AI描述">
          <el-input
            v-model="nlQuery"
            type="textarea"
            :rows="2"
            placeholder="例如：想要一款适合学生的华为手机，预算2000-3000，库存充足"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="brand" placeholder="如 苹果/华为" clearable style="width: 160px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="priceMin" placeholder="最低" style="width: 110px" clearable />
          <span class="sep">-</span>
          <el-input v-model="priceMax" placeholder="最高" style="width: 110px" clearable />
        </el-form-item>
        <el-form-item label="库存>= " :label-width="'80px'">
          <el-input v-model="inventoryMin" placeholder="最小库存" style="width: 140px" clearable />
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="sort" placeholder="默认" style="width: 160px" clearable>
            <el-option label="默认（最新）" value="newest" />
            <el-option label="价格升序" value="priceAsc" />
            <el-option label="价格降序" value="priceDesc" />
            <el-option label="库存升序" value="inventoryAsc" />
            <el-option label="库存降序" value="inventoryDesc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-empty v-if="!loading && list.length === 0 && searched" description="没有找到相关商品" />
      <el-row v-else :gutter="16">
        <el-col :span="6" v-for="item in list" :key="item.id" style="margin-bottom: 16px">
          <el-card shadow="hover" @click="goDetail(item.id)" class="goods-card">
            <el-image :src="item.image || placeholder" fit="cover" style="height: 160px; width: 100%" />
            <div class="meta">
              <div class="name" v-html="formatHighlightedName(item.name)"></div>
              <div class="price">¥{{ item.price }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchGoods } from '@/api/goods'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const nlQuery = ref('')
const brand = ref('')
const priceMin = ref('')
const priceMax = ref('')
const inventoryMin = ref('')
const sort = ref('newest')
const loading = ref(false)
const list = ref([])
const searched = ref(false)
const placeholder = 'https://picsum.photos/seed/search/300/200.jpg'

async function onSearch() {
  loading.value = true
  searched.value = true
  try {
    const res = await searchGoods({
      keyword: keyword.value || undefined,
      nlQuery: nlQuery.value || undefined,
      categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
      brand: brand.value || undefined,
      priceMin: priceMin.value || undefined,
      priceMax: priceMax.value || undefined,
      inventoryMin: inventoryMin.value || undefined,
      sort: sort.value || undefined,
      pageNum: 1,
      pageSize: 20
    })
    const data = res.data
    const rows = data?.list || []
    list.value = rows.map((g) => ({
      id: g.id,
      name: g.name,
      price: g.price != null ? Number(g.price) : 0,
      image: g.image
    }))
  } catch (e) {
    ElMessage.error(e.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

function goDetail(id) {
  router.push(`/goods/${id}`)
}

function formatHighlightedName(name) {
  if (!name) return '未知商品'
  return String(name)
}

onMounted(() => {
  if (route.query.q) {
    keyword.value = String(route.query.q)
  }
  if (route.query.keyword) {
    keyword.value = String(route.query.keyword)
  }
  if (route.query.nlQuery) {
    nlQuery.value = String(route.query.nlQuery)
  }
  if (keyword.value || route.query.categoryId) {
    onSearch()
  }
})
</script>

<style scoped>
.search-container {
  padding: 20px;
}
.meta {
  padding-top: 8px;
}
.name {
  font-weight: 500;
  margin-bottom: 4px;
}

.name :deep(em) {
  color: #f56c6c;
  font-style: normal;
  font-weight: 700;
}
.price {
  color: #f56c6c;
  font-weight: bold;
}
.goods-card {
  cursor: pointer;
}

.sep {
  margin: 0 8px;
}
</style>
