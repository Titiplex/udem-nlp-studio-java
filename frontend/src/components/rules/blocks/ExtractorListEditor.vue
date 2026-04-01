<script setup lang="ts">
type ExtractItem = {
  type?: string
  extractor?: string
  into?: string
}

const props = defineProps<{
  modelValue?: ExtractItem[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ExtractItem[]]
}>()

function addItem() {
  emit('update:modelValue', [...(props.modelValue ?? []), {type: '', extractor: '', into: ''}])
}

function updateItem(index: number, patch: Partial<ExtractItem>) {
  const next = [...(props.modelValue ?? [])]
  next[index] = {...next[index], ...patch}
  emit('update:modelValue', next)
}

function removeItem(index: number) {
  emit('update:modelValue', (props.modelValue ?? []).filter((_, i) => i !== index))
}

function moveUp(index: number) {
  if (index <= 0) return
  const next = [...(props.modelValue ?? [])]
  ;[next[index - 1], next[index]] = [next[index], next[index - 1]]
  emit('update:modelValue', next)
}

function moveDown(index: number) {
  const list = props.modelValue ?? []
  if (index >= list.length - 1) return
  const next = [...list]
  ;[next[index], next[index + 1]] = [next[index + 1], next[index]]
  emit('update:modelValue', next)
}
</script>

<template>
  <div class="extract-editor">
    <div class="editor-header">
      <h4>Extractors</h4>
      <button class="ghost-btn" type="button" @click="addItem">Add extractor</button>
    </div>

    <p v-if="(modelValue ?? []).length === 0" class="empty-state">No extractors.</p>

    <div v-for="(item, index) in modelValue ?? []" :key="index" class="extract-card">
      <div class="extract-card-header">
        <strong>Extractor {{ index + 1 }}</strong>
        <div class="toolbar-actions">
          <button class="ghost-btn" type="button" @click="moveUp(index)">↑</button>
          <button class="ghost-btn" type="button" @click="moveDown(index)">↓</button>
          <button class="danger-btn" type="button" @click="removeItem(index)">Remove</button>
        </div>
      </div>

      <div class="extract-grid">
        <input
            class="field-input"
            :value="item.type ?? ''"
            placeholder="type"
            @input="updateItem(index, { type: ($event.target as HTMLInputElement).value })"
        />
        <input
            class="field-input"
            :value="item.extractor ?? ''"
            placeholder="extractor"
            @input="updateItem(index, { extractor: ($event.target as HTMLInputElement).value })"
        />
        <input
            class="field-input"
            :value="item.into ?? ''"
            placeholder="into"
            @input="updateItem(index, { into: ($event.target as HTMLInputElement).value })"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.extract-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
  background: #fafafa;
}

.editor-header,
.extract-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.editor-header h4 {
  margin: 0;
}

.extract-card {
  border: 1px solid #d1d5db;
  border-radius: 12px;
  padding: 12px;
  background: white;
}

.extract-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
  margin-top: 12px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
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
  padding: 8px 10px;
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