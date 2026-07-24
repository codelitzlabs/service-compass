export type Lifecycle = 'experimental' | 'production' | 'deprecated'
export type DestinationLabel = string

export interface Environment { id: string; name: string; color: string }
export interface Team { id: string; name: string; description: string; owners: string[] }
export interface TeamSummary { id: string; name: string }
export interface DestinationAccount { id?: string; label: string; identifier: string; authenticationMethod: string }
export interface DestinationLink {
  id: string; url: string; environment?: Environment; authenticationMethod?: string; accountIdentifier?: string;
  accessNotes?: string; accessUrl?: string; accounts: DestinationAccount[]
}
export interface Destination { id: string; name: string; label: DestinationLabel; links: DestinationLink[] }
export interface DestinationLinkInput {
  url: string; environment?: string; authenticationMethod?: string; accountIdentifier?: string; accessNotes?: string; accessUrl?: string
  accounts: DestinationAccount[]
}
export interface DestinationInput { name: string; label: DestinationLabel; links: DestinationLinkInput[] }
export interface Service {
  id: string; name: string; description: string; owners: string[]; teams: TeamSummary[]; lifecycle: Lifecycle; repositoryUrl: string;
  tags: string[]; destinations: Destination[]; createdAt: string; updatedAt: string
}
export interface ServiceInput {
  name: string; description: string; owners: string[]; teams: string[]; lifecycle: Lifecycle; repositoryUrl: string;
  tags: string[]; destinations: DestinationInput[]
}
interface Page<T> { content: T[]; totalElements: number }
export interface AppContext { companyName: string }
export interface CatalogImportResult { teams: number; services: number }

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const json = !(init?.body instanceof FormData)
  const csrfToken = readCookie('XSRF-TOKEN')
  const response = await fetch(`/api${path}`, { ...init, headers: {
    ...(json ? { 'Content-Type': 'application/json' } : {}),
    ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}),
    ...init?.headers,
  } })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Request failed (${response.status})`)
  }
  return response.status === 204 ? undefined as T : response.json()
}

function readCookie(name: string): string | undefined {
  if (typeof document === 'undefined') return undefined
  const prefix = `${encodeURIComponent(name)}=`
  const cookie = document.cookie.split('; ').find(value => value.startsWith(prefix))
  return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : undefined
}

export const api = {
  context: () => request<AppContext>('/context'),
  list: (query = '') => request<Page<Service>>(`/services?size=100&sort=name,asc&query=${encodeURIComponent(query)}`),
  get: (id: string) => request<Service>(`/services/${id}`),
  environments: () => request<Environment[]>('/services/environments'),
  teams: () => request<Team[]>('/teams'),
  createTeam: (data: { name: string; description: string; owners: string[] }) => request<Team>('/teams', { method: 'POST', body: JSON.stringify(data) }),
  deleteTeam: (id: string) => request<void>(`/teams/${id}`, { method: 'DELETE' }),
  create: (data: ServiceInput) => request<Service>('/services', { method: 'POST', body: JSON.stringify(data) }),
  update: (id: string, data: ServiceInput) => request<Service>(`/services/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id: string) => request<void>(`/services/${id}`, { method: 'DELETE' }),
  importTemplateUrl: '/api/import/template',
  exportCatalogUrl: '/api/import/export',
  importCatalog: (file: File) => { const body = new FormData(); body.append('file', file); return request<CatalogImportResult>('/import', { method: 'POST', body }) },
}
