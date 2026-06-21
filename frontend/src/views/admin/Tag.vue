<template>
  <div class="page-wrap card">
    <div class="toolbar"><el-button type="primary" @click="open()"><el-icon><Plus /></el-icon>新增</el-button></div>
    <el-table :data="list" v-loading="ld" stripe>
      <el-table-column prop="id" label="#" width="70" />
      <el-table-column prop="name" label="名称" min-width="200" />
      <el-table-column label="时间" width="160"><template #default="{row}">{{ row.createdAt }}</template></el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="open(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="del(row.id)"><template #reference><el-button size="small" type="danger">删除</el-button></template></el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog :title="ed.id?'编辑标签':'新增标签'" v-model="dlg" width="400px">
      <el-form ref="fr" :model="ed" :rules="r" label-width="60px">
        <el-form-item label="名称" prop="name"><el-input v-model="ed.name" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dlg=false">取消</el-button><el-button type="primary" :loading="sv" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTags, saveTag, updateTag, deleteTag } from '@/api/tag'
const list=ref([]),ld=ref(false),sv=ref(false),dlg=ref(false),fr=ref(null)
const ed=reactive({id:null,name:''})
const r={name:[{required:true,message:'请输入名称'}]}
onMounted(load)
async function load(){ld.value=true;try{list.value=(await getTags()).data}finally{ld.value=false}}
function open(row){Object.assign(ed,row?{...row}:{id:null,name:''});dlg.value=true}
async function save(){if(!(await fr.value.validate().catch(()=>false)))return;sv.value=true;try{ed.id?await updateTag(ed):await saveTag(ed);ElMessage.success('已保存');dlg.value=false;load()}finally{sv.value=false}}
async function del(id){try{await deleteTag(id);ElMessage.success('已删除');load()}catch{ElMessage.error('失败')}}
</script>

<style scoped>.page-wrap{padding:20px;border:1px solid var(--border)}.toolbar{margin-bottom:16px}</style>
