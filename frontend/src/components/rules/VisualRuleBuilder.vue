<script setup lang="ts">
import type {FieldDescriptor, RuleBuilderSchema, RuleDetail} from '../../bridge/desktopBridge'
import DynamicFieldRenderer from '../DynamicFieldRenderer.vue'
import KeyValueEditor from './blocks/KeyValueEditor.vue'
import StringListEditor from './blocks/StringListEditor.vue'
import ExtractorListEditor from './blocks/ExtractorListEditor.vue'
import MatchBlockEditor from './blocks/MatchBlockEditor.vue'
import SetBlockEditor from './blocks/SetBlockEditor.vue'

const props = defineProps<{
  schema: RuleBuilderSchema | null
  modelValue: RuleDetail
}>()

const emit = defineEmits<{
  'update:modelValue': [value: RuleDetail]
}>()

function patchField(key: string, value: unknown) {
  emit('update:modelValue', {
    ...props.modelValue,
    payload: {
      ...props.modelValue.payload,
      [key]: value,
    },
  })
}

function supportsSpecialEditor(field: FieldDescriptor): boolean {
  return ['match', 'set', 'feats', 'featsTemplate', 'extract', 'before', 'after'].includes(field.key)
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

      <div v-for="field in schema.fields" :key="field.key" class="builder-section">
        <MatchBlockEditor
            v-if="field.key === 'match'"
            :model-value="modelValue.payload[field.key] as Record<string, unknown> | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <SetBlockEditor
            v-else-if="field.key === 'set'"
            :model-value="modelValue.payload[field.key] as Record<string, unknown> | undefined"
            :subtype="modelValue.subtype"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <KeyValueEditor
            v-else-if="field.key === 'feats'"
            title="Static features"
            value-placeholder="Feature value"
            :model-value="modelValue.payload[field.key] as Record<string, unknown> | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <KeyValueEditor
            v-else-if="field.key === 'featsTemplate'"
            title="Feature templates"
            value-placeholder="{template.path}"
            :model-value="modelValue.payload[field.key] as Record<string, unknown> | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <ExtractorListEditor
            v-else-if="field.key === 'extract'"
            :model-value="modelValue.payload[field.key] as Array<Record<string, unknown>> | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <StringListEditor
            v-else-if="field.key === 'before'"
            title="Before"
            placeholder="Value to replace"
            :model-value="modelValue.payload[field.key] as string[] | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <StringListEditor
            v-else-if="field.key === 'after'"
            title="After"
            placeholder="Replacement value"
            :model-value="modelValue.payload[field.key] as string[] | undefined"
            @update:model-value="(value) => patchField(field.key, value)"
        />

        <div v-else class="fallback-section">
          <DynamicFieldRenderer
              :field="field"
              :model-value="modelValue.payload[field.key]"
              @update:model-value="(value) => patchField(field.key, value)"
          />
          <p v-if="supportsSpecialEditor(field) === false" class="fallback-note">
            Standard editor.
          </p>
        </div>
      </div>
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

.builder-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fallback-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fallback-note,
.empty-state {
  color: #6b7280;
}
</style>