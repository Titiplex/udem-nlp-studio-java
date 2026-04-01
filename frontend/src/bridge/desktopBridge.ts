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

export interface DesktopBridge {
    ping(): string

    getAppInfo(): string

    listRules(): string

    getRule(id: string): string

    listEntries(): string

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
            args
        )

        console.debug(`[bridge] ${String(method)} ->`, raw)

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