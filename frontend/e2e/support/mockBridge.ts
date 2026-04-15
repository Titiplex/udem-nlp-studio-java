import type {Page} from '@playwright/test'

type BridgeFixtureOptions = {
    appName?: string
    appVersion?: string
}

export async function installMockBridge(page: Page, options: BridgeFixtureOptions = {}) {
    await page.addInitScript((initOptions: BridgeFixtureOptions) => {
        type RuleSummary = {
            id: string
            name: string
            kind: string
            subtype: string
            scope: string
            enabled: boolean
            priority: number
        }

        type RuleDetail = RuleSummary & {
            description: string
            payload: Record<string, unknown>
            rawYaml: string
        }

        type EntrySummary = {
            id: string
            documentOrder: number
            rawChujText: string
            rawGlossText: string
            translation: string
            approved: boolean
            hasCorrection: boolean
        }

        type EntryDetail = {
            id: string | null
            documentOrder: number
            contextText: string
            surfaceText: string
            rawChujText: string
            rawGlossText: string
            translation: string
            comments: string
            correctedChujText: string
            correctedGlossText: string
            correctedTranslation: string
            approved: boolean
            conlluPreview: string
        }

        const state = {
            appName: initOptions.appName ?? 'NLP Studio E2E',
            appVersion: initOptions.appVersion ?? '4.0.0-e2e',

            descriptors: [
                {
                    kind: 'ANNOTATION',
                    subtype: 'conllu',
                    label: 'Annotation rule',
                    description: 'Annotation rule builder',
                },
                {
                    kind: 'CORRECTION',
                    subtype: 'split',
                    label: 'Split correction rule',
                    description: 'Split correction rule builder',
                },
            ],

            schemas: {
                'ANNOTATION::conllu': {
                    kind: 'ANNOTATION',
                    subtype: 'conllu',
                    label: 'Annotation schema',
                    description: 'Schema used for annotation rules',
                    fields: [],
                },
                'CORRECTION::split': {
                    kind: 'CORRECTION',
                    subtype: 'split',
                    label: 'Split schema',
                    description: 'Schema used for split rules',
                    fields: [],
                },
            } as Record<string, unknown>,

            rules: [
                {
                    id: 'rule-1',
                    name: 'Stored agreement rule',
                    kind: 'ANNOTATION',
                    subtype: 'conllu',
                    scope: 'token',
                    enabled: true,
                    priority: 20,
                },
            ] as RuleSummary[],

            ruleDetails: {
                'rule-1': {
                    id: 'rule-1',
                    name: 'Stored agreement rule',
                    kind: 'ANNOTATION',
                    subtype: 'conllu',
                    scope: 'token',
                    enabled: true,
                    priority: 20,
                    description: 'Existing annotation rule',
                    payload: {
                        set: {
                            upos: 'VERB',
                        },
                    },
                    rawYaml: `- name: Stored agreement rule
  scope: token
  set:
    upos: VERB
`,
                },
            } as Record<string, RuleDetail>,

            entries: [
                {
                    id: 'entry-1',
                    documentOrder: 1,
                    rawChujText: 'ix-naq',
                    rawGlossText: 'A1-ganar',
                    translation: 'Il gagne.',
                    approved: false,
                    hasCorrection: false,
                },
                {
                    id: 'entry-2',
                    documentOrder: 2,
                    rawChujText: 'ha-ix-to',
                    rawGlossText: 'DEM-A1-ir',
                    translation: 'Celui-ci va.',
                    approved: false,
                    hasCorrection: true,
                },
            ] as EntrySummary[],

            entryDetails: {
                'entry-1': {
                    id: 'entry-1',
                    documentOrder: 1,
                    contextText: '',
                    surfaceText: '',
                    rawChujText: 'ix-naq',
                    rawGlossText: 'A1-ganar',
                    translation: 'Il gagne.',
                    comments: '',
                    correctedChujText: '',
                    correctedGlossText: '',
                    correctedTranslation: '',
                    approved: false,
                    conlluPreview: '',
                },
                'entry-2': {
                    id: 'entry-2',
                    documentOrder: 2,
                    contextText: '',
                    surfaceText: '',
                    rawChujText: 'ha-ix-to',
                    rawGlossText: 'DEM-A1-ir',
                    translation: 'Celui-ci va.',
                    comments: '',
                    correctedChujText: 'ha-ix-to',
                    correctedGlossText: 'DEM-A1-ir',
                    correctedTranslation: 'Celui-ci va.',
                    approved: false,
                    conlluPreview: '# sent_id = 2\n# text = ha-ix-to',
                },
            } as Record<string, EntryDetail>,

            annotationSettings: {
                posDefinitionsYaml: '- VERB\n- DET\n- NOUN',
                featDefinitionsYaml: '- Pers[subj]\n- Number[subj]',
                lexiconsYaml: 'spanish_verbs:\n  - ganar\n  - ir',
                extractorsYaml: 'agreement_verbs:\n  tag_schema: {}',
                glossMapYaml: '{}',
                baseYamlPreview: 'def:\n  pos:\n    - VERB',
                effectiveYamlPreview: 'def:\n  pos:\n    - VERB\nrules:\n  - name: Stored agreement rule',
            },

            exportPreview: {
                fileName: 'workspace-bundle.json',
                content: '{\n  "demo": true,\n  "kind": "bundle"\n}',
            },

            savedExportPath: '/tmp/workspace-bundle.json',
            bridgeCalls: [] as Array<{ method: string; args: string[] }>,
        }

        function ok(data: unknown) {
            return JSON.stringify({success: true, data})
        }

        function error(message: string) {
            return JSON.stringify({success: false, message})
        }

        function record(method: string, args: string[]) {
            state.bridgeCalls.push({method, args})
            ;(window as any).__bridgeCalls = state.bridgeCalls
        }

        function summarizeRule(rule: RuleDetail): RuleSummary {
            return {
                id: String(rule.id),
                name: rule.name,
                kind: rule.kind,
                subtype: rule.subtype,
                scope: rule.scope,
                enabled: rule.enabled,
                priority: rule.priority,
            }
        }

        function upsertEntry(detail: EntryDetail) {
            state.entryDetails[String(detail.id)] = detail

            const summary: EntrySummary = {
                id: String(detail.id),
                documentOrder: detail.documentOrder,
                rawChujText: detail.rawChujText,
                rawGlossText: detail.rawGlossText,
                translation: detail.translation,
                approved: detail.approved,
                hasCorrection: !!(
                    detail.correctedChujText ||
                    detail.correctedGlossText ||
                    detail.correctedTranslation
                ),
            }

            const index = state.entries.findIndex((entry) => entry.id === summary.id)
            if (index >= 0) {
                state.entries[index] = summary
            } else {
                state.entries.push(summary)
            }

            state.entries.sort((a, b) => a.documentOrder - b.documentOrder || a.id.localeCompare(b.id))
        }

        ;(window as any).__bridgeState = state

        window.appBridge = {
            ping() {
                record('ping', [])
                return ok('pong')
            },

            getAppInfo() {
                record('getAppInfo', [])
                return ok({name: state.appName, version: state.appVersion})
            },

            listRuleDescriptors() {
                record('listRuleDescriptors', [])
                return ok(state.descriptors)
            },

            listRuleSchemas() {
                record('listRuleSchemas', [])
                return ok(Object.values(state.schemas))
            },

            getRuleSchema(kind: string, subtype: string) {
                record('getRuleSchema', [kind, subtype])
                return ok(state.schemas[`${kind}::${subtype}`] ?? null)
            },

            listRules() {
                record('listRules', [])
                return ok(state.rules)
            },

            getRule(id: string) {
                record('getRule', [id])
                const rule = state.ruleDetails[id]
                return rule ? ok(rule) : error(`Rule not found: ${id}`)
            },

            parseRuleYaml(payloadJson: string) {
                record('parseRuleYaml', [payloadJson])
                const draft = JSON.parse(payloadJson)
                const parsed = {
                    ...draft,
                    name: 'Parsed rule',
                    payload: {
                        set: {
                            upos: 'VERB',
                        },
                    },
                }
                return ok({rule: parsed, issues: []})
            },

            generateRuleYaml(payloadJson: string) {
                record('generateRuleYaml', [payloadJson])
                const draft = JSON.parse(payloadJson)
                const yaml = `- name: ${draft.name || 'Untitled rule'}
  scope: ${draft.scope || 'token'}
  set:
    upos: VERB
`
                return ok({
                    rule: {
                        ...draft,
                        rawYaml: yaml,
                    },
                    issues: [],
                })
            },

            validateRule(payloadJson: string) {
                record('validateRule', [payloadJson])
                const draft = JSON.parse(payloadJson)
                const issues = draft.name
                    ? []
                    : [{path: 'name', level: 'error', message: 'Le nom est requis.'}]
                return ok({rule: draft, issues})
            },

            saveRule(payloadJson: string) {
                record('saveRule', [payloadJson])
                const draft = JSON.parse(payloadJson)

                const id = draft.id ?? `rule-${state.rules.length + 1}`
                const saved: RuleDetail = {
                    ...draft,
                    id,
                }

                state.ruleDetails[id] = saved
                const summary = summarizeRule(saved)
                const existingIndex = state.rules.findIndex((rule) => rule.id === id)
                if (existingIndex >= 0) {
                    state.rules[existingIndex] = summary
                } else {
                    state.rules.push(summary)
                }

                return ok({
                    rule: saved,
                    issues: [],
                })
            },

            listEntries() {
                record('listEntries', [])
                return ok(state.entries)
            },

            getEntry(id: string) {
                record('getEntry', [id])
                const entry = state.entryDetails[id]
                return entry ? ok(entry) : error(`Entry not found: ${id}`)
            },

            saveEntry(payloadJson: string) {
                record('saveEntry', [payloadJson])
                const draft = JSON.parse(payloadJson)
                const id = draft.id ?? `entry-${state.entries.length + 1}`
                const saved: EntryDetail = {
                    ...draft,
                    id,
                }

                upsertEntry(saved)
                return ok(saved)
            },

            importEntries(payloadJson: string) {
                record('importEntries', [payloadJson])
                const payload = JSON.parse(payloadJson) as { rawText: string; replaceExisting: boolean }
                const blocks = payload.rawText
                    .trim()
                    .split(/\n\s*\n/g)
                    .map((block) => block.split('\n').map((line) => line.trim()))
                    .filter((lines) => lines.length >= 3)

                if (payload.replaceExisting) {
                    state.entries = []
                    state.entryDetails = {}
                }

                for (const lines of blocks) {
                    const id = `entry-${state.entries.length + 1}`
                    const detail: EntryDetail = {
                        id,
                        documentOrder: state.entries.length + 1,
                        contextText: '',
                        surfaceText: '',
                        rawChujText: lines[0] ?? '',
                        rawGlossText: lines[1] ?? '',
                        translation: lines.slice(2).join(' '),
                        comments: '',
                        correctedChujText: '',
                        correctedGlossText: '',
                        correctedTranslation: '',
                        approved: false,
                        conlluPreview: '',
                    }
                    upsertEntry(detail)
                }

                return ok({
                    importedEntries: blocks.length,
                    totalEntries: state.entries.length,
                })
            },

            runCorrection(payloadJson: string) {
                record('runCorrection', [payloadJson])
                const payload = JSON.parse(payloadJson) as { entryId: string; force: boolean }
                const base = state.entryDetails[payload.entryId]
                if (!base) return error(`Entry not found: ${payload.entryId}`)

                const corrected: EntryDetail = {
                    ...base,
                    correctedChujText: base.rawChujText,
                    correctedGlossText: base.rawGlossText,
                    correctedTranslation: base.translation,
                    conlluPreview: `# sent_id = ${base.documentOrder}\n# text = ${base.rawChujText}`,
                }

                upsertEntry(corrected)
                return ok(corrected)
            },

            runCorrectionOnAll(payloadJson: string) {
                record('runCorrectionOnAll', [payloadJson])

                for (const entry of state.entries) {
                    const detail = state.entryDetails[entry.id]
                    if (!detail) continue

                    const corrected: EntryDetail = {
                        ...detail,
                        correctedChujText: detail.rawChujText,
                        correctedGlossText: detail.rawGlossText,
                        correctedTranslation: detail.translation,
                        conlluPreview: `# sent_id = ${detail.documentOrder}\n# text = ${detail.rawChujText}`,
                    }
                    upsertEntry(corrected)
                }

                return ok({
                    totalEntries: state.entries.length,
                    correctedEntries: state.entries.length,
                    skippedApprovedEntries: 0,
                })
            },

            exportRawText(payloadJson: string) {
                record('exportRawText', [payloadJson])
                const content = state.entries
                    .map((entry) => `${entry.rawChujText}\n${entry.rawGlossText}\n${entry.translation}`)
                    .join('\n\n')
                return ok({
                    fileName: 'workspace.txt',
                    content,
                })
            },

            exportConllu(payloadJson: string) {
                record('exportConllu', [payloadJson])
                const content = state.entries
                    .map((entry) => `# sent_id = ${entry.documentOrder}\n# text = ${entry.rawChujText}`)
                    .join('\n\n')
                return ok({
                    fileName: 'workspace.conllu',
                    content,
                })
            },

            generateWorkspaceExport(payloadJson: string) {
                record('generateWorkspaceExport', [payloadJson])
                return ok(state.exportPreview)
            },

            saveWorkspaceExport(payloadJson: string) {
                record('saveWorkspaceExport', [payloadJson])
                return ok(state.savedExportPath)
            },

            importWorkspaceFromFile(payloadJson: string) {
                record('importWorkspaceFromFile', [payloadJson])

                state.entries = [
                    {
                        id: 'imported-1',
                        documentOrder: 1,
                        rawChujText: 'imported-entry',
                        rawGlossText: 'A1-import',
                        translation: 'Imported entry.',
                        approved: false,
                        hasCorrection: false,
                    },
                ]

                state.entryDetails = {
                    'imported-1': {
                        id: 'imported-1',
                        documentOrder: 1,
                        contextText: '',
                        surfaceText: '',
                        rawChujText: 'imported-entry',
                        rawGlossText: 'A1-import',
                        translation: 'Imported entry.',
                        comments: '',
                        correctedChujText: '',
                        correctedGlossText: '',
                        correctedTranslation: '',
                        approved: false,
                        conlluPreview: '',
                    },
                }

                return ok({
                    importedEntries: 1,
                    importedRules: 1,
                    summary: '1 entries and 1 rules imported from bundle.',
                })
            },

            getAnnotationSettings() {
                record('getAnnotationSettings', [])
                return ok(state.annotationSettings)
            },

            saveAnnotationSettings(payloadJson: string) {
                record('saveAnnotationSettings', [payloadJson])
                state.annotationSettings = JSON.parse(payloadJson)
                return ok(state.annotationSettings)
            },
        } as any
    }, options)
}