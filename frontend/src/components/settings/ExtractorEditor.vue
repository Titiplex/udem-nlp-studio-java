<script setup lang="ts">
import {ref, watch} from 'vue'
import {type ExtractorDraft, parseExtractorsYaml, stringifyExtractorsYaml,} from '../../utils/annotationSettingsCodec'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const extractors = ref<ExtractorDraft[]>([])
const parseError = ref('')

function syncFromProps() {
  try {
    extractors.value = parseExtractorsYaml(props.modelValue)
    parseError.value = ''
  } catch (error) {
    extractors.value = []
    parseError.value = error instanceof Error ? error.message : 'Invalid extractors YAML'
  }
}

function commit() {
  emit('update:modelValue', stringifyExtractorsYaml(extractors.value))
}

function addExtractor() {
  extractors.value.push({
    name: '',
    seriesRows: [
      {key: 'A', label: 'subj'},
    ],
    personsText: '1\n2\n3',
    numberSuffix: 'PL',
    extraRoot: {},
    extraTagSchema: {},
    extraValues: {},
  })
  commit()
}

function removeExtractor(index: number) {
  extractors.value.splice(index, 1)
  commit()
}

function patchExtractor(index: number, patch: Partial<ExtractorDraft>) {
  extractors.value[index] = {
    ...extractors.value[index],
    ...patch,
  }
  commit()
}

function addSeriesRow(index: number) {
  const next = [...extractors.value[index].seriesRows, {key: '', label: ''}]
  patchExtractor(index, {seriesRows: next})
}

function removeSeriesRow(extractorIndex: number, rowIndex: number) {
  const next = [...extractors.value[extractorIndex].seriesRows]
  next.splice(rowIndex, 1)
  patchExtractor(extractorIndex, {seriesRows: next})
}

function patchSeriesRow(extractorIndex: number, rowIndex: number, patch: { key?: string; label?: string }) {
  const next = [...extractors.value[extractorIndex].seriesRows]
  next[rowIndex] = {
    ...next[rowIndex],
    ...patch,
  }
  patchExtractor(extractorIndex, {seriesRows: next})
}

watch(() => props.modelValue, syncFromProps, {immediate: true})
</script>

<template>
  <section class="card">
    <div class="card-header">
      <div>
        <h3>Extractors</h3>
        <p class="help">
          Éditeur guidé pour les extracteurs fondés sur `tag_schema`,
          notamment `series`, `person` et `number.suffix`.
        </p>
      </div>

      <button class="action-btn" @click="addExtractor">Add extractor</button>
    </div>

    <p v-if="parseError" class="error-text">
      {{ parseError }}
    </p>

    <div v-if="extractors.length > 0" class="extractor-list">
      <section v-for="(extractor, index) in extractors" :key="index" class="extractor-card">
        <div class="extractor-card-header">
          <input
              class="field-input"
              type="text"
              placeholder="Extractor name"
              :value="extractor.name"
              @input="patchExtractor(index, { name: ($event.target as HTMLInputElement).value })"
          >
          <button class="delete-btn" @click="removeExtractor(index)">Remove</button>
        </div>

        <div class="subsection">
          <div class="subsection-header">
            <strong>Series</strong>
            <button class="small-btn" @click="addSeriesRow(index)">Add series row</button>
          </div>

          <div v-if="extractor.seriesRows.length > 0" class="series-list">
            <div v-for="(row, rowIndex) in extractor.seriesRows" :key="rowIndex" class="series-row">
              <input
                  class="field-input"
                  type="text"
                  placeholder="Key"
                  :value="row.key"
                  @input="patchSeriesRow(index, rowIndex, { key: ($event.target as HTMLInputElement).value })"
              >
              <input
                  class="field-input"
                  type="text"
                  placeholder="Label"
                  :value="row.label"
                  @input="patchSeriesRow(index, rowIndex, { label: ($event.target as HTMLInputElement).value })"
              >
              <button class="small-btn" @click="removeSeriesRow(index, rowIndex)">Remove</button>
            </div>
          </div>
        </div>

        <div class="subsection">
          <label class="field-label">Persons</label>
          <textarea
              class="field-textarea"
              placeholder="1&#10;2&#10;3"
              :value="extractor.personsText"
              @input="patchExtractor(index, { personsText: ($event.target as HTMLTextAreaElement).value })"
          />
        </div>

        <div class="subsection">
          <label class="field-label">Number suffix</label>
          <input
              class="field-input"
              type="text"
              placeholder="PL"
              :value="extractor.numberSuffix"
              @input="patchExtractor(index, { numberSuffix: ($event.target as HTMLInputElement).value })"
          >
        </div>
      </section>
    </div>

    <p v-else class="empty-state">No extractors yet.</p>
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

.extractor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.extractor-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #f9fafb;
}

.extractor-card-header {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-bottom: 12px;
}

.subsection {
  margin-bottom: 12px;
}

.subsection:last-child {
  margin-bottom: 0;
}

.subsection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.series-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.series-row {
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 10px;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 8px;
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
  min-height: 100px;
  resize: vertical;
}

.action-btn,
.delete-btn,
.small-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
}

.small-btn {
  padding: 8px 10px;
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