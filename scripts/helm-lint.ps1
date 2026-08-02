param(
    [string]$ReleaseName = "helpdesk",
    [string]$Namespace = "default"
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$chart = Join-Path $root "deploy\helm\helpdesk"

Write-Host "Helm lint..." -ForegroundColor Cyan
helm lint $chart
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Helm template + kubeconform..." -ForegroundColor Cyan
$rendered = helm template $ReleaseName $chart -f (Join-Path $chart "values-local.yaml")
$rendered | Out-File -Encoding utf8 "$env:TEMP\helpdesk-manifests.yaml"

if (Get-Command kubeconform -ErrorAction SilentlyContinue) {
    Get-Content "$env:TEMP\helpdesk-manifests.yaml" | kubeconform -summary -ignore-missing-schemas
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} else {
    Write-Host "kubeconform no instalado — solo helm lint ejecutado." -ForegroundColor Yellow
    Write-Host "Instalar: https://github.com/yannh/kubeconform" -ForegroundColor Yellow
}

Write-Host "OK" -ForegroundColor Green
