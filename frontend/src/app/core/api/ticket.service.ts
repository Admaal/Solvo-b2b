import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AsignarTicketRequest,
  CambiarEstadoRequest,
  Comentario,
  CrearComentarioRequest,
  CrearTicketRequest,
  PaginaResponse,
  SlaResponse,
  Ticket,
  TicketListParams,
} from '../models/ticket.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tickets`;

  listar(params: TicketListParams): Observable<PaginaResponse<Ticket>> {
    let httpParams = new HttpParams()
      .set('organizacionId', params.organizacionId)
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 20))
      .set('sort', params.sort ?? 'creadoEn')
      .set('direction', params.direction ?? 'desc');

    if (params.clienteId) {
      httpParams = httpParams.set('clienteId', params.clienteId);
    }
    if (params.estado) {
      httpParams = httpParams.set('estado', params.estado);
    }
    if (params.prioridad) {
      httpParams = httpParams.set('prioridad', params.prioridad);
    }

    return this.http.get<PaginaResponse<Ticket>>(this.baseUrl, { params: httpParams });
  }

  obtener(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.baseUrl}/${id}`);
  }

  crear(request: CrearTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.baseUrl, request);
  }

  cambiarEstado(id: string, request: CambiarEstadoRequest): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.baseUrl}/${id}/estado`, request);
  }

  asignar(id: string, request: AsignarTicketRequest): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.baseUrl}/${id}/asignacion`, request);
  }

  calcularSla(id: string): Observable<SlaResponse> {
    return this.http.get<SlaResponse>(`${this.baseUrl}/${id}/sla`);
  }

  listarComentarios(ticketId: string): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.baseUrl}/${ticketId}/comentarios`);
  }

  crearComentario(ticketId: string, request: CrearComentarioRequest): Observable<Comentario> {
    return this.http.post<Comentario>(`${this.baseUrl}/${ticketId}/comentarios`, request);
  }
}
