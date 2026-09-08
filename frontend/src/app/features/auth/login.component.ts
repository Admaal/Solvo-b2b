import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { timeout, TimeoutError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { DEMO_USERS, DemoUser } from '../../core/models/auth.model';

const LOGIN_TIMEOUT_MS = 120_000;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly demoUsers = DEMO_USERS;
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', Validators.required],
  });

  entrarComo(user: DemoUser): void {
    this.form.patchValue({ email: user.email });
    this.enviar(user.email, user.password);
  }

  login(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const email = this.form.controls.email.value;
    const user = this.demoUsers.find((item) => item.email === email);
    this.enviar(email, user?.password ?? '');
  }

  private enviar(email: string, password: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.auth.login({ email, password }).pipe(timeout(LOGIN_TIMEOUT_MS)).subscribe({
      next: () => this.router.navigate(['/tickets']),
      error: (err: unknown) => {
        const coldStart = err instanceof TimeoutError || (err as { name?: string })?.name === 'TimeoutError';
        this.error.set(
          coldStart
            ? 'El API de demo se está despertando (Render free). Espera unos segundos y vuelve a pulsar.'
            : 'No se pudo iniciar sesión. Verifica email, contraseña y que el backend esté en marcha.'
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
