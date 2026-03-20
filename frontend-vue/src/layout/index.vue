<template>
  <div class="layout-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-image src="https://picsum.photos/seed/logo/120/40.jpg" fit="contain" />
            <span class="logo-text">智能电商平台</span>
          </div>
          <div class="header-nav">
            <el-menu mode="horizontal" :default-active="activeMenu" class="nav-menu">
              <el-menu-item index="home">首页</el-menu-item>
              <el-menu-item index="search">搜索</el-menu-item>
              <el-menu-item index="cart">购物车</el-menu-item>
              <el-menu-item index="order">我的订单</el-menu-item>
              <el-menu-item index="user">个人中心</el-menu-item>
            </el-menu>
          </div>
          <div class="header-actions">
            <el-button type="text" @click="handleLogin" v-if="!isLoggedIn">登录</el-button>
            <el-dropdown v-else>
              <el-button type="text">
                {{ userInfo.username }} <el-icon><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu :default-active="activeMenu" class="aside-menu">
            <el-menu-item index="home">首页</el-menu-item>
            <el-menu-item index="search">搜索</el-menu-item>
            <el-menu-item index="cart">购物车</el-menu-item>
            <el-menu-item index="order">我的订单</el-menu-item>
            <el-menu-item index="user">个人中心</el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.name?.toString() || 'home')
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

const handleLogin = () => {
  router.push('/login')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
}

.header-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.nav-menu {
  background-color: transparent;
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  color: white;
  font-weight: 500;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2);
}

.aside {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.aside-menu {
  border-right: none;
}

.aside-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>