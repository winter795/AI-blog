<template>
  <div class="empty">
    <p>{{ text }}</p>
    <p v-if="sub" class="empty-sub">{{ sub }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' },
  sub: { type: String, default: '' },
})
</script>

<style scoped>
.empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
  font-size: 14px;
}
.empty-sub {
  margin-top: 6px;
  font-size: 12px;
}
</style>
