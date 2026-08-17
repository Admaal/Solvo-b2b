# Despliegue de la API en Cloud Run. El frontend va en Vercel (NG_APP_API_URL).
param(
    [Parameter(Mandatory = $true)]
    [string]$GcpProjectId,

    [Parameter(Mandatory = $true)]
    [string]$GcpRegion,

    [Parameter(Mandatory = $true)]
    [string]$CorsOrigin
)

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
$Image = "$GcpRegion-docker.pkg.dev/$GcpProjectId/helpdesk/helpdesk-backend:latest"

Write-Host "==> Build imagen Alpine..."
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
    --set-env-vars "SPRING_PROFILES_ACTIVE=prod,HELPDESK_DEMO_DATA=true,CORS_ALLOWED_ORIGINS=$CorsOrigin" `
    --set-secrets "HELPDESK_RUNTIME_JSON=helpdesk-runtime:latest"

$ServiceUrl = gcloud run services describe helpdesk-api `
    --region $GcpRegion `
    --format "value(status.url)"

Write-Host ""
Write-Host "API desplegada: $ServiceUrl"
Write-Host "Siguiente paso (Vercel):"
Write-Host "  1. Root Directory = frontend"
Write-Host "  2. Env NG_APP_API_URL = $ServiceUrl"
Write-Host "  3. Redeploy. CORS ya apunta a $CorsOrigin"
Write-Host "Seed demo: HELPDESK_DEMO_DATA=true (solo si la BD está vacía)."
