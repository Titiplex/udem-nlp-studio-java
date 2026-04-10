<script setup lang="ts">
import {computed, onMounted} from 'vue'
import {waitForBridge} from '../../bridge/desktopBridge'
import {useEntryEditorStore} from '../../stores/entryEditorStore'
import EntryListPane from './EntryListPane.vue'
import EntryDetailForm from './EntryDetailForm.vue'
import EntryDiffPane from './EntryDiffPane.vue'
import ConflictBanner from '../common/ConflictBanner.vue'

const store = useEntryEditorStore()

const draftMeta = computed(() => store.draftMetaLine)

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
          <p v-if="draftMeta" class="editor-meta">{{ draftMeta }}</p>
        </div>

        <div class="editor-actions">
          <button class="action-btn" @click="store.saveEntry()">Save</button>
          <button class="action-btn primary" @click="store.runCorrection(false)">Run correction</button>
          <button class="action-btn" @click="store.runCorrection(true)">Force correction</button>
        </div>
      </div>

      <ConflictBanner
          v-if="store.hasConflict"
          title="Entry save conflict"
          :message="store.conflictMessage"
          :can-reload="!!store.selectedEntryId || !!store.draft.id"
          @reload="store.reloadRemoteVersion()"
          @dismiss="store.clearConflict()"
      />

      <p v-if="store.statusMessage && !store.hasConflict" class="status-line">{{ store.statusMessage }}</p>

      <section class="workspace-tools">
        <div class="tool-card">
          <h3>Corpus import</h3>
          <p class="tool-help">
            Colle ici un corpus brut au format interlinéaire : ligne Chuj, ligne gloss,
            ligne traduction, puis ligne vide entre les entrées.
          </p>
          <textarea
              v-model="store.importBuffer"
              class="tool-textarea"
              placeholder="Ix naq&#10;A1 ganar&#10;Il gagne.&#10;&#10;Ha ix to&#10;DEM A1 ir&#10;Celui-ci va."
          />
          <div class="tool-actions">
            <button class="action-btn" @click="store.importEntries(false)">Import append</button>
            <button class="action-btn" @click="store.importEntries(true)">Replace workspace</button>
          </div>
        </div>

        <div class="tool-card">
          <h3>Batch actions</h3>
          <p class="tool-help">
            Lance les traitements sur tout le workspace sans devoir ouvrir chaque entrée.
          </p>
          <div class="tool-actions">
            <button class="action-btn primary" @click="store.runCorrectionOnAll(false)">
              Correct all
            </button>
            <button class="action-btn" @click="store.runCorrectionOnAll(true)">
              Force all
            </button>
            <button class="action-btn" @click="store.loadAggregateRawPreview(true, false)">
              Refresh raw export
            </button>
            <button class="action-btn" @click="store.loadAggregateConlluPreview(true, false)">
              Refresh CoNLL-U export
            </button>
          </div>
        </div>
      </section>

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

.editor-meta {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 13px;
}

.editor-actions,
.tool-actions {
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

.workspace-tools {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

.tool-card {
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.tool-card h3 {
  margin-top: 0;
}

.tool-help {
  margin: 0 0 12px;
  color: #6b7280;
}

.tool-textarea {
  width: 100%;
  min-height: 180px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
  resize: vertical;
  margin-bottom: 12px;
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

  .workspace-tools {
    grid-template-columns: 1fr;
  }
}
</style>