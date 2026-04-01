<script setup lang="ts">
import type {RuleBuilderSchema, RuleDetail} from '../../bridge/desktopBridge'
import DynamicFieldRenderer from '../DynamicFieldRenderer.vue'

const props = defineProps<{
  schema: RuleBuilderSchema | null
  modelValue: RuleDetail
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RuleDetail]
}>()

function patchField(key: string, value: unknown) {
  const nextPayload = {
    ...props.modelValue.payload,
    [key]: value,
  }

  emit('update:modelValue', {
    ...props.modelValue,
    payload: nextPayload,
  })
}
</script>

<template>
  <div class="visual-builder">
    <p v-if="!schema" class="empty-state">No schema selected.</p>

    <template v-else>
      <div class="schema-meta">
        <h3>{{ schema.label }}</h3>
        <p>{{ schema.description }}</p>
      </div>

      <DynamicFieldRenderer
          v-for="field in schema.fields"
          :key="field.key"
          :field="field"
          :model-value="modelValue.payload[field.key]"
          @update:model-value="(value) => patchField(field.key, value)"
      />
    </template>
  </div>
</template>

<style scoped>
.visual-builder {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.schema-meta h3 {
  margin: 0 0 6px;
}

.schema-meta p {
  margin: 0;
  color: #6b7280;
}

.empty-state {
  color: #6b7280;
}
</style>