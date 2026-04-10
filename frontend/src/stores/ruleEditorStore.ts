import {defineStore} from 'pinia'
import {
    callBridge,
    type RuleDetail,
    type RuleDraftResult,
    type RuleSummary,
    type ValidationIssue,
} from '../bridge/desktopBridge'

export type EditorTab = 'visual' | 'yaml' | 'preview' | 'validation'

function emptyRule(): RuleDetail {
    return {
        id: null,
        name: '',
        kind: '',
        subtype: '',
        scope: 'token',
        enabled: true,
        priority: 100,
        description: '',
        payload: {},
        rawYaml: '',
        version: 0,
        updatedBy: null,
        updatedAt: null,
    }
}

function isConflictMessage(message?: string | null): boolean {
    return !!message && message.toLowerCase().startsWith('conflict:')
}

function prettyJson(value: unknown): string {
    try {
        return JSON.stringify(value ?? {}, null, 2)
    } catch {
        return String(value ?? '')
    }
}

export const useRuleEditorStore = defineStore('ruleEditor', {
    state: () => ({
        rules: [] as RuleSummary[],
        selectedRuleId: null as string | null,
        draft: emptyRule() as RuleDetail,
        remoteConflictDraft: null as RuleDetail | null,
        issues: [] as ValidationIssue[],
        activeTab: 'visual' as EditorTab,
        busy: false,
        statusMessage: '',
        conflictMessage: '',
        dirty: false,
    }),

    getters: {
        selectedRuleSummary(state): RuleSummary | null {
            return state.rules.find((r) => r.id === state.selectedRuleId) ?? null
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

        localConflictYaml(state): string {
            return state.draft.rawYaml || prettyJson(state.draft.payload)
        },

        remoteConflictYaml(state): string {
            return state.remoteConflictDraft?.rawYaml || prettyJson(state.remoteConflictDraft?.payload)
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
            if (!this.selectedRuleId) {
                this.remoteConflictDraft = null
                return
            }

            const resp = callBridge<RuleDetail>('getRule', this.selectedRuleId)
            if (!resp.success || !resp.data) {
                this.remoteConflictDraft = null
                return
            }

            this.remoteConflictDraft = structuredClone(resp.data)
        },

        async refreshRules() {
            const resp = callBridge<RuleSummary[]>('listRules')
            if (!resp.success) {
                this.statusMessage = resp.message ?? 'Impossible de charger les règles.'
                return
            }

            this.rules = resp.data ?? []

            if (!this.selectedRuleId && this.rules.length > 0) {
                this.selectedRuleId = this.rules[0].id
            }
        },

        async loadRule(id: string) {
            this.busy = true
            try {
                const resp = callBridge<RuleDetail>('getRule', id)
                if (!resp.success || !resp.data) {
                    this.statusMessage = resp.message ?? 'Impossible de charger la règle.'
                    return
                }

                this.selectedRuleId = id
                this.draft = structuredClone(resp.data)
                this.issues = []
                this.dirty = false
                this.clearConflict()
                this.statusMessage = 'Règle chargée.'
            } finally {
                this.busy = false
            }
        },

        async reloadRemoteVersion() {
            if (this.selectedRuleId) {
                await this.loadRule(this.selectedRuleId)
            }
        },

        setDraft(next: RuleDetail) {
            this.draft = structuredClone(next)
            this.dirty = true
        },

        patchDraft(patch: Partial<RuleDetail>) {
            this.draft = {
                ...this.draft,
                ...patch,
                kind: patch.kind ? patch.kind.toUpperCase() : this.draft.kind,
            }
            this.dirty = true
        },

        patchPayload(key: string, value: unknown) {
            this.draft = {
                ...this.draft,
                payload: {
                    ...this.draft.payload,
                    [key]: value,
                },
            }
            this.dirty = true
        },

        removePayloadKeys(keys: string[]) {
            const next = {...this.draft.payload}
            for (const key of keys) {
                delete next[key]
            }
            this.draft = {
                ...this.draft,
                payload: next,
            }
            this.dirty = true
        },

        validateDraft() {
            const resp = callBridge<RuleDraftResult>('validateRule', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Validation impossible.'
                return
            }

            this.draft = structuredClone(resp.data.rule)
            this.issues = resp.data.issues ?? []
            this.statusMessage = 'Validation terminée.'
        },

        generateYaml() {
            const resp = callBridge<RuleDraftResult>('generateRuleYaml', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Génération YAML impossible.'
                return
            }

            this.draft = structuredClone(resp.data.rule)
            this.issues = resp.data.issues ?? []
            this.dirty = true
            this.statusMessage = 'YAML généré.'
        },

        parseYaml() {
            if (!this.draft.rawYaml || !this.draft.rawYaml.trim()) {
                this.statusMessage = 'Le YAML est vide.'
                this.issues = [{
                    path: 'rawYaml',
                    level: 'error',
                    message: 'Le YAML est vide.',
                }]
                return
            }

            const resp = callBridge<RuleDraftResult>('parseRuleYaml', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Parsing YAML impossible.'
                return
            }

            this.draft = structuredClone(resp.data.rule)
            this.issues = resp.data.issues ?? []
            this.dirty = true
            this.statusMessage = 'YAML parsé.'
        },

        async saveDraft() {
            const resp = callBridge<RuleDraftResult>('saveRule', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                if (isConflictMessage(resp.message)) {
                    await this.setConflict(resp.message ?? 'Conflict detected.')
                    return
                }
                this.statusMessage = resp.message ?? 'Sauvegarde impossible.'
                return
            }

            this.draft = structuredClone(resp.data.rule)
            this.issues = resp.data.issues ?? []
            this.dirty = false
            this.clearConflict()
            this.statusMessage = 'Règle sauvegardée.'
            void this.refreshRules()
        },

        createNewRule(kind: string, subtype: string) {
            this.selectedRuleId = null
            this.draft = {
                id: null,
                name: '',
                kind: kind.toUpperCase(),
                subtype,
                scope: 'token',
                enabled: true,
                priority: 100,
                description: '',
                payload: {},
                rawYaml: '',
                version: 0,
                updatedBy: null,
                updatedAt: null,
            }
            this.issues = []
            this.dirty = false
            this.clearConflict()
            this.activeTab = 'visual'
            this.statusMessage = 'Nouveau draft.'
        },
    },
})