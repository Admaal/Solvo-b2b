-- Esquema alineado con las entidades JPA (Hibernate 6 / PostgreSQL).
-- Ejecutar en el SQL Editor de Supabase ANTES del primer Render con ddl-auto=validate.
-- Alternativa: arrancar una vez el backend local contra Supabase con perfil `local`
-- (ddl-auto=update) y después dejar prod en validate.

CREATE TABLE IF NOT EXISTS organizaciones (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS equipos (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    organizacion_id UUID NOT NULL REFERENCES organizaciones (id)
);

CREATE TABLE IF NOT EXISTS categorias (
    codigo VARCHAR(50) PRIMARY KEY,
    prioridad_por_defecto VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(255) NOT NULL,
    organizacion_id UUID NOT NULL REFERENCES organizaciones (id),
    equipo_id UUID REFERENCES equipos (id)
);

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    asunto VARCHAR(255) NOT NULL,
    descripcion VARCHAR(4000) NOT NULL,
    estado VARCHAR(255) NOT NULL,
    prioridad VARCHAR(255) NOT NULL,
    categoria_codigo VARCHAR(50) NOT NULL REFERENCES categorias (codigo),
    organizacion_id UUID NOT NULL REFERENCES organizaciones (id),
    cliente_id UUID NOT NULL REFERENCES usuarios (id),
    agente_asignado_id UUID REFERENCES usuarios (id),
    creado_en TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS comentarios_ticket (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets (id),
    autor_id UUID NOT NULL REFERENCES usuarios (id),
    texto VARCHAR(2000) NOT NULL,
    creado_en TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_equipos_organizacion_id ON equipos (organizacion_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_organizacion_id ON usuarios (organizacion_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_equipo_id ON usuarios (equipo_id);
CREATE INDEX IF NOT EXISTS idx_tickets_organizacion_id ON tickets (organizacion_id);
CREATE INDEX IF NOT EXISTS idx_tickets_cliente_id ON tickets (cliente_id);
CREATE INDEX IF NOT EXISTS idx_tickets_agente_asignado_id ON tickets (agente_asignado_id);
CREATE INDEX IF NOT EXISTS idx_tickets_categoria_codigo ON tickets (categoria_codigo);
CREATE INDEX IF NOT EXISTS idx_comentarios_ticket_id ON comentarios_ticket (ticket_id);
CREATE INDEX IF NOT EXISTS idx_comentarios_autor_id ON comentarios_ticket (autor_id);

-- Data API (anon/authenticated) queda denegado. El JDBC `helpdesk_app` tiene políticas FOR ALL.
-- El rol `postgres` tiene BYPASSRLS.
ALTER TABLE organizaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE equipos ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;
ALTER TABLE comentarios_ticket ENABLE ROW LEVEL SECURITY;

CREATE POLICY helpdesk_app_all ON organizaciones FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
CREATE POLICY helpdesk_app_all ON equipos FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
CREATE POLICY helpdesk_app_all ON categorias FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
CREATE POLICY helpdesk_app_all ON usuarios FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
CREATE POLICY helpdesk_app_all ON tickets FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
CREATE POLICY helpdesk_app_all ON comentarios_ticket FOR ALL TO helpdesk_app USING (true) WITH CHECK (true);
