import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-placeholder',
  standalone: true,
  imports: [RouterLink],
  template: `
    <a class="back-link" routerLink="/tickets">← Volver al listado</a>

    <section class="admin-hero">
      <p class="admin-hero__eyebrow">Administración</p>
      <h1 class="page-title">Ruta protegida (demo RBAC)</h1>
      <p class="page-subtitle">
        Solo usuarios con rol <strong>ADMINISTRADOR</strong> acceden a esta pantalla.
        El CRUD de usuarios, equipos y organizaciones queda fuera del alcance de esta
        versión de portfolio — el foco está en tickets, comentarios y calidad de ingeniería.
      </p>
      <p class="admin-hero__note">
        Auth demo: login por email sin contraseña, pensado para reclutadores que prueben
        la app en segundos.
      </p>
    </section>
  `,
  styles: `
    .admin-hero {
      margin-top: var(--spacing-8);
      padding: var(--spacing-32);
      border-radius: var(--radius-cards);
      background: var(--color-voltage);
      border: 1px solid var(--color-mist);
      max-width: 640px;
      box-shadow: var(--shadow-sm-2);
    }

    .admin-hero__eyebrow {
      margin: 0 0 var(--spacing-8);
      font-size: var(--text-caption);
      font-weight: 500;
      color: var(--color-inkwell-navy);
      text-transform: uppercase;
      letter-spacing: 0.06em;
      font-family: var(--font-input-mono);
    }

    .admin-hero__note {
      margin: var(--spacing-16) 0 0;
      font-size: var(--text-body-sm);
      color: var(--color-inkwell-navy);
      opacity: 0.85;
    }
  `,
})
export class AdminPlaceholderComponent {}
