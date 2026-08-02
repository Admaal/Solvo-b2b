import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { TicketDetailComponent } from './ticket-detail.component';
import { AuthService } from '../../core/auth/auth.service';
import { TicketService } from '../../core/api/ticket.service';
import { Ticket } from '../../core/models/ticket.model';

const ORG_ID = '11111111-1111-1111-1111-111111111111';
const USER_ID = '33333333-3333-3333-3333-333333333333';
const GESTOR_ID = '44444444-4444-4444-4444-444444444444';
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

describe('TicketDetailComponent', () => {
  let fixture: ComponentFixture<TicketDetailComponent>;
  let component: TicketDetailComponent;
  let ticketService: jasmine.SpyObj<TicketService>;
  let auth: jasmine.SpyObj<AuthService>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    ticketService = jasmine.createSpyObj('TicketService', [
      'obtener',
      'calcularSla',
      'cambiarEstado',
      'asignar',
      'listarComentarios',
      'crearComentario',
    ]);
    auth = jasmine.createSpyObj('AuthService', ['hasRole'], {
      usuarioId: () => GESTOR_ID,
    });
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    ticketService.obtener.and.returnValue(of(ticketFixture));
    ticketService.calcularSla.and.returnValue(of({ vencimiento: '2026-01-02T10:00:00Z' }));
    ticketService.listarComentarios.and.returnValue(of([]));
    ticketService.crearComentario.and.returnValue(
      of({
        id: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        ticketId: TICKET_ID,
        autorId: GESTOR_ID,
        autorEmail: 'gestor@bancoa.demo',
        texto: 'Nuevo comentario',
        creadoEn: '2026-01-01T12:00:00Z',
      })
    );
    ticketService.cambiarEstado.and.returnValue(
      of({ ...ticketFixture, estado: 'EN_PROGRESO' })
    );
    ticketService.asignar.and.returnValue(
      of({ ...ticketFixture, agenteAsignadoId: GESTOR_ID })
    );
    auth.hasRole.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [TicketDetailComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'id' ? TICKET_ID : null),
              },
            },
          },
        },
        { provide: TicketService, useValue: ticketService },
        { provide: AuthService, useValue: auth },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TicketDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('carga ticket y SLA desde la ruta', () => {
    fixture.detectChanges();

    expect(ticketService.obtener).toHaveBeenCalledWith(TICKET_ID);
    expect(ticketService.calcularSla).toHaveBeenCalledWith(TICKET_ID);
    expect(ticketService.listarComentarios).toHaveBeenCalledWith(TICKET_ID);
    expect(component.ticket()).toEqual(ticketFixture);
    expect(component.slaVencimiento()).toBe('2026-01-02T10:00:00Z');
    expect(component.loading()).toBeFalse();
  });

  it('puedeGestionar para GESTOR o ADMINISTRADOR', () => {
    auth.hasRole.and.callFake((...roles: string[]) =>
      roles.some((r) => r === 'GESTOR' || r === 'ADMINISTRADOR')
    );

    fixture.detectChanges();

    expect(component.puedeGestionar()).toBeTrue();
  });

  it('cambiarEstado actualiza ticket y muestra snackbar', () => {
    fixture.detectChanges();
    component.estadoForm.controls.estado.setValue('EN_PROGRESO');

    component.cambiarEstado();

    expect(ticketService.cambiarEstado).toHaveBeenCalledWith(TICKET_ID, {
      estado: 'EN_PROGRESO',
    });
    expect(component.ticket()?.estado).toBe('EN_PROGRESO');
    expect(snackBar.open).toHaveBeenCalledWith('Estado actualizado', 'Cerrar', {
      duration: 3000,
    });
    expect(component.saving()).toBeFalse();
  });

  it('asignarme usa usuarioId y llama al servicio', () => {
    fixture.detectChanges();

    component.asignarme();

    expect(ticketService.asignar).toHaveBeenCalledWith(TICKET_ID, {
      agenteId: GESTOR_ID,
    });
    expect(component.ticket()?.agenteAsignadoId).toBe(GESTOR_ID);
    expect(snackBar.open).toHaveBeenCalledWith('Ticket asignado', 'Cerrar', {
      duration: 3000,
    });
  });

  it('enviarComentario añade comentario a la lista', () => {
    fixture.detectChanges();
    component.comentarioForm.controls.texto.setValue('Actualización del cliente');

    component.enviarComentario();

    expect(ticketService.crearComentario).toHaveBeenCalledWith(TICKET_ID, {
      texto: 'Actualización del cliente',
    });
    expect(component.comentarios().length).toBe(1);
    expect(component.comentarios()[0].texto).toBe('Nuevo comentario');
  });
});
