# Despliegue en Google Cloud Run

La demo pública de la **API** corre en **Cloud Run** (`min-instances=0`) para coste $0 en reposo.
El frontend se sirve en **Vercel** y llama a esta API por URL absoluta.
Kubernetes (Helm) se usa bajo demanda en clúster local para demostraciones.

Haz esto **después** de tener el código auditado (fases 1–3) y las tablas creadas en Supabase.

## Prerrequisitos GCP

1. Proyecto GCP con facturación (free tier / créditos).
2. APIs habilitadas: Cloud Run, Artifact Registry, Secret Manager.
3. Repositorio Artifact Registry `helpdesk` en la región elegida.
4. **Un solo secret** en Secret Manager: `helpdesk-runtime` con JSON:

```json
{
  "DATABASE_URL": "jdbc:postgresql://HOST:5432/postgres?sslmode=require",
  "DATABASE_USERNAME": "postgres",
  "DATABASE_PASSWORD": "tu-password-supabase",
  "JWT_SECRET": "tu-jwt-secret-minimo-32-caracteres"
}
```

`JWT_SECRET` debe tener ≥ 32 caracteres y **no** coincidir con el default de `application.properties`.

## Secrets de GitHub (Settings → Secrets)

| Secret | Ejemplo |
|--------|---------|
| `GCP_PROJECT_ID` | `mi-proyecto-helpdesk` |
| `GCP_REGION` | `europe-southwest1` |
| `GCP_SA_KEY` | JSON de cuenta de servicio con roles Run Admin + Artifact Registry Writer + Secret Manager Accessor |

## Variable de repositorio

| Variable | Ejemplo |
|----------|---------|
| `CORS_ALLOWED_ORIGINS` | `https://tu-app.vercel.app` |

## Imagen Docker (Artifact Registry)

La imagen usa **JRE Alpine + fat JAR** (~210 MB). El free tier de Artifact Registry (~512 MB) cabe 2 tags si borras digests antiguos. No persigas 100 MB: no merece la pena.

```bash
docker build -t helpdesk-backend:local backend
docker images helpdesk-backend:local
```

**Cuota free tier (~512 MB):** conserva solo `latest` + 1 digest reciente. Borra tags antiguos:

```bash
gcloud artifacts docker images list REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend
gcloud artifacts docker images delete IMAGE@DIGEST --quiet
```

## Tablas en Supabase (obligatorio antes de `validate`)

Prod usa `spring.jpa.hibernate.ddl-auto=validate`. Crea el esquema con [docs/supabase-schema.sql](../../docs/supabase-schema.sql) o un arranque único local contra Supabase con perfil `local`.

## Despliegue manual

```bash
# Build y push
gcloud auth configure-docker REGION-docker.pkg.dev
docker build -t REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest backend
docker push REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest

# Deploy (un solo secret + seed de portfolio)
gcloud run deploy helpdesk-api \
  --image REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest \
  --region REGION \
  --allow-unauthenticated \
  --min-instances 0 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,HELPDESK_DEMO_DATA=true,CORS_ALLOWED_ORIGINS=https://TU-PROYECTO.vercel.app \
  --set-secrets HELPDESK_RUNTIME_JSON=helpdesk-runtime:latest
```

Asistido: `.\scripts\deploy-demo.ps1 -GcpProjectId ... -GcpRegion ... -CorsOrigin https://tu-app.vercel.app`

CI en `main` despliega igual si existen los secrets GCP; si no, el job se omite.

## Seed de datos demo

Al arrancar contra una **BD vacía** con `HELPDESK_DEMO_DATA=true`, `DemoDataLoader` siembra 9 tickets + comentarios.
Sin el flag, prod no siembra. Si ya hay tickets, no se re-siembra.

Swagger/OpenAPI y actuator (salvo `health`) no están expuestos en prod.

## Frontend (Vercel)

No hay rewrite `/api` en Vercel. El build inyecta `NG_APP_API_URL` (URL de este servicio Cloud Run) en `environment.ts`.

Ver [docs/SETUP-GITHUB.md](../../docs/SETUP-GITHUB.md) sección 8.
