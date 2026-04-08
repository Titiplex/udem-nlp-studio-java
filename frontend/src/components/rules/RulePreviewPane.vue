<script setup lang="ts">
import type {RuleDetail} from '../../bridge/desktopBridge'

defineProps<{
  rule: RuleDetail
}>()
</script>

<template>
  <div class="preview-pane">
    <section class="preview-card">
      <h3>Rule summary</h3>
      <dl class="summary-grid">
        <dt>Name</dt>
        <dd>{{ rule.name || '(untitled)' }}</dd>
        <dt>Kind</dt>
        <dd>{{ rule.kind }}</dd>
        <dt>Subtype</dt>
        <dd>{{ rule.subtype }}</dd>
        <dt>Scope</dt>
        <dd>{{ rule.scope }}</dd>
        <dt>Enabled</dt>
        <dd>{{ rule.enabled }}</dd>
        <dt>Priority</dt>
        <dd>{{ rule.priority }}</dd>
      </dl>
    </section>

    <section class="preview-card">
      <h3>Payload</h3>
      <pre>{{ JSON.stringify(rule.payload, null, 2) }}</pre>
    </section>

    <section class="preview-card">
      <h3>YAML</h3>
      <pre>{{ rule.rawYaml || '(empty)' }}</pre>
    </section>
  </div>
</template>

<style scoped>
.preview-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-card {
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
  background: white;
}

.preview-card h3 {
  margin-top: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px 12px;
}

.summary-grid dt {
  font-weight: 700;
  color: #374151;
}

.summary-grid dd {
  margin: 0;
}

pre {
  margin: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
</style>