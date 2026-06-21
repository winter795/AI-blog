<template>
  <div class="about-root">
    <h1>About</h1>
    <div class="about-body markdown-body" v-html="content" v-if="content"></div>
    <div class="about-body" v-else>
      <p>欢迎来到我的个人AI博客。这里记录技术思考、项目实践与生活灵感。</p>
      <p>博客由 Vue 3 + Spring Boot 构建，集成了 AI 辅助写作与 Markdown 编辑能力。</p>
      <h3>联系</h3>
      <p>GitHub <a href="https://github.com" target="_blank">github.com</a></p>
      <p>Email <a href="mailto:admin@example.com">admin@example.com</a></p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { marked } from 'marked'
import request from '@/api/index'
import { useSEO } from '@/stores/seo'

useSEO('关于')
const content = ref('')

onMounted(async () => {
  try {
    const res = await request.get('/public/config')
    if (res.data.about) content.value = marked.parse(res.data.about)
  } catch {}
})
</script>

<style scoped>
.about-root { max-width: 640px; }
.about-root h1 { font-size: 28px; font-weight: 700; margin-bottom: 32px; }
.about-body { line-height: 2; color: var(--text-secondary); font-size: 15px; }
.about-body :deep(h2) { margin: 1.5em 0 0.5em; color: var(--text); }
.about-body :deep(h3) { font-size: 16px; margin: 32px 0 10px; color: var(--text); font-weight: 600; }
.about-body p { margin-bottom: 10px; }
.about-body a { color: var(--accent); border-bottom: 1px solid var(--accent); padding-bottom: 1px; }
.about-body a:hover { border-bottom-width: 2px; }
</style>
