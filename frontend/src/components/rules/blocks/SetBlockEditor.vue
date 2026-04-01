<script setup lang="ts">
type SetBlock = Record<string, unknown>

const props = defineProps<{
  modelValue: SetBlock | undefined
  subtype: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: SetBlock]
}>()

function patch(key: string, value: unknown) {
  emit('update:modelValue', {
    ...(props.modelValue ?? {}),
    [key]: value,
  })
}
</script>

<template>
  <div class="block-editor">
    <h4>Set</h4>

    <div class="field">
      <label>operation</label>
      <input
          class="field-input"
          :value="typeof modelValue?.operation === 'string' ? modelValue?.operation : subtype"
          @input="patch('operation', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div v-if="subtype === 'split'" class="field">
      <label>position</label>
      <input
          class="field-input"
          :value="typeof modelValue?.position === 'string' ? modelValue?.position : ''"
          placeholder="end / start / expression"
          @input="patch('position', ($event.target as HTMLInputElement).value)"
      />
    </div>
  </div>
</template>

<style scoped>
.block-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
  background: #fafafa;
}

.block-editor h4 {
  margin: 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  background: white;
  font: inherit;
}
</style>