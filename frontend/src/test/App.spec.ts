import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import {createPinia} from 'pinia'
import App from '../App.vue'

describe('App', () => {
    it('loads app info and bridge status on mount', async () => {
        window.appBridge = {
            ping: () => JSON.stringify({success: true, data: 'pong'}),
            getAppInfo: () =>
                JSON.stringify({
                    success: true,
                    data: {
                        name: 'NLP Studio Test',
                        version: '9.9.9',
                    },
                }),
        } as any

        const wrapper = mount(App, {
            global: {
                plugins: [createPinia()],
                stubs: {
                    RuleWorkbench: {template: '<div>RuleWorkbench</div>'},
                    EntriesWorkbench: {template: '<div>EntriesWorkbench</div>'},
                    PreviewWorkbench: {template: '<div>PreviewWorkbench</div>'},
                    SettingsWorkbench: {template: '<div>SettingsWorkbench</div>'},
                    TopbarExchangeMenu: {template: '<div>TopbarExchangeMenu</div>'},
                },
            },
        })

        await new Promise((resolve) => setTimeout(resolve, 0))
        await new Promise((resolve) => setTimeout(resolve, 0))

        expect(wrapper.text()).toContain('NLP Studio Test')
        expect(wrapper.text()).toContain('Version 9.9.9')
        expect(wrapper.text()).toContain('pong')
        expect(wrapper.text()).toContain('Rules')
    })

    it('shows bridge unavailable when bridge never appears', async () => {
        delete window.appBridge

        const wrapper = mount(App, {
            global: {
                plugins: [createPinia()],
                stubs: {
                    RuleWorkbench: {template: '<div>RuleWorkbench</div>'},
                    EntriesWorkbench: {template: '<div>EntriesWorkbench</div>'},
                    PreviewWorkbench: {template: '<div>PreviewWorkbench</div>'},
                    SettingsWorkbench: {template: '<div>SettingsWorkbench</div>'},
                    TopbarExchangeMenu: {template: '<div>TopbarExchangeMenu</div>'},
                },
            },
        })

        await new Promise((resolve) => setTimeout(resolve, 3100))

        expect(wrapper.text()).toContain('Desktop bridge unavailable')
    })
})