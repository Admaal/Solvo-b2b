# Solvo-B2B Helpdesk — Portfolio

Sistema de gestión de incidencias B2B orientado al segmento **banca/consultora** en España.
Demuestra arquitectura hexagonal real, RBAC navegable, comentarios en tickets, API REST documentada y despliegue consciente del coste.

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Java 21, Spring Boot 3.4, JPA, Spring Security + JWT |
| Frontend | Angular 19, Standalone, Signals, Angular Material |
| Base de datos | PostgreSQL (local Docker / Supabase en prod) |
| Tests | JUnit 5, Mockito, ArchUnit, PIT, Testcontainers, Jasmine |
| Infra | Docker slim, Helm, GitHub Actions, Cloud Run, Vercel |

## Arquitectura hexagonal

```
┌─────────────────────────────────────────────────────────┐
│  Angular (features: auth, tickets, admin)               │
│  JWT interceptor · Guards por rol                         │
└────────────────────────┬────────────────────────────────┘
                         │ REST /api/v1
┌────────────────────────▼────────────────────────────────┐
│  infrastructure/web     ← controladores, DTOs           │
│  infrastructure/security← JWT filter                    │
│  infrastructure/persistence ← adaptadores JPA           │
├─────────────────────────────────────────────────────────┤
│  application/usecases   ← CrearTicket, AgregarComentario… │
│  application/ports      ← interfaces                    │
├─────────────────────────────────────────────────────────┤
│  domain/                ← Ticket, ComentarioTicket        │
│  (Java puro — sin Spring)                               │
└─────────────────────────────────────────────────────────┘
```

**Regla de oro:** las transiciones de estado de `Ticket` se validan en el dominio, no en el frontend ni en controladores.

## RBAC demostrable

| Rol | Capacidades en la UI |
|-----|----------------------|
| Cliente | Crear tickets, ver solo los suyos, comentar en sus tickets |
| Gestor | Ver todos, asignarse, cambiar estado, comentar |
| Administrador | Igual que gestor + ruta `/admin` (RBAC demo, sin CRUD) |

**Auth demo:** email + contraseña `demo` (un clic en la UI). No es auth bancaria; sí impide entrar como admin solo con el email. Respuesta de login genérica (401, sin enumerar usuarios).

**Aislamiento de tenant:** `organizacionId` y `clienteId` al crear salen del JWT. Cambiar estado y asignar exigen que el solicitante pertenezca a la organización del ticket.

**JWT en `localStorage`:** decisión consciente de SPA demo. Cookie HttpOnly rompería el flujo Angular simple; no es el modelo de un banco en producción.

## Calidad (valla mecánica)

- **ArchUnit** — `domain/` y `application/` no importan Spring/JPA
- **PIT** — mutation score ≥ 75% en dominio y aplicación (gate de CI)
- **Testcontainers** — flujo JPA + API REST + RBAC + aislamiento multi-org contra PostgreSQL real
- **Helm lint + kubeconform** — manifiestos K8s válidos en CI
- **Hooks Cursor** — gate rápido en cada edición Java

## Datos demo (local y prod)

Al arrancar el backend con BD vacía (`ticketRepository.count() == 0`) y `HELPDESK_DEMO_DATA=true`:

- 3 usuarios: `cliente@bancoa.demo`, `gestor@bancoa.demo`, `admin@bancoa.demo` (contraseña `demo`)
- **9 tickets** en estados ABIERTO, EN_PROGRESO, RESUELTO y CERRADO
- Categorías ACCESOS, PAGOS, TARJETAS, CONTRATOS (prioridades distintas)
- Comentarios demo en tickets en progreso

En **prod** el seed está apagado salvo `HELPDESK_DEMO_DATA=true` (flag explícito para el demo de portfolio). Swagger/OpenAPI no se publica en prod.

**Importante:** local (Docker Postgres) y prod (Supabase) son bases distintas. El seed solo corre en BD vacía. Reset local: `.\scripts\reset-local-db.ps1`.

## Capturas

En `docs/screenshots/`:

1. `01-login.png` — selector de rol demo
2. `02-tickets-grid.png` — grid con paginación y filtros
3. `03-ticket-detalle.png` — detalle + comentarios + SLA
4. `04-swagger.png` — Swagger UI (solo local)

Generar: `.\scripts\capture-screenshots.ps1`

## Decisiones de despliegue

| Entorno | Plataforma | Motivo |
|---------|------------|--------|
| Demo pública API | Cloud Run (`min-instances=0`) | Coste $0 en reposo |
| Frontend | Vercel | Estático SPA; llama a Cloud Run por URL absoluta |
| Portfolio K8s | Helm en `kind` bajo demanda | Competencia K8s sin GKE 24/7 |
| BD producción | Supabase free tier | Evita Cloud SQL de pago |
| Secretos | Un JSON en Secret Manager | Cuota free tier (1 secret) |
| Imagen Docker | JRE Alpine + fat JAR | ~210 MB; borrar digests antiguos en registry |

Orden de go-live: esquema Supabase → Cloud Run → Vercel con `NG_APP_API_URL`.

## Enlaces

- Repositorio: https://github.com/Admaal/Solvo-b2b
- Demo live: desplegar con [docs/SETUP-GITHUB.md](SETUP-GITHUB.md) o `scripts/deploy-demo.ps1` (API) + Vercel
- API Swagger: solo local — http://localhost:8080/swagger-ui.html
