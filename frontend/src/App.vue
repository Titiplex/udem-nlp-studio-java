<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {type AppInfo, callBridge} from './bridge/desktopBridge'
import RuleWorkbench from './components/rules/RuleWorkbench.vue'

const appName = ref('NLP Studio')
const version = ref('unknown')
const status = ref('Loading...')

onMounted(() => {
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

      <div class="status-pill">
        {{ status }}
      </div>
    </header>

    <main class="main-layout">
      <aside class="sidebar">
        <button class="nav-btn active">Rules</button>
        <button class="nav-btn">Entries</button>
        <button class="nav-btn">Preview</button>
        <button class="nav-btn">Settings</button>
      </aside>

      <section class="content">
        <RuleWorkbench/>
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
</style>