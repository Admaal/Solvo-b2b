package com.helpdesk.integration;

import com.helpdesk.application.usecases.AsignarTicket;
import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.integration.support.DatosPruebaFactory;
import com.helpdesk.integration.support.DatosPruebaFactory.DatosOrganizacion;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataCategoriaRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataEquipoRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataOrganizacionRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@Transactional
@Testcontainers(disabledWithoutDocker = true)
class TicketFlujoIntegracionIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CrearTicket crearTicket;

    @Autowired
    private AsignarTicket asignarTicket;

    @Autowired
    private CambiarEstadoTicket cambiarEstadoTicket;

    @Autowired
    private SpringDataOrganizacionRepository organizacionRepository;

    @Autowired
    private SpringDataEquipoRepository equipoRepository;

    @Autowired
    private SpringDataCategoriaRepository categoriaRepository;

    @Autowired
    private SpringDataUsuarioRepository usuarioRepository;

    private DatosOrganizacion datos;

    @BeforeEach
    void sembrarDatos() {
        datos = DatosPruebaFactory.sembrar(
                organizacionRepository,
                equipoRepository,
                categoriaRepository,
                usuarioRepository
        );
    }

    @Test
    void flujoCompletoCrearAsignarCambiarEstado() {
        Ticket creado = crearTicket.ejecutar(new CrearTicket.Comando(
                "No puedo acceder",
                "Portal devuelve 403",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.clienteId()),
                datos.codigoCategoria()
        ));

        assertEquals(EstadoTicket.ABIERTO, creado.estado());

        Ticket asignado = asignarTicket.ejecutar(new AsignarTicket.Comando(
                creado.id(),
                UsuarioId.of(datos.gestorId()),
                UsuarioId.of(datos.gestorId())
        ));

        assertNotNull(asignado.agenteAsignadoId());

        Ticket enProgreso = cambiarEstadoTicket.ejecutar(new CambiarEstadoTicket.Comando(
                creado.id(),
                EstadoTicket.EN_PROGRESO
        ));

        assertEquals(EstadoTicket.EN_PROGRESO, enProgreso.estado());

        Ticket resuelto = cambiarEstadoTicket.ejecutar(new CambiarEstadoTicket.Comando(
                creado.id(),
                EstadoTicket.RESUELTO
        ));

        assertEquals(EstadoTicket.RESUELTO, resuelto.estado());
    }
}
