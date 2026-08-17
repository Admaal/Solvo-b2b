package com.helpdesk.application.ports;

import com.helpdesk.domain.model.Usuario;

public record CredencialesLogin(Usuario usuario, String passwordHash) {
}
