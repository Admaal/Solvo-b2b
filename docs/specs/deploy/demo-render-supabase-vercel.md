---
id: demo-render-supabase-vercel
status: active
confirmed: true
---

# Spec: Demo pública Render + Supabase + Vercel

> `confirmed: true` tras el «sí» de implementación del plan (spec + keep-alive cada 5 min).

## Contexto y problema

Un reclutador no puede evaluar el helpdesk sin instalar Java, Docker y Angular. La guía de go-live apuntaba a Cloud Run, que no se va a usar.

## Resultado deseado

Demo pública: SPA en Vercel, API Java en Render, Postgres en Supabase. Login de los tres roles demo sin backend local.

## Historia de usuario

Como **reclutador**, quiero **abrir una URL, entrar como cliente/gestor/admin y ver tickets reales**, para **evaluar el proyecto sin instalar nada**.

## Alcance

- Incluido: API en Render (Dockerfile existente), BD Supabase, front en Vercel.
- Incluido: keep-alive GET `/actuator/health` cada 5 min (GitHub Actions `schedule`).
- Incluido: seed demo si la BD está vacía (`HELPDESK_DEMO_DATA=true`).
- Incluido: quitar el deploy a Cloud Run del CI y de la guía de go-live.
- Incluido: README/PORTFOLIO/SETUP con el stack de demo; Helm solo como K8s local.
- Incluido: capturas reales y copy de `/admin` alineada con auth `demo`.

## Criterios de aceptación (EARS)

- [x] **AC-01:** WHEN un visitante abre la URL de Vercel THE SPA SHALL cargar el login demo sin backend local.
- [ ] **AC-02:** WHEN elige “Entrar como Cliente/Gestor/Admin” THE system SHALL autenticar con email + `demo` y mostrar el listado del rol.
- [ ] **AC-03:** WHEN el gestor cambia un estado válido THE system SHALL persistirlo en Supabase y reflejarlo al recargar.
- [x] **AC-04:** IF `JWT_SECRET` falta, es el default o tiene menos de 32 caracteres THEN THE API en `prod` SHALL no arrancar.
- [x] **AC-05:** THE CORS de prod SHALL aceptar solo el origen de Vercel (y no `*`).
- [ ] **AC-06:** WHILE no hay tráfico humano THE keep-alive SHALL llamar a `/actuator/health` al menos cada 5 minutos.
- [x] **AC-07:** THE job de CI en `main` SHALL pasar tests/PIT/IT/Helm/Docker **sin** secrets GCP.
- [x] **AC-08:** THE README SHALL mostrar capturas reales y enlaces al repo y a la guía de demo, sin mencionar Cloud Run como destino de la demo.

## Casos límite y errores

- `DEMO_API_HEALTH_URL` ausente → el workflow de keep-alive termina en éxito y omite el ping.
- Redeploy de la API con tickets ya sembrados → el seed no duplica filas.
- `NG_APP_API_URL` ausente en Vercel → el build falla a propósito.

## Fuera de alcance

- Reescribir Java en Edge Functions de Supabase.
- CRUD de admin, GKE, rate limit de login, Flyway, E2E Playwright en CI.

## Riesgos nuevos de esta fase

- **¿Toca datos personales de alguien que no sea el propio usuario?** No.
  Test de aislamiento con dos identidades: no aplica (ya existe `AislamientoTenantIT`). Gate: no aplica.
- **¿Escribe algo que pueda repetirse por reintento, reconexión o doble tap?** Sí — el seed.
  Test de idempotencia/no duplicación: el loader solo corre si `tickets` está vacío; verificación manual en go-live (9 tickets, no 18). Gate: go-live, no test nuevo.
- **¿Introduce un rol o permiso nuevo?** No. Test de lo que ese rol no puede
  hacer: no aplica. Gate: no aplica.
- **¿Toca dinero, cupos o algo limitado?** No. Validación en servidor y test de
  límite/carrera: no aplica. Gate: no aplica.
- **¿Acepta contenido que vaya a ver otra persona?** Sí, tickets/comentarios demo.
  Saneado/moderación y test: Angular escapa por interpolación; sin moderación extra. Gate: no aplica.
- **¿Cambia el esquema de datos de algo que ya tiene usuarios reales?** No.
  Migración reversible y test de upgrade/downgrade: proyecto Supabase nuevo + SQL existente. Gate: no aplica.
- **¿Añade un secreto o credencial nueva?** Sí. Comprobación de que no llega al
  cliente ni al repo: `JWT_SECRET`, password de Supabase y `NG_APP_API_URL` solo en paneles; `.env` en gitignore. Gate: revisión de diff en `stop`.
- **¿Añade tests, un gate nuevo o más trabajo a uno existente?** Sí. Medición y
  ajuste de presupuesto/timeout: workflow de keep-alive en `schedule` (no `afterFileEdit`). Gate: CI schedule.

## Trazabilidad

- Plan: demo pública Render + Supabase + Vercel
- Tests por criterio: `ProdJwtSecretGuard` (AC-04); `admin-placeholder.component.spec.ts` (copy demo); CI sin job GCP (AC-07)
- Evidencia adicional (navegador, contrato, migración): go-live en `docs/SETUP-GITHUB.md`
