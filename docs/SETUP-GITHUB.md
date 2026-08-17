# Guía de configuración del repositorio

Checklist para publicar el proyecto y activar CI/CD. Hazlo **después** de crear el repo en GitHub.

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
- Docker build (imagen slim)
- Deploy Cloud Run (solo si hay secrets GCP)

## 3. Secrets de GitHub

En **Settings → Secrets and variables → Actions**:

| Secret | Descripción |
|--------|-------------|
| `GCP_PROJECT_ID` | ID del proyecto GCP |
| `GCP_REGION` | ej. `europe-southwest1` |
| `GCP_SA_KEY` | JSON de cuenta de servicio |

### Permisos de la cuenta de servicio

- Cloud Run Admin
- Artifact Registry Writer
- Secret Manager Secret Accessor

## 4. Variable de repositorio

En **Settings → Secrets and variables → Actions → Variables**:

| Variable | Ejemplo |
|----------|---------|
| `CORS_ALLOWED_ORIGINS` | `https://tu-app.vercel.app` |

Puedes poner primero la URL de preview de Vercel y ajustarla cuando exista el dominio de producción.

## 5. Google Cloud (una sola vez)

```bash
# APIs
gcloud services enable run.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com

# Artifact Registry
gcloud artifacts repositories create helpdesk \
  --repository-format=docker \
  --location=REGION

# Un solo secret JSON (free tier Secret Manager)
cat > runtime.json <<'EOF'
{
  "DATABASE_URL": "jdbc:postgresql://HOST:5432/postgres?sslmode=require",
  "DATABASE_USERNAME": "postgres",
  "DATABASE_PASSWORD": "TU_PASSWORD_SUPABASE",
  "JWT_SECRET": "tu-jwt-secret-minimo-32-caracteres"
}
EOF
gcloud secrets create helpdesk-runtime --data-file=runtime.json
rm runtime.json
```

`JWT_SECRET` no puede ser el valor por defecto de `application.properties`: en `prod` el arranque falla a propósito.

Detalle completo: [deploy/cloudrun/README.md](../deploy/cloudrun/README.md)

## 6. Supabase (base de datos prod)

1. Crear proyecto nuevo (free tier).
2. Copiar connection string (Session pooler, puerto 5432).
3. **Antes de Cloud Run con `ddl-auto=validate`:** crear tablas.
   - Opción A: ejecutar [docs/supabase-schema.sql](supabase-schema.sql) en el SQL Editor.
   - Opción B: arrancar el backend **local** apuntando a Supabase con perfil `local` (`ddl-auto=update`) una sola vez.
4. En prod Cloud Run usa `validate` — las tablas deben existir antes del deploy.

Si la BD ya tenía esquema sin `password_hash`, añade la columna o recrea el proyecto.

### Seed demo para reclutadores

`DemoDataLoader` siembra **9 tickets** + comentarios solo si `tickets` está vacío **y** `HELPDESK_DEMO_DATA=true`.
CI/Cloud Run de demo pasan ese flag. Para ver el seed en Supabase: truncar tablas o proyecto nuevo, luego desplegar.

## 7. Reset BD local (ver seed nuevo)

```powershell
docker compose down -v
docker compose up -d
cd backend
.\mvnw.cmd spring-boot:run
```

## 8. Vercel (frontend) — después de tener URL de Cloud Run

El SPA **no** hace proxy `/api` en producción: llama a Cloud Run por URL absoluta.

1. Importar el repo en Vercel.
2. **Root Directory:** `frontend`
3. El `vercel.json` ya define rewrite SPA → `index.html` y el build (`node set-api-url.cjs && npm run build`).
4. Variable de entorno de **build**:

| Variable | Valor |
|----------|-------|
| `NG_APP_API_URL` | URL de Cloud Run, p. ej. `https://helpdesk-api-xxxxx.run.app` |

5. Deploy. Actualiza `CORS_ALLOWED_ORIGINS` (GitHub variable + Cloud Run) con `https://tu-proyecto.vercel.app`.

No despliegues Vercel a producción sin `NG_APP_API_URL`: el build local por defecto usa `/api/v1` (solo válido detrás del proxy de `ng serve`).

## 9. Verificar CI

Tras el primer push, revisa la pestaña **Actions** en GitHub. Todo debe estar en verde, incluido PIT.

## 10. Capturas para portfolio

Con la app local o desplegada:

```powershell
.\scripts\capture-screenshots.ps1
```

Guarda PNG en `docs/screenshots/` (ver `docs/PORTFOLIO.md`).
