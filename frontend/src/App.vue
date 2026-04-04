<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {type AppInfo, callBridge, waitForBridge} from './bridge/desktopBridge'
import RuleWorkbench from './components/rules/RuleWorkbench.vue'
import EntriesWorkbench from './components/entries/EntriesWorkbench.vue'
import PreviewWorkbench from './components/preview/PreviewWorkbench.vue'
import SettingsWorkbench from './components/settings/SettingsWorkbench.vue'
import {useWorkspaceStore, type WorkspaceSection} from './stores/workspaceStore'

const workspace = useWorkspaceStore()

const appName = ref('NLP Studio')
const version = ref('unknown')
const status = ref('Loading...')

const navItems: Array<{ key: WorkspaceSection; label: string }> = [
  {key: 'rules', label: 'Rules'},
  {key: 'entries', label: 'Entries'},
  {key: 'preview', label: 'Preview'},
  {key: 'settings', label: 'Settings'},
]

const currentSectionLabel = computed(() => {
  return navItems.find((item) => item.key === workspace.currentSection)?.label ?? 'Workspace'
})

onMounted(async () => {
  const ready = await waitForBridge()

  if (!ready) {
    status.value = 'Desktop bridge unavailable'
    return
  }

  const pingResp = callBridge<string>('ping')
  const infoResp = callBridge<AppInfo>('getAppInfo')

  status.value = pingResp.data ?? pingResp.message ?? 'No response'
  appName.value = infoResp.data?.name ?? 'NLP Studio'
  version.value = infoResp.data?.version ?? 'unknown'
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div>
        <h1>{{ appName }}</h1>
        <p>Version {{ version }}</p>
      </div>

      <div class="topbar-right">
        <div class="section-pill">{{ currentSectionLabel }}</div>
        <div class="status-pill">
          {{ status }}
        </div>
      </div>
    </header>

    <main class="main-layout">
      <aside class="sidebar">
        <button
            v-for="item in navItems"
            :key="item.key"
            class="nav-btn"
            :class="{ active: workspace.currentSection === item.key }"
            @click="workspace.openSection(item.key)"
        >
          {{ item.label }}
        </button>
      </aside>

      <section class="content">
        <RuleWorkbench v-if="workspace.currentSection === 'rules'"/>
        <EntriesWorkbench v-else-if="workspace.currentSection === 'entries'"/>
        <PreviewWorkbench v-else-if="workspace.currentSection === 'preview'"/>
        <SettingsWorkbench v-else/>
      </section>
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
  color: #111827;
}

.topbar {
  padding: 16px 20px;
  border-bottom: 1px solid #d1d5db;
  background: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.topbar h1 {
  margin: 0;
  font-size: 20px;
}

.topbar p {
  margin: 4px 0 0;
  color: #6b7280;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-pill,
.status-pill {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 14px;
}

.section-pill {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
}

.status-pill {
  background: #eef2ff;
  border: 1px solid #c7d2fe;
}

.main-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  min-height: calc(100vh - 81px);
}

.sidebar {
  border-right: 1px solid #d1d5db;
  background: #f9fafb;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-btn {
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 10px;
}

.nav-btn.active {
  background: #e0e7ff;
  border-color: #a5b4fc;
}

.content {
  padding: 24px;
}
</style>