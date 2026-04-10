<script setup lang="ts">
defineProps<{
  title?: string
  message: string
  canReload?: boolean
}>()

defineEmits<{
  reload: []
  dismiss: []
}>()
</script>

<template>
  <div class="conflict-banner">
    <div class="content">
      <strong>{{ title || 'Conflict detected' }}</strong>
      <p>{{ message }}</p>
    </div>

    <div class="actions">
      <button
          v-if="canReload"
          class="action-btn primary"
          @click="$emit('reload')"
      >
        Reload remote version
      </button>

      <button
          class="action-btn"
          @click="$emit('dismiss')"
      >
        Keep local draft
      </button>
    </div>
  </div>
</template>

<style scoped>
.conflict-banner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  color: #7f1d1d;
  border-radius: 14px;
  padding: 14px 16px;
}

.content {
  min-width: 0;
}

.content strong {
  display: block;
  margin-bottom: 4px;
}

.content p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
  padding: 8px 12px;
  color: #111827;
}

.action-btn.primary {
  background: #7f1d1d;
  color: white;
  border-color: #7f1d1d;
}

@media (max-width: 900px) {
  .conflict-banner {
    flex-direction: column;
  }
}
</style>