import {defineStore} from 'pinia'
import {
    type BatchCorrectionRequest,
    type BatchCorrectionResult,
    callBridge,
    type CorrectionRunRequest,
    type EntryDetail,
    type EntrySummary,
    type TextExport,
    type WorkspaceExportRequest,
    type WorkspaceImportRequest,
    type WorkspaceImportResult,
} from '../bridge/desktopBridge'

function emptyEntry(): EntryDetail {
    return {
        id: null,
        documentOrder: 1,
        rawChujText: '',
        rawGlossText: '',
        translation: '',
        correctedChujText: '',
        correctedGlossText: '',
        correctedTranslation: '',
        approved: false,
        conlluPreview: '',
        version: 0,
        updatedBy: null,
        updatedAt: null,
    }
}

function isConflictMessage(message?: string | null): boolean {
    return !!message && message.toLowerCase().startsWith('conflict:')
}

export const useEntryEditorStore = defineStore('entryEditor', {
    state: () => ({
        entries: [] as EntrySummary[],
        selectedEntryId: null as string | null,
        draft: emptyEntry() as EntryDetail,
        remoteConflictDraft: null as EntryDetail | null,
        busy: false,
        dirty: false,
        statusMessage: '',
        conflictMessage: '',
        importBuffer: '',
        aggregateConlluPreview: '',
        aggregateRawPreview: '',
    }),

    getters: {
        entryCount(state): number {
            return state.entries.length
        },

        correctedCount(state): number {
            return state.entries.filter((entry) => entry.hasCorrection).length
        },

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

        localConflictRaw(state): string {
            return [
                state.draft.rawChujText,
                state.draft.rawGlossText,
                state.draft.translation,
            ].join('\n')
        },

        remoteConflictRaw(state): string {
            if (!state.remoteConflictDraft) return ''
            return [
                state.remoteConflictDraft.rawChujText,
                state.remoteConflictDraft.rawGlossText,
                state.remoteConflictDraft.translation,
            ].join('\n')
        },

        localConflictCorrected(state): string {
            return [
                state.draft.correctedChujText,
                state.draft.correctedGlossText,
                state.draft.correctedTranslation,
            ].join('\n')
        },

        remoteConflictCorrected(state): string {
            if (!state.remoteConflictDraft) return ''
            return [
                state.remoteConflictDraft.correctedChujText,
                state.remoteConflictDraft.correctedGlossText,
                state.remoteConflictDraft.correctedTranslation,
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
            const entryId = this.selectedEntryId || this.draft.id
            if (!entryId) {
                this.remoteConflictDraft = null
                return
            }

            const resp = callBridge<EntryDetail>('getEntry', entryId)
            if (!resp.success || !resp.data) {
                this.remoteConflictDraft = null
                return
            }

            this.remoteConflictDraft = structuredClone(resp.data)
        },

        async refreshEntries() {
            const resp = callBridge<EntrySummary[]>('listEntries')
            if (!resp.success) {
                this.statusMessage = resp.message ?? 'Impossible de charger les entrées.'
                return
            }

            this.entries = resp.data ?? []

            if (!this.selectedEntryId && this.entries.length > 0) {
                this.selectedEntryId = this.entries[0].id
            }
        },

        async loadEntry(id: string) {
            this.busy = true
            try {
                const resp = callBridge<EntryDetail>('getEntry', id)
                if (!resp.success || !resp.data) {
                    this.statusMessage = resp.message ?? 'Impossible de charger l’entrée.'
                    return
                }

                this.selectedEntryId = id
                this.draft = structuredClone(resp.data)
                this.dirty = false
                this.clearConflict()
                this.statusMessage = 'Entrée chargée.'
            } finally {
                this.busy = false
            }
        },

        async reloadRemoteVersion() {
            if (!this.selectedEntryId) {
                if (this.draft.id) {
                    await this.loadEntry(this.draft.id)
                }
                return
            }
            await this.loadEntry(this.selectedEntryId)
        },

        createNewEntry() {
            const nextOrder =
                this.entries.reduce((max, entry) => Math.max(max, entry.documentOrder), 0) + 1

            this.selectedEntryId = null
            this.draft = {
                ...emptyEntry(),
                documentOrder: nextOrder,
            }
            this.dirty = false
            this.clearConflict()
            this.statusMessage = 'Nouvelle entrée.'
        },

        setDraft(next: EntryDetail) {
            this.draft = structuredClone(next)
            this.dirty = true
        },

        patchDraft(patch: Partial<EntryDetail>) {
            this.draft = {
                ...this.draft,
                ...patch,
            }
            this.dirty = true
        },

        async saveEntry() {
            const resp = callBridge<EntryDetail>('saveEntry', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                if (isConflictMessage(resp.message)) {
                    await this.setConflict(resp.message ?? 'Conflict detected.')
                    return
                }
                this.statusMessage = resp.message ?? 'Sauvegarde impossible.'
                return
            }

            this.draft = structuredClone(resp.data)
            this.selectedEntryId = resp.data.id
            this.dirty = false
            this.clearConflict()
            this.statusMessage = 'Entrée sauvegardée.'
            await this.refreshEntries()
        },

        async runCorrection(force = false) {
            if (!this.draft.id) {
                this.statusMessage = 'Sauvegarde l’entrée avant de lancer la correction.'
                return
            }

            const payload: CorrectionRunRequest = {
                entryId: this.draft.id,
                force,
            }

            const resp = callBridge<EntryDetail>('runCorrection', JSON.stringify(payload))
            if (!resp.success || !resp.data) {
                if (isConflictMessage(resp.message)) {
                    await this.setConflict(resp.message ?? 'Conflict detected.')
                    return
                }
                this.statusMessage = resp.message ?? 'Correction impossible.'
                return
            }

            this.draft = structuredClone(resp.data)
            this.dirty = false
            this.clearConflict()
            this.statusMessage = 'Correction exécutée.'
            await this.refreshEntries()
        },

        async importEntries(replaceExisting: boolean) {
            const payload: WorkspaceImportRequest = {
                rawText: this.importBuffer,
                replaceExisting,
            }

            const resp = callBridge<WorkspaceImportResult>('importEntries', JSON.stringify(payload))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Import impossible.'
                return
            }

            this.importBuffer = ''
            this.clearConflict()
            this.statusMessage = `${resp.data.importedEntries} entrées importées.`
            await this.refreshEntries()

            if (this.entries.length > 0) {
                await this.loadEntry(this.entries[0].id)
            } else {
                this.createNewEntry()
            }
        },

        async runCorrectionOnAll(force = false) {
            const payload: BatchCorrectionRequest = {force}

            const resp = callBridge<BatchCorrectionResult>('runCorrectionOnAll', JSON.stringify(payload))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Correction en lot impossible.'
                return
            }

            this.clearConflict()
            this.statusMessage =
                `Correction en lot terminée : ${resp.data.correctedEntries}/${resp.data.totalEntries} corrigées` +
                (resp.data.skippedApprovedEntries > 0
                    ? `, ${resp.data.skippedApprovedEntries} approuvées ignorées`
                    : '.')

            await this.refreshEntries()

            if (this.selectedEntryId) {
                await this.loadEntry(this.selectedEntryId)
            }
        },

        async loadAggregateConlluPreview(preferCorrected = true, correctedOnly = false) {
            const payload: WorkspaceExportRequest = {
                preferCorrected,
                correctedOnly,
            }

            const resp = callBridge<TextExport>('exportConllu', JSON.stringify(payload))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Chargement de la preview CoNLL-U impossible.'
                return
            }

            this.aggregateConlluPreview = resp.data.content
            this.statusMessage = 'Preview CoNLL-U chargée.'
        },

        async loadAggregateRawPreview(preferCorrected = true, correctedOnly = false) {
            const payload: WorkspaceExportRequest = {
                preferCorrected,
                correctedOnly,
            }

            const resp = callBridge<TextExport>('exportRawText', JSON.stringify(payload))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Chargement de l’export texte impossible.'
                return
            }

            this.aggregateRawPreview = resp.data.content
            this.statusMessage = 'Export texte chargé.'
        },
    },
})