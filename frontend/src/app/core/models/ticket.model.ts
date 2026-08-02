export type EstadoTicket = 'ABIERTO' | 'EN_PROGRESO' | 'RESUELTO' | 'CERRADO';
export type Prioridad = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export interface Ticket {
  id: string;
  asunto: string;
  descripcion: string;
  estado: EstadoTicket;
  prioridad: Prioridad;
  codigoCategoria: string;
  organizacionId: string;
  clienteId: string;
  agenteAsignadoId: string | null;
  creadoEn: string;
}

export interface PaginaResponse<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface CrearTicketRequest {
  asunto: string;
  descripcion: string;
  organizacionId: string;
  clienteId: string;
  codigoCategoria: string;
}

export interface CambiarEstadoRequest {
  estado: EstadoTicket;
}

export interface AsignarTicketRequest {
  agenteId: string;
}

export interface SlaResponse {
  vencimiento: string;
}

export interface TicketListParams {
  organizacionId: string;
  clienteId?: string;
  estado?: EstadoTicket;
  prioridad?: Prioridad;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export const TRANSICIONES_ESTADO: Record<EstadoTicket, EstadoTicket[]> = {
  ABIERTO: ['EN_PROGRESO'],
  EN_PROGRESO: ['RESUELTO'],
  RESUELTO: ['CERRADO', 'EN_PROGRESO'],
  CERRADO: [],
};

export const ESTADO_LABELS: Record<EstadoTicket, string> = {
  ABIERTO: 'Abierto',
  EN_PROGRESO: 'En progreso',
  RESUELTO: 'Resuelto',
  CERRADO: 'Cerrado',
};

export const PRIORIDAD_LABELS: Record<Prioridad, string> = {
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
};

export const CATEGORIAS = ['ACCESOS', 'PAGOS', 'TARJETAS', 'CONTRATOS'] as const;

export interface Comentario {
  id: string;
  ticketId: string;
  autorId: string;
  autorEmail: string;
  texto: string;
  creadoEn: string;
}

export interface CrearComentarioRequest {
  texto: string;
}
