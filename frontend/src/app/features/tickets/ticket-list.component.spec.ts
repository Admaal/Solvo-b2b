import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { TicketListComponent } from './ticket-list.component';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import { PaginaResponse, Ticket } from '../../core/models/ticket.model';

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

const paginaFixture: PaginaResponse<Ticket> = {
  contenido: [ticketFixture],
  pagina: 0,
  tamano: 20,
  totalElementos: 1,
  totalPaginas: 1,
};

describe('TicketListComponent', () => {
  let fixture: ComponentFixture<TicketListComponent>;
  let component: TicketListComponent;
  let ticketService: jasmine.SpyObj<TicketService>;
  let authMock: {
    hasRole: jasmine.Spy;
    organizacionId: jasmine.Spy;
    usuarioId: jasmine.Spy;
  };
  let router: Router;

  beforeEach(async () => {
    ticketService = jasmine.createSpyObj('TicketService', ['listar']);
    authMock = {
      hasRole: jasmine.createSpy('hasRole').and.returnValue(false),
      organizacionId: jasmine.createSpy('organizacionId').and.returnValue(ORG_ID),
      usuarioId: jasmine.createSpy('usuarioId').and.returnValue(USER_ID),
    };

    ticketService.listar.and.returnValue(of(paginaFixture));

    await TestBed.configureTestingModule({
      imports: [TicketListComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: TicketService, useValue: ticketService },
        { provide: AuthService, useValue: authMock },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(TicketListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('carga tickets al iniciar con organizacionId', () => {
    fixture.detectChanges();

    expect(ticketService.listar).toHaveBeenCalledWith({
      organizacionId: ORG_ID,
      clienteId: undefined,
      estado: undefined,
      prioridad: undefined,
      page: 0,
      size: 20,
    });
    expect(component.tickets()).toEqual([ticketFixture]);
    expect(component.totalElements()).toBe(1);
    expect(component.loading()).toBeFalse();
  });

  it('filtra por clienteId cuando el rol es CLIENTE', () => {
    authMock.hasRole.and.callFake((...roles: string[]) => roles.includes('CLIENTE'));

    fixture.detectChanges();

    expect(ticketService.listar).toHaveBeenCalledWith(
      jasmine.objectContaining({
        organizacionId: ORG_ID,
        clienteId: USER_ID,
      })
    );
  });

  it('no llama a listar sin organizacionId', () => {
    authMock.organizacionId.and.returnValue(null);

    component.cargar();

    expect(ticketService.listar).not.toHaveBeenCalled();
  });

  it('puedeCrear solo para CLIENTE', () => {
    authMock.hasRole.and.callFake((...roles: string[]) => roles.includes('CLIENTE'));
    expect(component.puedeCrear()).toBeTrue();

    authMock.hasRole.and.returnValue(false);
    expect(component.puedeCrear()).toBeFalse();
  });

  it('verDetalle navega al ticket', () => {
    component.verDetalle(ticketFixture);

    expect(router.navigate).toHaveBeenCalledWith(['/tickets', TICKET_ID]);
  });

  it('muestra error si listar falla', () => {
    ticketService.listar.and.returnValue(throwError(() => new Error('network')));

    fixture.detectChanges();

    expect(component.error()).toContain('No se pudieron cargar los tickets');
    expect(component.loading()).toBeFalse();
    expect(component.tickets()).toEqual([]);
  });
});
