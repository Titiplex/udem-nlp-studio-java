import {describe, expect, it} from 'vitest'
import {
    parseExtractorsYaml,
    parseLexiconsYaml,
    parseYamlList,
    stringifyExtractorsYaml,
    stringifyLexiconsYaml,
    stringifyYamlList,
} from './annotationSettingsCodec'

describe('annotationSettingsCodec', () => {
    it('parses and stringifies yaml lists', () => {
        const parsed = parseYamlList(`
- VERB
- NOUN
    `.trim())

        expect(parsed).toEqual(['VERB', 'NOUN'])

        const stringified = stringifyYamlList(parsed)
        expect(stringified).toContain('VERB')
        expect(stringified).toContain('NOUN')
    })

    it('parses and stringifies lexicons', () => {
        const parsed = parseLexiconsYaml(`
spanish_verbs:
  - ganar
  - ir
    `.trim())

        expect(parsed).toHaveLength(1)
        expect(parsed[0].name).toBe('spanish_verbs')
        expect(parsed[0].valuesText).toContain('ganar')

        const stringified = stringifyLexiconsYaml(parsed)
        expect(stringified).toContain('spanish_verbs')
        expect(stringified).toContain('ganar')
    })

    it('parses and stringifies extractors', () => {
        const parsed = parseExtractorsYaml(`
agreement_verbs:
  tag_schema:
    series:
      A: "subj"
      B: "obj"
    values:
      person: [ "1", "2", "3" ]
      number:
        suffix: "PL"
    `.trim())

        expect(parsed).toHaveLength(1)
        expect(parsed[0].name).toBe('agreement_verbs')
        expect(parsed[0].seriesRows).toHaveLength(2)
        expect(parsed[0].personsText).toContain('1')
        expect(parsed[0].numberSuffix).toBe('PL')

        const stringified = stringifyExtractorsYaml(parsed)
        expect(stringified).toContain('agreement_verbs')
        expect(stringified).toContain('suffix: PL')
    })
})