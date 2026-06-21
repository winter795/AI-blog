<template>
  <div class="login-root">
    <div class="login-card">
      <div class="login-brand">
        <h1>个人AI博客</h1>
        <p>管理后台</p>
      </div>
      <el-form ref="f" :model="m" :rules="r" @submit.prevent="go" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="m.username" placeholder="admin" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="m.password" type="password" show-password placeholder="······" size="large" />
        </el-form-item>
        <el-button native-type="submit" :loading="ld" size="large" class="btn">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import { useUserStore } from '@/stores'

const router = useRouter()
const store = useUserStore()
const ld = ref(false)
const f = ref(null)
const m = reactive({ username: '', password: '' })
const r = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function go() {
  if (!(await f.value.validate().catch(() => false))) return
  ld.value = true
  try {
    const res = await login(m)
    store.setToken(res.data.token)
    store.setUserInfo({ nickname: res.data.nickname, avatar: res.data.avatar, role: res.data.role })
    ElMessage.success('欢迎回来')
    router.replace(res.data.role === 'admin' ? '/admin' : '/visitor')
  } catch { ElMessage.error('用户名或密码错误') }
  finally { ld.value = false }
}
</script>

<style scoped>
.login-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
}
.login-card {
  width: 380px;
  padding: 44px 40px 36px;
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
}
.login-brand { text-align: left; margin-bottom: 32px; }
.login-brand h1 { font-size: 24px; color: var(--text); }
.login-brand p { font-size: 12px; color: var(--text-muted); margin-top: 4px; letter-spacing: 0.08em; text-transform: uppercase; }
.btn {
  width: 100%; margin-top: 8px;
  background: var(--text); border-color: var(--text); color: #fff; font-weight: 500;
  transition: all var(--transition);
}
.btn:hover { background: var(--accent); border-color: var(--accent); }
</style>
