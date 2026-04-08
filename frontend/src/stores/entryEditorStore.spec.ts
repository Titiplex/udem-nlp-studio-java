import {beforeEach, describe, expect, it} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {useEntryEditorStore} from './entryEditorStore'

function ok<T>(data: T) {
    return JSON.stringify({success: true, data})
}

describe('entryEditorStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())

        window.appBridge = {
            ping: () => ok('pong'),
            getAppInfo: () => ok({name: 'NLP Studio', version: '0.1.0'}),
            listRules: () => ok([]),
            getRule: () => ok(null),
            listEntries: () =>
                ok([
                    {
                        id: 'entry-1',
                        documentOrder: 1,
                        rawChujText: 'Ix naq',
                        rawGlossText: 'A1 ganar',
                        translation: 'Il gagne.',
                        approved: false,
                        hasCorrection: false,
                    },
                ]),
            getEntry: () =>
                ok({
                    id: 'entry-1',
                    documentOrder: 1,
                    rawChujText: 'Ix naq',
                    rawGlossText: 'A1 ganar',
                    translation: 'Il gagne.',
                    correctedChujText: '',
                    correctedGlossText: '',
                    correctedTranslation: '',
                    approved: false,
                    conlluPreview: '',
                }),
            saveEntry: (payloadJson: string) => ok(JSON.parse(payloadJson)),
            importEntries: () => ok({importedEntries: 2, totalEntries: 2}),
            runCorrectionOnAll: () =>
                ok({
                    totalEntries: 2,
                    correctedEntries: 2,
                    skippedApprovedEntries: 0,
                }),
            exportRawText: () =>
                ok({
                    fileName: 'workspace.txt',
                    content: 'Ix naq\nA1 ganar\nIl gagne.',
                }),
            exportConllu: () =>
                ok({
                    fileName: 'workspace.conllu',
                    content: '# sent_id = 1\n# text = Ix naq',
                }),
            listRuleDescriptors: () => ok([]),
            listRuleSchemas: () => ok([]),
            getRuleSchema: () => ok(null),
            parseRuleYaml: () => ok(null),
            generateRuleYaml: () => ok(null),
            validateRule: () => ok(null),
            saveRule: () => ok(null),
            runCorrection: () =>
                ok({
                    id: 'entry-1',
                    documentOrder: 1,
                    rawChujText: 'Ix naq',
                    rawGlossText: 'A1 ganar',
                    translation: 'Il gagne.',
                    correctedChujText: 'Ix naq',
                    correctedGlossText: 'A1 ganar',
                    correctedTranslation: 'Il gagne.',
                    approved: false,
                    conlluPreview: '# sent_id = 1\n# text = Ix naq',
                }),
        }
    })

    it('loads entry summaries and the selected entry', async () => {
        const store = useEntryEditorStore()

        await store.refreshEntries()
        expect(store.entries).toHaveLength(1)
        expect(store.selectedEntryId).toBe('entry-1')

        await store.loadEntry('entry-1')
        expect(store.draft.rawChujText).toBe('Ix naq')
        expect(store.dirty).toBe(false)
    })

    it('saves the current draft', async () => {
        const store = useEntryEditorStore()

        store.createNewEntry()
        store.patchDraft({
            rawChujText: 'Ha ix to',
            rawGlossText: 'DEM A1 ir',
            translation: 'Celui-ci va.',
        })

        await store.saveEntry()

        expect(store.statusMessage).toBe('Entrée sauvegardée.')
        expect(store.dirty).toBe(false)
        expect(store.draft.rawChujText).toBe('Ha ix to')
    })

    it('runs correction and updates preview', async () => {
        const store = useEntryEditorStore()

        await store.loadEntry('entry-1')
        await store.runCorrection()

        expect(store.draft.correctedChujText).toBe('Ix naq')
        expect(store.draft.conlluPreview).toContain('# text = Ix naq')
    })

    it('imports entries from raw text buffer', async () => {
        const store = useEntryEditorStore()

        store.importBuffer = 'Ix naq\nA1 ganar\nIl gagne.'
        await store.importEntries(true)

        expect(store.importBuffer).toBe('')
        expect(store.statusMessage).toContain('2 entrées importées')
    })

    it('loads aggregate previews', async () => {
        const store = useEntryEditorStore()

        await store.loadAggregateRawPreview(true, false)
        await store.loadAggregateConlluPreview(true, false)

        expect(store.aggregateRawPreview).toContain('Ix naq')
        expect(store.aggregateConlluPreview).toContain('# text = Ix naq')
    })

    it('runs batch correction', async () => {
        const store = useEntryEditorStore()

        await store.runCorrectionOnAll(false)

        expect(store.statusMessage).toContain('Correction en lot terminée')
    })
})