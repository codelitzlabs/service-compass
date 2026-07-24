import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowRight, Boxes, Download, Github, Plus, Search, Trash2, Upload, UsersRound, X } from 'lucide-react'
import { type FormEvent, useMemo, useState } from 'react'
import { Link } from 'react-router'
import { api, type Service, type Team } from './lib'
import { Button } from './Button'

function ServiceCard({ service }: { service: Service }) {
  return <article className="group flex min-h-36 flex-col rounded-xl border border-line bg-card p-5 transition hover:-translate-y-0.5 hover:border-brand hover:shadow-md">
    <Link to={`/services/${service.id}`} className="focus-ring mb-3 flex flex-1 items-start justify-between gap-3 rounded"><span><strong className="block text-lg group-hover:text-brand-dark">{service.name}</strong><small className="mt-1 line-clamp-2 leading-5 text-muted">{service.description}</small></span><span className="flex shrink-0 items-center gap-2"><small className="rounded-full bg-brand-soft px-2 py-1 font-semibold capitalize text-brand-dark">{service.lifecycle}</small><ArrowRight size={18} className="text-muted group-hover:text-brand"/></span></Link>
    <div className="mt-auto flex flex-wrap items-center gap-2 border-t border-line pt-3"><small className="text-muted">{service.teams.map(team => team.name).join(', ')}</small><span className="ml-auto flex items-center gap-2"><small className="flex items-center gap-1 text-muted"><Boxes size={14}/>{service.destinations.length}</small><a href={service.repositoryUrl} target="_blank" rel="noreferrer" title={`Open ${service.name} repository`} aria-label={`Open ${service.name} repository on GitHub`} className="focus-ring rounded-md p-1.5 text-muted hover:bg-brand-soft hover:text-brand-dark"><Github size={17}/></a></span></div>
  </article>
}

function TeamSection({ team, services, onDelete, deleting }: { team: Team; services: Service[]; onDelete: () => void; deleting: boolean }) {
  return <section className="overflow-hidden rounded-2xl border border-line bg-white shadow-sm">
    <header className="flex flex-col justify-between gap-4 border-b border-line bg-[#f9fbf8] px-6 py-5 sm:flex-row sm:items-center"><div className="flex items-center gap-4"><span className="grid size-11 place-items-center rounded-xl bg-ink text-white"><UsersRound size={20}/></span><div><h2 className="text-xl font-bold">{team.name}</h2><p className="mt-0.5 text-sm text-muted">{team.description || 'Engineering team'} · {services.length} {services.length === 1 ? 'service' : 'services'}</p><p className="mt-1 text-xs text-muted">Owned by {team.owners.join(', ')}</p></div></div><div className="flex items-center gap-2">{services.length === 0 && <button type="button" disabled={deleting} onClick={onDelete} title={`Delete ${team.name}`} aria-label={`Delete team ${team.name}`} className="focus-ring rounded-lg p-2.5 text-muted hover:bg-red-50 hover:text-red-700 disabled:opacity-50"><Trash2 size={18}/></button>}<Button asChild secondary><Link to="/services/new"><Plus size={16}/>Add service</Link></Button></div></header>
    {services.length > 0 ? <div className="grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-3">{services.map(service => <ServiceCard key={service.id} service={service}/>)}</div> : <div className="px-6 py-10 text-center"><p className="font-semibold">No services assigned yet</p><p className="mt-1 text-sm text-muted">Use this team name as an owner when creating a service.</p></div>}
  </section>
}

export function Catalog() {
  const client = useQueryClient(); const [query, setQuery] = useState(''); const [showTeamForm, setShowTeamForm] = useState(false)
  const [teamName, setTeamName] = useState(''); const [teamDescription, setTeamDescription] = useState('')
  const [teamOwners, setTeamOwners] = useState(''); const [importMessage, setImportMessage] = useState('')
  const services = useQuery({ queryKey: ['services'], queryFn: () => api.list() })
  const teams = useQuery({ queryKey: ['teams'], queryFn: api.teams })
  const createTeam = useMutation({ mutationFn: api.createTeam, onSuccess: () => { client.invalidateQueries({ queryKey: ['teams'] }); setTeamName(''); setTeamDescription(''); setTeamOwners(''); setShowTeamForm(false) } })
  const deleteTeam = useMutation({ mutationFn: api.deleteTeam, onSuccess: () => client.invalidateQueries({ queryKey: ['teams'] }) })
  const importCatalog = useMutation({ mutationFn: api.importCatalog, onSuccess: result => { client.invalidateQueries({ queryKey: ['services'] }); client.invalidateQueries({ queryKey: ['teams'] }); client.invalidateQueries({ queryKey: ['environments'] }); setImportMessage(`Imported ${result.teams} teams and ${result.services} services.`) } })
  const visibleTeams = useMemo(() => {
    const term = query.trim().toLowerCase(); if (!teams.data) return []
    return teams.data.filter(team => {
      if (!term) return true
      const teamMatches = team.name.toLowerCase().includes(term) || team.description.toLowerCase().includes(term) || team.owners.some(owner => owner.toLowerCase().includes(term))
      const serviceMatches = services.data?.content.some(service => service.teams.some(assigned => assigned.id === team.id) && matchesService(service, term))
      return teamMatches || serviceMatches
    })
  }, [query, services.data, teams.data])
  const submitTeam = (event: FormEvent) => { event.preventDefault(); createTeam.mutate({ name: teamName, description: teamDescription, owners: teamOwners.split(',').map(value => value.trim()).filter(Boolean) }) }
  const uploadCatalog = (file: File | undefined) => { if (!file) return; setImportMessage(''); importCatalog.mutate(file) }

  return <>
    <section className="mb-8 flex flex-col justify-between gap-5 lg:flex-row lg:items-end"><div><p className="mb-2 text-sm font-bold uppercase tracking-widest text-brand">Teams and services</p><h1 className="text-3xl font-bold sm:text-4xl">Engineering starts with the team.</h1><p className="mt-3 max-w-2xl text-muted">Browse services in the context of the people who own and operate them.</p></div><div className="flex flex-wrap gap-2"><Button asChild secondary><a href={api.exportCatalogUrl} download><Download size={18}/>Export JSON</a></Button><Button asChild secondary className={importCatalog.isPending ? 'pointer-events-none opacity-70' : ''}><label><Upload size={18}/>{importCatalog.isPending ? 'Importing…' : 'Upload JSON'}<input className="sr-only" type="file" accept="application/json,.json" onChange={event => { uploadCatalog(event.target.files?.[0]); event.target.value = '' }}/></label></Button><Button asChild secondary><a href={api.importTemplateUrl} download>Template</a></Button><Button secondary onClick={() => setShowTeamForm(value => !value)}>{showTeamForm ? <X size={18}/> : <Plus size={18}/>} {showTeamForm ? 'Close' : 'Create team'}</Button><Button asChild><Link to="/services/new"><Plus size={18}/>Add service</Link></Button></div></section>

    {(importMessage || importCatalog.isError) && <p className={`mb-5 rounded-lg p-4 text-sm ${importCatalog.isError ? 'bg-red-50 text-red-800' : 'bg-brand-soft text-brand-dark'}`}>{importCatalog.isError ? importCatalog.error.message : importMessage}</p>}

    {showTeamForm && <form onSubmit={submitTeam} className="mb-7 rounded-xl border border-brand/30 bg-brand-soft p-5"><div className="mb-4"><h2 className="font-bold">Create a team</h2><p className="text-sm text-muted">Define the team and the people who own it. Services are assigned separately.</p></div><div className="grid gap-3 sm:grid-cols-3"><label><span className="mb-1 block text-xs font-semibold">Team name</span><input required maxLength={120} value={teamName} onChange={e => setTeamName(e.target.value)} className="input" placeholder="Platform team"/></label><label><span className="mb-1 block text-xs font-semibold">Team owners</span><input required value={teamOwners} onChange={e => setTeamOwners(e.target.value)} className="input" placeholder="Ada, Linus"/></label><label><span className="mb-1 block text-xs font-semibold">Description</span><input maxLength={300} value={teamDescription} onChange={e => setTeamDescription(e.target.value)} className="input" placeholder="Shared infrastructure"/></label></div><div className="mt-4 flex justify-end"><Button disabled={createTeam.isPending}>{createTeam.isPending ? 'Creating…' : 'Create team'}</Button></div>{createTeam.isError && <p className="mt-3 text-sm text-red-800">{createTeam.error.message}</p>}</form>}

    <label className="relative mb-7 block"><Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" size={18}/><input value={query} onChange={e => setQuery(e.target.value)} aria-label="Search teams and services" placeholder="Search teams, services, or purpose…" className="focus-ring w-full rounded-lg border border-line bg-card py-3 pl-10 pr-4 shadow-sm"/></label>
    {(services.isLoading || teams.isLoading) && <p className="py-20 text-center text-muted">Loading teams…</p>}
    {(services.isError || teams.isError) && <p className="rounded-lg bg-red-50 p-5 text-red-800">Could not load the catalog.</p>}
    {deleteTeam.isError && <p className="mb-5 rounded-lg bg-red-50 p-4 text-sm text-red-800">{deleteTeam.error.message}</p>}
    {visibleTeams.length > 0 && <div className="space-y-7">{visibleTeams.map(team => { const term = query.trim().toLowerCase(); const teamMatches = !term || team.name.toLowerCase().includes(term) || team.description.toLowerCase().includes(term) || team.owners.some(owner => owner.toLowerCase().includes(term)); const assigned = services.data?.content.filter(service => service.teams.some(value => value.id === team.id)) ?? []; return <TeamSection key={team.id} team={team} services={teamMatches ? assigned : assigned.filter(service => matchesService(service, term))} deleting={deleteTeam.isPending && deleteTeam.variables === team.id} onDelete={() => confirm(`Delete empty team ${team.name}?`) && deleteTeam.mutate(team.id)}/> })}</div>}
    {!teams.isLoading && visibleTeams.length === 0 && <div className="rounded-xl border border-dashed border-line bg-card py-20 text-center"><UsersRound className="mx-auto mb-3 text-muted"/><h2 className="font-bold">No teams found</h2><p className="mt-2 text-muted">Create the first team to organize your service catalog.</p></div>}
  </>
}

function matchesService(service: Service, term: string) {
  return service.name.toLowerCase().includes(term) || service.description.toLowerCase().includes(term)
    || service.owners.some(owner => owner.toLowerCase().includes(term)) || service.tags.some(tag => tag.toLowerCase().includes(term))
}
