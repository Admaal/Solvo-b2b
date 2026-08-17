package com.helpdesk.integration.support;

import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.infrastructure.persistence.entity.CategoriaEntity;
import com.helpdesk.infrastructure.persistence.entity.EquipoEntity;
import com.helpdesk.infrastructure.persistence.entity.OrganizacionEntity;
import com.helpdesk.infrastructure.persistence.entity.UsuarioEntity;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataCategoriaRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataEquipoRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataOrganizacionRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public final class DatosPruebaFactory {

    public static final String CLIENTE_EMAIL = "cliente@bancoa.test";
    public static final String GESTOR_EMAIL = "gestor@bancoa.test";
    public static final String PASSWORD_DEMO = "demo";
    public static final String CATEGORIA_ACCESOS = "ACCESOS";
    public static final String CATEGORIA_PAGOS = "PAGOS";

    private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode(PASSWORD_DEMO);

    public record DatosOrganizacion(
            UUID organizacionId,
            UUID equipoId,
            UUID clienteId,
            UUID gestorId,
            UUID cliente2Id,
            String codigoCategoria
    ) {
    }

    private DatosPruebaFactory() {
    }

    public static DatosOrganizacion sembrar(
            SpringDataOrganizacionRepository organizacionRepository,
            SpringDataEquipoRepository equipoRepository,
            SpringDataCategoriaRepository categoriaRepository,
            SpringDataUsuarioRepository usuarioRepository
    ) {
        UUID organizacionId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID cliente2Id = UUID.randomUUID();
        UUID gestorId = UUID.randomUUID();

        OrganizacionEntity organizacion = organizacionRepository.save(
                new OrganizacionEntity(organizacionId, "Banco A"));
        EquipoEntity equipo = equipoRepository.save(
                new EquipoEntity(equipoId, "Pagos", organizacion));
        categoriaRepository.save(new CategoriaEntity(CATEGORIA_ACCESOS, Prioridad.ALTA));
        categoriaRepository.save(new CategoriaEntity(CATEGORIA_PAGOS, Prioridad.CRITICA));

        usuarioRepository.save(new UsuarioEntity(
                clienteId, CLIENTE_EMAIL, PASSWORD_HASH, Rol.CLIENTE, organizacion, null));
        usuarioRepository.save(new UsuarioEntity(
                cliente2Id, "cliente2@bancoa.test", PASSWORD_HASH, Rol.CLIENTE, organizacion, null));
        usuarioRepository.save(new UsuarioEntity(
                gestorId, GESTOR_EMAIL, PASSWORD_HASH, Rol.GESTOR, organizacion, equipo));

        return new DatosOrganizacion(organizacionId, equipoId, clienteId, gestorId, cliente2Id, CATEGORIA_ACCESOS);
    }

    public static DatosOrganizacion sembrarOtraOrganizacion(
            SpringDataOrganizacionRepository organizacionRepository,
            SpringDataEquipoRepository equipoRepository,
            SpringDataUsuarioRepository usuarioRepository
    ) {
        UUID organizacionId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        UUID gestorId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        OrganizacionEntity organizacion = organizacionRepository.save(
                new OrganizacionEntity(organizacionId, "Banco B"));
        EquipoEntity equipo = equipoRepository.save(
                new EquipoEntity(equipoId, "Soporte", organizacion));
        usuarioRepository.save(new UsuarioEntity(
                clienteId, "cliente@bancob.test", PASSWORD_HASH, Rol.CLIENTE, organizacion, null));
        usuarioRepository.save(new UsuarioEntity(
                gestorId, "gestor@bancob.test", PASSWORD_HASH, Rol.GESTOR, organizacion, equipo));

        return new DatosOrganizacion(organizacionId, equipoId, clienteId, gestorId, null, CATEGORIA_ACCESOS);
    }
}
