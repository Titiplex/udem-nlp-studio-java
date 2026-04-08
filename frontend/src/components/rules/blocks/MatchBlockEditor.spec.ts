import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import MatchBlockEditor from './MatchBlockEditor.vue'

describe('MatchBlockEditor', () => {
    it('emits structured gloss object updates', async () => {
        const wrapper = mount(MatchBlockEditor, {
            props: {
                modelValue: {
                    token: 'ix-naq',
                    gloss: {in_lexicon: 'spanish_verbs'},
                },
            },
        })

        const select = wrapper.get('select')
        await select.setValue('object')

        const textareas = wrapper.findAll('textarea')
        await textareas[0].setValue('{"in_lexicon":"motion_verbs"}')

        const emitted = wrapper.emitted('update:modelValue')
        expect(emitted).toBeTruthy()

        const lastPayload = emitted![emitted!.length - 1][0] as Record<string, unknown>
        expect((lastPayload.gloss as Record<string, unknown>).in_lexicon).toBe('motion_verbs')
    })
})