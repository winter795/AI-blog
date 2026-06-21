<template>
  <div class="page-wrap card">
    <div class="toolbar"><el-button type="primary" @click="open()"><el-icon><Plus /></el-icon>新增友链</el-button></div>
    <el-table :data="list" v-loading="ld" stripe>
      <el-table-column prop="id" label="#" width="60" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="url" label="链接" min-width="200" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="open(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)"><template #reference><el-button size="small" type="danger">删除</el-button></template></el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="ed.id?'编辑友链':'新增友链'" v-model="dlg" width="460px">
      <el-form ref="fr" :model="ed" label-width="60px">
        <el-form-item label="名称"><el-input v-model="ed.name" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="ed.url" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="ed.description" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="ed.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" :loading="sv" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/index'

const list = ref([]), ld = ref(false), sv = ref(false), dlg = ref(false), fr = ref(null)
const ed = reactive({ id: null, name: '', url: '', description: '', sortOrder: 0 })

onMounted(load)
async function load() { ld.value = true; try { list.value = (await request.get('/admin/friend-links')).data } finally { ld.value = false } }
function open(row) { Object.assign(ed, row ? { ...row } : { id: null, name: '', url: '', description: '', sortOrder: 0 }); dlg.value = true }
async function save() {
  sv.value = true
  try {
    ed.id ? await request.put('/admin/friend-links', ed) : await request.post('/admin/friend-links', ed)
    ElMessage.success('已保存'); dlg.value = false; load()
  } catch { ElMessage.error('失败') }
  finally { sv.value = false }
}
async function del(id) { try { await request.delete(`/admin/friend-links/${id}`); ElMessage.success('已删除'); load() } catch { ElMessage.error('失败') } }
</script>

<style scoped>
.page-wrap { padding: 20px; border: 1px solid var(--border); }
.toolbar { margin-bottom: 16px; }
</style>
