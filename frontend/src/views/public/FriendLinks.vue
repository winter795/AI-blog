<template>
  <div class="links-root">
    <h1>友链</h1>
    <div v-if="list.length" class="links-grid">
      <a v-for="l in list" :key="l.id" :href="l.url" target="_blank" rel="noopener" class="link-item">
        <h3>{{ l.name }}</h3>
        <p v-if="l.description">{{ l.description }}</p>
      </a>
    </div>
    <EmptyState v-else text="暂无友链，欢迎交换" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFriendLinks } from '@/api/public'
import EmptyState from '@/components/EmptyState.vue'
const list = ref([])
onMounted(async () => { const r = await getFriendLinks(); list.value = r.data })
</script>

<style scoped>
.links-root { max-width: 640px; }
.links-root h1 { font-size: 28px; font-weight: 700; margin-bottom: 32px; }
.links-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.link-item {
  display: block; padding: 16px 20px; border: 1px solid var(--border); border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.link-item:hover { border-color: var(--accent); transform: translateY(-1px); }
.link-item h3 { font-size: 15px; color: var(--text); margin-bottom: 4px; font-weight: 600; }
.link-item p { font-size: 12px; color: var(--text-muted); }
@media (max-width: 500px) { .links-grid { grid-template-columns: 1fr; } }
</style>
