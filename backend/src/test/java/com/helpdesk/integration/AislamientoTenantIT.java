package com.helpdesk.integration;

import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("integration-test")
@Transactional
@Testcontainers(disabledWithoutDocker = true)
class AislamientoTenantIT {

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
    private CambiarEstadoTicket cambiarEstadoTicket;

    @Autowired
    private SpringDataOrganizacionRepository organizacionRepository;

    @Autowired
    private SpringDataEquipoRepository equipoRepository;

    @Autowired
    private SpringDataCategoriaRepository categoriaRepository;

    @Autowired
    private SpringDataUsuarioRepository usuarioRepository;

    private DatosOrganizacion orgA;
    private DatosOrganizacion orgB;

    @BeforeEach
    void sembrar() {
        orgA = DatosPruebaFactory.sembrar(
                organizacionRepository, equipoRepository, categoriaRepository, usuarioRepository);
        orgB = DatosPruebaFactory.sembrarOtraOrganizacion(
                organizacionRepository, equipoRepository, usuarioRepository);
    }

    @Test
    void gestorDeOrgBNoCambiaEstadoDeTicketDeOrgA() {
        Ticket ticketA = crearTicket.ejecutar(new CrearTicket.Comando(
                "Ticket banco A",
                "Incidencia exclusiva del tenant A.",
                OrganizacionId.of(orgA.organizacionId()),
                UsuarioId.of(orgA.clienteId()),
                orgA.codigoCategoria()
        ));

        Usuario gestorB = new Usuario(
                UsuarioId.of(orgB.gestorId()),
                "gestor@bancob.test",
                OrganizacionId.of(orgB.organizacionId()),
                Rol.GESTOR,
                null
        );

        assertThrows(AccesoDenegadoException.class, () -> cambiarEstadoTicket.ejecutar(
                new CambiarEstadoTicket.Comando(gestorB, ticketA.id(), EstadoTicket.EN_PROGRESO)
        ));
    }
}
