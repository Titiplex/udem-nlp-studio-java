<script setup lang="ts">
import type {ValidationIssue} from '../../bridge/desktopBridge'

defineProps<{
  issues: ValidationIssue[]
}>()
</script>

<template>
  <div class="validation-panel">
    <p v-if="issues.length === 0" class="ok-state">No validation issues.</p>

    <ul v-else class="issues-list">
      <li
          v-for="issue in issues"
          :key="`${issue.path}-${issue.level}-${issue.message}`"
          class="issue-item"
          :class="issue.level"
      >
        <span class="issue-badge">{{ issue.level.toUpperCase() }}</span>
        <div class="issue-body">
          <strong>{{ issue.path }}</strong>
          <p>{{ issue.message }}</p>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.validation-panel {
  border: 1px solid #d1d5db;
  border-radius: 14px;
  background: white;
  padding: 16px;
}

.ok-state {
  margin: 0;
  color: #166534;
}

.issues-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.issue-item {
  display: flex;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
}

.issue-item.error {
  border-color: #fecaca;
  background: #fef2f2;
}

.issue-item.warning {
  border-color: #fde68a;
  background: #fffbeb;
}

.issue-badge {
  font-size: 11px;
  font-weight: 700;
  border-radius: 999px;
  padding: 6px 8px;
  align-self: flex-start;
  border: 1px solid #d1d5db;
  background: white;
}

.issue-body p {
  margin: 4px 0 0;
}
</style>