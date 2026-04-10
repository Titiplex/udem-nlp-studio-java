import {defineStore} from 'pinia'
import {type AnnotationSettings, callBridge} from '../bridge/desktopBridge'

function emptySettings(): AnnotationSettings {
    return {
        posDefinitionsYaml: '',
        featDefinitionsYaml: '',
        lexiconsYaml: '',
        extractorsYaml: '',
        glossMapYaml: '',
        baseYamlPreview: '',
        effectiveYamlPreview: '',
        version: 0,
        updatedBy: null,
        updatedAt: null,
    }
}

function isConflictMessage(message?: string | null): boolean {
    return !!message && message.toLowerCase().startsWith('conflict:')
}

export const useAnnotationSettingsStore = defineStore('annotationSettings', {
    state: () => ({
        draft: emptySettings() as AnnotationSettings,
        busy: false,
        dirty: false,
        statusMessage: '',
        conflictMessage: '',
    }),

    getters: {
        draftMetaLine(state): string {
            const parts: string[] = []

            if (state.draft.version != null) {
                parts.push(`v${state.draft.version}`)
            }

            if (state.draft.updatedBy) {
                parts.push(`updated by ${state.draft.updatedBy}`)
            }

            if (state.draft.updatedAt) {
                parts.push(`at ${state.draft.updatedAt}`)
            }

            return parts.join(' • ')
        },

        hasConflict(state): boolean {
            return !!state.conflictMessage
        },
    },

    actions: {
        clearConflict() {
            this.conflictMessage = ''
        },

        setConflict(message: string) {
            this.conflictMessage = message
            this.statusMessage = message
        },

        async loadSettings() {
            this.busy = true
            try {
                const resp = callBridge<AnnotationSettings>('getAnnotationSettings')
                if (!resp.success || !resp.data) {
                    this.statusMessage = resp.message ?? 'Impossible de charger les settings.'
                    return
                }

                this.draft = structuredClone(resp.data)
                this.dirty = false
                this.clearConflict()
                this.statusMessage = 'Settings d’annotation chargés.'
            } finally {
                this.busy = false
            }
        },

        async reloadRemoteVersion() {
            await this.loadSettings()
        },

        setDraft(next: AnnotationSettings) {
            this.draft = structuredClone(next)
            this.dirty = true
        },

        patchDraft(patch: Partial<AnnotationSettings>) {
            this.draft = {
                ...this.draft,
                ...patch,
            }
            this.dirty = true
        },

        async saveSettings() {
            const resp = callBridge<AnnotationSettings>(
                'saveAnnotationSettings',
                JSON.stringify(this.draft),
            )

            if (!resp.success || !resp.data) {
                if (isConflictMessage(resp.message)) {
                    this.setConflict(resp.message ?? 'Conflict detected.')
                    return
                }
                this.statusMessage = resp.message ?? 'Sauvegarde impossible.'
                return
            }

            this.draft = structuredClone(resp.data)
            this.dirty = false
            this.clearConflict()
            this.statusMessage = 'Settings d’annotation sauvegardés.'
        },
    },
})