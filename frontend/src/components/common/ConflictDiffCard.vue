<script setup lang="ts">
defineProps<{
  title: string
  localLabel?: string
  remoteLabel?: string
  localText: string
  remoteText: string
}>()
</script>

<template>
  <section class="diff-card">
    <h3>{{ title }}</h3>

    <div class="diff-grid">
      <div class="diff-pane">
        <div class="pane-header">{{ localLabel || 'Local draft' }}</div>
        <pre>{{ localText || '∅' }}</pre>
      </div>

      <div class="diff-pane">
        <div class="pane-header">{{ remoteLabel || 'Remote version' }}</div>
        <pre>{{ remoteText || '∅' }}</pre>
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

.diff-card h3 {
  margin: 0 0 12px;
}

.diff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.diff-pane {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #f9fafb;
}

.pane-header {
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.diff-pane pre {
  margin: 0;
  padding: 12px;
  max-height: 280px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

@media (max-width: 1100px) {
  .diff-grid {
    grid-template-columns: 1fr;
  }
}
</style>