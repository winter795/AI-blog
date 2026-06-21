<template>
  <div class="admin-root">
    <el-container>
      <el-aside width="220px" class="aside">
        <div class="aside-brand">
          <router-link to="/admin" class="brand-link">Blog<span>Admin</span></router-link>
        </div>
        <el-menu :default-active="route.path" router class="menu" background-color="transparent">
          <el-menu-item index="/admin/article"><el-icon><Document /></el-icon>文章管理</el-menu-item>
          <el-menu-item index="/admin/category"><el-icon><FolderOpened /></el-icon>分类管理</el-menu-item>
          <el-menu-item index="/admin/tag"><el-icon><CollectionTag /></el-icon>标签管理</el-menu-item>
          <el-menu-item index="/admin/comment"><el-icon><ChatDotRound /></el-icon>评论审核</el-menu-item>
          <el-menu-item index="/admin/friend-links"><el-icon><Link /></el-icon>友链管理</el-menu-item>
          <el-menu-item index="/admin/settings"><el-icon><Tools /></el-icon>站点设置</el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="topbar">
          <el-button text @click="$router.push('/visitor')" class="visit-btn">← 访问前台</el-button>
          <div class="topbar-right">
            <span class="nick">{{ store.userInfo?.nickname || '管理员' }}</span>
            <el-button text type="danger" @click="logout">退出</el-button>
          </div>
        </el-header>
        <el-main class="admin-main"><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

function logout() {
  ElMessageBox.confirm('确定退出？', '提示', { type: 'warning' }).then(() => {
    store.logout(); router.replace('/login')
  })
}
</script>

<style scoped>
.admin-root { height: 100vh; }
.aside {
  background: var(--card-bg); border-right: 1px solid var(--border);
}
.aside-brand { padding: 20px 24px; }
.brand-link { font-family: var(--font-display); font-size: 16px; color: var(--text); }
.brand-link span { color: var(--accent); font-weight: 700; margin-left: 4px; }
.menu { border-right: none; --el-menu-bg-color: transparent; --el-menu-text-color: var(--text-secondary); --el-menu-active-color: var(--accent); --el-menu-hover-bg-color: var(--bg); }
.el-menu-item { font-size: 13px; height: 44px; }
.topbar {
  background: var(--card-bg); border-bottom: 1px solid var(--border);
  display: flex; align-items: center; justify-content: space-between; padding: 0 24px; height: 56px;
}
.visit-btn { font-size: 13px; color: var(--text-muted); }
.topbar-right { display: flex; align-items: center; gap: 12px; }
.nick { font-size: 13px; color: var(--text-secondary); }
.admin-main { background: var(--bg); padding: 24px; min-height: calc(100vh - 56px); }
</style>
