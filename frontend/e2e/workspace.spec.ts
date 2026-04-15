import {expect, test} from '@playwright/test'
import {installMockBridge} from './support/mockBridge'

test.describe('entries workspace', () => {
    test.beforeEach(async ({page}) => {
        await installMockBridge(page)
        await page.goto('/')
        await page.getByRole('button', {name: 'Entries', exact: true}).click()
    })

    test('loads entries list and selected entry details', async ({page}) => {
        await expect(page.getByRole('heading', {name: 'Entries', exact: true})).toBeVisible()
        await expect(page.getByText('2 entrées')).toBeVisible()

        await expect(page.getByText('ix-naq').first()).toBeVisible()
        await expect(page.getByText('A1-ganar').first()).toBeVisible()

        await expect(page.getByRole('heading', {name: 'Entries workspace', exact: true})).toBeVisible()
        await expect(page.getByText('Entry #1')).toBeVisible()
    })

    test('runs correction and refreshes CoNLL-U preview', async ({page}) => {
        await page.getByRole('button', {name: 'Run correction', exact: true}).click()

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

        await page.getByRole('main').getByRole('button', {name: 'Import append', exact: true}).click()

        await expect(page.getByText('4 entrées')).toBeVisible()
        await expect(page.getByText('new-entry')).toBeVisible()
        await expect(page.getByText('second-entry')).toBeVisible()
    })

    test('imports workspace bundle from File menu and refreshes workspace', async ({page}) => {
        const fileButton = page.getByRole('button', {name: 'File', exact: true})

        await fileButton.click()
        await page.getByRole('banner').getByRole('button', {name: 'Import append', exact: true}).click()

        await expect(page.getByText('1 entries and 1 rules imported from bundle.')).toBeVisible()

        await fileButton.click() // ferme le menu ouvert qui bloque les clics sur la nav

        await page.getByRole('button', {name: 'Entries', exact: true}).click()
        await expect(page.getByText('1 entrées')).toBeVisible()
        await expect(page.getByText('imported-entry')).toBeVisible()
    })
})