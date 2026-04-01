<script setup lang="ts">
const props = defineProps<{
  modelValue?: string[]
  title?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

function addItem() {
  emit('update:modelValue', [...(props.modelValue ?? []), ''])
}

function updateItem(index: number, value: string) {
  const next = [...(props.modelValue ?? [])]
  next[index] = value
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
  <div class="list-editor">
    <div class="editor-header">
      <h4>{{ title ?? 'List' }}</h4>
      <button class="ghost-btn" type="button" @click="addItem">Add</button>
    </div>

    <p v-if="(modelValue ?? []).length === 0" class="empty-state">No items.</p>

    <div v-for="(item, index) in modelValue ?? []" :key="index" class="list-row">
      <input
          class="field-input"
          :value="item"
          :placeholder="placeholder ?? 'Value'"
          @input="updateItem(index, ($event.target as HTMLInputElement).value)"
      />
      <button class="ghost-btn" type="button" @click="moveUp(index)">↑</button>
      <button class="ghost-btn" type="button" @click="moveDown(index)">↓</button>
      <button class="danger-btn" type="button" @click="removeItem(index)">Remove</button>
    </div>
  </div>
</template>

<style scoped>
.list-editor {
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

.list-row {
  display: grid;
  grid-template-columns: 1fr auto auto auto;
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