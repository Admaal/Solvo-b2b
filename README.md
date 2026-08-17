# Solvo-B2B Helpdesk

[![CI](https://github.com/Admaal/Solvo-b2b/actions/workflows/ci.yml/badge.svg)](https://github.com/Admaal/Solvo-b2b/actions/workflows/ci.yml)

Sistema de gestión de incidencias corporativas — **Java 21 + Spring Boot + Angular 19**, arquitectura hexagonal, RBAC y despliegue consciente del coste.

> Pieza de portfolio orientada al segmento banca/consultora en España.  
> Documentación completa: [docs/PORTFOLIO.md](docs/PORTFOLIO.md)

## Demo

| Entorno | Cómo probar |
|---------|-------------|
| Local | Ver [Demo rápida](#demo-rápida-local) |
| Pública | Vercel + Cloud Run — [docs/SETUP-GITHUB.md](docs/SETUP-GITHUB.md) |

### Capturas

| Login demo | Listado tickets | Detalle + comentarios | Swagger |
|------------|-----------------|----------------------|---------|
| ![Login](docs/screenshots/01-login.png) | ![Grid](docs/screenshots/02-tickets-grid.png) | ![Detalle](docs/screenshots/03-ticket-detalle.png) | ![Swagger](docs/screenshots/04-swagger.png) |

> Generar capturas: `.\scripts\capture-screenshots.ps1` (app en `:4200` y `:8080`).  
> Swagger UI solo está activo fuera del perfil `prod` (local).

## Demo rápida (local)

```powershell
docker compose up -d
cd backend && .\mvnw.cmd spring-boot:run    # :8080 (perfil `local`, ddl-auto=update)
cd frontend && npm start                     # :4200 (proxy `/api` → backend)
```

| Rol | Email | Contraseña |
|-----|-------|------------|
| Cliente | `cliente@bancoa.demo` | `demo` |
| Gestor | `gestor@bancoa.demo` | `demo` |
| Admin | `admin@bancoa.demo` | `demo` |

Los botones «Entrar como…» envían email + contraseña en un clic. Auth de demo, no bancaria: evita el bypass total por email.

Al primer arranque con BD vacía se siembran **9 tickets** en todos los estados + comentarios demo.

**Ver seed nuevo:** `.\scripts\reset-local-db.ps1` y reiniciar backend.

El JWT se guarda en `localStorage` (decisión de SPA demo; no es cookie HttpOnly).

## Qué demuestra

- Arquitectura hexagonal con dominio Java puro (sin Spring en `domain/`)
- Máquina de estados en dominio — transiciones inválidas rechazadas en backend
- Aislamiento multi-tenant: el gestor de la org A no muta tickets de la org B
- RBAC navegable (cliente vs gestor vs admin)
- Comentarios en tickets (hilo cliente/gestor)
- Grid Angular con paginación server-side real
- JWT + interceptores + guards
- Tests: ArchUnit, PIT ≥75% (también en CI), Testcontainers, Jasmine
- Imagen Docker Alpine JRE + layered build cache en CI (~210 MB; limpiar digests viejos en Artifact Registry)
- Helm chart K8s + CI/CD GitHub Actions + Cloud Run + Vercel

## Stack

`Java 21` · `Spring Boot 3.4` · `PostgreSQL` · `Angular 19` · `Material` · `Docker` · `Helm` · `GitHub Actions` · `Cloud Run` · `Vercel`

## Estructura

```
backend/          # API REST hexagonal
frontend/         # Angular standalone + Signals (Vercel)
deploy/helm/      # Chart Kubernetes (kind bajo demanda)
.github/          # CI + deploy Cloud Run
docs/             # Portfolio, setup GitHub, screenshots
scripts/          # reset BD, capturas, deploy API
```

## Comandos

```powershell
.\backend\mvnw.cmd test              # unitarios + ArchUnit
.\backend\mvnw.cmd verify            # + Testcontainers
.\backend\mvnw.cmd pitest:mutationCoverage
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
cd frontend && npm run build
docker build -t helpdesk-backend:local backend
.\scripts\helm-lint.ps1
.\scripts\reset-local-db.ps1         # wipe Postgres local + seed fresco
```

## Despliegue

| Componente | Plataforma |
|------------|------------|
| API (demo) | Cloud Run `min-instances=0` |
| Frontend | Vercel (SPA; `apiUrl` absoluto a Cloud Run) |
| BD prod | Supabase |
| K8s (portfolio) | Helm + kind (sin clúster 24/7) |

Orden: tablas en Supabase → Cloud Run → Vercel con `NG_APP_API_URL`.  
**Configurar repo y GCP:** [docs/SETUP-GITHUB.md](docs/SETUP-GITHUB.md)  
**Deploy API asistido:** `.\scripts\deploy-demo.ps1 -GcpProjectId ... -GcpRegion ... -CorsOrigin https://tu-app.vercel.app`

## Licencia

[MIT](LICENSE) — proyecto de portfolio.
