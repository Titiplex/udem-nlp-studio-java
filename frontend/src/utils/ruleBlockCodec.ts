export type JsonObject = Record<string, unknown>

function asObject(value: unknown): JsonObject {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
        return {}
    }
    return value as JsonObject
}

function parseJsonObject(raw: string): JsonObject {
    const normalized = raw.trim()
    if (!normalized) return {}

    const parsed = JSON.parse(normalized)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
        throw new Error('Expected a JSON object')
    }

    return parsed as JsonObject
}

export interface MatchEditorDraft {
    tokenText: string
    glossMode: 'text' | 'object'
    glossText: string
    glossObjectText: string
    requireText: string
    forbidText: string
    extraJsonText: string
}

export interface SetEditorDraft {
    operation: string
    position: string
    upos: string
    staticFeats: Array<{ key: string; value: string }>
    templateFeats: Array<{ key: string; value: string }>
    extractRows: Array<{ type: string; extractor: string; into: string }>
    extraJsonText: string
}

export function parseMatchBlock(value?: JsonObject): MatchEditorDraft {
    const block = asObject(value)
    const gloss = block.gloss
    const require = Array.isArray(block.require) ? block.require.map(String) : []
    const forbid = Array.isArray(block.forbid) ? block.forbid.map(String) : []

    const extra = {...block}
    delete extra.token
    delete extra.gloss
    delete extra.require
    delete extra.forbid

    return {
        tokenText: typeof block.token === 'string' ? block.token : '',
        glossMode: typeof gloss === 'string' || gloss == null ? 'text' : 'object',
        glossText: typeof gloss === 'string' ? gloss : '',
        glossObjectText:
            gloss && typeof gloss === 'object' && !Array.isArray(gloss)
                ? JSON.stringify(gloss, null, 2)
                : '{}',
        requireText: require.join('\n'),
        forbidText: forbid.join('\n'),
        extraJsonText: Object.keys(extra).length > 0 ? JSON.stringify(extra, null, 2) : '{}',
    }
}

export function stringifyMatchBlock(draft: MatchEditorDraft): JsonObject {
    const out: JsonObject = {}

    if (draft.tokenText.trim()) {
        out.token = draft.tokenText.trim()
    }

    if (draft.glossMode === 'text') {
        if (draft.glossText.trim()) {
            out.gloss = draft.glossText.trim()
        }
    } else {
        const parsedGloss = parseJsonObject(draft.glossObjectText)
        if (Object.keys(parsedGloss).length > 0) {
            out.gloss = parsedGloss
        }
    }

    const require = draft.requireText
        .split('\n')
        .map((value) => value.trim())
        .filter(Boolean)
    if (require.length > 0) {
        out.require = require
    }

    const forbid = draft.forbidText
        .split('\n')
        .map((value) => value.trim())
        .filter(Boolean)
    if (forbid.length > 0) {
        out.forbid = forbid
    }

    const extra = parseJsonObject(draft.extraJsonText)
    return {
        ...extra,
        ...out,
    }
}

export function parseSetBlock(value?: JsonObject): SetEditorDraft {
    const block = asObject(value)

    const staticFeatsObject = asObject(block.feats)
    const templateFeatsObject = asObject(block.feats_template ?? block.featsTemplate)
    const extractList = Array.isArray(block.extract) ? block.extract : []

    const extra = {...block}
    delete extra.operation
    delete extra.position
    delete extra.upos
    delete extra.feats
    delete extra.feats_template
    delete extra.featsTemplate
    delete extra.extract

    return {
        operation: typeof block.operation === 'string' ? block.operation : '',
        position: typeof block.position === 'string' ? block.position : '',
        upos: typeof block.upos === 'string' ? block.upos : '',
        staticFeats: Object.entries(staticFeatsObject).map(([key, value]) => ({
            key,
            value: String(value ?? ''),
        })),
        templateFeats: Object.entries(templateFeatsObject).map(([key, value]) => ({
            key,
            value: String(value ?? ''),
        })),
        extractRows: extractList
            .filter((item) => item && typeof item === 'object' && !Array.isArray(item))
            .map((item) => {
                const obj = item as JsonObject
                return {
                    type: typeof obj.type === 'string' ? obj.type : '',
                    extractor: typeof obj.extractor === 'string' ? obj.extractor : '',
                    into: typeof obj.into === 'string' ? obj.into : '',
                }
            }),
        extraJsonText: Object.keys(extra).length > 0 ? JSON.stringify(extra, null, 2) : '{}',
    }
}

export function stringifySetBlock(draft: SetEditorDraft): JsonObject {
    const out: JsonObject = {}

    if (draft.operation.trim()) {
        out.operation = draft.operation.trim()
    }

    if (draft.position.trim()) {
        out.position = draft.position.trim()
    }

    if (draft.upos.trim()) {
        out.upos = draft.upos.trim()
    }

    const feats = Object.fromEntries(
        draft.staticFeats
            .map((row) => [row.key.trim(), row.value.trim()] as const)
            .filter(([key, value]) => key && value),
    )
    if (Object.keys(feats).length > 0) {
        out.feats = feats
    }

    const featsTemplate = Object.fromEntries(
        draft.templateFeats
            .map((row) => [row.key.trim(), row.value.trim()] as const)
            .filter(([key, value]) => key && value),
    )
    if (Object.keys(featsTemplate).length > 0) {
        out.feats_template = featsTemplate
    }

    const extract = draft.extractRows
        .map((row) => ({
            ...(row.type.trim() ? {type: row.type.trim()} : {}),
            ...(row.extractor.trim() ? {extractor: row.extractor.trim()} : {}),
            ...(row.into.trim() ? {into: row.into.trim()} : {}),
        }))
        .filter((row) => Object.keys(row).length > 0)

    if (extract.length > 0) {
        out.extract = extract
    }

    const extra = parseJsonObject(draft.extraJsonText)
    return {
        ...extra,
        ...out,
    }
}