import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { PageEvent } from '@angular/material/paginator';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import {
  ESTADO_LABELS,
  EstadoTicket,
  PRIORIDAD_LABELS,
  Prioridad,
  Ticket,
} from '../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './ticket-list.component.html',
  styleUrl: './ticket-list.component.scss',
})
export class TicketListComponent implements OnInit {
  private readonly ticketService = inject(TicketService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly estadoLabels = ESTADO_LABELS;
  readonly prioridadLabels = PRIORIDAD_LABELS;
  readonly estados: EstadoTicket[] = ['ABIERTO', 'EN_PROGRESO', 'RESUELTO', 'CERRADO'];
  readonly prioridades: Prioridad[] = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];

  estadoLabel(estado: EstadoTicket): string {
    return this.estadoLabels[estado];
  }

  prioridadLabel(prioridad: Prioridad): string {
    return this.prioridadLabels[prioridad];
  }

  readonly tickets = signal<Ticket[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  estadoFiltro = signal<EstadoTicket | ''>('');
  prioridadFiltro = signal<Prioridad | ''>('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    const organizacionId = this.auth.organizacionId();
    if (!organizacionId) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.ticketService
      .listar({
        organizacionId,
        clienteId: this.auth.hasRole('CLIENTE') ? this.auth.usuarioId() ?? undefined : undefined,
        estado: this.estadoFiltro() || undefined,
        prioridad: this.prioridadFiltro() || undefined,
        page: this.pageIndex(),
        size: this.pageSize(),
      })
      .subscribe({
        next: (pagina) => {
          this.tickets.set(pagina.contenido);
          this.totalElements.set(pagina.totalElementos);
          this.loading.set(false);
        },
        error: () => {
          this.tickets.set([]);
          this.error.set('No se pudieron cargar los tickets. Comprueba que el backend está en marcha.');
          this.loading.set(false);
        },
      });
  }

  onFiltroChange(): void {
    this.pageIndex.set(0);
    this.cargar();
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.cargar();
  }

  verDetalle(ticket: Ticket): void {
    this.router.navigate(['/tickets', ticket.id]);
  }

  puedeCrear(): boolean {
    return this.auth.hasRole('CLIENTE');
  }
}
