import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { TicketCreateComponent } from './ticket-create.component';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import { Ticket } from '../../core/models/ticket.model';

const ORG_ID = '11111111-1111-1111-1111-111111111111';
const USER_ID = '33333333-3333-3333-3333-333333333333';
const TICKET_ID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

const ticketFixture: Ticket = {
  id: TICKET_ID,
  asunto: 'Problema con extracto',
  descripcion: 'Descripción larga del problema',
  estado: 'ABIERTO',
  prioridad: 'ALTA',
  codigoCategoria: 'ACCESOS',
  organizacionId: ORG_ID,
  clienteId: USER_ID,
  agenteAsignadoId: null,
  creadoEn: '2026-01-01T10:00:00Z',
};

describe('TicketCreateComponent', () => {
  let fixture: ComponentFixture<TicketCreateComponent>;
  let component: TicketCreateComponent;
  let ticketService: jasmine.SpyObj<TicketService>;
  let authMock: {
    logout: jasmine.Spy;
    organizacionId: jasmine.Spy;
    usuarioId: jasmine.Spy;
  };
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let router: Router;

  beforeEach(async () => {
    ticketService = jasmine.createSpyObj('TicketService', ['crear']);
    authMock = {
      logout: jasmine.createSpy('logout'),
      organizacionId: jasmine.createSpy('organizacionId').and.returnValue(ORG_ID),
      usuarioId: jasmine.createSpy('usuarioId').and.returnValue(USER_ID),
    };
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    ticketService.crear.and.returnValue(of(ticketFixture));

    await TestBed.configureTestingModule({
      imports: [TicketCreateComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: TicketService, useValue: ticketService },
        { provide: AuthService, useValue: authMock },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(TicketCreateComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('no crea con formulario inválido', () => {
    component.crear();

    expect(ticketService.crear).not.toHaveBeenCalled();
    expect(component.form.touched).toBeTrue();
  });

  it('categoriaCritica es true para PAGOS', () => {
    component.form.controls.codigoCategoria.setValue('PAGOS');
    expect(component.categoriaCritica).toBeTrue();

    component.form.controls.codigoCategoria.setValue('ACCESOS');
    expect(component.categoriaCritica).toBeFalse();
  });

  it('sesión incompleta muestra snackbar y redirige a login', () => {
    authMock.organizacionId.and.returnValue(null);

    component.form.patchValue({
      asunto: 'Asunto válido',
      descripcion: 'Descripción con longitud suficiente',
      codigoCategoria: 'ACCESOS',
    });
    component.crear();

    expect(snackBar.open).toHaveBeenCalledWith(
      'Sesión incompleta. Vuelve a iniciar sesión.',
      'Cerrar',
      { duration: 4000 }
    );
    expect(authMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(ticketService.crear).not.toHaveBeenCalled();
  });

  it('crear OK llama al servicio y navega al detalle', () => {
    component.form.patchValue({
      asunto: 'Asunto válido',
      descripcion: 'Descripción con longitud suficiente',
      codigoCategoria: 'ACCESOS',
    });

    component.crear();

    expect(ticketService.crear).toHaveBeenCalledWith({
      asunto: 'Asunto válido',
      descripcion: 'Descripción con longitud suficiente',
      codigoCategoria: 'ACCESOS',
    });
    expect(snackBar.open).toHaveBeenCalledWith('Ticket creado', 'Cerrar', {
      duration: 3000,
    });
    expect(router.navigate).toHaveBeenCalledWith(['/tickets', TICKET_ID]);
  });

  it('error HTTP muestra mensaje y deja de guardar', () => {
    ticketService.crear.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { mensaje: 'Categoría inválida' } }))
    );

    component.form.patchValue({
      asunto: 'Asunto válido',
      descripcion: 'Descripción con longitud suficiente',
      codigoCategoria: 'ACCESOS',
    });
    component.saving.set(true);

    component.crear();

    expect(snackBar.open).toHaveBeenCalledWith('Categoría inválida', 'Cerrar', {
      duration: 5000,
    });
    expect(component.saving()).toBeFalse();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
