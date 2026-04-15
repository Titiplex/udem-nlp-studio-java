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

        const subblocks = wrapper.findAll('.subblock')
        expect(subblocks.length).toBeGreaterThanOrEqual(3)

        const templateBlock = subblocks.find((block) =>
            block.text().includes('Feature templates'),
        )
        expect(templateBlock).toBeTruthy()

        const addTemplateButton = templateBlock!.find('button')
        await addTemplateButton.trigger('click')

        const templateInputs = templateBlock!.findAll('input')
        expect(templateInputs.length).toBeGreaterThanOrEqual(2)

        await templateInputs[0].setValue('Pers[subj]')
        await templateInputs[1].setValue('{agreement_verbs.A.person}')

        const extractBlock = subblocks.find((block) =>
            block.text().includes('Extract'),
        )
        expect(extractBlock).toBeTruthy()

        const addExtractButton = extractBlock!.find('button')
        await addExtractButton.trigger('click')

        const extractInputs = extractBlock!.findAll('input')
        expect(extractInputs.length).toBeGreaterThanOrEqual(3)

        await extractInputs[0].setValue('scan_agreement')
        await extractInputs[1].setValue('agreement_verbs')
        await extractInputs[2].setValue('agreement_verbs')

        const emitted = wrapper.emitted('update:modelValue')
        expect(emitted).toBeTruthy()

        const lastPayload = emitted![emitted!.length - 1][0] as Record<string, unknown>

        const templates = lastPayload.feats_template as Record<string, unknown> | undefined
        expect(templates).toBeTruthy()
        expect(templates?.['Pers[subj]']).toBe('{agreement_verbs.A.person}')

        const extract = lastPayload.extract as Array<Record<string, unknown>> | undefined
        expect(extract).toBeTruthy()
        expect(extract?.[0]?.type).toBe('scan_agreement')
        expect(extract?.[0]?.extractor).toBe('agreement_verbs')
        expect(extract?.[0]?.into).toBe('agreement_verbs')
    })
})