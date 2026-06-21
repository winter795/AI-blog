<template>
  <div class="pub-root">
    <header class="pub-hd">
      <div class="pub-hd-inner">
        <router-link to="/visitor" class="logo" title="返回首页">Essays</router-link>
        <nav class="nav">
          <router-link to="/visitor"><el-icon><HomeFilled /></el-icon>首页</router-link>
          <router-link to="/visitor/about">关于</router-link>
          <router-link to="/visitor/links">友链</router-link>
          <router-link to="/visitor/ai">AI 搜索</router-link>
        </nav>
        <div class="hd-actions">
          <el-input v-model="kw" placeholder="搜索" size="small" class="hd-search"
            @keyup.enter="search" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <button class="theme-btn" @click="dark = !dark" :title="dark ? '日间模式' : '夜间模式'">
            {{ dark ? 'Light' : 'Dark' }}
          </button>
          <!-- 用户区 -->
          <template v-if="token">
            <el-dropdown trigger="click">
              <span class="user-trigger">
                <el-icon><UserFilled /></el-icon>
                <span class="user-name">{{ store.userInfo?.nickname || '用户' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/visitor/profile')">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" @click="$router.push('/admin')">
                    <el-icon><Setting /></el-icon>管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <router-link v-else to="/login" class="login-link">登录</router-link>
        </div>
      </div>
    </header>
    <main class="pub-main">
      <router-view />
    </main>
    <footer class="pub-ft">
      <div class="ft-inner">
        <span>Personal AI Blog</span>
        <span class="ft-sep">/</span>
        <span>Powered by Vue & Spring Boot</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useTheme, useUserStore } from '@/stores'
import { recordPV } from '@/api/site'

const router = useRouter()
const kw = ref('')
const { dark } = useTheme()
const store = useUserStore()
const token = computed(() => store.token)
const isAdmin = computed(() => store.userInfo?.role === 'admin')

function search() {
  if (kw.value.trim()) router.push({ path: '/visitor/search', query: { q: kw.value.trim() } })
}

function handleLogout() {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    store.logout()
    router.push('/visitor')
  })
}

onMounted(() => recordPV())
</script>

<style scoped>
.pub-root { min-height: 100vh; display: flex; flex-direction: column; background: var(--bg); }
.pub-hd {
  position: sticky; top: 0; z-index: 100;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border);
}
.pub-hd-inner {
  max-width: 1120px; margin: 0 auto; padding: 0 24px;
  height: 56px; display: flex; align-items: center; gap: 32px;
}
.logo {
  font-family: var(--font-display); font-size: 20px; font-weight: 700;
  color: var(--text); letter-spacing: -0.02em;
}
.nav { display: flex; gap: 24px; }
.nav a { font-size: 14px; color: var(--text-secondary); padding: 2px 0; }
.nav a:hover, .nav a.router-link-exact-active { color: var(--accent); }
.hd-actions { margin-left: auto; display: flex; align-items: center; gap: 10px; }
.hd-search { width: 160px; }
.theme-btn {
  border: 1px solid var(--border); background: var(--card-bg); color: var(--text-secondary);
  padding: 4px 12px; border-radius: var(--radius-sm); cursor: pointer; font-size: 11px;
  font-family: var(--font-mono); text-transform: uppercase;
  transition: all var(--transition);
}
.theme-btn:hover { border-color: var(--accent); color: var(--accent); }
.user-trigger {
  display: flex; align-items: center; gap: 6px; cursor: pointer;
  color: var(--text-secondary); font-size: 13px; padding: 2px 8px;
  border-radius: var(--radius-sm); transition: all var(--transition);
}
.user-trigger:hover { color: var(--accent); background: var(--bg); }
.user-name { max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.login-link {
  font-size: 13px; color: var(--accent); padding: 4px 14px;
  border: 1px solid var(--accent); border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.login-link:hover { background: var(--accent); color: #fff; }
.pub-main { flex: 1; max-width: 1120px; width: 100%; margin: 32px auto 0; padding: 0 24px; }
.pub-ft { border-top: 1px solid var(--border); padding: 24px 0; margin-top: 48px; }
.ft-inner { max-width: 1120px; margin: 0 auto; padding: 0 24px; text-align: center; font-size: 12px; color: var(--text-muted); display: flex; gap: 8px; justify-content: center; }
.ft-sep { color: var(--border); }
</style>
