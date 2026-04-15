<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {
  callBridge,
  type TextExport,
  waitForBridge,
  type WorkspaceDataImportResult,
  type WorkspaceExchangeRequest,
  type WorkspaceImportFileRequest,
} from '../../bridge/desktopBridge'
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const entryStore = useEntryEditorStore()

const preferCorrected = ref(true)
const correctedOnly = ref(false)

const exportFormat = ref('raw_text')
const importFormat = ref('raw_text')
const onlyEnabledRules = ref(true)
const selectedRuleKinds = ref<string[]>(['CORRECTION', 'ANNOTATION'])

const exportPreview = ref('')
const exportFileName = ref('')
const exchangeStatus = ref('')

const exportFormats = [
  {value: 'raw_text', label: 'Entries → raw text'},
  {value: 'conllu', label: 'Entries → CoNLL-U'},
  {value: 'entries_json', label: 'Entries → JSON'},
  {value: 'entries_csv', label: 'Entries → CSV (Excel-compatible)'},
  {value: 'entries_sql', label: 'Entries → SQL INSERT'},
  {value: 'rules_json', label: 'Rules → JSON'},
  {value: 'rules_yaml', label: 'Rules → YAML'},
  {value: 'workspace_bundle_json', label: 'Workspace bundle → JSON'},
]

const importFormats = [
  {value: 'raw_text', label: 'Import entries from raw text'},
  {value: 'entries_json', label: 'Import entries from JSON'},
  {value: 'rules_json', label: 'Import rules from JSON'},
  {value: 'workspace_bundle_json', label: 'Import workspace bundle from JSON'},
]

const showEntryOptions = computed(() =>
    ['raw_text', 'conllu', 'entries_json', 'entries_csv', 'entries_sql', 'workspace_bundle_json'].includes(exportFormat.value),
)

const showRuleOptions = computed(() =>
    ['rules_json', 'rules_yaml', 'workspace_bundle_json'].includes(exportFormat.value),
)

function buildExchangeRequest(): WorkspaceExchangeRequest {
  return {
    format: exportFormat.value,
    preferCorrected: preferCorrected.value,
    correctedOnly: correctedOnly.value,
    ruleKinds: selectedRuleKinds.value,
    onlyEnabledRules: onlyEnabledRules.value,
  }
}

async function reloadAll() {
  await entryStore.loadAggregateConlluPreview(preferCorrected.value, correctedOnly.value)
  await entryStore.loadAggregateRawPreview(preferCorrected.value, correctedOnly.value)
}

function toggleRuleKind(kind: string, checked: boolean) {
  if (checked) {
    if (!selectedRuleKinds.value.includes(kind)) {
      selectedRuleKinds.value = [...selectedRuleKinds.value, kind]
    }
    return
  }

  selectedRuleKinds.value = selectedRuleKinds.value.filter((value) => value !== kind)
}

function generateExportPreview() {
  const resp = callBridge<TextExport>('generateWorkspaceExport', JSON.stringify(buildExchangeRequest()))
  if (!resp.success || !resp.data) {
    exchangeStatus.value = resp.message ?? 'Export preview generation failed.'
    return
  }

  exportPreview.value = resp.data.content
  exportFileName.value = resp.data.fileName
  exchangeStatus.value = `Preview generated: ${resp.data.fileName}`
}

function saveExportToFile() {
  const resp = callBridge<string>('saveWorkspaceExport', JSON.stringify(buildExchangeRequest()))
  if (!resp.success) {
    exchangeStatus.value = resp.message ?? 'Save export failed.'
    return
  }

  const path = resp.data ?? ''
  exchangeStatus.value = path
      ? `Export saved: ${path}`
      : 'Export cancelled.'
}

function importFromFile(replaceExistingEntries: boolean, replaceExistingRules: boolean) {
  const payload: WorkspaceImportFileRequest = {
    format: importFormat.value,
    replaceExistingEntries,
    replaceExistingRules,
  }

  const resp = callBridge<WorkspaceDataImportResult>('importWorkspaceFromFile', JSON.stringify(payload))
  if (!resp.success || !resp.data) {
    exchangeStatus.value = resp.message ?? 'Import failed.'
    return
  }

  exchangeStatus.value = resp.data.summary
  void entryStore.refreshEntries()
  void reloadAll()
}

onMounted(async () => {
  const ready = await waitForBridge()
  if (!ready) {
    entryStore.statusMessage = 'Desktop bridge unavailable.'
    return
  }

  await reloadAll()
  generateExportPreview()
})
</script>

<template>
  <section class="preview-shell">
    <div class="topbar">
      <div>
        <h2>Preview & exchange</h2>
        <p class="subtitle">
          Preview agrégé du workspace, export multi-format et import piloté depuis l’app.
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
    <p v-if="exchangeStatus" class="status-line">{{ exchangeStatus }}</p>

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

    <section class="exchange-card">
      <div class="exchange-header">
        <div>
          <h3>Import / export center</h3>
          <p class="subtitle">
            Exporte les entrées, les règles ou un bundle complet. Importe depuis des fichiers texte ou JSON.
          </p>
        </div>
      </div>

      <div class="exchange-grid">
        <section class="tool-card">
          <h4>Export</h4>

          <div class="field">
            <label class="field-label">Format</label>
            <select v-model="exportFormat" class="field-input">
              <option v-for="fmt in exportFormats" :key="fmt.value" :value="fmt.value">
                {{ fmt.label }}
              </option>
            </select>
          </div>

          <div v-if="showEntryOptions" class="option-group">
            <label class="checkbox-row">
              <input v-model="preferCorrected" type="checkbox">
              <span>Use corrected text when available</span>
            </label>
            <label class="checkbox-row">
              <input v-model="correctedOnly" type="checkbox">
              <span>Only corrected entries</span>
            </label>
          </div>

          <div v-if="showRuleOptions" class="option-group">
            <label class="checkbox-row">
              <input
                  type="checkbox"
                  :checked="selectedRuleKinds.includes('CORRECTION')"
                  @change="toggleRuleKind('CORRECTION', ($event.target as HTMLInputElement).checked)"
              >
              <span>Correction rules</span>
            </label>

            <label class="checkbox-row">
              <input
                  type="checkbox"
                  :checked="selectedRuleKinds.includes('ANNOTATION')"
                  @change="toggleRuleKind('ANNOTATION', ($event.target as HTMLInputElement).checked)"
              >
              <span>Annotation rules</span>
            </label>

            <label class="checkbox-row">
              <input v-model="onlyEnabledRules" type="checkbox">
              <span>Only enabled rules</span>
            </label>
          </div>

          <div class="tool-actions">
            <button class="action-btn" @click="generateExportPreview()">Generate preview</button>
            <button class="action-btn primary" @click="saveExportToFile()">Save to file</button>
          </div>

          <p class="mini-info">
            Current export file: <strong>{{ exportFileName || 'not generated yet' }}</strong>
          </p>
        </section>

        <section class="tool-card">
          <h4>Import</h4>

          <div class="field">
            <label class="field-label">Format</label>
            <select v-model="importFormat" class="field-input">
              <option v-for="fmt in importFormats" :key="fmt.value" :value="fmt.value">
                {{ fmt.label }}
              </option>
            </select>
          </div>

          <p class="tool-help">
            Le sélecteur de fichier s’ouvre via le bridge desktop. Les imports JSON servent aux entrées, règles et
            bundles complets.
          </p>

          <div class="tool-actions">
            <button class="action-btn" @click="importFromFile(false, false)">Import append</button>
            <button class="action-btn primary" @click="importFromFile(true, true)">Replace matching data</button>
          </div>
        </section>
      </div>

      <section class="preview-card export-preview-card">
        <h4>Generated export preview</h4>
        <pre>{{ exportPreview || 'No export preview generated yet.' }}</pre>
      </section>
    </section>

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

.exchange-card,
.preview-card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}

.exchange-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
  margin-top: 14px;
}

.tool-card {
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.field-input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
  width: 100%;
}

.tool-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.tool-help,
.mini-info {
  color: #6b7280;
}

.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.preview-card h3,
.preview-card h4,
.tool-card h4 {
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

.export-preview-card {
  margin-top: 16px;
}

@media (max-width: 1200px) {
  .topbar {
    flex-direction: column;
  }

  .exchange-grid,
  .preview-grid {
    grid-template-columns: 1fr;
  }
}
</style>