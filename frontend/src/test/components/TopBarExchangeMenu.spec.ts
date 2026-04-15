import {beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {createPinia, setActivePinia} from 'pinia'
import TopbarExchangeMenu from '../../components/shell/TopbarExchangeMenu.vue'
import {useEntryEditorStore} from '../../stores/entryEditorStore'

const callBridgeMock = vi.fn()

vi.mock('../../bridge/desktopBridge', () => ({
    callBridge: (...args: unknown[]) => callBridgeMock(...args),
}))

describe('TopbarExchangeMenu', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        callBridgeMock.mockReset()
    })

    it('opens the menu and generates an export preview', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'generateWorkspaceExport') {
                return {
                    success: true,
                    data: {
                        fileName: 'workspace-bundle.json',
                        content: '{"demo":true}',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        await wrapper.get('button.menu-btn').trigger('click')
        expect(wrapper.text()).toContain('Export')
        expect(wrapper.text()).toContain('Import')

        const buttons = wrapper.findAll('button.action-btn')
        await buttons[0].trigger('click')

        expect(wrapper.text()).toContain('Preview generated: workspace-bundle.json')
        expect(wrapper.text()).toContain('workspace-bundle.json')
        expect(wrapper.text()).toContain('{"demo":true}')
    })

    it('imports from file and refreshes entry previews', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'importWorkspaceFromFile') {
                return {
                    success: true,
                    data: {
                        importedEntries: 2,
                        importedRules: 1,
                        summary: '2 entries and 1 rules imported from bundle.',
                    },
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        const store = useEntryEditorStore()
        store.refreshEntries = vi.fn().mockResolvedValue(undefined)
        store.loadAggregateConlluPreview = vi.fn().mockResolvedValue(undefined)
        store.loadAggregateRawPreview = vi.fn().mockResolvedValue(undefined)

        await wrapper.get('button.menu-btn').trigger('click')

        const buttons = wrapper.findAll('button.action-btn')
        const importAppend = buttons[2]
        await importAppend.trigger('click')

        expect(wrapper.text()).toContain('2 entries and 1 rules imported from bundle.')
        expect(store.refreshEntries).toHaveBeenCalled()
        expect(store.loadAggregateConlluPreview).toHaveBeenCalled()
        expect(store.loadAggregateRawPreview).toHaveBeenCalled()
    })

    it('shows an error message when export preview generation fails', async () => {
        callBridgeMock.mockImplementation((method: string) => {
            if (method === 'generateWorkspaceExport') {
                return {
                    success: false,
                    message: 'Export preview generation failed upstream.',
                }
            }
            return {success: false, message: 'unexpected'}
        })

        const wrapper = mount(TopbarExchangeMenu, {
            global: {
                plugins: [createPinia()],
            },
        })

        await wrapper.get('button.menu-btn').trigger('click')

        const buttons = wrapper.findAll('button.action-btn')
        await buttons[0].trigger('click')

        expect(wrapper.text()).toContain('Export preview generation failed upstream.')
    })
})