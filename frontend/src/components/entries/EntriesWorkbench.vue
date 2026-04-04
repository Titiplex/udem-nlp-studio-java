<script setup lang="ts">
import {onMounted} from 'vue'
import {waitForBridge} from '../../bridge/desktopBridge'
import {useEntryEditorStore} from '../../stores/entryEditorStore'
import EntryListPane from './EntryListPane.vue'
import EntryDetailForm from './EntryDetailForm.vue'
import EntryDiffPane from './EntryDiffPane.vue'

const store = useEntryEditorStore()

onMounted(async () => {
  const ready = await waitForBridge()

  if (!ready) {
    store.statusMessage = 'Desktop bridge unavailable.'
    return
  }

  await store.refreshEntries()

  if (store.selectedEntryId) {
    await store.loadEntry(store.selectedEntryId)
  } else {
    store.createNewEntry()
  }
})
</script>

<template>
  <div class="workbench">
    <EntryListPane
        :entries="store.entries"
        :selected-entry-id="store.selectedEntryId"
        @select="store.loadEntry($event)"
        @create="store.createNewEntry()"
    />

    <section class="editor-shell">
      <div class="editor-topbar">
        <div>
          <h2>Entries workspace</h2>
          <p class="editor-subtitle">
            {{ store.draft.id ? `Entry #${store.draft.documentOrder}` : 'New entry' }}
            <span v-if="store.dirty">• unsaved</span>
          </p>
        </div>

        <div class="editor-actions">
          <button class="action-btn" @click="store.saveEntry()">Save</button>
          <button class="action-btn primary" @click="store.runCorrection(false)">Run correction</button>
          <button class="action-btn" @click="store.runCorrection(true)">Force correction</button>
        </div>
      </div>

      <p v-if="store.statusMessage" class="status-line">{{ store.statusMessage }}</p>

      <EntryDetailForm
          :model-value="store.draft"
          @update:model-value="store.setDraft($event)"
      />

      <div class="diff-grid">
        <EntryDiffPane
            label="Chuj text"
            :raw="store.draft.rawChujText"
            :corrected="store.draft.correctedChujText"
        />
        <EntryDiffPane
            label="Gloss"
            :raw="store.draft.rawGlossText"
            :corrected="store.draft.correctedGlossText"
        />
        <EntryDiffPane
            label="Translation"
            :raw="store.draft.translation"
            :corrected="store.draft.correctedTranslation"
        />
      </div>

      <section class="preview-card">
        <h3>CoNLL-U preview</h3>
        <pre>{{ store.draft.conlluPreview || 'No preview yet.' }}</pre>
      </section>
    </section>
  </div>
</template>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  min-height: 700px;
}

.editor-shell {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.editor-topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.editor-topbar h2 {
  margin: 0;
}

.editor-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
}

.editor-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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

.diff-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.preview-card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.preview-card h3 {
  margin-top: 0;
}

.preview-card pre {
  margin: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

@media (max-width: 1200px) {
  .workbench {
    grid-template-columns: 1fr;
  }
}
</style>