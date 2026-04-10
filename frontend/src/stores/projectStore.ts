import {defineStore} from 'pinia'
import {
    callBridge,
    type CreateProjectRequest,
    type ProjectConnectionStatus,
    type ProjectDetail,
    type ProjectSummary,
    type RegisterProjectRequest,
    type SaveSecretsRequest,
    type SwitchProjectRequest,
} from '../bridge/desktopBridge'

export const useProjectStore = defineStore('project', {
    state: () => ({
        projects: [] as ProjectSummary[],
        activeProject: null as ProjectDetail | null,
        connectionStatus: null as ProjectConnectionStatus | null,
        loading: false,
        error: '' as string,
    }),

    getters: {
        hasActiveProject: (state) => !!state.activeProject,
        activeSourceConfigured: (state) => {
            const source = state.activeProject?.sources?.[0]
            return !!source && source.secretsConfigured
        },
        activeSourceLabel: (state) => {
            const source = state.activeProject?.sources?.[0]
            if (!source) return 'No source'
            return `${source.host}/${source.database} [${source.schema}]`
        },
    },

    actions: {
        async loadProjects() {
            this.loading = true
            this.error = ''

            const resp = callBridge<ProjectSummary[]>('listProjects')
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot load projects'
                this.projects = []
                this.loading = false
                return
            }

            this.projects = resp.data ?? []
            this.loading = false
        },

        async loadActiveProject() {
            this.error = ''
            const resp = callBridge<ProjectDetail>('getActiveProject')
            if (!resp.success) {
                this.activeProject = null
                return
            }
            this.activeProject = resp.data ?? null
        },

        async refreshAll() {
            await this.loadProjects()
            await this.loadActiveProject()
        },

        async createProject(payload: CreateProjectRequest) {
            this.loading = true
            this.error = ''

            const resp = callBridge<ProjectDetail>('createProject', JSON.stringify(payload))
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot create project'
                this.loading = false
                return false
            }

            this.activeProject = resp.data ?? null
            await this.loadProjects()
            this.loading = false
            return true
        },

        async registerProject(payload: RegisterProjectRequest) {
            this.loading = true
            this.error = ''

            const resp = callBridge<ProjectSummary>('registerProject', JSON.stringify(payload))
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot register project'
                this.loading = false
                return false
            }

            await this.refreshAll()
            this.loading = false
            return true
        },

        async switchProject(projectId: string) {
            this.loading = true
            this.error = ''

            const payload: SwitchProjectRequest = {projectId}
            const resp = callBridge<ProjectDetail>('switchProject', JSON.stringify(payload))
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot switch project'
                this.loading = false
                return false
            }

            this.activeProject = resp.data ?? null
            this.connectionStatus = null
            await this.loadProjects()
            this.loading = false
            return true
        },

        async saveSecrets(payload: SaveSecretsRequest) {
            this.loading = true
            this.error = ''

            const resp = callBridge<string>('saveProjectSecrets', JSON.stringify(payload))
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot save project secrets'
                this.loading = false
                return false
            }

            await this.loadActiveProject()
            this.loading = false
            return true
        },

        async testConnection() {
            this.error = ''
            const resp = callBridge<ProjectConnectionStatus>('testActiveProjectConnection')
            if (!resp.success) {
                this.connectionStatus = {
                    success: false,
                    message: resp.message ?? 'Connection test failed',
                }
                return false
            }

            this.connectionStatus = resp.data ?? {
                success: false,
                message: 'No connection status returned',
            }

            return this.connectionStatus.success
        },

        async initializeSchema() {
            this.loading = true
            this.error = ''

            const resp = callBridge<ProjectDetail>('initializeActiveProjectSchema')
            if (!resp.success) {
                this.error = resp.message ?? 'Cannot initialize schema'
                this.loading = false
                return false
            }

            this.activeProject = resp.data ?? null
            this.loading = false
            return true
        },
    },
})