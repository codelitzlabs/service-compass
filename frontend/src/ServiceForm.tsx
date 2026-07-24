import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Plus, Save, Trash2 } from 'lucide-react'
import { type FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'
import { api, type DestinationInput, type DestinationLinkInput, type Lifecycle, type ServiceInput } from './lib'
import { Button } from './Button'

const labels: { value: string; text: string }[] = [
  { value: 'runtime', text: 'Runtime' }, { value: 'documentation', text: 'Documentation' }, { value: 'logs', text: 'Logs' },
  { value: 'metrics', text: 'Metrics' }, { value: 'dashboard', text: 'Dashboard' }, { value: 'other', text: 'Other' },
  { value: 'grafana', text: 'Grafana' }, { value: 'argocd', text: 'Argo CD' }, { value: 'confluence', text: 'Confluence' },
  { value: 'sentry', text: 'Sentry' }, { value: 'swagger', text: 'Swagger' },
]
const authenticationMethods = ['SSO', 'Sign in', 'OAuth', 'VPN', 'API key', 'Other']
const blankLink = (): DestinationLinkInput => ({ url: '', environment: '', authenticationMethod: '', accountIdentifier: '', accessNotes: '', accessUrl: '', accounts: [] })
const blankDestination = (): DestinationInput => ({ name: '', label: 'documentation', links: [blankLink()] })
const empty: ServiceInput = { name: '', description: '', owners: [], teams: [], lifecycle: 'production', repositoryUrl: '', tags: [], destinations: [blankDestination()] }

export function ServiceForm() {
  const { id } = useParams(); const [searchParams] = useSearchParams(); const cloneId = searchParams.get('clone') ?? ''
  const edit = Boolean(id); const cloning = !edit && Boolean(cloneId); const sourceId = id ?? cloneId
  const navigate = useNavigate(); const client = useQueryClient()
  const [form, setForm] = useState<ServiceInput>(empty)
  const [ownersText, setOwnersText] = useState('')
  const [tagsText, setTagsText] = useState('')
  const existing = useQuery({ queryKey: ['service', sourceId], queryFn: () => api.get(sourceId), enabled: Boolean(sourceId) })
  const environments = useQuery({ queryKey: ['environments'], queryFn: api.environments })
  const teams = useQuery({ queryKey: ['teams'], queryFn: api.teams })

  useEffect(() => {
    if (!existing.data) return
    const service = existing.data
    setForm({ name: cloning ? `${service.name}-copy` : service.name, description: service.description, owners: service.owners,
      teams: cloning ? [] : service.teams.map(team => team.name), lifecycle: service.lifecycle, repositoryUrl: service.repositoryUrl,
      tags: service.tags, destinations: service.destinations.map(d => ({ name: d.name, label: d.label,
        links: d.links.map(link => ({ url: link.url, environment: link.environment?.name ?? '',
          authenticationMethod: link.authenticationMethod ?? '', accountIdentifier: '',
          accessNotes: link.accessNotes ?? '', accessUrl: link.accessUrl ?? '',
          accounts: link.accounts?.length ? link.accounts.map(account => ({ label: account.label, identifier: account.identifier,
            authenticationMethod: account.authenticationMethod || link.authenticationMethod || 'Not specified' }))
            : link.accountIdentifier ? [{ label: 'User', identifier: link.accountIdentifier, authenticationMethod: link.authenticationMethod || 'Not specified' }] : [] })) })) })
    setOwnersText(service.owners.join(', '))
    setTagsText(service.tags.join(', '))
  }, [cloning, existing.data])

  const mutation = useMutation({
    mutationFn: (value: ServiceInput) => edit ? api.update(id!, value) : api.create(value),
    onSuccess: service => { client.invalidateQueries({ queryKey: ['services'] }); client.invalidateQueries({ queryKey: ['environments'] }); navigate(`/services/${service.id}`) },
  })
  const set = <K extends keyof ServiceInput>(key: K, value: ServiceInput[K]) => setForm(current => ({ ...current, [key]: value }))
  const updateDestination = (index: number, patch: Partial<DestinationInput>) => set('destinations', form.destinations.map((item, i) => i === index ? { ...item, ...patch } : item))
  const updateLink = (destinationIndex: number, linkIndex: number, patch: Partial<DestinationLinkInput>) => updateDestination(destinationIndex, {
    links: form.destinations[destinationIndex].links.map((link, i) => i === linkIndex ? { ...link, ...patch } : link),
  })
  const updateAccount = (destinationIndex: number, linkIndex: number, accountIndex: number, patch: Partial<DestinationLinkInput['accounts'][number]>) => {
    const link = form.destinations[destinationIndex].links[linkIndex]
    updateLink(destinationIndex, linkIndex, { accounts: link.accounts.map((account, i) => i === accountIndex ? { ...account, ...patch } : account) })
  }
  const toggleTeam = (team: string) => set('teams', form.teams.includes(team) ? form.teams.filter(value => value !== team) : [...form.teams, team])
  const submit = (event: FormEvent) => {
    event.preventDefault()
    mutation.mutate({ ...form, owners: split(ownersText), tags: split(tagsText) })
  }

  if ((edit || cloning) && existing.isLoading) return <p className="py-20 text-center text-muted">Loading service…</p>
  return <div className="mx-auto max-w-4xl">
    <Link to={edit ? `/services/${id}` : cloning ? `/services/${cloneId}` : '/'} className="mb-7 inline-flex items-center gap-2 text-sm font-semibold text-muted"><ArrowLeft size={17}/>Cancel</Link>
    <p className="mb-2 text-sm font-bold uppercase tracking-widest text-brand">{edit ? 'Update context' : cloning ? 'Clone service' : 'New catalog entry'}</p>
    <h1 className="text-3xl font-bold">{edit ? `Edit ${form.name}` : cloning ? `Clone ${existing.data?.name ?? 'service'}` : 'Add a service'}</h1>
    <p className="mb-7 mt-2 text-muted">{cloning ? 'Choose a unique name and assign the clone to its new team. All service context and destinations have been copied.' : 'Connect people to the places where this service is built, run, and understood.'}</p>
    <form onSubmit={submit} className="space-y-7">
      <Section title="Identity"><div className="grid gap-5 sm:grid-cols-2">
        <Field label="Service name"><input required maxLength={120} value={form.name} onChange={e => set('name', e.target.value)} className="input" placeholder="payments-api"/></Field>
        <Field label="Service owners" hint="People or contacts, comma separated"><input required value={ownersText} onChange={e => setOwnersText(e.target.value)} className="input" placeholder="Ada Lovelace, on-call@example.com"/></Field>
        <Field label="Lifecycle"><select value={form.lifecycle} onChange={e => set('lifecycle', e.target.value as Lifecycle)} className="input"><option value="experimental">Experimental</option><option value="production">Production</option><option value="deprecated">Deprecated</option></select></Field>
        <Field label="Tags" hint="Comma separated"><input value={tagsText} onChange={e => setTagsText(e.target.value)} className="input" placeholder="java, payments"/></Field>
        <Field label="Repository"><input required type="url" value={form.repositoryUrl} onChange={e => set('repositoryUrl', e.target.value)} className="input" placeholder="https://github.com/org/service"/></Field>
        <div className="sm:col-span-2"><Field label="Teams" hint={cloning ? 'Select the team that will own this clone' : 'Select one or more'}><span className={`flex flex-wrap gap-2 rounded-lg border p-3 ${cloning && form.teams.length === 0 ? 'border-amber-400' : 'border-line'}`}>{teams.data?.map(team => <label key={team.id} className={`cursor-pointer rounded-full border px-3 py-1.5 text-sm ${form.teams.includes(team.name) ? 'border-brand bg-brand-soft text-brand-dark' : 'border-line bg-white text-muted'}`}><input className="sr-only" type="checkbox" checked={form.teams.includes(team.name)} onChange={() => toggleTeam(team.name)}/>{team.name}</label>)}{teams.data?.length === 0 && <span className="text-sm text-muted">Create a team on the main page first.</span>}</span></Field></div>
        <div className="sm:col-span-2"><Field label="Purpose"><textarea required maxLength={500} rows={3} value={form.description} onChange={e => set('description', e.target.value)} className="input" placeholder="What does this service do?"/></Field></div>
      </div></Section>
      <Section title="Destinations" description="Add runtime, documentation, observability, and other useful links. Repository is managed in Identity.">
        <div className="space-y-4">{form.destinations.map((destination, index) => <div key={index} className="rounded-lg border border-line bg-paper p-4">
          <div className="mb-3 flex items-center justify-between"><strong className="text-sm">Destination {index + 1}</strong>{form.destinations.length > 1 && <button type="button" onClick={() => set('destinations', form.destinations.filter((_, i) => i !== index))} className="text-muted hover:text-red-700" aria-label={`Remove destination ${index + 1}`}><Trash2 size={17}/></button>}</div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Name"><input required value={destination.name} onChange={e => updateDestination(index, { name: e.target.value })} className="input" placeholder="Production Grafana"/></Field>
            <Field label="Label" hint="Choose or type a new one"><input required maxLength={30} list="destination-label-options" value={destination.label} onChange={e => updateDestination(index, { label: e.target.value })} className="input" placeholder="e.g. feature-flags"/></Field>
          </div>
          <div className="mt-4 space-y-3">{destination.links.map((link, linkIndex) => <div key={linkIndex} className="rounded-lg border border-line bg-white p-3">
            <div className="grid gap-3 sm:grid-cols-2">
              <Field label={`Link ${linkIndex + 1}`}><input required type="url" value={link.url} onChange={e => updateLink(index, linkIndex, { url: e.target.value })} className="input" placeholder="https://…"/></Field>
              <Field label="Environment" hint="Optional"><input list="environment-options" value={link.environment ?? ''} onChange={e => updateLink(index, linkIndex, { environment: e.target.value })} className="input" placeholder="Global / all environments"/></Field>
              <Field label="Access request URL" hint="Optional"><input type="url" maxLength={500} value={link.accessUrl ?? ''} onChange={e => updateLink(index, linkIndex, { accessUrl: e.target.value })} className="input" placeholder="https://…"/></Field>
              <Field label="Access notes" hint="No passwords, keys, or tokens"><textarea rows={2} maxLength={500} value={link.accessNotes ?? ''} onChange={e => updateLink(index, linkIndex, { accessNotes: e.target.value })} className="input" placeholder="VPN required; choose Sign in with SSO."/></Field>
            </div>
            <div className="mt-4 border-t border-line pt-3">
              <div className="mb-2 flex items-center justify-between"><span><strong className="text-sm">Users and accounts</strong><small className="ml-2 text-muted">Never enter passwords</small></span><button type="button" onClick={() => updateLink(index, linkIndex, { accounts: [...link.accounts, { label: '', identifier: '', authenticationMethod: '' }] })} className="inline-flex items-center gap-1 text-sm font-semibold text-brand-dark"><Plus size={15}/>Add user</button></div>
              <div className="space-y-2">{link.accounts.map((account, accountIndex) => <div key={accountIndex} className="grid gap-2 sm:grid-cols-[1fr_1.5fr_1fr_auto] sm:items-end">
                <Field label="Label"><input required maxLength={80} value={account.label} onChange={e => updateAccount(index, linkIndex, accountIndex, { label: e.target.value })} className="input" placeholder="Administrator, Marketing…"/></Field>
                <Field label="User or account"><input required maxLength={120} value={account.identifier} onChange={e => updateAccount(index, linkIndex, accountIndex, { identifier: e.target.value })} className="input" placeholder="admin@example.com or root"/></Field>
                <Field label="Login method"><input required maxLength={40} list="authentication-method-options" value={account.authenticationMethod} onChange={e => updateAccount(index, linkIndex, accountIndex, { authenticationMethod: e.target.value })} className="input" placeholder="SSO, sign in…"/></Field>
                <button type="button" onClick={() => updateLink(index, linkIndex, { accounts: link.accounts.filter((_, i) => i !== accountIndex) })} className="mb-1 p-2 text-muted hover:text-red-700" aria-label={`Remove ${account.label || 'account'}`}><Trash2 size={16}/></button>
              </div>)}</div>
            </div>
            {destination.links.length > 1 && <button type="button" onClick={() => updateDestination(index, { links: destination.links.filter((_, i) => i !== linkIndex) })} className="mt-3 inline-flex items-center gap-1 text-sm text-muted hover:text-red-700" aria-label={`Remove link ${linkIndex + 1}`}><Trash2 size={16}/>Remove link</button>}
          </div>)}</div>
          <Button type="button" secondary className="mt-3" onClick={() => updateDestination(index, { links: [...destination.links, blankLink()] })}><Plus size={16}/>Add link</Button>
        </div>)}</div>
        <datalist id="destination-label-options">{labels.map(label => <option key={label.value} value={label.value}>{label.text}</option>)}</datalist>
        <datalist id="environment-options">{environments.data?.map(environment => <option key={environment.id} value={environment.name}/>)}</datalist>
        <datalist id="authentication-method-options">{authenticationMethods.map(method => <option key={method} value={method}/>)}</datalist>
        <Button type="button" secondary className="mt-4" onClick={() => set('destinations', [...form.destinations, blankDestination()])}><Plus size={17}/>Add destination</Button>
      </Section>
      {mutation.isError && <p className="rounded-lg bg-red-50 p-4 text-red-800">{mutation.error.message}</p>}
      <div className="flex justify-end"><Button disabled={mutation.isPending || form.teams.length === 0}><Save size={17}/>{mutation.isPending ? 'Saving…' : cloning ? 'Create cloned service' : 'Save service'}</Button></div>
    </form>
  </div>
}

const split = (value: string) => value.split(',').map(item => item.trim()).filter(Boolean)
function Field({ label, hint, children }: { label: string; hint?: string; children: React.ReactNode }) { return <label className="block"><span className="mb-1.5 flex justify-between text-sm font-semibold"><span>{label}</span>{hint && <small className="font-normal text-muted">{hint}</small>}</span>{children}</label> }
function Section({ title, description, children }: { title: string; description?: string; children: React.ReactNode }) { return <section className="rounded-xl border border-line bg-card p-6 shadow-sm"><h2 className="font-bold">{title}</h2>{description && <p className="mb-5 mt-1 text-sm text-muted">{description}</p>}{!description && <div className="mb-5"/>}{children}</section> }
