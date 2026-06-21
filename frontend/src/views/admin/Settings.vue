<template>
  <div class="page-wrap card">
    <h3>站点设置</h3>
    <el-form :model="f" label-width="100px">
      <el-form-item label="关于页内容">
        <el-input v-model="f.about" type="textarea" :rows="12" placeholder="关于页的 Markdown 内容..." />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="sv" @click="save">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/index'

const sv = ref(false)
const f = reactive({ about: '' })

onMounted(async () => {
  try {
    const res = await request.get('/admin/config')
    f.about = res.data.about || ''
  } catch {}
})

async function save() {
  sv.value = true
  try {
    await request.post('/admin/config', { about: f.about })
    ElMessage.success('已保存')
  } catch { ElMessage.error('保存失败') }
  finally { sv.value = false }
}
</script>

<style scoped>
.page-wrap { padding: 24px; border: 1px solid var(--border); max-width: 800px; }
.page-wrap h3 { margin-bottom: 20px; font-size: 16px; }
</style>
