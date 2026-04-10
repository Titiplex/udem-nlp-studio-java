import {defineStore} from 'pinia'
import {callBridge, type AnnotationSettings} from '../bridge/desktopBridge'

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
        remoteConflictDraft: null as AnnotationSettings | null,
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

        hasRemoteConflictDraft(state): boolean {
            return !!state.remoteConflictDraft
        },

        localConflictYaml(state): string {
            return [
                '# POS',
                state.draft.posDefinitionsYaml,
                '',
                '# FEATS',
                state.draft.featDefinitionsYaml,
                '',
                '# LEXICONS',
                state.draft.lexiconsYaml,
                '',
                '# EXTRACTORS',
                state.draft.extractorsYaml,
                '',
                '# GLOSS MAP',
                state.draft.glossMapYaml,
            ].join('\n')
        },

        remoteConflictYaml(state): string {
            if (!state.remoteConflictDraft) return ''
            return [
                '# POS',
                state.remoteConflictDraft.posDefinitionsYaml,
                '',
                '# FEATS',
                state.remoteConflictDraft.featDefinitionsYaml,
                '',
                '# LEXICONS',
                state.remoteConflictDraft.lexiconsYaml,
                '',
                '# EXTRACTORS',
                state.remoteConflictDraft.extractorsYaml,
                '',
                '# GLOSS MAP',
                state.remoteConflictDraft.glossMapYaml,
            ].join('\n')
        },
    },

    actions: {
        clearConflict() {
            this.conflictMessage = ''
            this.remoteConflictDraft = null
        },

        async setConflict(message: string) {
            this.conflictMessage = message
            this.statusMessage = message
            await this.fetchRemoteForConflict()
        },

        async fetchRemoteForConflict() {
            const resp = callBridge<AnnotationSettings>('getAnnotationSettings')
            if (!resp.success || !resp.data) {
                this.remoteConflictDraft = null
                return
            }

            this.remoteConflictDraft = structuredClone(resp.data)
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
                    await this.setConflict(resp.message ?? 'Conflict detected.')
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