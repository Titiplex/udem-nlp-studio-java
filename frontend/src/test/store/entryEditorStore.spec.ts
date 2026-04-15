import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const callBridgeMock = vi.fn()

vi.mock('../../bridge/desktopBridge', () => ({
    callBridge: (...args: unknown[]) => callBridgeMock(...args),
}))

describe('entryEditorStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        callBridgeMock.mockReset()
    })

    it('refreshEntries loads entries and selects the first one', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'listEntries') {
                return {
                    success: true,
                    data: [
                        {
                            id: 'e1',
                            documentOrder: 1,
                            rawChujText: 'ix-naq',
                            rawGlossText: 'A1-ganar',
                            translation: 'Il gagne.',
                            approved: false,
                            hasCorrection: false,
                        },
                    ],
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        await store.refreshEntries()

        expect(store.entries).toHaveLength(1)
        expect(store.selectedEntryId).toBe('e1')
        expect(store.entryCount).toBe(1)
        expect(store.correctedCount).toBe(0)
    })

    it('loadEntry loads the draft and clears dirty state', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'getEntry') {
                return {
                    success: true,
                    data: {
                        id: 'e1',
                        documentOrder: 1,
                        contextText: 'ctx',
                        surfaceText: 'surface',
                        rawChujText: 'ix-naq',
                        rawGlossText: 'A1-ganar',
                        translation: 'Il gagne.',
                        comments: 'comment',
                        correctedChujText: '',
                        correctedGlossText: '',
                        correctedTranslation: '',
                        approved: false,
                        conlluPreview: '# sent_id = 1',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        await store.loadEntry('e1')

        expect(store.selectedEntryId).toBe('e1')
        expect(store.draft.rawChujText).toBe('ix-naq')
        expect(store.draft.contextText).toBe('ctx')
        expect(store.dirty).toBe(false)
        expect(store.statusMessage).toBe('Entrée chargée.')
    })

    it('saveEntry refuses empty segmentation', async () => {
        const store = useEntryEditorStore()
        store.patchDraft({
            rawChujText: '   ',
            translation: 'ok',
        })

        await store.saveEntry()

        expect(store.statusMessage).toContain('segmentation morphémique')
        expect(callBridgeMock).not.toHaveBeenCalled()
    })

    it('saveEntry refuses empty translation', async () => {
        const store = useEntryEditorStore()
        store.patchDraft({
            rawChujText: 'ix-naq',
            translation: '   ',
        })

        await store.saveEntry()

        expect(store.statusMessage).toContain('traduction est obligatoire')
        expect(callBridgeMock).not.toHaveBeenCalled()
    })

    it('saveEntry persists draft then refreshes entries', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'saveEntry') {
                return {
                    success: true,
                    data: {
                        id: 'saved-1',
                        documentOrder: 1,
                        contextText: '',
                        surfaceText: '',
                        rawChujText: 'ix-naq',
                        rawGlossText: 'A1-ganar',
                        translation: 'Il gagne.',
                        comments: '',
                        correctedChujText: '',
                        correctedGlossText: '',
                        correctedTranslation: '',
                        approved: false,
                        conlluPreview: '',
                    },
                }
            }
            if (method === 'listEntries') {
                return {
                    success: true,
                    data: [
                        {
                            id: 'saved-1',
                            documentOrder: 1,
                            rawChujText: 'ix-naq',
                            rawGlossText: 'A1-ganar',
                            translation: 'Il gagne.',
                            approved: false,
                            hasCorrection: false,
                        },
                    ],
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        store.patchDraft({
            rawChujText: 'ix-naq',
            rawGlossText: 'A1-ganar',
            translation: 'Il gagne.',
        })

        await store.saveEntry()

        expect(store.selectedEntryId).toBe('saved-1')
        expect(store.dirty).toBe(false)
        expect(store.statusMessage).toBe('Entrée sauvegardée.')
        expect(store.entries).toHaveLength(1)
    })

    it('runCorrection requires a saved entry', async () => {
        const store = useEntryEditorStore()

        await store.runCorrection()

        expect(store.statusMessage).toContain('Sauvegarde l’entrée')
        expect(callBridgeMock).not.toHaveBeenCalled()
    })

    it('runCorrection updates the draft and refreshes entries', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'runCorrection') {
                return {
                    success: true,
                    data: {
                        id: 'e1',
                        documentOrder: 1,
                        contextText: '',
                        surfaceText: '',
                        rawChujText: 'ix-naq',
                        rawGlossText: 'A1-ganar',
                        translation: 'Il gagne.',
                        comments: '',
                        correctedChujText: 'ix-naq',
                        correctedGlossText: 'A1-ganar',
                        correctedTranslation: 'Il gagne.',
                        approved: false,
                        conlluPreview: '# sent_id = 1',
                    },
                }
            }
            if (method === 'listEntries') {
                return {
                    success: true,
                    data: [
                        {
                            id: 'e1',
                            documentOrder: 1,
                            rawChujText: 'ix-naq',
                            rawGlossText: 'A1-ganar',
                            translation: 'Il gagne.',
                            approved: false,
                            hasCorrection: true,
                        },
                    ],
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        store.patchDraft({
            id: 'e1',
            rawChujText: 'ix-naq',
            rawGlossText: 'A1-ganar',
            translation: 'Il gagne.',
        })

        await store.runCorrection()

        expect(store.draft.correctedChujText).toBe('ix-naq')
        expect(store.statusMessage).toBe('Correction exécutée.')
        expect(store.correctedCount).toBe(1)
    })

    it('importEntries clears buffer, refreshes list and loads first entry', async () => {
        callBridgeMock.mockImplementation((method: string, payload?: string) => {
            if (method === 'importEntries') {
                expect(payload).toContain('Ix-naq')
                return {
                    success: true,
                    data: {
                        importedEntries: 2,
                        totalEntries: 2,
                    },
                }
            }
            if (method === 'listEntries') {
                return {
                    success: true,
                    data: [
                        {
                            id: 'e1',
                            documentOrder: 1,
                            rawChujText: 'Ix-naq',
                            rawGlossText: 'A1-ganar',
                            translation: 'Il gagne.',
                            approved: false,
                            hasCorrection: false,
                        },
                    ],
                }
            }
            if (method === 'getEntry') {
                return {
                    success: true,
                    data: {
                        id: 'e1',
                        documentOrder: 1,
                        contextText: '',
                        surfaceText: '',
                        rawChujText: 'Ix-naq',
                        rawGlossText: 'A1-ganar',
                        translation: 'Il gagne.',
                        comments: '',
                        correctedChujText: '',
                        correctedGlossText: '',
                        correctedTranslation: '',
                        approved: false,
                        conlluPreview: '',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        store.importBuffer = 'Ix-naq\nA1-ganar\nIl gagne.'

        await store.importEntries(true)

        expect(store.importBuffer).toBe('')
        expect(store.entries).toHaveLength(1)
        expect(store.selectedEntryId).toBe('e1')
        expect(store.statusMessage).toBe('Entrée chargée.')
    })

    it('loadAggregateConlluPreview stores generated content', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'exportConllu') {
                return {
                    success: true,
                    data: {
                        fileName: 'workspace.conllu',
                        content: '# sent_id = 1\n# text = ix-naq',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        await store.loadAggregateConlluPreview(true, false)

        expect(store.aggregateConlluPreview).toContain('# text = ix-naq')
        expect(store.statusMessage).toBe('Preview CoNLL-U chargée.')
    })

    it('loadAggregateRawPreview stores generated content', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'exportRawText') {
                return {
                    success: true,
                    data: {
                        fileName: 'workspace.txt',
                        content: 'ix-naq\nA1-ganar\nIl gagne.',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const store = useEntryEditorStore()
        await store.loadAggregateRawPreview(true, false)

        expect(store.aggregateRawPreview).toContain('ix-naq')
        expect(store.statusMessage).toBe('Export texte chargé.')
    })
})