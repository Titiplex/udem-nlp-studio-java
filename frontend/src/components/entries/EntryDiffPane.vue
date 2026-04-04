<script setup lang="ts">
const props = defineProps<{
  label: string
  raw: string
  corrected: string
}>()

const changed = computed(() => (props.raw ?? '') !== (props.corrected ?? ''))

import {computed} from 'vue'
</script>

<template>
  <section class="diff-card">
    <div class="diff-header">
      <h4>{{ label }}</h4>
      <span class="diff-badge" :class="{ changed }">
        {{ changed ? 'Changed' : 'Unchanged' }}
      </span>
    </div>

    <div class="diff-grid">
      <div>
        <p class="pane-title">Raw</p>
        <pre>{{ raw || '—' }}</pre>
      </div>

      <div>
        <p class="pane-title">Corrected</p>
        <pre>{{ corrected || '—' }}</pre>
      </div>
    </div>
  </section>
</template>

<style scoped>
.diff-card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.diff-header h4 {
  margin: 0;
}

.diff-badge {
  border-radius: 999px;
  padding: 4px 8px;
  background: #e5e7eb;
  font-size: 12px;
}

.diff-badge.changed {
  background: #fde68a;
}

.diff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.pane-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f9fafb;
  border-radius: 10px;
  padding: 12px;
  min-height: 84px;
}

@media (max-width: 900px) {
  .diff-grid {
    grid-template-columns: 1fr;
  }
}
</style>