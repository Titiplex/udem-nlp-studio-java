import {defineStore} from 'pinia'

export type WorkspaceSection = 'projects' | 'rules' | 'entries' | 'preview' | 'settings'

export const useWorkspaceStore = defineStore('workspace', {
    state: () => ({
        currentSection: 'projects' as WorkspaceSection,
    }),
    actions: {
        openSection(section: WorkspaceSection) {
            this.currentSection = section
        },
    },
})