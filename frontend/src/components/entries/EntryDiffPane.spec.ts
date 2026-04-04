import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import EntryDiffPane from './EntryDiffPane.vue'

describe('EntryDiffPane', () => {
    it('shows changed state when raw and corrected differ', () => {
        const wrapper = mount(EntryDiffPane, {
            props: {
                label: 'Gloss',
                raw: 'A1 ganar',
                corrected: 'A1 B2 ganar',
            },
        })

        expect(wrapper.text()).toContain('Gloss')
        expect(wrapper.text()).toContain('Changed')
        expect(wrapper.text()).toContain('A1 ganar')
        expect(wrapper.text()).toContain('A1 B2 ganar')
    })

    it('shows unchanged state when values are equal', () => {
        const wrapper = mount(EntryDiffPane, {
            props: {
                label: 'Translation',
                raw: 'Il gagne.',
                corrected: 'Il gagne.',
            },
        })

        expect(wrapper.text()).toContain('Unchanged')
    })
})