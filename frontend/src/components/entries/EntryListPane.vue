<script setup lang="ts">
import type {EntrySummary} from '../../bridge/desktopBridge'

defineProps<{
  entries: EntrySummary[]
  selectedEntryId: string | null
}>()

defineEmits<{
  select: [id: string]
  create: []
}>()

function preview(value: string, max = 48) {
  const normalized = (value ?? '').trim()
  if (!normalized) return '—'
  return normalized.length > max ? normalized.slice(0, max) + '…' : normalized
}
</script>

<template>
  <aside class="list-pane">
    <div class="list-pane-header">
      <div>
        <h3>Entries</h3>
        <p>{{ entries.length }} entrées</p>
      </div>

      <button class="add-btn" @click="$emit('create')">New</button>
    </div>

    <div class="entry-list">
      <button
          v-for="entry in entries"
          :key="entry.id"
          class="entry-card"
          :class="{ active: entry.id === selectedEntryId }"
          @click="$emit('select', entry.id)"
      >
        <div class="entry-card-top">
          <strong>#{{ entry.documentOrder }}</strong>
          <span v-if="entry.approved" class="badge success">Approved</span>
          <span v-else-if="entry.hasCorrection" class="badge">Corrected</span>
        </div>

        <p class="entry-main">{{ preview(entry.rawChujText) }}</p>
        <p class="entry-sub">{{ preview(entry.rawGlossText) }}</p>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.list-pane {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list-pane-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.list-pane-header h3 {
  margin: 0;
}

.list-pane-header p {
  margin: 4px 0 0;
  color: #6b7280;
}

.add-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 9px 12px;
}

.entry-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 720px;
  overflow: auto;
}

.entry-card {
  text-align: left;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  border-radius: 12px;
  padding: 12px;
}

.entry-card.active {
  border-color: #a5b4fc;
  background: #eef2ff;
}

.entry-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.entry-main,
.entry-sub {
  margin: 0;
  line-height: 1.4;
}

.entry-sub {
  color: #6b7280;
  margin-top: 4px;
}

.badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 8px;
  background: #e5e7eb;
  font-size: 12px;
}

.badge.success {
  background: #dcfce7;
}
</style>