import { HttpErrorResponse } from '@angular/common/http';

export interface ApiErrorResponse {
  mensaje?: string;
  message?: string;
  error?: string;
}

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se pudo contactar al backend. Comprueba que esté en marcha (puerto 8080) y vuelve a intentarlo.';
    }

    const bodyMessage = extractBodyMessage(error.error);
    if (bodyMessage) {
      return bodyMessage;
    }

    if (error.statusText) {
      return `${fallback} (${error.status} ${error.statusText})`;
    }

    return `${fallback} (${error.status})`;
  }

  return extractBodyMessage(error) ?? fallback;
}

function extractBodyMessage(error: unknown): string | null {
  if (!error || typeof error !== 'object') {
    return null;
  }

  const body = error as ApiErrorResponse;
  return body.mensaje ?? body.message ?? body.error ?? null;
}
