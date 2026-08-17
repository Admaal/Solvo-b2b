# Solvo-B2B Helpdesk — Guía para agentes

## Spec

El documento de diseño (`proyecto-enterprise-java-angular.md`) es la especificación.

## Fase actual: 6 (pulido y portfolio) — COMPLETA

Todas las fases del roadmap (1–6) están implementadas.

## Gates mecánicos

| Gate | Comando |
|------|---------|
| Rápido (backend) | `backend\mvnw.cmd test` |
| Rápido (frontend) | `frontend\npm test -- --watch=false --browsers=ChromeHeadless` |
| Completo | `verify` + PIT + `frontend\npm run build` |
| Infra | `scripts\helm-lint.ps1` |

> `npm test` sin flags deja Karma en modo watch y no termina; usar el comando headless del gate rápido frontend.

## Testing verificado (no solo “existe”)

### Backend — COMPLETO

| Capa | Resultado verificado |
|------|----------------------|
| Unitarios + ArchUnit | 59/59 (`mvnw test`) |
| PIT | 78% mutaciones matadas (≥ 75%; también en CI) |
| Integración (`*IT.java`) | 6/6 ejecutados, 0 skipped (`mvnw verify`) |

**Testcontainers:** requiere el **daemon de Docker** (no basta `docker compose up` del Postgres local). Los IT usan `@ActiveProfiles("integration-test")`, `application-integration-test.properties` (JPA activo) y `testcontainers.version` **1.21.4** en `pom.xml`. Criterio de cierre: `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` en `TicketApiIntegracionIT` + `TicketFlujoIntegracionIT` + `AislamientoTenantIT`.

### Frontend — COMPLETO

| Ámbito | Resultado verificado |
|--------|----------------------|
| Jasmine/Karma | **33/33** SUCCESS (headless) |
| Specs | 8 archivos `.spec.ts` — auth, app shell, tickets (list/detail/create), admin-placeholder |

Cada componente de feature tiene spec con mocks de servicios y asserts de comportamiento (no solo `should create`). Criterio de cierre: `TOTAL: 33 SUCCESS` tras el gate rápido frontend.

## Invariantes

- `domain/` y `application/` no importan Spring ni JPA.
- Transiciones de estado solo en dominio.
- PIT ≥ 75% en domain + application.

## Arranque local

```powershell
docker compose up -d
cd backend; .\mvnw.cmd spring-boot:run
cd frontend; npm start
```

Usuarios demo: `cliente@bancoa.demo`, `gestor@bancoa.demo`, `admin@bancoa.demo` (contraseña `demo`)

## Documentación

- `docs/PORTFOLIO.md` — narrativa para reclutadores
- `docs/SETUP-GITHUB.md` — checklist repo + GCP + Vercel
- `deploy/cloudrun/README.md` — detalle Cloud Run
- `docs/supabase-schema.sql` — tablas para prod (`ddl-auto=validate`)
