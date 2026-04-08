import {defineStore} from 'pinia'

export type WorkspaceSection = 'rules' | 'entries' | 'preview' | 'settings'

export const useWorkspaceStore = defineStore('workspace', {
    state: () => ({
        currentSection: 'rules' as WorkspaceSection,
    }),
    actions: {
        openSection(section: WorkspaceSection) {
            this.currentSection = section
        },
    },
})