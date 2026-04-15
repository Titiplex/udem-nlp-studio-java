<script setup lang="ts">
import {computed, ref} from 'vue'
import {
  callBridge,
  type TextExport,
  type WorkspaceDataImportResult,
  type WorkspaceExchangeRequest,
  type WorkspaceImportFileRequest,
} from '../../bridge/desktopBridge'
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const entryStore = useEntryEditorStore()

const menuOpen = ref(false)
const exchangeStatus = ref('')

const exportFormat = ref('workspace_bundle_json')
const importFormat = ref('workspace_bundle_json')
const preferCorrected = ref(true)
const correctedOnly = ref(false)
const onlyEnabledRules = ref(true)
const includeAnnotationSettings = ref(true)
const selectedRuleKinds = ref<string[]>(['CORRECTION', 'ANNOTATION'])

const exportFileName = ref('')
const exportPreview = ref('')

const exportFormats = [
  {value: 'raw_text', label: 'Entries → raw text'},
  {value: 'conllu', label: 'Entries → CoNLL-U'},
  {value: 'entries_json', label: 'Entries → JSON'},
  {value: 'entries_csv', label: 'Entries → CSV (Excel-compatible)'},
  {value: 'entries_sql', label: 'Entries → SQL INSERT'},
  {value: 'rules_json', label: 'Rules → JSON'},
  {value: 'rules_yaml', label: 'Rules → YAML (all)'},
  {value: 'correction_rules_yaml', label: 'Correction rules → YAML'},
  {value: 'annotation_rules_yaml', label: 'Annotation rules → YAML'},
  {value: 'annotation_settings_json', label: 'Annotation settings → JSON'},
  {value: 'annotation_settings_yaml', label: 'Annotation settings → YAML'},
  {value: 'workspace_bundle_json', label: 'Workspace bundle → JSON'},
]

const importFormats = [
  {value: 'raw_text', label: 'Import entries from raw text'},
  {value: 'entries_json', label: 'Import entries from JSON'},
  {value: 'rules_json', label: 'Import rules from JSON'},
  {value: 'correction_rules_yaml', label: 'Import correction rules from YAML'},
  {value: 'annotation_rules_yaml', label: 'Import annotation rules from YAML'},
  {value: 'annotation_settings_json', label: 'Import annotation settings from JSON'},
  {value: 'annotation_settings_yaml', label: 'Import annotation settings from YAML'},
  {value: 'workspace_bundle_json', label: 'Import workspace bundle from JSON'},
]

const showEntryOptions = computed(() =>
    ['raw_text', 'conllu', 'entries_json', 'entries_csv', 'entries_sql', 'workspace_bundle_json'].includes(exportFormat.value),
)

const showRuleOptions = computed(() =>
    ['rules_json', 'rules_yaml', 'correction_rules_yaml', 'annotation_rules_yaml', 'workspace_bundle_json'].includes(exportFormat.value),
)

const showSettingsOption = computed(() => exportFormat.value === 'workspace_bundle_json')

function buildExchangeRequest(): WorkspaceExchangeRequest {
  return {
    format: exportFormat.value,
    preferCorrected: preferCorrected.value,
    correctedOnly: correctedOnly.value,
    ruleKinds: selectedRuleKinds.value,
    onlyEnabledRules: onlyEnabledRules.value,
    includeAnnotationSettings: includeAnnotationSettings.value,
  }
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

async function refreshEntryPreviews() {
  await entryStore.loadAggregateConlluPreview(preferCorrected.value, correctedOnly.value)
  await entryStore.loadAggregateRawPreview(preferCorrected.value, correctedOnly.value)
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
  exchangeStatus.value = path ? `Export saved: ${path}` : 'Export cancelled.'
}

async function importFromFile(replaceExistingEntries: boolean, replaceExistingRules: boolean, replaceAnnotationSettings: boolean) {
  const payload: WorkspaceImportFileRequest = {
    format: importFormat.value,
    replaceExistingEntries,
    replaceExistingRules,
    replaceAnnotationSettings,
  }

  const resp = callBridge<WorkspaceDataImportResult>('importWorkspaceFromFile', JSON.stringify(payload))
  if (!resp.success || !resp.data) {
    exchangeStatus.value = resp.message ?? 'Import failed.'
    return
  }

  exchangeStatus.value = resp.data.summary
  await entryStore.refreshEntries()
  await refreshEntryPreviews()
}

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}
</script>

<template>
  <div class="menu-root">
    <div class="menu-strip">
      <button class="menu-btn" :class="{ active: menuOpen }" @click="toggleMenu()">
        File
      </button>
      <button class="menu-btn disabled" type="button">
        Help
      </button>
    </div>

    <div v-if="menuOpen" class="menu-panel">
      <div class="menu-columns">
        <section class="menu-card">
          <h3>Export</h3>

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
              <span>Prefer corrected text</span>
            </label>
            <label class="checkbox-row">
              <input v-model="correctedOnly" type="checkbox">
              <span>Corrected only</span>
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

          <div v-if="showSettingsOption" class="option-group">
            <label class="checkbox-row">
              <input v-model="includeAnnotationSettings" type="checkbox">
              <span>Include annotation settings in bundle</span>
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

        <section class="menu-card">
          <h3>Import</h3>

          <div class="field">
            <label class="field-label">Format</label>
            <select v-model="importFormat" class="field-input">
              <option v-for="fmt in importFormats" :key="fmt.value" :value="fmt.value">
                {{ fmt.label }}
              </option>
            </select>
          </div>

          <p class="mini-info">
            Les imports YAML des règles sont explicites par type pour éviter les déductions foireuses sur le kind.
          </p>

          <div class="tool-actions">
            <button class="action-btn" @click="importFromFile(false, false, false)">Import append</button>
            <button class="action-btn primary" @click="importFromFile(true, true, true)">Replace matching data</button>
          </div>
        </section>
      </div>

      <section class="preview-card">
        <h3>Generated export preview</h3>
        <pre>{{ exportPreview || 'No export preview generated yet.' }}</pre>
      </section>

      <p v-if="exchangeStatus" class="status-line">{{ exchangeStatus }}</p>
    </div>
  </div>
</template>

<style scoped>
.menu-root {
  position: relative;
}

.menu-strip {
  display: flex;
  align-items: center;
  gap: 6px;
}

.menu-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  font: inherit;
  cursor: pointer;
}

.menu-btn.active {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.menu-btn.disabled {
  opacity: 0.55;
  cursor: default;
}

.menu-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  width: min(980px, calc(100vw - 80px));
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.12);
  padding: 16px;
  z-index: 20;
}

.menu-columns {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
}

.menu-card,
.preview-card {
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.menu-card h3,
.preview-card h3 {
  margin-top: 0;
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

.option-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 14px;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

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
  cursor: pointer;
}

.action-btn.primary {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.preview-card {
  margin-top: 16px;
}

.preview-card pre {
  margin: 0;
  background: white;
  border-radius: 12px;
  padding: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  overflow: auto;
  max-height: 320px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.status-line,
.mini-info {
  color: #6b7280;
}

.status-line {
  margin: 14px 0 0;
}

@media (max-width: 1100px) {
  .menu-panel {
    width: min(94vw, 94vw);
  }

  .menu-columns {
    grid-template-columns: 1fr;
  }
}
</style>