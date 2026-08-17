package com.helpdesk.infrastructure.config;

import com.helpdesk.application.usecases.AgregarComentario;
import com.helpdesk.application.usecases.AsignarTicket;
import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.domain.model.EquipoId;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.entity.CategoriaEntity;
import com.helpdesk.infrastructure.persistence.entity.EquipoEntity;
import com.helpdesk.infrastructure.persistence.entity.OrganizacionEntity;
import com.helpdesk.infrastructure.persistence.entity.UsuarioEntity;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataCategoriaRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataEquipoRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataOrganizacionRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataTicketRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!integration-test")
@ConditionalOnProperty(name = "helpdesk.demo-data.enabled", havingValue = "true")
public class DemoDataLoader implements ApplicationRunner {

    public static final UUID ORGANIZACION_DEMO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID EQUIPO_DEMO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CLIENTE_DEMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID GESTOR_DEMO_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID ADMIN_DEMO_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final String CATEGORIA_ACCESOS = "ACCESOS";
    public static final String CATEGORIA_PAGOS = "PAGOS";
    public static final String CATEGORIA_TARJETAS = "TARJETAS";
    public static final String CATEGORIA_CONTRATOS = "CONTRATOS";
    public static final String PASSWORD_DEMO = "demo";

    private final SpringDataOrganizacionRepository organizacionRepository;
    private final SpringDataEquipoRepository equipoRepository;
    private final SpringDataCategoriaRepository categoriaRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final SpringDataTicketRepository ticketRepository;
    private final CrearTicket crearTicket;
    private final AsignarTicket asignarTicket;
    private final CambiarEstadoTicket cambiarEstadoTicket;
    private final AgregarComentario agregarComentario;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(
            SpringDataOrganizacionRepository organizacionRepository,
            SpringDataEquipoRepository equipoRepository,
            SpringDataCategoriaRepository categoriaRepository,
            SpringDataUsuarioRepository usuarioRepository,
            SpringDataTicketRepository ticketRepository,
            CrearTicket crearTicket,
            AsignarTicket asignarTicket,
            CambiarEstadoTicket cambiarEstadoTicket,
            AgregarComentario agregarComentario,
            PasswordEncoder passwordEncoder
    ) {
        this.organizacionRepository = organizacionRepository;
        this.equipoRepository = equipoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.crearTicket = crearTicket;
        this.asignarTicket = asignarTicket;
        this.cambiarEstadoTicket = cambiarEstadoTicket;
        this.agregarComentario = agregarComentario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        asegurarCategorias();
        if (usuarioRepository.count() == 0) {
            sembrarUsuarios();
        }
        if (ticketRepository.count() == 0) {
            sembrarTickets();
        }
    }

    private void asegurarCategorias() {
        guardarCategoriaSiFalta(CATEGORIA_ACCESOS, Prioridad.ALTA);
        guardarCategoriaSiFalta(CATEGORIA_PAGOS, Prioridad.CRITICA);
        guardarCategoriaSiFalta(CATEGORIA_TARJETAS, Prioridad.MEDIA);
        guardarCategoriaSiFalta(CATEGORIA_CONTRATOS, Prioridad.BAJA);
    }

    private void guardarCategoriaSiFalta(String codigo, Prioridad prioridad) {
        if (!categoriaRepository.existsById(codigo)) {
            categoriaRepository.save(new CategoriaEntity(codigo, prioridad));
        }
    }

    private void sembrarUsuarios() {
        OrganizacionEntity organizacion = organizacionRepository.save(
                new OrganizacionEntity(ORGANIZACION_DEMO_ID, "Banco Demo"));
        EquipoEntity equipo = equipoRepository.save(
                new EquipoEntity(EQUIPO_DEMO_ID, "Soporte N1", organizacion));

        String hash = passwordEncoder.encode(PASSWORD_DEMO);
        usuarioRepository.save(new UsuarioEntity(
                CLIENTE_DEMO_ID, "cliente@bancoa.demo", hash, Rol.CLIENTE, organizacion, null));
        usuarioRepository.save(new UsuarioEntity(
                GESTOR_DEMO_ID, "gestor@bancoa.demo", hash, Rol.GESTOR, organizacion, equipo));
        usuarioRepository.save(new UsuarioEntity(
                ADMIN_DEMO_ID, "admin@bancoa.demo", hash, Rol.ADMINISTRADOR, organizacion, equipo));
    }

    private void sembrarTickets() {
        OrganizacionId organizacionId = OrganizacionId.of(ORGANIZACION_DEMO_ID);
        UsuarioId clienteId = UsuarioId.of(CLIENTE_DEMO_ID);
        UsuarioId gestorId = UsuarioId.of(GESTOR_DEMO_ID);
        Usuario cliente = usuarioDemo(clienteId, Rol.CLIENTE);
        Usuario gestor = usuarioDemo(gestorId, Rol.GESTOR);

        // ABIERTO (3)
        crearTicket.ejecutar(comando(
                "Acceso denegado al portal de nóminas",
                "Error 403 con SSO corporativo desde las 09:15.",
                organizacionId, clienteId, CATEGORIA_ACCESOS));
        crearTicket.ejecutar(comando(
                "Alta de usuario en banca electrónica pendiente",
                "Solicitud enviada ayer; el cliente no recibe email de activación.",
                organizacionId, clienteId, CATEGORIA_CONTRATOS));
        Ticket tarjetaBloqueada = crearTicket.ejecutar(comando(
                "Tarjeta corporativa bloqueada en TPV",
                "Intento de pago en comercio rechazado con código 05.",
                organizacionId, clienteId, CATEGORIA_TARJETAS));

        comentar(cliente, tarjetaBloqueada, "Ocurre en todos los comercios, no solo en uno.");

        // EN_PROGRESO (2)
        Ticket transferencia = avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Transferencia internacional bloqueada",
                        "Operación REF-88421 pendiente de revisión antifraude.",
                        organizacionId, clienteId, CATEGORIA_PAGOS)),
                gestor, EstadoTicket.EN_PROGRESO);
        comentar(gestor, transferencia, "Escalado a equipo antifraude. SLA crítico activo.");
        comentar(cliente, transferencia, "Necesito la operación hoy para cierre de proveedor.");

        avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Incidencia en firma digital de contrato",
                        "El proceso se queda en paso 3 al subir el PDF.",
                        organizacionId, clienteId, CATEGORIA_CONTRATOS)),
                gestor, EstadoTicket.EN_PROGRESO);

        // RESUELTO (2)
        Ticket mfa = avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Reset de contraseña MFA",
                        "Usuario bloqueado tras 5 intentos en app móvil.",
                        organizacionId, clienteId, CATEGORIA_ACCESOS)),
                gestor, EstadoTicket.RESUELTO);
        comentar(gestor, mfa, "MFA restablecido y verificado con el cliente por teléfono.");

        avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Duplicado en cargo de comisión",
                        "Aparecen dos cargos idénticos en extracto de marzo.",
                        organizacionId, clienteId, CATEGORIA_PAGOS)),
                gestor, EstadoTicket.RESUELTO);

        // CERRADO (2)
        avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Certificado digital expirado",
                        "Renovación completada con proveedor PKI.",
                        organizacionId, clienteId, CATEGORIA_ACCESOS)),
                gestor, EstadoTicket.CERRADO);

        avanzarHasta(
                crearTicket.ejecutar(comando(
                        "Solicitud de ampliación de límite TPV",
                        "Límite diario actualizado según política de riesgos.",
                        organizacionId, clienteId, CATEGORIA_TARJETAS)),
                gestor, EstadoTicket.CERRADO);
    }

    private CrearTicket.Comando comando(
            String asunto,
            String descripcion,
            OrganizacionId organizacionId,
            UsuarioId clienteId,
            String categoria
    ) {
        return new CrearTicket.Comando(asunto, descripcion, organizacionId, clienteId, categoria);
    }

    private Ticket avanzarHasta(Ticket ticket, Usuario gestor, EstadoTicket estadoObjetivo) {
        asignarTicket.ejecutar(new AsignarTicket.Comando(ticket.id(), gestor.id(), gestor.id()));

        Ticket actual = ticket;
        while (actual.estado() != estadoObjetivo) {
            EstadoTicket siguiente = siguienteEstadoHacia(actual.estado(), estadoObjetivo);
            actual = cambiarEstadoTicket.ejecutar(new CambiarEstadoTicket.Comando(gestor, actual.id(), siguiente));
        }
        return actual;
    }

    private EstadoTicket siguienteEstadoHacia(EstadoTicket actual, EstadoTicket objetivo) {
        return switch (actual) {
            case ABIERTO -> EstadoTicket.EN_PROGRESO;
            case EN_PROGRESO -> objetivo == EstadoTicket.EN_PROGRESO ? EstadoTicket.EN_PROGRESO : EstadoTicket.RESUELTO;
            case RESUELTO -> objetivo == EstadoTicket.CERRADO ? EstadoTicket.CERRADO : EstadoTicket.EN_PROGRESO;
            case CERRADO -> EstadoTicket.CERRADO;
        };
    }

    private void comentar(Usuario autor, Ticket ticket, String texto) {
        agregarComentario.ejecutar(new AgregarComentario.Comando(autor, ticket.id(), texto));
    }

    private Usuario usuarioDemo(UsuarioId id, Rol rol) {
        return new Usuario(
                id,
                rol == Rol.CLIENTE ? "cliente@bancoa.demo" : "gestor@bancoa.demo",
                OrganizacionId.of(ORGANIZACION_DEMO_ID),
                rol,
                rol == Rol.CLIENTE ? null : EquipoId.of(EQUIPO_DEMO_ID)
        );
    }
}
