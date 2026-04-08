import {describe, expect, it} from 'vitest'
import {parseMatchBlock, parseSetBlock, stringifyMatchBlock, stringifySetBlock,} from './ruleBlockCodec'

describe('ruleBlockCodec', () => {
    it('parses and stringifies a simple match block', () => {
        const draft = parseMatchBlock({
            token: 'ix-naq',
            gloss: {in_lexicon: 'spanish_verbs'},
            require: ['agreement_verbs.A.person'],
        })

        expect(draft.tokenText).toBe('ix-naq')
        expect(draft.glossMode).toBe('object')

        const block = stringifyMatchBlock(draft)
        expect(block.token).toBe('ix-naq')
        expect((block.gloss as Record<string, unknown>).in_lexicon).toBe('spanish_verbs')
    })

    it('parses and stringifies a set block with feats and extract', () => {
        const draft = parseSetBlock({
            upos: 'VERB',
            feats_template: {
                'Pers[subj]': '{agreement_verbs.A.person}',
            },
            extract: [
                {type: 'scan_agreement', extractor: 'agreement_verbs', into: 'agreement_verbs'},
            ],
        })

        expect(draft.upos).toBe('VERB')
        expect(draft.templateFeats).toHaveLength(1)
        expect(draft.extractRows).toHaveLength(1)

        const block = stringifySetBlock(draft)
        expect(block.upos).toBe('VERB')
        expect((block.feats_template as Record<string, unknown>)['Pers[subj]']).toBe('{agreement_verbs.A.person}')
        expect(Array.isArray(block.extract)).toBe(true)
    })
})