<template>
  <div class="home-split">
    <section class="articles">
      <div v-if="loading" class="sk-wrap">
        <div v-for="i in 5" :key="i" class="sk-card">
          <span class="sk-line sk-s"></span>
          <span class="sk-line sk-l"></span>
          <span class="sk-line sk-m"></span>
        </div>
      </div>
      <EmptyState v-else-if="!list.length" text="暂无文章" />
      <article v-for="(a, i) in list" :key="a.id" class="art-card" :style="{ animationDelay: i * 60 + 'ms' }">
        <div class="art-top">
          <span class="art-date">{{ a.createdAt?.slice(0, 10) }}</span>
          <span v-if="a.isTop" class="top-badge">置顶</span>
        </div>
        <h2 class="art-title">
          <router-link :to="'/visitor/article/' + a.id">{{ a.title }}</router-link>
        </h2>
        <p class="art-desc">{{ a.summary || a.content?.replace(/[#*`>\-\[\]()!]/g,'').slice(0, 200) }}</p>
        <div class="art-meta">
          <span>{{ a.viewCount || 0 }} 阅读</span>
          <span v-if="a.likeCount">{{ a.likeCount }} 赞</span>
          <router-link v-if="a.categoryId" :to="'/visitor/category/' + a.categoryId" class="art-cat">分类</router-link>
          <router-link :to="'/visitor/article/' + a.id" class="art-read">Read</router-link>
        </div>
      </article>
      <div class="pager" v-if="total > pageSize">
        <el-pagination v-model:current-page="pg" :page-size="ps" :total="total"
          layout="prev, pager, next" @current-change="load" />
      </div>
    </section>
    <aside class="sidebar">
      <Sidebar />
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticlePage } from '@/api/public'
import Sidebar from '@/components/Sidebar.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const list = ref([]), pg = ref(1), ps = ref(10), total = ref(0), loading = ref(false)

onMounted(load)
watch(() => [route.path, route.query], () => { pg.value = 1; load() })

async function load() {
  loading.value = true
  try {
    const p = { pageNum: pg.value, pageSize: ps.value }
    if (route.name === 'PublicCategory') p.categoryId = route.params.id
    if (route.name === 'PublicTag') p.tagId = route.params.id
    if (route.name === 'Search') p.keyword = route.query.q
    const r = await getArticlePage(p)
    list.value = r.data.records; total.value = r.data.total
  } finally { loading.value = false }
}
</script>

<style scoped>
.home-split { display: flex; gap: 32px; align-items: flex-start; }
.articles { flex: 1; min-width: 0; }
.art-card {
  padding: 28px 0; border-bottom: 1px solid var(--border);
  animation: fadeIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}
.art-card:first-child { padding-top: 0; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
.art-top { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.art-date { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }
.top-badge { font-size: 10px; color: var(--accent); border: 1px solid var(--accent); padding: 1px 8px; border-radius: 10px; text-transform: uppercase; letter-spacing: 0.05em; }
.art-title { font-size: 20px; margin-bottom: 8px; }
.art-title a { color: var(--text); transition: color var(--transition); }
.art-title a:hover { color: var(--accent); }
.art-desc {
  font-size: 14px; color: var(--text-secondary); line-height: 1.7;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
  margin-bottom: 12px;
}
.art-meta { display: flex; align-items: center; gap: 16px; font-size: 12px; color: var(--text-muted); }
.art-cat { color: var(--accent); font-weight: 500; }
.art-read { margin-left: auto; color: var(--text-secondary); font-weight: 500; font-size: 12px; transition: color var(--transition); }
.art-read:hover { color: var(--accent); }
.pager { display: flex; justify-content: center; margin-top: 28px; }
.sk-wrap { margin-top: 0; }
.sk-card { padding: 28px 0; border-bottom: 1px solid var(--border); }
.sk-line { display: block; height: 14px; border-radius: 4px; background: var(--border); animation: skPulse 1.4s infinite; margin-bottom: 10px; }
.sk-s { width: 80px; }
.sk-m { width: 60%; }
.sk-l { width: 45%; }
@keyframes skPulse { 0%, 100% { opacity: 0.4; } 50% { opacity: 0.8; } }
.sidebar { width: 280px; flex-shrink: 0; }
@media (max-width: 800px) { .home-split { flex-direction: column; } .sidebar { width: 100%; } }
</style>
