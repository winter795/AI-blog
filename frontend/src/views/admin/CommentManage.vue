<template>
  <div class="page-wrap card">
    <div class="toolbar">
      <el-radio-group v-model="sf" @change="load">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="list" v-loading="ld" stripe>
      <el-table-column prop="id" label="#" width="60" />
      <el-table-column prop="author" label="昵称" width="100" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'danger':'warning'" size="small">{{['待审','通过','拒绝'][row.status]}}</el-tag></template>
      </el-table-column>
      <el-table-column label="时间" width="150"><template #default="{row}">{{ row.createdAt?.slice(0,16) }}</template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{row}">
          <template v-if="row.status===0">
            <el-button size="small" type="success" @click="audit(row.id,1)">通过</el-button>
            <el-button size="small" type="warning" @click="audit(row.id,2)">拒绝</el-button>
          </template>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)"><template #reference><el-button size="small" type="danger">删除</el-button></template></el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager" v-if="total>ps"><el-pagination v-model:current-page="pg" :page-size="ps" :total="total" layout="prev,pager,next" @current-change="load" /></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCommentPage, auditComment, deleteComment } from '@/api/admin'
const list=ref([]),sf=ref(undefined),pg=ref(1),ps=ref(10),total=ref(0),ld=ref(false)
onMounted(load)
async function load(){ld.value=true;try{const r=await getCommentPage({pageNum:pg.value,pageSize:ps.value,status:sf.value});list.value=r.data.records;total.value=r.data.total}finally{ld.value=false}}
async function audit(id,status){try{await auditComment(id,status);ElMessage.success('已审核');load()}catch{ElMessage.error('失败')}}
async function del(id){try{await deleteComment(id);ElMessage.success('已删除');load()}catch{ElMessage.error('失败')}}
</script>

<style scoped>
.page-wrap{padding:20px;border:1px solid var(--border)}
.toolbar{margin-bottom:16px}
.pager{margin-top:16px;display:flex;justify-content:flex-end}
</style>
