import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'tickets', pathMatch: 'full' },
      {
        path: 'tickets',
        loadComponent: () =>
          import('./features/tickets/ticket-list.component').then((m) => m.TicketListComponent),
      },
      {
        path: 'tickets/nuevo',
        canActivate: [roleGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./features/tickets/ticket-create.component').then((m) => m.TicketCreateComponent),
      },
      {
        path: 'tickets/:id',
        loadComponent: () =>
          import('./features/tickets/ticket-detail.component').then((m) => m.TicketDetailComponent),
      },
      {
        path: 'admin',
        canActivate: [roleGuard(['ADMINISTRADOR'])],
        loadComponent: () =>
          import('./features/admin/admin-placeholder.component').then((m) => m.AdminPlaceholderComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'tickets' },
];
