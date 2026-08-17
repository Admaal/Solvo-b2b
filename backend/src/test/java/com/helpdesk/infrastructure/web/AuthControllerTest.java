package com.helpdesk.infrastructure.web;

import com.helpdesk.infrastructure.config.SecurityConfig;
import com.helpdesk.infrastructure.security.JwtAuthenticationFilter;
import com.helpdesk.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void loginDevuelveToken() throws Exception {
        when(authService.login(any())).thenReturn(
                new com.helpdesk.infrastructure.web.dto.LoginResponse(
                        "token-jwt",
                        "gestor@banco.test",
                        "GESTOR",
                        java.util.UUID.randomUUID(),
                        java.util.UUID.randomUUID()
                )
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.helpdesk.infrastructure.web.dto.LoginRequest("gestor@banco.test", "demo")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.email").value("gestor@banco.test"));
    }
}
