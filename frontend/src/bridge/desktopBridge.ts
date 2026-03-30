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
    listEntries(): string
    listRuleDescriptors(): string
    listRuleSchemas(): string
    getRuleSchema(kind: string, subtype: string): string
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
    if (!window.appBridge) {
        return { success: false, message: 'Desktop bridge unavailable' }
    }

    const fn = window.appBridge[method] as (...params: string[]) => string

    try {
        const raw = fn(...args)
        return JSON.parse(raw) as BridgeResponse<T>
    } catch {
        return { success: false, message: 'Bridge call failed' }
    }
}