package com.helpdesk.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.domain.model.OrganizacionId;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Transactional
@Testcontainers(disabledWithoutDocker = true)
class TicketApiIntegracionIT {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CrearTicket crearTicket;

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
    void loginClienteCreaTicketYLoLista() throws Exception {
        String token = login(DatosPruebaFactory.CLIENTE_EMAIL);

        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asunto": "Problema con extracto PDF",
                                  "descripcion": "El extracto mensual no se genera desde ayer.",
                                  "codigoCategoria": "ACCESOS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"));

        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + token)
                        .param("organizacionId", datos.organizacionId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].asunto").value("Problema con extracto PDF"));
    }

    @Test
    void gestorVeTodosLosTicketsDelCliente() throws Exception {
        crearTicket.ejecutar(new CrearTicket.Comando(
                "Ticket privado cliente 1",
                "Solo visible para su dueño en listados filtrados.",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.clienteId()),
                datos.codigoCategoria()
        ));
        crearTicket.ejecutar(new CrearTicket.Comando(
                "Ticket privado cliente 2",
                "Pertenece a otro cliente de la misma organización.",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.cliente2Id()),
                datos.codigoCategoria()
        ));

        String tokenGestor = login(DatosPruebaFactory.GESTOR_EMAIL);

        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .param("organizacionId", datos.organizacionId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void clienteSoloVeSusTickets() throws Exception {
        crearTicket.ejecutar(new CrearTicket.Comando(
                "Mío",
                "Ticket del cliente autenticado.",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.clienteId()),
                datos.codigoCategoria()
        ));
        crearTicket.ejecutar(new CrearTicket.Comando(
                "Ajeno",
                "Ticket de otro cliente.",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.cliente2Id()),
                datos.codigoCategoria()
        ));

        String tokenCliente = login(DatosPruebaFactory.CLIENTE_EMAIL);

        MvcResult resultado = mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .param("organizacionId", datos.organizacionId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(body.get("totalElementos").asInt()).isEqualTo(1);
        assertThat(body.get("contenido").get(0).get("asunto").asText()).isEqualTo("Mío");
    }

    @Test
    void gestorCambiaEstadoDeTicket() throws Exception {
        var ticket = crearTicket.ejecutar(new CrearTicket.Comando(
                "Pendiente de asignación",
                "Ticket para transición de estado vía API.",
                OrganizacionId.of(datos.organizacionId()),
                UsuarioId.of(datos.clienteId()),
                datos.codigoCategoria()
        ));

        String tokenGestor = login(DatosPruebaFactory.GESTOR_EMAIL);

        mockMvc.perform(patch("/api/v1/tickets/{id}/estado", ticket.id().value())
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "estado": "EN_PROGRESO" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    private String login(String email) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, DatosPruebaFactory.PASSWORD_DEMO)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }
}
