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
    }
}

export const useRuleEditorStore = defineStore('ruleEditor', {
    state: () => ({
        rules: [] as RuleSummary[],
        selectedRuleId: null as string | null,
        draft: emptyRule() as RuleDetail,
        issues: [] as ValidationIssue[],
        activeTab: 'visual' as EditorTab,
        busy: false,
        statusMessage: '',
        dirty: false,
    }),

    getters: {
        selectedRuleSummary(state): RuleSummary | null {
            return state.rules.find((r) => r.id === state.selectedRuleId) ?? null
        },
    },

    actions: {
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
                this.statusMessage = 'Règle chargée.'
            } finally {
                this.busy = false
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

        saveDraft() {
            const resp = callBridge<RuleDraftResult>('saveRule', JSON.stringify(this.draft))
            if (!resp.success || !resp.data) {
                this.statusMessage = resp.message ?? 'Sauvegarde impossible.'
                return
            }

            this.draft = structuredClone(resp.data.rule)
            this.selectedRuleId = resp.data.rule.id ?? null
            this.issues = resp.data.issues ?? []
            this.dirty = false
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
            }
            this.issues = []
            this.dirty = false
            this.activeTab = 'visual'
            this.statusMessage = 'Nouveau draft.'
        },
    },
})