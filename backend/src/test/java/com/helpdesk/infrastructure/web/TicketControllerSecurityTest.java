package com.helpdesk.infrastructure.web;

import com.helpdesk.application.usecases.AsignarTicket;
import com.helpdesk.application.usecases.CalcularVencimientoSLA;
import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.application.usecases.ListarTickets;
import com.helpdesk.application.usecases.ObtenerTicket;
import com.helpdesk.infrastructure.config.SecurityConfig;
import com.helpdesk.infrastructure.security.JwtAuthenticationFilter;
import com.helpdesk.infrastructure.security.JwtService;
import com.helpdesk.infrastructure.web.mapper.TicketDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
class TicketControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CrearTicket crearTicket;
    @MockBean
    private ListarTickets listarTickets;
    @MockBean
    private ObtenerTicket obtenerTicket;
    @MockBean
    private AsignarTicket asignarTicket;
    @MockBean
    private CambiarEstadoTicket cambiarEstadoTicket;
    @MockBean
    private CalcularVencimientoSLA calcularVencimientoSLA;
    @MockBean
    private UsuarioActualProvider usuarioActualProvider;
    @MockBean
    private TicketDtoMapper ticketDtoMapper;

    @Test
    void listarTicketsSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                        .param("organizacionId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    void gestorNoPuedeCrearTickets() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType("application/json")
                        .content("""
                                {
                                  "asunto": "Test",
                                  "descripcion": "Detalle",
                                  "organizacionId": "%s",
                                  "clienteId": "%s",
                                  "codigoCategoria": "ACCESOS"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
