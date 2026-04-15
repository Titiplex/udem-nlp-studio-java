export interface BridgeResponse<T> {
    success: boolean
    message?: string | null
    data?: T
}

export interface AppInfo {
    name: string
    version: string
}

export interface RuleSummary {
    id: string
    name: string
    kind: string
    subtype: string
    scope: string
    enabled: boolean
    priority: number
}

export interface RuleDescriptor {
    kind: string
    subtype: string
    label: string
    description: string
}

export interface ValidationIssue {
    path: string
    level: 'error' | 'warning'
    message: string
}

export interface RuleDetail {
    id: string | null
    name: string
    kind: string
    subtype: string
    scope: string
    enabled: boolean
    priority: number
    description: string
    payload: Record<string, unknown>
    rawYaml: string
}

export interface RuleDraftResult {
    rule: RuleDetail
    issues: ValidationIssue[]
}

export interface FieldDescriptor {
    key: string
    label: string
    type: string
    required: boolean
    repeatable: boolean
    placeholder?: string | null
    helpText?: string | null
    enumValues: string[]
    defaultValue?: Record<string, unknown> | null
    nestedFields: FieldDescriptor[]
}

export interface RuleBuilderSchema {
    kind: string
    subtype: string
    label: string
    description: string
    fields: FieldDescriptor[]
}

export interface EntrySummary {
    id: string
    documentOrder: number
    rawChujText: string
    rawGlossText: string
    translation: string
    approved: boolean
    hasCorrection: boolean
}

export interface EntryDetail {
    id: string | null
    documentOrder: number
    rawChujText: string
    rawGlossText: string
    translation: string
    correctedChujText: string
    correctedGlossText: string
    correctedTranslation: string
    approved: boolean
    conlluPreview: string
}

export interface CorrectionRunRequest {
    entryId: string
    force: boolean
}

export interface WorkspaceImportRequest {
    rawText: string
    replaceExisting: boolean
}

export interface WorkspaceImportResult {
    importedEntries: number
    totalEntries: number
}

export interface BatchCorrectionRequest {
    force: boolean
}

export interface BatchCorrectionResult {
    totalEntries: number
    correctedEntries: number
    skippedApprovedEntries: number
}

export interface WorkspaceExportRequest {
    preferCorrected: boolean
    correctedOnly: boolean
}

export interface WorkspaceExchangeRequest {
    format: string
    preferCorrected: boolean
    correctedOnly: boolean
    ruleKinds: string[]
    onlyEnabledRules: boolean
}

export interface WorkspaceImportFileRequest {
    format: string
    replaceExistingEntries: boolean
    replaceExistingRules: boolean
}

export interface WorkspaceDataImportResult {
    importedEntries: number
    importedRules: number
    summary: string
}

export interface TextExport {
    fileName: string
    content: string
}

export interface AnnotationSettings {
    posDefinitionsYaml: string
    featDefinitionsYaml: string
    lexiconsYaml: string
    extractorsYaml: string
    glossMapYaml: string
    baseYamlPreview: string
    effectiveYamlPreview: string
}

export interface DesktopBridge {
    ping(): string

    getAppInfo(): string

    listRules(): string

    getRule(id: string): string

    listEntries(): string

    getEntry(id: string): string

    saveEntry(payloadJson: string): string

    importEntries(payloadJson: string): string

    runCorrectionOnAll(payloadJson: string): string

    exportRawText(payloadJson: string): string

    exportConllu(payloadJson: string): string

    generateWorkspaceExport(payloadJson: string): string

    saveWorkspaceExport(payloadJson: string): string

    importWorkspaceFromFile(payloadJson: string): string

    getAnnotationSettings(): string

    saveAnnotationSettings(payloadJson: string): string

    listRuleDescriptors(): string

    listRuleSchemas(): string

    getRuleSchema(kind: string, subtype: string): string

    parseRuleYaml(payloadJson: string): string

    generateRuleYaml(payloadJson: string): string

    validateRule(payloadJson: string): string

    saveRule(payloadJson: string): string

    runCorrection(payloadJson: string): string
}

declare global {
    interface Window {
        appBridge?: DesktopBridge
    }
}

export function callBridge<T>(
    method: keyof DesktopBridge,
    ...args: string[]
): BridgeResponse<T> {
    const bridge = window.appBridge

    if (!bridge) {
        console.error(`[bridge] window.appBridge is missing`)
        return {success: false, message: 'Desktop bridge unavailable'}
    }

    const candidate = bridge[method] as unknown

    if (typeof candidate !== 'function') {
        console.error(`[bridge] method missing: ${String(method)}`, bridge)
        return {success: false, message: `Bridge method missing: ${String(method)}`}
    }

    try {
        const raw = Reflect.apply(
            candidate as (...params: string[]) => string,
            bridge,
            args,
        )

        if (typeof raw !== 'string') {
            return {
                success: false,
                message: `Bridge method ${String(method)} did not return a string`,
            }
        }

        return JSON.parse(raw) as BridgeResponse<T>
    } catch (error) {
        console.error(`[bridge] ${String(method)} failed`, error)
        return {
            success: false,
            message: `Bridge call failed: ${String(method)}`,
        }
    }
}

export async function waitForBridge(timeoutMs = 3000): Promise<boolean> {
    const start = Date.now()

    while (Date.now() - start < timeoutMs) {
        if (window.appBridge) {
            return true
        }
        await new Promise((resolve) => setTimeout(resolve, 50))
    }

    return false
}