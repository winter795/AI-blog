<template>
  <div class="detail-split">
    <article class="detail-main">
      <div v-if="art" class="art-body">
        <div class="breadcrumb"><router-link to="/visitor">首页</router-link> <span>/</span> <span>{{ art.title }}</span></div>
        <h1>{{ art.title }}</h1>
        <div class="art-info">
          <span>{{ art.createdAt?.slice(0, 10) }}</span>
          <span>{{ art.viewCount }} 阅读</span>
          <router-link v-if="art.categoryId" :to="'/visitor/category/' + art.categoryId" class="cat-link">分类</router-link>
        </div>
        <div v-if="tags.length" class="art-tags">
          <router-link v-for="t in tags" :key="t.id" :to="'/visitor/tag/' + t.id" class="tag-pill">{{ t.name }}</router-link>
        </div>
        <div class="art-content markdown-body" v-html="html"></div>
        <div class="like-bar">
          <button class="like-btn" :class="{ liked }" @click="doLike">
            <span>{{ liked ? 'Liked' : 'Like' }}</span>
            <span class="like-count" v-if="art.likeCount">{{ art.likeCount }}</span>
          </button>
        </div>
        <nav class="pn-nav">
          <div v-if="prev.id">
            <span>Previous</span>
            <router-link :to="'/visitor/article/' + prev.id">{{ prev.title }}</router-link>
          </div>
          <div v-if="next.id" class="pn-next">
            <span>Next</span>
            <router-link :to="'/visitor/article/' + next.id">{{ next.title }}</router-link>
          </div>
        </nav>
      </div>
      <CommentSection :article-id="art?.id" />
    </article>
    <aside class="detail-side">
      <div v-if="toc.length" class="toc">
        <h3>目录</h3>
        <ul>
          <li v-for="h in toc" :key="h.id" :style="{ paddingLeft: (h.level - 1) * 14 + 'px' }">
            <a :href="'#' + h.id" @click.prevent="jump(h.id)">{{ h.text }}</a>
          </li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { getArticleDetail } from '@/api/public'
import { likeArticle } from '@/api/site'
import { useSEO } from '@/stores/seo'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const art = ref(null), tags = ref([]), prev = ref({}), next = ref({}), toc = ref([]), liked = ref(false)

marked.setOptions({
  highlight: (code, lang) => {
    if (lang && hljs.getLanguage(lang)) return hljs.highlight(code, { language: lang }).value
    return hljs.highlightAuto(code).value
  },
})

const html = computed(() => {
  if (!art.value?.content) return ''
  const hs = []
  const tokens = marked.lexer(art.value.content)
  tokens.forEach(t => { if (t.type === 'heading' && t.depth <= 3) { const id = 'h-' + Math.random().toString(36).slice(2,8); hs.push({ id, level: t.depth, text: t.text }) } })
  toc.value = hs
  const renderer = new marked.Renderer()
  let i = 0
  renderer.heading = ({ text, depth }) => { const h = hs[i++]; return `<h${depth} id="${h?.id || ''}">${text}</h${depth}>` }
  return marked.parse(art.value.content, { renderer })
})

function jump(id) { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }) }

async function doLike() {
  if (liked.value) return
  try { await likeArticle(art.value.id); art.value.likeCount = (art.value.likeCount||0)+1; liked.value = true; ElMessage.success('已赞') }
  catch { ElMessage.error('失败') }
}

onMounted(async () => {
  const r = await getArticleDetail(route.params.id)
  art.value = r.data.article; tags.value = r.data.tags||[]; prev.value = r.data.prev||{}; next.value = r.data.next||{}
  if (art.value) useSEO(art.value.title, art.value.summary)
})
</script>

<style scoped>
.detail-split { display: flex; gap: 32px; align-items: flex-start; }
.detail-main { flex: 1; min-width: 0; }
.art-body { margin-bottom: 32px; }
.breadcrumb { font-size: 12px; color: var(--text-muted); margin-bottom: 16px; }
.breadcrumb a { color: var(--accent); transition: color var(--transition); }
.breadcrumb a:hover { color: var(--accent-hover); }
.breadcrumb span { margin: 0 4px; }
.art-body h1 { font-size: 28px; margin-bottom: 12px; color: var(--text); font-weight: 700; }
.art-info { display: flex; gap: 16px; font-size: 12px; color: var(--text-muted); margin-bottom: 12px; font-family: var(--font-mono); }
.cat-link { color: var(--accent); font-weight: 500; font-family: var(--font-body); }
.art-tags { display: flex; gap: 8px; margin-bottom: 28px; flex-wrap: wrap; }
.tag-pill { padding: 2px 12px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 11px; color: var(--text-secondary); transition: all var(--transition); }
.tag-pill:hover { border-color: var(--accent); color: var(--accent); }
.art-content { line-height: 1.9; font-size: 15px; color: var(--text); }
.art-content :deep(h2) { margin: 1.6em 0 0.5em; font-size: 22px; font-weight: 600; }
.art-content :deep(h3) { margin: 1.2em 0 0.4em; font-size: 18px; font-weight: 600; }
.art-content :deep(p) { margin: 0.8em 0; max-width: 100%; }
.art-content :deep(pre) { background: #1e1e2e; padding: 18px 20px; border-radius: var(--radius-md); overflow-x: auto; margin: 16px 0; }
.art-content :deep(code) { font-family: var(--font-mono); font-size: 13px; }
.art-content :deep(:not(pre) > code) { background: var(--bg); padding: 2px 6px; border-radius: 3px; font-size: 13px; color: var(--accent); }
.art-content :deep(blockquote) { border-left: 2px solid var(--accent); padding-left: 16px; color: var(--text-secondary); margin: 14px 0; }
.art-content :deep(img) { max-width: 100%; border-radius: var(--radius-sm); }
.like-bar { text-align: center; margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--border); }
.like-btn {
  display: inline-flex; align-items: center; gap: 6px; padding: 8px 24px;
  border: 1px solid var(--border); border-radius: var(--radius-sm); background: var(--card-bg);
  cursor: pointer; font-size: 13px; color: var(--text-secondary); transition: all var(--transition);
}
.like-btn:hover, .like-btn.liked { border-color: var(--accent); color: var(--accent); }
.like-count { font-family: var(--font-mono); font-size: 12px; }
.pn-nav { display: flex; justify-content: space-between; margin-top: 20px; padding-top: 18px; border-top: 1px solid var(--border); }
.pn-nav span { display: block; font-size: 10px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 4px; }
.pn-nav a { color: var(--accent); font-size: 14px; }
.pn-next { text-align: right; }
.detail-side { width: 220px; flex-shrink: 0; }
.toc { position: sticky; top: 76px; padding-left: 20px; border-left: 1px solid var(--border); }
.toc h3 { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; color: var(--text-muted); margin-bottom: 12px; font-weight: 500; }
.toc ul { list-style: none; }
.toc li { padding: 4px 0; }
.toc a { font-size: 12px; color: var(--text-secondary); transition: color var(--transition); }
.toc a:hover { color: var(--accent); }
@media (max-width: 800px) { .detail-split { flex-direction: column; } .detail-side { width: 100%; } .art-body h1 { font-size: 24px; } }
</style>
