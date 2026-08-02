# Solvo-B2B Helpdesk

[![CI](https://github.com/Admaal/Solvo-b2b/actions/workflows/ci.yml/badge.svg)](https://github.com/Admaal/Solvo-b2b/actions/workflows/ci.yml)

Sistema de gestión de incidencias corporativas — **Java 21 + Spring Boot + Angular 19**, arquitectura hexagonal, RBAC y despliegue consciente del coste.

> Pieza de portfolio orientada al segmento banca/consultora en España.  
> Documentación completa: [docs/PORTFOLIO.md](docs/PORTFOLIO.md)

## Demo

| Entorno | Cómo probar |
|---------|-------------|
| Local | Ver [Demo rápida](#demo-rápida-local) |
| Pública | Firebase + Cloud Run — [docs/SETUP-GITHUB.md](docs/SETUP-GITHUB.md) |

### Capturas

| Login demo | Listado tickets | Detalle + comentarios | Swagger |
|------------|-----------------|----------------------|---------|
| ![Login](docs/screenshots/01-login.png) | ![Grid](docs/screenshots/02-tickets-grid.png) | ![Detalle](docs/screenshots/03-ticket-detalle.png) | ![Swagger](docs/screenshots/04-swagger.png) |

> Generar capturas: `.\scripts\capture-screenshots.ps1` (app en `:4200` y `:8080`).

## Demo rápida (local)

```powershell
docker compose up -d
cd backend && .\mvnw.cmd spring-boot:run    # :8080
cd frontend && npm start                     # :4200
```

| Rol | Email | Auth |
|-----|-------|------|
| Cliente | `cliente@bancoa.demo` | Sin contraseña (demo) |
| Gestor | `gestor@bancoa.demo` | Sin contraseña (demo) |
| Admin | `admin@bancoa.demo` | Sin contraseña (demo) |

Al primer arranque con BD vacía se siembran **9 tickets** en todos los estados + comentarios demo.

**Ver seed nuevo:** `.\scripts\reset-local-db.ps1` y reiniciar backend.

## Qué demuestra

- Arquitectura hexagonal con dominio Java puro (sin Spring en `domain/`)
- Máquina de estados en dominio — transiciones inválidas rechazadas en backend
- RBAC navegable (cliente vs gestor vs admin)
- Comentarios en tickets (hilo cliente/gestor)
- Grid Angular con paginación server-side real
- JWT + interceptores + guards
- Tests: ArchUnit, PIT ≥75%, Testcontainers, Jasmine
- Imagen Docker Alpine JRE + layered build cache en CI (~210 MB; limpiar digests viejos en Artifact Registry)
- Helm chart K8s + CI/CD GitHub Actions + Cloud Run

## Stack

`Java 21` · `Spring Boot 3.4` · `PostgreSQL` · `Angular 19` · `Material` · `Docker` · `Helm` · `GitHub Actions` · `Cloud Run`

## Estructura

```
backend/          # API REST hexagonal
frontend/         # Angular standalone + Signals
deploy/helm/      # Chart Kubernetes (kind bajo demanda)
.github/          # CI + deploy Cloud Run
docs/             # Portfolio, setup GitHub, screenshots
scripts/          # reset BD, capturas, deploy demo
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
| Frontend | Firebase Hosting |
| BD prod | Supabase |
| K8s (portfolio) | Helm + kind (sin clúster 24/7) |

**Configurar repo y GCP:** [docs/SETUP-GITHUB.md](docs/SETUP-GITHUB.md)  
**Deploy asistido:** `.\scripts\deploy-demo.ps1 -GcpProjectId ... -GcpRegion ...`

## Licencia

[MIT](LICENSE) — proyecto de portfolio.
