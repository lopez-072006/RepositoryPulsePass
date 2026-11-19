import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Boton } from '../componentes/ui/Boton'
import { useAuth } from '../contextos/AuthContext'
import { crearEvento, publicarEvento, type NuevoEvento } from '../features/eventos/api'
import { obtenerSedesActivas } from '../features/sedes/api'

const inicial: NuevoEvento = { eventCode: '', name: '', description: '', category: 'MUSIC', eventDate: '', minimumAge: 18, venueCode: '' }
export default function AdminEventos() {
 const { sesion } = useAuth(); const [form, setForm] = useState(inicial); const [code, setCode] = useState<string | null>(null)
 const sedes = useQuery({ queryKey: ['sedes','activas'], queryFn: obtenerSedesActivas })
 const crear = useMutation({ mutationFn: () => sesion.estado === 'autenticada' ? crearEvento(form, sesion.token) : Promise.reject(), onSuccess: (x) => setCode(x.eventCode) })
 const publicar = useMutation({ mutationFn: () => sesion.estado === 'autenticada' && code ? publicarEvento(code, sesion.token) : Promise.reject() })
 if (sesion.estado === 'anonima') return null
 return <section className="mx-auto max-w-xl"><h1 className="font-titulo text-3xl font-bold">Administrar eventos</h1><form className="mt-6 grid gap-3" onSubmit={(e)=>{e.preventDefault();crear.mutate()}}>{(['eventCode','name','description','eventDate'] as const).map((k)=><input key={k} type={k==='eventDate'?'datetime-local':'text'} required={k!=='description'} placeholder={k} className="rounded-lg border p-3" value={form[k]} onChange={(e)=>setForm({...form,[k]:e.target.value})}/>) }<select className="rounded-lg border p-3" value={form.category} onChange={(e)=>setForm({...form,category:e.target.value})}>{['MUSIC','SPORTS','TECHNOLOGY','EDUCATION','CULTURE','ENTERTAINMENT'].map(x=><option key={x}>{x}</option>)}</select><input className="rounded-lg border p-3" type="number" min="0" value={form.minimumAge} onChange={(e)=>setForm({...form,minimumAge:Number(e.target.value)})}/><select required className="rounded-lg border p-3" value={form.venueCode} onChange={(e)=>setForm({...form,venueCode:e.target.value})}><option value="">Sede</option>{(sedes.data??[]).map(s=><option key={s.code} value={s.code}>{s.name}</option>)}</select><Boton cargando={crear.isPending}>Crear evento</Boton></form>{code?<div className="mt-5"><p>Evento {code} creado.</p><Boton className="mt-3" cargando={publicar.isPending} onClick={()=>publicar.mutate()}>Publicar</Boton></div>:null}</section>
}
