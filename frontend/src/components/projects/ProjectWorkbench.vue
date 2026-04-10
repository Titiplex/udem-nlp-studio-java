<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {useProjectStore} from '../../stores/projectStore'
import type {CreateProjectRequest, SaveSecretsRequest} from '../../bridge/desktopBridge'

const projectStore = useProjectStore()

const registerForm = reactive({
  manifestPath: '',
})

const createForm = reactive<CreateProjectRequest>({
  name: '',
  directory: '',
  sourceId: 'main',
  host: '',
  port: 5432,
  database: 'postgres',
  schema: '',
  ssl: true,
  username: '',
  password: '',
})

const secretForm = reactive<SaveSecretsRequest>({
  projectId: '',
  usernameRef: '',
  username: '',
  passwordRef: '',
  password: '',
})

const registerMessage = ref('')
const createMessage = ref('')
const secretMessage = ref('')
const provisioningMessage = ref('')

const activeSource = computed(() => projectStore.activeProject?.sources?.[0] ?? null)
const activeProjectId = computed(() => projectStore.activeProject?.projectId ?? '')

function resetMessages() {
  registerMessage.value = ''
  createMessage.value = ''
  secretMessage.value = ''
  provisioningMessage.value = ''
}

async function submitRegister() {
  resetMessages()
  const ok = await projectStore.registerProject({manifestPath: registerForm.manifestPath})
  registerMessage.value = ok ? 'Project registered.' : projectStore.error
}

async function submitCreate() {
  resetMessages()
  const ok = await projectStore.createProject({...createForm})
  createMessage.value = ok ? 'Project created.' : projectStore.error

  if (ok && activeSource.value) {
    secretForm.projectId = activeProjectId.value
    secretForm.usernameRef = activeSource.value.id ? `secret://${activeSource.value.id}/username` : ''
    secretForm.passwordRef = activeSource.value.id ? `secret://${activeSource.value.id}/password` : ''
    secretForm.username = createForm.username
    secretForm.password = createForm.password
  }
}

async function submitSecrets() {
  resetMessages()
  secretForm.projectId = activeProjectId.value
  if (activeSource.value) {
    secretForm.usernameRef = `secret://${activeSource.value.id}/username`
    secretForm.passwordRef = `secret://${activeSource.value.id}/password`
  }

  const ok = await projectStore.saveSecrets({...secretForm})
  secretMessage.value = ok ? 'Secrets saved.' : projectStore.error
}

async function testConnection() {
  resetMessages()
  const ok = await projectStore.testConnection()
  provisioningMessage.value = ok
      ? projectStore.connectionStatus?.message ?? 'Connection OK'
      : projectStore.connectionStatus?.message ?? projectStore.error
}

async function initializeSchema() {
  resetMessages()
  const ok = await projectStore.initializeSchema()
  provisioningMessage.value = ok ? 'Schema initialized.' : projectStore.error
}

async function switchProject(projectId: string) {
  resetMessages()
  await projectStore.switchProject(projectId)
}

onMounted(async () => {
  await projectStore.refreshAll()

  if (activeSource.value) {
    secretForm.projectId = activeProjectId.value
    secretForm.usernameRef = `secret://${activeSource.value.id}/username`
    secretForm.passwordRef = `secret://${activeSource.value.id}/password`
  }
})
</script>

<template>
  <div class="project-workbench">
    <section class="panel project-list-panel">
      <div class="panel-header">
        <h2>Projects</h2>
        <p>Known local projects and current active project.</p>
      </div>

      <div v-if="projectStore.projects.length === 0" class="empty-state">
        No project registered yet.
      </div>

      <div v-else class="project-list">
        <button
            v-for="project in projectStore.projects"
            :key="project.projectId"
            class="project-card"
            :class="{ active: project.active }"
            @click="switchProject(project.projectId)"
        >
          <div class="project-card-top">
            <strong>{{ project.name }}</strong>
            <span class="badge" v-if="project.active">Active</span>
          </div>
          <div class="project-meta">{{ project.sourceKind }}</div>
          <div class="project-meta">{{ project.sourceLabel }}</div>
          <div class="project-meta small">{{ project.lastOpenedAt }}</div>
        </button>
      </div>
    </section>

    <section class="panel active-project-panel">
      <div class="panel-header">
        <h2>Active project</h2>
        <p v-if="projectStore.activeProject">
          {{ projectStore.activeProject.name }}
        </p>
        <p v-else>No active project selected.</p>
      </div>

      <template v-if="projectStore.activeProject">
        <div class="info-grid">
          <div class="info-card">
            <h3>Source</h3>
            <p>{{ projectStore.activeSourceLabel }}</p>
            <p>Secrets configured: <strong>{{ projectStore.activeSourceConfigured ? 'yes' : 'no' }}</strong></p>
          </div>

          <div class="info-card">
            <h3>Members</h3>
            <ul class="member-list">
              <li v-for="member in projectStore.activeProject.members" :key="member.principalId">
                <strong>{{ member.displayName || member.principalId }}</strong>
                <span>— {{ member.role }}</span>
              </li>
            </ul>
          </div>
        </div>

        <div class="actions">
          <button class="action-btn" @click="testConnection">Test connection</button>
          <button class="action-btn primary" @click="initializeSchema">Initialize schema</button>
        </div>

        <p
            v-if="projectStore.connectionStatus"
            class="status-line"
            :class="{ ok: projectStore.connectionStatus.success, bad: !projectStore.connectionStatus.success }"
        >
          {{ projectStore.connectionStatus.message }}
        </p>

        <p v-if="provisioningMessage" class="status-line">{{ provisioningMessage }}</p>

        <div class="form-block" v-if="activeSource">
          <h3>Update secrets</h3>
          <div class="form-grid">
            <label>
              Username
              <input v-model="secretForm.username" type="text">
            </label>
            <label>
              Password
              <input v-model="secretForm.password" type="password">
            </label>
          </div>
          <button class="action-btn" @click="submitSecrets">Save secrets</button>
          <p v-if="secretMessage" class="status-line">{{ secretMessage }}</p>
        </div>
      </template>
    </section>

    <section class="panel create-project-panel">
      <div class="panel-header">
        <h2>Register an existing project</h2>
      </div>

      <div class="form-grid one-column">
        <label>
          Manifest path
          <input v-model="registerForm.manifestPath" type="text" placeholder="C:\projects\my-project\project.yaml">
        </label>
      </div>

      <button class="action-btn" @click="submitRegister">Register project</button>
      <p v-if="registerMessage" class="status-line">{{ registerMessage }}</p>
    </section>

    <section class="panel create-project-panel">
      <div class="panel-header">
        <h2>Create a new project</h2>
      </div>

      <div class="form-grid">
        <label>
          Project name
          <input v-model="createForm.name" type="text">
        </label>

        <label>
          Project directory
          <input v-model="createForm.directory" type="text" placeholder="C:\projects\corpus-a">
        </label>

        <label>
          Source id
          <input v-model="createForm.sourceId" type="text">
        </label>

        <label>
          Host
          <input v-model="createForm.host" type="text" placeholder="db.xxx.supabase.co">
        </label>

        <label>
          Port
          <input v-model.number="createForm.port" type="number">
        </label>

        <label>
          Database
          <input v-model="createForm.database" type="text">
        </label>

        <label>
          Schema
          <input v-model="createForm.schema" type="text" placeholder="Leave empty for auto-generated schema">
        </label>

        <label>
          Username
          <input v-model="createForm.username" type="text">
        </label>

        <label>
          Password
          <input v-model="createForm.password" type="password">
        </label>

        <label class="checkbox">
          <input v-model="createForm.ssl" type="checkbox">
          Use SSL
        </label>
      </div>

      <button class="action-btn primary" @click="submitCreate">Create project</button>
      <p v-if="createMessage" class="status-line">{{ createMessage }}</p>
    </section>
  </div>
</template>

<style scoped>
.project-workbench {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.panel {
  background: #ffffff;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  padding: 16px;
}

.project-list-panel {
  grid-row: span 3;
}

.active-project-panel,
.create-project-panel {
  min-width: 0;
}

.panel-header h2 {
  margin: 0 0 4px;
  font-size: 18px;
}

.panel-header p {
  margin: 0 0 12px;
  color: #6b7280;
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.project-card {
  text-align: left;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.project-card.active {
  border-color: #6366f1;
  background: #eef2ff;
}

.project-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.project-meta {
  margin-top: 6px;
  color: #4b5563;
  word-break: break-word;
}

.project-meta.small {
  font-size: 12px;
  color: #6b7280;
}

.badge {
  background: #c7d2fe;
  color: #312e81;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.info-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #f9fafb;
}

.info-card h3 {
  margin: 0 0 8px;
  font-size: 15px;
}

.member-list {
  margin: 0;
  padding-left: 18px;
}

.form-block {
  margin-top: 16px;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.form-grid.one-column {
  grid-template-columns: 1fr;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
}

input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: white;
}

.checkbox {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 10px;
  margin: 12px 0;
}

.action-btn {
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 10px;
  padding: 10px 14px;
}

.action-btn.primary {
  background: #4f46e5;
  color: #fff;
  border-color: #4f46e5;
}

.status-line {
  margin: 8px 0 0;
  color: #374151;
}

.status-line.ok {
  color: #166534;
}

.status-line.bad {
  color: #991b1b;
}

.empty-state {
  color: #6b7280;
}

@media (max-width: 1100px) {
  .project-workbench {
    grid-template-columns: 1fr;
  }

  .project-list-panel {
    grid-row: auto;
  }

  .info-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>