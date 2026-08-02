import { HttpErrorResponse } from '@angular/common/http';
import { extractApiErrorMessage } from './api-error.model';

describe('extractApiErrorMessage', () => {
  it('extrae mensaje del backend', () => {
    expect(extractApiErrorMessage({ mensaje: 'Error de validación' }, 'fallback')).toBe(
      'Error de validación'
    );
  });

  it('usa fallback si no hay mensaje', () => {
    expect(extractApiErrorMessage({}, 'fallback')).toBe('fallback');
  });

  it('informa cuando no hay conexión con el backend', () => {
    const error = new HttpErrorResponse({ status: 0, statusText: 'Unknown Error' });
    expect(extractApiErrorMessage(error, 'No se pudo crear el ticket')).toContain('No se pudo contactar al backend');
  });

  it('incluye status cuando no hay body', () => {
    const error = new HttpErrorResponse({ status: 500, statusText: 'Internal Server Error' });
    expect(extractApiErrorMessage(error, 'No se pudo crear el ticket')).toBe(
      'No se pudo crear el ticket (500 Internal Server Error)'
    );
  });
});
