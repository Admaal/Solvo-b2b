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

- Tests backend (unitarios + Testcontainers)
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
| `CORS_ALLOWED_ORIGINS` | `https://tu-app.web.app` |

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

Detalle completo: [deploy/cloudrun/README.md](../deploy/cloudrun/README.md)

## 6. Supabase (base de datos prod)

1. Crear proyecto nuevo (free tier).
2. Copiar connection string (Session pooler, puerto 5432).
3. **Primera vez:** arrancar el backend local apuntando a Supabase con `ddl-auto=update` (perfil default en local) para crear tablas, o usar SQL manual.
4. En prod Cloud Run usa `ddl-auto=validate` — las tablas deben existir antes del deploy.

### Seed demo para reclutadores

`DemoDataLoader` siembra **9 tickets** + comentarios solo si `tickets` está vacío.
Para ver el seed en Supabase: truncar tablas o proyecto nuevo, luego desplegar Cloud Run.

## 7. Reset BD local (ver seed nuevo)

```powershell
docker compose down -v
docker compose up -d
cd backend
.\mvnw.cmd spring-boot:run
```

## 8. Firebase Hosting (frontend)

```bash
cd frontend
cp .firebaserc.example .firebaserc   # editar projectId
npm run build
npm install -g firebase-tools
firebase login
firebase deploy --only hosting
```

`firebase.json` reescribe `/api/**` hacia Cloud Run — no hace falta hardcodear la URL del backend en producción.

## 9. Verificar CI

Tras el primer push, revisa la pestaña **Actions** en GitHub. Todo debe estar en verde.

## 10. Capturas para portfolio

Con la app local o desplegada:

```powershell
.\scripts\capture-screenshots.ps1
```

Guarda PNG en `docs/screenshots/` (ver `docs/PORTFOLIO.md`).
