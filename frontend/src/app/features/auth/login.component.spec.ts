import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { NEVER, of } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/auth/auth.service';
import { DEMO_USERS } from '../../core/models/auth.model';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let auth: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    auth = jasmine.createSpyObj('AuthService', ['login']);
    auth.login.and.returnValue(of({
      token: 'jwt',
      email: DEMO_USERS[2].email,
      rol: 'ADMINISTRADOR',
      usuarioId: '55555555-5555-5555-5555-555555555555',
      organizacionId: '11111111-1111-1111-1111-111111111111',
    }));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: AuthService, useValue: auth },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('avisa del arranque en frío de Render', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('2 minutos');
    expect(text).toContain('Render');
  });

  it('deja de girar y explica el cold start si el API no responde', fakeAsync(() => {
    auth.login.and.returnValue(NEVER);

    component.entrarComo(DEMO_USERS[2]);
    expect(component.loading()).toBeTrue();

    tick(120_000);

    expect(component.loading()).toBeFalse();
    expect(component.error()).toContain('despertando');
  }));
});
