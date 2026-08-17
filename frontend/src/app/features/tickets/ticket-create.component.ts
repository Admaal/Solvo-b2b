import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import { CATEGORIAS } from '../../core/models/ticket.model';
import { extractApiErrorMessage } from '../../core/models/api-error.model';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './ticket-create.component.html',
  styleUrl: './ticket-create.component.scss',
})
export class TicketCreateComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly ticketService = inject(TicketService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly categorias = CATEGORIAS;
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    asunto: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
    descripcion: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(4000)]],
    codigoCategoria: ['ACCESOS', Validators.required],
  });

  get categoriaCritica(): boolean {
    return this.form.controls.codigoCategoria.value === 'PAGOS';
  }

  crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const organizacionId = this.auth.organizacionId();
    const clienteId = this.auth.usuarioId();
    if (!organizacionId || !clienteId) {
      this.snackBar.open('Sesión incompleta. Vuelve a iniciar sesión.', 'Cerrar', { duration: 4000 });
      this.auth.logout();
      this.router.navigate(['/auth/login']);
      return;
    }

    this.saving.set(true);
    this.ticketService
      .crear({
        asunto: this.form.controls.asunto.value,
        descripcion: this.form.controls.descripcion.value,
        codigoCategoria: this.form.controls.codigoCategoria.value,
      })
      .subscribe({
        next: (ticket) => {
          this.snackBar.open('Ticket creado', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/tickets', ticket.id]);
        },
        error: (err: HttpErrorResponse) => {
          const mensaje = extractApiErrorMessage(err, 'No se pudo crear el ticket');
          this.snackBar.open(mensaje, 'Cerrar', { duration: 5000 });
          this.saving.set(false);
        },
        complete: () => this.saving.set(false),
      });
  }
}
