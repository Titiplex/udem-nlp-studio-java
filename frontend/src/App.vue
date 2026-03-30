<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {type AppInfo, callBridge, type RuleSummary,} from './bridge/desktopBridge'
import RuleBuilderPanel from './components/RuleBuilderPanel.vue'

const appName = ref('NLP Studio')
const version = ref('unknown')
const status = ref('Loading...')

const rules = ref<RuleSummary[]>([])

const selectedRuleId = ref<string | null>(null)

const selectedRule = computed(() =>
    rules.value.find((rule) => rule.id === selectedRuleId.value) ?? null
)

onMounted(() => {
  const pingResp = callBridge<string>('ping')
  const infoResp = callBridge<AppInfo>('getAppInfo')
  const rulesResp = callBridge<RuleSummary[]>('listRules')

  status.value = pingResp.data ?? pingResp.message ?? 'No response'
  appName.value = infoResp.data?.name ?? 'NLP Studio'
  version.value = infoResp.data?.version ?? 'unknown'

  rules.value = rulesResp.data ?? []

  if (rules.value.length > 0) {
    selectedRuleId.value = rules.value[0].id
  }
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div>
        <h1>{{ appName }}</h1>
        <p>Version {{ version }}</p>
      </div>

      <div class="status-pill">
        {{ status }}
      </div>
    </header>

    <main class="main-layout">
      <aside class="sidebar">
        <button class="nav-btn active">Dashboard</button>
        <button class="nav-btn">Entries</button>
        <button class="nav-btn">Rules</button>
        <button class="nav-btn">Preview</button>
        <button class="nav-btn">Settings</button>
      </aside>

      <section class="content">
        <div class="workspace-grid">
          <section class="panel">
            <h2>Rule builder</h2>
            <RuleBuilderPanel/>
          </section>

          <section class="panel">
            <h2>Selected rule</h2>
            <template v-if="selectedRule">
              <pre>{{ JSON.stringify(selectedRule, null, 2) }}</pre>
            </template>
            <p v-else>No rule selected.</p>
          </section>
        </div>
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
}

.topbar h1 {
  margin: 0;
  font-size: 20px;
}

.topbar p {
  margin: 4px 0 0;
  color: #6b7280;
}

.status-pill {
  padding: 8px 12px;
  border-radius: 999px;
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  font-size: 14px;
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

.workspace-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}

.panel {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 16px;
  min-height: 260px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.panel h2 {
  margin-top: 0;
}

pre {
  margin: 0;
  padding: 12px;
  border-radius: 12px;
  background: #f9fafb;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}
</style>