import {expect, test} from '@playwright/test'
import {installMockBridge} from './support/mockBridge'

test.describe('rules workspace', () => {
    test.beforeEach(async ({page}) => {
        await installMockBridge(page)
        await page.goto('/')
    })

    test('creates a new rule draft, generates YAML and saves it', async ({page}) => {
        await expect(page.getByText('Stored agreement rule')).toBeVisible()

        await page.getByRole('button', {name: 'Annotation rule'}).click()
        await expect(page.getByText('Nouveau draft.')).toBeVisible()

        await page.getByLabel('Name').fill('E2E created rule')
        await page.getByLabel('Scope').fill('token')

        await page.getByRole('button', {name: 'Generate YAML'}).click()
        await expect(page.getByText('YAML généré.')).toBeVisible()

        await page.getByRole('button', {name: 'yaml'}).click()
        await expect(page.locator('textarea.yaml-editor')).toContainText('E2E created rule')

        await page.getByRole('button', {name: 'Save'}).click()
        await expect(page.getByText('Règle sauvegardée.')).toBeVisible()

        await expect(page.getByText('E2E created rule')).toBeVisible()
    })

    test('validates a rule draft and shows validation feedback', async ({page}) => {
        await page.getByRole('button', {name: 'Annotation rule'}).click()
        await page.getByRole('button', {name: 'Validate'}).click()

        await expect(page.getByText('Validation terminée.')).toBeVisible()

        await page.getByRole('button', {name: 'validation'}).click()
        await expect(page.getByText(/nom est requis/i)).toBeVisible()
    })

    test('parses YAML back into the draft', async ({page}) => {
        await page.getByRole('button', {name: 'Annotation rule'}).click()
        await page.getByRole('button', {name: 'yaml'}).click()

        const editor = page.locator('textarea.yaml-editor')
        await editor.fill(`- name: Temporary rule
  scope: token
  set:
    upos: VERB
`)

        await page.getByRole('button', {name: 'Parse YAML'}).click()
        await expect(page.getByText('YAML parsé.')).toBeVisible()

        await page.getByRole('button', {name: 'visual'}).click()
        await expect(page.getByLabel('Name')).toHaveValue('Parsed rule')
    })
})