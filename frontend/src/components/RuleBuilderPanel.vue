<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {callBridge, type RuleBuilderSchema, type RuleDescriptor,} from '../bridge/desktopBridge'
import DynamicFieldRenderer from './DynamicFieldRenderer.vue'

const descriptors = ref<RuleDescriptor[]>([])
const selectedKey = ref('')
const selectedSchema = ref<RuleBuilderSchema | null>(null)
const formState = ref<Record<string, unknown>>({})

const selectedDescriptor = computed(() =>
    descriptors.value.find((d) => `${d.kind}::${d.subtype}` === selectedKey.value) ?? null
)

function initForm(schema: RuleBuilderSchema) {
  const initial: Record<string, unknown> = {}

  for (const field of schema.fields) {
    if (field.defaultValue && typeof field.defaultValue === 'object') {
      initial[field.key] = field.type === 'JSON'
          ? JSON.stringify(field.defaultValue, null, 2)
          : field.defaultValue
    } else if (field.type === 'BOOLEAN') {
      initial[field.key] = false
    } else if (field.type === 'MULTISELECT' || field.type === 'STRING_LIST') {
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
  }
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

    <div v-if="selectedDescriptor" class="schema-meta">
      <h3>{{ selectedDescriptor.label }}</h3>
      <p>{{ selectedDescriptor.description }}</p>
    </div>

    <div v-if="selectedSchema" class="builder-grid">
      <div class="form-column">
        <DynamicFieldRenderer
            v-for="field in selectedSchema.fields"
            :key="field.key"
            :field="field"
            :model-value="formState[field.key]"
            @update:model-value="(value) => (formState[field.key] = value)"
        />
      </div>

      <div class="preview-column">
        <h4>Form state preview</h4>
        <pre>{{ JSON.stringify(formState, null, 2) }}</pre>
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

.toolbar-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.toolbar-select {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
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
  grid-template-columns: 1.2fr 1fr;
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

.empty-state {
  color: #6b7280;
}
</style>