<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {
  callBridge,
  type RuleBuilderSchema,
  type RuleDescriptor,
  type RuleDetail,
  type RuleDraftResult,
  type ValidationIssue,
} from '../bridge/desktopBridge'
import DynamicFieldRenderer from './DynamicFieldRenderer.vue'

const descriptors = ref<RuleDescriptor[]>([])
const selectedKey = ref('')
const selectedSchema = ref<RuleBuilderSchema | null>(null)
const formState = ref<Record<string, unknown>>({})
const rawYaml = ref('')
const issues = ref<ValidationIssue[]>([])
const saveMessage = ref('')

const meta = ref({
  id: null as string | null,
  name: '',
  kind: '',
  subtype: '',
  scope: 'token',
  enabled: true,
  priority: 100,
  description: '',
})

const selectedDescriptor = computed(() =>
    descriptors.value.find((d) => `${d.kind}::${d.subtype}` === selectedKey.value) ?? null
)

function initForm(schema: RuleBuilderSchema) {
  const initial: Record<string, unknown> = {}

  for (const field of schema.fields) {
    if (field.defaultValue && typeof field.defaultValue === 'object' && Object.keys(field.defaultValue).length > 0) {
      initial[field.key] = structuredClone(field.defaultValue)
    } else if (field.type === 'BOOLEAN') {
      initial[field.key] = false
    } else if (field.type === 'MULTISELECT' || field.type === 'STRING_LIST') {
      initial[field.key] = []
    } else if (field.type === 'KEY_VALUE' || field.type === 'OBJECT') {
      initial[field.key] = {}
    } else if (field.type === 'OBJECT_LIST') {
      initial[field.key] = []
    } else {
      initial[field.key] = ''
    }
  }

  formState.value = initial
}

function loadSchema(kind: string, subtype: string) {
  const resp = callBridge<RuleBuilderSchema>('getRuleSchema', kind, subtype)
  selectedSchema.value = resp.data ?? null

  if (selectedSchema.value) {
    initForm(selectedSchema.value)
    meta.value.kind = kind
    meta.value.subtype = subtype
  }
}

function buildDraft(): RuleDetail {
  return {
    id: meta.value.id,
    name: meta.value.name,
    kind: meta.value.kind,
    subtype: meta.value.subtype,
    scope: meta.value.scope,
    enabled: meta.value.enabled,
    priority: meta.value.priority,
    description: meta.value.description,
    payload: {
      name: meta.value.name,
      scope: meta.value.scope,
      description: meta.value.description,
      ...formState.value,
    },
    rawYaml: rawYaml.value,
  }
}

function applyDraft(rule: RuleDetail) {
  meta.value = {
    id: rule.id,
    name: rule.name ?? '',
    kind: rule.kind ?? '',
    subtype: rule.subtype ?? '',
    scope: rule.scope ?? 'token',
    enabled: rule.enabled ?? true,
    priority: rule.priority ?? 100,
    description: rule.description ?? '',
  }

  rawYaml.value = rule.rawYaml ?? ''

  const nextPayload = {...(rule.payload ?? {})}
  delete nextPayload.name
  delete nextPayload.scope
  delete nextPayload.description
  formState.value = nextPayload
}

function applyResult(result?: RuleDraftResult) {
  if (!result) return
  applyDraft(result.rule)
  issues.value = result.issues ?? []
}

function generateYaml() {
  const resp = callBridge<RuleDraftResult>('generateRuleYaml', JSON.stringify(buildDraft()))
  if (!resp.success) {
    saveMessage.value = resp.message ?? 'YAML generation failed'
    return
  }
  applyResult(resp.data)
  saveMessage.value = 'YAML généré.'
}

function parseYaml() {
  const resp = callBridge<RuleDraftResult>('parseRuleYaml', JSON.stringify(buildDraft()))
  if (!resp.success) {
    saveMessage.value = resp.message ?? 'YAML parse failed'
    return
  }
  applyResult(resp.data)
  saveMessage.value = 'YAML parsé.'
}

function validateDraft() {
  const resp = callBridge<RuleDraftResult>('validateRule', JSON.stringify(buildDraft()))
  if (!resp.success) {
    saveMessage.value = resp.message ?? 'Validation failed'
    return
  }
  applyResult(resp.data)
  saveMessage.value = 'Validation terminée.'
}

function saveDraft() {
  const resp = callBridge<RuleDraftResult>('saveRule', JSON.stringify(buildDraft()))
  if (!resp.success) {
    saveMessage.value = resp.message ?? 'Save failed'
    return
  }
  applyResult(resp.data)
  saveMessage.value = 'Règle sauvegardée.'
}

onMounted(() => {
  const resp = callBridge<RuleDescriptor[]>('listRuleDescriptors')
  descriptors.value = resp.data ?? []

  if (descriptors.value.length > 0) {
    const first = descriptors.value[0]
    selectedKey.value = `${first.kind}::${first.subtype}`
  }
})

watch(selectedKey, (value) => {
  const descriptor = descriptors.value.find((d) => `${d.kind}::${d.subtype}` === value)
  if (descriptor) {
    loadSchema(descriptor.kind, descriptor.subtype)
  }
})
</script>

<template>
  <div class="builder-panel">
    <div class="builder-toolbar">
      <div class="toolbar-block">
        <label class="toolbar-label">Rule type</label>
        <select v-model="selectedKey" class="toolbar-select">
          <option value="">Select a rule type…</option>
          <option
              v-for="descriptor in descriptors"
              :key="`${descriptor.kind}::${descriptor.subtype}`"
              :value="`${descriptor.kind}::${descriptor.subtype}`"
          >
            {{ descriptor.label }} — {{ descriptor.kind }}/{{ descriptor.subtype }}
          </option>
        </select>
      </div>
    </div>

    <div class="meta-grid">
      <div class="field">
        <label class="field-label">Name</label>
        <input v-model="meta.name" class="field-input" type="text"/>
      </div>

      <div class="field">
        <label class="field-label">Scope</label>
        <input v-model="meta.scope" class="field-input" type="text"/>
      </div>

      <div class="field">
        <label class="field-label">Priority</label>
        <input v-model.number="meta.priority" class="field-input" type="number"/>
      </div>

      <div class="field checkbox-row">
        <input v-model="meta.enabled" type="checkbox"/>
        <label class="field-label">Enabled</label>
      </div>
    </div>

    <div class="field">
      <label class="field-label">Description</label>
      <textarea v-model="meta.description" class="field-textarea"/>
    </div>

    <div v-if="selectedDescriptor" class="schema-meta">
      <h3>{{ selectedDescriptor.label }}</h3>
      <p>{{ selectedDescriptor.description }}</p>
    </div>

    <div v-if="selectedSchema" class="builder-grid">
      <div class="form-column">
        <h4>Visual editor</h4>

        <DynamicFieldRenderer
            v-for="field in selectedSchema.fields"
            :key="field.key"
            :field="field"
            :model-value="formState[field.key]"
            @update:model-value="(value) => (formState[field.key] = value)"
        />
      </div>

      <div class="preview-column">
        <h4>YAML editor</h4>
        <textarea
            v-model="rawYaml"
            class="yaml-editor"
            placeholder="- name: My rule&#10;  scope: token&#10;  match: ..."
        />

        <div class="action-row">
          <button class="action-btn" @click="generateYaml">Generate YAML</button>
          <button class="action-btn" @click="parseYaml">Parse YAML</button>
          <button class="action-btn" @click="validateDraft">Validate</button>
          <button class="action-btn primary" @click="saveDraft">Save</button>
        </div>

        <p v-if="saveMessage" class="status-text">{{ saveMessage }}</p>

        <div class="issues-panel">
          <h4>Validation</h4>
          <p v-if="issues.length === 0" class="empty-state">No validation issues.</p>
          <ul v-else class="issues-list">
            <li v-for="issue in issues" :key="`${issue.path}-${issue.message}`" :class="issue.level">
              <strong>{{ issue.level.toUpperCase() }}</strong>
              <span>{{ issue.path }} — {{ issue.message }}</span>
            </li>
          </ul>
        </div>

        <h4>Payload preview</h4>
        <pre>{{ JSON.stringify(buildDraft(), null, 2) }}</pre>
      </div>
    </div>

    <p v-else class="empty-state">
      No schema loaded.
    </p>
  </div>
</template>

<style scoped>
.builder-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.builder-toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 320px;
}

.toolbar-label,
.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.toolbar-select,
.field-input,
.field-textarea,
.yaml-editor {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
  width: 100%;
}

.field-textarea,
.yaml-editor {
  min-height: 120px;
  resize: vertical;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.schema-meta h3 {
  margin: 0 0 6px;
}

.schema-meta p {
  margin: 0;
  color: #6b7280;
}

.builder-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 20px;
}

.form-column,
.preview-column {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.preview-column h4 {
  margin-top: 0;
}

.preview-column pre {
  margin: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  margin-bottom: 12px;
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

.status-text {
  margin: 0 0 12px;
  color: #374151;
}

.issues-panel {
  margin-bottom: 16px;
}

.issues-list {
  margin: 0;
  padding-left: 18px;
}

.issues-list li {
  margin-bottom: 8px;
}

.issues-list li.error {
  color: #b91c1c;
}

.issues-list li.warning {
  color: #92400e;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-state {
  color: #6b7280;
}
</style>