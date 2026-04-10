<script setup lang="ts">
import {computed, onMounted} from 'vue'
import {waitForBridge} from '../../bridge/desktopBridge'
import {useAnnotationSettingsStore} from '../../stores/annotationSettingsStore'
import StringListEditor from './StringListEditor.vue'
import LexiconEditor from './LexiconEditor.vue'
import ExtractorEditor from './ExtractorEditor.vue'

const store = useAnnotationSettingsStore()

const draftMeta = computed(() => store.draftMetaLine)

onMounted(async () => {
  const ready = await waitForBridge()
  if (!ready) {
    store.statusMessage = 'Desktop bridge unavailable.'
    return
  }

  await store.loadSettings()
})
</script>

<template>
  <section class="settings-shell">
    <div class="settings-topbar">
      <div>
        <h2>Annotation settings</h2>
        <p class="subtitle">
          Édite ici le socle global partagé par toutes les règles d’annotation.
          Les sections fréquentes sont maintenant guidées. Le `gloss_map` reste en YAML libre.
        </p>
        <p v-if="draftMeta" class="meta">{{ draftMeta }}</p>
      </div>

      <div class="actions">
        <button class="action-btn" @click="store.loadSettings()">Reload</button>
        <button class="action-btn primary" @click="store.saveSettings()">Save settings</button>
      </div>
    </div>

    <p v-if="store.statusMessage" class="status-line">{{ store.statusMessage }}</p>

    <div class="editor-grid">
      <StringListEditor
          title="POS definitions"
          :model-value="store.draft.posDefinitionsYaml"
          help-text="Liste globale des POS reconnus."
          empty-label="No POS definitions yet."
          @update:model-value="store.patchDraft({ posDefinitionsYaml: $event })"
      />

      <StringListEditor
          title="Feature definitions"
          :model-value="store.draft.featDefinitionsYaml"
          help-text="Liste globale des features CoNLL-U reconnues."
          empty-label="No feature definitions yet."
          @update:model-value="store.patchDraft({ featDefinitionsYaml: $event })"
      />

      <LexiconEditor
          :model-value="store.draft.lexiconsYaml"
          @update:model-value="store.patchDraft({ lexiconsYaml: $event })"
      />

      <ExtractorEditor
          :model-value="store.draft.extractorsYaml"
          @update:model-value="store.patchDraft({ extractorsYaml: $event })"
      />

      <section class="card full">
        <h3>Gloss map</h3>
        <p class="help">
          Cette section reste en YAML libre pour l’instant, car sa structure peut vite devenir plus variée.
        </p>
        <textarea
            class="editor"
            :value="store.draft.glossMapYaml"
            @input="store.patchDraft({ glossMapYaml: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>
    </div>

    <div class="preview-grid">
      <section class="card">
        <h3>Base YAML preview</h3>
        <pre>{{ store.draft.baseYamlPreview || 'No preview.' }}</pre>
      </section>

      <section class="card">
        <h3>Effective YAML preview</h3>
        <pre>{{ store.draft.effectiveYamlPreview || 'No preview.' }}</pre>
      </section>
    </div>
  </section>
</template>

<style scoped>
.settings-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settings-topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.subtitle {
  margin: 4px 0 0;
  color: #6b7280;
  max-width: 900px;
}

.meta {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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

.editor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}

.card.full {
  grid-column: 1 / -1;
}

.card h3 {
  margin-top: 0;
}

.help {
  margin: 4px 0 12px;
  color: #6b7280;
}

.editor {
  width: 100%;
  min-height: 180px;
  resize: vertical;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 12px;
  font: inherit;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: #fff;
}

pre {
  margin: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 540px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

@media (max-width: 1200px) {
  .settings-topbar {
    flex-direction: column;
  }

  .editor-grid,
  .preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>