<template>
  <div class="profile-root">
    <div class="profile-card card">
      <div class="profile-header">
        <div class="avatar">
          <el-icon :size="48"><UserFilled /></el-icon>
        </div>
        <div class="profile-info">
          <h1>{{ store.userInfo?.nickname || '用户' }}</h1>
          <p v-if="isAdmin" class="role-badge">管理员</p>
        </div>
      </div>

      <div class="profile-actions">
        <el-button v-if="isAdmin" type="primary" @click="$router.push('/admin')">
          <el-icon><Setting /></el-icon>进入管理后台
        </el-button>
        <el-button @click="$router.push('/visitor')">
          <el-icon><Reading /></el-icon>浏览文章
        </el-button>
        <el-button @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores'

const router = useRouter()
const store = useUserStore()
const isAdmin = computed(() => store.userInfo?.role === 'admin')

function handleLogout() {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    store.logout()
    router.push('/visitor')
  })
}
</script>

<style scoped>
.profile-root { max-width: 640px; margin: 0 auto; }
.profile-card { padding: 40px; border: 1px solid var(--border); }
.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 32px; }
.avatar {
  width: 80px; height: 80px; border-radius: 50%; background: var(--bg);
  display: flex; align-items: center; justify-content: center;
  border: 2px solid var(--border); color: var(--text-muted);
}
.profile-info h1 { font-size: 22px; margin-bottom: 4px; }
.role-badge { font-size: 12px; color: var(--accent); font-weight: 500; }
.profile-actions { display: flex; gap: 10px; flex-wrap: wrap; }
</style>
