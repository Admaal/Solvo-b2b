# Despliegue en Google Cloud Run

La demo pública corre en **Cloud Run** (`min-instances=0`) para coste $0 en reposo.
Kubernetes (Helm) se usa bajo demanda en clúster local para demostraciones.

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

## Secrets de GitHub (Settings → Secrets)

| Secret | Ejemplo |
|--------|---------|
| `GCP_PROJECT_ID` | `mi-proyecto-helpdesk` |
| `GCP_REGION` | `europe-southwest1` |
| `GCP_SA_KEY` | JSON de cuenta de servicio con roles Run Admin + Artifact Registry Writer + Secret Manager Accessor |

## Variable de repositorio

| Variable | Ejemplo |
|----------|---------|
| `CORS_ALLOWED_ORIGINS` | `https://helpdesk-demo.web.app` |

## Imagen Docker (Artifact Registry)

La imagen usa **JRE Alpine + fat JAR** (~210 MB). El free tier de Artifact Registry (~512 MB) cabe 2 tags si borras digests antiguos.

```bash
docker build -t helpdesk-backend:local backend
docker images helpdesk-backend:local
```

**Cuota free tier (~512 MB):** conserva solo `latest` + 1 digest reciente. Borra tags antiguos:

```bash
gcloud artifacts docker images list REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend
gcloud artifacts docker images delete IMAGE@DIGEST --quiet
```

## Despliegue manual

```bash
# Build y push
gcloud auth configure-docker REGION-docker.pkg.dev
docker build -t REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest backend
docker push REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest

# Deploy (un solo secret)
gcloud run deploy helpdesk-api \
  --image REGION-docker.pkg.dev/PROJECT/helpdesk/helpdesk-backend:latest \
  --region REGION \
  --allow-unauthenticated \
  --min-instances 0 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,CORS_ALLOWED_ORIGINS=https://TU-PROYECTO.web.app \
  --set-secrets HELPDESK_RUNTIME_JSON=helpdesk-runtime:latest
```

## Seed de datos demo

Al arrancar contra una **BD vacía**, `DemoDataLoader` siembra 9 tickets en todos los estados + comentarios.
Si ya hay tickets, no se re-siembra. Para resetear Supabase: truncar tablas o recrear el proyecto.

## Frontend (Firebase Hosting)

El frontend se despliega por separado en Firebase Hosting (estático).
`firebase.json` reescribe `/api/**` hacia Cloud Run.

```bash
cd frontend
npm run build
firebase deploy --only hosting
```

Ver también: [docs/SETUP-GITHUB.md](../../docs/SETUP-GITHUB.md)
