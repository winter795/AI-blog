<template>
  <div class="edit-wrap card">
    <div class="edit-hd">
      <el-button text @click="$router.back()">← 返回</el-button>
      <h2>{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
    </div>
    <el-form ref="fr" :model="f" :rules="r" label-width="60px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="f.title" placeholder="文章标题" size="large" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="f.summary" type="textarea" :rows="2" placeholder="摘要（选填）" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="分类">
            <el-select v-model="f.categoryId" placeholder="选择分类" clearable>
              <el-option v-for="c in cats" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="标签">
            <el-select v-model="f.tagIds" multiple placeholder="选择标签" clearable>
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="内容" prop="content">
        <!-- 工具栏 -->
        <div class="md-toolbar">
          <button type="button" title="粗体" @click="wrapMd('**', '**')"><strong>B</strong></button>
          <button type="button" title="标题" @click="insertMd('## ')">H</button>
          <button type="button" title="代码块" @click="wrapMd('```\n', '\n```')">&lt;/&gt;</button>
          <button type="button" title="引用" @click="insertMd('> ')">&#10077;</button>
          <span class="tb-sep"></span>
          <button type="button" title="无序列表" @click="insertMd('- ')">≡</button>
          <button type="button" title="链接" @click="wrapMd('[', '](url)')">🔗</button>
          <button type="button" title="插入图片" @click="triggerUpload">
            🖼
            <input ref="fileInput" type="file" accept="image/*" hidden @change="handleUpload" />
          </button>
          <span class="tb-sep"></span>
          <button type="button" title="分隔线" @click="insertMd('\n---\n')">—</button>
          <span v-if="uploading" class="tb-msg">上传中...</span>
          <span v-if="uploadUrl" class="tb-msg">图片已插入</span>
        </div>
        <div class="editor-split">
          <textarea ref="textareaRef" class="md-input" v-model="f.content" placeholder="在此输入 Markdown 内容..."></textarea>
          <div class="md-preview markdown-body" v-html="preview"></div>
        </div>
        <div class="ai-tools">
          <el-button size="small" :loading="aiLoading" @click="aiSummary">AI 生成摘要</el-button>
          <el-button size="small" :loading="aiLoading" @click="aiTags">AI 推荐标签</el-button>
          <span v-if="aiMsg" class="ai-msg">{{ aiMsg }}</span>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="sv" @click="save(1)">发布</el-button>
        <el-button :loading="sv" @click="save(0)">保存草稿</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { getArticle, saveArticle, updateArticle } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { generateSummary, generateTags } from '@/api/ai'
import request from '@/api/index'

const route=useRoute(),router=useRouter()
const isEdit=computed(()=>!!route.params.id)
const sv=ref(false),fr=ref(null),cats=ref([]),tags=ref([]),textareaRef=ref(null),fileInput=ref(null)
const uploading=ref(false),uploadUrl=ref(''), aiLoading=ref(false), aiMsg=ref('')
const f=reactive({title:'',summary:'',content:'',categoryId:null,tagIds:[],status:0})
const r={title:[{required:true,message:'请输入标题'}],content:[{required:true,message:'请输入内容'}]}
const preview=computed(()=>{try{return marked.parse(f.content)}catch{return''}})

onMounted(async()=>{
  const[cr,tr]=await Promise.all([getCategories(),getTags()]);cats.value=cr.data;tags.value=tr.data
  if(isEdit.value){const res=await getArticle(route.params.id);const{article,tagIds}=res.data;Object.assign(f,{title:article.title,summary:article.summary||'',content:article.content||'',categoryId:article.categoryId,tagIds:tagIds||[],status:article.status})}
})

// ── 工具栏辅助方法 ──
function insertText(textarea, before, after, placeholder) {
  const el = textarea
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = f.content.substring(start, end)
  const replacement = (typeof before === 'function' ? before(selected) : before) + (selected || placeholder || '') + after
  f.content = f.content.substring(0, start) + replacement + f.content.substring(end)
  setTimeout(() => { el.focus(); el.setSelectionRange(start + before.length, start + before.length + (selected || placeholder || '').length) })
}
function insertMd(text) { insertText(textareaRef.value, text, '') }
function wrapMd(before, after, placeholder) { insertText(textareaRef.value, before, after, placeholder) }
function triggerUpload() { fileInput.value.click() }

async function handleUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  uploading.value = true
  try {
    const res = await request.post('/admin/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    const url = res.data.url
    insertText(textareaRef.value, '![', '](' + url + ')', file.name)
    uploadUrl.value = url
    setTimeout(() => uploadUrl.value = '', 3000)
  } catch { ElMessage.error('上传失败') }
  finally { uploading.value = false; fileInput.value.value = '' }
}

// ── 保存 ──
async function save(status){if(!(await fr.value.validate().catch(()=>false)))return;sv.value=true;try{const d={...f,status};isEdit.value?(d.id=Number(route.params.id),await updateArticle(d)):await saveArticle(d);ElMessage.success(status?'已发布':'已保存');router.back()}catch{ElMessage.error('保存失败')}finally{sv.value=false}}

// ── AI ──
async function aiSummary(){if(!f.content)return ElMessage.warning('请先输入内容');aiLoading.value=true;try{const r=await generateSummary(f.content);f.summary=r.data;aiMsg.value='摘要已生成'}catch{ElMessage.error('AI服务不可用')}finally{aiLoading.value=false}}
async function aiTags(){if(!f.content)return ElMessage.warning('请先输入内容');aiLoading.value=true;try{const r=await generateTags(f.content);aiMsg.value='标签: '+r.data.join(', ')}catch{ElMessage.error('AI服务不可用')}finally{aiLoading.value=false}}
</script>

<style scoped>
.edit-wrap { padding: 24px 30px; border: 1px solid var(--border); max-width: 1100px; }
.edit-hd { display: flex; align-items: center; gap: 16px; margin-bottom: 28px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.edit-hd h2 { font-size: 18px; }
.md-toolbar {
  display: flex; align-items: center; gap: 4px; padding: 6px 10px;
  border: 1px solid var(--border); border-bottom: none; border-radius: 6px 6px 0 0;
  background: var(--bg);
}
.md-toolbar button {
  width: 30px; height: 28px; border: none; border-radius: 4px;
  background: transparent; color: var(--text-secondary); cursor: pointer;
  font-size: 13px; display: flex; align-items: center; justify-content: center;
  transition: all var(--transition); position: relative;
}
.md-toolbar button:hover { background: var(--border); color: var(--text); }
.tb-sep { width: 1px; height: 18px; background: var(--border); margin: 0 4px; }
.tb-msg { font-size: 11px; color: var(--accent); margin-left: 8px; }
.editor-split { display: flex; gap: 0; width: 100%; }
.md-input {
  flex:1; min-height: 420px; padding: 14px; border: 1px solid var(--border); border-radius: 0 0 0 6px;
  font-family: var(--font-mono); font-size: 13px; line-height: 1.7; resize: vertical;
  background: var(--bg); color: var(--text); outline: none; border-right: none;
}
.md-input:focus { border-color: var(--accent); }
.md-input:focus + .md-preview { border-left-color: var(--accent); }
.md-preview {
  flex:1; min-height: 420px; padding: 14px; border: 1px solid var(--border); border-radius: 0 0 6px 0;
  background: var(--card-bg); overflow-y: auto; font-size: 14px; line-height: 1.8;
}
.ai-tools { margin-top: 8px; display: flex; align-items: center; gap: 8px; }
.ai-msg { font-size: 12px; color: var(--text-muted); }
@media (max-width: 800px) { .editor-split { flex-direction: column; } .md-input { border-right: 1px solid var(--border); border-radius: 0; } .md-preview { border-radius: 0 0 6px 6px; } }
</style>
