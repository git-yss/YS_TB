<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>

      <el-descriptions v-if="displayUser" :column="1" border>
        <el-descriptions-item label="用户 ID">{{ displayUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ displayUser.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ displayUser.email || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ displayUser.tel || '—' }}</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ displayUser.balance != null ? displayUser.balance : '0' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="未登录或暂无用户信息" />

      <div v-if="displayUser" class="actions">
        <el-button type="primary" :loading="refreshing" @click="refresh">刷新资料</el-button>
      </div>

      <el-divider v-if="displayUser" />

      <el-form v-if="displayUser" :model="profileForm" label-width="90px" class="form" @submit.prevent="submitProfile">
        <el-form-item label="年龄">
          <el-input v-model="profileForm.age" placeholder="例如 25" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="profileForm.sex" placeholder="请选择" style="width: 160px">
            <el-option label="未知" value="" />
            <el-option label="男" value="1" />
            <el-option label="女" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="updatingProfile" @click="submitProfile">保存资料</el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="displayUser" />

      <el-form v-if="displayUser" :model="pwdForm" label-width="90px" class="form" @submit.prevent="submitPassword">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（>=6）" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :loading="changingPwd" @click="submitPassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'

const userStore = useUserStore()
const refreshing = ref(false)
const updatingProfile = ref(false)
const changingPwd = ref(false)

const displayUser = computed(() => userStore.userInfo)

const profileForm = ref({
  age: '',
  sex: ''
})

const pwdForm = ref({
  oldPassword: '',
  newPassword: ''
})

watch(
  displayUser,
  (u) => {
    if (!u) return
    profileForm.value.age = u.age != null ? String(u.age) : ''
    // 后端 sex 可能是 '0'/'1'
    profileForm.value.sex = u.sex != null ? String(u.sex) : ''
  },
  { immediate: true }
)

async function refresh() {
  const id = userStore.userInfo?.id
  if (!id) return
  refreshing.value = true
  try {
    const res = await getUserInfo(id)
    userStore.setUserInfo(res.data)
    ElMessage.success('已更新')
  } catch (e) {
    ElMessage.error(e.message || '刷新失败')
  } finally {
    refreshing.value = false
  }
}

async function submitProfile() {
  const uid = userStore.userInfo?.id
  if (!uid) return
  updatingProfile.value = true
  try {
    await updateUserInfo({
      userId: uid,
      age: profileForm.value.age || undefined,
      sex: profileForm.value.sex || undefined
    })
    ElMessage.success('资料已保存')
    await refresh()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    updatingProfile.value = false
  }
}

async function submitPassword() {
  const uid = userStore.userInfo?.id
  if (!uid) return
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    ElMessage.warning('请填写旧密码和新密码')
    return
  }
  changingPwd.value = true
  try {
    await changePassword({
      userId: uid,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.value.oldPassword = ''
    pwdForm.value.newPassword = ''
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo?.id) {
    refresh()
  }
})
</script>

<style scoped>
.user-container {
  padding: 20px;
  max-width: 640px;
}

.actions {
  margin-top: 12px;
}

.form {
  margin-top: 12px;
}
</style>
