import {expect, test} from '@playwright/test'
import {installMockBridge} from './support/mockBridge'

test.describe('entries workspace', () => {
    test.beforeEach(async ({page}) => {
        await installMockBridge(page)
        await page.goto('/')
        await page.getByRole('button', {name: 'Entries'}).click()
    })

    test('loads entries list and selected entry details', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Entries'})).toBeVisible()
        await expect(page.getByText('2 entrées')).toBeVisible()

        await expect(page.getByText('ix-naq').first()).toBeVisible()
        await expect(page.getByText('A1-ganar').first()).toBeVisible()

        await expect(page.getByRole('heading', {name: 'Entries workspace'})).toBeVisible()
        await expect(page.getByText('Entry #1')).toBeVisible()
    })

    test('runs correction and refreshes CoNLL-U preview', async ({page}) => {
        await page.getByRole('button', {name: 'Run correction'}).click()

        await expect(page.getByText('Correction exécutée.')).toBeVisible()
        await expect(page.getByText('# sent_id = 1')).toBeVisible()
        await expect(page.getByText('# text = ix-naq')).toBeVisible()
    })

    test('imports entries from corpus import and updates the list', async ({page}) => {
        const importTextarea = page.getByPlaceholder(/Ix-naq aj winh/i)

        await importTextarea.fill(
            `new-entry
A1-new
Nouvelle entrée.

second-entry
A1-second
Deuxième entrée.`
        )

        await page.getByRole('button', {name: 'Import append'}).click()

        await expect(page.getByText('3 entrées')).toBeVisible()
        await expect(page.getByText('imported-entry')).not.toBeVisible()
        await expect(page.getByText('second-entry')).toBeVisible()
    })

    test('imports workspace bundle from File menu and refreshes workspace', async ({page}) => {
        await page.getByRole('button', {name: 'File'}).click()
        await page.getByRole('button', {name: 'Import append'}).click()

        await expect(page.getByText('1 entries and 1 rules imported from bundle.')).toBeVisible()

        await page.getByRole('button', {name: 'Entries'}).click()
        await expect(page.getByText('1 entrées')).toBeVisible()
        await expect(page.getByText('imported-entry')).toBeVisible()
    })
})