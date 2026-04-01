<script setup lang="ts">
defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div class="yaml-panel">
    <textarea
        class="yaml-editor"
        :value="modelValue"
        @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
        placeholder="- name: My rule&#10;  scope: token&#10;  match: ..."
    />
  </div>
</template>

<style scoped>
.yaml-panel {
  display: flex;
  flex-direction: column;
}

.yaml-editor {
  min-height: 460px;
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  padding: 14px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: #f9fafb;
  resize: vertical;
}
</style>