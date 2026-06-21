<template>
  <div class="cs">
    <h3>评论 ({{ list.length }})</h3>
    <div v-if="list.length">
      <div v-for="c in list" :key="c.id" class="cs-item">
        <div class="cs-head">
          <strong>{{ c.author }}</strong>
          <span class="cs-time">{{ c.createdAt?.slice(0, 16) }}</span>
        </div>
        <p>{{ c.content }}</p>
      </div>
    </div>
    <p v-else class="cs-empty">暂无评论，写下第一条吧</p>
    <div class="cs-form">
      <h4>发表评论</h4>
      <el-input v-model="f.author" placeholder="昵称" size="default" />
      <el-input v-model="f.email" placeholder="邮箱 (选填)" size="default" />
      <el-input v-model="f.content" type="textarea" :rows="3" placeholder="写下你的想法" />
      <div class="cs-captcha">
        <span class="captcha-question">{{ captcha.expression }}</span>
        <el-input v-model="f.answer" placeholder="?" style="width:80px" size="default" />
        <button class="captcha-refresh" type="button" @click="refresh">换一题</button>
      </div>
      <el-button type="primary" :loading="submitting" @click="submit">提交评论</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getCaptcha, submitComment, getComments } from '@/api/site'

const props = defineProps({ articleId: Number })
const list = ref([]), submitting = ref(false), captcha = ref({ expression: '', token: '' })
const f = reactive({ author: '', email: '', content: '', answer: '' })

onMounted(async () => { await load(); refresh() })

async function load() { const r = await getComments(props.articleId); list.value = r.data }
async function refresh() { const r = await getCaptcha(); captcha.value = r.data; f.answer = '' }
async function submit() {
  if (!f.author || !f.content) return ElMessage.warning('请填写昵称和内容')
  submitting.value = true
  try {
    await submitComment({ articleId: props.articleId, author: f.author, email: f.email, content: f.content, token: captcha.value.token, answer: f.answer })
    ElMessage.success('提交成功，等待审核'); f.author = ''; f.email = ''; f.content = ''; refresh()
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.cs { margin-top: 32px; padding-top: 28px; border-top: 1px solid var(--border); }
.cs h3 { font-size: 15px; font-weight: 600; color: var(--text); margin-bottom: 20px; }
.cs-item { padding: 14px 0; border-bottom: 1px solid var(--border); }
.cs-item:last-child { border-bottom: none; }
.cs-head { display: flex; justify-content: space-between; margin-bottom: 4px; }
.cs-head strong { font-size: 13px; color: var(--text); font-weight: 600; }
.cs-time { font-size: 10px; color: var(--text-muted); font-family: var(--font-mono); }
.cs-item p { font-size: 14px; color: var(--text-secondary); line-height: 1.7; max-width: 100%; }
.cs-empty { text-align: center; color: var(--text-muted); padding: 32px; font-size: 13px; }
.cs-form { margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border); }
.cs-form h4 { font-size: 13px; font-weight: 600; margin-bottom: 12px; color: var(--text); }
.cs-form .el-input { margin-bottom: 10px; }
.cs-captcha { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.captcha-question {
  font-family: var(--font-mono); font-size: 16px; font-weight: 600; color: var(--text);
  background: var(--bg); padding: 6px 16px; border-radius: var(--radius-sm);
}
.captcha-refresh {
  border: none; background: none; color: var(--accent); cursor: pointer; font-size: 11px;
  transition: opacity var(--transition);
}
.captcha-refresh:hover { opacity: 0.7; }
</style>
