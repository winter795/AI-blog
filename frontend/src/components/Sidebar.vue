<template>
  <div class="sb">
    <div class="sb-card" v-if="latest.length">
      <h3>最新文章</h3>
      <ul>
        <li v-for="a in latest" :key="a.id">
          <router-link :to="'/visitor/article/' + a.id">{{ a.title }}</router-link>
        </li>
      </ul>
    </div>
    <div class="sb-card" v-if="hot.length">
      <h3>热门文章</h3>
      <ul>
        <li v-for="a in hot" :key="a.id">
          <router-link :to="'/visitor/article/' + a.id">{{ a.title }}</router-link>
          <span class="sb-count">{{ a.viewCount }}</span>
        </li>
      </ul>
    </div>
    <div class="sb-card" v-if="categories.length">
      <h3>分类</h3>
      <div class="sb-cloud">
        <router-link v-for="c in categories" :key="c.id" :to="'/visitor/category/' + c.id">{{ c.name }}</router-link>
      </div>
    </div>
    <div class="sb-card" v-if="tags.length">
      <h3>标签</h3>
      <div class="sb-cloud">
        <router-link v-for="t in tags" :key="t.id" :to="'/visitor/tag/' + t.id">{{ t.name }}</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLatestArticles, getHotArticles, getCategories, getTags } from '@/api/public'
const latest = ref([]), hot = ref([]), categories = ref([]), tags = ref([])
onMounted(async () => {
  const [l,h,c,t] = await Promise.all([getLatestArticles(), getHotArticles(), getCategories(), getTags()])
  latest.value = l.data; hot.value = h.data; categories.value = c.data; tags.value = t.data
})
</script>

<style scoped>
.sb-card { margin-bottom: 28px; }
.sb-card h3 { font-size: 11px; text-transform: uppercase; letter-spacing: 0.08em; color: var(--text-muted); margin-bottom: 12px; font-weight: 500; }
.sb-card ul { list-style: none; }
.sb-card li { display: flex; justify-content: space-between; align-items: center; padding: 5px 0; font-size: 13px; }
.sb-card li a { color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; margin-right: 8px; transition: color var(--transition); }
.sb-card li a:hover { color: var(--accent); }
.sb-count { font-size: 10px; color: var(--text-muted); font-family: var(--font-mono); }
.sb-cloud { display: flex; flex-wrap: wrap; gap: 6px; }
.sb-cloud a { padding: 2px 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: 11px; color: var(--text-secondary); transition: all var(--transition); }
.sb-cloud a:hover { border-color: var(--accent); color: var(--accent); }
</style>
