<script setup lang="ts">
import type {RuleDetail} from '../../bridge/desktopBridge'

const props = defineProps<{
  modelValue: RuleDetail
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RuleDetail]
}>()

function patch<K extends keyof RuleDetail>(key: K, value: RuleDetail[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value,
  })
}
</script>

<template>
  <div class="meta-grid">
    <div class="field">
      <label class="field-label" for="rule-name-input">Name</label>
      <input
          id="rule-name-input"
          class="field-input"
          :value="modelValue.name"
          @input="patch('name', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="field">
      <label class="field-label" for="rule-scope-input">Scope</label>
      <input
          id="rule-scope-input"
          class="field-input"
          :value="modelValue.scope"
          @input="patch('scope', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="field">
      <label class="field-label" for="rule-priority-input">Priority</label>
      <input
          id="rule-priority-input"
          class="field-input"
          type="number"
          :value="modelValue.priority"
          @input="patch('priority', Number(($event.target as HTMLInputElement).value))"
      />
    </div>

    <div class="field checkbox-row">
      <input
          id="rule-enabled-input"
          type="checkbox"
          :checked="modelValue.enabled"
          @change="patch('enabled', ($event.target as HTMLInputElement).checked)"
      />
      <label class="field-label" for="rule-enabled-input">Enabled</label>
    </div>

    <div class="field full">
      <label class="field-label" for="rule-description-input">Description</label>
      <textarea
          id="rule-description-input"
          class="field-textarea"
          :value="modelValue.description"
          @input="patch('description', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>
  </div>
</template>

<style scoped>
.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field.full {
  grid-column: 1 / -1;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
}

.field-input,
.field-textarea {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
}

.field-textarea {
  min-height: 90px;
  resize: vertical;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>