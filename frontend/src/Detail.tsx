import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Activity, ArrowLeft, ArrowUpRight, BookOpen, Boxes, Check, ChevronDown, Code2, Copy, CopyPlus, ExternalLink, FileText, Gauge, KeyRound, Pencil, Trash2, UserRound, UsersRound } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { api, type DestinationLabel } from './lib'
import { Button } from './Button'

const icons: Record<DestinationLabel, typeof Code2> = new Proxy({ runtime: Boxes, documentation: BookOpen, logs: FileText, metrics: Activity, dashboard: Gauge, grafana: Activity, argocd: Boxes, confluence: BookOpen, sentry: Activity, swagger: FileText, other: ExternalLink }, {
  get: (values, label: string) => values[label as keyof typeof values] ?? ExternalLink,
})

export function Detail() {
  const { id = '' } = useParams(); const navigate = useNavigate(); const client = useQueryClient(); const [environment, setEnvironment] = useState('all')
  const [copiedLink, setCopiedLink] = useState('')
  const [copiedAccount, setCopiedAccount] = useState('')
  const [expandedDestinations, setExpandedDestinations] = useState<string[]>([])
  const query = useQuery({ queryKey: ['service', id], queryFn: () => api.get(id) })
  const remove = useMutation({ mutationFn: () => api.delete(id), onSuccess: () => { client.invalidateQueries({ queryKey: ['services'] }); navigate('/') } })
  const environmentNames = useMemo(() => [...new Set(query.data?.destinations.flatMap(d => d.links.flatMap(link => link.environment ? [link.environment.name] : [])) ?? [])].sort(), [query.data])
  const destinations = query.data?.destinations.map(destination => ({ ...destination,
    links: destination.links.filter(link => environment === 'all' || !link.environment || link.environment.name === environment),
  })).filter(destination => destination.links.length > 0) ?? []
  const copyLink = async (linkId: string, url: string) => {
    await navigator.clipboard.writeText(url)
    setCopiedLink(linkId)
    window.setTimeout(() => setCopiedLink(current => current === linkId ? '' : current), 2000)
  }
  const copyAccount = async (accountKey: string, identifier: string) => {
    await navigator.clipboard.writeText(identifier)
    setCopiedAccount(accountKey)
    window.setTimeout(() => setCopiedAccount(current => current === accountKey ? '' : current), 2000)
  }
  const toggleDestination = (destinationId: string) => setExpandedDestinations(current =>
    current.includes(destinationId) ? current.filter(id => id !== destinationId) : [...current, destinationId])

  if (query.isLoading) return <p className="py-20 text-center text-muted">Loading service context…</p>
  if (!query.data) return <p>Service unavailable. <Link className="underline" to="/">Return to catalog</Link></p>
  const service = query.data
  return <>
    <Link to="/" className="mb-7 inline-flex items-center gap-2 text-sm font-semibold text-muted"><ArrowLeft size={17}/>All services</Link>
    <section className="mb-8 flex flex-col justify-between gap-5 sm:flex-row"><div>
      <div className="mb-3 flex flex-wrap items-center gap-3"><h1 className="text-4xl font-bold">{service.name}</h1><span className="rounded-full bg-brand-soft px-2.5 py-1 text-xs font-semibold capitalize text-brand-dark">{service.lifecycle}</span></div>
      <p className="max-w-2xl leading-7 text-muted">{service.description}</p>
      <div className="mt-4 flex flex-wrap items-center gap-2 text-sm"><UsersRound size={16} className="text-brand"/><span className="text-muted">Teams</span>{service.teams.map(team => <strong key={team.id} className="rounded-md bg-brand-soft px-2 py-1 text-brand-dark">{team.name}</strong>)}</div>
      <div className="mt-4 flex flex-wrap items-center gap-2 text-sm"><UserRound size={16} className="text-brand"/><span className="text-muted">Owned by</span>{service.owners.map(owner => <strong key={owner} className="rounded-md bg-white px-2 py-1 shadow-sm">{owner}</strong>)}</div>
    </div><div className="flex items-start gap-2"><Button asChild secondary><Link to={`/services/new?clone=${id}`}><CopyPlus size={16}/>Clone</Link></Button><Button asChild secondary><Link to={`/services/${id}/edit`}><Pencil size={16}/>Edit</Link></Button><button aria-label="Delete service" className="p-3 text-muted hover:text-red-700" onClick={() => confirm(`Delete ${service.name}?`) && remove.mutate()}><Trash2 size={18}/></button></div></section>

    <a href={service.repositoryUrl} target="_blank" rel="noreferrer" className="focus-ring mb-7 flex items-center gap-4 rounded-xl border border-brand/30 bg-brand-soft p-5"><span className="grid size-11 place-items-center rounded-lg bg-white text-brand-dark"><Code2 size={21}/></span><span className="flex-1"><strong className="block">Source repository</strong><small className="text-muted">{service.repositoryUrl}</small></span><ArrowUpRight size={17}/></a>
    <section className="mb-5 flex flex-col justify-between gap-3 sm:flex-row sm:items-center"><div><h2 className="text-sm font-bold uppercase tracking-widest text-muted">Destinations</h2><p className="mt-1 text-sm text-muted">Links that apply globally remain visible with every environment.</p></div>{environmentNames.length > 0 && <label className="flex items-center gap-2 text-sm font-semibold"><span>Environment</span><select className="input min-w-44" value={environment} onChange={e => setEnvironment(e.target.value)}><option value="all">All environments</option>{environmentNames.map(name => <option key={name}>{name}</option>)}</select></label>}</section>
    {destinations.length > 0 ? <div className="grid items-start gap-3 sm:grid-cols-2 lg:grid-cols-3">{destinations.map(destination => {
      const Icon = icons[destination.label]
      const expanded = expandedDestinations.includes(destination.id)
      const authenticationMethods = [...new Set(destination.links.flatMap(link =>
        link.accounts?.length ? link.accounts.map(account => account.authenticationMethod) : link.authenticationMethod ? [link.authenticationMethod] : []).filter(Boolean))]
      return <article key={destination.id} className="overflow-hidden rounded-xl border border-line bg-card shadow-sm">
        <button type="button" aria-expanded={expanded} aria-controls={`destination-${destination.id}`} onClick={() => toggleDestination(destination.id)} className="focus-ring flex w-full items-center gap-3 p-5 text-left hover:bg-brand-soft/40">
          <span className="grid size-11 shrink-0 place-items-center rounded-lg bg-brand-soft text-brand-dark"><Icon size={21}/></span>
          <span className="min-w-0 flex-1">
            <strong className="block truncate">{destination.name}</strong>
            <small className="capitalize text-muted">{destination.label} · {destination.links.length} {destination.links.length === 1 ? 'link' : 'links'}</small>
            {authenticationMethods.length > 0 && <span className="mt-1 flex flex-wrap gap-1">{authenticationMethods.map(method => <small key={method} className="rounded-full bg-paper px-2 py-0.5 font-semibold text-brand-dark">{method}</small>)}</span>}
          </span>
          <ChevronDown size={18} className={`shrink-0 text-muted transition-transform ${expanded ? 'rotate-180' : ''}`}/>
        </button>
        {expanded && <div id={`destination-${destination.id}`} className="space-y-3 border-t border-line p-4">{destination.links.map(link => {
          const accounts = link.accounts?.length ? link.accounts : link.accountIdentifier ? [{
            label: 'User', identifier: link.accountIdentifier, authenticationMethod: link.authenticationMethod || 'Not specified',
          }] : []
          return <section key={link.id} className="rounded-lg border border-line bg-white p-3">
          <div className="flex items-start gap-2">
            <a href={link.url} target="_blank" rel="noreferrer" title={link.url} className="focus-ring group min-w-0 flex-1 rounded px-1 py-0.5">
              <span className="flex items-center justify-between gap-2"><strong className="text-sm">{link.environment?.name ?? 'Global'}</strong><ArrowUpRight size={16} className="shrink-0 text-muted group-hover:text-brand"/></span>
              <small className="block truncate text-muted">{link.url}</small>
            </a>
            <button type="button" title={copiedLink === link.id ? 'Copied' : `Copy ${link.url}`} aria-label={copiedLink === link.id ? 'Link copied' : `Copy link for ${link.environment?.name ?? 'Global'}`} onClick={() => void copyLink(link.id, link.url)} className="focus-ring grid size-9 shrink-0 place-items-center rounded-md text-muted hover:bg-brand-soft hover:text-brand-dark">{copiedLink === link.id ? <Check size={16}/> : <Copy size={16}/>}</button>
          </div>
          {(accounts.length > 0 || link.accessNotes || link.accessUrl) && <div className="mt-3 space-y-2 border-t border-line pt-3 text-sm">
            {accounts.map((account, accountIndex) => {
              const accountKey = account.id ?? `${link.id}-${accountIndex}`
              return <div key={accountKey} className="flex items-start gap-2">
              <UserRound size={15} className="mt-0.5 shrink-0 text-brand"/>
              <p className="min-w-0 flex-1"><span className="text-muted">{account.label}</span><strong className="ml-2 break-all">{account.identifier}</strong></p>
              <span className="inline-flex shrink-0 items-center gap-1 rounded-full bg-brand-soft px-2 py-0.5 text-xs font-semibold text-brand-dark"><KeyRound size={12}/>{account.authenticationMethod}</span>
              <button type="button" title={copiedAccount === accountKey ? 'Copied' : `Copy ${account.identifier}`} aria-label={copiedAccount === accountKey ? `${account.label} account copied` : `Copy ${account.label} account`} onClick={() => void copyAccount(accountKey, account.identifier)} className="focus-ring grid size-7 shrink-0 place-items-center rounded-md text-muted hover:bg-brand-soft hover:text-brand-dark">{copiedAccount === accountKey ? <Check size={14}/> : <Copy size={14}/>}</button>
            </div>})}
            {link.accessNotes && <p className="whitespace-pre-wrap break-words text-muted">{link.accessNotes}</p>}
            {link.accessUrl && <a href={link.accessUrl} target="_blank" rel="noreferrer" className="focus-ring inline-flex items-center gap-1 rounded font-semibold text-brand-dark hover:underline">Request access<ArrowUpRight size={14}/></a>}
          </div>}
        </section>})}</div>}
      </article>
    })}</div> : <div className="rounded-xl border border-dashed border-line bg-card py-14 text-center text-muted">No destinations match this environment.</div>}
    {service.tags.length > 0 && <div className="mt-8 flex flex-wrap gap-2 border-t border-line pt-6">{service.tags.map(tag => <span key={tag} className="rounded bg-card px-3 py-1.5 text-sm text-muted">{tag}</span>)}</div>}
  </>
}
