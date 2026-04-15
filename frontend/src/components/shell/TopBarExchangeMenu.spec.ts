import {beforeEach, describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import {createPinia, setActivePinia} from 'pinia'
import TopbarExchangeMenu from './TopbarExchangeMenu.vue'

function ok<T>(data: T) {
    return JSON.stringify({success: true, data})
}

async function flush() {
    await Promise.resolve()
    await Promise.resolve()
}

describe('TopbarExchangeMenu', () => {
    beforeEach(() => {
        setActivePinia(createPinia())

        window.appBridge = {
            ping: () => ok('pong'),
            getAppInfo: () => ok({name: 'NLP Studio', version: '0.1.0'}),

            listRules: () => ok([]),
            getRule: () => ok(null),

            listEntries: () => ok([
                {
                    id: 'entry-1',
                    documentOrder: 1,
                    rawChujText: 'Ix-naq',
                    rawGlossText: 'A1-ganar',
                    translation: 'Il gagne.',
                    approved: false,
                    hasCorrection: false,
                },
            ]),
            getEntry: () => ok({
                id: 'entry-1',
                documentOrder: 1,
                contextText: 'Field note A',
                surfaceText: 'Ix naq',
                rawChujText: 'Ix-naq',
                rawGlossText: 'A1-ganar',
                translation: 'Il gagne.',
                comments: 'Initial comment',
                correctedChujText: '',
                correctedGlossText: '',
                correctedTranslation: '',
                approved: false,
                conlluPreview: '',
            }),
            saveEntry: () => ok(null),
            importEntries: () => ok({importedEntries: 1, totalEntries: 1}),
            runCorrectionOnAll: () => ok({
                totalEntries: 1,
                correctedEntries: 1,
                skippedApprovedEntries: 0,
            }),
            exportRawText: () => ok({
                fileName: 'workspace.txt',
                content: 'Ix-naq\nA1-ganar\nIl gagne.',
            }),
            exportConllu: () => ok({
                fileName: 'workspace.conllu',
                content: '# sent_id = 1\n# text = Ix-naq',
            }),

            generateWorkspaceExport: (payloadJson: string) => {
                const payload = JSON.parse(payloadJson)
                return ok({
                    fileName: `${payload.format}.json`,
                    content: `preview for ${payload.format}`,
                })
            },
            saveWorkspaceExport: () => ok('/tmp/workspace-bundle.json'),
            importWorkspaceFromFile: () => ok({
                importedEntries: 1,
                importedRules: 2,
                summary: '1 entries and 2 rules imported from bundle.',
            }),

            getAnnotationSettings: () => ok({
                posDefinitionsYaml: '- VERB',
                featDefinitionsYaml: '- Pers[subj]',
                lexiconsYaml: 'spanish_verbs:\n  - ganar',
                extractorsYaml: 'agreement_verbs: {}',
                glossMapYaml: '{}',
                baseYamlPreview: 'base',
                effectiveYamlPreview: 'effective',
            }),
            saveAnnotationSettings: () => ok(null),

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

    it('opens the file menu and generates an export preview', async () => {
        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        await wrapper.get('button.menu-btn').trigger('click')
        expect(wrapper.text()).toContain('Export')
        expect(wrapper.text()).toContain('Import')

        await wrapper.get('button.action-btn').trigger('click')
        await flush()

        expect(wrapper.text()).toContain('Preview generated')
        expect(wrapper.text()).toContain('workspace_bundle_json.json')
        expect(wrapper.text()).toContain('preview for workspace_bundle_json')
    })

    it('saves an export to file', async () => {
        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        await wrapper.get('button.menu-btn').trigger('click')
        const buttons = wrapper.findAll('button.action-btn')
        await buttons[1].trigger('click')
        await flush()

        expect(wrapper.text()).toContain('Export saved: /tmp/workspace-bundle.json')
    })

    it('imports data from file and shows the import summary', async () => {
        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        await wrapper.get('button.menu-btn').trigger('click')

        const allButtons = wrapper.findAll('button.action-btn')
        const importAppendButton = allButtons.find((button) => button.text() === 'Import append')
        expect(importAppendButton).toBeTruthy()

        await importAppendButton!.trigger('click')
        await flush()

        expect(wrapper.text()).toContain('1 entries and 2 rules imported from bundle.')
    })
})