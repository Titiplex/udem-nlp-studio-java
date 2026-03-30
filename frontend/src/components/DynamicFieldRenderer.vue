<script setup lang="ts">
import type {FieldDescriptor} from '../bridge/desktopBridge'

const props = defineProps<{
  field: FieldDescriptor
  modelValue: unknown
}>()

const emit = defineEmits<{
  'update:modelValue': [value: unknown]
}>()

function update(value: unknown) {
  emit('update:modelValue', value)
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value) ? value.map(String) : []
}
</script>

<template>
  <div class="field">
    <label class="field-label">
      {{ field.label }}
      <span v-if="field.required" class="required">*</span>
    </label>

    <p v-if="field.helpText" class="field-help">
      {{ field.helpText }}
    </p>

    <input
        v-if="field.type === 'TEXT'"
        class="field-input"
        type="text"
        :placeholder="field.placeholder ?? ''"
        :value="(modelValue as string) ?? ''"
        @input="update(($event.target as HTMLInputElement).value)"
    />

    <textarea
        v-else-if="field.type === 'TEXTAREA' || field.type === 'YAML' || field.type === 'JSON' || field.type === 'TEMPLATE'"
        class="field-textarea"
        :placeholder="field.placeholder ?? ''"
        :value="typeof modelValue === 'string' ? modelValue : JSON.stringify(modelValue ?? {}, null, 2)"
        @input="update(($event.target as HTMLTextAreaElement).value)"
    />

    <input
        v-else-if="field.type === 'NUMBER'"
        class="field-input"
        type="number"
        :value="typeof modelValue === 'number' ? modelValue : 0"
        @input="update(Number(($event.target as HTMLInputElement).value))"
    />

    <label v-else-if="field.type === 'BOOLEAN'" class="checkbox-row">
      <input
          type="checkbox"
          :checked="Boolean(modelValue)"
          @change="update(($event.target as HTMLInputElement).checked)"
      />
      <span>Enabled</span>
    </label>

    <select
        v-else-if="field.type === 'SELECT'"
        class="field-input"
        :value="(modelValue as string) ?? ''"
        @change="update(($event.target as HTMLSelectElement).value)"
    >
      <option value="">Select…</option>
      <option
          v-for="option in field.enumValues"
          :key="option"
          :value="option"
      >
        {{ option }}
      </option>
    </select>

    <div v-else-if="field.type === 'MULTISELECT'" class="multiselect-list">
      <label
          v-for="option in field.enumValues"
          :key="option"
          class="checkbox-row"
      >
        <input
            type="checkbox"
            :checked="asStringArray(modelValue).includes(option)"
            @change="
            update(
              ($event.target as HTMLInputElement).checked
                ? [...asStringArray(modelValue), option]
                : asStringArray(modelValue).filter((x) => x !== option)
            )
          "
        />
        <span>{{ option }}</span>
      </label>
    </div>

    <textarea
        v-else
        class="field-textarea"
        :value="typeof modelValue === 'string' ? modelValue : JSON.stringify(modelValue ?? {}, null, 2)"
        @input="update(($event.target as HTMLTextAreaElement).value)"
    />
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-weight: 600;
  font-size: 14px;
}

.required {
  color: #dc2626;
}

.field-help {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.field-input,
.field-textarea {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: white;
}

.field-textarea {
  min-height: 120px;
  resize: vertical;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.multiselect-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>