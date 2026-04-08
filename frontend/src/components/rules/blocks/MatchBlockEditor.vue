<script setup lang="ts">
import {ref, watch} from 'vue'
import {type MatchEditorDraft, parseMatchBlock, stringifyMatchBlock,} from '../../../utils/ruleBlockCodec'

type MatchBlock = Record<string, unknown>

const props = defineProps<{
  modelValue?: MatchBlock
}>()

const emit = defineEmits<{
  'update:modelValue': [value: MatchBlock]
}>()

const draft = ref<MatchEditorDraft>(parseMatchBlock())
const parseError = ref('')

function syncFromProps() {
  try {
    draft.value = parseMatchBlock(props.modelValue)
    parseError.value = ''
  } catch (error) {
    parseError.value = error instanceof Error ? error.message : 'Invalid match block'
  }
}

function commit() {
  try {
    emit('update:modelValue', stringifyMatchBlock(draft.value))
    parseError.value = ''
  } catch (error) {
    parseError.value = error instanceof Error ? error.message : 'Invalid match block'
  }
}

watch(() => props.modelValue, syncFromProps, {immediate: true})
</script>

<template>
  <div class="block-editor">
    <div class="header-row">
      <h4>Match</h4>
      <span class="hint">Guided editor</span>
    </div>

    <p v-if="parseError" class="error-text">{{ parseError }}</p>

    <div class="field">
      <label>token</label>
      <input
          class="field-input"
          :value="draft.tokenText"
          placeholder="Surface token matcher"
          @input="draft.tokenText = ($event.target as HTMLInputElement).value; commit()"
      >
    </div>

    <div class="field">
      <label>gloss mode</label>
      <select
          class="field-input"
          :value="draft.glossMode"
          @change="draft.glossMode = ($event.target as HTMLSelectElement).value as 'text' | 'object'; commit()"
      >
        <option value="text">Text</option>
        <option value="object">Structured object</option>
      </select>
    </div>

    <div v-if="draft.glossMode === 'text'" class="field">
      <label>gloss</label>
      <input
          class="field-input"
          :value="draft.glossText"
          placeholder="Gloss matcher"
          @input="draft.glossText = ($event.target as HTMLInputElement).value; commit()"
      >
    </div>

    <div v-else class="field">
      <label>gloss object</label>
      <textarea
          class="field-textarea"
          :value="draft.glossObjectText"
          placeholder='{"in_lexicon":"spanish_verbs"}'
          @input="draft.glossObjectText = ($event.target as HTMLTextAreaElement).value; commit()"
      />
    </div>

    <div class="field">
      <label>require</label>
      <textarea
          class="field-textarea small"
          :value="draft.requireText"
          placeholder="agreement_verbs.A.person"
          @input="draft.requireText = ($event.target as HTMLTextAreaElement).value; commit()"
      />
    </div>

    <div class="field">
      <label>forbid</label>
      <textarea
          class="field-textarea small"
          :value="draft.forbidText"
          placeholder="agreement_verbs.B.person"
          @input="draft.forbidText = ($event.target as HTMLTextAreaElement).value; commit()"
      />
    </div>

    <div class="field">
      <label>advanced JSON</label>
      <textarea
          class="field-textarea"
          :value="draft.extraJsonText"
          placeholder="{}"
          @input="draft.extraJsonText = ($event.target as HTMLTextAreaElement).value; commit()"
      />
      <p class="field-help">
        Pour les cas non couverts par l’éditeur guidé.
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

.header-row {
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

.field-textarea.small {
  min-height: 80px;
}

.field-help {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}
</style>