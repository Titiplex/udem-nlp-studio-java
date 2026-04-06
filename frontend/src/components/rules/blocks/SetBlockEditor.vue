<script setup lang="ts">
import {ref, watch} from 'vue'
import {parseSetBlock, type SetEditorDraft, stringifySetBlock,} from '../../../utils/ruleBlockCodec'

type SetBlock = Record<string, unknown>

const props = defineProps<{
  modelValue?: SetBlock
  subtype: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: SetBlock]
}>()

const draft = ref<SetEditorDraft>(parseSetBlock())
const parseError = ref('')

function syncFromProps() {
  try {
    draft.value = parseSetBlock(props.modelValue)
    if (!draft.value.operation && props.subtype) {
      draft.value.operation = props.subtype
    }
    parseError.value = ''
  } catch (error) {
    parseError.value = error instanceof Error ? error.message : 'Invalid set block'
  }
}

function commit() {
  try {
    emit('update:modelValue', stringifySetBlock(draft.value))
    parseError.value = ''
  } catch (error) {
    parseError.value = error instanceof Error ? error.message : 'Invalid set block'
  }
}

function addStaticFeat() {
  draft.value.staticFeats.push({key: '', value: ''})
  commit()
}

function removeStaticFeat(index: number) {
  draft.value.staticFeats.splice(index, 1)
  commit()
}

function addTemplateFeat() {
  draft.value.templateFeats.push({key: '', value: ''})
  commit()
}

function removeTemplateFeat(index: number) {
  draft.value.templateFeats.splice(index, 1)
  commit()
}

function addExtractRow() {
  draft.value.extractRows.push({type: '', extractor: '', into: ''})
  commit()
}

function removeExtractRow(index: number) {
  draft.value.extractRows.splice(index, 1)
  commit()
}

watch(() => props.modelValue, syncFromProps, {immediate: true})
</script>

<template>
  <div class="block-editor">
    <div class="header-row">
      <h4>Set</h4>
      <span class="hint">Subtype: {{ subtype || '—' }}</span>
    </div>

    <p v-if="parseError" class="error-text">{{ parseError }}</p>

    <div class="field">
      <label>operation</label>
      <input
          class="field-input"
          :value="draft.operation"
          @input="draft.operation = ($event.target as HTMLInputElement).value; commit()"
      >
    </div>

    <div v-if="subtype === 'split' || draft.operation === 'split'" class="field">
      <label>position</label>
      <input
          class="field-input"
          :value="draft.position"
          placeholder="end / start / expression"
          @input="draft.position = ($event.target as HTMLInputElement).value; commit()"
      >
    </div>

    <div class="field">
      <label>upos</label>
      <input
          class="field-input"
          :value="draft.upos"
          placeholder="VERB / NOUN / ..."
          @input="draft.upos = ($event.target as HTMLInputElement).value; commit()"
      >
    </div>

    <section class="subblock">
      <div class="subblock-header">
        <strong>Static features</strong>
        <button class="small-btn" @click="addStaticFeat">Add</button>
      </div>

      <div v-if="draft.staticFeats.length > 0" class="row-list">
        <div v-for="(row, index) in draft.staticFeats" :key="`static-${index}`" class="row-grid">
          <input
              class="field-input"
              :value="row.key"
              placeholder="Feature"
              @input="draft.staticFeats[index].key = ($event.target as HTMLInputElement).value; commit()"
          >
          <input
              class="field-input"
              :value="row.value"
              placeholder="Value"
              @input="draft.staticFeats[index].value = ($event.target as HTMLInputElement).value; commit()"
          >
          <button class="small-btn" @click="removeStaticFeat(index)">Remove</button>
        </div>
      </div>
    </section>

    <section class="subblock">
      <div class="subblock-header">
        <strong>Feature templates</strong>
        <button class="small-btn" @click="addTemplateFeat">Add</button>
      </div>

      <div v-if="draft.templateFeats.length > 0" class="row-list">
        <div v-for="(row, index) in draft.templateFeats" :key="`tpl-${index}`" class="row-grid">
          <input
              class="field-input"
              :value="row.key"
              placeholder="Feature"
              @input="draft.templateFeats[index].key = ($event.target as HTMLInputElement).value; commit()"
          >
          <input
              class="field-input"
              :value="row.value"
              placeholder="{template.path}"
              @input="draft.templateFeats[index].value = ($event.target as HTMLInputElement).value; commit()"
          >
          <button class="small-btn" @click="removeTemplateFeat(index)">Remove</button>
        </div>
      </div>
    </section>

    <section class="subblock">
      <div class="subblock-header">
        <strong>Extract</strong>
        <button class="small-btn" @click="addExtractRow">Add</button>
      </div>

      <div v-if="draft.extractRows.length > 0" class="extract-list">
        <div v-for="(row, index) in draft.extractRows" :key="`extract-${index}`" class="extract-card">
          <div class="row-grid single">
            <input
                class="field-input"
                :value="row.type"
                placeholder="type"
                @input="draft.extractRows[index].type = ($event.target as HTMLInputElement).value; commit()"
            >
            <input
                class="field-input"
                :value="row.extractor"
                placeholder="extractor"
                @input="draft.extractRows[index].extractor = ($event.target as HTMLInputElement).value; commit()"
            >
            <input
                class="field-input"
                :value="row.into"
                placeholder="into"
                @input="draft.extractRows[index].into = ($event.target as HTMLInputElement).value; commit()"
            >
            <button class="small-btn" @click="removeExtractRow(index)">Remove</button>
          </div>
        </div>
      </div>
    </section>

    <div class="field">
      <label>advanced JSON</label>
      <textarea
          class="field-textarea"
          :value="draft.extraJsonText"
          placeholder="{}"
          @input="draft.extraJsonText = ($event.target as HTMLTextAreaElement).value; commit()"
      />
      <p class="field-help">
        Pour les clés non encore couvertes par l’éditeur guidé.
      </p>
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

.header-row,
.subblock-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.block-editor h4 {
  margin: 0;
}

.hint {
  color: #6b7280;
  font-size: 13px;
}

.error-text {
  margin: 0;
  color: #b91c1c;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.subblock {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  padding: 12px;
}

.row-list,
.extract-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 10px;
}

.row-grid.single {
  grid-template-columns: 1fr 1fr 1fr auto;
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
  min-height: 110px;
  resize: vertical;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.small-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
}

.field-help {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}
</style>