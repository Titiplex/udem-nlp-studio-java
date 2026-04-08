<script setup lang="ts">
import type {EditorTab} from '../../stores/ruleEditorStore'

defineProps<{
  activeTab: EditorTab
}>()

const emit = defineEmits<{
  'update:activeTab': [tab: EditorTab]
}>()

const tabs: EditorTab[] = ['visual', 'yaml', 'preview', 'validation']
</script>

<template>
  <div class="tabs">
    <button
        v-for="tab in tabs"
        :key="tab"
        class="tab-btn"
        :class="{ active: activeTab === tab }"
        @click="emit('update:activeTab', tab)"
    >
      {{ tab }}
    </button>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.tab-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  text-transform: capitalize;
}

.tab-btn.active {
  background: #e0e7ff;
  border-color: #a5b4fc;
}
</style>