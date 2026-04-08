<script setup lang="ts">
import {ref, watch} from 'vue'
import {type LexiconDraft, parseLexiconsYaml, stringifyLexiconsYaml,} from '../../utils/annotationSettingsCodec'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const lexicons = ref<LexiconDraft[]>([])
const parseError = ref('')

function syncFromProps() {
  try {
    lexicons.value = parseLexiconsYaml(props.modelValue)
    parseError.value = ''
  } catch (error) {
    lexicons.value = []
    parseError.value = error instanceof Error ? error.message : 'Invalid lexicons YAML'
  }
}

function commit() {
  emit('update:modelValue', stringifyLexiconsYaml(lexicons.value))
}

function addLexicon() {
  lexicons.value.push({
    name: '',
    valuesText: '',
  })
  commit()
}

function removeLexicon(index: number) {
  lexicons.value.splice(index, 1)
  commit()
}

function patchLexicon(index: number, patch: Partial<LexiconDraft>) {
  lexicons.value[index] = {
    ...lexicons.value[index],
    ...patch,
  }
  commit()
}

watch(() => props.modelValue, syncFromProps, {immediate: true})
</script>

<template>
  <section class="card">
    <div class="card-header">
      <div>
        <h3>Lexicons</h3>
        <p class="help">
          Ajoute ici les lexiques globaux. Un lexique = un nom + une liste de valeurs,
          une par ligne.
        </p>
      </div>

      <button class="action-btn" @click="addLexicon">Add lexicon</button>
    </div>

    <p v-if="parseError" class="error-text">
      {{ parseError }}
    </p>

    <div v-if="lexicons.length > 0" class="lexicon-list">
      <section v-for="(lexicon, index) in lexicons" :key="index" class="lexicon-card">
        <div class="lexicon-card-header">
          <input
              class="field-input"
              type="text"
              placeholder="Lexicon name"
              :value="lexicon.name"
              @input="patchLexicon(index, { name: ($event.target as HTMLInputElement).value })"
          >
          <button class="delete-btn" @click="removeLexicon(index)">Remove</button>
        </div>

        <textarea
            class="field-textarea"
            placeholder="ganar&#10;ir&#10;comer"
            :value="lexicon.valuesText"
            @input="patchLexicon(index, { valuesText: ($event.target as HTMLTextAreaElement).value })"
        />
      </section>
    </div>

    <p v-else class="empty-state">No lexicons yet.</p>
  </section>
</template>

<style scoped>
.card {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.card h3 {
  margin: 0;
}

.help {
  margin: 4px 0 0;
  color: #6b7280;
}

.lexicon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lexicon-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #f9fafb;
}

.lexicon-card-header {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-bottom: 10px;
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
  min-height: 120px;
  resize: vertical;
}

.action-btn,
.delete-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
}

.error-text {
  color: #b91c1c;
  margin: 0 0 12px;
}

.empty-state {
  color: #6b7280;
  margin: 0;
}
</style>