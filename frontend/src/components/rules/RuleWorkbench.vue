<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {callBridge, type RuleBuilderSchema, type RuleDescriptor, waitForBridge,} from '../../bridge/desktopBridge'
import {useRuleEditorStore} from '../../stores/ruleEditorStore'
import RuleListPane from './RuleListPane.vue'
import RuleEditorTabs from './RuleEditorTabs.vue'
import RuleMetadataForm from './RuleMetadataForm.vue'
import VisualRuleBuilder from './VisualRuleBuilder.vue'
import YamlRuleEditor from './YamlRuleEditor.vue'
import RulePreviewPane from './RulePreviewPane.vue'
import ValidationPanel from './ValidationPanel.vue'
import ConflictBanner from '../common/ConflictBanner.vue'
import ConflictDiffCard from '../common/ConflictDiffCard.vue'

const store = useRuleEditorStore()

const descriptors = ref<RuleDescriptor[]>([])
const schema = ref<RuleBuilderSchema | null>(null)

const schemaKey = computed(() => {
  if (!store.draft.kind || !store.draft.subtype) return ''
  return `${store.draft.kind}::${store.draft.subtype}`
})

const canRunYamlActions = computed(() => {
  return !!store.draft.kind && !!store.draft.subtype
})

const draftMeta = computed(() => store.draftMetaLine)

function loadSchema(kind: string, subtype: string) {
  const resp = callBridge<RuleBuilderSchema>('getRuleSchema', kind, subtype)
  schema.value = resp.data ?? null
}

watch(schemaKey, (value) => {
  if (!value) {
    schema.value = null
    return
  }
  const [kind, subtype] = value.split('::')
  loadSchema(kind, subtype)
}, {immediate: true})

onMounted(async () => {
  const ready = await waitForBridge()

  if (!ready) {
    store.statusMessage = 'Desktop bridge unavailable.'
    return
  }

  const descriptorsResp = callBridge<RuleDescriptor[]>('listRuleDescriptors')
  descriptors.value = descriptorsResp.data ?? []

  await store.refreshRules()

  if (store.selectedRuleId) {
    await store.loadRule(store.selectedRuleId)
  } else if (descriptors.value.length > 0) {
    store.createNewRule(descriptors.value[0].kind, descriptors.value[0].subtype)
  }
})
</script>

<template>
  <div class="workbench">
    <RuleListPane :descriptors="descriptors"/>

    <section class="editor-shell">
      <div class="editor-topbar">
        <div>
          <h2>{{ store.draft.name || 'Rule editor' }}</h2>
          <p class="editor-subtitle">
            {{ store.draft.kind || '—' }} / {{ store.draft.subtype || '—' }}
            <span v-if="store.dirty">• unsaved</span>
          </p>
          <p v-if="draftMeta" class="editor-meta">{{ draftMeta }}</p>
        </div>

        <div class="editor-actions">
          <button class="action-btn" :disabled="!canRunYamlActions" @click="store.generateYaml()">Generate YAML</button>
          <button class="action-btn" :disabled="!canRunYamlActions" @click="store.parseYaml()">Parse YAML</button>
          <button class="action-btn" :disabled="!canRunYamlActions" @click="store.validateDraft()">Validate</button>
          <button class="action-btn primary" :disabled="!canRunYamlActions" @click="store.saveDraft()">Save</button>
        </div>
      </div>

      <ConflictBanner
          v-if="store.hasConflict"
          title="Rule save conflict"
          :message="store.conflictMessage"
          :can-reload="!!store.selectedRuleId"
          @reload="store.reloadRemoteVersion()"
          @dismiss="store.clearConflict()"
      />

      <ConflictDiffCard
          v-if="store.hasConflict && store.hasRemoteConflictDraft"
          title="Rule YAML / payload: local vs remote"
          local-label="Local draft"
          remote-label="Remote version"
          :local-text="store.localConflictYaml"
          :remote-text="store.remoteConflictYaml"
      />

      <p v-if="store.statusMessage && !store.hasConflict" class="status-line">{{ store.statusMessage }}</p>

      <RuleMetadataForm
          :model-value="store.draft"
          @update:model-value="store.setDraft($event)"
      />

      <RuleEditorTabs
          :active-tab="store.activeTab"
          @update:active-tab="store.activeTab = $event"
      />

      <div class="editor-body">
        <VisualRuleBuilder
            v-if="store.activeTab === 'visual'"
            :schema="schema"
            :model-value="store.draft"
            @update:model-value="store.setDraft($event)"
        />

        <YamlRuleEditor
            v-else-if="store.activeTab === 'yaml'"
            :model-value="store.draft.rawYaml"
            @update:model-value="store.patchDraft({ rawYaml: $event })"
        />

        <RulePreviewPane
            v-else-if="store.activeTab === 'preview'"
            :rule="store.draft"
        />

        <ValidationPanel
            v-else-if="store.activeTab === 'validation'"
            :issues="store.issues"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 20px;
  min-height: 700px;
}

.editor-shell {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.editor-topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.editor-topbar h2 {
  margin: 0;
}

.editor-subtitle {
  margin: 4px 0 0;
  color: #6b7280;
}

.editor-meta {
  margin: 4px 0 0;
  color: #9ca3af;
  font-size: 13px;
}

.editor-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
}

.action-btn.primary {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.status-line {
  margin: 0;
  color: #374151;
}

.editor-body {
  min-height: 420px;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>