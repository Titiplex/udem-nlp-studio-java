<script setup lang="ts">
import {computed} from 'vue'

const props = defineProps<{
  modelValue: Record<string, unknown> | undefined
  title?: string
  valuePlaceholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, unknown>]
}>()

const entries = computed(() =>
    Object.entries(props.modelValue ?? {}).map(([key, value]) => ({
      key,
      value: String(value ?? ''),
    }))
)

function updateEntry(index: number, patch: Partial<{ key: string; value: string }>) {
  const next = entries.value.map((entry, i) => (i === index ? {...entry, ...patch} : entry))
  emitObject(next)
}

function addEntry() {
  emitObject([...entries.value, {key: '', value: ''}])
}

function removeEntry(index: number) {
  emitObject(entries.value.filter((_, i) => i !== index))
}

function emitObject(items: Array<{ key: string; value: string }>) {
  const out: Record<string, unknown> = {}
  for (const item of items) {
    if (!item.key.trim()) continue
    out[item.key.trim()] = item.value
  }
  emit('update:modelValue', out)
}
</script>

<template>
  <div class="kv-editor">
    <div class="editor-head">
      <h4>{{ title ?? 'Key / Value pairs' }}</h4>
      <button class="ghost-btn" @click="addEntry">Add</button>
    </div>

    <div v-if="entries.length === 0" class="empty-state">
      No entries.
    </div>

    <div v-for="(entry, index) in entries" :key="index" class="kv-row">
      <input
          class="field-input"
          :value="entry.key"
          placeholder="Feature key"
          @input="updateEntry(index, { key: ($event.target as HTMLInputElement).value })"
      />
      <input
          class="field-input"
          :value="entry.value"
          :placeholder="valuePlaceholder ?? 'Value'"
          @input="updateEntry(index, { value: ($event.target as HTMLInputElement).value })"
      />
      <button class="danger-btn" @click="removeEntry(index)">Remove</button>
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

.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-head h4 {
  margin: 0;
}

.kv-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 10px;
}

.field-input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
}

.ghost-btn,
.danger-btn {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  cursor: pointer;
}

.danger-btn {
  color: #b91c1c;
}

.empty-state {
  color: #6b7280;
}
</style>