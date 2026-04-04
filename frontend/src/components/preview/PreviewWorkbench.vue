<script setup lang="ts">
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const entryStore = useEntryEditorStore()
</script>

<template>
  <section class="preview-shell">
    <h2>Preview</h2>
    <p class="subtitle">
      Vue concentrée sur l’aperçu CoNLL-U de l’entrée actuellement sélectionnée.
    </p>

    <div class="preview-card">
      <h3 v-if="entryStore.draft.id">Entry #{{ entryStore.draft.documentOrder }}</h3>
      <h3 v-else>No entry selected</h3>
      <pre>{{ entryStore.draft.conlluPreview || 'No CoNLL-U preview yet.' }}</pre>
    </div>
  </section>
</template>

<style scoped>
.preview-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.subtitle {
  margin: 0;
  color: #6b7280;
}

.preview-card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}

.preview-card h3 {
  margin-top: 0;
}

.preview-card pre {
  margin: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
</style>