import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

const SESSION_KEY = 'helpdesk_session';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('login persiste la sesión', () => {
    const response = {
      token: 'jwt',
      email: 'cliente@bancoa.demo',
      rol: 'CLIENTE' as const,
      usuarioId: '33333333-3333-3333-3333-333333333333',
      organizacionId: '11111111-1111-1111-1111-111111111111',
    };

    service.login({ email: response.email, password: 'demo' }).subscribe((result) => {
      expect(result).toEqual(response);
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.rol()).toBe('CLIENTE');
    });

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(response);
  });

  it('logout limpia la sesión', () => {
    const response = {
      token: 'jwt',
      email: 'cliente@bancoa.demo',
      rol: 'CLIENTE' as const,
      usuarioId: '33333333-3333-3333-3333-333333333333',
      organizacionId: '11111111-1111-1111-1111-111111111111',
    };

    service.login({ email: response.email, password: 'demo' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(response);
    expect(service.isAuthenticated()).toBeTrue();

    service.logout();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('descarta sesión incompleta al cargar', () => {
    localStorage.setItem(
      SESSION_KEY,
      JSON.stringify({ token: 'jwt', email: 'cliente@bancoa.demo', rol: 'CLIENTE' })
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const fresh = TestBed.inject(AuthService);

    expect(fresh.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem(SESSION_KEY)).toBeNull();
  });
});
