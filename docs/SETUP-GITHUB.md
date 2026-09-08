# Guía de configuración del repositorio

Checklist para publicar el proyecto y activar CI. Hazlo **después** de crear el repo en GitHub.

## 1. Subir el código

```powershell
git init
git add .
git commit -m "feat: helpdesk B2B — portfolio listo"
git branch -M main
git remote add origin https://github.com/Admaal/Solvo-b2b.git
git push -u origin main
```

## 2. GitHub Actions (automático)

El workflow `.github/workflows/ci.yml` se ejecuta en cada push/PR a `main`:

- Tests backend (unitarios + PIT + Testcontainers)
- Tests frontend (Jasmine + build)
- Helm lint + kubeconform
- Docker build (imagen slim, **sin** push a un registry)

No hace falta ningún secret de cloud para que CI quede en verde.

El workflow `.github/workflows/keep-alive.yml` pinea `/actuator/health` cada 5 minutos **solo** si existe la variable `DEMO_API_HEALTH_URL`.

## 3. Variable de repositorio (keep-alive)

En **Settings → Secrets and variables → Actions → Variables** (después de tener la URL de Render):

| Variable | Ejemplo |
|----------|---------|
| `DEMO_API_HEALTH_URL` | `https://helpdesk-api-vq5a.onrender.com/actuator/health` |

Sin esta variable el cron termina en éxito y no pinea nada.

## 4. Supabase (base de datos de la demo)

Proyecto ya creado: **solvo-b2b-helpdesk** (`bhdglymhjmsjlcgbxnbg`, `eu-west-1`).
Dashboard: https://supabase.com/dashboard/project/bhdglymhjmsjlcgbxnbg
El esquema JPA + RLS ya está aplicado. No crees otro proyecto.

1. La contraseña de `postgres` **no se puede ver** después de crear el proyecto (Supabase no la guarda en claro). Hay que **resetearla**: [Database Settings](https://supabase.com/dashboard/project/bhdglymhjmsjlcgbxnbg/database/settings) → sección **Database password** → **Reset password**. El pooler no acepta el rol JDBC `helpdesk_app`; usa `postgres.bhdglymhjmsjlcgbxnbg`.
2. Connection string del **Session pooler**, puerto **5432** (no el pooler de transacciones 6543: Hibernate no encaja).
   - Host: `aws-1-eu-west-1.pooler.supabase.com` (no `aws-0`: este proyecto está en el cluster aws-1; el otro responde tenant not found)
   - User: `postgres.bhdglymhjmsjlcgbxnbg`
   - Database: `postgres`
3. **Antes** del primer Render con `ddl-auto=validate`: crear tablas.
   - Opción A: ejecutar [docs/supabase-schema.sql](supabase-schema.sql) en el SQL Editor.
   - Opción B: arrancar el backend **local** apuntando a Supabase con perfil `local` (`ddl-auto=update`) una sola vez.
4. En prod Render usa `validate` — las tablas deben existir antes del deploy.

Si la BD ya tenía esquema sin `password_hash`, añade la columna o recrea el proyecto.

### Seed demo para reclutadores

`DemoDataLoader` siembra **9 tickets** + comentarios solo si `tickets` está vacío **y** `HELPDESK_DEMO_DATA=true`.
Render de demo pasa ese flag. Un segundo deploy **no** duplica filas. Para ver el seed de nuevo: truncar tablas o proyecto nuevo, luego redesplegar.

## 5. Render (API Java)

1. Cuenta en [Render](https://render.com). Plan **free** (duerme ~15 min; el keep-alive de GitHub lo mitiga).
2. **New → Blueprint** y conectar este repo, o **New Web Service** con:
   - Root directory: `backend`
   - Runtime: Docker (`Dockerfile` en `backend/`)
   - Health check: `/actuator/health`
   - Instance: Free
3. El archivo [render.yaml](../render.yaml) declara el servicio `helpdesk-api`. Las variables `sync: false` se piden en el dashboard:

| Variable | Valor |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` (ya en el blueprint) |
| `HELPDESK_DEMO_DATA` | `true` (ya en el blueprint) |
| `DATABASE_URL` | `jdbc:postgresql://HOST:5432/postgres?sslmode=require` (Session pooler) |
| `DATABASE_USERNAME` | `postgres` (o el user del pooler) |
| `DATABASE_PASSWORD` | password de Supabase |
| `JWT_SECRET` | ≥ 32 caracteres, **distinto** del default de `application.properties` |
| `CORS_ALLOWED_ORIGINS` | `https://frontend-rouge-rho-30.vercel.app` |

Render inyecta `PORT`; Spring ya usa `server.port=${PORT:8080}`.

`JWT_SECRET` no puede ser el valor por defecto: en `prod` el arranque falla a propósito (`ProdJwtSecretGuard`).

El primer arranque de la JVM puede tardar 60–90 s; configura un health check con gracia alta si Render marca el deploy como fallido.

Copia la URL pública (`https://….onrender.com`) para Vercel y para `DEMO_API_HEALTH_URL`.

## 6. Vercel (frontend) — después de tener URL de Render

El SPA **no** hace proxy `/api` en producción: llama a Render por URL absoluta.

1. Importar el repo en Vercel.
2. **Root Directory:** `frontend`
3. El `vercel.json` ya define rewrite SPA → `index.html` y el build (`node set-api-url.cjs && npm run build`).
4. Variable de entorno de **build**:

| Variable | Valor |
|----------|-------|
| `NG_APP_API_URL` | `https://helpdesk-api-vq5a.onrender.com` |

5. Deploy. `CORS_ALLOWED_ORIGINS` en Render ya apunta a `https://frontend-rouge-rho-30.vercel.app`.

Demo pública: https://frontend-rouge-rho-30.vercel.app  
API: https://helpdesk-api-vq5a.onrender.com

El CLI no debe subir `node_modules` (límite 100 MB): hay `frontend/.vercelignore`. El root directory del proyecto Vercel es `frontend`.

JDBC al Session pooler necesita `channelBinding=disable` y `gssEncMode=disable` en `DATABASE_URL`; si no, PgJDBC falla con `password authentication failed` aunque la clave sea correcta.

No despliegues Vercel a producción sin `NG_APP_API_URL`: el build local por defecto usa `/api/v1` (solo válido detrás del proxy de `ng serve`).

## 7. Reset BD local (ver seed nuevo)

```powershell
docker compose down -v
docker compose up -d
cd backend
.\mvnw.cmd spring-boot:run
```

O: `.\scripts\reset-local-db.ps1`.

## 8. Verificar CI

Tras el primer push, revisa la pestaña **Actions** en GitHub. Todo debe estar en verde, incluido PIT. El job de keep-alive puede quedar en skip hasta configurar `DEMO_API_HEALTH_URL`.

## 9. Capturas para portfolio

Con la app local (`:4200` y `:8080`):

```powershell
.\scripts\capture-screenshots.ps1
```

Guarda PNG en `docs/screenshots/` (ver `docs/PORTFOLIO.md`).

## 10. Kubernetes (opcional, no es la demo pública)

Helm + kind bajo demanda: `.\scripts\helm-lint.ps1` y `.\scripts\kind-deploy.ps1`. No hace falta un clúster 24/7.
