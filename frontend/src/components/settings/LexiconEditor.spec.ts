import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import LexiconEditor from './LexiconEditor.vue'

describe('LexiconEditor', () => {
    it('emits updated YAML when adding a lexicon entry', async () => {
        const wrapper = mount(LexiconEditor, {
            props: {
                modelValue: `
spanish_verbs:
  - ganar
        `.trim(),
            },
        })

        await wrapper.get('button').trigger('click')

        const inputs = wrapper.findAll('input')
        expect(inputs.length).toBeGreaterThan(0)

        await inputs[1].setValue('motion_verbs')

        const textareas = wrapper.findAll('textarea')
        await textareas[1].setValue('ir\nvenir')

        const emitted = wrapper.emitted('update:modelValue')
        expect(emitted).toBeTruthy()

        const lastPayload = emitted![emitted!.length - 1][0] as string
        expect(lastPayload).toContain('motion_verbs')
        expect(lastPayload).toContain('venir')
    })
})