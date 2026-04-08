import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import SetBlockEditor from './SetBlockEditor.vue'

describe('SetBlockEditor', () => {
    it('emits template features and extract rows', async () => {
        const wrapper = mount(SetBlockEditor, {
            props: {
                subtype: 'conllu',
                modelValue: {
                    upos: 'VERB',
                },
            },
        })

        const buttons = wrapper.findAll('button')
        const addTemplateButton = buttons.find((button) => button.text() === 'Add')
        expect(addTemplateButton).toBeTruthy()

        await addTemplateButton!.trigger('click')
        await addTemplateButton!.trigger('click')

        const inputs = wrapper.findAll('input')
        expect(inputs.length).toBeGreaterThan(0)

        await inputs[1].setValue('Pers[subj]')
        await inputs[2].setValue('{agreement_verbs.A.person}')

        const emitted = wrapper.emitted('update:modelValue')
        expect(emitted).toBeTruthy()

        const lastPayload = emitted![emitted!.length - 1][0] as Record<string, unknown>
        const templates = lastPayload.feats_template as Record<string, unknown> | undefined
        expect(templates).toBeTruthy()
    })
})