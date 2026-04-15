import {expect, test} from '@playwright/test'
import {installMockBridge} from './support/mockBridge'

test.describe('app shell', () => {
    test.beforeEach(async ({page}) => {
        await installMockBridge(page)
        await page.goto('/')
    })

    test('loads app info and default rules workspace', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'NLP Studio E2E', exact: true})).toBeVisible()
        await expect(page.getByText('Version 4.0.0-e2e')).toBeVisible()
        await expect(page.getByText('pong')).toBeVisible()

        await expect(page.getByRole('heading', {name: 'Rules', exact: true})).toBeVisible()
        await expect(page.getByRole('heading', {name: 'Stored agreement rule', exact: true})).toBeVisible()
        await expect(page.getByText('ANNOTATION / conllu')).toBeVisible()
    })

    test('generates and saves an export preview from the File menu', async ({page}) => {
        await page.getByRole('button', {name: 'File', exact: true}).click()

        await expect(page.getByText('Generated export preview')).toBeVisible()

        await page.getByRole('button', {name: 'Generate preview', exact: true}).click()
        await expect(page.getByText('Preview generated: workspace-bundle.json')).toBeVisible()
        await expect(page.getByText('"demo": true')).toBeVisible()

        await page.getByRole('button', {name: 'Save to file', exact: true}).click()
        await expect(page.getByText('Export saved: /tmp/workspace-bundle.json')).toBeVisible()
    })
})