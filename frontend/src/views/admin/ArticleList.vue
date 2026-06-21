<template>
  <div class="page-wrap card">
    <div class="toolbar">
      <el-input v-model="kw" placeholder="搜索标题/正文..." style="width:220px" clearable @keyup.enter="load" @clear="load">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="sf" placeholder="状态" style="width:110px" clearable @change="load">
        <el-option label="已发布" :value="1" /><el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" @click="$router.push('/admin/article/edit')"><el-icon><Plus /></el-icon>新建</el-button>
    </div>

    <!-- Skeleton -->
    <template v-if="ld && !list.length">
      <div v-for="i in 5" :key="i" class="sk-row">
        <span class="sk-cell sk-cell-s"></span>
        <span class="sk-cell sk-cell-l"></span>
        <span class="sk-cell sk-cell-m"></span>
        <span class="sk-cell sk-cell-s"></span>
        <span class="sk-cell sk-cell-m"></span>
      </div>
    </template>

    <el-table v-else :data="list" v-loading="ld" stripe>
      <el-table-column prop="id" label="#" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" width="100">
        <template #default="{row}">{{ catMap[row.categoryId] || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{row}">
          <el-switch :model-value="row.status===1" @change="toggle(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读" width="70" />
      <el-table-column label="时间" width="160"><template #default="{row}">{{ row.createdAt }}</template></el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="$router.push('/admin/article/edit/'+row.id)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)"><template #reference><el-button size="small" type="danger">删除</el-button></template></el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager" v-if="total > ps"><el-pagination v-model:current-page="pg" :page-size="ps" :total="total" layout="total,prev,pager,next" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getArticlePage, deleteArticle, toggleArticleStatus } from '@/api/article'
import { getCategories } from '@/api/category'

const list=ref([]),kw=ref(''),sf=ref(null),pg=ref(1),ps=ref(10),total=ref(0),ld=ref(true)
const catList = ref([])
const catMap = computed(() => {
  const m = {}
  catList.value.forEach(c => m[c.id] = c.name)
  return m
})

onMounted(async () => {
  try { catList.value = (await getCategories()).data } catch {}
  load()
})

async function load(){ld.value=true;try{const r=await getArticlePage({pageNum:pg.value,pageSize:ps.value,keyword:kw.value||undefined,status:sf.value});list.value=r.data.records;total.value=r.data.total}finally{ld.value=false}}
async function toggle(r){try{await toggleArticleStatus(r.id);ElMessage.success('已切换');load()}catch{ElMessage.error('失败')}}
async function del(id){try{await deleteArticle(id);ElMessage.success('已删除');load()}catch{ElMessage.error('失败')}}
</script>

<style scoped>
.page-wrap { padding: 20px; border: 1px solid var(--border); }
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
/* Skeleton */
.sk-row { display: flex; gap: 12px; padding: 14px 16px; border-bottom: 1px solid var(--border); }
.sk-cell { height: 14px; border-radius: 4px; background: var(--border); animation: sk-pulse 1.4s infinite; }
.sk-cell-s { width: 40px; }
.sk-cell-m { width: 100px; }
.sk-cell-l { flex: 1; }
@keyframes sk-pulse { 0%, 100% { opacity: 0.4; } 50% { opacity: 0.8; } }
</style>
