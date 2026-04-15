import {beforeEach, describe, expect, it} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {useAnnotationSettingsStore} from './annotationSettingsStore'

function ok<T>(data: T) {
    return JSON.stringify({success: true, data})
}

describe('annotationSettingsStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())

        window.appBridge = {
            ping: () => ok('pong'),
            getAppInfo: () => ok({name: 'NLP Studio', version: '0.1.0'}),

            listRules: () => ok([]),
            getRule: () => ok(null),

            listEntries: () => ok([]),
            getEntry: () => ok(null),
            saveEntry: () => ok(null),
            importEntries: () => ok(null),
            runCorrectionOnAll: () => ok(null),
            exportRawText: () => ok(null),
            exportConllu: () => ok(null),
            generateWorkspaceExport: () => ok(null),
            saveWorkspaceExport: () => ok(null),
            importWorkspaceFromFile: () => ok(null),

            getAnnotationSettings: () => ok({
                posDefinitionsYaml: '- VERB',
                featDefinitionsYaml: '- Pers[subj]',
                lexiconsYaml: 'spanish_verbs:\n  - ganar',
                extractorsYaml: 'agreement_verbs: {}',
                glossMapYaml: '{}',
                baseYamlPreview: 'def:\n  pos:\n    - VERB',
                effectiveYamlPreview: 'def:\n  pos:\n    - VERB\nrules: []',
            }),
            saveAnnotationSettings: (payloadJson: string) => {
                const parsed = JSON.parse(payloadJson)
                return ok({
                    ...parsed,
                    baseYamlPreview: 'updated-base',
                    effectiveYamlPreview: 'updated-effective',
                })
            },

            listRuleDescriptors: () => ok([]),
            listRuleSchemas: () => ok([]),
            getRuleSchema: () => ok(null),
            parseRuleYaml: () => ok(null),
            generateRuleYaml: () => ok(null),
            validateRule: () => ok(null),
            saveRule: () => ok(null),
            runCorrection: () => ok(null),
        }
    })

    it('loads annotation settings', async () => {
        const store = useAnnotationSettingsStore()
        await store.loadSettings()

        expect(store.draft.posDefinitionsYaml).toContain('VERB')
        expect(store.dirty).toBe(false)
    })

    it('patches and saves annotation settings', async () => {
        const store = useAnnotationSettingsStore()
        await store.loadSettings()

        store.patchDraft({glossMapYaml: 'pos: {}'})
        expect(store.dirty).toBe(true)

        await store.saveSettings()

        expect(store.draft.baseYamlPreview).toBe('updated-base')
        expect(store.draft.effectiveYamlPreview).toBe('updated-effective')
        expect(store.dirty).toBe(false)
    })
})