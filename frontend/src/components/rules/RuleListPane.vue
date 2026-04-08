<script setup lang="ts">
import {computed} from 'vue'
import type {RuleDescriptor} from '../../bridge/desktopBridge'
import {useRuleEditorStore} from '../../stores/ruleEditorStore'

const props = defineProps<{
  descriptors: RuleDescriptor[]
}>()

const store = useRuleEditorStore()

const groupedDescriptors = computed(() => {
  return props.descriptors.reduce<Record<string, RuleDescriptor[]>>((acc, item) => {
    const key = item.kind
    if (!acc[key]) acc[key] = []
    acc[key].push(item)
    return acc
  }, {})
})
</script>

<template>
  <aside class="rule-list-pane">
    <div class="pane-header">
      <h3>Rules</h3>
      <button class="refresh-btn" @click="store.refreshRules()">Refresh</button>
    </div>

    <div class="new-rule-panel">
      <h4>New rule</h4>
      <div
          v-for="(items, kind) in groupedDescriptors"
          :key="kind"
          class="descriptor-group"
      >
        <p class="group-title">{{ kind }}</p>
        <button
            v-for="descriptor in items"
            :key="`${descriptor.kind}-${descriptor.subtype}`"
            class="new-rule-btn"
            @click="store.createNewRule(descriptor.kind, descriptor.subtype)"
        >
          {{ descriptor.label }}
        </button>
      </div>
    </div>

    <div class="rule-list">
      <button
          v-for="rule in store.rules"
          :key="rule.id"
          class="rule-card"
          :class="{ active: store.selectedRuleId === rule.id }"
          @click="store.loadRule(rule.id)"
      >
        <div class="rule-card-head">
          <strong>{{ rule.name || '(untitled)' }}</strong>
          <span class="kind-badge">{{ rule.kind }}</span>
        </div>
        <div class="rule-card-meta">
          <span>{{ rule.subtype }}</span>
          <span>{{ rule.scope }}</span>
          <span>#{{ rule.priority }}</span>
        </div>
      </button>

      <p v-if="store.rules.length === 0" class="empty-text">
        No stored rules.
      </p>
    </div>
  </aside>
</template>

<style scoped>
.rule-list-pane {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pane-header h3 {
  margin: 0;
}

.refresh-btn,
.new-rule-btn,
.rule-card {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
}

.new-rule-panel {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  background: #f9fafb;
}

.new-rule-panel h4 {
  margin-top: 0;
  margin-bottom: 12px;
}

.descriptor-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 10px;
}

.group-title {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  text-transform: uppercase;
  font-weight: 700;
}

.rule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rule-card {
  text-align: left;
}

.rule-card.active {
  background: #eef2ff;
  border-color: #a5b4fc;
}

.rule-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.rule-card-meta {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
  flex-wrap: wrap;
}

.kind-badge {
  font-size: 11px;
  border-radius: 999px;
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
}

.empty-text {
  color: #6b7280;
}
</style>