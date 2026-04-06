import yaml from 'js-yaml'

export interface LexiconDraft {
    name: string
    valuesText: string
}

export interface ExtractorSeriesRow {
    key: string
    label: string
}

export interface ExtractorDraft {
    name: string
    seriesRows: ExtractorSeriesRow[]
    personsText: string
    numberSuffix: string
    extraRoot: Record<string, unknown>
    extraTagSchema: Record<string, unknown>
    extraValues: Record<string, unknown>
}

function dumpYaml(value: unknown): string {
    return yaml.dump(value, {
        noRefs: true,
        lineWidth: 120,
    }).trim()
}

function asObject(value: unknown): Record<string, unknown> {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
        return {}
    }
    return value as Record<string, unknown>
}

export function parseYamlList(raw: string): string[] {
    const normalized = raw.trim()
    if (!normalized) return []

    const loaded = yaml.load(normalized)
    if (!Array.isArray(loaded)) {
        throw new Error('Expected a YAML list')
    }

    return loaded
        .map((item) => String(item ?? '').trim())
        .filter(Boolean)
}

export function stringifyYamlList(items: string[]): string {
    const normalized = items.map((item) => item.trim()).filter(Boolean)
    return dumpYaml(normalized)
}

export function parseLexiconsYaml(raw: string): LexiconDraft[] {
    const normalized = raw.trim()
    if (!normalized) return []

    const loaded = yaml.load(normalized)
    const root = asObject(loaded)

    return Object.entries(root).map(([name, value]) => {
        let values: string[]

        if (Array.isArray(value)) {
            values = value.map((item) => String(item ?? '').trim()).filter(Boolean)
        } else if (value == null) {
            values = []
        } else {
            values = [String(value).trim()].filter(Boolean)
        }

        return {
            name,
            valuesText: values.join('\n'),
        }
    })
}

export function stringifyLexiconsYaml(items: LexiconDraft[]): string {
    const root: Record<string, string[]> = {}

    for (const item of items) {
        const name = item.name.trim()
        if (!name) continue

        const values = item.valuesText
            .split('\n')
            .map((value) => value.trim())
            .filter(Boolean)

        root[name] = values
    }

    return dumpYaml(root)
}

export function parseExtractorsYaml(raw: string): ExtractorDraft[] {
    const normalized = raw.trim()
    if (!normalized) return []

    const loaded = yaml.load(normalized)
    const root = asObject(loaded)

    return Object.entries(root).map(([name, value]) => {
        const extractor = asObject(value)
        const tagSchema = asObject(extractor.tag_schema)
        const series = asObject(tagSchema.series)
        const values = asObject(tagSchema.values)
        const number = asObject(values.number)

        const extraRoot = { ...extractor }
        delete extraRoot.tag_schema

        const extraTagSchema = { ...tagSchema }
        delete extraTagSchema.series
        delete extraTagSchema.values

        const extraValues = { ...values }
        delete extraValues.person
        delete extraValues.number

        return {
            name,
            seriesRows: Object.entries(series).map(([key, label]) => ({
                key,
                label: String(label ?? ''),
            })),
            personsText: Array.isArray(values.person)
                ? values.person.map((item) => String(item ?? '').trim()).filter(Boolean).join('\n')
                : '',
            numberSuffix: typeof number.suffix === 'string' ? number.suffix : 'PL',
            extraRoot,
            extraTagSchema,
            extraValues,
        }
    })
}

export function stringifyExtractorsYaml(items: ExtractorDraft[]): string {
    const root: Record<string, unknown> = {}

    for (const item of items) {
        const name = item.name.trim()
        if (!name) continue

        const series: Record<string, string> = {}
        for (const row of item.seriesRows) {
            const key = row.key.trim()
            const label = row.label.trim()
            if (!key || !label) continue
            series[key] = label
        }

        const persons = item.personsText
            .split('\n')
            .flatMap((value) => value.split(','))
            .map((value) => value.trim())
            .filter(Boolean)

        root[name] = {
            ...item.extraRoot,
            tag_schema: {
                ...item.extraTagSchema,
                series,
                values: {
                    ...item.extraValues,
                    person: persons,
                    number: {
                        suffix: item.numberSuffix.trim() || 'PL',
                    },
                },
            },
        }
    }

    return dumpYaml(root)
}