<script setup lang="ts">
import {computed} from 'vue'

type Entry = { key: string; value: string }

const props = defineProps<{
  modelValue?: Record<string, unknown>
  title?: string
  keyPlaceholder?: string
  valuePlaceholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
}>()

const entries = computed<Entry[]>(() =>
    Object.entries(props.modelValue ?? {}).map(([key, value]) => ({
      key,
      value: String(value ?? ''),
    }))
)

function emitEntries(nextEntries: Entry[]) {
  const out: Record<string, unknown> = {}
  for (const entry of nextEntries) {
    const key = entry.key.trim()
    if (!key) continue
    out[key] = entry.value
  }
  emit('update:modelValue', out)
}

function addEntry() {
  emitEntries([...entries.value, {key: '', value: ''}])
}

function updateKey(index: number, key: string) {
  emitEntries(entries.value.map((entry, i) => i === index ? {...entry, key} : entry))
}

function updateValue(index: number, value: string) {
  emitEntries(entries.value.map((entry, i) => i === index ? {...entry, value} : entry))
}

function removeEntry(index: number) {
  emitEntries(entries.value.filter((_, i) => i !== index))
}
</script>

<template>
  <div class="kv-editor">
    <div class="editor-header">
      <h4>{{ title ?? 'Key / Value' }}</h4>
      <button class="ghost-btn" type="button" @click="addEntry">Add</button>
    </div>

    <p v-if="entries.length === 0" class="empty-state">No entries.</p>

    <div v-for="(entry, index) in entries" :key="index" class="kv-row">
      <input
          class="field-input"
          :value="entry.key"
          :placeholder="keyPlaceholder ?? 'Key'"
          @input="updateKey(index, ($event.target as HTMLInputElement).value)"
      />
      <input
          class="field-input"
          :value="entry.value"
          :placeholder="valuePlaceholder ?? 'Value'"
          @input="updateValue(index, ($event.target as HTMLInputElement).value)"
      />
      <button class="danger-btn" type="button" @click="removeEntry(index)">Remove</button>
    </div>
  </div>
</template>

<style scoped>
.kv-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
  background: #fafafa;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.editor-header h4 {
  margin: 0;
}

.kv-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 10px;
}

.field-input {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: white;
}

.ghost-btn,
.danger-btn {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: white;
  cursor: pointer;
}

.danger-btn {
  color: #b91c1c;
}

.empty-state {
  margin: 0;
  color: #6b7280;
}
</style>