<script setup lang="ts">
import {onMounted} from 'vue'
import {waitForBridge} from '../../bridge/desktopBridge'
import {useAnnotationSettingsStore} from '../../stores/annotationSettingsStore'

const store = useAnnotationSettingsStore()

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
          Édite ici le socle global partagé par toutes les règles d’annotation :
          définitions, lexiques, extracteurs et gloss map.
          Les règles sauvegardées dans le RuleWorkbench viendront ensuite s’y ajouter.
        </p>
      </div>

      <div class="actions">
        <button class="action-btn" @click="store.loadSettings()">Reload</button>
        <button class="action-btn primary" @click="store.saveSettings()">Save settings</button>
      </div>
    </div>

    <p v-if="store.statusMessage" class="status-line">{{ store.statusMessage }}</p>

    <div class="editor-grid">
      <section class="card">
        <h3>POS definitions</h3>
        <textarea
            class="editor"
            :value="store.draft.posDefinitionsYaml"
            @input="store.patchDraft({ posDefinitionsYaml: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>

      <section class="card">
        <h3>Feature definitions</h3>
        <textarea
            class="editor"
            :value="store.draft.featDefinitionsYaml"
            @input="store.patchDraft({ featDefinitionsYaml: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>

      <section class="card">
        <h3>Lexicons</h3>
        <textarea
            class="editor tall"
            :value="store.draft.lexiconsYaml"
            @input="store.patchDraft({ lexiconsYaml: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>

      <section class="card">
        <h3>Extractors</h3>
        <textarea
            class="editor tall"
            :value="store.draft.extractorsYaml"
            @input="store.patchDraft({ extractorsYaml: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>

      <section class="card full">
        <h3>Gloss map</h3>
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

.editor.tall {
  min-height: 260px;
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