import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from './lib'

describe('API client', () => {
  afterEach(() => vi.restoreAllMocks())

  it('encodes catalog search terms', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ content: [], totalElements: 0 }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await api.list('owner & platform')

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/services?size=100&sort=name,asc&query=owner%20%26%20platform',
      expect.any(Object),
    )
  })

  it('surfaces problem details returned by the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ detail: 'A service with that name already exists' }), {
        status: 409,
        headers: { 'Content-Type': 'application/problem+json' },
      }),
    )

    await expect(api.list()).rejects.toThrow('A service with that name already exists')
  })

  it('sends the CSRF cookie when updating a service', async () => {
    Object.defineProperty(globalThis, 'document', { configurable: true, value: { cookie: 'XSRF-TOKEN=csrf-token' } })
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ id: 'service-id' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    )

    await api.update('service-id', {
      name: 'orders-api', description: 'Orders', owners: ['Owner'], teams: ['Team'], lifecycle: 'production',
      repositoryUrl: 'https://example.com/repository', tags: [], destinations: [],
    })

    expect(fetchMock).toHaveBeenCalledWith('/api/services/service-id', expect.objectContaining({
      headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'csrf-token' }),
    }))
    Reflect.deleteProperty(globalThis, 'document')
  })

  it('exposes the reusable catalog export URL', () => {
    expect(api.exportCatalogUrl).toBe('/api/import/export')
  })
})
