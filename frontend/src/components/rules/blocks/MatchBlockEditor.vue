<script setup lang="ts">
type MatchBlock = Record<string, unknown>

const props = defineProps<{
  modelValue?: MatchBlock
}>()

const emit = defineEmits<{
  'update:modelValue': [value: MatchBlock]
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
    <h4>Match</h4>

    <div class="field">
      <label>token</label>
      <textarea
          class="field-textarea"
          :value="typeof modelValue?.token === 'string' ? modelValue?.token : ''"
          placeholder="Token matcher"
          @input="patch('token', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="field">
      <label>gloss</label>
      <textarea
          class="field-textarea"
          :value="typeof modelValue?.gloss === 'string' ? modelValue?.gloss : JSON.stringify(modelValue?.gloss ?? '', null, 2)"
          placeholder="Gloss matcher or object"
          @input="patch('gloss', ($event.target as HTMLTextAreaElement).value)"
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

.field-textarea {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  min-height: 100px;
  resize: vertical;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: white;
}
</style>