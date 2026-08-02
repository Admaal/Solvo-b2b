import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard, roleGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    auth = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'hasRole']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite acceso si hay sesión', () => {
    auth.isAuthenticated.and.returnValue(true);
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBeTrue();
  });

  it('redirige a login sin sesión', () => {
    auth.isAuthenticated.and.returnValue(false);
    router.createUrlTree.and.returnValue({} as never);

    TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/login']);
  });
});

describe('roleGuard', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    auth = jasmine.createSpyObj('AuthService', ['hasRole']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite acceso con rol válido', () => {
    auth.hasRole.and.returnValue(true);
    const guard = roleGuard(['ADMINISTRADOR']);
    const result = TestBed.runInInjectionContext(() => guard({} as never, {} as never));
    expect(result).toBeTrue();
  });

  it('redirige a tickets sin rol', () => {
    auth.hasRole.and.returnValue(false);
    router.createUrlTree.and.returnValue({} as never);
    const guard = roleGuard(['ADMINISTRADOR']);

    TestBed.runInInjectionContext(() => guard({} as never, {} as never));

    expect(router.createUrlTree).toHaveBeenCalledWith(['/tickets']);
  });
});
