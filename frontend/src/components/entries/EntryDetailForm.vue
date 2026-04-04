<script setup lang="ts">
import type {EntryDetail} from '../../bridge/desktopBridge'

const props = defineProps<{
  modelValue: EntryDetail
}>()

const emit = defineEmits<{
  'update:modelValue': [value: EntryDetail]
}>()

function patch<K extends keyof EntryDetail>(key: K, value: EntryDetail[K]) {
  emit('update:modelValue', {
    ...props.modelValue,
    [key]: value,
  })
}
</script>

<template>
  <div class="entry-form">
    <div class="meta-grid">
      <div class="field">
        <label class="field-label">Order</label>
        <input
            class="field-input"
            type="number"
            :value="modelValue.documentOrder"
            @input="patch('documentOrder', Number(($event.target as HTMLInputElement).value))"
        >
      </div>

      <label class="checkbox-row">
        <input
            type="checkbox"
            :checked="modelValue.approved"
            @change="patch('approved', ($event.target as HTMLInputElement).checked)"
        >
        <span>Approved (skip automatic correction unless forced)</span>
      </label>
    </div>

    <div class="editor-grid">
      <section class="card">
        <h3>Raw entry</h3>

        <div class="field">
          <label class="field-label">Chuj text</label>
          <textarea
              class="field-textarea"
              :value="modelValue.rawChujText"
              @input="patch('rawChujText', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>

        <div class="field">
          <label class="field-label">Gloss</label>
          <textarea
              class="field-textarea"
              :value="modelValue.rawGlossText"
              @input="patch('rawGlossText', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>

        <div class="field">
          <label class="field-label">Translation</label>
          <textarea
              class="field-textarea"
              :value="modelValue.translation"
              @input="patch('translation', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>
      </section>

      <section class="card">
        <h3>Corrected / reviewed</h3>

        <div class="field">
          <label class="field-label">Corrected Chuj text</label>
          <textarea
              class="field-textarea"
              :value="modelValue.correctedChujText"
              @input="patch('correctedChujText', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>

        <div class="field">
          <label class="field-label">Corrected gloss</label>
          <textarea
              class="field-textarea"
              :value="modelValue.correctedGlossText"
              @input="patch('correctedGlossText', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>

        <div class="field">
          <label class="field-label">Corrected translation</label>
          <textarea
              class="field-textarea"
              :value="modelValue.correctedTranslation"
              @input="patch('correctedTranslation', ($event.target as HTMLTextAreaElement).value)"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.entry-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meta-grid {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.editor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  padding: 16px;
}

.card h3 {
  margin-top: 0;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.field:last-child {
  margin-bottom: 0;
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
  width: 100%;
}

.field-textarea {
  min-height: 110px;
  resize: vertical;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 1100px) {
  .editor-grid {
    grid-template-columns: 1fr;
  }
}
</style>