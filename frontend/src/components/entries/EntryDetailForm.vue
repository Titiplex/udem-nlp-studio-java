<script setup lang="ts">
import {ref} from 'vue'
import type {EntryDetail} from '../../bridge/desktopBridge'

const props = defineProps<{
  modelValue: EntryDetail
}>()

const emit = defineEmits<{
  'update:modelValue': [value: EntryDetail]
}>()

const legacyMode = ref(false)

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
      <div class="field order-field">
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

      <label class="checkbox-row">
        <input
            type="checkbox"
            :checked="legacyMode"
            @change="legacyMode = ($event.target as HTMLInputElement).checked"
        >
        <span>Legacy editing mode</span>
      </label>
    </div>

    <div class="editor-grid">
      <section class="card">
        <h3>Entry input</h3>

        <template v-if="!legacyMode">
          <div class="structured-grid">
            <div class="field field-span-2">
              <label class="field-label">Contexte / PJ (optionnel)</label>
              <textarea
                  class="field-textarea compact-textarea"
                  :value="modelValue.contextText"
                  @input="patch('contextText', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Contexte d’énonciation, source, pièce jointe, note de terrain, etc."
              />
            </div>

            <div class="field field-span-2">
              <label class="field-label">Phrase écrite normalement (optionnelle)</label>
              <textarea
                  class="field-textarea compact-textarea"
                  :value="modelValue.surfaceText"
                  @input="patch('surfaceText', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Phrase sans segmentation morphémique"
              />
            </div>

            <div class="field field-span-2 required-field">
              <label class="field-label">Segmentation morphémique (obligatoire)</label>
              <textarea
                  class="field-textarea"
                  :value="modelValue.rawChujText"
                  @input="patch('rawChujText', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Phrase segmentée avec des tirets entre morphèmes"
              />
              <p class="field-help">
                Ce champ correspond au texte de base utilisé par le pipeline actuel.
              </p>
            </div>

            <div class="field field-span-2">
              <label class="field-label">Gloss segmentée (optionnelle)</label>
              <textarea
                  class="field-textarea compact-textarea"
                  :value="modelValue.rawGlossText"
                  @input="patch('rawGlossText', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Gloss alignée sur la segmentation"
              />
            </div>

            <div class="field field-span-2 required-field">
              <label class="field-label">Traduction (obligatoire)</label>
              <textarea
                  class="field-textarea compact-textarea"
                  :value="modelValue.translation"
                  @input="patch('translation', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Traduction libre ou littérale"
              />
            </div>

            <div class="field field-span-2">
              <label class="field-label">Commentaires (optionnels)</label>
              <textarea
                  class="field-textarea compact-textarea"
                  :value="modelValue.comments"
                  @input="patch('comments', ($event.target as HTMLTextAreaElement).value)"
                  placeholder="Commentaires, doute analytique, variation, remarques diverses"
              />
            </div>
          </div>
        </template>

        <template v-else>
          <div class="field">
            <label class="field-label">Chuj text / segmentation</label>
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

          <div class="field">
            <label class="field-label">Context</label>
            <textarea
                class="field-textarea compact-textarea"
                :value="modelValue.contextText"
                @input="patch('contextText', ($event.target as HTMLTextAreaElement).value)"
            />
          </div>

          <div class="field">
            <label class="field-label">Surface form</label>
            <textarea
                class="field-textarea compact-textarea"
                :value="modelValue.surfaceText"
                @input="patch('surfaceText', ($event.target as HTMLTextAreaElement).value)"
            />
          </div>

          <div class="field">
            <label class="field-label">Comments</label>
            <textarea
                class="field-textarea compact-textarea"
                :value="modelValue.comments"
                @input="patch('comments', ($event.target as HTMLTextAreaElement).value)"
            />
          </div>
        </template>
      </section>

      <section class="card">
        <h3>Corrected / reviewed</h3>

        <div class="field">
          <label class="field-label">Corrected segmentation</label>
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
              class="field-textarea compact-textarea"
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

.order-field {
  min-width: 110px;
}

.editor-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 16px;
}

.structured-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.field-span-2 {
  grid-column: 1 / -1;
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

.field-help {
  margin: 0;
  font-size: 12px;
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
  min-height: 120px;
  resize: vertical;
}

.compact-textarea {
  min-height: 88px;
}

.required-field .field-label::after {
  content: ' *';
  color: #b91c1c;
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

  .structured-grid {
    grid-template-columns: 1fr;
  }

  .field-span-2 {
    grid-column: auto;
  }
}
</style>