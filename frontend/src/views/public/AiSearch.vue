<template>
  <div class="ai-root">
    <div class="ai-search-bar">
      <el-input v-model="query" size="large" placeholder="输入任意问题，AI 从博客文章中寻找答案..."
        @keyup.enter="ask" @input="onInput" clearable class="ai-input">
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #append>
          <el-button :loading="loading" @click="ask" type="primary">提问</el-button>
        </template>
      </el-input>
      <div v-if="suggestions.length && !loading && !answer" class="suggestions">
        <span v-for="s in suggestions" :key="s" @click="query=s;ask()">{{ s }}</span>
      </div>
    </div>

    <div v-if="loading || answer || sources.length" class="ai-result">
      <div v-if="sources.length" class="sources-card">
        <h3>参考文章</h3>
        <div v-for="(s, i) in sources" :key="s.id" class="source-item">
          <span class="src-num">[{{ i + 1 }}]</span>
          <router-link :to="'/visitor/article/' + s.id">{{ s.title }}</router-link>
          <span class="src-score">{{ (s.score * 100).toFixed(0) }}%</span>
        </div>
      </div>
      <div class="answer-card" v-if="answer || loading">
        <div class="answer-content">{{ answer }}<span v-if="loading" class="cursor">|</span></div>
      </div>
    </div>

    <div v-if="!loading && !answer && !hasAsked" class="ai-intro">
      <h2>AI 知识检索</h2>
      <p>基于全站文章内容，用自然语言提问，AI 为你智能检索并生成答案。</p>
      <div class="example-qs">
        <span v-for="eq in examples" :key="eq" @click="query=eq;ask()">{{ eq }}</span>
      </div>
    </div>

    <!-- 相关文章 -->
    <div v-if="related.length && !loading" class="related-section">
      <h3>相关文章</h3>
      <div class="related-grid">
        <router-link v-for="r in related" :key="r.id" :to="'/visitor/article/' + r.id" class="related-card">
          <h4>{{ r.title }}</h4>
          <span>{{ (r.score * 100).toFixed(0) }}% 相似</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { aiSearch, getSuggestions } from '@/api/ai'

const query = ref('')
const answer = ref('')
const sources = ref([])
const loading = ref(false)
const hasAsked = ref(false)
const suggestions = ref([])
const related = ref([])
const examples = ['什么是临期食品？', '如何优化数据库性能？', 'Spring Boot 如何配置 JWT？']

let suggestTimer = null
function onInput() {
  clearTimeout(suggestTimer)
  if (!query.value.trim()) { suggestions.value = []; return }
  suggestTimer = setTimeout(async () => {
    try { const r = await getSuggestions(query.value); suggestions.value = r.data || [] }
    catch { suggestions.value = [] }
  }, 400)
}

function ask() {
  if (!query.value.trim() || loading.value) return
  answer.value = ''
  sources.value = []
  loading.value = true
  hasAsked.value = true
  suggestions.value = []

  aiSearch(
    query.value,
    (token) => { answer.value += token },
    (srcs) => { sources.value = srcs },
    () => { loading.value = false },
    (err) => { answer.value = '抱歉，AI 服务暂时不可用: ' + err; loading.value = false }
  )
}
</script>

<style scoped>
.ai-root { max-width: 800px; margin: 0 auto; }
.ai-search-bar { margin-bottom: 24px; position: relative; }
.ai-input :deep(.el-input__inner) { font-size: 15px; }
.suggestions { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 6px; }
.suggestions span {
  font-size: 12px; color: var(--text-secondary); background: var(--bg);
  border: 1px solid var(--border); padding: 3px 12px; border-radius: 14px;
  cursor: pointer; transition: all var(--transition);
}
.suggestions span:hover { border-color: var(--accent); color: var(--accent); }

.ai-result { margin-top: 20px; }
.sources-card {
  background: var(--card-bg); border: 1px solid var(--border);
  border-radius: var(--radius-md); padding: 16px 20px; margin-bottom: 16px;
}
.sources-card h3 { font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em; color: var(--text-muted); margin-bottom: 10px; font-weight: 500; }
.source-item { display: flex; align-items: center; gap: 10px; padding: 4px 0; font-size: 13px; }
.src-num { color: var(--accent); font-family: var(--font-mono); font-size: 12px; min-width: 24px; }
.source-item a { color: var(--text-secondary); flex: 1; transition: color var(--transition); }
.source-item a:hover { color: var(--accent); }
.src-score { color: var(--text-muted); font-family: var(--font-mono); font-size: 11px; }

.answer-card {
  background: var(--card-bg); border: 1px solid var(--border);
  border-radius: var(--radius-md); padding: 24px;
}
.answer-content { font-size: 15px; line-height: 1.9; color: var(--text); white-space: pre-wrap; }
.cursor { animation: blink 0.7s infinite; color: var(--accent); font-weight: 700; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.ai-intro { text-align: center; padding: 60px 20px; }
.ai-intro h2 { font-size: 24px; margin-bottom: 10px; }
.ai-intro p { color: var(--text-secondary); margin: 0 auto 24px; }
.example-qs { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.example-qs span {
  padding: 6px 16px; border: 1px solid var(--border); border-radius: var(--radius-sm);
  font-size: 13px; color: var(--text-secondary); cursor: pointer; transition: all var(--transition);
}
.example-qs span:hover { border-color: var(--accent); color: var(--accent); }

.related-section { margin-top: 40px; }
.related-section h3 { font-size: 14px; color: var(--text-muted); margin-bottom: 14px; }
.related-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.related-card {
  padding: 14px 16px; border: 1px solid var(--border); border-radius: var(--radius-sm);
  display: flex; justify-content: space-between; align-items: center;
  transition: all var(--transition);
}
.related-card:hover { border-color: var(--accent); }
.related-card h4 { font-size: 14px; color: var(--text); font-weight: 500; }
.related-card span { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }
@media (max-width: 600px) { .related-grid { grid-template-columns: 1fr; } }
</style>
