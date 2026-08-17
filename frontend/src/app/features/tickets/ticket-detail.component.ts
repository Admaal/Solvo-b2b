import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import {
  Comentario,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  TRANSICIONES_ESTADO,
  Ticket,
} from '../../core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.scss',
})
export class TicketDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly ticketService = inject(TicketService);
  private readonly auth = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly estadoLabels = ESTADO_LABELS;
  readonly prioridadLabels = PRIORIDAD_LABELS;

  estadoLabel(estado: keyof typeof ESTADO_LABELS): string {
    return this.estadoLabels[estado];
  }

  prioridadLabel(prioridad: keyof typeof PRIORIDAD_LABELS): string {
    return this.prioridadLabels[prioridad];
  }

  readonly ticket = signal<Ticket | null>(null);
  readonly comentarios = signal<Comentario[]>([]);
  readonly slaVencimiento = signal<string | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);
  readonly enviandoComentario = signal(false);

  readonly puedeGestionar = computed(() =>
    this.auth.hasRole('GESTOR', 'ADMINISTRADOR')
  );

  readonly transicionesDisponibles = computed(() => {
    const actual = this.ticket()?.estado;
    return actual ? TRANSICIONES_ESTADO[actual] : [];
  });

  readonly estadoForm = this.fb.nonNullable.group({
    estado: ['', Validators.required],
  });

  readonly comentarioForm = this.fb.nonNullable.group({
    texto: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(2000)]],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }
    this.cargar(id);
  }

  cargar(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.ticketService.obtener(id).subscribe({
      next: (ticket) => {
        this.ticket.set(ticket);
        this.loading.set(false);
        this.cargarSla(id);
        this.cargarComentarios(id);
      },
      error: () => {
        this.ticket.set(null);
        this.error.set('No se pudo cargar el ticket.');
        this.loading.set(false);
      },
    });
  }

  cambiarEstado(): void {
    const ticket = this.ticket();
    if (!ticket || this.estadoForm.invalid) {
      return;
    }

    this.saving.set(true);
    this.ticketService
      .cambiarEstado(ticket.id, { estado: this.estadoForm.controls.estado.value as Ticket['estado'] })
      .subscribe({
        next: (actualizado) => {
          this.ticket.set(actualizado);
          this.estadoForm.reset();
          this.snackBar.open('Estado actualizado', 'Cerrar', { duration: 3000 });
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  asignarme(): void {
    const ticket = this.ticket();
    const agenteId = this.auth.usuarioId();
    if (!ticket || !agenteId) {
      return;
    }

    this.saving.set(true);
    this.ticketService.asignar(ticket.id, { agenteId }).subscribe({
      next: (actualizado) => {
        this.ticket.set(actualizado);
        this.snackBar.open('Ticket asignado', 'Cerrar', { duration: 3000 });
        this.saving.set(false);
      },
      error: () => this.saving.set(false),
    });
  }

  enviarComentario(): void {
    const ticket = this.ticket();
    if (!ticket || this.comentarioForm.invalid) {
      this.comentarioForm.markAllAsTouched();
      return;
    }

    this.enviandoComentario.set(true);
    this.ticketService
      .crearComentario(ticket.id, { texto: this.comentarioForm.controls.texto.value })
      .subscribe({
        next: (comentario) => {
          this.comentarios.update((lista) => [...lista, comentario]);
          this.comentarioForm.reset();
          this.snackBar.open('Comentario añadido', 'Cerrar', { duration: 3000 });
          this.enviandoComentario.set(false);
        },
        error: () => {
          this.snackBar.open('No se pudo publicar el comentario', 'Cerrar', { duration: 4000 });
          this.enviandoComentario.set(false);
        },
      });
  }

  private cargarSla(id: string): void {
    this.ticketService.calcularSla(id).subscribe({
      next: (sla) => this.slaVencimiento.set(sla.vencimiento),
    });
  }

  private cargarComentarios(id: string): void {
    this.ticketService.listarComentarios(id).subscribe({
      next: (lista) => this.comentarios.set(lista),
    });
  }
}
