import { defineStore } from 'pinia'
import { callBridge, type AnnotationSettings } from '../bridge/desktopBridge'

function emptySettings(): AnnotationSettings {
    return {
        posDefinitionsYaml: '',
        featDefinitionsYaml: '',
        lexiconsYaml: '',
        extractorsYaml: '',
        glossMapYaml: '',
        baseYamlPreview: '',
        effectiveYamlPreview: '',
    }
}

export const useAnnotationSettingsStore = defineStore('annotationSettings', {
    state: () => ({
        draft: emptySettings() as AnnotationSettings,
        busy: false,
        dirty: false,
        statusMessage: '',
    }),

    actions: {
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
                this.statusMessage = 'Settings d’annotation chargés.'
            } finally {
                this.busy = false
            }
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
                this.statusMessage = resp.message ?? 'Sauvegarde impossible.'
                return
            }

            this.draft = structuredClone(resp.data)
            this.dirty = false
            this.statusMessage = 'Settings d’annotation sauvegardés.'
        },
    },
})