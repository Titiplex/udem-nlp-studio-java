<script setup lang="ts">
import {ref, watch} from 'vue'
import {parseYamlList, stringifyYamlList} from '../../utils/annotationSettingsCodec'

const props = defineProps<{
  title: string
  modelValue: string
  helpText?: string
  emptyLabel?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const items = ref<string[]>([])
const parseError = ref('')

function syncFromProps() {
  try {
    items.value = parseYamlList(props.modelValue)
    parseError.value = ''
  } catch (error) {
    items.value = []
    parseError.value = error instanceof Error ? error.message : 'Invalid YAML list'
  }
}

function commit() {
  emit('update:modelValue', stringifyYamlList(items.value))
}

function addItem() {
  items.value.push('')
  commit()
}

function removeItem(index: number) {
  items.value.splice(index, 1)
  commit()
}

function updateItem(index: number, value: string) {
  items.value[index] = value
  commit()
}

watch(() => props.modelValue, syncFromProps, {immediate: true})
</script>

<template>
  <section class="card">
    <div class="card-header">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="helpText" class="help">{{ helpText }}</p>
      </div>

      <button class="action-btn" @click="addItem">Add</button>
    </div>

    <p v-if="parseError" class="error-text">
      {{ parseError }}
    </p>

    <div v-if="items.length > 0" class="item-list">
      <div v-for="(item, index) in items" :key="index" class="item-row">
        <input
            class="field-input"
            type="text"
            :value="item"
            @input="updateItem(index, ($event.target as HTMLInputElement).value)"
        >
        <button class="delete-btn" @click="removeItem(index)">Remove</button>
      </div>
    </div>

    <p v-else class="empty-state">{{ emptyLabel ?? 'No items yet.' }}</p>
  </section>
</template>

<style scoped>
.card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.card h3 {
  margin: 0;
}

.help {
  margin: 4px 0 0;
  color: #6b7280;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.field-input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
}

.action-btn,
.delete-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
}

.error-text {
  color: #b91c1c;
  margin: 0 0 12px;
}

.empty-state {
  color: #6b7280;
  margin: 0;
}
</style>