<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {waitForBridge} from '../../bridge/desktopBridge'
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const entryStore = useEntryEditorStore()

const preferCorrected = ref(true)
const correctedOnly = ref(false)

async function reloadAll() {
  await entryStore.loadAggregateConlluPreview(preferCorrected.value, correctedOnly.value)
  await entryStore.loadAggregateRawPreview(preferCorrected.value, correctedOnly.value)
}

onMounted(async () => {
  const ready = await waitForBridge()
  if (!ready) {
    entryStore.statusMessage = 'Desktop bridge unavailable.'
    return
  }

  await reloadAll()
})
</script>

<template>
  <section class="preview-shell">
    <div class="topbar">
      <div>
        <h2>Preview</h2>
        <p class="subtitle">
          Aperçu agrégé du workspace courant, utile pour relire ou exporter tout le corpus.
        </p>
      </div>

      <div class="actions">
        <label class="checkbox-row">
          <input v-model="preferCorrected" type="checkbox">
          <span>Prefer corrected text</span>
        </label>

        <label class="checkbox-row">
          <input v-model="correctedOnly" type="checkbox">
          <span>Corrected only</span>
        </label>

        <button class="action-btn primary" @click="reloadAll()">Refresh preview</button>
      </div>
    </div>

    <p v-if="entryStore.statusMessage" class="status-line">{{ entryStore.statusMessage }}</p>

    <div class="stats-row">
      <div class="stat-card">
        <strong>{{ entryStore.entryCount }}</strong>
        <span>Total entries</span>
      </div>
      <div class="stat-card">
        <strong>{{ entryStore.correctedCount }}</strong>
        <span>Corrected entries</span>
      </div>
    </div>

    <div class="preview-grid">
      <section class="preview-card">
        <h3>Workspace raw export</h3>
        <pre>{{ entryStore.aggregateRawPreview || 'No raw export preview yet.' }}</pre>
      </section>

      <section class="preview-card">
        <h3>Workspace CoNLL-U export</h3>
        <pre>{{ entryStore.aggregateConlluPreview || 'No CoNLL-U preview yet.' }}</pre>
      </section>
    </div>
  </section>
</template>

<style scoped>
.preview-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.subtitle {
  margin: 4px 0 0;
  color: #6b7280;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 14px;
}

.action-btn.primary {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.status-line {
  margin: 0;
  color: #374151;
}

.stats-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stat-card {
  min-width: 160px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card strong {
  font-size: 20px;
}

.stat-card span {
  color: #6b7280;
}

.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
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
  max-height: 620px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

@media (max-width: 1200px) {
  .topbar {
    flex-direction: column;
  }

  .preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>