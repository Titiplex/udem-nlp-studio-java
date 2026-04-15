import {expect, test} from '@playwright/test'
import {installMockBridge} from './support/mockBridge'

test.describe('rules workspace', () => {
    test.beforeEach(async ({page}) => {
        await installMockBridge(page)
        await page.goto('/')
    })

    test('creates a new rule draft, generates YAML and saves it', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Stored agreement rule', exact: true})).toBeVisible()

        await page.getByRole('button', {name: 'Annotation rule', exact: true}).click()
        await expect(page.getByText('Nouveau draft.')).toBeVisible()

        const nameInput = page.getByLabel('Name')
        const scopeInput = page.getByLabel('Scope')

        await nameInput.fill('E2E created rule')
        await expect(nameInput).toHaveValue('E2E created rule')

        await scopeInput.fill('token')
        await expect(scopeInput).toHaveValue('token')

        await page.getByRole('button', {name: 'Generate YAML', exact: true}).click()
        await expect(page.getByText('YAML généré.')).toBeVisible()

        await page.getByRole('button', {name: 'yaml', exact: true}).click()

        const yamlEditor = page.locator('textarea.yaml-editor')
        await expect(yamlEditor).toBeVisible()
        await expect(yamlEditor).toHaveValue(/name: E2E created rule/)
        await expect(yamlEditor).toHaveValue(/scope: token/)
        await expect(yamlEditor).toHaveValue(/upos: VERB/)

        await page.getByRole('button', {name: 'Save', exact: true}).click()
        await expect(page.getByText('Règle sauvegardée.')).toBeVisible()

        await page.getByRole('button', {name: 'visual', exact: true}).click()
        await expect(page.getByLabel('Name')).toHaveValue('E2E created rule')
    })

    test('validates a rule draft and shows validation feedback', async ({page}) => {
        await page.getByRole('button', {name: 'Annotation rule', exact: true}).click()
        await page.getByRole('button', {name: 'Validate', exact: true}).click()

        await expect(page.getByText('Validation terminée.')).toBeVisible()

        await page.getByRole('button', {name: 'validation', exact: true}).click()
        await expect(page.getByText(/nom est requis/i)).toBeVisible()
    })

    test('parses YAML back into the draft', async ({page}) => {
        await page.getByRole('button', {name: 'Annotation rule', exact: true}).click()
        await page.getByRole('button', {name: 'yaml', exact: true}).click()

        const editor = page.locator('textarea.yaml-editor')
        await editor.fill(`- name: Temporary rule
  scope: token
  set:
    upos: VERB
`)

        await page.getByRole('button', {name: 'Parse YAML', exact: true}).click()
        await expect(page.getByText('YAML parsé.')).toBeVisible()

        await page.getByRole('button', {name: 'visual', exact: true}).click()
        await expect(page.getByLabel('Name')).toHaveValue('Parsed rule')
    })
})