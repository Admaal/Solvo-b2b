import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { extractApiErrorMessage } from '../models/api-error.model';
import { AuthService } from './auth.service';

function isTicketCreateRequest(url: string, method: string): boolean {
  return method === 'POST' && /\/tickets\/?$/.test(url);
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/login')) {
        auth.logout();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        snackBar.open(
          extractApiErrorMessage(error, 'Acceso denegado'),
          'Cerrar',
          { duration: 4000 }
        );
      } else if (
        error.status === 400 &&
        !req.url.includes('/auth/login') &&
        !isTicketCreateRequest(req.url, req.method)
      ) {
        const mensaje = extractApiErrorMessage(error, 'Solicitud inválida');
        snackBar.open(mensaje, 'Cerrar', { duration: 5000 });
      }
      return throwError(() => error);
    })
  );
};
