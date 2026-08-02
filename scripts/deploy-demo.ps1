# Despliegue demo: Firebase Hosting + Cloud Run (requiere gcloud y firebase CLI autenticados)
param(
    [Parameter(Mandatory = $true)]
    [string]$GcpProjectId,

    [Parameter(Mandatory = $true)]
    [string]$GcpRegion,

    [string]$CorsOrigin = "https://TU_PROYECTO.web.app"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
$Image = "$GcpRegion-docker.pkg.dev/$GcpProjectId/helpdesk/helpdesk-backend:latest"

Write-Host "==> Build imagen slim..."
docker build -t $Image "$Root\backend"

Write-Host "==> Push Artifact Registry..."
gcloud auth configure-docker "$GcpRegion-docker.pkg.dev" --quiet
docker push $Image

Write-Host "==> Deploy Cloud Run..."
gcloud run deploy helpdesk-api `
    --image $Image `
    --region $GcpRegion `
    --platform managed `
    --allow-unauthenticated `
    --min-instances 0 `
    --max-instances 3 `
    --memory 512Mi `
    --cpu 1 `
    --port 8080 `
    --set-env-vars "SPRING_PROFILES_ACTIVE=prod,CORS_ALLOWED_ORIGINS=$CorsOrigin" `
    --set-secrets "HELPDESK_RUNTIME_JSON=helpdesk-runtime:latest"

Write-Host "==> Build frontend..."
Push-Location "$Root\frontend"
npm run build

Write-Host "==> Deploy Firebase Hosting..."
firebase deploy --only hosting
Pop-Location

Write-Host "Demo desplegada. Verifica seed en Supabase (BD vacía en primer arranque)."
