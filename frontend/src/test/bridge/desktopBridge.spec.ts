import {describe, expect, it, vi} from 'vitest'
import {callBridge, waitForBridge} from '../../bridge/desktopBridge'

describe('desktopBridge', () => {
    it('returns an error when window.appBridge is missing', () => {
        delete window.appBridge

        const result = callBridge('ping')

        expect(result.success).toBe(false)
        expect(result.message).toContain('Desktop bridge unavailable')
    })

    it('calls a bridge method and parses successful JSON', () => {
        window.appBridge = {
            ping: () => JSON.stringify({success: true, data: 'pong'}),
        } as any

        const result = callBridge<string>('ping')

        expect(result.success).toBe(true)
        expect(result.data).toBe('pong')
    })

    it('returns an error when the bridge method is missing', () => {
        window.appBridge = {} as any

        const result = callBridge('ping')

        expect(result.success).toBe(false)
        expect(result.message).toContain('Bridge method missing')
    })

    it('returns an error when the bridge method throws', () => {
        window.appBridge = {
            ping: () => {
                throw new Error('boom')
            },
        } as any

        const result = callBridge('ping')

        expect(result.success).toBe(false)
        expect(result.message).toContain('Bridge call failed')
    })

    it('returns an error when the bridge method does not return a string', () => {
        window.appBridge = {
            ping: () => 42 as any,
        } as any

        const result = callBridge('ping')

        expect(result.success).toBe(false)
        expect(result.message).toContain('did not return a string')
    })

    it('returns an error when the bridge returns invalid JSON', () => {
        window.appBridge = {
            ping: () => 'not-json',
        } as any

        const result = callBridge('ping')

        expect(result.success).toBe(false)
        expect(result.message).toContain('Bridge call failed')
    })

    it('waitForBridge resolves true when bridge appears before timeout', async () => {
        delete window.appBridge

        setTimeout(() => {
            window.appBridge = {
                ping: () => JSON.stringify({success: true, data: 'pong'}),
            } as any
        }, 20)

        const ready = await waitForBridge(200)

        expect(ready).toBe(true)
    })

    it('waitForBridge resolves false on timeout', async () => {
        delete window.appBridge

        const ready = await waitForBridge(80)

        expect(ready).toBe(false)
    })

    it('passes arguments to the desktop bridge method', () => {
        const spy = vi.fn(() => JSON.stringify({success: true, data: 'ok'}))

        window.appBridge = {
            getRule: spy,
        } as any

        const result = callBridge('getRule', '123')

        expect(result.success).toBe(true)
        expect(spy).toHaveBeenCalledWith('123')
    })
})