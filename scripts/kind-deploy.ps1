param(
    [string]$ReleaseName = "helpdesk",
    [string]$ClusterName = "helpdesk-kind"
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

function Ensure-KindCluster {
    if (-not (Get-Command kind -ErrorAction SilentlyContinue)) {
        Write-Error "kind no está instalado. Ver https://kind.sigs.k8s.io/"
    }
    $exists = kind get clusters 2>$null | Select-String -Pattern "^$ClusterName$"
    if (-not $exists) {
        Write-Host "Creando clúster kind '$ClusterName'..." -ForegroundColor Yellow
        kind create cluster --name $ClusterName
    }
}

Write-Host "1. Build imagen Docker..." -ForegroundColor Cyan
docker build -t helpdesk-backend:local (Join-Path $root "backend")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "2. Clúster kind..." -ForegroundColor Cyan
Ensure-KindCluster
kind load docker-image helpdesk-backend:local --name $ClusterName

Write-Host "3. PostgreSQL (compose)..." -ForegroundColor Cyan
Push-Location $root
docker compose up -d postgres
Pop-Location

Write-Host "4. Helm install..." -ForegroundColor Cyan
helm upgrade --install $ReleaseName (Join-Path $root "deploy\helm\helpdesk") `
    -f (Join-Path $root "deploy\helm\helpdesk\values-local.yaml") `
    --set image.repository=helpdesk-backend `
    --set image.tag=local `
  --set secrets.databaseUrl=jdbc:postgresql://host.docker.internal:5432/helpdesk

Write-Host ""
Write-Host "Despliegue local listo. Port-forward:" -ForegroundColor Green
Write-Host "  kubectl port-forward svc/$ReleaseName-helpdesk 8080:80"
