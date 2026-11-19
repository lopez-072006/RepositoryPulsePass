import { useState } from 'react'
import { Boton } from '../componentes/ui/Boton'
import { useAuth } from '../contextos/AuthContext'
import { getJson, patchJson } from '../lib/http'
import { ticketSchema, type Ticket } from '../features/tickets/esquemas'

export default function ValidacionIngreso() {
 const { sesion } = useAuth(); const [code,setCode]=useState(''); const [ticket,setTicket]=useState<Ticket|null>(null); const [error,setError]=useState<string|null>(null)
 const auth={headers:{Authorization:`Bearer ${sesion.estado==='autenticada'?sesion.token:''}`}}
 const buscar=async()=>{try{setTicket(await getJson(`/api/tickets/${code}`,ticketSchema,auth));setError(null)}catch(e){setError(e instanceof Error?e.message:'Ticket no disponible.')}}
 const usar=async()=>{if(!ticket)return;try{setTicket(await patchJson(`/api/tickets/${ticket.ticketCode}/use`,ticketSchema,auth));setError(null)}catch(e){setError(e instanceof Error?e.message:'No fue posible validar.')}}
 return <section className="mx-auto max-w-xl"><h1 className="font-titulo text-3xl font-bold">Validar ingreso</h1><div className="mt-6 flex gap-3"><input className="flex-1 rounded-lg border p-3" placeholder="Código de ticket" value={code} onChange={e=>setCode(e.target.value)}/><Boton onClick={()=>void buscar()}>Buscar</Boton></div>{error?<p className="mt-4 text-red-700">{error}</p>:null}{ticket?<div className="mt-5 rounded-xl bg-white p-5 shadow"><b>{ticket.eventName}</b><p>{ticket.ticketCode} · {ticket.status}</p>{ticket.status==='PAID'?<Boton className="mt-3" onClick={()=>void usar()}>Marcar usado</Boton>:null}</div>:null}</section>
}
